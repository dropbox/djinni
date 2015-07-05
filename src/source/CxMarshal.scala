package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CxMarshal(spec: Spec) extends Marshal(spec) {

  override def typename(tm: MExpr): String = toCxType(tm, None)
  def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idCx.enumType(name)
    case i: Interface => idCx.ty(name)
    case r: Record => idCx.ty(name)
  }

  override def fqTypename(tm: MExpr): String = toCxType(tm, Some(spec.cxNamespace))
  def fqTypename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => withNs(Some(spec.cxNamespace), idCx.enumType(name))
    case i: Interface => withNs(Some(spec.cxNamespace), idCx.ty(name))
    case r: Record => withNs(Some(spec.cxNamespace), idCx.ty(name))
  }

  override def paramType(tm: MExpr): String = toCxParamType(tm)
  override def fqParamType(tm: MExpr): String = toCxParamType(tm, Some(spec.cxNamespace))

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toCxType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = ret.fold("void")(toCxType(_, Some(spec.cxNamespace)))

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

  def references(m: Meta, exclude: String): Seq[SymbolReference] = m match {
    case p: MPrimitive => p.idlName match {
      case "i8" | "i16" | "i32" | "i64" => List(ImportRef("<cstdint>"))
      case _ => List()
    }
    case MString => List(ImportRef("<string>"))
    case MDate => List(ImportRef("<chrono>"))
    case MBinary => List(ImportRef("<vector>"), ImportRef("<cstdint>"))
    case MOptional => List()
    case MList => List(ImportRef("<vector>"))
    case MSet => List(ImportRef("<unordered_set>"))
    case MMap => List(ImportRef("<unordered_map>"))
    case d: MDef => d.defType match {
      case DEnum | DRecord =>
        if (d.name != exclude) {
          List(ImportRef(q(spec.cxIncludePrefix + spec.cxFileIdentStyle(d.name) + "." + spec.cxHeaderExt)))
        } else {
          List()
        }
      case DInterface =>
        if (d.name != exclude) {
          List(ImportRef("<memory>"), DeclRef(s"class ${typename(d.name, d.body)};", Some(spec.cxNamespace)))
        } else {
          List(ImportRef("<memory>"))
        }
    }
    case p: MParam => List()
 }

  private def toCxType(ty: TypeRef, namespace: Option[String] = None): String = toCxType(ty.resolved, namespace)
  private def toCxType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "std::string"
      case MDate => "std::chrono::system_clock::time_point"
      case MBinary => "std::vector<uint8_t>"
      case MOptional => ""
      case MList => "std::vector"
      case MSet => "std::unordered_set"
      case MMap => "std::unordered_map"
      case d: MDef =>
        d.defType match {
          case DEnum => withNs(namespace, idCx.enumType(d.name))
          case DRecord => withNs(namespace, idCx.ty(d.name))
          case DInterface => s"std::shared_ptr<${withNs(namespace, idCx.ty(d.name))}>"
        }
      case p: MParam => idCx.typeParam(p.name)
    }
    def expr(tm: MExpr): String = {
      val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
      base(tm.base) + args
    }
    expr(tm)
  }

  // this can be used in c++ generation to know whether a const& should be applied to the parameter or not
  private def toCxParamType(tm: MExpr, namespace: Option[String] = None): String = {
    val cxType = toCxType(tm, namespace)
    val refType = "const " + cxType + " &"
    val valueType = cxType

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
