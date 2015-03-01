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
  // Same as typename() but always fully namespace or package qualified
  def fqTypename(tm: MExpr): String
  def fqTypename(ty: TypeRef): String = fqTypename(ty.resolved)

  implicit def identToString(ident: Ident): String = ident.name
  protected val idCpp = spec.cppIdentStyle
  protected val idJava = spec.javaIdentStyle
  protected val idObjc = spec.objcIdentStyle

  protected def withNs(namespace: Option[String], t: String) = namespace.fold(t)("::"+_+"::"+t)
}
