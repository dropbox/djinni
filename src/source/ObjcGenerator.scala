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

  val marshal = new ObjcMarshal(spec)

  class ObjcRefs() {
    var body = mutable.TreeSet[String]()
    var header = mutable.TreeSet[String]()

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.foreach(find)
      find(tm.base)
    }
    def find(m: Meta) = for(r <- marshal.references(m)) r match {
      case ImportRef(arg) => header.add("#import " + arg)
      case DeclRef(decl, _) => header.add(decl)
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new ObjcRefs()

    refs.header.add("#import <Foundation/Foundation.h>")

    val self = marshal.typename(ident, e)
    writeObjcFile(marshal.headerName(ident), origin, refs.header, w => {
      writeDoc(w, doc)
      w.wl(s"typedef NS_ENUM(NSInteger, $self)")
      w.bracedSemi {
        for (i <- e.options) {
          writeDoc(w, i.doc)
          w.wl(self + idObjc.enum(i.ident.name) + ",")
        }
      }
    })
  }

  def bodyName(ident: String): String = idObjc.ty(ident) + "." + spec.objcppExt // Must be a Obj-C++ file in case the constants are not compile-time constant expressions

  def writeObjcConstVariable(w: IndentWriter, c: Const, s: String): Unit = {
    val nullability = marshal.nullability(c.ty.resolved).fold("")(" __" + _)
    val td = marshal.fqFieldType(c.ty) + nullability
    // MBinary | MList | MSet | MMap are not allowed for constants.
    w.w(s"${td} const $s${idObjc.const(c.ident)}")
  }

  def generateObjcConstants(w: IndentWriter, consts: Seq[Const], selfName: String) = {
    def boxedPrimitive(ty: TypeRef): String = {
      val (_, needRef) = toObjcType(ty)
      if (needRef) "@" else ""
    }
    def writeObjcConstValue(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(boxedPrimitive(ty) + l.toString)
      case d: Double if marshal.fieldType(ty) == "float" => w.w(boxedPrimitive(ty) + d.toString + "f")
      case d: Double => w.w(boxedPrimitive(ty) + d.toString)
      case b: Boolean => w.w(boxedPrimitive(ty) + (if (b) "YES" else "NO"))
      case s: String => w.w("@" + s)
      case e: EnumValue => w.w(idObjc.enum(e.ty + "_" + e.name))
      case v: ConstRef => w.w(selfName + idObjc.const (v.name))
      case z: Map[_, _] => { // Value is record
        val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        val head = record.fields.head
        w.w(s"[[${marshal.typename(ty)} alloc] initWith${IdentStyle.camelUpper(head.ident)}:")
        writeObjcConstValue(w, head.ty, vMap.apply(head.ident))
        w.nestedN(2) {
          val skipFirst = SkipFirst()
          for (f <- record.fields) skipFirst {
            w.wl
            w.w(s"${idObjc.field(f.ident)}:")
            writeObjcConstValue(w, f.ty, vMap.apply(f.ident))
          }
        }
        w.w("]")
      }
    }

    w.wl("#pragma clang diagnostic push")
    w.wl("#pragma clang diagnostic ignored " + q("-Wunused-variable"))
    for (c <- consts) {
      w.wl
      writeObjcConstVariable(w, c, selfName)
      w.w(s" = ")
      writeObjcConstValue(w, c.ty, c.value)
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

    val self = marshal.typename(ident, i)

    refs.header.add("#import <Foundation/Foundation.h>")

    def writeObjcFuncDecl(method: Interface.Method, w: IndentWriter) {
      val label = if (method.static) "+" else "-"
      val ret = marshal.returnType(method.ret)
      val decl = s"$label ($ret)${idObjc.method(method.ident)}"
      writeAlignedObjcCall(w, decl, method.params, "", p => (idObjc.field(p.ident), s"(${marshal.paramType(p.ty)})${idObjc.local(p.ident)}"))
    }

    writeObjcFile(marshal.headerName(ident), origin, refs.header, w => {
      writeDoc(w, doc)
      for (c <- i.consts) {
        writeDoc(w, c.doc)
        w.w(s"extern ")
        writeObjcConstVariable(w, c, self)
        w.wl(s";")
      }
      w.wl
      if (i.ext.objc) w.wl(s"@protocol $self") else w.wl(s"@interface $self : NSObject")
      for (m <- i.methods) {
        w.wl
        writeDoc(w, m.doc)
        writeObjcFuncDecl(m, w)
        w.wl(";")
      }
      w.wl
      w.wl("@end")
    })

    if (i.consts.nonEmpty) {
      refs.body.add("#import " + q(spec.objcIncludePrefix + marshal.headerName(ident)))
      writeObjcFile(bodyName(ident.name), origin, refs.body, w => {
        generateObjcConstants(w, i.consts, self)
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
    val noBaseSelf = marshal.typename(ident, r) // Used for constant names
    val self = marshal.typename(objcName, r)

    refs.header.add("#import <Foundation/Foundation.h>")
    refs.body.add("!#import " + q(spec.objcIncludePrefix + (if (r.ext.objc) "../" else "") + marshal.headerName(ident)))

    if (r.ext.objc) {
      refs.header.add(s"@class $noBaseSelf;")
    }

    def checkMutable(tm: MExpr): Boolean = tm.base match {
      case MOptional => checkMutable(tm.args.head)
      case MString => true
      case MBinary => true
      case _ => false
    }

    val firstInitializerArg = if(r.fields.isEmpty) "" else IdentStyle.camelUpper("with_" + r.fields.head.ident.name)

    writeObjcFile(marshal.headerName(objcName), origin, refs.header, w => {
      writeDoc(w, doc)
      w.wl(s"@interface $self : NSObject")

      def writeInitializer(sign: String, prefix: String) {
        val decl = s"$sign (nonnull instancetype)$prefix$firstInitializerArg"
        writeAlignedObjcCall(w, decl, r.fields, "", f => (idObjc.field(f.ident), s"(${marshal.paramType(f.ty)})${idObjc.local(f.ident)}"))
        w.wl(";")
      }

      writeInitializer("-", "init")
      if (!r.ext.objc) writeInitializer("+", IdentStyle.camelLower(objcName))

      for (f <- r.fields) {
        w.wl
        writeDoc(w, f.doc)
        val nullability = marshal.nullability(f.ty.resolved).fold("")(", " + _)
        w.wl(s"@property (nonatomic, readonly${nullability}) ${marshal.fqFieldType(f.ty)} ${idObjc.field(f.ident)};")
      }
      if (r.derivingTypes.contains(DerivingType.Ord)) {
        w.wl
        w.wl(s"- (NSComparisonResult)compare:(nonnull $self *)other;")
      }
      w.wl
      w.wl("@end")
      // Constants come last in case one of them is of the record's type
      if (r.consts.nonEmpty) {
        w.wl
        for (c <- r.consts) {
          writeDoc(w, c.doc)
          w.w(s"extern ")
          writeObjcConstVariable(w, c, noBaseSelf);
          w.wl(s";")
        }
      }
    })

    writeObjcFile(bodyName(objcName), origin, refs.body, w => {
      if (r.consts.nonEmpty) generateObjcConstants(w, r.consts, noBaseSelf)
      w.wl
      w.wl(s"@implementation $self")
      w.wl
      // Constructor from all fields (not copying)
      val init = s"- (nonnull instancetype)init$firstInitializerArg"
      writeAlignedObjcCall(w, init, r.fields, "", f => (idObjc.field(f.ident), s"(${marshal.paramType(f.ty)})${idObjc.local(f.ident)}"))
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

      // Convenience initializer
      if(!r.ext.objc) {
        val decl = s"+ (nonnull instancetype)${IdentStyle.camelLower(objcName)}$firstInitializerArg"
        writeAlignedObjcCall(w, decl, r.fields, "", f => (idObjc.field(f.ident), s"(${marshal.paramType(f.ty)})${idObjc.local(f.ident)}"))
        w.wl
        w.braced {
          val call = s"return [[self alloc] init$firstInitializerArg"
          writeAlignedObjcCall(w, call, r.fields, "", f => (idObjc.field(f.ident), s"${idObjc.local(f.ident)}"))
          w.wl("];")
        }
        w.wl
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
                      w.w(s"((self.${idObjc.field(f.ident)} == nil && typedOther.${idObjc.field(f.ident)} == nil) || ")
                      w.w(s"(self.${idObjc.field(f.ident)} != nil && [self.${idObjc.field(f.ident)} isEqual:typedOther.${idObjc.field(f.ident)}]))")
                  }
                case MString => w.w(s"[self.${idObjc.field(f.ident)} isEqualToString:typedOther.${idObjc.field(f.ident)}]")
                case t: MPrimitive => w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                case df: MDef => df.defType match {
                  case DRecord => w.w(s"[self.${idObjc.field(f.ident)} isEqual:typedOther.${idObjc.field(f.ident)}]")
                  case DEnum => w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                  case _ => throw new AssertionError("Unreachable")
                }
                case e: MExtern => e.defType match {
                  case DRecord => if(e.objc.pointer) {
                      w.w(s"[self.${idObjc.field(f.ident)} isEqual:typedOther.${idObjc.field(f.ident)}]")
                    } else {
                      w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                    }
                  case DEnum => w.w(s"self.${idObjc.field(f.ident)} == typedOther.${idObjc.field(f.ident)}")
                  case _ => throw new AssertionError("Unreachable")
                }
                case _ => throw new AssertionError("Unreachable")
              }
            }
          }
          w.wl(";")
        }
        w.wl

        w.wl("- (NSUInteger)hash")
        w.braced {
          w.w(s"return ").nestedN(2) {
            w.w(s"NSStringFromClass([self class]).hash")
            for (f <- r.fields) {
              w.wl(" ^")
              f.ty.resolved.base match {
                case MOptional =>
                  f.ty.resolved.args.head.base match {
                    case df: MDef if df.defType == DEnum =>
                      w.w(s"(NSUInteger)self.${idObjc.field(f.ident)}")
                    case _ => w.w(s"self.${idObjc.field(f.ident)}.hash")
                  }
                case t: MPrimitive => w.w(s"(NSUInteger)self.${idObjc.field(f.ident)}")
                case df: MDef => df.defType match {
                  case DEnum => w.w(s"(NSUInteger)self.${idObjc.field(f.ident)}")
                  case _ => w.w(s"self.${idObjc.field(f.ident)}.hash")
                }
                case e: MExtern => e.defType match {
                  case DEnum => w.w(s"(NSUInteger)self.${idObjc.field(f.ident)}")
                  case DRecord => w.w("(" + e.objc.hash.format("self." + idObjc.field(f.ident)) + ")")
                  case _ => throw new AssertionError("Unreachable")
                }
                case _ => w.w(s"self.${idObjc.field(f.ident)}.hash")
              }
            }
          }
          w.wl(";")
        }
        w.wl
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
              case e: MExtern => e.defType match {
                case DRecord => if(e.objc.pointer) w.wl(s"tempResult = [self.${idObjc.field(f.ident)} compare:other.${idObjc.field(f.ident)}];") else generatePrimitiveOrder(f.ident, w)
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
        w.wl
      }
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

  // TODO: this should be in ObjcMarshal
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
            case MDate => ("NSDate", true)
            case MBinary => ("NSData", true)
            case MOptional => throw new AssertionError("optional should have been special cased")
            case MList => ("NSArray", true)
            case MSet => ("NSSet", true)
            case MMap => ("NSDictionary", true)
            case d: MDef => d.defType match {
              case DEnum => if (needRef) ("NSNumber", true) else (idObjc.ty(d.name), false)
              case DRecord => (idObjc.ty(d.name), true)
              case DInterface =>
                val ext = d.body.asInstanceOf[Interface].ext
                if (ext.cpp) (s"${idObjc.ty(d.name)}*", false) else (s"id<${idObjc.ty(d.name)}>", false)
            }
            case e: MExtern => if(needRef) (e.objc.boxed, true) else (e.objc.typename, e.objc.pointer)
            case p: MParam => throw new AssertionError("Parameter should not happen at Obj-C top level")
          }
          base
      }
    }
    f(tm, needRef)
  }

  // TODO: this should be in ObjcMarshal
  def toObjcTypeDef(ty: TypeRef): String = toObjcTypeDef(ty.resolved, false)
  def toObjcTypeDef(ty: TypeRef, needRef: Boolean): String = toObjcTypeDef(ty.resolved, needRef)
  def toObjcTypeDef(tm: MExpr): String = toObjcTypeDef(tm, false)
  def toObjcTypeDef(tm: MExpr, needRef: Boolean): String = {
    val (name, asterisk) = toObjcType(tm, needRef)
    name + (if (asterisk) " *" else " ")
  }
}
