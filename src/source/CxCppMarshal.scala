package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CxCppMarshal(spec: Spec) extends Marshal(spec) {
  private val cppMarshal = new CppMarshal(spec)
  private val cxMarshal = new CxMarshal(spec)

  override def typename(tm: MExpr): String = throw new AssertionError("not applicable")
  def typename(name: String, ty: TypeDef): String = throw new AssertionError("not applicable")

  override def fqTypename(tm: MExpr): String = throw new AssertionError("not applicable")
  def fqTypename(name: String, ty: TypeDef): String = throw new AssertionError("not applicable")

  override def paramType(tm: MExpr): String = throw new AssertionError("not applicable")
  override def fqParamType(tm: MExpr): String = throw new AssertionError("not applicable")

  override def returnType(ret: Option[TypeRef]): String = throw new AssertionError("not applicable")
  override def fqReturnType(ret: Option[TypeRef]): String = throw new AssertionError("not applicable")

  override def fieldType(tm: MExpr): String = throw new AssertionError("not applicable")
  override def fqFieldType(tm: MExpr): String = throw new AssertionError("not applicable")

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

  def headerName(ident: String): String = idCx.ty(ident) + "_convert." + spec.cxHeaderExt

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
}
