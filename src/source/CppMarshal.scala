package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CppMarshal(spec: Spec) extends Marshal(spec) {

  override def typename(tm: MExpr): String = toCppType(tm, None)
  def typename(name: String, ty: TypeDef): String = ty match {
  	case e: Enum => idCpp.enumType(name)
  	case i: Interface => idCpp.ty(name)
  	case r: Record => idCpp.ty(name)
  }

  override def fqTypename(tm: MExpr): String = toCppType(tm, spec.cppNamespace)
  def fqTypename(name: String, ty: TypeDef): String = ty match {
  	case e: Enum => withNs(spec.cppNamespace, idCpp.enumType(name))
  	case i: Interface => withNs(spec.cppNamespace, idCpp.ty(name))
  	case r: Record => withNs(spec.cppNamespace, idCpp.ty(name))
  }

  override def paramType(tm: MExpr): String = toCppParamType(tm)
  override def fqParamType(tm: MExpr): String = toCppParamType(tm, spec.cppNamespace)

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toCppType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = ret.fold("void")(toCppType(_, spec.cppNamespace))

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")
  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")

  private def toCppType(ty: TypeRef, namespace: Option[String] = None): String = toCppType(ty.resolved, namespace)
  private def toCppType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "std::string"
      case MBinary => "std::vector<uint8_t>"
      case MOptional => spec.cppOptionalTemplate
      case MList => "std::vector"
      case MSet => "std::unordered_set"
      case MMap => "std::unordered_map"
      case d: MDef =>
        d.defType match {
          case DEnum => withNs(namespace, idCpp.enumType(d.name))
          case DRecord => withNs(namespace, idCpp.ty(d.name))
          case DInterface => s"std::shared_ptr<${withNs(namespace, idCpp.ty(d.name))}>"
        }
      case p: MParam => idCpp.typeParam(p.name)
    }
    def expr(tm: MExpr): String = {
      val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
      base(tm.base) + args
    }
    expr(tm)
  }

  // this can be used in c++ generation to know whether a const& should be applied to the parameter or not
  private def toCppParamType(tm: MExpr, namespace: Option[String] = None): String = {
    val cppType = toCppType(tm, namespace)
    val refType = "const " + cppType + " &"
    val valueType = cppType

    def toType(expr: MExpr): String = expr.base match {
      case p: MPrimitive => valueType
      case d: MDef => d.defType match {
        case DEnum => valueType
        case _  => refType
      }
      case MOptional => toType(expr.args.head)
      case _ => refType
    }
    toType(tm)
  }
}
