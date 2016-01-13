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
import djinni.writer.IndentWriter
import sun.reflect.generics.tree.ReturnType

import scala.collection.mutable

class CWrapperGenerator(spec: Spec) extends Generator(spec) {
  val marshal = new CWrapperMarshal(spec)
  val cppMarshal = new CppMarshal(spec)
  val dw = "djinni_this"

  def getCppDefArgs(m: Interface.Method, self: String) = {
    if (m.static || self == "") {
      m.params.map(p => cppMarshal.fqParamType(p.ty) + " " +  idCpp.local(p.ident.name)).mkString("(", ", ", ")")
    } else {
      (Seq[String](self) ++
        m.params.map(p => cppMarshal.fqParamType(p.ty) + " " +  idCpp.local(p.ident.name))).mkString("(", ", ", ")")
    }
  }

  def getDefArgs(m: Interface.Method, self: String, forHeader: Boolean) = {
    if (m.static || self == "") {
      m.params.map(p => marshal.cParamType(p.ty, forHeader) + " " +  idCpp.local(p.ident.name)).mkString("(", ", ", ")")
    } else {
      (Seq[String](self) ++
        m.params.map(p => marshal.cParamType(p.ty, forHeader) + " " +  idCpp.local(p.ident.name))).mkString("(", ", ", ")")
    }
  }

  def getCArgTypes(m: Interface.Method, self: String, forHeader: Boolean) = {
    (Seq(self) ++ m.params.map(p => marshal.cParamType(p.ty, forHeader))).mkString("(", ", ", ")")
  }

  def getRecordTypes(r: Record, forHeader: Boolean) = {
    r.fields.map( f => marshal.cParamType(f.ty, forHeader))
  }

  def getElementTypeRef(tm: MExpr, ident: Ident): TypeRef = {
    val tref = TypeRef(TypeExpr(ident, Seq()))
    tref.resolved = tm
    return tref
  }

  def getContainerElTypeRef(tm: MExpr, index: Int, ident: Ident): TypeRef = {
    val tref = TypeRef(TypeExpr(ident, Seq()))
    tref.resolved = tm.args(index)
    return tref
  }

  def declareGlobalGetter(methodName: String, ret: String, cArgs: String, className: String, w: IndentWriter): Unit = {
    val methodSignature = ret + p(" * ptr") + cArgs

    w.wl("static " + ret + " " + p(" * " + marshal.pyCallback(idCpp.method(className + methodName))) + cArgs +  ";")
    w.wl
    w.wl("void "  + idCpp.method(className + "_add_callback" + methodName) + p(methodSignature) + " {" ).nested {
      w.wl(marshal.pyCallback(idCpp.method(className + methodName)) + " = ptr;")
    }
    w.wl("}")
    w.wl
  }

  def declareGlobalGetterSignature(methodName: String, ret: String, cArgs: String, className: String, w: IndentWriter): Unit = {
    val methodSignature = ret + p(" * ptr") + cArgs
    w.wl("void "  + idCpp.method(className + "_add_callback" + methodName) + p(methodSignature) + ";" )
  }

  def writeWrapHandleAsUniquePointer(tm: MExpr, createMethod: String, w: IndentWriter): Unit = {
    w.wl("djinni::Handle" + t("DjinniObjectHandle") + " _handle(" +
      createMethod + ", " + "& " + marshal.getReleaseMethodName(tm) + ");")
  }

  def declareObjectUniquePointerHandle(tm: MExpr, classAsMethodName: String, w: IndentWriter): Unit = {
    val createMethod = marshal.pyCallback(idCpp.method(classAsMethodName + "__python_create"))
    writeWrapHandleAsUniquePointer(tm, createMethod + p(""), w)
  }

  def getUniquePointerDeclarationIfNeeded(elName:String, elToFrom: String, elTy: MExpr): String = {
    val indentedElName = if (elName != "") " " + elName else elName
    val decl = if (marshal.needsRAII(elTy)) {
      if (marshal.canRAIIUseStandardUniquePtr(elTy)) {
        // Optional Primitive don't need custom unique pointer deleters
        "std::unique_ptr"  + t(marshal.removePointer(marshal.paramType(elTy))) + indentedElName + p(elToFrom)
      } else {
        "djinni::Handle" + t(marshal.removePointer(marshal.paramType(elTy))) + indentedElName + p(
          elToFrom + ", " + marshal.getReleaseMethodName(elTy))
      }
    } else {
      elToFrom
    }

    return decl
  }

  def declareUniquePointer(elName:String, elToFrom: String, elTy: MExpr, w: IndentWriter): Unit = {
    val declaration = getUniquePointerDeclarationIfNeeded(elName, elToFrom, elTy)
    if (declaration != elToFrom) { // only if unique pointer was needed, write declaration
      w.wl(declaration + ";")
    }
  }

  def raiiForElement(elToFrom: String, elTy: MExpr): String = {
    getUniquePointerDeclarationIfNeeded("", elToFrom, elTy)
  }

  def writeListToCpp(tm: MExpr, ident: Ident, retType: String, classAsMethodName: String, w: IndentWriter): Unit = {
    val elTyRef = getContainerElTypeRef(tm, 0, ident)
    w.wl(retType + "_ret;")
    w.wl("size_t size = " + marshal.pyCallback(idCpp.method(classAsMethodName) + idCpp.method("__get_size") + p("dh.get()") + ";"))
    w.wl("_ret.reserve" + p("size") + ";")
    w.wl
    w.wl("for (int i = 0; i < size; i++) {").nested {
        w.wl("_ret.push_back" + p(marshal.convertTo(raiiForElement(
          marshal.pyCallback(idCpp.method(classAsMethodName) + idCpp.method("__get_elem") + p("dh.get(), i")), tm.args(0)), elTyRef)) + ";")
    }
    w.wl("}")
    w.wl
    w.wl("return _ret;")
  }

  def writeListFromCpp(tm: MExpr, ident: Ident, retType: String, classAsMethodName: String, w: IndentWriter): Unit = {
    val elTyRef = getContainerElTypeRef(tm, 0, ident)
    val addToList = marshal.pyCallback(idCpp.method(classAsMethodName + "__python_add"))
    declareObjectUniquePointerHandle(tm, classAsMethodName, w)

    w.wl("size_t size = dc.size();")
    w.wl("for (int i = 0; i < size; i++) {").nested {
      if (marshal.needsRAII(elTyRef)) {
        // separate taking ownership of memory and function call, when elements need RAII
        w.wl("auto _el = " + marshal.convertFrom("dc[i]", elTyRef) + ";")
        w.wl(addToList + p("_handle.get(), " + "_el.release()") + ";")
      }
      else {
        w.wl(addToList + p("_handle.get(), " + marshal.convertFrom("dc[i]", elTyRef)) + ";")
      }
    }
    w.wl("}")
    w.wl
    w.wl("return _handle;")

  }

  def writeMapToCpp(tm: MExpr, ident: Ident, retType: String, className: String, w: IndentWriter): Unit = {
    val keyTyRef = getContainerElTypeRef(tm, 0, ident)
    val valTyRef = getContainerElTypeRef(tm, 1, ident)

    w.wl(retType + "_ret;")
    // Asking for size makes it easier to know when to stop asking for the next/ deciding what value would signal 'end of map'
    w.wl("size_t size = " + marshal.pyCallback(idCpp.method(className) + idCpp.method("__get_size") + p("dh.get()") + ";"))
    w.wl
    w.wl("for (int i = 0; i < size; i++) {").nested {
      w.wl("auto _key_c = " + raiiForElement(
        marshal.pyCallback(idCpp.method(className) + idCpp.method("__python_next") + p("dh.get()")), tm.args(0)) + "; // key that would potentially be surrounded by unique pointer")
      val key = if (marshal.needsRAII(tm.args(0))) "_key_c.get()" else "_key_c" // don't delete ownership
      w.wl("auto _val = " + marshal.convertTo(raiiForElement(
        marshal.pyCallback(idCpp.method(className) + idCpp.method("__get_value") + p("dh.get(), " + key)), tm.args(1)), valTyRef) + ";")
      w.wl
      w.wl("auto _key = " + marshal.convertTo("_key_c", keyTyRef) + ";")
      w.wl("_ret.emplace(std::move(_key), std::move(_val));")
    }
    w.wl("}")
    w.wl
    w.wl("return _ret;")
  }

  def writeMapFromCpp(tm: MExpr, ident: Ident, retType: String, classAsMethodName: String, w: IndentWriter): Unit = {
    val addToMap= marshal.pyCallback(idCpp.method(classAsMethodName + "__python_add"))
    val keyTyRef = getContainerElTypeRef(tm, 0, ident)
    val valTyRef = getContainerElTypeRef(tm, 1, ident)

    declareObjectUniquePointerHandle(tm, classAsMethodName, w)
    w.wl("for (const auto & it : dc) {").nested {
      val key = if (marshal.needsRAII(tm.args(0))) {
        // separate taking ownership of memory and function call, when keys need RAII
        w.wl("auto _key = " + marshal.convertFrom("it.first", keyTyRef) + ";")
        "_key.release()"
      } else marshal.convertFrom("it.first", keyTyRef)

      val value = if (marshal.needsRAII(tm.args(1))) {
        // separate taking ownership of memory and function call, when values need RAII
        w.wl("auto _val = " +  marshal.convertFrom("it.second", valTyRef) + ";")
        "_val.release()"
      } else marshal.convertFrom("it.second", valTyRef)

      w.wl(addToMap + p("_handle.get(), "+ key + ", " + value) + ";")
    }
    w.wl("}")
    w.wl
    w.wl("return _handle;")

  }

  def writeSetToCpp(tm: MExpr, ident: Ident, retType: String, className: String, w: IndentWriter): Unit = {
    val keyTyRef = getContainerElTypeRef(tm, 0, ident)

    w.wl(retType + "_ret;")
    w.wl("size_t size = " + marshal.pyCallback(idCpp.method(className) + idCpp.method("__get_size") + p("dh.get()") + ";"))
    w.wl
    w.wl("for (int i = 0; i < size; i++) {").nested {
      w.wl("auto _el = " + marshal.convertTo(raiiForElement(
        marshal.pyCallback(idCpp.method(className) + idCpp.method("__python_next") + p("dh.get()")), tm.args(0)), keyTyRef) + ";")
      w.wl("_ret.insert(_el);")
    }
    w.wl("}")
    w.wl
    w.wl("return _ret;")
  }

  def writeSetFromCpp(tm: MExpr, ident: Ident, retType: String, classAsMethodName: String, w: IndentWriter): Unit = {
    val createMap= marshal.pyCallback(idCpp.method(classAsMethodName + "__python_create"))
    val addToMap= marshal.pyCallback(idCpp.method(classAsMethodName + "__python_add"))
    val keyTyRef = getContainerElTypeRef(tm, 0, ident)
    declareObjectUniquePointerHandle(tm, classAsMethodName, w)

    w.wl("for (const auto & it : dc) {").nested {
      val key_val = if (marshal.needsRAII(tm.args(0))) {
        // separate taking ownership of memory and function call, when keys need RAII
        w.wl("auto _key_val = " + marshal.convertFrom("it", keyTyRef) + ";")
        "_key_val.release()"
      } else marshal.convertFrom("it", keyTyRef)

      w.wl(addToMap + p("_handle.get(), "+ key_val) + ";")
    }
    w.wl("}")
    w.wl
    w.wl("return _handle;")
  }

  def declareGlobalGetters(tm: MExpr, ident: Ident, retType: String, className: String, w: IndentWriter,
    getName: String, getType: String, getReturnType: String, addCArgs: String): Unit = {
    val handle = "DjinniObjectHandle *"

    var ret = ""
    var cArgs = ""
    if (getName != "") {
      ret = getReturnType
      cArgs = Seq(handle, getType).mkString("(", ", ", ")")
      declareGlobalGetter(getName, ret, cArgs, className, w)
    }

    ret = "size_t"
    cArgs = Seq(handle).mkString("(", ", ", ")")
    declareGlobalGetter("__get_size", ret, cArgs, className, w)

    ret = "DjinniObjectHandle *"
    cArgs = Seq("").mkString("(", ", ", ")")
    declareGlobalGetter("__python_create", ret, cArgs, className, w)

    ret = "void"
    cArgs = Seq(handle, addCArgs).mkString("(", ", ", ")")
    declareGlobalGetter("__python_add", ret, cArgs, className, w)
  }

  def declareGlobalGettersSignatures(tm: MExpr, ident: Ident, retType: String, className: String, w: IndentWriter,
    getName: String, getType: String, getReturnType: String, addCArgs: String): Unit = {
    val handle = "struct DjinniObjectHandle *"

    var ret = ""
    var cArgs = ""
    if (getName != "") {
      ret = getReturnType
      cArgs = Seq(handle, getType).mkString("(", ", ", ")")
      declareGlobalGetterSignature(getName, ret, cArgs, className, w)
    }

    ret = "size_t"
    cArgs = Seq(handle).mkString("(", ", ", ")")
    declareGlobalGetterSignature("__get_size", ret, cArgs, className, w)

    ret = "struct DjinniObjectHandle *"
    cArgs = Seq("").mkString("(", ", ", ")")
    declareGlobalGetterSignature("__python_create", ret, cArgs, className, w)

    ret = "void"
    cArgs = Seq(handle, addCArgs).mkString("(", ", ", ")")
    declareGlobalGetterSignature("__python_add", ret, cArgs, className, w)
  }

  def writeObjectReleaseMethodSignature(objectAsMethodName: String, objectHandle: String,  w: IndentWriter): Unit ={
    w.wl("void " + objectAsMethodName + "___delete" + p(objectHandle) + ";")
    w.wl("void " + "optional_" + objectAsMethodName + "___delete" + p("struct DjinniOptionalObjectHandle *") + ";")
    w.wl("void " + objectAsMethodName + "_add_callback_" + "__delete" + p(
      "void(* ptr)" + p("struct " + marshal.djinniObjectHandle + " * ")) + ";")
  }

  def writeObjectReleaseMethods(objectAsMethodName: String, objectHandlePtr: String,  w: IndentWriter): Unit = {
    // Callback for delete python object handle
    w.wl("static void(*" + marshal.pyCallback(objectAsMethodName + "___delete") + ")" + p(objectHandlePtr) + ";")
    w.wl("void " + objectAsMethodName + "_add_callback_" + "__delete" + p(
      "void(* ptr)" + p(objectHandlePtr)) + " {").nested {
      w.wl(marshal.pyCallback(objectAsMethodName + "___delete") + " = ptr;")
    }
    w.wl("}")
    w.wl
    // Function to call delete of record from Python
    var cArgs = Seq(objectHandlePtr + " drh").mkString("(", ", ", ")")
    w.wl("void " + objectAsMethodName + "___delete" + cArgs + " {").nested {
      w.wl(marshal.pyCallback(objectAsMethodName + "___delete") + p("drh") + ";")
    }
    w.wl("}")
    cArgs = Seq("DjinniOptionalObjectHandle * " + " drh").mkString("(", ", ", ")")
    w.wl("void " + "optional_" + objectAsMethodName + "___delete" + cArgs + " {").nested {
      w.wl(marshal.pyCallback(objectAsMethodName + "___delete") + p(p(objectHandlePtr) + " drh") + ";")
    }
    w.wl("}")
  }

  def writeOptionalContainerFromCpp(optHandle: String, handle: String, retType: String, djinniWrapper: String, deleteMethod: String, w: IndentWriter): Unit = {
    w.wl("djinni::Handle" + t(optHandle) + " " +  djinniWrapper + "::fromCpp" + p( spec.cppOptionalTemplate + t(retType) + " dc") + " {").nested {
      w.wl("if (dc == std::experimental::nullopt) {").nested {
        w.wl("return nullptr;")
      }
      w.wl("}")
      w.wl("return " + "djinni::optionals::toOptionalHandle" + p("std::move" + p(djinniWrapper + "::fromCpp" + p("std::move(* dc)")) + ", " + deleteMethod) + ";")
    }
    w.wl("}")
    w.wl
  }

  def writeOptionalContainerToCpp(optHandle: String, retTypeStr: String, djinniWrapper: String, reinterpret: String, deleteMethod: String, w: IndentWriter): Unit = {
    w.wl(spec.cppOptionalTemplate + t(retTypeStr) + djinniWrapper + "::toCpp" + p("djinni::Handle" + t(optHandle) + " dh") + " {").nested {
      w.wl(" if (dh) {").nested {
        w.wl("return " + spec.cppOptionalTemplate + t(retTypeStr) + p(djinniWrapper + "::toCpp" + p("djinni::optionals::fromOptionalHandle(std::move(dh), " + deleteMethod +")")) + ";")
      }
      w.wl("}")
      w.wl("return std::experimental::nullopt;")
    }
    w.wl("}")
    w.wl
  }

  def writeContainerFromToCpp(handle: String, optHandle: String, cppHandle: String, cppOptHandle: String, tm: MExpr, retType: String, w: IndentWriter): Unit = {
    w.wl("static " + "djinni::Handle" + t(cppHandle)  + " fromCpp" + p(cppMarshal.fqParamType(tm) + " dc") + ";")
    w.wl("static " + retType + " toCpp" + p("djinni::Handle" + t(cppHandle) + " dh") + ";" )

    w.wl("static " + "djinni::Handle" + t(cppOptHandle) + "fromCpp" +  p( spec.cppOptionalTemplate + t(retType) + " dc") + ";")
    w.wl("static " + spec.cppOptionalTemplate + t(retType) + " toCpp" + p("djinni::Handle" + t(cppOptHandle) + " dh") + ";" )
  }

  def generateList(tm: MExpr, name: String, className: String, ident: Ident, origin: String, h: mutable.TreeSet[String], hpp: mutable.TreeSet[String]): Unit = {
    // className is the composed idl named of the container type
    val handle = "DjinniObjectHandle"
    val handlePtr = handle + " *"
    val optHandle = "DjinniOptionalObjectHandle"
    val optHandlePtr = optHandle + " *"
    val fileName = marshal.dh + name
    val classAsMethodName = name
    val djinniWrapper = "Djinni" + idCpp.ty(name)

    // typerefing to be able to use the cpp returnType function
    val listTyRef = TypeRef(TypeExpr(ident, Seq()))
    listTyRef.resolved = tm
    val retType = cppMarshal.fqReturnType(Some(listTyRef))

    var listElType = marshal.cParamType(getContainerElTypeRef(tm, 0, ident), true)

    writeCHeader(fileName, origin, "", h, true, w => {
      writeObjectReleaseMethodSignature(name, "struct " + handlePtr, w)
        declareGlobalGettersSignatures(tm, ident, retType, classAsMethodName, w,
        "__get_elem", "size_t", listElType,
          listElType)
    })

    listElType = marshal.cParamType(getContainerElTypeRef(tm, 0, ident), false)
    writeContainerCppHeader(fileName, ident, origin, djinniWrapper, handlePtr, optHandlePtr, handle, optHandle, tm, retType)

    writeCFile(fileName, ident.name, origin, hpp, true, w => {
      writeObjectReleaseMethods(name, handlePtr, w)
      // declare global list methods (get size, get elem, create..)
      declareGlobalGetters(tm, ident, retType, classAsMethodName, w,
        "__get_elem", "size_t", listElType,
        listElType)
      // List from cpp
      w.wl("djinni::Handle" + t(handle) + " " + djinniWrapper+ "::fromCpp" + p(cppMarshal.fqParamType(tm) + " dc") + " {").nested {
        writeListFromCpp(tm, ident, retType, classAsMethodName, w)
      }
      w.wl("}")
      w.wl
      // List to cpp
      w.wl(retType + " " + djinniWrapper + "::toCpp" + p("djinni::Handle" + t(handle) + " dh") + " {" ).nested {
          writeListToCpp(tm, ident, retType, classAsMethodName, w)
      }
      w.wl("}")
      w.wl
      writeOptionalContainerFromCpp(optHandle, handle, retType, djinniWrapper, "optional_" + marshal.getReleaseMethodName(tm), w)
      writeOptionalContainerToCpp(optHandle, retType, djinniWrapper, handlePtr,  marshal.getReleaseMethodName(tm), w)
    })
  }

  def generateMap(tm: MExpr, name: String, className: String, ident: Ident, origin: String, h: mutable.TreeSet[String], hpp: mutable.TreeSet[String]): Unit = {
    // className is the composed idl named of the container type
    val handle = "DjinniObjectHandle"
    val handlePtr = "DjinniObjectHandle *"
    val optHandle = "DjinniOptionalObjectHandle"
    val optHandlePtr = "DjinniOptionalObjectHandle *"
    val fileName = marshal.dh + name
    val classAsMethodName = name
    val djinniWrapper = "Djinni" + idCpp.ty(name)

    // typerefing to be able to use the cpp returnType function
    val mapTyRef = TypeRef(TypeExpr(ident, Seq()))
    mapTyRef.resolved = tm
    val retType = cppMarshal.fqReturnType(Some(mapTyRef))

    var keyType = marshal.cParamType(getContainerElTypeRef(tm, 0, ident), true)
    var valType = marshal.cParamType(getContainerElTypeRef(tm, 1, ident), true)

    writeCHeader(fileName, origin, "", h, true, w => {
      writeObjectReleaseMethodSignature(name, "struct " + handlePtr, w)
      declareGlobalGettersSignatures(tm, ident, retType, classAsMethodName, w,
      "__get_value", keyType, valType,
      keyType + ", " + valType)

      // __python_next
      val ret = keyType
      val cArgs = Seq("struct " + handlePtr).mkString("(", ", ", ")")
      declareGlobalGetterSignature("__python_next", ret, cArgs, classAsMethodName, w)
    })

    keyType = marshal.cParamType(getContainerElTypeRef(tm, 0, ident), false)
    valType = marshal.cParamType(getContainerElTypeRef(tm, 1, ident), false)
    writeContainerCppHeader(fileName, ident, origin, djinniWrapper, handlePtr, optHandlePtr, handle, optHandle, tm, retType)

    writeCFile(fileName, ident.name, origin, hpp, true, w => {
      writeObjectReleaseMethods(name, handlePtr, w)
      // declare global list methods (get size, get elem, create..)
      declareGlobalGetters(tm, ident, retType, classAsMethodName, w,
        "__get_value", keyType, valType,
        keyType + ", " + valType)

      // __python_next
      val ret = keyType
      val cArgs = Seq(handlePtr).mkString("(", ", ", ")")
      declareGlobalGetter("__python_next", ret, cArgs, classAsMethodName, w)

      // Map from cpp
      w.wl("djinni::Handle" + t(handle) + " " + djinniWrapper+ "::fromCpp" + p(cppMarshal.fqParamType(tm) + " dc") + " {").nested {
        writeMapFromCpp(tm, ident, retType, classAsMethodName, w)
      }
      w.wl("}")
      w.wl
      // Map to cpp
      w.wl(retType + " " + djinniWrapper + "::toCpp" + p("djinni::Handle" + t(handle) + " dh") + " {" ).nested {
        writeMapToCpp(tm, ident, retType, classAsMethodName, w)
      }
      w.wl("}")
      w.wl

      writeOptionalContainerFromCpp(optHandle,handle, retType, djinniWrapper, "optional_" + marshal.getReleaseMethodName(tm), w)
      writeOptionalContainerToCpp(optHandle, retType, djinniWrapper, handlePtr,  marshal.getReleaseMethodName(tm), w)
    })

  }

  def writeContainerCppHeader(fileName: String, ident: Ident, origin: String, djinniWrapper: String, handlePtr: String,
                              optHandlePtr: String, handle: String, optHandle: String, tm: MExpr, retType: String): Unit = {
    writeCppHeader(fileName, ident.name, origin, "", true, w => {
      w.wl("struct " + djinniWrapper + " {").nested {
        writeContainerFromToCpp(handlePtr, optHandlePtr, handle, optHandle, tm, retType, w)
      }
      w.wl("};")
    })
  }

  def generateSet(tm: MExpr, name: String, className: String, ident: Ident, origin: String, h: mutable.TreeSet[String], hpp: mutable.TreeSet[String]): Unit = {
    // className is the composed idl named of the container type
    val handle = "DjinniObjectHandle"
    val handlePtr = "DjinniObjectHandle *"
    val optHandle = "DjinniOptionalObjectHandle"
    val optHandlePtr = "DjinniOptionalObjectHandle *"
    val fileName = marshal.dh + name
    val classAsMethodName = name
    val djinniWrapper = "Djinni" + idCpp.ty(name)

    // typerefing to be able to use the cpp returnType function
    val setTyRef = TypeRef(TypeExpr(ident, Seq()))
    setTyRef.resolved = tm
    val retType = cppMarshal.fqReturnType(Some(setTyRef))

    var keyType = marshal.cParamType(getContainerElTypeRef(tm, 0, ident), true)

    writeCHeader(fileName, origin, "", h, true, w => {
      writeObjectReleaseMethodSignature(name, "struct " + handlePtr, w)
      declareGlobalGettersSignatures(tm, ident, retType, classAsMethodName, w,
        "", "", keyType,
        keyType)

      // __python_next
      val ret = keyType
      val cArgs = Seq("struct " + handlePtr).mkString("(", ", ", ")")
      declareGlobalGetterSignature("__python_next", ret, cArgs, classAsMethodName, w)
    })

    keyType = marshal.cParamType(getContainerElTypeRef(tm, 0, ident), false)
    writeContainerCppHeader(fileName, ident, origin, djinniWrapper, handlePtr, optHandlePtr, handle, optHandle, tm, retType)

    writeCFile(fileName, ident.name, origin, hpp, true, w => {
      writeObjectReleaseMethods(name, handlePtr, w)
      // declare global list methods (get size, get elem, create..)
      declareGlobalGetters(tm, ident, retType, classAsMethodName, w,
        "", "", keyType,
        keyType)

      // __python_next
      val ret = keyType
      val cArgs = Seq(handlePtr).mkString("(", ", ", ")")
      declareGlobalGetter("__python_next", ret, cArgs, classAsMethodName, w)

      // Set from cpp
      w.wl("djinni::Handle" + t(handle) + " " + djinniWrapper+ "::fromCpp" + p(cppMarshal.fqParamType(tm) + " dc") + " {").nested {
        writeSetFromCpp(tm, ident, retType, classAsMethodName, w)
      }
      w.wl("}")
      w.wl
      // Set to cpp
      w.wl(retType + " " + djinniWrapper + "::toCpp" + p("djinni::Handle" + t(handle) + " dh") + " {" ).nested {
        writeSetToCpp(tm, ident, retType, classAsMethodName, w)
      }
      w.wl("}")
      w.wl
      writeOptionalContainerFromCpp(optHandle, handle, retType, djinniWrapper, "optional_" + marshal.getReleaseMethodName(tm), w)
      writeOptionalContainerToCpp(optHandle, retType, djinniWrapper, handlePtr,  marshal.getReleaseMethodName(tm), w)
    })

  }

  class CRefs(ident: Ident, origin: String) {
    var hpp = mutable.TreeSet[String]()
    var h = mutable.TreeSet[String]()

    def collect(ty: TypeRef, justCollect: Boolean) {
      collect(ty.resolved, justCollect)
    }
    def collect(tm: MExpr, justCollect: Boolean) {
      tm.args.foreach(t => collect(t,justCollect))
      collect(tm.base, justCollect)

      val name = marshal.getExprIdlName(tm)
      val fileName = marshal.dh + name + ".hpp"
      if (justCollect) {
        if (tm.base == MList || tm.base == MSet || tm.base == MMap) {
          hpp.add("#include " + q(fileName))
        }
      } else {
        val className = "struct Djinni" + idCpp.ty(marshal.getExprIdlName(tm))
        if (! writtenFiles.contains(marshal.dh + name + ".hpp") ) {
          writtenFiles.put(fileName.toLowerCase(), fileName)
          tm.base match {
            case MList => generateList(tm, name, className, ident, origin, h, hpp)
            case MMap => generateMap(tm, name, className, ident, origin, h, hpp)
            case MSet => generateSet(tm, name, className, ident, origin, h, hpp)
            case _ =>
          }
        }
      }
    }
    def collect(m: Meta, justCollect: Boolean) = if (justCollect) for(r <- marshal references(m, ident)) r match {
      case ImportRef(arg) =>
        if (arg.endsWith(".hpp\"") || ! arg.contains("."))
          hpp.add("#include " + arg + marshal.pythonCdefIgnore)
        else h.add("#include " + arg)
      case _ =>
    }
  }

  def declareGlobalFunctionPointers(ident: Ident, cClassWrapper: String, methods: Seq[Interface.Method], w: IndentWriter): Unit = {
    for (m <- methods) {
      val ret = marshal.cReturnType(m.ret, false)
      val cArgs = getCArgTypes(m, marshal.djinniObjectHandle + " * ", false)
      w.wl("static " + ret + "(*" + marshal.pyCallback(idCpp.method(ident.name) + "_" + idCpp.method(m.ident.name)) + ")" + cArgs + ";")
    }
    // function to delete handlerto python object
    w.wl("static void(*" + marshal.pyCallback(idCpp.method(ident.name) + "___delete" ) + ")" + p(marshal.djinniObjectHandle + " * ") + ";")
  }

  def declareGlobalRecordFieldGetters(ident: Ident, r: Record, prefix: String, number: Int, w: IndentWriter) = {
    val recordAsMethodName = idCpp.method(ident.name)
    var field_number = number
    for (f <- r.fields) {
      field_number += 1
      val f_id = prefix + field_number.toString()

      val ret = marshal.returnType(Some(f.ty))
      val cArgs = Seq("DjinniRecordHandle *").mkString("(", ", ", ")")
      declareGlobalGetter("_get_" + ident.name + "_f" + f_id, ret, cArgs, recordAsMethodName, w)

    }
    val ret = "DjinniRecordHandle *"
    val cArgs =getRecordTypes(r, false).mkString("(",",",")")
    declareGlobalGetter("_python_create_" + recordAsMethodName, ret, cArgs, recordAsMethodName, w)
  }

  def declareGlobalSignaturesRecordFieldGetters(ident: Ident, r: Record, prefix: String, number: Int, w: IndentWriter) = {
    val recordAsMethodName = idCpp.method(ident.name)
    var field_number = number
    for (f <- r.fields) {
      field_number += 1
      val f_id = prefix + field_number.toString()

      val ret = marshal.cReturnType(Some(f.ty), true)
      val cArgs = Seq("struct DjinniRecordHandle *").mkString("(", ", ", ")")
      declareGlobalGetterSignature("_get_" + ident.name + "_f" + f_id, ret, cArgs, recordAsMethodName, w)
    }

    // For creating record
    var ret = "struct DjinniRecordHandle *"
    var cArgs = getRecordTypes(r, true).mkString("(", ",", ")")
    declareGlobalGetterSignature("_python_create_" + recordAsMethodName, ret, cArgs, recordAsMethodName, w)
    // For releasing record
    ret = "void"
    cArgs = Seq("struct DjinniRecordHandle *").mkString("(", ", ", ")")
    declareGlobalGetterSignature("___delete", ret, cArgs, recordAsMethodName, w)
  }

  def writeRecordToCpp(ident: Ident, r: Record, prefix: String, number: Int, w: IndentWriter) = {
    val recordAsFqClassType = withCppNs(idCpp.ty(ident.name))
    val recordAsMethodName = idCpp.method(ident.name)
    var field_number = number

    // get ownership of record fields memory
    for (f <- r.fields) {
      field_number += 1
      val f_id = prefix + field_number.toString()
      val methodName = recordAsMethodName + "_" + "get_" + ident.name + "_f" + f_id
      if (marshal.needsRAII(f.ty)) {
        declareUniquePointer("_field_" + idCpp.method(f.ident.name),
                             marshal.pyCallback(idCpp.method(methodName) + p("dh.get()")), f.ty.resolved, w)
      }
    }
    w.wl

    field_number = number
    w.wl("auto _aux = " + recordAsFqClassType + "(" ).nested {
      val skipFirst = SkipFirst()
      for (f <- r.fields) {
        field_number += 1
        val f_id = prefix + field_number.toString()
        val methodName = recordAsMethodName + "_" + "get_" + ident.name + "_f" + f_id

        skipFirst { w.wl(",") }
        if (marshal.needsRAII(f.ty)) {
          w.w(marshal.convertTo(" _field_" + idCpp.method(f.ident.name), f.ty)) // RAII done above
        } else {
          w.w(marshal.convertTo(marshal.pyCallback(idCpp.method(methodName) + p("dh.get()")), f.ty))
        }
      }
    }
    w.wl(");")
    w.wl("return _aux;")
  }
  def writeRecordFromCpp(ident: Ident, r: Record,prefix: String, number: Int, w: IndentWriter) = {
    val recordAsMethodName = idCpp.method(ident.name)

    for (f <- r.fields) {
      if (marshal.needsRAII(f.ty)) {
        w.wl("auto  _field_" + idCpp.method(f.ident.name) + " = " + marshal.convertFrom("dr." + f.ident.name, f.ty) + ";")
      }
    }
    w.wl
    w.wl("djinni::Handle" + t("DjinniRecordHandle") + " _aux(").nested {
      w.wl( marshal.pyCallback(recordAsMethodName + idCpp.method("_python_create_") + recordAsMethodName) + "(").nested {
        val skipFirst = SkipFirst()
        for (f <- r.fields) {
          skipFirst {
            w.wl(",")
          }
          if (marshal.needsRAII(f.ty)) {
            w.w("_field_" + idCpp.method(f.ident.name) + ".release()")
          }
          else w.w(marshal.convertFrom(idCpp.local("dr." + f.ident.name), f.ty))

        }
      }
    w.wl("),")
    w.wl(recordAsMethodName + "___delete" + ");")
    }
    w.wl("return _aux;")
  }

  def writeCFile(fileName: String, ident: String, origin: String, includes: Iterable[String], create: Boolean, f: IndentWriter => Unit) {
    if (create) createFile(spec.cWrapperOutFolder.get, fileName + ".cpp", (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      // always include support library
      w.wl("#include <iostream> // for debugging")
      w.wl("#include <cassert>")
      w.wl("#include \"wrapper_marshal.hpp\"")
      w.wl("#include \"" + ident + ".hpp\"")  // include abstract cpp base class header
      // Includes for Djinni Wrappers
      if (includes.nonEmpty) {
        w.wl
        includes.foreach(i => {w.wl(marshal.removeComment(i))})
      }
      w.wl
    })
    appendToFile(spec.cWrapperOutFolder.get, fileName + ".cpp", (w: IndentWriter) => {
      f(w)
    })
  }

  def writeAsExternC(w: IndentWriter, f: IndentWriter => Unit ): Unit = {
    w.wl("#ifdef __cplusplus")
    w.wl("extern \"C\" {")
    w.wl("#endif")
    w.wl
    f(w)
    w.wl
    w.wl("#ifdef __cplusplus")
    w.wl("}")
    w.wl("#endif")
  }

  def writeCHeader(fileName: String, origin: String, cppClass: String, includes: Iterable[String], create: Boolean, f: IndentWriter => Unit): Unit = {
    if (create) createFile(spec.cWrapperOutFolder.get, fileName + ".h",  (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      w.wl("#pragma once" + marshal.pythonCdefIgnore )
      // Includes for referenced types
      w.wl("#include <stdbool.h> " + marshal.pythonCdefIgnore )  // seeing if 2 handles are equal needs this
      if (includes.nonEmpty) {
        w.wl
        includes.foreach(w.wl)
      }
    })
    appendToFile(spec.cWrapperOutFolder.get, fileName + ".h", (w: IndentWriter) => {
      w.wl
      f(w)
    })
  }

  def writeCppHeader(fileName: String, ident: String, origin: String, cppClass: String, create: Boolean, f: IndentWriter => Unit): Unit = {
    // hpp file contains definition of DjinniStruct wrapper over cpp shared_ptr
    // meant to facillitate interuse of interfaces across files
    if (create) createFile(spec.cWrapperOutFolder.get, fileName + ".hpp", (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      w.wl("#pragma once")
      w.wl
      w.wl("#include <atomic>")
      w.wl("#include " + spec.cppOptionalHeader)
      w.wl("#include \"" + ident + ".hpp\"")  // include abstract cpp base class header
      writeAsExternC(w, w => {
        w.wl("#include \"" + fileName + ".h\"") // include own header
      })
    })
    appendToFile(spec.cWrapperOutFolder.get, fileName + ".hpp", (w: IndentWriter) => {
      f(w)
    })
  }

  def writeMethodSignatures(methods: Seq[Interface.Method], forHeader: Boolean, forCpp: Boolean,
                            cWrapper: String, cMethodWrapper: String, w: IndentWriter): Unit = {
    val structCWrapper = if(cWrapper != "")  "struct " + cWrapper else ""
    val self = if(structCWrapper != "") structCWrapper + " * " + dw else ""
    // C Wrapper method signatures for calling into cpp
    for (m <- methods) {
      val ret = if(forCpp) cppMarshal.fqReturnType(m.ret) else marshal.cReturnType(m.ret, forHeader)

      val params = if(forCpp) getCppDefArgs(m, self) else getDefArgs(m, self, true)
      w.wl(ret + " " + cMethodWrapper + idCpp.method(m.ident.name) + params + ";")
      w.wl
    }
  }

  def writeGetWrappedObj(ident: Ident, w: IndentWriter, className: String, ext: Ext): Unit = {
    w.wl("std::shared_ptr<" + withCppNs(className) + "> " + marshal.wrappedName(className) + "::get" + p("djinni::WrapperRef" + t(marshal.wrappedName(className)) + " dw" ) + " {").nested {
      w.wl("if (dw) {").nested {
        w.wl("return dw->wrapped_obj;")
      }
      w.wl("}")
      w.wl("return nullptr;")
    }
    w.wl("}")
    w.wl


    if (ext.py) {
      w.wl("void " + idCpp.method(ident.name) + "___delete" + p(marshal.djinniObjectHandle + " * dh") + " {").nested {
        // Only if we implement the interface in Python, will there be a delete from python callback
        w.wl(marshal.pyCallback(idCpp.method(ident.name) + "___delete") + p("dh") + ";")
      }
      w.wl("}")
    }

    w.wl("void " + idCpp.method(ident.name) + "___wrapper_add_ref" + p(marshal.wrappedName(className) + " * dh") + " {").nested {
      w.wl("dh->ref_count.fetch_add(1);")
    }
    w.wl("}")

    w.wl("void " + idCpp.method(ident.name) + "___wrapper_dec_ref" + p(marshal.wrappedName(className) + " * dh") + " {").nested {
        w.wl("const size_t ref = dh->ref_count.fetch_sub(1);")
        w.wl("if (ref == 1) {// value before sub is returned").nested {
          w.wl("delete dh;")
        }
        w.wl("}")
    }
    w.wl("}")

    w.wl("djinni::Handle" + t(marshal.wrappedName(className)) + " " + marshal.wrappedName(className) +  "::wrap" + p("std::shared_ptr<" + withCppNs(className) + "> obj") + " {").nested {
      w.wl("if (obj)").nested {
        w.wl("return " + "djinni::Handle" + t(marshal.wrappedName(className)) + p("new "  + marshal.wrappedName(className)  + "{ std::move(obj) }, " +
          idCpp.method(ident.name) + "___wrapper_dec_ref") + ";")
      }
      w.wl("return nullptr;")
    }
    w.wl("}")
    w.wl
  }

  def writeDjinniWrapper(ident: Ident, w: IndentWriter, className: String): Unit = {
    w.wl("struct " + marshal.wrappedName(className) + " final {"  ).nested {
      w.wl(marshal.wrappedName(className) + p("std::shared_ptr" + t(withCppNs(className)) + "wo") + ": wrapped_obj" + p("wo") + " {};")
      w.wl
      w.wl("static std::shared_ptr" + t(withCppNs(className)) + " " + "get" + p("djinni::Handle" + t(marshal.wrappedName(className)) + " dw" ) + ";")
      w.wl("static djinni::Handle" + t(marshal.wrappedName(className)) + " wrap" +  p("std::shared_ptr" + t(withCppNs(className)) + " obj") + ";")
      w.wl
      w.wl("const " + "std::shared_ptr" + t(withCppNs(className)) + " " + "wrapped_obj;" )
      w.wl("std::atomic<size_t> ref_count {1};")
    }
    w.wl("};")
  }

  def writeDjinniUnwrapperSignature(cMethodWrapper:String, cWrapper: String, ext: Ext, w: IndentWriter): Unit = {
    if (ext.py) {
      w.wl("void " + cMethodWrapper + "___delete(" + "struct " + marshal.djinniObjectHandle + " * " + dw + ");")
    }
    w.wl("void " + idCpp.method(cMethodWrapper) + "___wrapper_dec_ref" + p("struct " + cWrapper + " * dh") + ";")
    w.wl("void " + idCpp.method(cMethodWrapper) + "___wrapper_add_ref" + p("struct " + cWrapper + " * dh") + ";")
  }

  def checkNonOptionals(m: Interface.Method, w: IndentWriter): Unit = {
    m.params.map(arg => if (arg.ty.resolved.base != MOptional) w.wl("assert " + p(arg.ident.name + " != nullptr") + ";"))
  }

  def writeMethodToCpp(m: Interface.Method, fctPrefix: String, cWrapper: String, cMethodWrapper: String, w: IndentWriter): Unit = {
    val ret = marshal.cReturnType(m.ret, false)
    val args = m.params.map (p => marshal.convertTo((if (marshal.needsRAII(p.ty.resolved)) "_" else "") + p.ident.name, p.ty)) // x
    val fctCall = fctPrefix + idCpp.method(m.ident.name) + args.mkString("(", ", ", ")")
    val returnStmt = if (m.ret.isEmpty) fctCall + ";"
    else "return " + marshal.convertFrom(fctCall , m.ret.get) + (if (marshal.needsRAII(m.ret.get)) ".release()" else "") + ";"

    val params = getDefArgs(m, cWrapper + " * " + dw, false)
    w.wl(ret + " " + cMethodWrapper + "_" + idCpp.method(m.ident.name) + params + " {").nested {
      // take ownership of arguments memory when arguments come from Python
      m.params.foreach(p => declareUniquePointer("_" + p.ident.name, p.ident.name, p.ty.resolved, w))
      w.wl("try {").nested {
        w.wl(returnStmt)
      }
      val onExceptionReturn = if (m.ret.isDefined) "0" else ""
      w.wl("} CW_TRANSLATE_EXCEPTIONS_RETURN" + p(onExceptionReturn) + ";")
    }
    w.wl("}")
  }

  def writeMethodFromCpp(ident: Ident, m: Interface.Method, cppClass: String, handle: String, w: IndentWriter): Unit = {
    val ret = cppMarshal.fqReturnType(m.ret)
    val args = m.params.map(p => cppMarshal.fqParamType(p.ty) + " " +  idCpp.local(p.ident.name)).mkString("(", ", ", ")")
    val params = Seq(handle) ++  m.params.map (p =>
      if (marshal.needsRAII(p.ty.resolved)) "_" + idPython.local(p.ident.name) + ".release()"
      else marshal.convertFrom(idPython.local(p.ident.name), p.ty))

    w.wl(ret + " " + cppClass + "::" + idCpp.method(m.ident.name) + args + " {").nested {
      // take ownership of arguments memory when arguments come from Python
      m.params.foreach(p => if (marshal.needsRAII(p.ty)) w.wl("auto _" + p.ident.name + " = " + marshal.convertFrom(p.ident.name, p.ty) + ";"))

      val method_call = marshal.pyCallback(idCpp.method(ident.name) + "_" + idCpp.method(m.ident.name)) + params.mkString("(", ", ", ")")
      if (m.ret.isDefined) {
        val method_call = marshal.pyCallback(idCpp.method(ident.name) + "_" + idCpp.method(m.ident.name)) + params.mkString("(", ", ", ")")
        w.wl("auto _ret = " +  marshal.convertTo(raiiForElement(method_call, m.ret.get.resolved), m.ret.get) + ";")
        w.wl("djinni::cw_throw_if_pending();")
        w.wl("return _ret;")
      }
      else {
        w.wl(method_call  + ";")
        w.wl("djinni::cw_throw_if_pending();")
      }
    }
    w.wl("}")
  }

  def writeGetSetEqHandle(djinniWrapper: String, cMethodName: String, cPythonProxy: String, w: IndentWriter): Unit = {

    // Function to return handle around PythonProxy holding a python object
    w.wl(djinniWrapper + " * " + " make_proxy_object_from_handle_" + cMethodName + p(
                            marshal.djinniObjectHandle + " * " + "c_ptr") + " {").nested {
      w.wl("return new " + djinniWrapper + "{" + "std::make_shared<" + cPythonProxy + ">(c_ptr)};" )
    }
    w.wl("}")
    w.wl
    // Function to return python object held via handle by Python
    w.wl(marshal.djinniObjectHandle + " * " + "get_handle_from_proxy_object_" + cMethodName + p(
                          djinniWrapper + " * dw") + " {").nested {
      w.wl(cPythonProxy + " * " + "cast_ptr = dynamic_cast" + t(cPythonProxy + " * ") + p("dw->wrapped_obj.get()") + ";")
      w.wl("if (!cast_ptr) { return nullptr; }")
      w.wl("else return cast_ptr->get_m_py_obj_handle();")
    }
    w.wl("}")
    w.wl
    w.wl("bool " + "equal_handles_" + cMethodName + p(djinniWrapper + " * dw1, " +
                                                      djinniWrapper + " * dw2") + " {").nested {
      w.wl("return dw1->wrapped_obj == dw2->wrapped_obj;")
    }
    w.wl("}")
    w.wl
  }

  def writeGetSetEqHandleSignatures(djinniWrapper: String, cMethodName: String, w: IndentWriter) = {
    w.wl("struct " + djinniWrapper + " * " + " make_proxy_object_from_handle_" + cMethodName + p(
      "struct " + marshal.djinniObjectHandle + " * " + "c_ptr") + ";")
    w.wl("struct " + marshal.djinniObjectHandle + " * " + "get_handle_from_proxy_object_" + cMethodName + p("struct " + djinniWrapper + " * dw") + ";")
    w.wl("bool " + "equal_handles_" + cMethodName + p("struct " + djinniWrapper + " * dw1, " +
                                                      "struct " + djinniWrapper + " * dw2") + ";")
    w.wl
  }

  def writeUnimplementableInterface(ident: Ident, origin: String, cppClass: String, refs: CRefs, i: Interface): Unit = {
    val cClassWrapper = "DjinniWrapper" + cppClass

    refs.hpp.add("#include " + q(marshal.cw + ident.name + ".hpp") + marshal.pythonCdefIgnore) // make sure own header included (for static)

    // C header for cdef
    writeCHeader(marshal.cw + ident.name, origin, cppClass, refs.h, true, w=> {
      // Djinni Wrapper over cpp class
      w.wl( "struct " + marshal.wrappedName(cppClass) + ";"  )
      writeDjinniUnwrapperSignature(idCpp.method(ident.name), cClassWrapper, i.ext, w)
    })

    // Cpp header for Djinni Wrapper signatures
    writeCppHeader(marshal.cw + ident.name, ident.name, origin, cppClass, true, w=>{
      writeDjinniWrapper(ident, w, cppClass)
    })

    writeCFile(marshal.cw + ident.name, ident, origin, refs.hpp, true, w => {
      writeGetWrappedObj(ident, w, cppClass, i.ext)
    })
  }

  def writeCToCpp(ident: Ident, origin: String, cppClass: String, refs: CRefs, i: Interface, create: Boolean): Unit = {
    val cClassWrapper = "DjinniWrapper" + cppClass
    val cMethodWrapper = idCpp.method(marshal.cw + ident.name)

    refs.hpp.add("#include " + q(marshal.cw + ident.name + ".hpp") + marshal.pythonCdefIgnore) // make sure own header included (for static)

    // C header for cdef
    writeCHeader(marshal.cw + ident.name, origin, cppClass, refs.h, create, w=> {
      if (create) {
        // Djinni Wrapper over cpp class
        w.wl( "struct " + marshal.wrappedName(cppClass) + ";"  )
        writeDjinniUnwrapperSignature(idCpp.method(ident.name), cClassWrapper, i.ext, w)
      }
      w.wl
      writeMethodSignatures(i.methods, true, false, cClassWrapper, cMethodWrapper + "_", w)
    })

    // Cpp header for Djinni Wrapper signatures
    writeCppHeader(marshal.cw + ident.name, ident.name, origin, cppClass, create, w=>{
      if (create) { // if not create then our writeCFromCpp function already created this wrapper
        writeDjinniWrapper(ident, w, cppClass)
      }
    })

    writeCFile(marshal.cw + ident.name, ident, origin, refs.hpp, create, w => {
      if (create) {
        writeGetWrappedObj(ident, w, cppClass, i.ext)
      }
      // Asbtract Class Implementation
      // C Wrapper class methods calling into cpp
      var skipFirst = SkipFirst()
      for (m <- i.methods if ! m.static) {
        skipFirst { w.wl }
        val fctPrefix = dw + "->" + "wrapped_obj" + "->"
        writeMethodToCpp(m, fctPrefix, cClassWrapper, cMethodWrapper, w)

      }

      skipFirst = SkipFirst()
      for (m <- i.methods if m.static) {
        skipFirst { w.wl }
        val fctPrefix = withCppNs(cppClass) + "::"
        writeMethodToCpp(m, fctPrefix, cClassWrapper, cMethodWrapper, w)
      }
    })
  }

  // could eventually change name and use marshal.pyHelper or something similar instead of marshal.cw
  def writeCFromCpp(ident: Ident, origin: String, cppClass: String, refs: CRefs, i: Interface, create: Boolean): Unit = {
    val cMethodWrapper = idCpp.method(marshal.cw + ident.name)
    val cClassWrapper = "DjinniWrapper" + idCpp.ty(ident.name)
    val className =  idCpp.ty(ident.name)
    val classAsMethodName = idCpp.method(ident.name)
    val cPythonProxy = idCpp.ty(ident.name) + "PythonProxy"
    val handle = "m_py_obj_handle"

    refs.hpp.add("#include " + q(marshal.cw + ident.name + ".hpp")) // make sure own header included

    writeCHeader(marshal.cw + ident.name, origin, cppClass, refs.h, create, w => {
      w.wl( "struct " + marshal.wrappedName(cppClass) + ";"  )
      writeDjinniUnwrapperSignature(idCpp.method(ident.name), cClassWrapper, i.ext, w)
      w.wl
      writeGetSetEqHandleSignatures(cClassWrapper, cMethodWrapper, w)
      // Functions used to add callbacks
      for (m <- i.methods) {
        val ret = marshal.cReturnType(m.ret, true)
        val cArgs = getCArgTypes(m, "struct " + marshal.djinniObjectHandle + " * ", true)
        val methodSignature = ret + "(* ptr)" + cArgs
        w.wl("void " + classAsMethodName +  "_add_callback_" + idCpp.method(m.ident.name) + p(methodSignature) + ";")
        w.wl
      }
      w.wl("void " + classAsMethodName + "_add_callback_" + "__delete" + p(
                      "void(* ptr)" + p("struct " + marshal.djinniObjectHandle + " * ")) + ";")
    })

    writeCppHeader(marshal.cw + ident.name, ident.name, origin, cppClass, create, w => {
      writeDjinniWrapper(ident, w, cppClass) // not per se needed but helpful for allowing general dynamic casts
      w.wl
      // PyProxy Implementation
      w.wl("class " + cPythonProxy + " final : public " + withCppNs(className) + " {" ).nested {
        w.wl("public:").nested {
          // Constructor
          w.wl("explicit " + cPythonProxy + p(marshal.djinniObjectHandle + " * " + "c_ptr") + ";")
          // Destructor
          w.wl("~" + cPythonProxy + "();")
          // Handle Getter
          w.wl(marshal.djinniObjectHandle + " * " + "get_m_py_obj_handle();")
          w.wl
          // Methods
          writeMethodSignatures(i.methods, false, true, "", "", w)
        }
        w.wl("private:").nested {
          w.wl(marshal.djinniObjectHandle + " * " + handle + ";")
        }
      }
      w.wl("};")
    })

    writeCFile(marshal.cw + ident.name, ident, origin, refs.hpp, create, w => {
      // Declare global handles to c-python function
      declareGlobalFunctionPointers(ident, cClassWrapper, i.methods, w)
      w.wl
      writeGetWrappedObj(ident, w, cppClass, i.ext)
      writeGetSetEqHandle(cClassWrapper, cMethodWrapper, cPythonProxy, w)
      // Getter for python object handle
      w.wl(marshal.djinniObjectHandle + " * " + cPythonProxy + "::get_m_py_obj_handle() {").nested {
        w.wl("return m_py_obj_handle;")
      }
      w.wl("}")
      w.wl
      // Constructor from void*
      w.wl(cPythonProxy + "::" + cPythonProxy + p(marshal.djinniObjectHandle + " * " + "c_ptr") + " : " + handle + p("c_ptr") + " {}")
      w.wl
      // Destructor
      w.wl(cPythonProxy + "::~" + cPythonProxy + "() {").nested {
        w.wl(marshal.pyCallback(idCpp.method(ident.name) + "___delete") + p(handle) + ";")
      }
      w.wl("}")
      w.wl
      // Function used to add callbacks
      for (m <- i.methods) {
        val ret = marshal.cReturnType(m.ret, false)
        val cArgs = getCArgTypes(m, marshal.djinniObjectHandle + " * ", false)
        val methodSignature = ret + "(* ptr)" + cArgs
        w.wl("void " + classAsMethodName + "_add_callback_" + idCpp.method(m.ident.name) + p(methodSignature) + " {").nested {
          w.wl(marshal.pyCallback(idCpp.method(ident.name) + "_" + idCpp.method(m.ident.name)) + " = ptr;")
        }
        w.wl("}")
        w.wl
      }
      // Callback for delete python object handle
      w.wl("void " + classAsMethodName + "_add_callback_" + "__delete" + p(
          "void(* ptr)"+ p(marshal.djinniObjectHandle + " * ")) + " {").nested {
        w.wl(marshal.pyCallback(idCpp.method(ident.name) + "___delete") + " = ptr;")
      }
      w.wl("}")
      w.wl

      val skipFirst = SkipFirst()
      for (m <- i.methods) {
        skipFirst { w.wl }
        // TODO: raise exception or complain if method is static?
        writeMethodFromCpp(ident, m, cPythonProxy, handle, w)
      }
    })
  }

  def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface): Unit ={
    val cppClass = idCpp.ty(ident.name)

    System.out.println("Generting C interface...", origin)

    val refs = new CRefs(ident, origin)
    // first collect all references, don't write
    i.methods.map(m => {
      m.params.foreach(p => refs.collect(p.ty, true))
      m.ret.foreach(t => refs.collect(t, true))
    })
    i.consts.foreach(c => {
      refs.collect(c.ty, true)
    })
    // go through references and write
    i.methods.map(m => {
      m.params.foreach(p => refs.collect(p.ty, false))
      m.ret.foreach(t => refs.collect(t, false))
    })
    i.consts.foreach(c => {
      refs.collect(c.ty, false)
    })

    if (i.ext.cpp && i.ext.py) {
      writeCFromCpp(ident, origin, cppClass, refs, i, true)
      writeCToCpp(ident, origin, cppClass, refs, i, false)
    } else if (i.ext.cpp) {
      writeCToCpp(ident, origin, cppClass, refs, i, true)
    } else if (i.ext.py) {
      writeCFromCpp(ident, origin, cppClass, refs, i, true)
    } else {
      // Write out only the pieces of code needed to compile usage sites, without the code to implement in either
      // language.  This ensures the code can compile for unused methods which reference types not used in Python.
      writeUnimplementableInterface(ident, origin, cppClass, refs, i)
    }
  }

  def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum): Unit ={
    val refs = new CRefs(ident, origin)
    writeCHeader(marshal.dh + ident.name, origin, "", refs.h, true, w => {

    })

    writeCppHeader(marshal.dh + ident.name, ident.name, origin, "", true, w => {
      w.wl("int32_t int32_from_enum_" + idCpp.method(ident.name) + p(spec.cppOptionalTemplate + t(withCppNs(idCpp.enumType(ident.name))) + " e") + ";")
      w.wl("int32_t int32_from_enum_" + idCpp.method(ident.name) + p(withCppNs(idCpp.enumType(ident.name)) + " e") + ";")
      w.wl(spec.cppOptionalTemplate + t(withCppNs(idCpp.enumType(ident.name))) +
        " get_boxed_enum_" + idCpp.method(ident.name) + "_from_int32" + p("int32_t e") + ";")
    })

    writeCFile(marshal.dh + ident.name, ident, origin, refs.hpp, true, w => {
      // Get int32 from boxed enum
      w.wl("int32_t int32_from_enum_" + idCpp.method(ident.name) + p(spec.cppOptionalTemplate + t(withCppNs(idCpp.enumType(ident.name))) + " e") + " {").nested {
        w.wl("if (e) {").nested {
          w.wl("return static_cast<int32_t>" + p("* e") + ";")
        }
        w.wl("}")
        w.wl("return -1; // -1 to signal null boxed enum")
      }
      w.wl("}")
      w.wl
      // Get int32 from enum
      w.wl("int32_t int32_from_enum_" + idCpp.method(ident.name) + p(withCppNs(idCpp.enumType(ident.name)) + " e") + " {").nested {
          w.wl("return static_cast<int32_t>" + p("e") + ";")
      }
      w.wl("}")
      // Get boxed enum from int32
      w.wl(spec.cppOptionalTemplate + t(withCppNs(idCpp.enumType(ident.name))) +
            " get_boxed_enum_" + idCpp.method(ident.name) + "_from_int32" + p("int32_t e") + " {").nested {
        w.wl("if (e == -1) { // to signal null enum").nested {
          w.wl("return std::experimental::nullopt;")
        }
        w.wl("}")
        w.wl("return " + spec.cppOptionalTemplate + t(withCppNs(idCpp.enumType(ident.name))) +
              p("static_cast" + t(withCppNs(idCpp.enumType(ident.name))) + p("e")) + ";")
      }
      w.wl("}")
    })
  }

  def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record): Unit = {
    val refs = new CRefs(ident, origin)
    r.fields.map(f => refs.collect(f.ty, true))
    refs.hpp.add("#include " + q(marshal.dh + ident.name + ".hpp") + marshal.pythonCdefIgnore)
    r.fields.map(f => refs.collect(f.ty, false))

    val recordAsMethodName = idCpp.method(ident.name)
    val recordName = "Djinni" + idCpp.ty(ident.name)
    val recordHandle = "DjinniRecordHandle"
    val optRecordHandle = "DjinniOptionalRecordHandle"
    val cppRecordName = idCpp.ty(ident.name)
    val fqCppRecordName = withCppNs(cppRecordName)

    writeCHeader(marshal.dh + ident.name, origin, "", refs.h, true, w => {
      // record definition
      w.wl("struct " + recordName + ";")
      w.wl("void " + recordAsMethodName + "___delete" + p("struct " + recordHandle + " * ") + ";")
      w.wl("void " + "optional_" + recordAsMethodName + "___delete" + p("struct " + optRecordHandle + " * ") + ";")
      // global signatures for callbacks for getting record fields
      declareGlobalSignaturesRecordFieldGetters(ident, r, "", 0, w)
    })

    writeCppHeader(marshal.dh + ident.name, ident.name, origin, "", true, w => {
        w.wl("struct " + recordName + " {").nested {
        // from cpp
        w.wl("static " + "djinni::Handle" + t(recordHandle)  + " fromCpp" + p("const " + fqCppRecordName + "&" + " dr") + ";")
        // to cpp
        w.wl("static " + fqCppRecordName + " " + "toCpp" + p("djinni::Handle" + t(recordHandle) + " dh") + ";")

        w.wl("static " + "djinni::Handle" + t(optRecordHandle)  + " fromCpp" + p( spec.cppOptionalTemplate + t(fqCppRecordName) + " dc") + ";")
        w.wl("static " + spec.cppOptionalTemplate + t(fqCppRecordName) + " toCpp" + p("djinni::Handle" + t(optRecordHandle) + " dh") + ";" )

      }
      w.wl("};")
    })

    writeCFile(marshal.dh + ident.name, ident, origin, refs.hpp, true, w => {
      // Callback for delete python object handle
      w.wl("static void(*" + marshal.pyCallback(recordAsMethodName + "___delete" ) + ")" + p(recordHandle + " * ") + ";")

      w.wl("void " + recordAsMethodName + "_add_callback_" + "__delete" + p(
        "void(* ptr)"+ p(recordHandle + " * ")) + " {").nested {
        w.wl(marshal.pyCallback(idCpp.method(ident.name) + "___delete") + " = ptr;")
      }
      w.wl("}")
      w.wl
      // Function to call delete of record from Python
      var ret = "void"
      var cArgs = Seq("DjinniRecordHandle * drh").mkString("(", ", ", ")")
      w.wl("void "  + idCpp.method(ident.name + "___delete") + cArgs + " {" ).nested {
        w.wl(marshal.pyCallback(idCpp.method(ident.name) + "___delete") + p("drh") + ";")
      }
      w.wl("}")
      // Function to call delete of Optional record from Python
      ret = "void"
      cArgs = Seq("DjinniOptionalRecordHandle * drh").mkString("(", ", ", ")")
      w.wl("void "  + idCpp.method("optional_" + ident.name + "___delete") + cArgs + " {" ).nested {
        w.wl(marshal.pyCallback(idCpp.method(ident.name) + "___delete") + p("(DjinniRecordHandle *) drh") + "; // can't static cast, find better way")
      }
      w.wl("}")

      // global callback pointers for getting record fields
      declareGlobalRecordFieldGetters(ident, r, "", 0, w)
      // from cpp
      w.wl("djinni::Handle" + t(recordHandle) + " " + recordName + "::fromCpp" + p("const " + fqCppRecordName + "&" + " dr") + " {").nested {
        writeRecordFromCpp(ident, r, "", 0, w)
      }
      w.wl("}")
      w.wl
      // to cpp
      w.wl(fqCppRecordName + " " + recordName + "::toCpp" + p("djinni::Handle" + t(recordHandle) + " dh") + " {").nested {
        writeRecordToCpp(ident, r, "", 0, w)
      }
      w.wl("}")
      w.wl

      writeOptionalContainerFromCpp(optRecordHandle, recordHandle, fqCppRecordName, recordName,
        "optional_" + recordAsMethodName + "___delete", w)
      writeOptionalContainerToCpp(optRecordHandle, fqCppRecordName, recordName, "DjinniRecordHandle *",
        recordAsMethodName + "___delete", w)
    })
  }
}
