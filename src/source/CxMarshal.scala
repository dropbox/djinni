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

  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("cx to cx conversion")
  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("cx to cx conversion")

  def references(m: Meta, exclude: String): Seq[SymbolReference] = m match {
    case p: MPrimitive => p.idlName match {
      case "i8" | "i16" | "i32" | "i64" => List()
      case _ => List()
    }
    case MString | MDate | MBinary | MOptional | MList | MSet | MMap  => List()
    case d: MDef => d.defType match {
      case DEnum | DRecord =>
        if (d.name != exclude) {
          List(ImportRef(q(spec.cxIncludePrefix + spec.cxFileIdentStyle(d.name) + "." + spec.cxHeaderExt)))
        } else {
          List()
        }
      case DInterface =>
        if (d.name != exclude) {
          List(DeclRef(s"class ${typename(d.name, d.body)};", Some(spec.cxNamespace)))
        } else {
          List()
        }
    }
    case p: MParam => List()
  }

  private def toCxType(ty: TypeRef, namespace: Option[String] = None): String = toCxType(ty.resolved, namespace)
  private def toCxType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "Platform::String"
      case MDate => "Windows::Foundation::DateTime"
      case MBinary => "Platform::Array<int8_t>" //no uint8_t in Cx
      case MOptional => ""
      case MList => "Platform::Collections::Vector"
      case MSet => "Platform::Collections::Map" //no set in C++/Cx
      case MMap => "Platform::Collections::Map"
      case d: MDef =>
        d.defType match {
          case DEnum => withNs(namespace, idCx.enumType(d.name))
          case DRecord => withNs(namespace, idCx.ty(d.name))
          case DInterface => withNs(namespace, s"I${idCx.ty(d.name)}")
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
    val refType = "const " + cxType
    val valueType = cxType

    def toType(expr: MExpr): String = expr.base match {
      case p: MPrimitive => valueType
      case d: MDef => d.defType match {
        case DEnum => valueType + "^"
        case _  => refType + "^"
      }
      case MOptional => toType(expr.args.head)
      case _ => refType
    }
    toType(tm)
  }
}
