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

import djinni.ast.Record.DerivingType

import scala.collection.mutable
import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter


class PythonGenerator(spec: Spec) extends Generator(spec) {
  val lib = "lib" // internal library name (from PyCFFIlib_cffi import lib)
  val marshal = new PythonMarshal(spec)
  val cMarshal = new CWrapperMarshal(spec)

  def getRecordArguments(r: Record, self: String) = {
    if (self != "") Seq(self) ++ r.fields.map( f => idPython.local(f.ident.name))
    else r.fields.map( f => idPython.local(f.ident.name))
  }

  def getRecordTypes(r: Record) = {
    r.fields.map( f => cMarshal.cParamType(f.ty, true))
  }

  // Override since Python doesn't share the C-style comment syntax.
  override def writeDoc(w: IndentWriter, doc: Doc) {
    doc.lines.foreach(l => w.wl(s"#$l"))
  }

  // Use docstrings rather than comments when available.
  def writeDocString(w: IndentWriter, doc: Doc, docLists: Seq[IndentWriter => Unit] = Seq()): Boolean = {
    if (doc.lines.isEmpty && docLists.isEmpty) {
      return false
    }
    if (doc.lines.length == 1 && docLists.isEmpty) {
      w.wl(s"""\"\"\"${doc.lines.head} \"\"\"""")
    } else {
      w.wl("\"\"\"")
      doc.lines.foreach (l => w.wl(s"$l"))
      docLists.foreach (docList => docList(w))
      w.wl("\"\"\"")
    }
    return true
  }

  def writeDocList(w: IndentWriter, name: String, docItems: IndentWriter => Unit) {
    w.wl(name)
    w.nested {
      docItems(w)
    }
  }

  def writeDocListItem(w: IndentWriter, name: String, lines: Seq[String]): Unit = {
    if (lines.isEmpty) {
      w.wl(s"${name}")
    } else {
      w.wl(s"${name}: ${lines.head.trim}")
      w.nested {
        lines.tail.foreach (l => {
          w.wl(l)
        })
      }
    }
  }

  def writeDocConstantsList(w: IndentWriter, consts: Seq[Const]): Unit = {
    writeDocList(w, "Constants", { w => consts.foreach (c => {
      writeDocListItem(w, idPython.const(c.ident), c.doc.lines)
    })})
  }

  def writeDocFieldsList(w: IndentWriter, fields: Seq[Field]): Unit = {
    writeDocList(w, "Fields", { w => fields.foreach (f => {
      writeDocListItem(w, idPython.field(f.ident), f.doc.lines)
    })})
  }

  def writeDocEnumOptionsList(w: IndentWriter, options: Seq[Enum.Option]): Unit = {
    writeDocList(w, "Members", { w => options.foreach (o => {
      writeDocListItem(w, idPython.enum(o.ident), o.doc.lines)
    })})
  }

  def writeGetterCallback(methodName: String, ret: String, cArgs: String, defArgs: String, w: IndentWriter, f: IndentWriter => Unit ): Unit ={
    w.wl("@ffi.callback" + "(\"" + ret + cArgs + "\")")
    w.wl("def " + methodName + defArgs + ":").nested {
      f(w)
    }
    w.wl
  }

  def getContainerElTyRef(tm: MExpr, index: Int, ident: Ident): TypeRef = {
    val elTyRef = TypeRef(TypeExpr(ident, Seq()))
    elTyRef.resolved = tm.args(index)
    return elTyRef
  }

  def getElTyRef(tm: MExpr, ident: Ident): TypeRef = {
    val elTyRef = TypeRef(TypeExpr(ident, Seq()))
    elTyRef.resolved = tm
    return elTyRef
  }

  def writeReleaseCallback(className: String, w: IndentWriter): Unit = {
    // Function to release handler to python object
    w.wl("@ffi.callback(\"void(" + "struct " + cMarshal.djinniObjectHandle + " * " + ")\")")
    w.wl("def __delete" + p("c_ptr") + ":").nested {
        w.wl("assert c_ptr in " + className + ".c_data_set")
        w.wl(className  + ".c_data_set.remove(c_ptr)")
    }
    w.wl
  }

  def writeGettersCallbacks(tm: MExpr, ident: Ident, name: String, className: String, w: IndentWriter,
                            self: String, keyToPy: String, keyAssert: String,
                            getName: String, getRetTyRef: TypeRef, getRetType: String, getType: String, getCall: String, elIndex: String, elTyRef: TypeRef,
                            getSizeCall: String,
                            createName: String,
                            addElemCall: String, toAddDefArgs: String, toAddCArgs: String,
                            extraAddCallback: String,
                            extraCallBackImpl: IndentWriter => Unit): Unit = {

    // GET ELEMENT/ GET VALUE
    var ret = getRetType
    var defArgs = Seq("cself, "+ elIndex).mkString("(", ", ", ")")
    var cArgs = Seq("struct DjinniObjectHandle *", getType).mkString("(", ", ", ")")
    if (getName != "") {
      writeGetterCallback(getName, ret, cArgs, defArgs, w, w => {
        if( keyToPy != "") w.wl(keyToPy)
        if( keyAssert != "") w.wl(keyAssert)
        writeReturnFromCallback(self, getRetTyRef, getCall, w)
      })
    }

    // GET SIZE
    ret = "size_t"
    defArgs = Seq("cself").mkString("(", ", ", ")")
    cArgs = Seq("struct DjinniObjectHandle *").mkString("(", ", ", ")")
    writeGetterCallback("__get_size", ret, cArgs, defArgs, w, w => {
      w.wl("return " + getSizeCall)
    })

    // CREATE
    ret = "struct DjinniObjectHandle *"
    defArgs = Seq("").mkString("(", ", ", ")")
    cArgs = Seq("").mkString("(", ", ", ")")
    writeGetterCallback("__python_create", ret, cArgs, defArgs, w, w => {
      w.wl("c_ptr = ffi.new_handle" + p(createName))
      w.wl(className + ".c_data_set.add(c_ptr)")
      w.wl("return ffi.cast(\"struct DjinniObjectHandle *\", c_ptr)")
    })

    // ADD TO
    ret = "void"
    defArgs = Seq("cself, " + toAddDefArgs).mkString("(", ", ", ")")
    cArgs = Seq("struct DjinniObjectHandle *", toAddCArgs).mkString("(", ", ", ")")
    writeGetterCallback("__python_add", ret, cArgs, defArgs, w, w => {
      w.wl(addElemCall)
    })

    // RELEASE
    writeReleaseCallback(className, w)
    extraCallBackImpl(w)

    // Function to add callback on cpp side
    w.wl("@staticmethod")
    w.wl("def _add_callbacks():").nested {
      if (getName != "") {
        w.wl("lib." + name + "_add_callback" + getName + p(className + "." + getName))
      }
      // release method callback added
      w.wl("lib." + name + "_add_callback_" + "__delete" + p(className + "." + "__delete"))
      w.wl("lib." + name + "_add_callback" + "__get_size" + p(className + "." + "__get_size"))
      w.wl("lib." + name + "_add_callback" + "__python_create" + p(className + "." + "__python_create"))
      w.wl("lib." + name + "_add_callback" + "__python_add" + p(className + "." + "__python_add"))
      if (extraAddCallback != "") w.wl(extraAddCallback)
    }
    w.wl
  }

  def writeChecksForContainer(tm: MExpr, exclude: String, w: IndentWriter): Unit = {
    val className = marshal.getClassHoldingCDataSet(tm)
    if (className != "" && className != exclude) {
      w.wl(marshal.getClassHoldingCDataSet(tm) + ".check_c_data_set_empty()")
    }

    tm.base match {
      case MList => {
        writeChecksForContainer(tm.args(0), exclude, w)
      }
      case MSet | MMap => {
        writeChecksForContainer(tm.args(0), exclude, w)
        if (tm.base == MMap) writeChecksForContainer(tm.args(1), exclude, w)
      }
      case _ =>
    }
  }

  def generateContainer(tm: MExpr, isOpt: Boolean, fileName: String, classAsMethodName: String, ident: Ident, origin: String, python: mutable.TreeSet[String]) = {
//    System.out.println("PYTHON: generating container ", fileName)
    val helperClass = idPython.className(fileName) + "Helper"
    val proxyName = idPython.className(fileName) + "Proxy"
    val next = if(tm.base == MList) ""
    else {
      if (isOpt) "next(CPyObjectProxy.toPyIterOpt(cself))"
      else "next(CPyObjectProxy.toPyIter(cself))"
    }

    writePythonFile(marshal.dh + fileName, origin, python, true, w => {
      w.wl("class " + helperClass + ":").nested {
        w.wl("c_data_set = MultiSet()")
        w.wl
        w.wl("@staticmethod")
        w.wl("def check_c_data_set_empty():").nested {
          w.wl("assert len" + p(helperClass + ".c_data_set") + " == 0")
          writeChecksForContainer(tm, helperClass, w)
        }
        w.wl
        tm.base match {
          case MList =>
            val elTyref = getContainerElTyRef(tm, 0, ident)
            val elTy = cMarshal.cReturnType(Some(elTyref), true)

            writeGettersCallbacks(tm, ident, fileName, helperClass, w,
              "CPyObject.toPy(cself)", "", "",

              "__get_elem", elTyref, elTy, "size_t",
                  "CPyObject.toPy(" + "None" + ", " + "cself)[index]", "index", elTyref,
            "len(CPyObject.toPy(" + "None" + ", " +  "cself))",

            "list()",
            "CPyObject.toPy(" + "None" + ", " + "cself).append(" + marshal.convertTo("el", elTyref) + ")",
            "el", elTy, "", w => {}
          )
          case MMap =>
            val keyTyRef = getContainerElTyRef(tm, 0, ident)
            val keyTy =  cMarshal.cReturnType(Some(keyTyRef), true)
            val valTyref = getContainerElTyRef(tm, 1, ident)
            val valTy = cMarshal.cReturnType(Some(valTyref), true)
            val keyToPy = "pyKey = " + marshal.convertToRelease("key", keyTyRef)
            val keyAssert = "assert pyKey is not None"

            writeGettersCallbacks(tm, ident, fileName, helperClass, w,
            "CPyObjectProxy.toPyObj(cself)", keyToPy, (if (keyTyRef.resolved.base != MOptional) keyAssert else ""),
              "__get_value", valTyref, valTy, keyTy, "CPyObjectProxy.toPyObj(" + "None" + ", " + "cself)[pyKey]", "key", keyTyRef,
            "len(CPyObjectProxy.toPyObj(" + "None" + ", " + "cself))",
            proxyName + p("dict()"),
            "CPyObjectProxy.toPyObj(" + "None" + ", " + "cself)[" + marshal.convertTo("key", keyTyRef) + "] = " + marshal.convertTo("value", valTyref), "key, value", keyTy + ", "+ valTy,
              "lib." + fileName + "_add_callback" + "__python_next" + p(helperClass + "." + "__python_next"),
            w =>{
              // NEXT in iteration
              val ret = keyTy
              val defArgs = Seq("cself").mkString("(", ", ", ")")
              val cArgs = Seq("struct DjinniObjectHandle *").mkString("(", ", ", ")")
              writeGetterCallback("__python_next", ret, cArgs, defArgs, w, w => {
                writeReturnFromCallback("CPyObjectProxy.toPyObj(None, cself)", keyTyRef, next, w)
              })
            }
            )

            // change function get elem with get key and write function to get elem
          case MSet =>
            val keyTyRef = getContainerElTyRef(tm, 0, ident)
            val keyTy =  cMarshal.cReturnType(Some(keyTyRef), true)

            writeGettersCallbacks(tm, ident, fileName, helperClass, w,
              "CPyObjectProxy.toPyObj(cself)", "", "",
              "", keyTyRef, keyTy, "size_t", "CPyObjectProxy.toPyObj(" + "None" + ", " + "cself)[index]", "index", keyTyRef,
              "len(CPyObjectProxy.toPyObj(" + "None" + ", " + "cself))",
              proxyName + p("set()"),
              "CPyObjectProxy.toPyObj(" + "None" + ", " + "cself).add(" + marshal.convertTo("el", keyTyRef) + ")",
              "el", keyTy,
              "lib." + fileName + "_add_callback" + "__python_next" + p(helperClass + "." + "__python_next"),
              w => {
                // NEXT in iteration
                val ret = keyTy
                val defArgs = Seq("cself").mkString("(", ", ", ")")
                val cArgs = Seq("struct DjinniObjectHandle *").mkString("(", ", ", ")")

                writeGetterCallback("__python_next", ret, cArgs, defArgs, w, w => {
                  writeReturnFromCallback("CPyObjectProxy.toPyObj(None, cself)", keyTyRef, next, w)
                })
              })

          case _ =>
        }
      }
      w.wl(helperClass + "._add_callbacks()")
      w.wl
      if (tm.base == MSet || tm.base == MMap) {
        w.wl("class " + proxyName + ":").nested {
          w.wl("def iter(d):").nested {
            w.wl("for k in d:").nested {
              w.wl("yield k")
            }
          }
          w.wl
          w.wl("def __init__(self, py_obj):").nested {
            w.wl("self._py_obj = py_obj")
            w.wl("if py_obj is not None:").nested {
              w.wl("self._py_iter = iter(py_obj)")
            }
            w.wl("else:").nested {
              w.wl("self._py_iter = None")
            }
          }
        }
      }
    })
  }

  // for map and set have proxy map and set that can hold on to metadata like iterator

  class PythonRefs(ident: Ident, origin: String) {
    var python = mutable.TreeSet[String]()

    def collect(ty: TypeRef, justCollect: Boolean) { collect(ty.resolved, justCollect, false) }
    def collect(tm: MExpr, justCollect: Boolean, isOpt: Boolean) {
      tm.args.foreach(t => collect(t, justCollect, isOpt))
      collect(tm.base, justCollect)

      val idlName = marshal.getExprIdlName(tm)
      val fileName = idlName

      tm.base match {
        case MList | MSet | MMap => {
          if (justCollect) {
            if (tm.base == MList) python.add("from " + marshal.dh + fileName + " import " + idPython.className(fileName) + "Helper")
            else {
              python.add("from " + marshal.dh + fileName + " import " + idPython.className(fileName) + "Helper")
              python.add("from " + marshal.dh + fileName + " import " + idPython.className(fileName) + "Proxy")
            }
          } else {
            if (!writtenFiles.contains(idlName + ".py")) {
              writtenFiles.put(fileName.toLowerCase(), fileName)
              generateContainer(tm, isOpt, fileName, idlName, ident, origin, marshal.referencesForContainer(tm, idlName))
            }
          }
        }
        case MOptional =>
           tm.args(0).base match {
             case m @ (MPrimitive(_,_,_,_,_,_,_,_) | MDate) => {
               python.add("from djinni.pycffi_marshal import CPyBoxed" + idPython.className(m.asInstanceOf[MOpaque].idlName))
             }
             case _ => collect(tm.args(0), justCollect, true)
           }
        case _ =>
      }
    }
    def collect(m: Meta, justCollect: Boolean) = if (justCollect) for(r <- marshal references(m, ident.name)) r match {
      case ImportRef(arg) => {
        python.add(arg)
      }
      case _ =>
    }
  }

  def getCArgTypes(m: Interface.Method, self: String) = {
    (Seq(self) ++ m.params.map(p => cMarshal.cParamType(p.ty, true))).mkString("(", ", ", ")")
  }

  def getDefArgs(m: Interface.Method, self: String) = {
    if (m.static) {
       m.params.map(p => idPython.local(p.ident.name)).mkString("(", ", ", ")")
    } else {
      (Seq(self) ++ m.params.map(p => idPython.local(p.ident.name))).mkString("(", ", ", ")")
    }
  }

  def getLibArgsFrom(m: Interface.Method, pythonClass: String) = {
    if (m.static || pythonClass == "") {
      m.params.map(p => marshal.releaseRAII(marshal.pyName(p.ident.name, p.ty), p.ty)).mkString("(", ", ", ")")
    } else {
      (Seq("self._cpp_impl") ++ m.params.map(p => marshal.releaseRAII(marshal.pyName(p.ident.name, p.ty), p.ty))).mkString("(", ", ", ")")
    }
  }

  def getLibArgsTo(m: Interface.Method, pythonClass: String) = {
      m.params.map(p => marshal.convertTo(p.ident.name, p.ty)).mkString("(", ", ", ")")
  }

  def writePythonFile(ident: String, origin: String, refs: Iterable[String], includeCffiLib: Boolean, f: IndentWriter => Unit) {
    createFileOnce(spec.pyOutFolder.get, idPython.ty(ident) + ".py", (w: IndentWriter) => {
      w.wl("# AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("# This file generated by Djinni from " + origin)
      w.wl
      w.wl("from djinni.support import MultiSet # default imported in all files")
      w.wl("from djinni.exception import CPyException # default imported in all files")

      var condensed_refs = Iterable[String]()
      var support_lib_refs = ""
      val prefix_len = "from djinni.pycffi_marshal import ".length
      refs.foreach(r => {
        if (r.startsWith("from djinni.pycffi_marshal import ")) {
          if (support_lib_refs == "") support_lib_refs = r
          else support_lib_refs += ", " + r.slice(prefix_len, r.length)
        } else {
          condensed_refs = condensed_refs ++ Seq(r)
        }
      })

      w.wl(support_lib_refs)
      if (condensed_refs.nonEmpty) {
        w.wl
        condensed_refs.foreach(w.wl)
      }

      if (includeCffiLib) {
        val cffi = spec.pycffiPackageName + "_cffi"
        w.wl("from " + cffi + " import ffi, lib")
        w.wl
      }
      w.wl("from djinni import exception # this forces run of __init__.py which gives cpp option to call back into py to create exception")
      w.wl
      f(w)
    })
  }

  def checkNonOptionals(m: Interface.Method, w: IndentWriter): Unit = {
    m.params.map(arg => if (arg.ty.resolved.base != MOptional) w.wl("assert " + arg.ident.name + " is not None "))
  }

  def processPackedArgs(m: Interface.Method,
                        withStmts: mutable.ArrayBuffer[String] ): Unit = {
    val args = mutable.ArrayBuffer(m.params.filter((p: Field) => marshal.isPacked(p.ty)):_*)
    if (args.nonEmpty) {
      withStmts.appendAll(args.map(arg => {
        val argName = idPython.local(arg.ident.name) // .name?
        val pyArgName = idPython.local(marshal.pyName(arg.ident.name, arg.ty))

        marshal.getPacked(arg, argName, pyArgName)
      }))
    }
  }

  def writeWithStmts(withStmts: Seq[String], w: IndentWriter)(f: => Unit) {
    if (withStmts.nonEmpty) {
      val len = withStmts.length

      if (len == 1) {
        w.wl("with " + withStmts.head + ":")
      } else {
        w.wl("with " + withStmts.head + ",\\")
        // extra nesting for 2nd..last with
        w.nestedN(2){
          val middle = withStmts.slice(1, len-1)
          middle.map(s => {
            w.wl(s + ",\\")
          })
          w.wl(withStmts(len-1) + ":")
        }
      }
      w.nested(f)
    } else {
      f
    }
  }

  def writeDeclStmts(declStmts: Seq[String], w: IndentWriter): Unit = {
    declStmts.map(s => w.wl(s))
  }

  def checkForExceptionFromPython( libcall: IndentWriter => Unit, returnNotVoid: Boolean, w:IndentWriter): Unit = {
    w.wl("try:").nested {
      libcall(w)
    }
    w.wl("except Exception as _djinni_py_e:").nested {
      w.wl("CPyException.setExceptionFromPy" + p("_djinni_py_e"))
      if (returnNotVoid) {
        w.wl("return ffi.NULL")
      }
    }
  }

  def writeReturnFromCallback(self: String, ret: TypeRef, libCall: String, w: IndentWriter): Unit = {
    val returnStmt: String = ret.resolved.base match {
      case MString | MBinary =>
        checkForExceptionFromPython( w=> {
          w.wl("with " + marshal.convertFrom(libCall, ret) + " as py_obj:").nested {
            w.wl("_ret = " + marshal.releaseRAII("py_obj", ret))
            w.wl("assert _ret != ffi.NULL")
            w.wl("return _ret")
          }
        }, true, w)
        return
      case MOptional => {
        val optTy = ret.resolved.args(0)
        optTy.base match {
          case MSet | MMap =>
            checkForExceptionFromPython( w=> {
            w.wl("return " + marshal.convertFrom(libCall, ret))}, true, w)
          case MString | MBinary  =>
            checkForExceptionFromPython( w=> {
            w.wl("with " + marshal.convertFrom(libCall, ret) + " as py_obj:").nested {
              w.wl("return " + marshal.releaseRAII("py_obj", optTy, true)) // here
            } }, true, w)
          case MPrimitive(_,_,_,_,_,_,_,_) | MDate =>
            checkForExceptionFromPython( w=> {
            w.wl("with " + marshal.convertFrom(libCall, ret) + " as py_obj:").nested {
              w.wl("return " + marshal.releaseRAII("py_obj", ret)) // here
            }},true, w)
          case _ =>
            checkForExceptionFromPython( w=> {
              w.wl("return " + marshal.convertFrom(libCall, ret))
            }, true, w)
        }
        // for optionals we don't need asserts of non-None, so we return here
        return
      }
      case d: MDef => d.defType match {
        case DEnum =>
          checkForExceptionFromPython(w => {
            w.wl("_ret= " + marshal.convertFrom(libCall, ret))
            w.wl("assert _ret.value != -1")
            w.wl("return _ret")
          }, true, w)
          return
        case _ => "_ret = " + marshal.convertFrom(libCall, ret)
      }
      case _ => "_ret = " + marshal.convertFrom(libCall, ret)
    }

    checkForExceptionFromPython(w => {
      w.wl(returnStmt)
      if (!marshal.isPrimitive(ret)) {
        w.wl("assert _ret != ffi.NULL")
      }
      w.wl("return _ret")

    }, true, w)
  }

  def checkForExceptionFromCpp(ret: String, w: IndentWriter) {
    w.wl("CPyException.toPyCheckAndRaise" + p(ret))
  }

  def writeCppProxyMethod(m: Interface.Method, cMethodWrapper: String, pythonClass: String, w: IndentWriter): Unit = {
    val defArgs = getDefArgs(m, "self")
    if (m.static) {
      w.wl("@staticmethod")
    }
    w.wl("def " + m.ident.name + defArgs + ":").nested{
      val withStmts = mutable.ArrayBuffer[String]()
      processPackedArgs(m, withStmts)
      // check that if python promised to send as arguments non optionals then they are not None
      //checkNonOptionals(m, w)
      writeWithStmts(withStmts, w) {
        val libArgs = getLibArgsFrom(m, pythonClass)
        val libCall = lib + "." + cMethodWrapper + "_" + m.ident.name + libArgs
        if (m.ret.isEmpty) {
          w.wl(libCall)
          checkForExceptionFromCpp("ffi.NULL", w)
        }
        else {
          w.wl("_ret_c = " + libCall)
          checkForExceptionFromCpp("_ret_c", w)
          w.wl("_ret = " + marshal.convertTo("_ret_c", m.ret.get))
          // Check that if C promised to return a non-optional it is not None
          if (m.ret.get.resolved.base != MOptional){
            m.ret.get.resolved.base match {
              case d: MDef => d.defType match {
                case DEnum =>
                  w.wl("assert _ret.value != -1")
                case _ => w.wl("assert _ret is not None")
              }
              case _ => w.wl("assert _ret is not None")
            }
          }

          w.wl("return _ret")
        }
      }
    }
  }

  def writeCallbackMethod(m: Interface.Method, pythonClass: String, w: IndentWriter): Unit = {
    // Should I throw an exception if the method is declared static?
    val defArgs = getDefArgs(m, "cself")
    val cArgs = getCArgTypes(m, "struct " + cMarshal.djinniObjectHandle + " * ")
    val ret = cMarshal.cReturnType(m.ret, true)
    w.wl("@ffi.callback" + "(\"" + ret + cArgs + "\")")
    w.wl("def " + m.ident.name + defArgs + ":").nested {
      // check that if C promised non-optionals as arguments to callback, they are not None
      val libArgs = getLibArgsTo(m, "")
      val libCall = pythonClass + "Helper.selfToPy(cself)." + m.ident.name + libArgs
      if (m.ret.isDefined) {
        writeReturnFromCallback(pythonClass + "Helper.selfToPy(cself)", m.ret.get, libCall, w)
      } else {
        checkForExceptionFromPython(w=>{w.wl(libCall)}, false, w)
      }
    }
  }

  def writeCppProxyClass(pythonClass: String, cMethodWrapper: String, methods: Seq[Interface.Method], w: IndentWriter): Unit = {
    val proxyClass = pythonClass + "CppProxy"
    // Proxy Class Definition
    w.wl("class " + proxyClass + p(pythonClass) + ":").nested {
      // Init and Del methods
      w.wl("def __init__(self, proxy):").nested {
        w.wl("self._is_cpp_proxy = True")
        w.wl("self._cpp_impl" + " = proxy")
      }
      w.wl("def __del__(self):").nested {
        w.wl("if not lib:").nested {
          w.wl("return")
        }
        w.wl(lib + "." + cMethodWrapper + "___wrapper_dec_ref(self._cpp_impl)")
      }
      w.wl

      // Method implementations, calling into C code via ccfi library
      for (m <- methods) {
        writeCppProxyMethod(m, cMarshal.cw + cMethodWrapper, pythonClass, w)
        w.wl
      }
    }
  }
  def writeHelperMethodsForCppImplementedInterface(ident: String, ext: Ext, w: IndentWriter) {
    val helperClass = idPython.className(ident) + "Helper"
    val proxyClass = idPython.className(ident) + "CppProxy"
    // Static method to wrap as object of this class
    w.wl("@staticmethod")
    w.wl("def toPy(obj):").nested {
      w.wl("if obj == ffi.NULL:").nested {
        w.wl("return None")
      }
      if (ext.py) {
        w.wl("# Python Objects can be returned without being wrapped in proxies")
        w.wl("py_handle = lib.get_handle_from_proxy_object_" + cMarshal.cw + idPython.method(ident) + p("obj"))
        w.wl("if py_handle:").nested {
          w.wl("assert py_handle in " + helperClass + ".c_data_set")
          w.wl("aux = ffi.from_handle(ffi.cast(\"void * \", py_handle))")
          w.wl("lib." + idPython.method(ident) + "___wrapper_dec_ref" + p("obj"))
          w.wl("return aux")
        }
      }

      w.wl("return " + proxyClass + p("obj"))
    }
    w.wl
  }

  def writeCallbacksHelperClass(ident: Ident, pythonClass: String, methods: Seq[Interface.Method], ext: Ext, w: IndentWriter): Unit = {
    val proxyClass = pythonClass + "CallbacksHelper"
    val helperClass = pythonClass + "Helper"
    val classNameAsMethod = idPython.method(ident.name)
    // Proxy Class Definition
    w.wl("class " + proxyClass + "():").nested {
      // Method implementations, calling into C code via ccfi library
      for (m <- methods) {
        writeCallbackMethod(m, pythonClass, w)
        w.wl
      }

      // Function to release handler to python object
      writeReleaseCallback(helperClass, w)

      // Function to add callback on cpp side
      w.wl("@staticmethod")
      w.wl("def _add_callbacks():").nested {
        for (m <- methods) {
          w.wl("lib." + classNameAsMethod + "_add_callback_" + idPython.method(m.ident.name) + p(proxyClass + "." + m.ident.name))
        }
        w.wl
        // release method callback added
        w.wl("lib." + classNameAsMethod + "_add_callback_" + "__delete" + p(proxyClass + "." + "__delete"))
        w.wl
      }

    }
    w.wl(proxyClass + "._add_callbacks()")
    w.wl
  }

  def writeHelperMethodsForPythonImplementedInterface(ident: String, methods: Seq[Interface.Method], ext: Ext, w: IndentWriter): Unit = {
    val helperClass = idPython.className(ident) + "Helper"
    val cppProxyClass = idPython.className(ident) + "CppProxy"
    w.wl("@staticmethod")
    w.wl("def selfToPy(obj):").nested {
      w.wl("assert obj in " + helperClass + ".c_data_set")
      w.wl("return ffi.from_handle(ffi.cast(\"void * \",obj))")
    }
    w.wl

    // Helper function for having this interface as argument
    w.wl("@staticmethod")
    w.wl("def fromPy(py_obj):").nested {
      w.wl("if py_obj is None:").nested {
        w.wl("return" + " ffi.NULL")
      }
      // check whether cpp implementation given (then no need to wrap as pyproxy)
      if (ext.cpp) {
        w.wl("if isinstance(py_obj, " + cppProxyClass +"):").nested {
          w.wl("lib." + idPython.method(ident) + "___wrapper_add_ref(py_obj._cpp_impl)")
          w.wl("return py_obj._cpp_impl")
          w.wl
        }
      }

      w.wl("py_proxy = " + p("py_obj"))
      // check wheter object implements all needed functions
      for (m <- methods) {
        w.wl("if not hasattr" + p("py_obj, " + q(idPython.method(m.ident.name))) + ":").nested {
          w.wl("raise TypeError")
        }
      }
      w.wl
      w.wl("bare_c_ptr = ffi.new_handle(py_proxy)")
      w.wl(helperClass + ".c_data_set.add(bare_c_ptr)")
      w.wl("wrapped_c_ptr = lib.make_proxy_object_from_handle_" + cMarshal.cw + idPython.method(ident) + p("bare_c_ptr"))
      w.wl("return wrapped_c_ptr")
    }
  }

  def writeNonRecursiveConst(w: IndentWriter, ty: TypeRef, v: Any, selfName: String): Unit = v match {
    case l: Long => w.w(l.toString)
    case d: Double => w.w(d.toString)
    case b: Boolean => w.w(if (b) "True" else "False")
    case s: String => w.w(s)
    case e: EnumValue => throw new NotImplementedError()
    case v: ConstRef => w.w(selfName + "." + idPython.const(v))
  }

  def generateNonRecursiveConstants(w: IndentWriter, consts: Seq[Const], selfName: String, genRecord: Boolean) = {
    for (c <- consts) {
      if (! c.value.isInstanceOf[Map[_, _]]){ // is not a record
        w.w( idPython.const(c.ident) + " = ")
        writeNonRecursiveConst(w, c.ty, c.value, selfName)
        w.wl
      }
    }
  }

  def generateRecursiveConstants(w: IndentWriter, consts: Seq[Const], selfName: String): Unit = {
    def writeRecursiveConst(w: IndentWriter, cName: String, ty: TypeRef, v: Any, selfName: String): Unit = v match {
      case z: Map[_, _] => { // Value is record
        val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        val recordClassName = idPython.className(recordMdef.name)
        w.wl(selfName + "." + idPython.const(cName) + " = " + recordClassName + "(")
        w.increase()
        // Use exact sequence
        val skipFirst = SkipFirst()
        for (f <- record.fields) {
          skipFirst {w.wl(",")}
          writeRecursiveConst(w, cName, f.ty, vMap.apply(f.ident.name), selfName)
        }
        w.decrease()
        w.w(")")
        w.wl
      }
      case _ => writeNonRecursiveConst(w, ty, v, selfName)
    }

    for (c <- consts) {
      if (c.value.isInstanceOf[Map[_, _]]){ // is a record
        writeRecursiveConst(w, c.ident, c.ty, c.value, selfName)
        w.wl
      }
    }
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface): Unit = {
    System.out.println("Generting python interface...", origin, ident)
    val pythonClass = idPython.className(ident.name)
    val cMethodWrapper = idPython.method(ident.name)
    val refs = new PythonRefs(ident, origin)
    i.consts.map(c => {
      refs.collect(c.ty, true)
    })
    i.methods.map(m => {
      m.params.foreach(p => refs.collect(p.ty, true))
      m.ret.foreach(t => refs.collect(t, true))
    })
    i.methods.map(m => {
      m.params.foreach(p => refs.collect(p.ty, false))
      m.ret.foreach(t => refs.collect(t, false))
    })
    refs.python.add("from abc import ABCMeta, abstractmethod")
    refs.python.add("from future.utils import with_metaclass")

    writePythonFile(ident, origin, refs.python, true, w => {
      // Asbtract Class Definition
      w.wl("class " + pythonClass + "(with_metaclass(ABCMeta)):").nested {
        val docConsts = if (!i.consts.exists(!_.doc.lines.isEmpty)) Seq() else Seq({
          w: IndentWriter => writeDocConstantsList(w, i.consts)
        })
        if (writeDocString(w, doc, docConsts)) { w.wl }

        if( ! i.consts.isEmpty) {
          generateNonRecursiveConstants(w, i.consts, idPython.className(ident.name), true)
          w.wl
        }
        for (m <- i.methods if ! m.static) {
          w.wl("@abstractmethod")
          w.wl("def " + idPython.method(m.ident.name) + getDefArgs(m, "self") + ":").nested {
            writeDocString(w, m.doc)
            w.wl("raise NotImplementedError")
          }
          w.wl
        }
        if (i.ext.cpp) { // static methods only exist one way
          val proxyClass = pythonClass + "CppProxy"
          for (m <- i.methods if m.static) {
            val defArgs = getDefArgs(m, "self")
            w.wl("@staticmethod")
            w.wl("def " + idPython.method(m.ident.name) + defArgs + ":").nested {
              writeDocString(w, m.doc)
              if (m.ret.isDefined) {
                w.wl("return " + proxyClass + "." + idPython.method(m.ident.name) + defArgs)
              } else {
                w.wl(proxyClass + "." + idPython.method(m.ident.name) + defArgs)
              }
            }
          }
        }
      }
      w.wl
      // Recursive constants, such as records, might reference the interface class that is currently definied
      // They are added as properties on the class, after the class definition to avoid 'incomplete type' errors
      if( ! i.consts.isEmpty) {
        generateRecursiveConstants(w, i.consts, idPython.className(ident.name))
        w.wl
      }
      if (i.ext.cpp && i.ext.py) {
        writeCppProxyClass(pythonClass, cMethodWrapper, i.methods, w)
        writeCallbacksHelperClass(ident, pythonClass, i.methods, i.ext, w)
        w.wl("class " + pythonClass + "Helper" + ":").nested {
          w.wl("c_data_set = MultiSet()")
          writeHelperMethodsForCppImplementedInterface(ident.name, i.ext, w)
          writeHelperMethodsForPythonImplementedInterface(ident.name, i.methods, i.ext, w)
        }
      } else if (i.ext.cpp) {
        writeCppProxyClass(pythonClass, cMethodWrapper, i.methods, w)
        w.wl("class " + pythonClass + "Helper" + ":").nested {
          w.wl("c_data_set = MultiSet()")
          writeHelperMethodsForCppImplementedInterface(ident.name, i.ext, w)
        }
      } else if (i.ext.py) {
        writeCallbacksHelperClass(ident, pythonClass, i.methods, i.ext, w)
        w.wl("class " + pythonClass + "Helper" + ":").nested {
          w.wl("c_data_set = MultiSet()")
          writeHelperMethodsForCppImplementedInterface(ident.name, i.ext, w)
          writeHelperMethodsForPythonImplementedInterface(ident.name, i.methods, i.ext, w)
        }
      }
    })
  }

  def writeRecordCallbacks(ident: Ident, r: Record, number: Int, callbackNames: mutable.Set[String], w: IndentWriter) = {
    val recordAsMethod = idPython.method(ident.name)
    var field_number = number
    for (f <- r.fields) {
      field_number += 1
      val f_id = field_number.toString()

      val ret = cMarshal.cReturnType(Some(f.ty), true)
      val defArgs = Seq("cself").mkString("(", ", ", ")")
      val cArgs = Seq("struct DjinniRecordHandle *").mkString("(", ", ", ")")
      val methodName = "get_" + ident.name + "_f" + f_id;
      callbackNames.add(methodName)

      w.wl("@ffi.callback" + "(\"" + ret + cArgs + "\")")
      w.wl("def " + methodName + defArgs + ":").nested {
        val libCall = "CPyRecord.toPy" + p("None" + ", cself") + "." + idPython.local(f.ident.name)
        writeReturnFromCallback("CPyRecord.toPy" + p("None" + ", cself"), f.ty, libCall, w)
      }
      w.wl
    }

    // Callback to allow creating a Python Record from C
    w.wl("@ffi.callback" + p(q("struct DjinniRecordHandle *" + getRecordTypes(r).mkString("(", ",", ")"))))
    w.wl("def " + "python_create_" + recordAsMethod + getRecordArguments(r, "").mkString("(", ",", ")") +":").nested {
      writeRecordToFromCb(ident, r, "", 0, w)
    }
    callbackNames.add("python_create_" + recordAsMethod)
    w.wl

    val recordAsClassName = idPython.className(ident.name)
    w.wl("@ffi.callback" + p(q("void " + p("struct DjinniRecordHandle *"))))
    w.wl("def " + "__delete" + p("dh") +":").nested {
      w.wl("assert dh in " + recordAsClassName + ".c_data_set")
      w.wl(recordAsClassName  + ".c_data_set.remove(dh)")
    }
    callbackNames.add("__delete")
    w.wl
  }

  def writeRecordToFromCb(ident: Ident, r: Record, prefix: String, number: Int, w: IndentWriter) = {
    val recordClassName = idPython.className(ident.name)
    val skipFirst = SkipFirst()
    w.wl("py_rec = " + recordClassName + "(").nested {
      for (f <- r.fields) {
        skipFirst {w.wl(",")}

        w.w(marshal.convertTo(f.ident.name, f.ty ))
      }
    }
    w.wl(")")
    w.wl("return CPyRecord.fromPy(" + recordClassName +  ".c_data_set, py_rec) #to do: can be optional?")
  }

  def cmpRecordFields(fieldName: String, cmpOp: String, needsParens: Boolean) = {
    if (needsParens) {
      "self." + fieldName + cmpOp + p("other." + fieldName)
    } else {
      "self." + fieldName + cmpOp + "other." + fieldName
    }
  }

  def writeRecordDerivings(r: Record, w: IndentWriter): Unit = {
    // EQUAL
    if (r.derivingTypes.contains(DerivingType.Eq)) {
      w.wl("def " + "__eq__" + "(self, other):").nested {
        w.w("return ")
        val skipFirst = SkipFirst()
        for (f <- r.fields) {
          skipFirst{w.wl(" and \\")}
          val isPrimitive = marshal.isPrimitive(f.ty)
          w.w(cmpRecordFields(f.ident.name, if (isPrimitive) "==" else ".__eq__", !isPrimitive))
        }
      }
      w.wl
    }

    if (r.derivingTypes.contains(DerivingType.Ord)) {
      // LESS THAN
      w.wl("def " + "__lt__" + "(self, other):").nested {
        for (f <- r.fields) {
          val isPrimitive = marshal.isPrimitive(f.ty)
          w.wl("if " + cmpRecordFields(f.ident.name, if (isPrimitive) "<" else ".__lt__", !isPrimitive) + ":").nested {
            w.wl("return True")
          }
          w.wl("if "+ cmpRecordFields(f.ident.name, if (isPrimitive) ">" else ".__gt__", !isPrimitive) +":").nested {
            w.wl("return False")
          }
        }
        w.wl("return False")
      }
      // LESS THAN OR EQUAL
      w.wl("def " + "__le__" + "(self, other):").nested {
        w.wl("return not other.__lt__(self)")
      }
      // GREATER THAN OR EQUAL
      w.wl("def " + "__ge__" + "(self, other):").nested {
        w.wl("return not self.__lt__(other)")
      }
      // GREATER THAN
      w.wl("def " + "__gt__" + "(self, other):").nested {
        w.wl("return other.__lt__(self)")
      }
    }

    // HASH CODE
    w.wl("def __hash__(self):").nested {
      w.wl("# Pick an arbitrary non-zero starting value")
      w.wl("hash_code = 17")
      for (f <- r.fields) { // TODO: should we check that if f is a record it has derivings? we don't in Java
        w.wl("hash_code = hash_code * 31 + self." + f.ident.name + ".__hash__()")
      }
      w.wl("return hash_code")
    }
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record): Unit = {
    val recordClassName = idPython.className(ident.name)
    val recordAsMethod = idPython.method(ident.name)
    val refs = new PythonRefs(ident, origin)
    refs.python.add("from djinni.pycffi_marshal import CPyRecord")
    r.fields.foreach(f => refs.collect(f.ty, true))
    r.fields.foreach(f => refs.collect(f.ty, false))
    r.consts.foreach(c => refs.collect(c.ty, true))

    writePythonFile(ident.name + (if(r.ext.py) "_base" else ""), origin, refs.python, true, w => {
      // Record Definition
      w.wl("class " + recordClassName + (if(r.ext.py) "Base" else "") + ":").nested {
        var docLists = mutable.ArrayBuffer[IndentWriter => Unit]()
        if (r.fields.exists(!_.doc.lines.isEmpty)) docLists += { w: IndentWriter => writeDocFieldsList(w, r.fields)}
        if (r.consts.exists(!_.doc.lines.isEmpty)) docLists += { w: IndentWriter => writeDocConstantsList(w, r.consts)}
        if (writeDocString(w, doc, docLists)) { w.wl }


        w.wl("c_data_set = MultiSet()")
        w.wl
        w.wl("@staticmethod")
        w.wl("def check_c_data_set_empty():").nested {
          w.wl("assert len" + p(recordClassName + ".c_data_set") + " == 0")
          r.fields.map( f => if (marshal.getClassHoldingCDataSet(f.ty.resolved) != "") {
            w.wl(marshal.getClassHoldingCDataSet(f.ty.resolved) + ".check_c_data_set_empty()")
          })
        }
        w.wl
        if (! r.consts.isEmpty) {
          generateNonRecursiveConstants(w, r.consts, recordClassName, false)
          w.wl
        }
        // TODO: figure out if we should have default intitializations for members of record
        //  r.fields.map(f => w.wl("#" + marshal.privateClassMember(idPython.local(f.ident.name)) + " = could have default initializer"))
        // Derivings
        if (r.derivingTypes.nonEmpty){
          w.wl("# Record deriving types")
          writeRecordDerivings(r, w)
        }
        w.wl
        // Constructor
        w.wl("def __init__" + getRecordArguments(r, "self").mkString("(", ", ", ")") + ":" ).nested {
          r.fields.foreach( f => w.wl("self." + idPython.local(f.ident.name) + " = " + idPython.local(f.ident.name)))
          if (r.fields.isEmpty) { w.wl("pass") }
        }
      }
      w.wl
      // Const record object of type equal to current record type must be defined after class definition
      if (r.consts.nonEmpty) {
        generateRecursiveConstants(w, r.consts, recordClassName)
        w.wl
      }
    })

    writePythonFile(ident.name + "_helper", origin, refs.python, true, w => {
      w.wl("from " + ident.name + " import " + recordClassName )
      w.wl
      w.wl("class " + recordClassName + "Helper" + ":").nested {
        w.wl("@staticmethod")
        w.wl("def release(c_ptr):").nested {
          w.wl("assert c_ptr in c_data_set")
          w.wl("c_data_set.remove(ffi.cast(\"void*\", c_ptr))")
        }
        w.wl
        // Callbacks to pass every record field to C, and to allow creating record from C
        val callbackNames = mutable.Set[String]()
        writeRecordCallbacks(ident, r, 0, callbackNames, w)

        // Function to give C access to callbacks
        w.wl("@staticmethod")
        w.wl("def _add_callbacks():").nested {
          for (cb <- callbackNames) {
            w.wl("lib." + recordAsMethod + "_add_callback_" + idPython.method(cb) + p(recordClassName + "Helper" + "." + cb))
          }
        }
      }
      w.wl
      // Recursive constants, such as records, might reference the record class that is currently definied
      // They are added as properties on the class, after the class definition to avoid 'incomplete type' errors
      if( ! r.consts.isEmpty) {
        generateRecursiveConstants(w, r.consts, recordClassName)
        w.wl
      }
      // Send callback pointers to C
      w.wl(recordClassName + "Helper" + "._add_callbacks()")
      w.wl
    })

  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum): Unit = {
    val enumClassName = idPython.className(ident.name)
    val refs = new PythonRefs(ident, origin)
    writePythonFile(ident, origin, refs.python, false, w => {
      w.wl("from enum import IntEnum, unique")
      w.wl
      // TODO: Consider whether to use IntEnum rather than Enum
      w.wl("@unique")
      w.wl("class " + enumClassName + p("IntEnum") + ":").nested {
        if (writeDocString(w, doc)) { w.wl }

        // Generate enum fields, and set their values to 0..# of fields -1
        var i = 0
        for (o <- e.options) {
          writeDocString(w, o.doc) // Enum supports docstrings, unlike plain constant variables.
          w.wl(s"${idPython.enum(o.ident.name)} = $i")
          i += 1
        }
      }
    })
  }
}
