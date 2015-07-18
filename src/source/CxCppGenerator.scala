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

class CxCppGenerator(spec: Spec) extends Generator(spec) {

  val cxcppMarshal = new CxCppMarshal(spec)
  val cxMarshal = new CxMarshal(spec)
  val cppMarshal = new CppMarshal(spec)

  val writeCxCppFile = writeCppFileGeneric(spec.cxcppOutFolder.get, spec.cxcppNamespace, spec.cppFileIdentStyle, spec.cxcppIncludePrefix, spec.cxcppExt, spec.cxcppHeaderExt) _
  def writeHxFile(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit = (w => {})) =
    writeHppFileGeneric(spec.cxcppHeaderOutFolder.get, spec.cxcppNamespace, spec.cppFileIdentStyle, spec.cxcppHeaderExt)(name, origin, includes, fwds, f, f2)

  class CxCppRefs(name: String) {
    var hx = mutable.TreeSet[String]()
    var hxFwds = mutable.TreeSet[String]()
    var cxcpp = mutable.TreeSet[String]()

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.foreach(find)
      find(tm.base)
    }
    def find(m: Meta) = for(r <- cxcppMarshal.references(m)) r match {
      case ImportRef(arg) => hx.add("#include " + arg)
      case DeclRef(decl, Some(spec.cxcppNamespace)) => hxFwds.add(decl)
      case DeclRef(_, _) =>
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new CxCppRefs(ident.name)
    val self = cxcppMarshal.typename(ident, e)

    if (spec.cppEnumHashWorkaround) {
      refs.hx.add("#include <functional>") // needed for std::hash
    }

    writeHxFile(ident, origin, refs.hx, refs.hxFwds, w => {
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
          val fqSelf = cxcppMarshal.fqTypename(ident, e)
          w.wl
          wrapNamespace(w, "std",
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

  def generateHxConstants(w: IndentWriter, consts: Seq[Const]) = {
    for (c <- consts) {
      w.wl
      writeDoc(w, c.doc)
      w.wl(s"static ${cxcppMarshal.fieldType(c.ty)} const ${idCpp.const(c.ident)};")
    }
  }

  def generateCxCppConstants(w: IndentWriter, consts: Seq[Const], selfName: String) = {
    def writeCxCppConst(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(l.toString)
      case d: Double if cxcppMarshal.fieldType(ty) == "float" => w.w(d.toString + "f")
      case d: Double => w.w(d.toString)
      case b: Boolean => w.w(if (b) "true" else "false")
      case s: String => w.w(s)
      case e: EnumValue => w.w(cxcppMarshal.typename(ty) + "::" + idCpp.enum(e.ty.name + "_" + e.name))
      case v: ConstRef => w.w(selfName + "::" + idCpp.const(v))
      case z: Map[_, _] => { // Value is record
      val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        w.wl(cxcppMarshal.typename(ty) + "(")
        w.increase()
        // Use exact sequence
        val skipFirst = SkipFirst()
        for (f <- record.fields) {
          skipFirst {w.wl(",")}
          writeCxCppConst(w, f.ty, vMap.apply(f.ident.name))
          w.w(" /* " + idCpp.field(f.ident) + " */ ")
        }
        w.w(")")
        w.decrease()
      }
    }

    val skipFirst = SkipFirst()
    for (c <- consts) {
      skipFirst{ w.wl }
      w.w(s"${cxcppMarshal.fieldType(c.ty)} const $selfName::${idCpp.const(c.ident)} = ")
      writeCxCppConst(w, c.ty, c.value)
      w.wl(";")
    }
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val refs = new CxCppRefs(ident.name)
    for (c <- r.consts)
      refs.find(c.ty)
    for (f <- r.fields)
      refs.find(f.ty)

    val cxName = ident.name + (if (r.ext.cx) "_base" else "")
    val cxSelf = cxMarshal.fqTypename(ident, r)
    val cppSelf = cppMarshal.fqTypename(ident, r)

    refs.hx.add("!#include " + q(spec.cxcppIncludeCxPrefix + (if(r.ext.cx) "../" else "") + cxcppMarshal.headerName(ident)))
    refs.hx.add("!#include " + q(spec.cxcppIncludeCppPrefix + (if(r.ext.cpp) "../" else "") + spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))

    refs.cxcpp.add("#include <cassert>")
    refs.cxcpp.add("!#import " + q(spec.cxcppIncludePrefix + cxcppMarshal.headerName(cxName)))

    def checkMutable(tm: MExpr): Boolean = tm.base match {
      case MOptional => checkMutable(tm.args.head)
      case MString => true
      case MBinary => true
      case _ => false
    }

    val helperClass = cxcppMarshal.helperClass(ident)

    writeHxFile(cxcppMarshal.headerName(cxName), origin, refs.hx, refs.hxFwds, w => {
      w.wl
      w.wl(s"struct $helperClass")
      w.bracedSemi {
        w.wl(s"using CppType = $cppSelf;")
        w.wl(s"using CxType = $cxSelf;");
        w.wl
        w.wl(s"using Boxed = $helperClass;")
        w.wl
        w.wl(s"static CppType toCpp(CxType^ cx);")
        w.wl(s"static CxType fromCpp(const CppType& cpp);")
      }
    })

    writeCxCppFile(cxcppMarshal.bodyName(cxName), origin, refs.cxcpp, w => {
      w.wl(s"auto $helperClass::toCpp(CxType cx) -> CppType")
      w.braced {
        w.wl("assert(cx);")
        if(r.fields.isEmpty) w.wl("(void)cx; // Suppress warnings in relase builds for empty records")
        writeAlignedCall(w, "return {", r.fields, "}", f => cxcppMarshal.toCpp(f.ty, "cx->" + idCx.field(f.ident)))
        w.wl(";")
      }
      w.wl
      w.wl(s"auto $helperClass::fromCpp(const CppType& cpp) -> CxType")
      w.braced {
        if(r.fields.isEmpty) w.wl("(void)cpp; // Suppress warnings in relase builds for empty records")
        val first = if(r.fields.isEmpty) "" else IdentStyle.camelUpper("with_" + r.fields.head.ident.name)
        writeAlignedCall(w, "return ref new CxType(", r.fields, ")", f=> cxcppMarshal.fromCpp(f.ty, "cpp." + idCpp.field(f.ident)))
        w.wl(";")
      }
    })
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new CxCppRefs(ident.name)
    refs.hx.add("#include \""+spec.cppIncludePrefix + spec.cppFileIdentStyle(ident.name) + "." + spec.cppHeaderExt+"\"")
    refs.hx.add("#include \""+spec.cxIncludePrefix + spec.cxFileIdentStyle(ident.name) + "." + spec.cxHeaderExt+"\"")
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    val self = cxcppMarshal.typename(ident, i)
    val cxSelf = if(i.ext.cx) cxMarshal.fqTypename(ident, i)
    val cppSelf = cppMarshal.fqTypename(ident, i)
    val helperClass = cxcppMarshal.helperClass(ident)

    writeHxFile(cxcppMarshal.headerName(ident.name), origin, refs.hx, refs.hxFwds, w => {
      w.wl
      w.wl(s"class $self")
      w.bracedSemi {
        w.wlOutdent("public:")
        w.wl(s"using CppType = $cppSelf;")
        w.wl(s"using CxType = $cxSelf;");
        w.wl
        w.wl(s"using Boxed = $self;")
        w.wl
        w.wl(s"static CppType toCpp(CxType^ cx);")
        w.wl(s"static CxType^ fromCpp(const CppType& cpp);")
        if (i.ext.cx) {
          w.wl
          w.wlOutdent("private:")
          w.wl(s"class CxProxy;")
        }

      }

    })

    writeCxCppFile(cxcppMarshal.bodyName(ident.name), origin, refs.cxcpp, w => {

      //only interface classes have proxy objects
      if (i.ext.cx) {
        w.wl(s"class $helperClass sealed : public $cppSelf, public ::djinni::CxWrapperCache<CxProxy>::Handle").braced {
          w.wlOutdent("public:")
          w.wl("using Handle::Handle;")
          w.wl(s"using CxType = $cxSelf")
          w.wl
          w.wl("CxProxy(Platform::Object^ cx) : ::djinni::CxWrapperCache<CxProxy>::Handle{ cx } {}")
          w.wl
          //methods
          for (m <- i.methods) {
            w.wl
            writeDoc(w, m.doc)
            val ret = cppMarshal.returnType(m.ret)
            val params = m.params.map(p => cppMarshal.paramType(p.ty) + " " + idCpp.local(p.ident))
            if (m.static) {
              w.wl(s"static $ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")} override")
            } else {
              val constFlag = if (m.const) " const" else ""
              w.wl(s"$ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")}$constFlag override")
            }
            w.braced {
              val call = (if (!m.static) "auto r = static_cast<CxType^>(Handle::get())->" else cppSelf + "::") + idCpp.method(m.ident) + "("
              writeAlignedCall(w, call, m.params, ")", p => cxcppMarshal.fromCpp(p.ty, idCpp.local(p.ident.name)))
              w.wl(";")
              m.ret.fold()(r => w.wl(s"return ${cxcppMarshal.toCpp(r, "r")};"))
            }
          }
        }
        w.wl
      }

      if (i.consts.nonEmpty) {
        generateCxCppConstants(w, i.consts, self)
        w.wl
      }
      w.wl(s"auto $self::toCpp(CxType^ cx) -> CppType")
      w.braced {
        w.wl("if (!cx)").braced {
          w.wl("return nullptr;")
        }
        w.wl("return ::djinni::CxWrapperCache<CxProxy>::getInstance()->get(cx);")
      }
      w.wl
      w.wl(s"auto $self::fromCpp(const CppType& cpp) -> CxType^")
      w.braced {
        w.braced {
          w.wl("if (!cpp)").braced {
            w.wl("return nullptr;")
          }
          w.wl("return static_cast<CxType^>(dynamic_cast<CxProxy &>(*cpp).Handle::get());")
        }
      }
    })
  }


  def writeCxCppTypeParams(w: IndentWriter, params: Seq[TypeParam]) {
    if (params.isEmpty) return
    w.wl("template " + params.map(p => "typename " + idCpp.typeParam(p.ident)).mkString("<", ", ", ">"))
  }

}
