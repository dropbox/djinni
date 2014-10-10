/**
  * Copyright 2014 Dropbox, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package djinni

import djinni.ast.Record.DerivingType
import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter

import scala.collection.mutable

class CppGenerator(spec: Spec) extends Generator(spec) {

  val writeCppFile = writeCppFileGeneric(spec.cppOutFolder.get, spec.cppNamespace, spec.cppFileIdentStyle, spec.cppIncludePrefix) _
  def writeHppFile(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit = (w => {})) =
    writeHppFileGeneric(spec.cppHeaderOutFolder.get, spec.cppNamespace, spec.cppFileIdentStyle)(name, origin, includes, fwds, f, f2)

  class CppRefs(name: String) {
    var hpp = mutable.TreeSet[String]()
    var hppFwds = mutable.TreeSet[String]()
    var cpp = mutable.TreeSet[String]()

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.map(find).mkString("<", ", ", ">")
      find(tm.base)
    }
    def find(m: Meta) = m match {
      case o: MOpaque =>
        o match {
          case p: MPrimitive =>
            val n = p.idlName
            if (n == "i8" || n == "i16" || n == "i32" || n == "i64") {
              hpp.add("#include <cstdint>")
            }
          case MString =>
            hpp.add("#include <string>")
          case MBinary =>
            hpp.add("#include <vector>")
            hpp.add("#include <cstdint>")
          case MOptional =>
            hpp.add("#include " + spec.cppOptionalHeader)
          case MList =>
            hpp.add("#include <vector>")
          case MSet =>
            hpp.add("#include <unordered_set>")
          case MMap =>
            hpp.add("#include <unordered_map>")
        }
      case d: MDef =>
        d.defType match {
          case DEnum
             | DRecord =>
            if (d.name != name) {
              hpp.add("#include " + q(spec.cppIncludePrefix + spec.cppFileIdentStyle(d.name) + "." + spec.cppHeaderExt))
            }
          case DInterface =>
            hpp.add("#include <memory>")
            if (d.name != name) {
              hppFwds.add(s"class ${idCpp.ty(d.name)};")
            }
        }
      case p: MParam =>
    }

  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new CppRefs(ident.name)
    val self = idCpp.enumType(ident)

    if (spec.cppEnumHashWorkaround) {
      refs.hpp.add("#include <functional>") // needed for std::hash
    }

    writeHppFile(ident, origin, refs.hpp, refs.hppFwds, w => {
      w.w(s"enum class $self : int").bracedSemi {
        for (o <- e.options) {
          writeDoc(w, o.doc)
          w.wl(idCpp.enum(o.ident.name) + ",")
        }
      }
    },
    w => {
      // std::hash specialization has to go *outside* of the wrapNs
      if (spec.cppEnumHashWorkaround) {
        val fqSelf = withNs(spec.cppNamespace, self)
        w.wl
        wrapNamespace(w, Some("std"),
          (w: IndentWriter) => {
            w.wl("template <>")
            w.w(s"struct hash<$fqSelf>").bracedSemi {
              w.w(s"size_t operator()($fqSelf type) const").braced {
                w.wl("return std::hash<int>()(static_cast<int>(type));")
              }
            }
          }
        )
      }
    })
  }

  def generateHppConstants(w: IndentWriter, consts: Seq[Const]) = {
    for (c <- consts) {
      w.wl
      writeDoc(w, c.doc)
      w.wl(s"static const ${toCppType(c.ty)} ${idCpp.const(c.ident)};")
    }
  }

  def generateCppConstants(w: IndentWriter, consts: Seq[Const], selfName: String) = {
    def writeCppConst(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(l.toString)
      case d: Double => w.w(d.toString)
      case b: Boolean => w.w(if (b) "true" else "false")
      case s: String => w.w(s)
      case e: EnumValue => w.w(idCpp.enumType(e.ty) + "::" + idCpp.enum(e.ty.name + "_" + e.name))
      case v: ConstRef => w.w(selfName + "::" + idCpp.const(v))
      case z: Map[_, _] => { // Value is record
      val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        w.wl(idCpp.ty(recordMdef.name) + "(")
        w.increase()
        // Use exact sequence
        val skipFirst = SkipFirst()
        for (f <- record.fields) {
          skipFirst {w.wl(",")}
          writeCppConst(w, f.ty, vMap.apply(f.ident.name))
          w.w(" /* " + idCpp.field(f.ident) + " */ ")
        }
        w.w(")")
        w.decrease()
      }
    }

    val skipFirst = SkipFirst()
    for (c <- consts) {
      skipFirst{ w.wl }
      w.w(s"const ${toCppType(c.ty)} $selfName::${idCpp.const(c.ident)} = ")
      writeCppConst(w, c.ty, c.value)
      w.wl(";")
    }
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val refs = new CppRefs(ident.name)
    r.fields.foreach(f => refs.find(f.ty))
    r.consts.foreach(c => refs.find(c.ty))
    refs.hpp.add("#include <utility>") // Add for std::move

    val self = idCpp.ty(ident)
    val (cppName, cppFinal) = if (r.ext.cpp) (ident.name + "_base", "") else (ident.name, " final")
    val actualSelf = idCpp.ty(cppName)

    // Requiring the extended class
    if (r.ext.cpp) {
      refs.hpp.add(s"class $self; // Requiring extended class")
      refs.cpp.add("#include "+q("../" + spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))
    }

    // C++ Header
    def writeCppPrototype(w: IndentWriter) {
      writeDoc(w, doc)
      writeCppTypeParams(w, params)
      w.w("struct " + actualSelf + cppFinal).bracedSemi {
        generateHppConstants(w, r.consts)
        // Field definitions.
        for (f <- r.fields) {
          w.wl
          writeDoc(w, f.doc)
          w.wl(toCppType(f.ty) + " " + idCpp.field(f.ident) + ";")
        }

        w.wl
        if (r.derivingTypes.contains(DerivingType.Eq)) {
          w.wl(s"bool operator==(const $actualSelf & other) const;")
          w.wl(s"bool operator!=(const $actualSelf & other) const;")
        }
        if (r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl(s"bool operator<(const $actualSelf & other) const;")
          w.wl(s"bool operator>(const $actualSelf & other) const;")
        }
        if (r.derivingTypes.contains(DerivingType.Eq) && r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl(s"bool operator<=(const $actualSelf & other) const;")
          w.wl(s"bool operator>=(const $actualSelf & other) const;")
        }

        // Constructor.
        w.wl
        if (r.fields.nonEmpty) {
          w.wl(actualSelf + "(").nestedN(2) {
            val skipFirst = SkipFirst()
            for (f <- r.fields) {
              skipFirst { w.wl(",") }
              w.w(toCppType(f.ty) + " " + idCpp.local(f.ident))
            }
            w.wl(") :")
            w.nested {
              val skipFirst = SkipFirst()
              for (f <- r.fields) {
                skipFirst { w.wl(",") }
                w.w(idCpp.field(f.ident) + "(std::move(" + idCpp.local(f.ident) + "))")
              }
              w.wl(" {")
            }
          }
          w.wl("}")
        } else {
          w.wl(actualSelf + "() {};")
        }

        if (r.ext.cpp) {
          w.wl
          w.wl(s"virtual ~$actualSelf() {}")
        }
      }
    }

    writeHppFile(cppName, origin, refs.hpp, refs.hppFwds, writeCppPrototype)

    if (r.consts.nonEmpty || r.derivingTypes.nonEmpty) {
      writeCppFile(cppName, origin, refs.cpp, w => {
        generateCppConstants(w, r.consts, actualSelf)

        if (r.derivingTypes.contains(DerivingType.Eq)) {
          w.wl
          w.w(s"bool $actualSelf::operator==(const $actualSelf & other) const").braced {
            w.w("return ").nested {
              val skipFirst = SkipFirst()
              for (f <- r.fields) {
                skipFirst { w.wl(" &&") }
                w.w(s"${idCpp.field(f.ident)} == other.${idCpp.field(f.ident)}")
              }
              w.wl(";")
            }
          }
          w.wl
          w.w(s"bool $actualSelf::operator!=(const $actualSelf & other) const").braced {
            w.wl("return !(*this == other);")
          }
        }
        if (r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl
          w.w(s"bool $actualSelf::operator<(const $actualSelf & other) const").braced {
            for (f <- r.fields) {
              w.w(s"if (${idCpp.field(f.ident)} < other.${idCpp.field(f.ident)})").braced {
                w.wl("return true;")
              }
              w.w(s"if (other.${idCpp.field(f.ident)} < ${idCpp.field(f.ident)})").braced {
                w.wl("return false;")
              }
            }
            w.wl("return false;")
          }
          w.wl
          w.w(s"bool $actualSelf::operator>(const $actualSelf & other) const").braced {
            w.wl("return other < *this;")
          }
        }
        if (r.derivingTypes.contains(DerivingType.Eq) && r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl
          w.w(s"bool $actualSelf::operator<=(const $actualSelf & other) const").braced {
            w.wl("return (*this < other || *this == other);")
          }
          w.wl
          w.w(s"bool $actualSelf::operator>=(const $actualSelf & other) const").braced {
            w.wl("return other <= *this;")
          }
        }
      })
    }

  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new CppRefs(ident.name)
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    val self = idCpp.ty(ident)

    writeHppFile(ident, origin, refs.hpp, refs.hppFwds, w => {
      writeDoc(w, doc)
      writeCppTypeParams(w, typeParams)
      w.w(s"class $self").bracedSemi {
        w.wlOutdent("public:")
        // Destructor
        w.wl("virtual ~" + idCpp.ty(ident) + "() {}")
        // Constants
        generateHppConstants(w, i.consts)
        // Methods
        for (m <- i.methods) {
          w.wl
          writeDoc(w, m.doc)
          val ret = m.ret.fold("void")(toCppType(_))
          val params = m.params.map(p => "const " + toCppType(p.ty) + " & " + idCpp.local(p.ident))
          if (m.static) {
            w.wl(s"static $ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")};")
          } else {
            val constFlag = if (m.const) " const" else ""
            w.wl(s"virtual $ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")}$constFlag = 0;")
          }
        }
      }
    })

    // Cpp only generated in need of Constants
    if (i.consts.nonEmpty) {
      writeCppFile(ident, origin, refs.cpp, w => {
        generateCppConstants(w, i.consts, self)
      })
    }

  }

  def writeCppTypeParams(w: IndentWriter, params: Seq[TypeParam]) {
    if (params.isEmpty) return
    w.wl("template " + params.map(p => "typename " + idCpp.typeParam(p.ident)).mkString("<", ", ", ">"))
  }

}
