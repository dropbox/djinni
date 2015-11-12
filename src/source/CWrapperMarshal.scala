/**
 * Copyright 2015 Dropbox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package djinni

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._

class CWrapperMarshal(spec: Spec) extends Marshal(spec) { // modeled(pretty much copied) after CppMarshal, not fully C-like
  val cppMarshal = new CppMarshal(spec)

  val pythonCdefIgnore = " // python_cdef_ignore"
  val pythonSetSourceIgnore = " // python_setsource_ignore"
  val djinniWrapper = "DjinniWrapper"
  val cw = "cw__" // prefix for c wrapper files
  val dh = "dh__" // prefix for c files containing djinni helpers for records
  val pyHelper = "py_helper_"
  val djinniObjectHandle = "DjinniObjectHandle"

  def ctypename(tm: MExpr, forHeader: Boolean): String = cParamType(tm, forHeader)
  override def typename(tm: MExpr): String = cParamType(tm, false)
  def typename(name: String, ty: TypeDef): String = ty match {
    case e: Enum => idCpp.enumType(name)
    case i: Interface => idCpp.ty(name)
    case r: Record => idCpp.ty(name)
  }
  override def fqTypename(tm: MExpr): String = throw new NotImplementedError()

  def cParamType(tm: MExpr, forHeader: Boolean = false): String = toCParamType(tm, forHeader)
  def cParamType(tm: TypeRef, forHeader: Boolean): String = cParamType(tm.resolved, forHeader)

  override def paramType(tm: MExpr): String = cParamType(tm, false)
  override def fqParamType(tm: MExpr): String = throw new NotImplementedError()

  def cReturnType(ret: Option[TypeRef], forHeader: Boolean = false): String = {
    if (ret.isEmpty) return "void"
    return toCType(ret.get, forHeader)
  }
  override def returnType(ret: Option[TypeRef]): String = cReturnType(ret, false)
  override def fqReturnType(ret: Option[TypeRef]): String = throw new NotImplementedError()


  def cFieldType(tm: MExpr, forHeader: Boolean) = ctypename(tm, forHeader)
  override def fieldType(tm: MExpr): String = ctypename(tm, false)
  override def fqFieldType(tm: MExpr): String = throw new NotImplementedError()

//  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")
//  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("cpp to cpp conversion")

  def references(m: Meta, exclude: String): Seq[SymbolReference] = m match {
    case p: MPrimitive => p.idlName match {
      case "i8" | "i16" | "i32" | "i64" => List(ImportRef("<stdint.h>" + pythonCdefIgnore))
      case "bool" => List(ImportRef("<stdbool.h>" + pythonCdefIgnore))
      case _ => List()
    }
    case MDate => List(ImportRef("<chrono>" + pythonSetSourceIgnore))
    case MBinary => List(ImportRef("<vector>" + pythonSetSourceIgnore),
                         ImportRef("<stdint.h>" + pythonCdefIgnore))
    case MOptional => List(ImportRef(spec.cppOptionalHeader))
    case d: MDef => d.defType match {
      case DInterface => List(ImportRef(q(cw + d.name + ".hpp")))
      case DRecord => List(ImportRef(q(dh + d.name + ".hpp")))
      case DEnum => List(ImportRef(q(d.name + ".hpp")), ImportRef(q(dh + d.name + ".hpp")))
    }
    case e: MExtern => throw new NotImplementedError()
    case _ => List()
    }

  // Types that need RAII will be placed in unique pointers at acquisition time, and released when the language
  // boundary is crossed; this function helps inform that decision in the generator
  def needsRAII(ty: TypeRef): Boolean = needsRAII(ty.resolved)
  def needsRAII(tm: MExpr): Boolean = tm.base match {
   case MString | MBinary => true
    case MList | MSet | MMap => true
    case d: MDef => d.defType match {
      case DRecord => true
      case DInterface => true
      case DEnum => false // we pass as ints
    }
    case MOptional => tm.args(0).base match {
      case mp: MPrimitive => true
      case MDate => true
      case _ => needsRAII(tm.args(0))
    }
   case e: MExtern => throw new NotImplementedError()
   case _ => false
  }

  def canRAIIUseStandardUniquePtr(tm: MExpr): Boolean = tm.base match {
    case MString | MBinary => true
    case MOptional => tm.args(0).base match {
      case mp: MPrimitive => true
      case MString | MBinary => true
      case MDate => true
      case _ => false
    }
    case e: MExtern => throw new NotImplementedError()
    case _ => false
  }

  private def toCType(ty: TypeRef, forHeader: Boolean): String = toCType(ty.resolved, forHeader)
  private def toCType(tm: MExpr,  forHeader: Boolean): String = {
    def base(m: Meta): String = {
      val structPrefix = if (forHeader) "struct " else ""

      m match {
        case p: MPrimitive => p.cName
        case MDate => "uint64_t"
        case MString | MBinary =>
          val idlName = idCpp.ty(m.asInstanceOf[MOpaque].idlName)
          structPrefix + "Djinni" + idlName + " *"
        case MList | MSet | MMap => structPrefix + djinniObjectHandle + " *"
        case MOptional => tm.args(0).base match  {
          case m @ (MPrimitive(_,_,_,_,_,_,_,_) | MDate) =>
            val idlName = m.asInstanceOf[MOpaque].idlName
            structPrefix + "DjinniBoxed" + idCpp.ty(idlName) + " *"
          case MList | MSet | MMap => structPrefix + "DjinniOptionalObjectHandle *"
          case d: MDef =>
            d.defType match {
              case DRecord => structPrefix + "DjinniOptionalRecordHandle *"
              case _ => base(tm.args(0).base)
            }
          case _ => base(tm.args(0).base)
        }
        case d: MDef =>
          d.defType match {
            case DEnum => "int"
            case DRecord => structPrefix + "DjinniRecordHandle *"
            case DInterface =>  structPrefix + djinniWrapper + idCpp.ty(d.name) + " *"
          }
        case p: MParam => idCpp.typeParam(p.name)
        case e: MExtern => throw new NotImplementedError()
      }
    }
    def expr(tm: MExpr): String = {
        base(tm.base)
    }
    expr(tm)
  }

  // getting the idl name for a type
  // useful for determining file names for container helpers ex: list_set_string
  // useful also in the marshaler (allows writing less code)
  def getExprIdlName(tm: MExpr): String = toCIdlType(tm)

  def getReleaseMethodName(tm: MExpr): String =  tm.base match {
    case MString | MBinary => "delete_djinni_" + idCpp.local(getExprIdlName(tm))
    case MList | MSet | MMap => getExprIdlName(tm) + "___delete"
    case d: MDef => d.defType match  {
      case DInterface => idCpp.method(d.name) + "___wrapper_dec_ref"
      case _ => idCpp.method(d.name) + "___delete"
    }
    case MOptional => tm.args(0).base match {
      case MString | MBinary => getReleaseMethodName(tm.args(0))
      case mp: MPrimitive => "delete_djinni_boxed_" + idCpp.method(mp.asInstanceOf[MOpaque].idlName)
      case MDate => "delete_djinni_boxed_date"
      case d: MDef => d.defType match {
        case DInterface => getReleaseMethodName(tm.args(0))
        case _ => "optional_" + getReleaseMethodName(tm.args(0))
      }
      case _ => "optional_" + getReleaseMethodName(tm.args(0))
    }
    case _ => throw new NotImplementedError()
  }


  private def toCIdlType(ty: TypeRef): String = toCIdlType(ty.resolved)
  private def toCIdlType(tm: MExpr): String = {
    def baseToIdl(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case d: MDef =>
        d.defType match {
          case DRecord => "record_" + d.name
          case DInterface => "interface_" + d.name
          case DEnum => "enum_" + d.name
        }
      case p: MParam => idCpp.typeParam(p.name)
      case e: MExtern => "extern"
      case MOptional => tm.args(0).base match {
        case mp: MPrimitive => "boxed"
        case _ => "optional"
      }
      case _ => m.asInstanceOf[MOpaque].idlName
    }

    def exprIdlName(tm: MExpr): String = {
      val baseTy = baseToIdl(tm.base)
      baseTy match {
        case "boxed" | "optional" => baseTy + "_" + toCIdlType(tm.args(0))
        // for list, set, map we return the name of the helper
        case "list" | "set" => idCpp.local(baseTy + (if (tm.args.isEmpty) "" else "_" + toCIdlType(tm.args(0))))
        case "map" =>  idCpp.local(baseTy + (if (tm.args.isEmpty) "" else "_"+ toCIdlType(tm.args(0))) + "_" + toCIdlType(tm.args(1)))
        case _ => baseTy
      }
    }

    exprIdlName(tm)
  }

// this can be used in c++ generation to know whether a const& should be applied to the parameter or not
  private def toCParamType(tm: MExpr, forHeader: Boolean): String = {
    val cType = toCType(tm, forHeader)
    val refType = "const " + cType + " &"
    val valueType = cType

    def toType(expr: MExpr): String = expr.base match {
      case p: MPrimitive => valueType
      case MString => valueType // DjinniString
      case MBinary => valueType // DjinniBinary
      case MDate => valueType // uint64_t
      case MList => valueType
      case MSet => valueType
      case MMap => valueType
      case MOptional => valueType
      case d: MDef => d.defType match {
        case DEnum => valueType
        case DInterface => valueType
        case DRecord => valueType
        case _  => refType
      }
      case e: MExtern => throw new NotImplementedError()
      case _ => refType
    }
    toType(tm)
  }

  def removePointer(s: String): String = if(s(s.length -1) == '*') s.slice(0, s.length-2) else s
  def removeComment(s: String): String = {
    val idx = s.indexOf("//")
    if (idx != -1) {
      return s.slice(0, idx-1)
    }
    else return s
  }
  def pyCallback(s: String): String = "s_py_callback_" + s

  def wrappedName(s: String): String = djinniWrapper + s // del

  // Get to data from within C structure
  def convertTo(name: String, ty: TypeRef): String =  convertTo(name, ty.resolved)
  def convertTo(name: String, ty: MExpr): String = {
    val local = idCpp.local(name)
    ty.base match {
      case MOptional => {
        ty.args(0).base match {
          case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
            val idlName = ty.args(0).base.asInstanceOf[MOpaque].idlName
            "DjinniBoxed" + idCpp.ty(idlName) + "::toCpp" + p("std::move" + p(local))
          case MString | MBinary =>
            val idlName = ty.args(0).base.asInstanceOf[MOpaque].idlName
            "DjinniOptional" + idCpp.ty(idlName) + "::toCpp" + p("std::move" + p(local))
          case MList | MMap | MSet => "Djinni" + idCpp.ty(toCIdlType(ty.args(0))) + "::toCpp" + p("std::move" + p(local))
          case d: MDef => d.defType match {
            case DRecord => "Djinni" + idCpp.ty(d.name) + "::toCpp"+ p("std::move" + p(local))
            case DEnum => "get_boxed_enum_" + idCpp.method(d.name) + "_from_int32" + p(local)
            case _ => convertTo(local, ty.args(0))
          }
          case _ => convertTo(local, ty.args(0))
        }
      }
      case MDate => "DjinniDate::toCpp" + p(local)
      case MString| MBinary| MList | MMap | MSet => "Djinni" + idCpp.ty(toCIdlType(ty)) + "::toCpp" + p("std::move" + p(local))
      case d: MDef => d.defType match {
        case DInterface => djinniWrapper + idCpp.ty(d.name) + "::get" + p("std::move" + p(local))
        case DRecord => "Djinni" + idCpp.ty(d.name) + "::toCpp" + p("std::move" + p(local))
        case DEnum => "static_cast<" + withCppNs(idCpp.enumType(d.name)) + ">" + p(local)
      }
      case e: MExtern => throw new NotImplementedError()
      case _ =>  local // MParam <- didn't need to do anything here
    }
  }

  // Pack data into C structure (for returning C structure)
  def convertFrom(name:String, ty: TypeRef): String = convertFrom(name, ty.resolved)
  def convertFrom(name:String, ty: MExpr): String = {
      val local = idCpp.local(name)
      ty.base match  {
        case MOptional => {
          ty.args(0).base match {
            case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
              val idlName = ty.args(0).base.asInstanceOf[MOpaque].idlName
              "DjinniBoxed" + idCpp.ty(idlName) + "::fromCpp" + p(local)
            case MString | MBinary  =>
              val idlName = ty.args(0).base.asInstanceOf[MOpaque].idlName
              "DjinniOptional" + idCpp.ty(idlName) + "::fromCpp" + p(local)
            case MList | MMap | MSet => "Djinni" + idCpp.ty(toCIdlType(ty.args(0))) + "::fromCpp" + p(local)
            case _ => convertFrom(local, ty.args(0))
          }
        }
        case MString | MBinary | MDate | MList | MSet | MMap =>
          val idlName = idCpp.ty(toCIdlType(ty))
          "Djinni" + idlName + "::fromCpp" + p(local)
        case d: MDef => d.defType match {
          case DInterface => djinniWrapper + idCpp.ty(d.name) + "::wrap" + p("std::move" + p(local))
          case DRecord => "Djinni" + idCpp.ty(d.name) + "::fromCpp" + p(local)
          case DEnum => "int32_from_enum_" + idCpp.method(d.name) + p(local)
        }
        case e: MExtern => throw new NotImplementedError()
        case _ => local // TODO: MParam <- didn't need to do anything here
      }
  }

  def checkForException(s: String) = "lib.check_for_exception" + p(s)

}

