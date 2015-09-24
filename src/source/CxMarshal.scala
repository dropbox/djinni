package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CxMarshal(spec: Spec) extends Marshal(spec) {

  private val cppMarshal = new CppMarshal(spec)

//  override def typename(tm: MExpr): String = toCxType(tm, None)._1
  override def typename(tm: MExpr): String = {
    val (name, needRef) = toCxType(tm, None)
    val result = if(needRef) (s"${name}^") else (s"${name}")
    result
  }

  def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idCx.enumType(name)
    case i: Interface => if(i.ext.cx) s"I${idCx.ty(name)}" else idCx.ty(name)
    case r: Record => idCx.ty(name)
  }

  override def fqTypename(tm: MExpr): String = toCxType(tm, Some(spec.cxNamespace))._1
  def fqTypename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => withNs(Some(spec.cxNamespace), idCx.enumType(name))
    case i: Interface => if(i.ext.cx) withNs(Some(spec.cxNamespace), s"I${idCx.ty(name)}") else withNs(Some(spec.cxNamespace), idCx.ty(name))
    case r: Record => withNs(Some(spec.cxNamespace), idCx.ty(name))
  }

  override def paramType(tm: MExpr): String = toCxParamType(tm)
  override def fqParamType(tm: MExpr): String = toCxParamType(tm, Some(spec.cxNamespace))

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")((t: TypeRef) => toCxParamType(t.resolved))
  override def fqReturnType(ret: Option[TypeRef]): String = ret.fold("void")((t: TypeRef) => toCxParamType(t.resolved, Some(spec.cxNamespace)))

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)


  override def toCpp(tm: MExpr, expr: String): String = {
    s"${ownClass(tm)}::toCpp($expr)"
  }
  override def fromCpp(tm: MExpr, expr: String): String = {
    s"${ownClass(tm)}::fromCpp($expr)"
  }


  def ownClass(name: String) = s"${idCx.ty(name)}"
  private def ownClass(tm: MExpr): String = ownName(tm) + ownTemplates(tm)

  private def ownName(tm: MExpr): String = tm.base match {
    case d: MDef => d.defType match {
      case DEnum => withNs(Some("djinni"), s"Enum<${cppMarshal.fqTypename(tm)}, ${fqTypename(tm)}>")
      case _ => withNs(Some(spec.cxcppNamespace), s"${ownClass(d.name)}")
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

  private def ownTemplates(tm: MExpr): String = {
    def f() = if(tm.args.isEmpty) "" else tm.args.map(ownClass).mkString("<", ", ", ">")
    tm.base match {
      case MOptional =>
        assert(tm.args.size == 1)
        val argHelperClass = ownClass(tm.args.head)
        s"<${spec.cppOptionalTemplate}, $argHelperClass>" //TODO THIS IS VERY WRONG!
      case MList | MSet =>
        assert(tm.args.size == 1)
        f
      case MMap =>
        assert(tm.args.size == 2)
        f
      case _ => f
    }
  }

  def helperClass(name: String) = idCpp.ty(name)
  private def helperClass(tm: MExpr): String = helperName(tm) + helperTemplates(tm)

  private def helperName(tm: MExpr): String = tm.base match {
    case d: MDef => d.defType match {
      case DEnum => withNs(Some("djinni"), s"Enum<${cppMarshal.fqTypename(tm)}, ${fqTypename(tm)}>")
      case DInterface =>
        val ext = d.body.asInstanceOf[Interface].ext
        if (ext.cpp && !ext.cx)
          withNs(Some(spec.cxcppNamespace), helperClass(d.name))
        else
          s"I${withNs(Some(spec.cxcppNamespace), helperClass(d.name))}"
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
      case DEnum => List() //no headers to import for enums
      case DRecord => //DEnum | DRecord =>
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

  def headerName(ident: String) = idCx.ty(ident) + "." + spec.cxHeaderExt
  def include(ident: String) = q(spec.cxIncludePrefix + headerName(ident))


  def isReference(td: TypeDecl) = td.body match {
    case i: Interface => true
    case r: Record => true
    case e: Enum => true
  }

  def boxedTypename(td: TypeDecl) = td.body match {
    case i: Interface => typename(td.ident, i)
    case r: Record => typename(td.ident, r)
    case e: Enum => "Platform::Object"
  }


//  // Return value: (Type_Name, Is_Class_Or_Not)
//  def toCxType(ty: TypeRef, namespace: Option[String] = None): (String, Boolean) = toCxType(ty.resolved, namespace, false)
//  def toCxType(ty: TypeRef, namespace: Option[String], needRef: Boolean): (String, Boolean) = toCxType(ty.resolved, namespace, needRef)
//  def toCxType(tm: MExpr, namespace: Option[String]): (String, Boolean) = toCxType(tm, namespace, false)
//  def toCxType(tm: MExpr, namespace: Option[String], needRef: Boolean): (String, Boolean) = {
//    def f(tm: MExpr, needRef: Boolean): (String, Boolean) = {
//      def base(tm: MExpr, needRef: Boolean): (String, Boolean) = {
//        tm.base match {
//          case MOptional =>
//            // We use "nil" for the empty optional.
//            assert(tm.args.size == 1)
//            val arg = tm.args.head
//            arg.base match {
//              case MOptional => throw new AssertionError("nested optional?")
//              case m => f(arg, true)
//            }
//          case o =>
//            val base = o match {
//              case p: MPrimitive => if (needRef) (p.cxBoxed, true) else (p.cxName, false)
//              case MString => ("Platform::String", true)
//              case MDate => ("Windows::Foundation::DateTime", true)
//              case MBinary => ("Platform::Array<uint16_t>", true)
//              case MOptional => throw new AssertionError("optional should have been special cased")
//              case MList => ("Windows::Foundation::Collections::IVector", true)
//              case MSet => ("Windows::Foundation::Collections::IMap", true) //no set in C++/Cx FOr now this shit is broken until I can figure out how to make something map onto itself.
//              case MMap => ("Windows::Foundation::Collections::IMap", true)
//              case d: MDef => d.defType match {
//                case DEnum => (idCx.ty(d.name), true)
//                case DRecord => (idCx.ty(d.name), true)
//                case DInterface =>
//                  val ext = d.body.asInstanceOf[Interface].ext
//                  if (ext.cpp && !ext.cx)
//                    (idCx.ty(d.name), true)
//                  else
//                    (s"I${withNs(namespace, idCx.ty(d.name))}", true)
//              }
//              case e: MExtern => e.body match {
//                case i: Interface => if (i.ext.cx) (s"I${e.cx.typename}", true) else (e.cx.typename, true)
//                case _ => if (needRef) (e.cx.boxed, true) else (e.cx.typename, e.cx.reference)
//              }
//              case p: MParam => throw new AssertionError("Parameter should not happen at Cx top level")
//            }
//            base
//        }
//      }
//      def expr(tm: MExpr, needRef: Boolean): (String, Boolean) = {
//        val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
//        base(tm, needRef) + args
//      }
//      expr(tm, needRef)
//    }
//    f(tm, needRef)
//  }

    def toCxType(ty: TypeRef, namespace: Option[String] = None): (String, Boolean) = toCxType(ty.resolved, namespace, false)
    def toCxType(ty: TypeRef, namespace: Option[String], needRef: Boolean): (String, Boolean) = toCxType(ty.resolved, namespace, needRef)
    def toCxType(tm: MExpr, namespace: Option[String]): (String, Boolean) = toCxType(tm, namespace, false)
    def toCxType(tm: MExpr, namespace: Option[String], needRef: Boolean): (String, Boolean) = {
    def base(m: MExpr, namespace: Option[String], needRef: Boolean): (String, Boolean) = m.base match {
      case p: MPrimitive => (p.cxName, false)
      case MString => ("Platform::String", true)
      case MDate => ("Windows::Foundation::DateTime", true)
      case MBinary => ("Platform::Array<uint16_t>", true)
      case MOptional => // We use "nullptr" for the empty optional.
        assert(tm.args.size == 1)
        val arg = tm.args.head
        arg.base match {
          case MOptional => throw new AssertionError("nested optional?")
          case p: MPrimitive => (p.cxBoxed, true)
          case m => expr(arg, namespace, true)
        }
      case MList => ("Windows::Foundation::Collections::IVector", true)
      case MSet => ("Windows::Foundation::Collections::IMap", true)
      case MMap => ("Windows::Foundation::Collections::IMap", true)
      case d: MDef =>
        d.defType match {
          case DEnum => (withNs(namespace, idCx.enumType(d.name)), false)
          case DRecord => (withNs(namespace, idCx.ty(d.name)), true)
          case DInterface =>
            val ext = d.body.asInstanceOf[Interface].ext
            if (ext.cpp && !ext.cx)
              (idCx.ty(d.name), true)
            else
              (s"I${withNs(namespace, idCx.ty(d.name))}", true)
        }
      case e: MExtern => e.body match {
        case i: Interface => if (i.ext.cx) (s"I${e.cx.typename}", true) else (e.cx.typename, true)
        case _ => (e.cpp.typename, needRef)
      }
      case p: MParam => (idCx.typeParam(p.name), needRef)
    }
    def exprWithReference(tm: MExpr, namespace: Option[String], needRef:Boolean): String = {
      val (arg, ref) = expr(tm, namespace, needRef)
      if(ref) s"$arg^" else arg
    }
    def expr(tm: MExpr, namespace: Option[String], needRef: Boolean): (String, Boolean) = {

      val args = tm.base match {
        case MOptional =>
          assert(tm.args.size == 1)
          val arg = tm.args.head
          arg.base match {
            case MOptional => throw new AssertionError("nested optional?")
//            case p: MPrimitive => ""
            case m => "" //if (tm.args.isEmpty) "" else tm.args.map(arg => exprWithReference(arg, namespace, needRef)).mkString("<", ", ", ">") //(tm.args[0].typename, true)
          }
        case MSet => if (tm.args.size == 1) (tm.args :+ tm.args(0)).map(arg => exprWithReference(arg, namespace, needRef)).mkString("<", ", ", ">") else tm.args.map(arg => exprWithReference(arg, namespace, needRef)).mkString("<", ", ", ">")
        case MMap => tm.args.map(arg => exprWithReference(arg, namespace, needRef)).mkString("<", ", ", ">")
        case d => if (tm.args.isEmpty) "" else tm.args.map(arg => exprWithReference(arg, namespace, needRef)).mkString("<", ", ", ">")
      }
      val (ret, ref) = base(tm, namespace, needRef)
      (ret+ args, ref)
    }
    expr(tm, namespace, needRef)
  }

  // this can be used in c++ generation to know whether a const& should be applied to the parameter or not
  private def toCxParamType(tm: MExpr, namespace: Option[String] = None): String = {
    val (name, needRef) = toCxType(tm, namespace)
    name + (if(needRef) "^" else "")
  }

}
