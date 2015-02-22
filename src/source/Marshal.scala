package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import scala.language.implicitConversions

// Generate code for marshalling a specific type from/to C++ including header and type names.
// This only generates information relevant to a single language interface.
// This means the C++ Marshal generates only C++ types and includes, but not JNI or ObjC++.
// As a consequence a typical code generator needs two Marshals: one for C++ and one for the destination, e.g. JNI.
abstract class Marshal(spec: Spec) {
  // Typename string to be used to declare a type or template parameter, without namespace or package, except for extern types which are always fully qualified.
  def typename(tm: MExpr): String
  def typename(ty: TypeRef): String = typename(ty.resolved)
  def typename(name: String, ty: TypeDef): String
  // Same as typename() but always fully namespace or package qualified
  def fqTypename(tm: MExpr): String
  def fqTypename(ty: TypeRef): String = fqTypename(ty.resolved)
  def fqTypename(name: String, ty: TypeDef): String

  implicit def identToString(ident: Ident): String = ident.name
  protected val idCpp = spec.cppIdentStyle
  protected val idJava = spec.javaIdentStyle
  protected val idObjc = spec.objcIdentStyle

  protected def toCppType(ty: TypeRef, namespace: Option[String] = None): String = toCppType(ty.resolved, namespace)
  protected def toCppType(tm: MExpr, namespace: Option[String]): String = {
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
  protected def toCppParamType(f: Field): String = toCppParamType(f, None, "")
  protected def toCppParamType(f: Field, namespace: Option[String] = None, prefix: String = ""): String = {
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

  protected def withNs(namespace: Option[String], t: String) = namespace.fold(t)("::"+_+"::"+t)
}
