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
import java.io.{OutputStreamWriter, FileOutputStream, File}
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
                   javaIdentStyle: JavaIdentStyle,
                   javaCppException: Option[String],
                   javaAnnotation: Option[String],
                   cppOutFolder: Option[File],
                   cppHeaderOutFolder: Option[File],
                   cppIncludePrefix: String,
                   cppNamespace: Option[String],
                   cppIdentStyle: CppIdentStyle,
                   cppFileIdentStyle: IdentConverter,
                   cppOptionalTemplate: String,
                   cppOptionalHeader: String,
                   cppEnumHashWorkaround: Boolean,
                   jniOutFolder: Option[File],
                   jniHeaderOutFolder: Option[File],
                   jniIncludePrefix: String,
                   jniIncludeCppPrefix: String,
                   jniNamespace: String = "dropboxsync",
                   jniClassIdentStyle: IdentConverter,
                   jniFileIdentStyle: IdentConverter,
                   jniBaseLibIncludePrefix: String,
                   cppExt: String,
                   cppHeaderExt: String,
                   objcOutFolder: Option[File],
                   objcIdentStyle: ObjcIdentStyle,
                   objcFileIdentStyle: IdentConverter,
                   objcExt: String,
                   objcHeaderExt: String,
                   objcIncludePrefix: String,
                   objcIncludeCppPrefix: String,
                   objcppNamespace: String)

  def preComma(s: String) = {
    if (s.isEmpty) s else ", " + s
  }
  def q(s: String) = '"' + s + '"'
  def firstUpper(token: String) = token.charAt(0).toUpper + token.substring(1)

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
        createFolder("C++", spec.cppOutFolder.get)
        createFolder("C++ header", spec.cppHeaderOutFolder.get)
        new CppGenerator(spec).generate(idl)
      }
      if (spec.javaOutFolder.isDefined) {
        createFolder("Java", spec.javaOutFolder.get)
        new JavaGenerator(spec).generate(idl)
      }
      if (spec.jniOutFolder.isDefined) {
        createFolder("JNI C++", spec.jniOutFolder.get)
        createFolder("JNI C++ header", spec.jniHeaderOutFolder.get)
        new JNIGenerator(spec).generate(idl)
      }
      if (spec.objcOutFolder.isDefined) {
        createFolder("Objective-C[++]", spec.objcOutFolder.get)
        new ObjcGenerator(spec).generate(idl)
      }
      None
    }
    catch {
      case GenerateException(message) => Some(message)
    }
  }
}

abstract class Generator(spec: Spec)
{

  protected val writtenFiles = mutable.HashMap[String,String]()

  protected def createFile(folder: File, fileName: String, f: IndentWriter => Unit) {
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
      f(new IndentWriter(out))
      out.flush()
    }
    finally {
      fout.close()
    }
  }

  implicit def identToString(ident: Ident): String = ident.name
  val idCpp = spec.cppIdentStyle
  val idJava = spec.javaIdentStyle
  val idObjc = spec.objcIdentStyle

  def wrapNamespace(w: IndentWriter, ns: Option[String], f: IndentWriter => Unit) {
    ns match {
      case None => f(w)
      case Some(s) =>
        val parts = s.split("::")
        w.wl(parts.map("namespace "+_+" {").mkString(" ")).wl
        f(w)
        w.wl
        w.wl(parts.map(p => "}").mkString(" ") + s"  // namespace $s")
    }
  }

  def writeHppFileGeneric(folder: File, namespace: Option[String], fileIdentStyle: IdentConverter)(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit) {
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

  def writeCppFileGeneric(folder: File, namespace: Option[String], fileIdentStyle: IdentConverter, includePrefix: String)(name: String, origin: String, includes: Iterable[String], f: IndentWriter => Unit) {
    createFile(folder, fileIdentStyle(name) + "." + spec.cppExt, (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      val myHeader = q(includePrefix + fileIdentStyle(name) + "." + spec.cppHeaderExt)
      w.wl(s"#include $myHeader  // my header")
      includes.foreach(w.wl(_))
      w.wl
      wrapNamespace(w, namespace, f)
    })
  }

  def generate(idl: Seq[TypeDecl]) {
    for (td <- idl) {
      td.body match {
        case e: Enum =>
          assert(td.params.isEmpty)
          generateEnum(td.origin, td.ident, td.doc, e)
        case r: Record => generateRecord(td.origin, td.ident, td.doc, td.params, r)
        case i: Interface => generateInterface(td.origin, td.ident, td.doc, td.params, i)
      }
    }
  }

  def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum)
  def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record)
  def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface)

  // --------------------------------------------------------------------------
  // Render type expression

  def toCppType(ty: TypeRef, namespace: Option[String] = None): String = toCppType(ty.resolved, namespace)
  def toCppType(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta): String = m match {
      case p: MPrimitive => p.cName
      case MString => "std::string"
      case MBinary => "std::vector<uint8_t>"
      case MOptional => spec.cppOptionalTemplate
      case MList => "std::vector"
      case MSet => "std::unordered_set"
      case MMap => "std::unordered_map"
      case d: MDef =>
        d.defType match {
          case DEnum => withNs(namespace, idCpp.enumType(d.name))
          case DRecord => withNs(namespace, idCpp.ty(d.name))
          case DInterface => s"std::shared_ptr<${withNs(namespace, idCpp.ty(d.name))}>"
        }
      case p: MParam => idCpp.typeParam(p.name)
    }
    def expr(tm: MExpr): String = {
      val args = if (tm.args.isEmpty) "" else tm.args.map(expr).mkString("<", ", ", ">")
      base(tm.base) + args
    }
    expr(tm)
  }

  def withNs(namespace: Option[String], t: String) = namespace.fold(t)("::"+_+"::"+t)

  def withPackage(packageName: Option[String], t: String) = packageName.fold(t)(_+"."+t)

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
