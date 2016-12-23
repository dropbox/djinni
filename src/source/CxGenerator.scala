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

class CxGenerator(spec: Spec) extends Generator(spec) {

  val cxMarshal = new CxMarshal(spec)
  val cxcppMarshal = new CxCppMarshal(spec)
  val cppMarshal = new CppMarshal(spec)

  val writeCxFile = writeCppFileGeneric(spec.cxOutFolder.get, spec.cxNamespace, spec.cxFileIdentStyle, spec.cxIncludePrefix, spec.cxExt, spec.cxHeaderExt) _
  def writeHxFile(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit = (w => {})) =
    writeHppFileGeneric(spec.cxHeaderOutFolder.get, spec.cxNamespace, spec.cxFileIdentStyle, spec.cxHeaderExt)(name, origin, includes, fwds, f, f2)

  class CxRefs(name: String) {
    var hx = mutable.TreeSet[String]()
    var hxFwds = mutable.TreeSet[String]()
    var cx = mutable.TreeSet[String]()

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.foreach(find)
      find(tm.base)
    }
    def find(m: Meta) = for(r <- cxMarshal.references(m, name)) r match {
      case ImportRef(arg) => hx.add("#include " + arg)
      case DeclRef(decl, Some(spec.cxNamespace)) => hxFwds.add(decl)
      case DeclRef(_, _) =>
    }

    def findConvert(ty: TypeRef) { findConvert(ty.resolved) }
    def findConvert(tm: MExpr) {
      tm.args.foreach(find)
      findConvert(tm.base)
    }
    def findConvert(m: Meta) = for(r <- cxMarshal.convertReferences(m, name)) r match {
      case ImportRef(arg) => cx.add("#include " + arg)
    }
  }

  def writeCxFuncDecl(klass: String, method: Interface.Method, w: IndentWriter) {
    val ret = cxMarshal.returnType(method.ret)
    val params = method.params.map(p => cxMarshal.paramType(p.ty) + " " + idCx.local(p.ident))
    val constFlag = if (method.const) " const" else ""
    w.wl(s"$ret $klass::${idCx.method(method.ident)}${params.mkString("(", ", ", ")")}")
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new CxRefs(ident.name)
    val self = cxMarshal.typename(ident, e)

    writeHxFile(ident, origin, refs.hx, refs.hxFwds, w => {
      w.w(s"public enum class $self").bracedSemi {
        for (o <- e.options) {
          writeDoc(w, o.doc)
          w.wl(idCx.enum(o.ident.name) + (if(o == e.options.last) "" else ","))
        }
      }
    })
  }

  def generateHxConstants(w: IndentWriter, consts: Seq[Const]) = {
    for (c <- consts) {
      w.wl
      writeDoc(w, c.doc)
      w.wl(s"static property ${cxMarshal.fieldType(c.ty)} ${idCx.const(c.ident)}")
      w.braced {
        w.wl(s"${cxMarshal.fieldType(c.ty)} get()")
        w.braced {
          w.wl(s"return ${idCx.const(c.ident)}_;")
        }
      }
    }
  }

  def generateHxPrivateConstants(w: IndentWriter, consts: Seq[Const]) = {
    def writeCxConst(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(l.toString)
      case d: Double if cxMarshal.fieldType(ty) == "float" => w.w(d.toString + "f")
      case d: Double => w.w(d.toString)
      case b: Boolean => w.w(if (b) "true" else "false")
      case s: String => w.w(s)
      case e: EnumValue => w.w(cxMarshal.typename(ty) + "::" + idCx.enum(e.ty.name + "_" + e.name))
      case v: ConstRef => w.w(idCx.const(v))
      case z: Map[_, _] => { // Value is record
      val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        w.wl(cxMarshal.typename(ty) + "(")
        w.increase()
        // Use exact sequence
        val skipFirst = SkipFirst()
        for (f <- record.fields) {
          skipFirst {w.wl(",")}
          writeCxConst(w, f.ty, vMap.apply(f.ident.name))
          w.w(" /* " + idCx.field(f.ident) + " */ ")
        }
        w.w(")")
        w.decrease()
      }
    }

    for (c <- consts) {
      w.wl
      writeDoc(w, c.doc)
      w.w(s"static const ${cxMarshal.fieldType(c.ty)} ${idCx.const(c.ident)}_ = ")
      writeCxConst(w, c.ty, c.value)
      w.wl(";")
    }
  }

//  def generateCxConstants(w: IndentWriter, consts: Seq[Const], selfName: String) = {
//    def writeCxConst(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
//      case l: Long => w.w(l.toString)
//      case d: Double if cxMarshal.fieldType(ty) == "float" => w.w(d.toString + "f")
//      case d: Double => w.w(d.toString)
//      case b: Boolean => w.w(if (b) "true" else "false")
//      case s: String => w.w(s)
//      case e: EnumValue => w.w(cxMarshal.typename(ty) + "::" + idCx.enum(e.ty.name + "_" + e.name))
//      case v: ConstRef => w.w(selfName + "::" + idCx.const(v))
//      case z: Map[_, _] => { // Value is record
//      val recordMdef = ty.resolved.base.asInstanceOf[MDef]
//        val record = recordMdef.body.asInstanceOf[Record]
//        val vMap = z.asInstanceOf[Map[String, Any]]
//        w.wl(cxMarshal.typename(ty) + "(")
//        w.increase()
//        // Use exact sequence
//        val skipFirst = SkipFirst()
//        for (f <- record.fields) {
//          skipFirst {w.wl(",")}
//          writeCxConst(w, f.ty, vMap.apply(f.ident.name))
//          w.w(" /* " + idCx.field(f.ident) + " */ ")
//        }
//        w.w(")")
//        w.decrease()
//      }
//    }
//
//    val skipFirst = SkipFirst()
//    for (c <- consts) {
//      skipFirst{ w.wl }
//      w.w(s"${cxMarshal.fieldType(c.ty)} const $selfName::${idCx.const(c.ident)} = ")
//      writeCxConst(w, c.ty, c.value)
//      w.wl(";")
//    }
//  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val refs = new CxRefs(ident.name)
    r.fields.foreach(f => refs.find(f.ty))
    r.consts.foreach(c => refs.find(c.ty))

    val self = cxMarshal.typename(ident, r)
    val (cxName, cxFinal) = if (r.ext.cx) (ident.name + "_base", "") else (ident.name, " sealed")
    val actualSelf = cxMarshal.typename(cxName, r)

    // Requiring the extended class
    if (r.ext.cx) {
      refs.hx.add(s"publc ref struct $self;")
      refs.cx.add("#include " + q("../" + spec.cxFileIdentStyle(ident) + "." + spec.cxHeaderExt))
    }

    // C++ Header
    def writeCxPrototype(w: IndentWriter) {
      writeDoc(w, doc)
      writeCxTypeParams(w, params)
      w.w("public ref struct " + actualSelf + cxFinal).bracedSemi {
        generateHxConstants(w, r.consts)
        // Field definitions.
        for (f <- r.fields) {
          writeDoc(w, f.doc)
          w.wl("property " + cxMarshal.fieldType(f.ty) + " " + idCx.field(f.ident) + ";")
        }

        // Constructor.
        if (r.fields.nonEmpty) {
          w.wl
          writeAlignedCall(w, actualSelf + "(", r.fields, ")", f => cxMarshal.fieldType(f.ty) + " " + idCx.local("new_"+f.ident.name))
          w.wl
          w.braced {
            r.fields.map(f => w.wl(s"${idCx.local(f.ident)} = ${idCx.local("new_"+f.ident.name)};") )
          }
        }

        if (r.derivingTypes.contains(DerivingType.Eq)) {
          w.wl
          w.wl(s"bool Equals($actualSelf^ rhs);")
        }
        if (r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl
          w.wl(s"int32 CompareTo($actualSelf^ rhs);")
        }

        w.wlOutdent("private:")
        generateHxPrivateConstants(w, r.consts)

      }
    }

    writeHxFile(cxName, origin, refs.hx, refs.hxFwds, writeCxPrototype)

    if (r.consts.nonEmpty || r.derivingTypes.nonEmpty) {
      writeCxFile(cxName, origin, refs.cx, w => {
//        generateCxConstants(w, r.consts, actualSelf)

        if (r.derivingTypes.contains(DerivingType.Eq)) {
          w.wl
          w.w(s"bool $actualSelf::Equals($actualSelf^ rhs)").braced {
            if (!r.fields.isEmpty) {
              writeAlignedCall(w, "return ", r.fields, " &&", "", f => s"this->${idCx.field(f.ident)} == rhs->${idCx.field(f.ident)}")
              w.wl(";")
            } else {
              w.wl("return true;")
            }
          }
        }
        if (r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl
          w.w(s"int32 $actualSelf::CompareTo($actualSelf^ rhs)").braced {
            w.wl(s"if (rhs == nullptr) return 1;")
            w.wl("int32 tempResult;")
            for (f <- r.fields) {
              f.ty.resolved.base match {
                case MString => w.wl(s"tempResult = Platform::String::CompareOrdinal(this->${idCx.field(f.ident)}, rhs->${idCx.field(f.ident)});")
                case t: MPrimitive =>
                  w.wl(s"if (this->${idCx.field(f.ident)} < rhs->${idCx.field(f.ident)}) {").nested {
                    w.wl(s"tempResult = -1;")
                  }
                  w.wl(s"} else if (this->${idCx.field(f.ident)} > rhs->${idCx.field(f.ident)}) {").nested {
                    w.wl(s"tempResult = 1;")
                  }
                  w.wl(s"} else {").nested {
                    w.wl(s"tempResult = 0;")
                  }
                  w.wl("}")
                case df: MDef => df.defType match {
                  case DRecord => w.wl(s"tempResult = this->${idCx.field(f.ident)}->CompareTo(rhs->${idCx.field(f.ident)});")
                  case DEnum => w.w(s"tempResult = this->${idCx.field(f.ident)}->CompareTo(rhs->${idCx.field(f.ident)});")
                  case _ => throw new AssertionError("Unreachable")
                }
                case _ => throw new AssertionError("Unreachable")
              }
            }
          }
        }
      })
    }
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new CxRefs(ident.name)
    refs.hx.add("#include <memory>")
    refs.hx.add("#include \"CppWrapperCache.h\"")
    refs.hx.add("#include \""+spec.cppIncludePrefix + spec.cppFileIdentStyle(ident.name) + "." + spec.cppHeaderExt+"\"")
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    refs.cx = refs.hx.clone()
    i.methods.map(m => {
      m.params.map(p => refs.findConvert(p.ty))
      m.ret.foreach(refs.findConvert)
    })
    refs.cx.add("#include \"Marshal.h\"")
    refs.cx.add("#include \""+ spec.cxcppIncludePrefix + cxcppMarshal.headerName(ident.name) + "." + spec.cxcppHeaderExt + "\"")

    val self = cxMarshal.typename(ident, i)
    val cppSelf = cppMarshal.fqTypename(ident, i)

    writeHxFile(ident, origin, refs.hx, refs.hxFwds, w => {
      writeDoc(w, doc)
      writeCxTypeParams(w, typeParams)
      if (i.ext.cx) w.wl(s"public interface class $self").bracedSemi {
        // Constants
        generateHxConstants(w, i.consts)
        // Methods
        for (m <- i.methods) {
          w.wl
          writeDoc(w, m.doc)
          val ret = cxMarshal.returnType(m.ret)
          val params = m.params.map(p => cxMarshal.paramType(p.ty) + " " + idCpp.local(p.ident))
          if (m.static) {
            w.wl(s"static $ret ${idCx.method(m.ident)}${params.mkString("(", ", ", ")")};")
          } else {
            val constFlag = if (m.const) " const" else ""
            w.wl(s"virtual $ret ${idCx.method(m.ident)}${params.mkString("(", ", ", ")")} = 0;")
          }
        }
      }
      else w.wl(s"public ref class $self sealed : public Platform::Object").bracedSemi {
        w.wlOutdent("public:")
        // Constants
        generateHxConstants(w, i.consts)
        // Methods
        for (m <- i.methods) {
          w.wl
          writeDoc(w, m.doc)
          val ret = cxMarshal.returnType(m.ret)
          val params = m.params.map(p => cxMarshal.paramType(p.ty) + " " + idCpp.local(p.ident))
          if (m.static) {
            w.wl(s"static $ret ${idCx.method(m.ident)}${params.mkString("(", ", ", ")")};")
          } else {
            val constFlag = if (m.const) " const" else ""
            w.wl(s"$ret ${idCx.method(m.ident)}${params.mkString("(", ", ", ")")};")
          }
        }
        //private members
        w.wlOutdent("private:")
        generateHxPrivateConstants(w, i.consts)
        w.wlOutdent("internal:")
        //construct from a cpp ref
        w.wl(s"$self(const std::shared_ptr<$cppSelf>& cppRef);")
        w.wl(s"::djinni::CppWrapperCache<$cppSelf>::Handle m_cppRef;")
      }
    })

    // Cx only generated in need of Constants
    writeCxFile(ident, origin, refs.cx, w => {
      if (! i.ext.cx) {
//        if (i.consts.nonEmpty) {
//          generateCxConstants(w, i.consts, self)
//        }
        //constructor
        w.wl(s"$self::$self(const std::shared_ptr<$cppSelf>& cppRef)")
        w.braced {
          w.wl("m_cppRef.assign(cppRef);")
        }
        //methods
        for (m <- i.methods) {
          w.wl
          writeCxFuncDecl(self, m, w)
          w.braced {
//           w.w("try").bracedEnd(" DJINNI_TRANSLATE_EXCEPTIONS()") {
            val ret = m.ret.fold("")(_ => "auto r = ")
            val call = ret + (if (!m.static) "m_cppRef.get()->" else cppSelf + "::") + idCpp.method(m.ident) + "("
            writeAlignedCall(w, call, m.params, ")", p => cxMarshal.toCpp(p.ty, idCx.local(p.ident.name)))
            w.wl(";")
            m.ret.fold()(r => w.wl(s"return ${cxMarshal.fromCpp(r, "r")};"))
            //            }
          }
        }
      }
    })

  }

  def writeCxTypeParams(w: IndentWriter, params: Seq[TypeParam]) {
    if (params.isEmpty) return
    w.wl("template " + params.map(p => "typename " + idCx.typeParam(p.ident)).mkString("<", ", ", ">"))
  }

}
