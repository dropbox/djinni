package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class JNIMarshal(spec: Spec) extends Marshal(spec) {

  // For JNI typename() is always fully qualified and describes the mangled Java type to be used in field/method signatures
  override def typename(tm: MExpr): String = javaTypeSignature(tm)
  def typename(name: String, ty: TypeDef): String = throw new AssertionError("not applicable")

  override def fqTypename(tm: MExpr): String = typename(tm)
  def fqTypename(name: String, ty: TypeDef): String = typename(name, ty)

  override def paramType(tm: MExpr): String = toJniType(tm, false)
  override def fqParamType(tm: MExpr): String = paramType(tm)

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(toJniType)
  override def fqReturnType(ret: Option[TypeRef]): String = returnType(ret)

  override def fieldType(tm: MExpr): String = paramType(tm)
  override def fqFieldType(tm: MExpr): String = fqParamType(tm)

  override def toCpp(tm: MExpr, expr: String): String = {
    s"${helperClass(tm)}::toCpp(jniEnv, $expr)"
  }
  override def fromCpp(tm: MExpr, expr: String): String = {
    s"${helperClass(tm)}::fromCpp(jniEnv, $expr)"
  }

  def helperClass(name: String) = spec.jniClassIdentStyle(name)
  private def helperClass(tm: MExpr) = helperName(tm) + helperTemplates(tm)

  def references(m: Meta, exclude: String = ""): Seq[SymbolReference] = m match {
    case o: MOpaque => List(ImportRef(q(spec.jniBaseLibIncludePrefix + "Marshal.hpp")))
    case d: MDef => List(ImportRef(q(spec.jniIncludePrefix + spec.jniFileIdentStyle(d.name) + "." + spec.cppHeaderExt)))
    case _ => List()
  }

  def toJniType(ty: TypeRef): String = toJniType(ty.resolved, false)
  def toJniType(m: MExpr, needRef: Boolean): String = m.base match {
    case p: MPrimitive => if (needRef) "jobject" else p.jniName
    case MString => "jstring"
    case MOptional => toJniType(m.args.head, true)
    case MBinary => "jbyteArray"
    case tp: MParam => helperClass(tp.name) + "::JniType"
    case _ => "jobject"
  }

  // The mangled Java typename without the "L...;" decoration useful only for class reflection on our own type
  def undecoratedTypename(name: String, ty: TypeDef): String = {
    val javaClassName = idJava.ty(name)
    spec.javaPackage.fold(javaClassName)(p => p.replaceAllLiterally(".", "/") + "/" + javaClassName)
  }

  private def javaTypeSignature(tm: MExpr): String = tm.base match {
    case o: MOpaque => o match {
      case p: MPrimitive => p.jSig
      case MString => "Ljava/lang/String;"
      case MBinary => "[B"
      case MDate => "Ljava/util/Date;"
      case MOptional =>  tm.args.head.base match {
        case p: MPrimitive => s"Ljava/lang/${p.jBoxed};"
        case MOptional => throw new AssertionError("nested optional?")
        case m => javaTypeSignature(tm.args.head)
      }
      case MList => "Ljava/util/ArrayList;"
      case MSet => "Ljava/util/HashSet;"
      case MMap => "Ljava/util/HashMap;"
    }
    case MParam(_) => "Ljava/lang/Object;"
    case d: MDef => s"L${undecoratedTypename(d.name, d.body)};"
  }

  def javaMethodSignature(params: Iterable[Field], ret: Option[TypeRef]) = {
    params.map(f => typename(f.ty)).mkString("(", "", ")") + ret.fold("V")(typename)
  }

  private def helperName(tm: MExpr): String = tm.base match {
    case d: MDef => withNs(Some(spec.jniNamespace), helperClass(d.name))
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
      case MDate => "Date"
      case d: MDef => throw new AssertionError("unreachable")
      case p: MParam => throw new AssertionError("not applicable")
    })
  }

  private def helperTemplates(tm: MExpr): String = tm.base match {
      case MOptional =>
        assert(tm.args.size == 1)
        val argHelperClass = helperClass(tm.args.head)
        s"<${spec.cppOptionalTemplate}, $argHelperClass>"
      case MList | MSet =>
        assert(tm.args.size == 1)
        val argHelperClass = helperClass(tm.args.head)
        s"<$argHelperClass>"
      case MMap =>
        assert(tm.args.size == 2)
        val keyHelperClass = helperClass(tm.args.head)
        val valueHelperClass = helperClass(tm.args.tail.head)
        s"<$keyHelperClass, $valueHelperClass>"
      case _ =>
        if(tm.args.isEmpty) "" else tm.args.map(helperClass).mkString("<", ",", ">")
  }

  def isJavaHeapObject(ty: TypeRef): Boolean = isJavaHeapObject(ty.resolved.base)
  def isJavaHeapObject(m: Meta): Boolean = m match {
    case _: MPrimitive => false
    case _ => true
  }
}
