package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class ObjcppMarshal(spec: Spec) extends Marshal(spec) {
  private val cppMarshal = new CppMarshal(spec)
  private val objcMarshal = new ObjcMarshal(spec)

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
    val helper = helperName(tm) + helperTemplates(tm)
    s"$helper::toCpp($expr)"
  }
  override def fromCpp(tm: MExpr, expr: String): String = {
    val helper = helperName(tm) + helperTemplates(tm)
    s"$helper::fromCpp($expr)"
  }

  def references(m: Meta): Seq[SymbolReference] = m match {
    case o: MOpaque =>
      List(ImportRef(q(spec.objcBaseLibIncludePrefix + "DJIMarshal+Private.h")))
    case d: MDef => d.defType match {
      case DEnum =>
        List(ImportRef(q(spec.objcBaseLibIncludePrefix + "DJIMarshal+Private.h")))
      case DInterface =>
        List(ImportRef(q(spec.objcppIncludePrefix + privateHeaderName(d.name))))
      case DRecord =>
        val r = d.body.asInstanceOf[Record]
        val objcName = d.name + (if (r.ext.objc) "_base" else "")
        List(ImportRef(q(spec.objcppIncludePrefix + privateHeaderName(objcName))))
    }
    case p: MParam => List()
  }

  def helperClass(name: String) = idCpp.ty(name)

  def privateHeaderName(ident: String): String = idObjc.ty(ident) + "+Private." + spec.objcHeaderExt

  private def helperName(tm: MExpr): String = tm.base match {
    case d: MDef => d.defType match {
      case DEnum => withNs(Some("djinni"), s"Enum<${cppMarshal.fqTypename(tm)}, ${objcMarshal.fqTypename(tm)}>")
      case _ => withNs(Some(spec.objcppNamespace), helperClass(d.name))
    }
    case o => withNs(Some("djinni"), o match {
      case p: MPrimitive => p.idlName match {
        case "i8" => "I8"
        case "i16" => "I16"
        case "i32" => "I32"
        case "i64" => "I64"
        case "f64" => "F64"
        case "bool" => "Bool"
      }
      case MOptional => "Optional"
      case MBinary => "Binary"
      case MString => "String"
      case MList => "List"
      case MSet => "Set"
      case MMap => "Map"
      case d: MDef => throw new AssertionError("unreachable")
      case p: MParam => throw new AssertionError("not applicable")
    })
  }

  private def helperTemplates(tm: MExpr): String = tm.base match {
      case MOptional =>
        assert(tm.args.size == 1)
        val argHelperClass = helperName(tm.args.head) + helperTemplates(tm.args.head)
        s"<${spec.cppOptionalTemplate}, $argHelperClass>"
      case MList | MSet =>
        assert(tm.args.size == 1)
        val argHelperClass = helperName(tm.args.head) + helperTemplates(tm.args.head)
        s"<$argHelperClass>"
      case MMap =>
        assert(tm.args.size == 2)
        val keyHelperClass = helperName(tm.args.head) + helperTemplates(tm.args.head)
        val valueHelperClass = helperName(tm.args.tail.head) + helperTemplates(tm.args.tail.head)
        s"<$keyHelperClass, $valueHelperClass>"
      case _ =>
        ""
  }
}
