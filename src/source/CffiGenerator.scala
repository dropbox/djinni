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
import djinni.writer.IndentWriter

class CffiGenerator(spec: Spec) extends Generator(spec) {
    val marshal = new PythonMarshal(spec)
    val cffi = spec.pycffiPackageName + "_cffi"

  def writeCffiFile(ident: String, origin: String, f: IndentWriter => Unit) {
    createFileOnce(spec.pycffiOutFolder.get, "pycffi_lib_build" + ".py", (w: IndentWriter) => {
      w.wl("# AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("# This file generated by Djinni from " + spec.idlFileName)
      w.wl
      f(w)
    })
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface): Unit = {
    System.out.println("Generting cffi interface...", origin, ident)
    writeCffiFile("pycffi_lib_build.py", origin, w => {
      w.wl("import sys")
      w.wl("from cffi import FFI")
      w.wl("from djinni.support import *")
      w.wl("ffi = FFI()")
      w.wl
      // Paths relative to test-suite/pybuild
      //TODO: raise exception if not at least 2 arguments?
      w.wl("args = sys.argv[1:]")
      w.wl("cdef_headers = sort_by_import_order(clean_headers_for(args, 'python_cdef_ignore'))")
      w.wl("setsource_headers = sort_by_import_order(clean_headers_for(args, 'python_setsource_ignore'))")
      w.wl

      w.wl("f = open('clean_headers.h', 'w')")
      w.wl("f.write(setsource_headers)")
      w.wl("f.close()")
      w.wl
      w.wl("ffi.set_source('" + cffi + "', '''#include \"clean_headers.h\"''',").nested {
        w.w("runtime_library_dirs=['.']")
        if(!spec.pycffiDynamicLibList.isEmpty) {
          w.wl(",")
          w.w("libraries=['" + spec.pycffiDynamicLibList + "']")
          w.w("libraries=[")
          val libs = spec.pycffiDynamicLibList.split(",");
          var first : Boolean = true
          for (lib <- libs) {
            if (!first) {
              w.w(", ")
            }
            w.w("'" + lib + "'");
            first = false
          }
          w.w("]")
        }
        w.wl(")")
      }
      w.wl
      w.wl("ffi.cdef(\"typedef _Bool bool;\"\n + cdef_headers)")
      w.wl
      w.wl("if __name__ == '__main__':").nested {
        w.wl("ffi.compile()")
      }
      w.wl
    })

  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record): Unit = {
      // Not needed
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum): Unit = {
      // Not needed
  }
}

