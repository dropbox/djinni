package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CppMarshal(spec: Spec) extends Marshal(spec) {

  def typename(tm: MExpr): String = toCppType(tm, None)
  def typename(name: String, ty: TypeDef): String = ty match {
  	case e: Enum => idCpp.enumType(name)
  	case i: Interface => idCpp.ty(name)
  	case r: Record => idCpp.ty(name)
  }

  def fqTypename(tm: MExpr): String = toCppType(tm, spec.cppNamespace)
  def fqTypename(name: String, ty: TypeDef): String = ty match {
  	case e: Enum => withNs(spec.cppNamespace, idCpp.enumType(name))
  	case i: Interface => withNs(spec.cppNamespace, idCpp.ty(name))
  	case r: Record => withNs(spec.cppNamespace, idCpp.ty(name))
  }

}
