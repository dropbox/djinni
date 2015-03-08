package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class ObjcMarshal(spec: Spec) extends Marshal(spec) {

  // For JNI typename() is always fully qualified and describes the mangled Java type to be used in field/method signatures
  override def typename(tm: MExpr): String = {
    val (name, _) = toObjcType(tm)
    name
  }
  def typename(name: String, ty: TypeDef): String = idObjc.ty(name)

  override def fqTypename(tm: MExpr): String = typename(tm)
  def fqTypename(name: String, ty: TypeDef): String = typename(name, ty)

  override def paramType(tm: MExpr): String = toObjcParamType(tm)
  override def fqParamType(tm: MExpr): String = paramType(tm)

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(paramType)
  override def fqReturnType(ret: Option[TypeRef]): String = returnType(ret)

  override def fieldType(tm: MExpr): String = paramType(tm)
  override def fqFieldType(tm: MExpr): String = fqParamType(tm)

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
                (idObjc.ty(d.name), true)
            }
            case p: MParam => throw new AssertionError("Parameter should not happen at Obj-C top level")
          }
          base
      }
    }
    f(tm, needRef)
  }

  def toObjcParamType(tm: MExpr): String = {
    val (name, needRef) = toObjcType(tm)
    val param = name + (if(needRef) " *" else "")
    tm.base match {
      case d: MDef => d.body match {
        case i: Interface => if(i.ext.objc) s"id<$name>" else param
        case _ => param
      }
      case MOptional => tm.args.head.base match {
        case d: MDef => d.body match {
          case i: Interface => if(i.ext.objc) s"id<$name>" else param
          case _ => param
        }
        case _ => param
      }
      case _ => param
    }
  }

}
