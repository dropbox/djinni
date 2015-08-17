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

  override def fqTypename(tm: MExpr): String = toCppType(tm, Some(spec.cppNamespace))
  def fqTypename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => withNs(Some(spec.cppNamespace), idCpp.enumType(name))
    case i: Interface => withNs(Some(spec.cppNamespace), idCpp.ty(name))
    case r: Record => withNs(Some(spec.cppNamespace), idCpp.ty(name))
  }

  override def paramType(tm: MExpr): String = toCppParamType(tm)
  override def fqParamType(tm: MExpr): String = toCppParamType(tm, Some(spec.cppNamespace))

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toCppType(_, None))
  override def fqReturnType(ret: Option[TypeRef]): String = ret.fold("void")(toCppType(_, Some(spec.cppNamespace)))

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")
  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")

  def references(m: Meta, exclude: String): Seq[SymbolReference] = m match {
    case p: MPrimitive => p.idlName match {
      case "i8" | "i16" | "i32" | "i64" => List(ImportRef("<cstdint>"))
      case _ => List()
    }
    case MString => List(ImportRef("<string>"))
    case MDate => List(ImportRef("<chrono>"))
    case MBinary => List(ImportRef("<vector>"), ImportRef("<cstdint>"))
    case MOptional => List(ImportRef(spec.cppOptionalHeader))
    case MEither => (spec.cppEitherHeader, spec.objcEitherClass) match {
      case (Some(h), Some(c)) => List(DeclRef(s"#define DB_EITHER_OBJC_CLASSNAME $c", null), ImportRef(h))
      case (None, _) => throw new AssertionError("no C++ either header specified")
      case _ => throw new AssertionError("no Objective-C either class specified")
    }
    case MList => List(ImportRef("<vector>"))
    case MSet => List(ImportRef("<unordered_set>"))
    case MMap => List(ImportRef("<unordered_map>"))
    case d: MDef => d.defType match {
      case DEnum | DRecord =>
        if (d.name != exclude) {
          List(ImportRef(include(d.name)))
        } else {
          List()
        }
      case DInterface =>
        if (d.name != exclude) {
          List(ImportRef("<memory>"), DeclRef(s"class ${typename(d.name, d.body)};", Some(spec.cppNamespace)))
        } else {
          List(ImportRef("<memory>"))
        }
    }
    case e: MExtern => e.defType match {
      // Do not forward declare extern types, they might be in arbitrary namespaces.
      // This isn't a problem as extern types cannot cause dependency cycles with types being generated here
      case DInterface => List(ImportRef("<memory>"), ImportRef(e.cpp.header))
      case _ => List(ImportRef(e.cpp.header))
    }
    case p: MParam => List()
  }

  def include(ident: String): String = q(spec.cppIncludePrefix + spec.cppFileIdentStyle(ident) + "." + spec.cppHeaderExt)

  private def toCppType(ty: TypeRef, namespace: Option[String] = None): String = toCppType(ty.resolved, namespace)
  private def toCppType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "std::string"
      case MDate => "std::chrono::system_clock::time_point"
      case MBinary => "std::vector<uint8_t>"
      case MOptional => spec.cppOptionalTemplate
      case MEither => spec.cppEitherTemplate match {
        case None => throw new AssertionError("either template unspecified")
        case Some(t) => t
      }
      case MList => "std::vector"
      case MSet => "std::unordered_set"
      case MMap => "std::unordered_map"
      case d: MDef =>
        d.defType match {
          case DEnum => withNs(namespace, idCpp.enumType(d.name))
          case DRecord => withNs(namespace, idCpp.ty(d.name))
          case DInterface => s"std::shared_ptr<${withNs(namespace, idCpp.ty(d.name))}>"
        }
      case e: MExtern => e.defType match {
        case DInterface => s"std::shared_ptr<${e.cpp.typename}>"
        case _ => e.cpp.typename
      }
      case p: MParam => idCpp.typeParam(p.name)
    }
    def expr(tm: MExpr): String = {
      val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
      base(tm.base) + args
    }
    expr(tm)
  }

  def byValue(tm: MExpr): Boolean = tm.base match {
    case p: MPrimitive => true
    case d: MDef => d.defType match {
      case DEnum => true
      case _  => false
    }
    case e: MExtern => e.defType match {
      case DInterface => false
      case DEnum => true
      case DRecord => e.cpp.byValue
    }
    case MOptional => byValue(tm.args.head)
    case _ => false
  }

  def byValue(td: TypeDecl): Boolean = td.body match {
    case i: Interface => false
    case r: Record => false
    case e: Enum => true
  }

  // this can be used in c++ generation to know whether a const& should be applied to the parameter or not
  private def toCppParamType(tm: MExpr, namespace: Option[String] = None): String = {
    val cppType = toCppType(tm, namespace)
    val refType = "const " + cppType + " &"
    val valueType = cppType
    if(byValue(tm)) valueType else refType
  }
}
