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

import scala.collection.mutable

// Python does not have explicit types, must see if needed, currently returnType and toPythonType used
class PythonMarshal(spec: Spec) extends Marshal(spec) {
  val cMarshal = new CWrapperMarshal(spec)
  val dh = "dh__" // prefix for py files containing helpers for structured typesw

  override def typename(tm: MExpr): String = toPythonType(tm)
  def typename(name: String, ty: TypeDef): String = throw new NotImplementedError() //idPython.ty(name)

  override def fqTypename(tm: MExpr): String = throw new NotImplementedError()
  def fqTypename(name: String, ty: TypeDef): String = throw new NotImplementedError()

  override def paramType(tm: MExpr): String = typename(tm)
  override def fqParamType(tm: MExpr): String = throw new NotImplementedError()

  override def returnType(ret: Option[TypeRef]): String = ret.fold("void")(ty => toPythonType(ty.resolved))
  override def fqReturnType(ret: Option[TypeRef]): String = throw new NotImplementedError()

  override def fieldType(tm: MExpr): String = typename(tm)
  override def fqFieldType(tm: MExpr): String = fqTypename(tm)

//  override def toCpp(tm: MExpr, expr: String): String = throw new AssertionError("direct python to cpp conversion not possible")
//  override def fromCpp(tm: MExpr, expr: String): String = throw new AssertionError("direct cpp to python conversion not possible")

  def references(m: Meta, exclude: String): Seq[SymbolReference] = m match {
    case d: MDef => {
      val className = idPython.className(d.name)
      if (idPython.local(d.name) != idPython.local(exclude)) {
        d.defType match {
          case DInterface  =>
            List(
              ImportRef("from " + idPython.local(d.name) + " import " + className),
              ImportRef("from " + idPython.local(d.name)  + " import " + className + "Helper"))
          case DRecord =>
            List(
              ImportRef("from djinni.pycffi_marshal import CPyRecord"),
              ImportRef("from " + idPython.local(d.name) + " import " + className),
              ImportRef("from " + idPython.local(d.name) + "_helper" + " import " + className + "Helper"))
          case DEnum => List(
              ImportRef("from djinni.pycffi_marshal import CPyEnum"),
              ImportRef("from " + idPython.local(d.name)  + " import " + className))
        }
      }
      else List()
    }
    case mp: MPrimitive => List(ImportRef("from djinni.pycffi_marshal import CPyPrimitive"))
    case MString => List(ImportRef("from djinni.pycffi_marshal import CPyString"))
    case MBinary => List(ImportRef("from djinni.pycffi_marshal import CPyBinary"))
    case MDate => List(ImportRef("from djinni.pycffi_marshal import CPyDate"))
    case MList => List(ImportRef("from djinni.pycffi_marshal import CPyObject"))
    case MSet | MMap => List(ImportRef("from djinni.pycffi_marshal import CPyObject, CPyObjectProxy"))
    case e: MExtern => List() // TODO: implement e: MExtern
    case _ => List()
  }

  def getExprIdlName(tm: MExpr) = toPythonType(tm)
  def referencesForContainer(tm: MExpr, exclude: String): mutable.TreeSet[String] = {
    val refs = mutable.TreeSet[String]()
    def getRef(tm: MExpr): Unit = {
      val idlName = toPythonType(tm)
        tm.base match {
          case MOptional => {
            tm.args(0).base match {
              case m @ (MPrimitive(_,_,_,_,_,_,_,_) | MDate) => {
                refs.add("from djinni.pycffi_marshal import CPyBoxed" + idPython.className(m.asInstanceOf[MOpaque].idlName))
              }
              case _ => getRef(tm.args(0))
            }
          }
          case MList => {
            refs.add("from djinni.pycffi_marshal import CPyObject")
            if (idPython.className(idlName) != idPython.className(exclude)) {
              refs.add("from " + dh + idlName + " import " + idPython.className(idlName) + "Helper")
            }
            getRef(tm.args(0))
          }
          case MSet | MMap => {
            refs.add("from djinni.pycffi_marshal import CPyObject, CPyObjectProxy")
            if (idPython.className(idlName) != idPython.className(exclude)) {
              refs.add("from " + dh +idlName + " import " + idPython.className(idlName) + "Helper")
              refs.add("from " + dh + idlName + " import " + idPython.className(idlName) + "Proxy")
            }
            getRef(tm.args(0))
            if (tm.base == MMap) getRef(tm.args(1))
          }
          case d: MDef => d.defType match {
            case DInterface =>
            case DRecord =>
              refs.add("from " + idPython.local(d.name)  + " import " + idPython.className(d.name))
              refs.add("from djinni.pycffi_marshal import CPyRecord")
            case DEnum => refs.add("from djinni.pycffi_marshal import CPyEnum")
          }
          case mp: MPrimitive => refs.add("from djinni.pycffi_marshal import CPyPrimitive")
          case MString => refs.add("from djinni.pycffi_marshal import CPyString")
          case MBinary => refs.add("from djinni.pycffi_marshal import CPyBinary")
          case MDate => refs.add("from djinni.pycffi_marshal import CPyDate")
          case _ =>
        }
    }

    getRef(tm)
    return refs
  }


  private def toPythonType(ty: TypeRef): String = toPythonType(ty.resolved) // see if this works wutg getIdlName
  private def toPythonType(tm: MExpr): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "string"
      case MBinary => "binary"
      case MDate => "date"
      case MList => "list"
      case MSet => "set"
      case MMap => "map"
      case MOptional => tm.args(0).base match {
        case MPrimitive(_,_,_,_,_,_,_,_) | MDate => "boxed"
        case _ => "optional"
      }
      case d: MDef => d.defType match {
        case DInterface => "interface_" + d.name
        case DRecord => "record_" + d.name
        case DEnum => "enum_" + d.name
      }
      case e: MExtern => "extern" // TODO: implement e: MExtern
      case _ => throw new NotImplementedError()
    }
    def expr(tm: MExpr): String = {
      val baseTy = base(tm.base)
      baseTy match {
        case "boxed" | "optional" => baseTy + "_" + toPythonType(tm.args(0))
        // for list, set, map we return the name of the helper
        case "list" | "set" => idPython.local(baseTy + (if (tm.args.isEmpty) "" else "_" + toPythonType(tm.args(0))))
        case "map" => idPython.local(baseTy + (if (tm.args.isEmpty) "" else "_"+ toPythonType(tm.args(0))) + "_" + toPythonType(tm.args(1)))
        case  _ => baseTy + (if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">"))
      }
    }
    expr(tm)
  }

  def isPrimitive(ty: TypeRef): Boolean = ty.resolved.base match {
    case mp: MPrimitive => true
    case _ => false
  }

  // TODO: pyName, isPacked and getPacked have been removed on a different branch (keep them removed when rebasing)
  def pyName(name: String, ty: MExpr): String = ty.base match {
    case MString => "pys_" + name
    case MBinary => "pybin_" + name
    case _ => name

  }
  def pyName(name: String, ty: TypeRef): String = ty.resolved.base match {
    case MOptional =>
      if (isPacked(ty))  "pyopt_" + name
      else name
    case _ => pyName(name, ty.resolved)
  }

  def isPacked(ty: MExpr): Boolean = ty.base match {
    case MString | MBinary => true
    case _ => false

  }
  def isPacked(ty: TypeRef): Boolean = ty.resolved.base match {
    case MOptional =>
      ty.resolved.args(0).base match {
        case MPrimitive(_,_,_,_,_,_,_,_) | MDate => true
        case MString | MBinary => true
        case _ => false
      }
    case e: MExtern => false // TODO: implement e: MExtern
    case _ => isPacked(ty.resolved)
  }

  // TODO: replace with 1 case here of convert fromo + as ..
  def getPacked(arg: MExpr, isOpt: Boolean, argName: String, pyArgName: String) : String = {
    val opt_s = if (isOpt) "Opt" else ""
    arg.base match {
      case MString => "CPyString.fromPy" + opt_s + p(argName) + " as " + pyArgName
      case MBinary => "CPyBinary.fromPy" + opt_s + p(argName) + " as " + pyArgName
      case _ => throw new NotImplementedError()
    }
  }
  def getPacked(arg: Field, argName: String, pyArgName: String) : String = {
    arg.ty.resolved.base match {
      case MOptional => {
        arg.ty.resolved.args(0).base match {
          case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
            val idlName = arg.ty.resolved.args(0).base.asInstanceOf[MOpaque].idlName
            "CPyBoxed" + idPython.className(idlName) + ".fromPyOpt" + p(argName) + " as " + pyArgName
          case _ => getPacked(arg.ty.resolved.args(0), true, argName, pyArgName)
        }
      }
      case e: MExtern => argName // TODO: implement e: MExtern
      case _ => getPacked(arg.ty.resolved, false, argName, pyArgName)
    }
  }

  def getClassHoldingCDataSet(tm: MExpr): String =  tm.base match {
    case MList | MSet | MMap => idPython.className(getExprIdlName(tm)) + "Helper"
    case d: MDef => idPython.className(d.name)
    case _ => ""
  }

  // from Python names
  def fromRAII(name: String, ty: MExpr, isOpt: Boolean) = {
    ty.base match {
      case MString => idPython.method(name + ".get_djinni_string()")
      case MBinary => idPython.method(name + ".get_djinni_binary()")
      case MOptional => convertFrom(name, ty, true)
      case e: MExtern => name // TODO: implement e: MExtern
      case _ => convertFrom(name, ty, isOpt)
    }
  }
  def fromRAII(name: String, ty: TypeRef): String = ty.resolved.base match {
    case MOptional => {
      ty.resolved.args(0).base match {
        case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
          idPython.method(name + ".get_djinni_boxed" + "()")
        case _ => fromRAII(name, ty.resolved.args(0), true)
      }
    }
    case e: MExtern => name // TODO: implement e: MExtern
    case _ => fromRAII(name, ty.resolved, false)
  }

  def releaseRAII(name: String, ty: MExpr, isOpt: Boolean) ={
    ty.base match {
      case MString => idPython.method(name + ".release_djinni_string()")
      case MBinary => idPython.method(name + ".release_djinni_binary()")
      case e: MExtern => name // TODO: implement e: MExtern
      case _ => fromRAII(name, ty, isOpt) // nothing else needs to be released yet, the asserts for empty c_data_sets would fail otherwise

    }
  }
  def releaseRAII(name: String, ty: TypeRef): String = ty.resolved.base match {
    case MOptional =>
      ty.resolved.args(0).base match {
        case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
          idPython.method(name + ".release_djinni_boxed" + "()")
        case _ => releaseRAII(name, ty.resolved.args(0), true)
      }
    case e: MExtern => name // TODO: implement e: MExtern
    case _ => releaseRAII(name, ty.resolved, false)
  }

  // Get to data from within C structure
  def convertTo(name: String, ty: TypeRef): String =  convertTo(name, ty.resolved, false)
    def convertTo(name: String, ty: MExpr, isOpt: Boolean): String = {
    val local = idPython.local(name)
    val idlName = idPython.className(getExprIdlName(ty))
    val opt_s = if (isOpt) "Opt" else ""

    ty.base match {
      case mp: MPrimitive => "CPyPrimitive.toPy" + p(local)
      case MString | MBinary | MDate => "CPy" + idlName + ".toPy" + opt_s + p(local)
      case MList => "CPyObject.toPy" + opt_s + p(idlName + "Helper" + ".c_data_set" + ", " + local)
      case MMap | MSet => "CPyObjectProxy.toPyObj" + opt_s + p(idlName + "Helper" + ".c_data_set" + ", " + local)
      case d: MDef => d.defType match {
        case DInterface => idPython.className(d.name) + "Helper" + ".toPy" + p(local)
        case DRecord => "CPyRecord.toPy" + opt_s + p(idPython.className(d.name) + ".c_data_set" + ", " + name) // TODO different in that it does not .local, why would it
        case DEnum => "CPyEnum.toPy" + opt_s + p(idPython.className(d.name) + ", " + name)
      }
      case MOptional => {
        ty.args(0).base match {
          case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
            val idlName = ty.args(0).base.asInstanceOf[MOpaque].idlName
            "CPyBoxed" + idPython.className(idlName) + ".toPyOpt" + p(local)
          case _ => convertTo(name, ty.args(0), true)
        }
      }
      case e: MExtern => name // TODO: implement e: MExtern
      case _ => name
    }
  }

  // to avoid with blocks in strings and binaries toPy
  def convertToRelease(name: String, ty: TypeRef): String = convertToRelease(name, ty.resolved)
  def convertToRelease(name: String, ty: MExpr): String = {
    val local = idPython.local(name)
    val idlName = idPython.className(getExprIdlName(ty))
    ty.base match {
      case MString | MBinary => "CPy" + idlName + ".toPyWithoutTakingOwnership" + p(local)
      case MOptional => ty.args(0).base match {
        case m @ (MPrimitive(_,_,_,_,_,_,_,_) | MDate) =>
          val idlName = m.asInstanceOf[MOpaque].idlName
          "CPyBoxed" + idPython.className(idlName) + ".toPyOptWithoutTakingOwnership" + p(local)
        case MString | MBinary => convertToRelease(name, ty.args(0))
        case _ => convertTo(name, ty, false)
      }
      case e: MExtern => name // TODO: implement e: MExtern
      case _ => convertTo(name, ty, false)
    }
  }

  def convertFrom(name: String, ty: TypeRef): String =  convertFrom(name, ty.resolved, false)
    def convertFrom(name: String, ty: MExpr, isOpt: Boolean): String = {
    val local = idPython.local(name)
    val idlName = idPython.className(getExprIdlName(ty))
    val opt_s = if (isOpt) "Opt" else ""

    ty.base match {
      case mp: MPrimitive => "CPyPrimitive.fromPy" + p(local)
      case MString | MBinary | MDate => "CPy" + idlName + ".fromPy" + opt_s + p(local)
      case MList => "CPyObject.fromPy" + opt_s + p(idlName + "Helper" + ".c_data_set, " + local)
      case MMap | MSet => "CPyObjectProxy.fromPy" + opt_s + p(idlName + "Helper" + ".c_data_set, " + idlName + "Proxy" + p(local))
      case d: MDef => d.defType match {
        case DInterface => idPython.className(d.name) + "Helper" + ".fromPy(" + local + ")"
        case DRecord => "CPyRecord.fromPy" + opt_s + p(idPython.className(d.name) + ".c_data_set, " + local)
        case DEnum => "CPyEnum.fromPy" + opt_s + p(local)
      }
      case MOptional => {
        ty.args(0).base match {
          case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
            val idlName = ty.args(0).base.asInstanceOf[MOpaque].idlName
            "CPyBoxed" + idPython.className(idlName) + ".fromPyOpt" + p(local)
          case _ => convertFrom(name, ty.args(0), true)
        }
      }
      case e: MExtern => name // TODO: implement e: MExtern
      case _ => name
    }
  }

  def privateClassMember(s: String) = "_" + s // private class member
}
