package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class ObjcMarshal(spec: Spec) extends Marshal(spec) {

  override def typename(tm: MExpr): String = {
    val (name, _) = toObjcType(tm)
    name
  }
  def typename(name: String, ty: TypeDef): String = idObjc.ty(name)

  override def fqTypename(tm: MExpr): String = typename(tm)
  def fqTypename(name: String, ty: TypeDef): String = typename(name, ty)

  def nullability(tm: MExpr): Option[String] = {
    val nonnull = Some("nonnull")
    val nullable = Some("nullable")
    val interfaceNullity = if (spec.cppNnType.nonEmpty) nonnull else nullable
    tm.base match {
      case MOptional => nullable
      case MPrimitive(_,_,_,_,_,_,_,_) => None
      case d: MDef => d.defType match {
        case DEnum => None
        case DInterface => interfaceNullity
        case DRecord => nonnull
      }
      case e: MExtern => e.defType match {
        case DEnum => None
        case DInterface => interfaceNullity
        case DRecord => if(e.objc.pointer) nonnull else None
      }
      case _ => nonnull
    }
  }

  override def paramType(tm: MExpr): String = {
    nullability(tm).fold("")(_ + " ") + toObjcParamType(tm)
  }
  override def fqParamType(tm: MExpr): String = paramType(tm)

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")((t: TypeRef) => nullability(t.resolved).fold("")(_ + " ") + toObjcParamType(t.resolved))
  override def fqReturnType(ret: Option[TypeRef]): String = returnType(ret)

  override def fieldType(tm: MExpr): String = toObjcParamType(tm)
  override def fqFieldType(tm: MExpr): String = toObjcParamType(tm)

  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("direct objc to cpp conversion not possible")
  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("direct cpp to objc conversion not possible")

  def references(m: Meta, exclude: String = ""): Seq[SymbolReference] = m match {
    case o: MOpaque =>
      List(ImportRef("<Foundation/Foundation.h>"))
    case d: MDef => d.defType match {
      case DEnum =>
        List(ImportRef(include(d.name)))
      case DInterface =>
        val ext = d.body.asInstanceOf[Interface].ext
        if (!ext.objc) {
          List(ImportRef("<Foundation/Foundation.h>"), DeclRef(s"@class ${typename(d.name, d.body)};", None))
        }
        else {
          List(ImportRef("<Foundation/Foundation.h>"), DeclRef(s"@protocol ${typename(d.name, d.body)};", None))
        }
      case DRecord =>
        val r = d.body.asInstanceOf[Record]
        val prefix = if (r.ext.objc) spec.objcExtendedRecordIncludePrefix else spec.objcIncludePrefix
        List(ImportRef(q(prefix + headerName(d.name))))
    }
    case e: MExtern => List(ImportRef(e.objc.header))
    case p: MParam => List()
  }

  def headerName(ident: String) = idObjc.ty(ident) + "." + spec.objcHeaderExt
  def include(ident: String) = q(spec.objcIncludePrefix + headerName(ident))

  def isPointer(td: TypeDecl) = td.body match {
    case i: Interface => true
    case r: Record => true
    case e: Enum => false
  }

  def boxedTypename(td: TypeDecl) = td.body match {
    case i: Interface => typename(td.ident, i)
    case r: Record => typename(td.ident, r)
    case e: Enum => "NSNumber"
  }

  // Return value: (Type_Name, Is_Class_Or_Not)
  def toObjcType(ty: TypeRef): (String, Boolean) = toObjcType(ty.resolved, false)
  def toObjcType(ty: TypeRef, needRef: Boolean): (String, Boolean) = toObjcType(ty.resolved, needRef)
  def toObjcType(tm: MExpr): (String, Boolean) = toObjcType(tm, false)
  def toObjcType(tm: MExpr, needRef: Boolean): (String, Boolean) = {
    def args(tm: MExpr) = if (tm.args.isEmpty) "" else tm.args.map(toBoxedParamType).mkString("<", ", ", ">")
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
            case MList => ("NSArray" + args(tm), true)
            case MSet => ("NSSet" + args(tm), true)
            case MMap => ("NSDictionary" + args(tm), true)
            case d: MDef => d.defType match {
              case DEnum => if (needRef) ("NSNumber", true) else (idObjc.ty(d.name), false)
              case DRecord => (idObjc.ty(d.name), true)
              case DInterface =>
                val ext = d.body.asInstanceOf[Interface].ext
                if (!ext.objc)
                  (idObjc.ty(d.name), true)
                else
                  (s"id<${idObjc.ty(d.name)}>", false)
            }
            case e: MExtern => e.body match {
              case i: Interface => if(i.ext.objc) (s"id<${e.objc.typename}>", false) else (e.objc.typename, true)
              case _ => if(needRef) (e.objc.boxed, true) else (e.objc.typename, e.objc.pointer)
            }
            case p: MParam => throw new AssertionError("Parameter should not happen at Obj-C top level")
          }
          base
      }
    }
    f(tm, needRef)
  }

  def toBoxedParamType(tm: MExpr): String = {
    val (name, needRef) = toObjcType(tm, true)
    name + (if(needRef) " *" else "")
  }

  def toObjcParamType(tm: MExpr): String = {
    val (name, needRef) = toObjcType(tm)
    name + (if(needRef) " *" else "")
  }

  /**
    * This method returns whether we can use global variable to represent a given constant.
    *
    * We can use global variables for constants which are safe to create during static init, which are numbers
    * strings, and optional strings. Anything else needs to be a class method.
    */
  def canBeConstVariable(c:Const): Boolean = c.ty.resolved.base match {
    case MPrimitive(_,_,_,_,_,_,_,_) => true
    case MString => true
    case MOptional =>
      assert(c.ty.resolved.args.size == 1)
      val arg = c.ty.resolved.args.head
      arg.base match {
        case MString => true
        case _ => false
      }
    case _ => false
  }
}
