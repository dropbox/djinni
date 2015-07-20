package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CxMarshal(spec: Spec) extends Marshal(spec) {

  private val cppMarshal = new CppMarshal(spec)

  override def typename(tm: MExpr): String = toCxType(tm, None)
  def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idCx.enumType(name)
    case i: Interface => if(i.ext.cx) s"I${idCx.ty(name)}" else idCx.ty(name)
    case r: Record => idCx.ty(name)
  }

  override def fqTypename(tm: MExpr): String = toCxType(tm, Some(spec.cxNamespace))
  def fqTypename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => withNs(Some(spec.cxNamespace), idCx.enumType(name))
    case i: Interface => if(i.ext.cx) withNs(Some(spec.cxNamespace), s"I${idCx.ty(name)}") else withNs(Some(spec.cxNamespace), idCx.ty(name))
    case r: Record => withNs(Some(spec.cxNamespace), idCx.ty(name))
  }

  override def paramType(tm: MExpr): String = toCxParamType(tm)
  override def fqParamType(tm: MExpr): String = toCxParamType(tm, Some(spec.cxNamespace))

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toCxType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = ret.fold("void")(toCxType(_, Some(spec.cxNamespace)))

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

  override def toCpp(tm: MExpr, expr: String): String = {
    s"${helperClass(tm)}::toCpp($expr)"
  }
  override def fromCpp(tm: MExpr, expr: String): String = {
    s"${helperClass(tm)}::fromCpp($expr)"
  }

  def helperClass(name: String) = idCpp.ty(name)
  private def helperClass(tm: MExpr): String = helperName(tm) + helperTemplates(tm)

  private def helperName(tm: MExpr): String = tm.base match {
    case d: MDef => d.defType match {
      case DEnum => withNs(Some("djinni"), s"Enum<${cppMarshal.fqTypename(tm)}, ${fqTypename(tm)}>")
      case _ => withNs(Some(spec.objcppNamespace), helperClass(d.name))
    }
    case o => withNs(Some("djinni"), o match {
      case p: MPrimitive => p.idlName match {
        case "i8" => "I8"
        case "i16" => "I16"
        case "i32" => "I32"
        case "i64" => "I64"
        case "f32" => "F32"
        case "f64" => "F64"
        case "bool" => "Bool"
      }
      case MOptional => "Optional"
      case MBinary => "Binary"
      case MDate => "Date"
      case MString => "String"
      case MList => "List"
      case MSet => "Set"
      case MMap => "Map"
      case d: MDef => throw new AssertionError("unreachable")
      case p: MParam => throw new AssertionError("not applicable")
    })
  }

  private def helperTemplates(tm: MExpr): String = {
    def f() = if(tm.args.isEmpty) "" else tm.args.map(helperClass).mkString("<", ", ", ">")
    tm.base match {
      case MOptional =>
        assert(tm.args.size == 1)
        val argHelperClass = helperClass(tm.args.head)
        s"<${spec.cppOptionalTemplate}, $argHelperClass>"
      case MList | MSet =>
        assert(tm.args.size == 1)
        f
      case MMap =>
        assert(tm.args.size == 2)
        f
      case _ => f
    }
  }

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
          List(ImportRef(q(spec.cxIncludePrefix + spec.cxFileIdentStyle(d.name) + "." + spec.cxHeaderExt)))
        } else {
          List()
        }
    }
    case p: MParam => List()
  }

  def convertReferences(m: Meta, exclude: String): Seq[SymbolReference] = m match {
    case p: MPrimitive => p.idlName match {
      case "i8" | "i16" | "i32" | "i64" => List()
      case _ => List()
    }
    case MString | MDate | MBinary | MOptional | MList | MSet | MMap  => List()
    case d: MDef => d.defType match {
      case DEnum | DRecord =>
        if (d.name != exclude) {
          List(ImportRef(q(spec.cxcppIncludePrefix + spec.cxFileIdentStyle(d.name) + "_convert." + spec.cxcppHeaderExt)))
        } else {
          List()
        }
      case DInterface =>
        if (d.name != exclude) {
          List(ImportRef(q(spec.cxcppIncludePrefix + spec.cxFileIdentStyle(d.name) + "_convert." + spec.cxcppHeaderExt)))
        } else {
          List()
        }
    }
    case p: MParam => List()
  }

  private def toCxType(ty: TypeRef, namespace: Option[String] = None): String = toCxType(ty.resolved, namespace)
  private def toCxType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cxName
      case MString => "Platform::String^"
      case MDate => "Windows::Foundation::DateTime^"
      case MBinary => "Platform::Array<uint16_t>^" //no uint8_t in Cx
      case MOptional => ""
      case MList => "Windows::Foundation::Collections::IVector"
      case MSet => "Windows::Foundation::Collections::IMap" //no set in C++/Cx FOr now this shit is broken until I can figure out how to make something map onto itself.
      case MMap => "Windows::Foundation::Collections::IMap"
      case d: MDef =>
        d.defType match {
          case DEnum => withNs(namespace, idCx.enumType(d.name))
          case DRecord => s"${withNs(namespace, idCx.ty(d.name))}^"
          case DInterface => d.body match {
            case e: Enum => s"${withNs(namespace, idCx.ty(d.name))}^"
            case i: Interface => if(i.ext.cx) s"I${withNs(namespace, idCx.ty(d.name))}^" else s"${withNs(namespace, idCx.ty(d.name))}^"
            case r: Record => s"${withNs(namespace, idCx.ty(d.name))}^"
          }
        }
      case p: MParam => idCx.typeParam(p.name)
    }
    def expr(tm: MExpr): String = {
      val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">^")
      base(tm.base) + args
    }
    expr(tm)
  }

  // this can be used in c++ generation to know whether a const& should be applied to the parameter or not
  private def toCxParamType(tm: MExpr, namespace: Option[String] = None): String = {
    val cxType = toCxType(tm, namespace)
    val refType = cxType
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
