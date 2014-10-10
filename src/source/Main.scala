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

import java.io.{IOException, FileInputStream, InputStreamReader, File}

import djinni.generatorTools._

object Main {

  def main(args: Array[String]) {
    var idlFile: File = null
    var cppOutFolder: Option[File] = None
    var cppNamespace: Option[String] = None
    var cppIncludePrefix: String = ""
    var cppFileIdentStyle: IdentConverter = IdentStyle.underLower
    var cppOptionalTemplate: String = "std::optional"
    var cppOptionalHeader: String = "<optional>"
    var cppEnumHashWorkaround : Boolean = true
    var javaOutFolder: Option[File] = None
    var javaPackage: Option[String] = None
    var javaCppException: Option[String] = None
    var javaAnnotation: Option[String] = None
    var objcOutFolder: Option[File] = None
    var jniOutFolder: Option[File] = None
    var jniHeaderOutFolderOptional: Option[File] = None
    var jniNamespace: String = "djinni_generated"
    var jniClassIdentStyleOptional: Option[IdentConverter] = None
    var jniIncludePrefix: String = ""
    var jniIncludeCppPrefix: String = ""
    var jniFileIdentStyleOptional: Option[IdentConverter] = None
    var jniBaseLibClassIdentStyleOptional: Option[IdentConverter] = None
    var jniBaseLibIncludePrefix: String = ""
    var cppHeaderOutFolderOptional: Option[File] = None
    var cppExt: String = "cpp"
    var cppHeaderExt: String = "hpp"
    var javaIdentStyle = IdentStyle.javaDefault
    var cppIdentStyle = IdentStyle.cppDefault
    var cppTypeEnumIdentStyle: IdentConverter = null
    var objcExt: String = "mm"
    var objcHeaderExt: String = "h"
    var objcIdentStyle = IdentStyle.objcDefault
    var objcTypePrefix: String = ""
    var objcIncludePrefix: String = ""
    var objcIncludeCppPrefix: String = ""
    var objcFileIdentStyleOptional: Option[IdentConverter] = None
    var objcppNamespace: String = "dropboxsync"

    val argParser = new scopt.OptionParser[Unit]("djinni") {

      def identStyle(optionName: String, update: IdentConverter => Unit) = {
        opt[String](optionName).valueName("...").foreach(spec =>
          IdentStyle.infer(spec) match {
            case None => failure("invalid ident spec: \"" + spec + "\"")
            case Some(func) => update(func)
          }
        )
      }

      override def showUsageOnError = false
      help("help")
      opt[File]("idl").valueName("<in-file>").required().foreach(idlFile = _)
        .text("The IDL file with the type definitions, typically with extension \".djinni\".")
      note("")
      opt[File]("java-out").valueName("<out-folder>").foreach(x => javaOutFolder = Some(x))
        .text("The output for the Java files (Generator disabled if unspecified).")
      opt[String]("java-package").valueName("...").foreach(x => javaPackage = Some(x))
        .text("The package name to use for generated Java classes.")
      opt[String]("java-cpp-exception").valueName("<exception-class>").foreach(x => javaCppException = Some(x))
        .text("The type for translated C++ exceptions in Java (default: java.lang.RuntimeException that is not checked)")
      opt[String]("java-annotation").valueName("<annotation-class>").foreach(x => javaAnnotation = Some(x))
        .text("Java annotation (@Foo) to place on all generated Java classes")
      note("")
      opt[File]("cpp-out").valueName("<out-folder>").foreach(x => cppOutFolder = Some(x))
        .text("The output folder for C++ files (Generator disabled if unspecified).")
      opt[File]("cpp-header-out").valueName("<out-folder>").foreach(x => cppHeaderOutFolderOptional = Some(x))
        .text("The output folder for C++ header files (default: the same as --cpp-out).")
      opt[String]("cpp-include-prefix").valueName("<prefix>").foreach(cppIncludePrefix = _)
        .text("The prefix for #includes of header files from C++ files.")
      opt[String]("cpp-namespace").valueName("...").foreach(x => cppNamespace = Some(x))
        .text("The namespace name to use for generated C++ classes.")
      opt[String]("cpp-ext").valueName("<ext>").foreach(cppExt = _)
        .text("The filename extension for C++ files (default: \"cpp\").")
      opt[String]("hpp-ext").valueName("<ext>").foreach(cppHeaderExt = _)
        .text("The filename extension for C++ header files (default: \"hpp\").")
      opt[String]("cpp-optional-template").valueName("<template>").foreach(x => cppOptionalTemplate = x)
        .text("The template to use for optional values (default: \"std::optional\")")
      opt[String]("cpp-optional-header").valueName("<header>").foreach(x => cppOptionalHeader = x)
        .text("The header to use for optional values (default: \"<optional>\")")
      opt[Boolean]("cpp-enum-hash-workaround").valueName("<true/false>").foreach(x => cppEnumHashWorkaround = x)
        .text("Work around LWG-2148 by generating std::hash specializations for C++ enums (default: true)")
      note("")
      opt[File]("jni-out").valueName("<out-folder>").foreach(x => jniOutFolder = Some(x))
        .text("The folder for the JNI C++ output files (Generator disabled if unspecified).")
      opt[File]("jni-header-out").valueName("<out-folder>").foreach(x => jniHeaderOutFolderOptional = Some(x))
        .text("The folder for the JNI C++ header files (default: the same as --jni-out).")
      opt[String]("jni-include-prefix").valueName("<prefix>").foreach(jniIncludePrefix = _)
        .text("The prefix for #includes of JNI header files from JNI C++ files.")
      opt[String]("jni-include-cpp-prefix").valueName("<prefix>").foreach(jniIncludeCppPrefix = _)
        .text("The prefix for #includes of the main header files from JNI C++ files.")
      opt[String]("jni-namespace").valueName("...").foreach(x => jniNamespace = x)
        .text("The namespace name to use for generated JNI C++ classes.")
      opt[String]("jni-base-lib-include-prefix").valueName("...").foreach(x => jniBaseLibIncludePrefix = x)
        .text("The JNI base library's include path, relative to the JNI C++ classes.")
      note("")
      opt[File]("objc-out").valueName("<out-folder>").foreach(x => objcOutFolder = Some(x))
        .text("The output folder for Objective-C files (Generator disabled if unspecified).")
      opt[String]("objc-ext").valueName("<ext>").foreach(objcExt = _)
        .text("The filename extension for Objective-C files (default: \"mm\")")
      opt[String]("objc-h-ext").valueName("<ext>").foreach(objcHeaderExt = _)
        .text("The filename extension for Objective-C header files (default: \"h\")")
      opt[String]("objc-type-prefix").valueName("<pre>").foreach(objcTypePrefix = _)
        .text("The prefix for Objective-C data types (usually two or three letters)")
      opt[String]("objc-include-prefix").valueName("<prefix>").foreach(objcIncludePrefix = _)
        .text("The prefix for #import of header files from Objective-C files.")
      opt[String]("objc-include-cpp-prefix").valueName("<prefix>").foreach(objcIncludeCppPrefix = _)
        .text("The prefix for #include of the main header files from Objective-C files.")
      opt[String]("objcpp-namespace").valueName("<prefix>").foreach(objcppNamespace = _)
        .text("Namespace for C++ objects defined in Objective-C++, such as wrapper caches")

      note("\nIdentifier styles (ex: \"FooBar\", \"fooBar\", \"foo_bar\", \"FOO_BAR\", \"m_fooBar\")\n")
      identStyle("ident-java-enum",      c => { javaIdentStyle = javaIdentStyle.copy(enum = c) })
      identStyle("ident-java-field",     c => { javaIdentStyle = javaIdentStyle.copy(field = c) })
      identStyle("ident-cpp-enum",       c => { cppIdentStyle = cppIdentStyle.copy(enum = c) })
      identStyle("ident-cpp-field",      c => { cppIdentStyle = cppIdentStyle.copy(field = c) })
      identStyle("ident-cpp-method",     c => { cppIdentStyle = cppIdentStyle.copy(method = c) })
      identStyle("ident-cpp-type",       c => { cppIdentStyle = cppIdentStyle.copy(ty = c) })
      identStyle("ident-cpp-enum-type",  c => { cppTypeEnumIdentStyle = c })
      identStyle("ident-cpp-type-param", c => { cppIdentStyle = cppIdentStyle.copy(typeParam = c) })
      identStyle("ident-cpp-local",      c => { cppIdentStyle = cppIdentStyle.copy(local = c) })
      identStyle("ident-cpp-file",       c => { cppFileIdentStyle = c })
      identStyle("ident-jni-class",           c => {jniClassIdentStyleOptional = Some(c)})
      identStyle("ident-jni-file",            c => {jniFileIdentStyleOptional = Some(c)})
      identStyle("ident-objc-enum",       c => { objcIdentStyle = objcIdentStyle.copy(enum = c) })
      identStyle("ident-objc-field",      c => { objcIdentStyle = objcIdentStyle.copy(field = c) })
      identStyle("ident-objc-method",     c => { objcIdentStyle = objcIdentStyle.copy(method = c) })
      identStyle("ident-objc-type",       c => { objcIdentStyle = objcIdentStyle.copy(ty = c) })
      identStyle("ident-objc-type-param", c => { objcIdentStyle = objcIdentStyle.copy(typeParam = c) })
      identStyle("ident-objc-local",      c => { objcIdentStyle = objcIdentStyle.copy(local = c) })
      identStyle("ident-objc-file",       c => { objcFileIdentStyleOptional = Some(c) })

    }

    if (!argParser.parse(args)) {
      System.exit(1); return
    }

    val cppHeaderOutFolder = if (cppHeaderOutFolderOptional.isDefined) cppHeaderOutFolderOptional else cppOutFolder
    val jniHeaderOutFolder = if (jniHeaderOutFolderOptional.isDefined) jniHeaderOutFolderOptional else jniOutFolder
    val jniClassIdentStyle = jniClassIdentStyleOptional.getOrElse(cppIdentStyle.ty)
    val jniBaseLibClassIdentStyle = jniBaseLibClassIdentStyleOptional.getOrElse(jniClassIdentStyle)
    val jniFileIdentStyle = jniFileIdentStyleOptional.getOrElse(cppFileIdentStyle)
    var objcFileIdentStyle = objcFileIdentStyleOptional.getOrElse(objcIdentStyle.ty)

    // Add ObjC prefix to identstyle
    objcIdentStyle = objcIdentStyle.copy(ty = IdentStyle.prefix(objcTypePrefix,objcIdentStyle.ty))
    objcFileIdentStyle = IdentStyle.prefix(objcTypePrefix, objcFileIdentStyle)

    if (cppTypeEnumIdentStyle != null) {
      cppIdentStyle = cppIdentStyle.copy(enumType = cppTypeEnumIdentStyle)
    }

    // Parse IDL file.
    System.out.println("Parsing...")
    val idl = try {
      (new Parser).parseFile(idlFile)
    }
    catch {
      case ex: IOException =>
        System.err.println("Error reading from --idl file: " + ex.getMessage)
        System.exit(1); return
    }

    // Resolve names in IDL file, check types.
    System.out.println("Resolving...")
    resolver.resolve(meta.defaults, idl) match {
      case Some(err) =>
        System.err.println(err)
        System.exit(1); return
      case _ =>
    }

    val outSpec = Spec(
      javaOutFolder,
      javaPackage,
      javaIdentStyle,
      javaCppException,
      javaAnnotation,
      cppOutFolder,
      cppHeaderOutFolder,
      cppIncludePrefix,
      cppNamespace,
      cppIdentStyle,
      cppFileIdentStyle,
      cppOptionalTemplate,
      cppOptionalHeader,
      cppEnumHashWorkaround,
      jniOutFolder,
      jniHeaderOutFolder,
      jniIncludePrefix,
      jniIncludeCppPrefix,
      jniNamespace,
      jniClassIdentStyle,
      jniFileIdentStyle,
      jniBaseLibIncludePrefix,
      cppExt,
      cppHeaderExt,
      objcOutFolder,
      objcIdentStyle,
      objcFileIdentStyle,
      objcExt,
      objcHeaderExt,
      objcIncludePrefix,
      objcIncludeCppPrefix,
      objcppNamespace)

    System.out.println("Generating...")
    val r = generate(idl, outSpec)
    r.foreach(e => System.err.println("Error generating output: " + e))
  }
}
