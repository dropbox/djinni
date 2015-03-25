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
import djinni.syntax.Error
import djinni.writer.IndentWriter

import scala.collection.mutable
import scala.collection.parallel.immutable

class ObjcppGenerator(spec: Spec) extends Generator(spec) {

  val objcMarshal = new ObjcMarshal(spec)
  val objcppMarshal = new ObjcppMarshal(spec)
  val cppMarshal = new CppMarshal(spec)

  class ObjcRefs() {
    var body = mutable.TreeSet[String]()
    var header = mutable.TreeSet[String]()
    var privHeader = mutable.TreeSet[String]()

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.map(find).mkString("<", ",", ">")
      tm.base match {
        case o: MOpaque =>
          header.add("#import <Foundation/Foundation.h>")
          body.add("#import " + q(spec.objcBaseLibIncludePrefix + "DJIMarshal+Private.h"))
        case d: MDef => d.defType match {
          case DEnum =>
            header.add("#import " + q(spec.objcIncludePrefix + headerName(d.name)))
            body.add("#import " + q(spec.objcBaseLibIncludePrefix + "DJIMarshal+Private.h"))
          case DInterface =>
            val ext = d.body.asInstanceOf[Interface].ext
            if (ext.cpp) {
              header.add("@class " + objcMarshal.typename(tm) + ";")
              body.add("#import " + q(spec.objcIncludePrivatePrefix + privateHeaderName(d.name)))
            }
            if (ext.objc) {
              header.add("@protocol " + objcMarshal.typename(tm) + ";")
              body.add("#import " + q(spec.objcIncludePrivatePrefix + privateHeaderName(d.name)))
            }
          case DRecord =>
            val r = d.body.asInstanceOf[Record]
            val prefix = if (r.ext.objc) "../" else ""
            header.add("@class " + objcMarshal.typename(tm) + ";")
            body.add("#import " + q(spec.objcIncludePrivatePrefix + prefix + privateHeaderName(d.name)))
        }
        case p: MParam =>
      }
    }
  }

  private def arcAssert(w: IndentWriter) = w.wl("static_assert(__has_feature(objc_arc), " + q("Djinni requires ARC to be enabled for this file") + ");")

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    // No generation required
  }

  def headerName(ident: String): String = idObjc.ty(ident) + "." + spec.objcHeaderExt
  def privateHeaderName(ident: String): String = idObjc.ty(ident) + "+Private." + spec.objcHeaderExt
  def bodyName(ident: String): String = idObjc.ty(ident) + "." + spec.objcExt
  def privateBodyName(ident: String): String = idObjc.ty(ident) + "+Private." + spec.objcExt

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new ObjcRefs()
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    val self = objcMarshal.typename(ident, i)
    val cppSelf = cppMarshal.fqTypename(ident, i)

    refs.privHeader.add("#include <memory>")
    refs.privHeader.add("!#include " + q(spec.objcIncludeCppPrefix + spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))
    refs.body.add("!#import " + q(spec.objcIncludePrefix + headerName(ident)))

    def writeObjcFuncDecl(method: Interface.Method, w: IndentWriter) {
      val label = if (method.static) "+" else "-"
      val ret = objcMarshal.fqReturnType(method.ret)
      val decl = s"$label ($ret)${idObjc.method(method.ident)}"
      writeAlignedObjcCall(w, decl, method.params, "", p => (idObjc.field(p.ident), s"(${objcMarshal.paramType(p.ty)})${idObjc.local(p.ident)}"))
    }

    val helperClass = objcppMarshal.helperClass(ident)

    writeObjcFile(privateHeaderName(ident.name), origin, refs.privHeader, w => {
      arcAssert(w)
      w.wl
      w.wl((if(i.ext.objc) "@protocol " else "@class ") + self + ";")
      w.wl
      wrapNamespace(w, Some(spec.objcppNamespace), w => {
        w.wl(s"class $helperClass").bracedSemi {
          w.wlOutdent("public:")
          w.wl(s"using CppType = std::shared_ptr<$cppSelf>;")
          w.wl("using ObjcType = " + (if(i.ext.objc) s"id<$self>" else s"$self*") + ";");
          w.wl
          w.wl(s"using Boxed = $helperClass;")
          w.wl
          w.wl(s"static CppType toCpp(ObjcType objc);")
          w.wl(s"static ObjcType fromCpp(const CppType& cpp);")
          w.wl
          w.wlOutdent("private:")
          w.wl("class ObjcProxy;")
        }
      })
      w.wl
    })

    if (i.ext.cpp) {
      refs.body.add("!#import " + q(spec.objcIncludePrivatePrefix + privateHeaderName(ident.name)))
      refs.body.add("#import " + q(spec.objcBaseLibIncludePrefix + "DJICppWrapperCache+Private.h"))
      refs.body.add("#include <utility>")
      refs.body.add("#import " + q(spec.objcBaseLibIncludePrefix + "DJIError.h"))
      refs.body.add("#include <exception>")

      writeObjcFile(bodyName(ident.name), origin, refs.body, w => {
        arcAssert(w)
        w.wl
        w.wl(s"@interface $self ()")
        w.wl
        w.wl(s"@property (nonatomic, readonly) ::djinni::DbxCppWrapperCache<$cppSelf>::Handle cppRef;")
        w.wl
        w.wl(s"- (id)initWithCpp:(const std::shared_ptr<$cppSelf>&)cppRef;")
        w.wl
        w.wl("@end")
        w.wl
        wrapNamespace(w, Some(spec.objcppNamespace), w => {
          w.wl(s"auto $helperClass::toCpp(ObjcType objc) -> CppType")
          w.braced {
            w.wl(s"return objc ? objc.cppRef.get() : nullptr;")
          }
          w.wl
          w.wl(s"auto $helperClass::fromCpp(const CppType& cpp) -> ObjcType")
          w.braced {
            w.wl(s"return !cpp ? nil : ::djinni::DbxCppWrapperCache<$cppSelf>::getInstance().get(cpp, [] (const auto& p)").bracedEnd(");") {
              w.wl(s"return [[$self alloc] initWithCpp:p];")
            }
          }
        })
        w.wl
        w.wl(s"@implementation $self")
        w.wl
        w.wl(s"- (id)initWithCpp:(const std::shared_ptr<$cppSelf>&)cppRef")
        w.braced {
          w.w("if (self = [super init])").braced {
            w.wl("_cppRef.assign(cppRef);") // Using operator= here deadlocks in DbxWrapperCache::remove()
          }
          w.wl("return self;")
        }
        for (m <- i.methods) {
          w.wl
          writeObjcFuncDecl(m, w)
          w.braced {
            w.w("try").bracedEnd(" DJINNI_TRANSLATE_EXCEPTIONS()") {
              val ret = m.ret.fold("")(_ => "auto r = ")
              val call = ret + (if (!m.static) "_cppRef.get()->" else cppSelf + "::") + idCpp.method(m.ident) + "("
              writeAlignedCall(w, call, m.params, ")", p => objcppMarshal.toCpp(p.ty, idObjc.local(p.ident.name)))
              w.wl(";")
              m.ret.fold()(r => w.wl(s"return ${objcppMarshal.fromCpp(r, "r")};"))
            }
          }
        }
        w.wl
        w.wl("@end")
      })
    }

    if (i.ext.objc) {
      val objcExtSelf = objcppMarshal.helperClass("objc_proxy")
      refs.body.add("#import " + q(spec.objcBaseLibIncludePrefix + "DJIObjcWrapperCache+Private.h"))
      refs.body.add("!#import " + q(spec.objcIncludePrivatePrefix + privateHeaderName(ident.name)))

      writeObjcFile(bodyName(ident.name), origin, refs.body, w => {
        arcAssert(w)
        w.wl
        wrapNamespace(w, Some(spec.objcppNamespace), w => {
          w.wl(s"class $helperClass::$objcExtSelf final")
          w.wl(s": public $cppSelf")
          w.wl(s", public ::djinni::DbxObjcWrapperCache<$objcExtSelf>::Handle") // Use base class to avoid name conflicts with user-defined methods having the same name as this new data member
          w.bracedSemi {
            w.wlOutdent("public:")
            w.wl("using Handle::Handle;")
            for (m <- i.methods) {
              val ret = cppMarshal.fqReturnType(m.ret)
              val params = m.params.map(p => cppMarshal.fqParamType(p.ty) + " " + idCpp.local(p.ident))
              w.wl(s"$ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")} override").braced {
                w.w("@autoreleasepool").braced {
                  val ret = m.ret.fold("")(_ => "auto r = ")
                  val call = ret + "[Handle::get() " + idObjc.method(m.ident)
                  writeAlignedObjcCall(w, ret + call, m.params, "]", p => (idObjc.field(p.ident), s"(${objcppMarshal.fromCpp(p.ty, idCpp.local(p.ident))})"))
                  w.wl(";")
                  m.ret.fold()(r => { w.wl(s"return ${objcppMarshal.toCpp(r, "r")};") })
                }
              }
            }
          }
          w.wl
          w.wl(s"auto $helperClass::toCpp(ObjcType objc) -> CppType")
          w.braced {
            w.wl(s"return objc ? ::djinni::DbxObjcWrapperCache<$objcExtSelf>::getInstance().get(objc) : nullptr;")
          }
          w.wl
          w.wl(s"auto $helperClass::fromCpp(const CppType& cpp) -> ObjcType")
          w.braced {
            w.wl(s"assert(!cpp || dynamic_cast<$objcExtSelf*>(cpp.get()));")
            w.wl(s"return cpp ? static_cast<$objcExtSelf&>(*cpp).Handle::get() : nil;")
          }
        })
      })
    }
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val refs = new ObjcRefs()
    for (c <- r.consts)
      refs.find(c.ty)
    for (f <- r.fields)
      refs.find(f.ty)

    val objcName = ident.name + (if (r.ext.objc) "_base" else "")
    val noBaseSelf = objcMarshal.typename(ident, r) // Used for constant names
    val self = objcMarshal.typename(objcName, r)
    val cppSelf = cppMarshal.fqTypename(ident, r)

    refs.header.add("#import <Foundation/Foundation.h>")

    refs.privHeader.add("!#import " + q(spec.objcIncludePrefix + headerName(objcName)))
    refs.privHeader.add("!#include " + q(spec.objcIncludeCppPrefix + spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))
    
    refs.body.add("#include <cassert>")
    refs.body.add("!#import " + q(spec.objcIncludePrivatePrefix + privateHeaderName(objcName)))

    if (r.ext.objc) {
      refs.body.add("#import " + q(spec.objcIncludePrefix + "../" + headerName(ident)))
      refs.header.add(s"@class ${objcMarshal.typename(ident, r)};")
    }

    def checkMutable(tm: MExpr): Boolean = tm.base match {
      case MOptional => checkMutable(tm.args.head)
      case MString => true
      case MBinary => true
      case _ => false
    }

    val helperClass = objcppMarshal.helperClass(ident)

    writeObjcFile(privateHeaderName(objcName), origin, refs.privHeader, w => {
      arcAssert(w)
      w.wl
      w.wl(s"@class $noBaseSelf;")
      w.wl
      wrapNamespace(w, Some(spec.objcppNamespace), w => {
        w.wl(s"struct $helperClass")
        w.bracedSemi {
          w.wl(s"using CppType = $cppSelf;")
          w.wl(s"using ObjcType = $noBaseSelf*;");
          w.wl
          w.wl(s"using Boxed = $helperClass;")
          w.wl
          w.wl(s"static CppType toCpp(ObjcType objc);")
          w.wl(s"static ObjcType fromCpp(const CppType& cpp);")
        }
      })
    })

    writeObjcFile(privateBodyName(objcName), origin, refs.body, w => {
      wrapNamespace(w, Some(spec.objcppNamespace), w => {
        w.wl(s"auto $helperClass::toCpp(ObjcType obj) -> CppType")
        w.braced {
          w.wl("assert(obj);")
          if(r.fields.isEmpty) w.wl("(void)obj; // Suppress warnings in relase builds for empty records")
          val call = "return CppType("
          writeAlignedCall(w, "return CppType(", r.fields, ")", f => objcppMarshal.toCpp(f.ty, "obj." + idObjc.field(f.ident)))
          w.wl(";")
        }
        w.wl
        w.wl(s"auto $helperClass::fromCpp(const CppType& cpp) -> ObjcType")
        w.braced {
          if(r.fields.isEmpty) w.wl("(void)cpp; // Suppress warnings in relase builds for empty records")
          val first = if(r.fields.isEmpty) "" else IdentStyle.camelUpper("with_" + r.fields.head.ident.name)
          val call = s"return [[$noBaseSelf alloc] init$first"
          writeAlignedObjcCall(w, call, r.fields, "]", f => (idObjc.field(f.ident), s"(${objcppMarshal.fromCpp(f.ty, "cpp." + idCpp.field(f.ident))})"))
          w.wl(";")
        }
      })
    })
 }

  def writeObjcFile(fileName: String, origin: String, refs: Iterable[String], f: IndentWriter => Unit) {
    createFile(spec.objcPrivateOutFolder.get, fileName, (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      if (refs.nonEmpty) {
        // Ignore the ! in front of each line; used to put own headers to the top
        // according to Objective-C style guide
        refs.foreach(s => w.wl(if (s.charAt(0) == '!') s.substring(1) else s))
        w.wl
      }
      f(w)
    })
  }
}
