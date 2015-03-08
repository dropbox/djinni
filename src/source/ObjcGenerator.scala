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
      tm.args.map(find).mkString("<", ",", ">")
      tm.base match {
        case o: MOpaque =>
          header.add("#import <Foundation/Foundation.h>")
        case d: MDef => d.defType match {
          case DEnum =>
            header.add("#import " + q(spec.objcIncludePrefix + headerName(d.name)))
          case DInterface =>
            header.add("#import <Foundation/Foundation.h>")
            val ext = d.body.asInstanceOf[Interface].ext
            if (ext.cpp) {
              header.add("@class " + marshal.typename(tm) + ";")
              body.add("#import " + q(spec.objcIncludePrefix + headerName(d.name)))
            }
            if (ext.objc) {
              header.add("@protocol " + marshal.typename(tm) + ";")
              body.add("#import " + q(spec.objcIncludePrefix + headerName(d.name)))
            }
          case DRecord =>
            val r = d.body.asInstanceOf[Record]
            val prefix = if (r.ext.objc) "../" else ""
            header.add("@class " + marshal.typename(tm) + ";")
            body.add("#import " + q(spec.objcIncludePrefix + prefix + headerName(d.name)))
        }
        case p: MParam =>
      }
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new ObjcRefs()

    refs.header.add("#import <Foundation/Foundation.h>")

    val self = marshal.typename(ident, e)
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
  }

  def headerName(ident: String): String = idObjc.ty(ident) + "." + spec.objcHeaderExt
  def bodyName(ident: String): String = idObjc.ty(ident) + "." + spec.objcExt

  def writeObjcConstVariable(w: IndentWriter, c: Const, s: String): Unit = c.ty.resolved.base match {
    // MBinary | MList | MSet | MMap are not allowed for constants.
    // Primitives should be `const type`. All others are pointers and should be `type * const`
    case t: MPrimitive => w.w(s"const ${marshal.fqFieldType(c.ty)} $s${idObjc.const(c.ident)}")
    case _ => w.w(s"${marshal.fqFieldType(c.ty)} const $s${idObjc.const(c.ident)}")
  }

  def generateObjcConstants(w: IndentWriter, consts: Seq[Const], selfName: String) = {
    def boxedPrimitive(ty: TypeRef): String = {
      val (_, needRef) = toObjcType(ty)
      if (needRef) "@" else ""
    }
    def writeObjcConstValue(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(boxedPrimitive(ty) + l.toString)
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
      w.w(s"$label ($ret)${idObjc.method(method.ident)}")
      val skipFirst = SkipFirst()
      for (p <- method.params) {
        skipFirst { w.w(s" ${idObjc.local(p.ident)}") }
        w.w(s":(${marshal.paramType(p.ty)})${idObjc.local(p.ident)}")
      }
    }

    writeObjcFile(headerName(ident), origin, refs.header, w => {
      writeDoc(w, doc)
      for (c <- i.consts) {
        writeDoc(w, c.doc)
        w.w(s"extern ")
        writeObjcConstVariable(w, c, self)
        w.wl(s";")
      }
      w.wl
      if (i.ext.cpp) w.wl(s"@interface $self : NSObject") else w.wl(s"@protocol $self")
      for (m <- i.methods) {
        w.wl
        writeDoc(w, m.doc)
        writeObjcFuncDecl(m, w)
        w.wl(";")
      }
      w.wl
      w.wl("@end")
    })

    refs.body.add("#include <vector>")
    refs.body.add("#import " + q(spec.objcIncludePrefix + headerName(ident)))

    if (i.consts.nonEmpty) {
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

    if (r.ext.objc) {
      refs.body.add("#import " + q(spec.objcIncludePrefix + "../" + headerName(ident)))
      refs.header.add(s"@class $noBaseSelf;")
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
        w.w(s"extern ")
        writeObjcConstVariable(w, c, noBaseSelf);
        w.wl(s";")
      }
      w.wl
      w.wl(s"@interface $self : NSObject")

      // Deep copy construtor
      w.wl(s"- (id)${idObjc.method("init_with_" + ident.name)}:($self *)${idObjc.field(ident)};")
      if (!r.fields.isEmpty) {
        val head = r.fields.head
        val skipFirst = SkipFirst()
        w.w(s"- (id)${idObjc.method("init_with_" + head.ident.name)}:(${marshal.paramType(head.ty)})${idObjc.local(head.ident)}")
        for (f <- r.fields) skipFirst {
          w.w(s" ${idObjc.field(f.ident)}:(${marshal.paramType(f.ty)})${idObjc.field(f.ident)}")
        }
        w.wl(";")
      }

      for (f <- r.fields) {
        w.wl
        writeDoc(w, f.doc)
        w.wl(s"@property (nonatomic, readonly) ${marshal.fqFieldType(f.ty)} ${idObjc.field(f.ident)};")
      }
      if (r.derivingTypes.contains(DerivingType.Ord)) {
        w.wl
        w.wl(s"- (NSComparisonResult)compare:($self *)other;")
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
            case p: MParam => throw new AssertionError("Parameter should not happen at Obj-C top level")
          }
          base
      }
    }
    f(tm, needRef)
  }

}
