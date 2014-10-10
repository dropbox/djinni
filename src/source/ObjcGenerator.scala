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

class ObjcGenerator(spec: Spec) extends Generator(spec) {

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
        case d: MDef => d.defType match {
          case DEnum =>
            body.add("#import " + q(headerName(d.name)))
            body.add("#import " + q(enumTranslatorHeaderName(d.name)))
            header.add("#import " + q(headerName(d.name)))
          case DInterface =>
            header.add("#import <Foundation/Foundation.h>")
            header.add("#import " + q(headerName(d.name)))
            val ext = d.body.asInstanceOf[Interface].ext
            if (ext.cpp) body.add("#import " + q(privateHeaderName(d.name + "_cpp_proxy")))
            if (ext.objc) body.add("#import " + q(privateHeaderName(d.name + "_objc_proxy")))
          case DRecord =>
            val r = d.body.asInstanceOf[Record]
            val prefix = if (r.ext.objc) "../" else ""
            header.add("@class " + idObjc.ty(d.name) + ";")
            body.add("#import " + q(prefix + privateHeaderName(d.name)))
        }
        case p: MParam =>
      }
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new ObjcRefs()

    refs.header.add("#import <Foundation/Foundation.h>")

    refs.privHeader.add("#import <Foundation/Foundation.h>")
    refs.privHeader.add("!#import " + q(headerName(ident)))
    refs.privHeader.add("!#include " + q(spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))

    refs.body.add("#import <Foundation/Foundation.h>")
    refs.body.add("!#import " + q(enumTranslatorHeaderName(ident)))

    val self = idObjc.ty(ident)
    val cppSelf = withNs(spec.cppNamespace, idCpp.enumType(ident))
    val name = IdentStyle.camelUpper(ident)
    val argName = idObjc.local(ident.name)
    writeObjcFile(headerName(ident), origin, refs.header, w => {
      writeDoc(w, doc)
      w.wl(s"typedef NS_ENUM(NSInteger, $self)")
      w.bracedSemi {
        for (i <- e.options) {
          writeDoc(w, i.doc)
          w.wl(self + idObjc.enum(i.ident.name) + ",")
        }
        w.wl(self + "Count,")
      }
    })

    writeObjcFile(enumTranslatorHeaderName(ident), origin, refs.privHeader, w => {
      w.wl("@interface " + self + "Translator : NSObject")
      w.wl
      w.wl(s"+ ($self)cpp${name}ToObjc${name}:($cppSelf)$argName;")
      w.wl(s"+ ($cppSelf)objc${name}ToCpp${name}:($self)$argName;")
      w.wl
      w.wl("@end")
    })

    writeObjcFile(enumTranslatorName(ident), origin, refs.body, w => {
      w.wl(s"static_assert(__has_feature(objc_arc), " + q("Djinni requires ARC to be enabled for this file") + ");" )
      w.wl
      w.wl(s"@implementation " + self + "Translator")
      w.wl
      w.wl(s"+ ($self)cpp${name}ToObjc${name}:($cppSelf)${argName}")
      w.braced {
        w.wl(s"return static_cast<$self>($argName);");
      }
      w.wl
      w.wl(s"+ ($cppSelf)objc${name}ToCpp${name}:($self)${argName}")
      w.braced {
        w.wl(s"return static_cast<enum $cppSelf>($argName);")
      }
      w.wl
      w.wl("@end")
    })
  }

  def enumTranslatorName(ident: String): String = idObjc.ty(ident) + "Translator." + spec.objcExt
  def enumTranslatorHeaderName(ident: String): String = idObjc.ty(ident) + "Translator+Private." + spec.objcHeaderExt
  def headerName(ident: String): String = idObjc.ty(ident) + "." + spec.objcHeaderExt
  def privateHeaderName(ident: String): String = idObjc.ty(ident) + "+Private." + spec.objcHeaderExt
  def bodyName(ident: String): String = idObjc.ty(ident) + "." + spec.objcExt

  def generateObjcConstants(w: IndentWriter, consts: Seq[Const], selfName: String) = {
    def boxedPrimitive(ty: TypeRef): String = {
      val (_, needRef) = toObjcType(ty)
      if (needRef) "@" else ""
    }
    def writeObjcConst(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(boxedPrimitive(ty) + l.toString)
      case d: Double => w.w(boxedPrimitive(ty) + d.toString)
      case b: Boolean => w.w(boxedPrimitive(ty) + (if (b) "YES" else "NO"))
      case s: String => w.w("@" + s)
      case e: EnumValue => w.w(idObjc.enum(e.ty + "_" + e.name))
      case v: ConstRef => ty.resolved.base match {
        case MString => w.w("[" + selfName + idObjc.const (v.name) + " copy]")
        case _ => w.w(selfName + idObjc.const (v.name))
      }
      case z: Map[_, _] => { // Value is record
        val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        val head = record.fields.head
                w.w(s"[[${idObjc.ty(recordMdef.name)} alloc] initWith${IdentStyle.camelUpper(head.ident)}:")
        writeObjcConst(w, head.ty, vMap.apply(head.ident))
        w.nestedN(2) {
          val skipFirst = SkipFirst()
          for (f <- record.fields) skipFirst {
            w.wl
            w.w(s"${idObjc.field(f.ident)}:")
            writeObjcConst(w, f.ty, vMap.apply(f.ident))
          }
        }
        w.w("]")
      }
    }

    w.wl("#pragma clang diagnostic push")
    w.wl("#pragma clang diagnostic ignored " + q("-Wunused-variable"))
    for (c <- consts) {
      w.wl
      w.w(s"const ${toObjcTypeDef(c.ty)}$selfName${idObjc.const(c.ident)} = ")
      writeObjcConst(w, c.ty, c.value)
      w.wl(";")
    }
    w.wl
    w.wl("#pragma clang diagnostic pop")
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new ObjcRefs()
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    val self = idObjc.ty(ident)

    refs.privHeader.add("#import <Foundation/Foundation.h>")
    refs.privHeader.add("#include <memory>")
    refs.privHeader.add("!#import " + q(headerName(ident)))
    refs.privHeader.add("!#include " + q(spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))

    def writeObjcFuncDecl(method: Interface.Method, w: IndentWriter) {
      val label = if (method.static) "+" else "-"
      val ret = method.ret.fold("void")(toObjcFullType(_))
      w.w(s"$label ($ret)${idObjc.method(method.ident)}")
      val skipFirst = SkipFirst()
      for (p <- method.params) {
        skipFirst { w.w(s" ${idObjc.local(p.ident)}") }
        w.w(s":(${toObjcFullType(p.ty)})${idObjc.local(p.ident)}")
      }
    }

    writeObjcFile(headerName(ident), origin, refs.header, w => {
      writeDoc(w, doc)
      for (c <- i.consts) {
        writeDoc(w, c.doc)
        w.wl(s"extern const ${toObjcTypeDef(c.ty)}$self${idObjc.const(c.ident)};")
      }
      w.wl
      w.wl(s"@protocol $self")
      for (m <- i.methods) {
        w.wl
        writeDoc(w, m.doc)
        writeObjcFuncDecl(m, w)
        w.wl(";")
      }
      w.wl
      w.wl("@end")
    })

    refs.body.add("#import " + q(headerName(ident)))

    if (i.consts.nonEmpty) {
      writeObjcFile(bodyName(ident.name), origin, refs.body, w => {
        generateObjcConstants(w, i.consts, self)
      })
    }

    val cppExtName = ident.name + "_cpp_proxy"
    val cppExtSelf = idObjc.ty(cppExtName)
    val cppName = withNs(spec.cppNamespace, idCpp.ty(ident))
    if (i.ext.cpp) {
      refs.privHeader.add("!#import " + q(headerName(cppExtName)))
      refs.privHeader.add("#import " + q("DJICppWrapperCache+Private.h"))
      refs.body.add("#include <utility>")
      refs.body.add("#import " + q("DJIError.h"))
      refs.body.add("#include <exception>")
      refs.body.add("!#import " + q(privateHeaderName(cppExtName)))

      writeObjcFile(headerName(cppExtName), origin, mutable.TreeSet[String](), w => {
        w.wl("#import " + q(headerName(ident)))
        w.wl("#import <Foundation/Foundation.h>")
        w.wl
        w.wl(s"@interface $cppExtSelf : NSObject <$self>")
        w.wl("@end")
      })

      writeObjcFile(privateHeaderName(cppExtName), origin, refs.privHeader, w => {
        w.wl(s"@interface $cppExtSelf ()")
        w.wl
        w.wl(s"@property (nonatomic, readonly) std::shared_ptr<$cppName> cppRef;")
        w.wl
        w.wl(s"+ (id)${idObjc.method(ident.name + "_with_cpp")}:(const std::shared_ptr<$cppName> &)cppRef;")
        w.wl
        w.wl(s"- (id)initWithCpp:(const std::shared_ptr<$cppName> &)cppRef;")
        w.wl
        w.wl("@end")
      })

      writeObjcFile(bodyName(cppExtName), origin, refs.body, w => {
        w.wl(s"static_assert(__has_feature(objc_arc), " + q("Djinni requires ARC to be enabled for this file") + ");" )
        w.wl
        w.wl(s"@implementation $cppExtSelf")
        w.wl
        w.wl(s"- (id)initWithCpp:(const std::shared_ptr<$cppName> &)cppRef")
        w.braced {
          w.w("if (self = [super init])").braced {
            w.wl("_cppRef = cppRef;")
          }
          w.wl("return self;")
        }
        w.wl
        w.wl(s"- (void)dealloc")
        w.braced {
          w.wl(s"djinni::DbxCppWrapperCache<$cppName> & cache = djinni::DbxCppWrapperCache<$cppName>::getInstance();")
          w.wl("cache.remove(_cppRef);")
        }
        w.wl
        w.wl(s"+ (id)${idObjc.method(ident.name + "_with_cpp")}:(const std::shared_ptr<$cppName> &)cppRef")
        w.braced {
          w.wl(s"djinni::DbxCppWrapperCache<$cppName> & cache = djinni::DbxCppWrapperCache<$cppName>::getInstance();")
          w.wl(s"return cache.get(cppRef, [] (const std::shared_ptr<$cppName> & p) { return [[$cppExtSelf alloc] initWithCpp:p]; });")
         }
        for (m <- i.methods) {
          w.wl
          writeObjcFuncDecl(m, w)
          w.braced {
            w.wl("try {").nested {
              for (p <- m.params) {
                translateObjcTypeToCpp(idObjc.local("cpp_" + p.ident.name), idObjc.local(p.ident.name), p.ty, w)
              }
              val params = m.params.map(p => "std::move(" + idObjc.local("cpp_" + p.ident.name) + ")").mkString("(", ", ", ")")
              val cppRef = if (!m.static) "_cppRef->" else  cppName + "::"
              m.ret match {
                case None =>
                  w.wl(s"$cppRef${idCpp.method(m.ident)}$params;")
                case Some(r) =>
                  w.wl(s"${toCppType(r, spec.cppNamespace)} cppRet = $cppRef${idCpp.method(m.ident)}$params;")
                  translateCppTypeToObjc("objcRet", "cppRet", r, true, w)
                  w.wl("return objcRet;")
              }
            }
            w.wl("} DJINNI_TRANSLATE_EXCEPTIONS()")
          }
        }
        w.wl
        w.wl("@end")
      })
    }

    val objcExtName = ident.name + "_objc_proxy"
    val objcExtSelf = idCpp.ty(objcExtName)
    if (i.ext.objc) {
      refs.privHeader.add("#import " + q("DJIObjcWrapperCache+Private.h"))
      refs.body.add("!#import " + q(privateHeaderName(objcExtName)))
      writeObjcFile(privateHeaderName(objcExtName), origin, refs.privHeader, w => {
        w.wl(s"namespace ${spec.objcppNamespace}").braced {
          w.wl(s"class $objcExtSelf final : public ${withNs(spec.cppNamespace, idCpp.ty(ident))}").bracedSemi {
            w.wl("public:")
            w.wl(s"id <$self> objcRef;")
            w.wl(s"$objcExtSelf (id objcRef);")
            w.wl(s"virtual ~$objcExtSelf () override;")
            w.wl(s"static std::shared_ptr<${withNs(spec.cppNamespace, idCpp.ty(ident.name))}> ${idCpp.method(ident.name + "_with_objc")} (id objcRef);")
            for (m <- i.methods) {
              val ret = m.ret.fold("void")(toCppType(_))
              val params = m.params.map(p => "const " + toCppType(p.ty, spec.cppNamespace) + " & " + idCpp.local(p.ident))
              w.wl(s"virtual $ret ${idCpp.method(m.ident)} ${params.mkString("(", ", ", ")")} override;")
            }
            w.wl
            w.wl("private:")
            w.wl(s"$objcExtSelf () {};")
          }
        }
      })

      writeObjcFile(bodyName(objcExtName), origin, refs.body, w => {
        w.wl(s"namespace ${spec.objcppNamespace}").braced {
          w.wl(s"$objcExtSelf::$objcExtSelf (id objcRef)").braced {
            w.wl(s"assert([[objcRef class] conformsToProtocol:@protocol($self)]);")
            w.wl("this->objcRef = objcRef;")
          }
          w.wl
          w.wl(s"$objcExtSelf::~$objcExtSelf ()").braced {
            w.wl(s"djinni::DbxObjcWrapperCache<$objcExtSelf> & cache = djinni::DbxObjcWrapperCache<$objcExtSelf>::getInstance();")
            w.wl(s"cache.remove(objcRef);")
          }
          w.wl
          w.wl(s"std::shared_ptr<${withNs(spec.cppNamespace, idCpp.ty(ident))}> $objcExtSelf::${idCpp.method(ident.name + "_with_objc")} (id objcRef)").braced {
            w.wl(s"djinni::DbxObjcWrapperCache<$objcExtSelf> & cache = djinni::DbxObjcWrapperCache<$objcExtSelf>::getInstance();")
            w.wl(s"return static_cast<std::shared_ptr<${withNs(spec.cppNamespace, idCpp.ty(ident.name))}>>(cache.get(objcRef));")
          }
          for (m <- i.methods) {
            w.wl
            val ret = m.ret.fold("void")(toCppType(_))
            val params = m.params.map(p => "const " + toCppType(p.ty, spec.cppNamespace) + " & " + idCpp.local(p.ident))
            w.wl(s"$ret $objcExtSelf::${idCpp.method(m.ident)} ${params.mkString("(", ", ", ")")}").braced {
              m.params.foreach(p =>
                translateCppTypeToObjc(idCpp.local("cpp_" + p.ident.name), idCpp.local(p.ident), p.ty, true, w))
              m.ret.fold()(r => w.w(toObjcTypeDef(r) + "objcRet = "))
              w.w("[objcRef " + idObjc.method(m.ident))
              val skipFirst = SkipFirst()
              for (p <- m.params) {
                skipFirst { w.w(" " + idObjc.local(p.ident)) }
                w.w(":" + idCpp.local("cpp_" + p.ident.name))
              }
              w.wl("];")
              m.ret.fold()(r => {
                translateObjcTypeToCpp("cppRet", "objcRet", r, w)
                w.wl("return cppRet;")
              })
            }
          }
        }
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
    val noBaseSelf = idObjc.ty(ident) // Used for constant names
    val self = idObjc.ty(objcName)
    val cppSelf = withNs(spec.cppNamespace, idCpp.ty(ident))

    refs.header.add("#import <Foundation/Foundation.h>")

    refs.privHeader.add("#import <Foundation/Foundation.h>")
    refs.privHeader.add("!#import " + q(headerName(objcName)))
    refs.privHeader.add("!#include " + q(spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt))

    refs.body.add("#import <Foundation/Foundation.h>")
    refs.body.add("#include <utility>")
    refs.body.add("!#import " + q(privateHeaderName(objcName)))

    if (r.ext.objc) {
      refs.body.add("#import " + q("../" + headerName(ident)))
      refs.privHeader.add("#import " + q("../" + headerName(ident)))
    }

    def checkMutable(tm: MExpr): Boolean = tm.base match {
      case MOptional => checkMutable(tm.args.head)
      case MString => true
      case MBinary => true
      case _ => false
    }

    writeObjcFile(headerName(objcName), origin, refs.header, w => {
      writeDoc(w, doc)
      for (c <- r.consts) {
        writeDoc(w, c.doc)
        w.wl(s"extern const ${toObjcTypeDef(c.ty)}$noBaseSelf${idObjc.const(c.ident)};")
      }
      w.wl
      w.wl(s"@interface $self : NSObject")
      for (f <- r.fields) {
        w.wl
        writeDoc(w, f.doc)
        w.wl(s"@property (nonatomic, readonly) ${toObjcTypeDef(f.ty)}${idObjc.field(f.ident)};")
      }
      if (r.derivingTypes.contains(DerivingType.Ord)) {
        w.wl
        w.wl(s"- (NSComparisonResult)compare:($self *)other;")
      }
      w.wl
      w.wl("@end")
    })

    writeObjcFile(privateHeaderName(objcName), origin, refs.privHeader, w => {
      w.wl(s"@interface $self ()")
      w.wl
      // Deep copy constructor
      w.wl(s"- (id)${idObjc.method("init_with_" + ident.name)}:(${idObjc.ty(ident)} *)${idObjc.field(ident)};")
      if (!r.fields.isEmpty) {
        val head = r.fields.head
        val skipFirst = SkipFirst()
        w.w(s"- (id)${idObjc.method("init_with_" + head.ident.name)}:(${toObjcFullType(head.ty)})${idObjc.local(head.ident)}")
        for (f <- r.fields) skipFirst {
          w.w(s" ${idObjc.field(f.ident)}:(${toObjcFullType(f.ty)})${idObjc.field(f.ident)}")
        }
        w.wl(";")
      }
      w.wl(s"- (id)${idObjc.method("init_with_cpp_" + ident.name)}:(const $cppSelf &)${idObjc.local(ident)};")
      w.wl(s"- ($cppSelf)${idObjc.method("cpp_" + ident.name)};")
      w.wl
      w.wl("@end")
    })

    writeObjcFile(bodyName(objcName), origin, refs.body, w => {
      if (r.consts.nonEmpty) generateObjcConstants(w, r.consts, noBaseSelf)
      w.wl(s"static_assert(__has_feature(objc_arc), " + q("Djinni requires ARC to be enabled for this file") + ");" )
      w.wl
      w.wl(s"@implementation $self")
      w.wl
      // Deep copy construtor
      w.wl(s"- (id)${idObjc.method("init_with_" + ident.name)}:(${idObjc.ty(ident)} *)${idObjc.local(ident)}")
      w.braced {
        w.w("if (self = [super init])").braced {
          for (f <- r.fields) {
            copyObjcValue(s"_${idObjc.field(f.ident)}", s"${idObjc.local(ident)}.${idObjc.field(f.ident)}", f.ty, w)
          }
        }
        w.wl("return self;")
      }
      w.wl
      // Constructor from all fields (not copying)
      if (!r.fields.isEmpty) {
        val head = r.fields.head
        val skipFirst = SkipFirst()
        w.w(s"- (id)${idObjc.method("init_with_" + head.ident.name)}:(${toObjcFullType(head.ty)})${idObjc.field(head.ident)}")
        for (f <- r.fields) skipFirst {
          w.w(s" ${idObjc.field(f.ident.name)}:(${toObjcFullType(f.ty)})${idObjc.local(f.ident)}")
        }
        w.wl
        w.braced {
          w.w("if (self = [super init])").braced {
            for (f <- r.fields) {
              if (checkMutable(f.ty.resolved))
                w.wl(s"_${idObjc.field(f.ident)} = [${idObjc.local(f.ident)} copy];")
              else
                w.wl(s"_${idObjc.field(f.ident)} = ${idObjc.local(f.ident)};")
            }
          }
          w.wl("return self;")
        }
        w.wl
      }
      // Cpp -> Objc translator
      w.wl(s"- (id)${idObjc.method("init_with_cpp_" + ident.name)}:(const $cppSelf &)${idObjc.local(ident)}")
      w.braced {
        w.w("if (self = [super init])").braced {
          for (f <- r.fields) {
            translateCppTypeToObjc("_" + idObjc.field(f.ident), idObjc.local(ident) + "." + idCpp.field(f.ident), f.ty, false, w)
          }
        }
        w.wl("return self;")
      }
      w.wl
      // Objc -> Cpp translator
      w.wl(s"- ($cppSelf)${idObjc.method("cpp_" + ident.name)}")
      w.braced {
        for (f <- r.fields) {
          translateObjcTypeToCpp(idObjc.local(f.ident), "_" + idObjc.field(f.ident), f.ty, w)
        }
        val skipFirst = SkipFirst()
        w.wl(s"return $cppSelf(").nestedN(2) {
          for (f <- r.fields)
          {
            skipFirst { w.wl(",") }
            w.w("std::move(" + idObjc.local(f.ident) + ")")
          }
        }
        w.wl(");")
      }

      if (r.derivingTypes.contains(DerivingType.Eq)) {
        w.wl("- (BOOL)isEqual:(id)other")
        w.braced {
          w.w(s"if (![other isKindOfClass:[$self class]])").braced {
            w.wl("return NO;")
          }
          w.wl(s"$self *typedOther = ($self *)other;")
          val skipFirst = SkipFirst()
          w.w(s"return ").nestedN(2) {
            for (f <- r.fields) {
              skipFirst { w.wl(" &&") }
              f.ty.resolved.base match {
                case MBinary => w.w(s"[self.${idObjc.field(f.ident)} isEqualToData:typedOther.${idObjc.field(f.ident)}]")
                case MList => w.w(s"[self.${idObjc.field(f.ident)} isEqualToArray:typedOther.${idObjc.field(f.ident)}]")
                case MSet => w.w(s"[self.${idObjc.field(f.ident)} isEqualToSet:typedOther.${idObjc.field(f.ident)}]")
                case MMap => w.w(s"[self.${idObjc.field(f.ident)} isEqualToDictionary:typedOther.${idObjc.field(f.ident)}]")
                case MOptional =>
                  f.ty.resolved.args.head.base match {
                    case df: MDef if df.defType == DEnum =>
                      w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                    case _ =>
                      w.w(s"((self.${idObjc.field(f.ident)} == nil && typedOther.${idObjc.field(f.ident)}] == nil) || ")
                      w.w(s"(self.${idObjc.field(f.ident)} != nil && [self.${idObjc.field(f.ident)} isEqual:typedOther.${idObjc.field(f.ident)}]))")
                  }
                case MString => w.w(s"[self.${idObjc.field(f.ident)} isEqualToString:typedOther.${idObjc.field(f.ident)}]")
                case t: MPrimitive => w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                case df: MDef => df.defType match {
                  case DRecord => w.w(s"[self.${idObjc.field(f.ident)} isEqual:typedOther.${idObjc.field(f.ident)}]")
                  case DEnum => w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                  case _ => throw new AssertionError("Unreachable")
                }
                case _ => throw new AssertionError("Unreachable")
              }
            }
          }
          w.wl(";")
        }
      }

      def generatePrimitiveOrder(ident: Ident, w: IndentWriter): Unit = {
        w.wl(s"if (self.${idObjc.field(ident)} < other.${idObjc.field(ident)}) {").nested {
          w.wl(s"tempResult = NSOrderedAscending;")
        }
        w.wl(s"} else if (self.${idObjc.field(ident)} > other.${idObjc.field(ident)}) {").nested {
          w.wl(s"tempResult = NSOrderedDescending;")
        }
        w.wl(s"} else {").nested {
          w.wl(s"tempResult = NSOrderedSame;")
        }
        w.wl("}")
      }
      if (r.derivingTypes.contains(DerivingType.Ord)) {
        w.wl(s"- (NSComparisonResult)compare:($self *)other")
        w.braced {
          w.wl("NSComparisonResult tempResult;")
          for (f <- r.fields) {
            f.ty.resolved.base match {
              case MString => w.wl(s"tempResult = [self.${idObjc.field(f.ident)} compare:other.${idObjc.field(f.ident)}];")
              case t: MPrimitive => generatePrimitiveOrder(f.ident, w)
              case df: MDef => df.defType match {
                case DRecord => w.wl(s"tempResult = [self.${idObjc.field(f.ident)} compare:other.${idObjc.field(f.ident)}];")
                case DEnum => generatePrimitiveOrder(f.ident, w)
                case _ => throw new AssertionError("Unreachable")
              }
              case _ => throw new AssertionError("Unreachable")
            }
            w.w("if (tempResult != NSOrderedSame)").braced {
              w.wl("return tempResult;")
            }
          }
          w.wl("return NSOrderedSame;")
        }
      }
      w.wl
      w.wl("@end")
    })
  }

  def writeObjcFile(fileName: String, origin: String, refs: Iterable[String], f: IndentWriter => Unit) {
    createFile(spec.objcOutFolder.get, fileName, (w: IndentWriter) => {
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

  def copyObjcValue(to: String, from: String, ty: TypeRef, w: IndentWriter): Unit =
    copyObjcValue(to, from, ty.resolved, w)
  def copyObjcValue(to: String, from: String, tm: MExpr, w: IndentWriter): Unit = {
    def f(to: String, from: String, tm: MExpr, needRef: Boolean, w: IndentWriter, valueLevel: Int): Unit = {
      tm.base match {
        case MOptional => {
          w.wl(s"if ($from == nil) {").nested {
            w.wl(s"$to = nil;")
          }
          w.wl("} else {").nested {
            f(to, from, tm.args.head, true, w, valueLevel)
          }
          w.wl("}")
        }
        case p: MPrimitive => w.wl(s"$to = $from;") // NSNumber is immutable, so are primitive values
        case MString => w.wl(s"$to = [$from copy];")
        case MBinary => w.wl(s"$to = [$from copy];")
        case MList => {
          val copyName = "copiedValue_" + valueLevel
          val currentName = "currentValue_" + valueLevel
          w.wl(s"$to = [NSMutableArray arrayWithCapacity:[$from count]];")
          w.w(s"for (${toObjcTypeDef(tm.args.head, true)}$currentName in $from)").braced {
            w.wl(s"id $copyName;")
            f(copyName, currentName, tm.args.head, true, w, valueLevel + 1)
            w.wl(s"[$to addObject:$copyName];")
          }
        }
        case MSet => {
          val copyName = "copiedValue_" + valueLevel
          val currentName = "currentValue_" + valueLevel
          w.wl(s"$to = [NSMutableSet setWithCapacity:[$from count]];")
          w.w(s"for (${toObjcTypeDef(tm.args.head, true)}$currentName in $from)").braced {
            w.wl(s"id $copyName;")
            f(copyName, currentName, tm.args.head, true, w, valueLevel + 1)
            w.wl(s"[$to addObject:$copyName];")
          }
        }
        case MMap => {
          w.wl(s"$to = [NSMutableDictionary dictionaryWithCapacity:[$from count]];")
          val keyName = "key_" + valueLevel
          val valueName = "value_" + valueLevel
          val copiedKeyName = "copiedKey_" + valueLevel
          val copiedValueName = "copiedValue_" + valueLevel
          w.w(s"for (id $keyName in $from)").braced {
            w.wl(s"id $copiedKeyName, $copiedValueName;")
            f(copiedKeyName, keyName, tm.args.apply(0), true, w, valueLevel + 1)
            w.wl(s"id $valueName = [$from objectForKey:$keyName];")
            f(copiedValueName, valueName, tm.args.apply(1), true, w, valueLevel + 1)
            w.wl(s"[$to setObject:$copiedValueName forKey:$copiedKeyName];")
          }
        }
        case d: MDef => {
          val typeName = d.name
          val self = idObjc.ty(typeName)
          d.defType match {
            case DEnum => w.wl(s"$to = $from;")
            case DRecord => w.wl(s"$to = [[${idObjc.ty(d.name)} alloc] ${idObjc.method("init_with_" + d.name)}:$from];")
            case DInterface => w.wl(s"$to = $from;")
          }
        }
        case p: MParam =>
      }
    }
    f(to, from, tm, false, w, 0)
  }

  def translateCppTypeToObjc(objcIdent: String, cppIdent: String, ty: TypeRef, withDecl: Boolean, w: IndentWriter): Unit =
    translateCppTypeToObjc(objcIdent, cppIdent, ty.resolved, withDecl, w)
  def translateCppTypeToObjc(objcIdent: String, cppIdent: String, tm: MExpr, withDecl: Boolean, w: IndentWriter): Unit = {
    def f(objcIdent: String, cppIdent: String, tm: MExpr, needRef: Boolean, withDecl: Boolean, w: IndentWriter, valueLevel: Int): Unit = {
      val objcType = if (withDecl) toObjcTypeDef(tm, needRef) else ""
      tm.base match {
        case MOptional =>
          // We use "nil" for the empty optional.
          assert(tm.args.size == 1)
          val arg = tm.args.head
          arg.base match {
            case MOptional => throw new AssertionError("nested optional?")
            case m =>
              if (withDecl) w.wl(s"$objcType$objcIdent;")
              w.wl(s"if ($cppIdent) {").nested {
                f(objcIdent, "(*(" + cppIdent + "))", arg, true, false, w, valueLevel)
              }
              w.wl("} else {").nested {
                m match {
                  case d: MDef if d.defType == DEnum => w.wl(s"$objcIdent = [NSNumber numberWithInt:-1];");
                  case _ => w.wl(s"$objcIdent = nil;")
                }
              }
              w.wl("}")
          }
        case o => o match {
          case p: MPrimitive =>
            if (needRef)
              p.idlName match {
                case "i8" => w.wl(s"$objcType$objcIdent = [NSNumber numberWithChar:$cppIdent];")
                case "i16" => w.wl(s"$objcType$objcIdent = [NSNumber numberWithShort:$cppIdent];")
                case "i32" => w.wl(s"$objcType$objcIdent = [NSNumber numberWithInt:$cppIdent];")
                case "i64" => w.wl(s"$objcType$objcIdent = [NSNumber numberWithLongLong:$cppIdent];")
                case "f64" => w.wl(s"$objcType$objcIdent = [NSNumber numberWithDouble:$cppIdent];")
                case "bool" => w.wl(s"$objcType$objcIdent = [NSNumber numberWithBool:(($cppIdent) ? YES : NO)];")
              }
            else
              p.idlName match {
                case "bool" => w.wl(s"$objcType$objcIdent = ($cppIdent) ? YES : NO;")
                case _ => w.wl(s"$objcType$objcIdent = $cppIdent;")
              }
          case MString => w.wl(s"$objcType$objcIdent = [[NSString alloc] initWithBytes:$cppIdent.data()").nestedN(2) {
            w.wl(s"length:$cppIdent.length()")
            w.wl("encoding:NSUTF8StringEncoding];")
          }
          case MBinary => w.wl(s"$objcType$objcIdent = [NSData dataWithBytes:(&$cppIdent[0]) length:($cppIdent.size())];")
          case MOptional => throw new AssertionError("optional should have been special cased")
          case MList =>
            val objcName = "objcValue_" + valueLevel
            val cppName = "cppValue_" + valueLevel
            w.wl(s"$objcType$objcIdent = [NSMutableArray arrayWithCapacity:${cppIdent}.size()];")
            w.w(s"for (const auto & $cppName : ${cppIdent})")
            w.braced {
              f(objcName, cppName, tm.args.head, true, true, w, valueLevel + 1)
              w.wl(s"[$objcIdent addObject:$objcName];")
            }
          case MSet =>
            val objcName = "objcValue_" + valueLevel
            val cppName = "cppValue_" + valueLevel
            w.wl(s"$objcType$objcIdent = [NSMutableSet setWithCapacity:${cppIdent}.size()];")
            w.w(s"for (const auto & $cppName : ${cppIdent})")
            w.braced {
              f(objcName, cppName, tm.args.head, true, true, w, valueLevel + 1)
              w.wl(s"[$objcIdent addObject:$objcName];")
            }
          case MMap => {
            val cppPairName = "cppPair_" + valueLevel
            val objcKeyName = "objcKey_" + valueLevel
            val objcValueName = "objcValue_" + valueLevel
            w.wl(s"$objcType$objcIdent = [NSMutableDictionary dictionaryWithCapacity:${cppIdent}.size()];")
            w.w(s"for (const auto & $cppPairName : ${cppIdent})").braced {
              f(objcKeyName, cppPairName + ".first", tm.args.apply(0), true, true, w, valueLevel + 1)
              f(objcValueName, cppPairName + ".second", tm.args.apply(1), true, true, w, valueLevel + 1)
              w.wl(s"[$objcIdent setObject:$objcValueName forKey:$objcKeyName];")
            }
          }
          case d: MDef => {
            val typeName = d.name
            val self = idObjc.ty(typeName)
            d.defType match {
              case DEnum =>
                val objcEnum = s"[${self}Translator ${idObjc.method("cpp_" + typeName + "_to_objc_" + typeName)}:$cppIdent]"
                if (needRef)
                  w.wl(s"$objcType$objcIdent = [NSNumber numberWithInt:$objcEnum];")
                else
                  w.wl(s"$objcType$objcIdent = $objcEnum;")
              case DRecord => w.wl(s"$objcType$objcIdent = [[${self} alloc] initWithCpp${IdentStyle.camelUpper(typeName)}:$cppIdent];")
              case DInterface =>
                val ext = d.body.asInstanceOf[Interface].ext
                val objcProxy = withNs(Some(spec.objcppNamespace), idCpp.ty(d.name + "_objc_proxy"))
                (ext.cpp, ext.objc) match {
                  case (true, true) => throw new AssertionError("Function implemented on both sides")
                  case (false, false) => throw new AssertionError("Function not implemented")
                  case (true, false) => w.wl(s"$objcType$objcIdent = [${idObjc.ty(d.name + "_cpp_proxy")} ${idObjc.method(d.name + "_with_cpp")}:$cppIdent];")
                  case (false, true) => w.wl(s"$objcType$objcIdent = std::static_pointer_cast<$objcProxy>($cppIdent)->objcRef;")
                }
            }
          }
          case p: MParam =>
        }
      }
    }
    f(objcIdent, cppIdent, tm, false, withDecl, w, 0)
  }

  def translateObjcTypeToCpp(cppIdent: String, objcIdent: String, ty: TypeRef, w: IndentWriter): Unit =
    translateObjcTypeToCpp(cppIdent, objcIdent, ty.resolved, w)
  def translateObjcTypeToCpp(cppIdent: String, objcIdent: String, tm: MExpr, w: IndentWriter): Unit = {
    def f(cppIdent: String, objcIdent: String, tm: MExpr, needRef: Boolean, w: IndentWriter, valueLevel: Int): Unit = {
      val cppType = toCppType(tm, spec.cppNamespace)
      tm.base match {
        case MOptional =>
          // We use "nil" for the empty optional.
          assert(tm.args.size == 1)
          val arg = tm.args.head
          arg.base match {
            case MOptional => throw new AssertionError("nested optional?")
            case m =>
              w.wl(s"$cppType $cppIdent = nullopt;")
              m match {
                case d: MDef if d.defType == DEnum =>
                  val enumVal = if (needRef) objcIdent else s"static_cast<${idObjc.ty(d.name)}>([$objcIdent intValue])"
                  w.w(s"if ($enumVal != static_cast<${idObjc.ty(d.name)}>(-1))")
                case _ => w.w(s"if ($objcIdent != nil)")
              }
              w.braced {
                f("optValue", objcIdent, arg, true, w, 0)
                w.wl(s"$cppIdent = optValue;")
              }
          }
        case o => o match {
          case p: MPrimitive =>
            if (needRef)
              p.idlName match {
                case "i8" => w.wl(s"$cppType $cppIdent = [$objcIdent charValue];")
                case "i16" => w.wl(s"$cppType $cppIdent = [$objcIdent shortValue];")
                case "i32" => w.wl(s"$cppType $cppIdent = [$objcIdent intValue];")
                case "i64" => w.wl(s"$cppType $cppIdent = [$objcIdent longLongValue];")
                case "f64" => w.wl(s"$cppType $cppIdent = [$objcIdent doubleValue];")
                case "bool" => w.wl(s"$cppType $cppIdent = [$objcIdent boolValue];")
              }
            else
              w.wl(s"$cppType $cppIdent = $objcIdent;")
          case MString => w.wl(s"$cppType $cppIdent([$objcIdent UTF8String], [$objcIdent lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);")
          case MBinary =>
            w.wl(s"$cppType $cppIdent([$objcIdent length]);")
            w.wl(s"[$objcIdent getBytes:(static_cast<void *>($cppIdent.data())) length:[$objcIdent length]];")
          case MOptional => throw new AssertionError("optional should have been special cased")
          case MList =>
            val cppName = "cppValue_" + valueLevel
            val objcName = "objcValue_" + valueLevel
            w.wl(s"$cppType $cppIdent;")
            w.wl(s"$cppIdent.reserve([$objcIdent count]);")
            w.w(s"for (${toObjcTypeDef(tm.args.head, true)}$objcName in $objcIdent)").braced {
              f(cppName, objcName, tm.args.head, true, w, valueLevel + 1)
              w.wl(s"$cppIdent.push_back(std::move($cppName));")
            }
          case MSet =>
            val cppName = "cppValue_" + valueLevel
            val objcName = "objcValue_" + valueLevel
            w.wl(s"$cppType $cppIdent;")
            w.w(s"for (${toObjcTypeDef(tm.args.head, true)}$objcName in $objcIdent)").braced {
              f(cppName, objcName, tm.args.head, true, w, valueLevel + 1)
              w.wl(s"$cppIdent.insert(std::move($cppName));")
            }
          case MMap =>
            val objcKeyName = "objcKey_" + valueLevel
            val cppKeyName = "cppKey_" + valueLevel
            val cppValueName = "cppValue_" + valueLevel
            w.wl(s"$cppType $cppIdent;")
            w.w(s"for (id $objcKeyName in $objcIdent)").braced {
              f(cppKeyName, objcKeyName, tm.args.apply(0), true, w, valueLevel + 1)
              f(cppValueName, s"[$objcIdent objectForKey:$objcKeyName]", tm.args.apply(1), true, w, valueLevel + 1)
              w.wl(s"$cppIdent.emplace(std::move($cppKeyName), std::move($cppValueName));")
            }
          case d: MDef => {
            val typeName = d.name
            val self = idObjc.ty(typeName)
            d.defType match {
              case DEnum =>
                val unboxed = if (needRef) s"($self)[$objcIdent intValue]" else objcIdent
                w.wl(s"$cppType $cppIdent = [${self}Translator ${idObjc.method("objc_" + typeName + "_to_cpp_" + typeName)}:$unboxed];")
              case DRecord => w.wl(s"$cppType $cppIdent = std::move([$objcIdent cpp${IdentStyle.camelUpper(typeName)}]);")
              case DInterface =>
                val ext = d.body.asInstanceOf[Interface].ext
                (ext.cpp, ext.objc) match {
                  case (true, true) => throw new AssertionError("Function implemented on both sides")
                  case (false, false) => throw new AssertionError("Function not implemented")
                  case (true, false) => w.wl(s"$cppType $cppIdent = [(${idObjc.ty(d.name + "_cpp_proxy")} *)$objcIdent cppRef];")
                  case (false, true) => w.wl(s"$cppType $cppIdent = ${withNs(Some(spec.objcppNamespace), idCpp.ty(d.name + "_objc_proxy"))}" +
                    s"::${idCpp.method(d.name + "_with_objc")}($objcIdent);")
                }
            }
          }
          case p: MParam =>
        }
      }
    }
    f(cppIdent, objcIdent, tm, false, w, 0)
  }

  // Return value: (Type_Name, Is_Class_Or_Not)
  def toObjcType(ty: TypeRef): (String, Boolean) = toObjcType(ty.resolved, false)
  def toObjcType(ty: TypeRef, needRef: Boolean): (String, Boolean) = toObjcType(ty.resolved, needRef)
  def toObjcType(tm: MExpr): (String, Boolean) = toObjcType(tm, false)
  def toObjcType(tm: MExpr, needRef: Boolean): (String, Boolean) = {
    def f(tm: MExpr, needRef: Boolean): (String, Boolean) = {
      tm.base match {
        case MOptional =>
          // We use "nil" for the empty optional.
          assert(tm.args.size == 1)
          val arg = tm.args.head
          arg.base match {
            case MOptional => throw new AssertionError("nested optional?")
            case m => f(arg, true)
          }
        case o =>
          val base = o match {
            case p: MPrimitive => if (needRef) (p.objcBoxed, true) else (p.objcName, false)
            case MString => ("NSString", true)
            case MBinary => ("NSData", true)
            case MOptional => throw new AssertionError("optional should have been special cased")
            case MList => ("NSMutableArray", true)
            case MSet => ("NSMutableSet", true)
            case MMap => ("NSMutableDictionary", true)
            case d: MDef => d.defType match {
              case DEnum => if (needRef) ("NSNumber", true) else (idObjc.ty(d.name), false)
              case DRecord => (idObjc.ty(d.name), true)
              case DInterface => (s"id <${idObjc.ty(d.name)}>", false)
            }
            case p: MParam => throw new AssertionError("Parameter should not happen at Obj-C top level")
          }
          base
      }
    }
    f(tm, needRef)
  }

  def toObjcFullType(ty: TypeRef): String = toObjcFullType(ty.resolved, false)
  def toObjcFullType(ty: TypeRef, needRef: Boolean): String = toObjcFullType(ty.resolved, needRef)
  def toObjcFullType(tm: MExpr): String = toObjcFullType(tm, false)
  def toObjcFullType(tm: MExpr, needRef: Boolean = false): String = {
    val (name, asterisk) = toObjcType(tm, needRef)
    name + (if (asterisk) " *" else "")
  }

  def toObjcTypeDef(ty: TypeRef): String = toObjcTypeDef(ty.resolved, false)
  def toObjcTypeDef(ty: TypeRef, needRef: Boolean): String = toObjcTypeDef(ty.resolved, needRef)
  def toObjcTypeDef(tm: MExpr): String = toObjcTypeDef(tm, false)
  def toObjcTypeDef(tm: MExpr, needRef: Boolean): String = {
    val (name, asterisk) = toObjcType(tm, needRef)
    name + (if (asterisk) " *" else " ")
  }

}
