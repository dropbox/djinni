/**
  * Copyright 2014 Dropbox, Inc.
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
import java.io._
import djinni.generatorTools._
import djinni.meta._
import djinni.syntax.Error
import djinni.writer.IndentWriter
import scala.language.implicitConversions
import scala.collection.mutable

package object generatorTools {

  case class Spec(
                   javaOutFolder: Option[File],
                   javaPackage: Option[String],
                   javaClassAccessModifier: JavaAccessModifier.Value,
                   javaIdentStyle: JavaIdentStyle,
                   javaCppException: Option[String],
                   javaAnnotation: Option[String],
                   javaNullableAnnotation: Option[String],
                   javaNonnullAnnotation: Option[String],
                   javaImplementAndroidOsParcelable: Boolean,
                   javaUseFinalForRecord: Boolean,
                   cppOutFolder: Option[File],
                   cppHeaderOutFolder: Option[File],
                   cppIncludePrefix: String,
                   cppExtendedRecordIncludePrefix: String,
                   cppNamespace: String,
                   cppIdentStyle: CppIdentStyle,
                   cppFileIdentStyle: IdentConverter,
                   cppOptionalTemplate: String,
                   cppOptionalHeader: String,
                   cppEnumHashWorkaround: Boolean,
                   cppNnHeader: Option[String],
                   cppNnType: Option[String],
                   cppNnCheckExpression: Option[String],
                   cppUseWideStrings: Boolean,
                   jniOutFolder: Option[File],
                   jniHeaderOutFolder: Option[File],
                   jniIncludePrefix: String,
                   jniIncludeCppPrefix: String,
                   jniNamespace: String,
                   jniClassIdentStyle: IdentConverter,
                   jniFileIdentStyle: IdentConverter,
                   jniBaseLibIncludePrefix: String,
                   cppExt: String,
                   cppHeaderExt: String,
                   objcOutFolder: Option[File],
                   objcppOutFolder: Option[File],
                   objcIdentStyle: ObjcIdentStyle,
                   objcFileIdentStyle: IdentConverter,
                   objcppExt: String,
                   objcHeaderExt: String,
                   objcIncludePrefix: String,
                   objcExtendedRecordIncludePrefix: String,
                   objcppIncludePrefix: String,
                   objcppIncludeCppPrefix: String,
                   objcppIncludeObjcPrefix: String,
                   objcppNamespace: String,
                   objcBaseLibIncludePrefix: String,
                   objcSwiftBridgingHeaderWriter: Option[Writer],
                   outFileListWriter: Option[Writer],
                   skipGeneration: Boolean,
                   yamlOutFolder: Option[File],
                   yamlOutFile: Option[String],
                   yamlPrefix: String)

  def preComma(s: String) = {
    if (s.isEmpty) s else ", " + s
  }
  def q(s: String) = '"' + s + '"'
  def firstUpper(token: String) = if (token.isEmpty()) token else token.charAt(0).toUpper + token.substring(1)

  type IdentConverter = String => String

  case class CppIdentStyle(ty: IdentConverter, enumType: IdentConverter, typeParam: IdentConverter,
                           method: IdentConverter, field: IdentConverter, local: IdentConverter,
                           enum: IdentConverter, const: IdentConverter)

  case class JavaIdentStyle(ty: IdentConverter, typeParam: IdentConverter,
                            method: IdentConverter, field: IdentConverter, local: IdentConverter,
                            enum: IdentConverter, const: IdentConverter)

  case class ObjcIdentStyle(ty: IdentConverter, typeParam: IdentConverter,
                            method: IdentConverter, field: IdentConverter, local: IdentConverter,
                            enum: IdentConverter, const: IdentConverter)

  object IdentStyle {
    val camelUpper = (s: String) => s.split('_').map(firstUpper).mkString
    val camelLower = (s: String) => {
      val parts = s.split('_')
      parts.head + parts.tail.map(firstUpper).mkString
    }
    val underLower = (s: String) => s
    val underUpper = (s: String) => s.split('_').map(firstUpper).mkString("_")
    val underCaps = (s: String) => s.toUpperCase
    val prefix = (prefix: String, suffix: IdentConverter) => (s: String) => prefix + suffix(s)

    val javaDefault = JavaIdentStyle(camelUpper, camelUpper, camelLower, camelLower, camelLower, underCaps, underCaps)
    val cppDefault = CppIdentStyle(camelUpper, camelUpper, camelUpper, underLower, underLower, underLower, underCaps, underCaps)
    val objcDefault = ObjcIdentStyle(camelUpper, camelUpper, camelLower, camelLower, camelLower, camelUpper, camelUpper)

    val styles = Map(
      "FooBar" -> camelUpper,
      "fooBar" -> camelLower,
      "foo_bar" -> underLower,
      "Foo_Bar" -> underUpper,
      "FOO_BAR" -> underCaps)

    def infer(input: String): Option[IdentConverter] = {
      styles.foreach((e) => {
        val (str, func) = e
        if (input endsWith str) {
          val diff = input.length - str.length
          return Some(if (diff > 0) {
            val before = input.substring(0, diff)
            prefix(before, func)
          } else {
            func
          })
        }
      })
      None
    }
  }

  object JavaAccessModifier extends Enumeration {
    val Public = Value("public")
    val Package = Value("package")

    def getCodeGenerationString(javaAccessModifier: JavaAccessModifier.Value): String = {
      javaAccessModifier match {
        case Public => "public "
        case Package => "/*package*/ "
      }
    }

  }
  implicit val javaAccessModifierReads: scopt.Read[JavaAccessModifier.Value] = scopt.Read.reads(JavaAccessModifier withName _)

  final case class SkipFirst() {
    private var first = true

    def apply(f: => Unit) {
      if (first) {
        first = false
      }
      else {
        f
      }
    }
  }

  case class GenerateException(message: String) extends java.lang.Exception(message)

  def createFolder(name: String, folder: File) {
    folder.mkdirs()
    if (folder.exists) {
      if (!folder.isDirectory) {
        throw new GenerateException(s"Unable to create $name folder at ${q(folder.getPath)}, there's something in the way.")
      }
    } else {
      throw new GenerateException(s"Unable to create $name folder at ${q(folder.getPath)}.")
    }
  }

  def generate(idl: Seq[TypeDecl], spec: Spec): Option[String] = {
    try {
      if (spec.cppOutFolder.isDefined) {
        if (!spec.skipGeneration) {
          createFolder("C++", spec.cppOutFolder.get)
          createFolder("C++ header", spec.cppHeaderOutFolder.get)
        }
        new CppGenerator(spec).generate(idl)
      }
      if (spec.javaOutFolder.isDefined) {
        if (!spec.skipGeneration) {
          createFolder("Java", spec.javaOutFolder.get)
        }
        new JavaGenerator(spec).generate(idl)
      }
      if (spec.jniOutFolder.isDefined) {
        if (!spec.skipGeneration) {
          createFolder("JNI C++", spec.jniOutFolder.get)
          createFolder("JNI C++ header", spec.jniHeaderOutFolder.get)
        }
        new JNIGenerator(spec).generate(idl)
      }
      if (spec.objcOutFolder.isDefined) {
        if (!spec.skipGeneration) {
          createFolder("Objective-C", spec.objcOutFolder.get)
        }
        new ObjcGenerator(spec).generate(idl)
      }
      if (spec.objcppOutFolder.isDefined) {
        if (!spec.skipGeneration) {
          createFolder("Objective-C++", spec.objcppOutFolder.get)
        }
        new ObjcppGenerator(spec).generate(idl)
      }
      if (spec.objcSwiftBridgingHeaderWriter.isDefined) {
        SwiftBridgingHeaderGenerator.writeAutogenerationWarning(spec.objcSwiftBridgingHeaderWriter.get)
        new SwiftBridgingHeaderGenerator(spec).generate(idl)
      }
      if (spec.yamlOutFolder.isDefined) {
        if (!spec.skipGeneration) {
          createFolder("YAML", spec.yamlOutFolder.get)
        }
        new YamlGenerator(spec).generate(idl)
      }
      None
    }
    catch {
      case GenerateException(message) => Some(message)
    }
  }

  sealed abstract class SymbolReference
  case class ImportRef(arg: String) extends SymbolReference // Already contains <> or "" in C contexts
  case class DeclRef(decl: String, namespace: Option[String]) extends SymbolReference
}

abstract class Generator(spec: Spec)
{
  protected val writtenFiles = mutable.HashMap[String,String]()

  protected def createFile(folder: File, fileName: String, makeWriter: OutputStreamWriter => IndentWriter, f: IndentWriter => Unit): Unit = {
    if (spec.outFileListWriter.isDefined) {
      spec.outFileListWriter.get.write(new File(folder, fileName).getPath + "\n")
    }
    if (spec.skipGeneration) {
      return
    }

    val file = new File(folder, fileName)
    val cp = file.getCanonicalPath
    writtenFiles.put(cp.toLowerCase, cp) match {
      case Some(existing) =>
        if (existing == cp) {
          throw GenerateException("Refusing to write \"" + file.getPath + "\"; we already wrote a file to that path.")
        } else {
          throw GenerateException("Refusing to write \"" + file.getPath + "\"; we already wrote a file to a path that is the same when lower-cased: \"" + existing + "\".")
        }
      case _ =>
    }

    val fout = new FileOutputStream(file)
    try {
      val out = new OutputStreamWriter(fout, "UTF-8")
      f(makeWriter(out))
      out.flush()
    }
    finally {
      fout.close()
    }
  }

  protected def createFile(folder: File, fileName: String, f: IndentWriter => Unit): Unit = createFile(folder, fileName, out => new IndentWriter(out), f)

  implicit def identToString(ident: Ident): String = ident.name
  val idCpp = spec.cppIdentStyle
  val idJava = spec.javaIdentStyle
  val idObjc = spec.objcIdentStyle

  def wrapNamespace(w: IndentWriter, ns: String, f: IndentWriter => Unit) {
    ns match {
      case "" => f(w)
      case s =>
        val parts = s.split("::")
        w.wl(parts.map("namespace "+_+" {").mkString(" ")).wl
        f(w)
        w.wl
        w.wl(parts.map(p => "}").mkString(" ") + s"  // namespace $s")
    }
  }

  def wrapAnonymousNamespace(w: IndentWriter, f: IndentWriter => Unit) {
    w.wl("namespace { // anonymous namespace")
    w.wl
    f(w)
    w.wl
    w.wl("} // end anonymous namespace")
  }

  def writeHppFileGeneric(folder: File, namespace: String, fileIdentStyle: IdentConverter)(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit) {
    createFile(folder, fileIdentStyle(name) + "." + spec.cppHeaderExt, (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      w.wl("#pragma once")
      if (includes.nonEmpty) {
        w.wl
        includes.foreach(w.wl)
      }
      w.wl
      wrapNamespace(w, namespace,
        (w: IndentWriter) => {
          if (fwds.nonEmpty) {
            fwds.foreach(w.wl)
            w.wl
          }
          f(w)
        }
      )
      f2(w)
    })
  }

  def writeCppFileGeneric(folder: File, namespace: String, fileIdentStyle: IdentConverter, includePrefix: String)(name: String, origin: String, includes: Iterable[String], f: IndentWriter => Unit) {
    createFile(folder, fileIdentStyle(name) + "." + spec.cppExt, (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      val myHeader = q(includePrefix + fileIdentStyle(name) + "." + spec.cppHeaderExt)
      w.wl(s"#include $myHeader  // my header")
      val myHeaderInclude = s"#include $myHeader"
      for (include <- includes if include != myHeaderInclude)
        w.wl(include)
      w.wl
      wrapNamespace(w, namespace, f)
    })
  }

  def generate(idl: Seq[TypeDecl]) {
    for (td <- idl.collect { case itd: InternTypeDecl => itd }) td.body match {
      case e: Enum =>
        assert(td.params.isEmpty)
        generateEnum(td.origin, td.ident, td.doc, e)
      case r: Record => generateRecord(td.origin, td.ident, td.doc, td.params, r)
      case i: Interface => generateInterface(td.origin, td.ident, td.doc, td.params, i)
    }
  }

  def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum)
  def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record)
  def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface)

  // --------------------------------------------------------------------------
  // Render type expression

  def withNs(namespace: Option[String], t: String) = namespace match {
      case None => t
      case Some("") => "::" + t
      case Some(s) => "::" + s + "::" + t
    }

  def withCppNs(t: String) = withNs(Some(spec.cppNamespace), t)

  def writeAlignedCall(w: IndentWriter, call: String, params: Seq[Field], delim: String, end: String, f: Field => String): IndentWriter = {
    w.w(call)
    val skipFirst = new SkipFirst
    params.foreach(p => {
      skipFirst { w.wl(delim); w.w(" " * call.length()) }
      w.w(f(p))
    })
    w.w(end)
  }

  def writeAlignedCall(w: IndentWriter, call: String, params: Seq[Field], end: String, f: Field => String): IndentWriter =
    writeAlignedCall(w, call, params, ",", end, f)

  def writeAlignedObjcCall(w: IndentWriter, call: String, params: Seq[Field], end: String, f: Field => (String, String)) = {
    w.w(call)
    val skipFirst = new SkipFirst
    params.foreach(p => {
      val (name, value) = f(p)
      skipFirst { w.wl; w.w(" " * math.max(0, call.length() - name.length)); w.w(name)  }
      w.w(":" + value)
    })
    w.w(end)
  }

  def normalEnumOptions(e: Enum) = e.options.filter(_.specialFlag == None)

  def writeEnumOptionNone(w: IndentWriter, e: Enum, ident: IdentConverter) {
    for (o <- e.options.find(_.specialFlag == Some(Enum.SpecialFlag.NoFlags))) {
      writeDoc(w, o.doc)
      w.wl(ident(o.ident.name) + " = 0,")
    }
  }

  def writeEnumOptions(w: IndentWriter, e: Enum, ident: IdentConverter) {
    var shift = 0
    for (o <- normalEnumOptions(e)) {
      writeDoc(w, o.doc)
      w.wl(ident(o.ident.name) + (if(e.flags) s" = 1 << $shift" else "") + ",")
      shift += 1
    }
  }

  def writeEnumOptionAll(w: IndentWriter, e: Enum, ident: IdentConverter) {
    for (o <- e.options.find(_.specialFlag == Some(Enum.SpecialFlag.AllFlags))) {
      writeDoc(w, o.doc)
      w.w(ident(o.ident.name) + " = ")
      w.w(normalEnumOptions(e).map(o => ident(o.ident.name)).fold("0")((acc, o) => acc + " | " + o))
      w.wl(",")
    }
  }

  // --------------------------------------------------------------------------

  def writeDoc(w: IndentWriter, doc: Doc) {
    doc.lines.length match {
      case 0 =>
      case 1 =>
        w.wl(s"/**${doc.lines.head} */")
      case _ =>
        w.wl("/**")
        doc.lines.foreach (l => w.wl(s" *$l"))
        w.wl(" */")
    }
  }
}
