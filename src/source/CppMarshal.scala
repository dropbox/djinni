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
  private def toCppParamType(f: Field): String = toCppParamType(f, None, "")
  private def toCppParamType(f: Field, namespace: Option[String] = None, prefix: String = ""): String = {
    val cppType = toCppType(f.ty, namespace)
    val localName = prefix + idCpp.local(f.ident);
    val refType = "const " + cppType + " & " + localName
    val valueType = cppType + " " + localName

    def toType(expr: MExpr): String = expr.base match {
      case MPrimitive(_,_,_,_,_,_,_,_) => valueType
      case d: MDef => d.defType match {
        case DEnum => valueType
        case _  => refType
      }
      case MOptional => toType(expr.args.head)
      case _ => refType
    }
    toType(f.ty.resolved)
  }
}
