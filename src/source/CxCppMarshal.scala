package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CxCppMarshal(spec: Spec) extends Marshal(spec) {
  private val cppMarshal = new CppMarshal(spec)
  private val cxMarshal = new CxMarshal(spec)

  override def typename(tm: MExpr): String = toCxCppType(tm, None)
  def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idCx.enumType(name)
    case i: Interface => idCx.ty(name)
    case r: Record => idCx.ty(name)
  }

  override def fqTypename(tm: MExpr): String = toCxCppType(tm, Some(spec.cxNamespace))
  def fqTypename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => withNs(Some(spec.cxNamespace), idCx.enumType(name))
    case i: Interface => withNs(Some(spec.cxNamespace), idCx.ty(name))
    case r: Record => withNs(Some(spec.cxNamespace), idCx.ty(name))
  }

  override def paramType(tm: MExpr): String = toCxCppParamType(tm)
  override def fqParamType(tm: MExpr): String = toCxCppParamType(tm, Some(spec.cxNamespace))

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toCxCppType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = ret.fold("void")(toCxCppType(_, Some(spec.cxNamespace)))

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

  override def toCpp(tm: MExpr, expr: String): String = {
    s"${helperClass(tm)}::toCpp($expr)"
  }
  override def fromCpp(tm: MExpr, expr: String): String = {
    s"${helperClass(tm)}::fromCpp($expr)"
  }

  def references(m: Meta): Seq[SymbolReference] = m match {
    case o: MOpaque =>
      List(ImportRef(q(spec.cxBaseLibIncludePrefix + "Marshal.h")))
    case d: MDef => d.defType match {
      case DEnum =>
        List(ImportRef(q(spec.cxBaseLibIncludePrefix + "Marshal.h")))
      case DInterface =>
        List(ImportRef(q(spec.cxcppIncludePrefix + headerName(d.name))))
      case DRecord =>
        val r = d.body.asInstanceOf[Record]
        val cxName = d.name + (if (r.ext.cx) "_base" else "")
        List(ImportRef(q(spec.cxcppIncludePrefix + headerName(cxName))))
    }
    case p: MParam => List()
  }

  def helperClass(name: String) = idCpp.ty(name)
  private def helperClass(tm: MExpr): String = helperName(tm) + helperTemplates(tm)

  def headerName(ident: String): String = idCx.ty(ident) + "_convert"
  def bodyName(ident: String): String = idCx.ty(ident) + "_convert"

  private def helperName(tm: MExpr): String = tm.base match {
    case d: MDef => d.defType match {
      case DEnum => withNs(Some("djinni"), s"Enum<${cppMarshal.fqTypename(tm)}, ${cxMarshal.fqTypename(tm)}>")
      case _ => withNs(Some(spec.cxcppNamespace), helperClass(d.name))
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

  private def toCxCppType(ty: TypeRef, namespace: Option[String] = None): String = toCxCppType(ty.resolved, namespace)
  private def toCxCppType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "std::string"
      case MDate => "std::chrono::system_clock::time_point"
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
  private def toCxCppParamType(tm: MExpr, namespace: Option[String] = None): String = {
    val cppType = toCxCppType(tm, namespace)
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
