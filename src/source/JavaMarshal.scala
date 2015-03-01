package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class JavaMarshal(spec: Spec) extends Marshal(spec) {

  override def typename(tm: MExpr): String = toJavaType(tm, None)
  def typename(name: String, ty: TypeDef): String = idJava.ty(name)

  override def fqTypename(tm: MExpr): String = toJavaType(tm, spec.javaPackage)
  def fqTypename(name: String, ty: TypeDef): String = withPackage(spec.javaPackage, idJava.ty(name))

  def toJavaType(tm: MExpr, packageName: Option[String]): String = {
    def f(tm: MExpr, needRef: Boolean): String = {
      tm.base match {
        case MOptional =>
          // HACK: We use "null" for the empty optional in Java.
          assert(tm.args.size == 1)
          val arg = tm.args.head
          arg.base match {
            case p: MPrimitive => p.jBoxed
            case MOptional => throw new AssertionError("nested optional?")
            case m => f(arg, true)
          }
        case o =>
          val args = if (tm.args.isEmpty) "" else tm.args.map(f(_, true)).mkString("<", ", ", ">")
          val base = o match {
            case p: MPrimitive => if (needRef) p.jBoxed else p.jName
            case MString => "String"
            case MBinary => "byte[]"
            case MOptional => throw new AssertionError("optional should have been special cased")
            case MList => "ArrayList"
            case MSet => "HashSet"
            case MMap => "HashMap"
            case d: MDef => withPackage(packageName, idJava.ty(d.name))
            case p: MParam => idJava.typeParam(p.name)
          }
          base + args
      }
    }
    f(tm, false)
  }

  def withPackage(packageName: Option[String], t: String) = packageName.fold(t)(_+"."+t)

 }
