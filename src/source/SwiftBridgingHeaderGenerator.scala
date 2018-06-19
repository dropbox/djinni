/**
  * Copyright 2016 Dropbox, Inc.
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

import java.io._

import djinni.ast._
import djinni.generatorTools._

class SwiftBridgingHeaderGenerator(spec: Spec) extends Generator(spec) {
  val marshal = new ObjcMarshal(spec)

  override def generateEnum(origin: String, ident: Ident, doc: Doc, comment: Comment, e: Enum) {
    spec.objcSwiftBridgingHeaderWriter.get.write("#import \"" + marshal.headerName(ident) + "\"\n")
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, comment: Comment, typeParams: Seq[TypeParam], i: Interface) {
    spec.objcSwiftBridgingHeaderWriter.get.write("#import \"" + marshal.headerName(ident) + "\"\n")
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, comment: Comment, params: Seq[TypeParam], r: Record) {
    spec.objcSwiftBridgingHeaderWriter.get.write("#import \"" + marshal.headerName(ident) + "\"\n")
  }
}

object SwiftBridgingHeaderGenerator {
  
  val bridgingHeaderName = (s: String) => s.split('-').mkString("_")
  val bridgingHeaderVariables = (s: String) => s.split('-').mkString("")

  def writeAutogenerationWarning(name: String, writer: Writer) {
    val bridgingHeaderVarName = bridgingHeaderName(name)
    writer.write("// AUTOGENERATED FILE - DO NOT MODIFY!\n")
    writer.write("// This file generated by Djinni\n\n")
    writer.write("// " + bridgingHeaderVarName + ".h\n")
    writer.write("// " + bridgingHeaderVarName + "\n\n")
  }

  def writeBridgingVars(name: String, writer: Writer) {
    val bridgingHeaderVarName = bridgingHeaderVariables(name)
    writer.write("#import <Foundation/Foundation.h>\n\n")
    writer.write("//! Project version number for " + bridgingHeaderVarName +".\n")
    writer.write("FOUNDATION_EXPORT double " + bridgingHeaderVarName + "VersionNumber;\n\n")
    writer.write("//! Project version string for " + bridgingHeaderVarName +".\n")
    writer.write("FOUNDATION_EXPORT const unsigned char " + bridgingHeaderVarName + "VersionString[];\n\n")
  }
}