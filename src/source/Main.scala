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

import java.io.{IOException, FileNotFoundException, FileInputStream, InputStreamReader, File, BufferedWriter, FileWriter}

import djinni.generatorTools._

object Main {

  def main(args: Array[String]) {
    var idlFile: File = null
    var idlIncludePaths: List[String] = List("")
    var cppOutFolder: Option[File] = None
    var cppNamespace: String = ""
    var cppIncludePrefix: String = ""
    var cppExtendedRecordIncludePrefix: String = ""
    var cppFileIdentStyle: IdentConverter = IdentStyle.underLower
    var cppOptionalTemplate: String = "std::optional"
    var cppOptionalHeader: String = "<optional>"
    var cppEnumHashWorkaround : Boolean = true
    var cppNnHeader: Option[String] = None
    var cppNnType: Option[String] = None
    var cppNnCheckExpression: Option[String] = None
    var cppUseWideStrings: Boolean = false
    var javaOutFolder: Option[File] = None
    var javaPackage: Option[String] = None
    var javaClassAccessModifier: JavaAccessModifier.Value = JavaAccessModifier.Public
    var javaCppException: Option[String] = None
    var javaAnnotation: Option[String] = None
    var javaNullableAnnotation: Option[String] = None
    var javaNonnullAnnotation: Option[String] = None
    var javaImplementAndroidOsParcelable : Boolean = false
    var javaUseFinalForRecord: Boolean = true
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
    var objcOutFolder: Option[File] = None
    var objcppOutFolder: Option[File] = None
    var objcppExt: String = "mm"
    var objcHeaderExt: String = "h"
    var objcIdentStyle = IdentStyle.objcDefault
    var objcTypePrefix: String = ""
    var objcIncludePrefix: String = ""
    var objcExtendedRecordIncludePrefix: String = ""
    var objcSwiftBridgingHeader: Option[String] = None
    var objcppIncludePrefix: String = ""
    var objcppIncludeCppPrefix: String = ""
    var objcppIncludeObjcPrefixOptional: Option[String] = None
    var objcFileIdentStyleOptional: Option[IdentConverter] = None
    var objcppNamespace: String = "djinni_generated"
    var objcBaseLibIncludePrefix: String = ""
    var inFileListPath: Option[File] = None
    var outFileListPath: Option[File] = None
    var skipGeneration: Boolean = false
    var yamlOutFolder: Option[File] = None
    var yamlOutFile: Option[String] = None
    var yamlPrefix: String = ""
	
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
      opt[String]("idl-include-path").valueName("<path> ...").optional().unbounded().foreach(x => idlIncludePaths = idlIncludePaths :+ x)
        .text("An include path to search for Djinni @import directives. Can specify multiple paths.")
      note("")
      opt[File]("java-out").valueName("<out-folder>").foreach(x => javaOutFolder = Some(x))
        .text("The output for the Java files (Generator disabled if unspecified).")
      opt[String]("java-package").valueName("...").foreach(x => javaPackage = Some(x))
        .text("The package name to use for generated Java classes.")
      opt[JavaAccessModifier.Value]("java-class-access-modifier").valueName("<public/package>").foreach(x => javaClassAccessModifier = x)
        .text("The access modifier to use for generated Java classes (default: public).")
      opt[String]("java-cpp-exception").valueName("<exception-class>").foreach(x => javaCppException = Some(x))
        .text("The type for translated C++ exceptions in Java (default: java.lang.RuntimeException that is not checked)")
      opt[String]("java-annotation").valueName("<annotation-class>").foreach(x => javaAnnotation = Some(x))
        .text("Java annotation (@Foo) to place on all generated Java classes")
      opt[String]("java-nullable-annotation").valueName("<nullable-annotation-class>").foreach(x => javaNullableAnnotation = Some(x))
        .text("Java annotation (@Nullable) to place on all fields and return values that are optional")
      opt[String]("java-nonnull-annotation").valueName("<nonnull-annotation-class>").foreach(x => javaNonnullAnnotation = Some(x))
        .text("Java annotation (@Nonnull) to place on all fields and return values that are not optional")
      opt[Boolean]("java-implement-android-os-parcelable").valueName("<true/false>").foreach(x => javaImplementAndroidOsParcelable = x)
        .text("all generated java classes will implement the interface android.os.Parcelable")
      opt[Boolean]("java-use-final-for-record").valueName("<use-final-for-record>").foreach(x => javaUseFinalForRecord = x)
        .text("Whether generated Java classes for records should be marked 'final' (default: true). ")
      note("")
      opt[File]("cpp-out").valueName("<out-folder>").foreach(x => cppOutFolder = Some(x))
        .text("The output folder for C++ files (Generator disabled if unspecified).")
      opt[File]("cpp-header-out").valueName("<out-folder>").foreach(x => cppHeaderOutFolderOptional = Some(x))
        .text("The output folder for C++ header files (default: the same as --cpp-out).")
      opt[String]("cpp-include-prefix").valueName("<prefix>").foreach(cppIncludePrefix = _)
        .text("The prefix for #includes of header files from C++ files.")
      opt[String]("cpp-namespace").valueName("...").foreach(x => cppNamespace = x)
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
      opt[String]("cpp-nn-header").valueName("<header>").foreach(x => cppNnHeader = Some(x))
        .text("The header to use for non-nullable pointers")
      opt[String]("cpp-nn-type").valueName("<header>").foreach(x => cppNnType = Some(x))
        .text("The type to use for non-nullable pointers (as a substitute for std::shared_ptr)")
      opt[String]("cpp-nn-check-expression").valueName("<header>").foreach(x => cppNnCheckExpression = Some(x))
        .text("The expression to use for building non-nullable pointers")
      opt[Boolean]( "cpp-use-wide-strings").valueName("<true/false>").foreach(x => cppUseWideStrings = x)
        .text("Use wide strings in C++ code (default: false)")
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
      opt[String]("objc-h-ext").valueName("<ext>").foreach(objcHeaderExt = _)
        .text("The filename extension for Objective-C[++] header files (default: \"h\")")
      opt[String]("objc-type-prefix").valueName("<pre>").foreach(objcTypePrefix = _)
        .text("The prefix for Objective-C data types (usually two or three letters)")
      opt[String]("objc-include-prefix").valueName("<prefix>").foreach(objcIncludePrefix = _)
        .text("The prefix for #import of header files from Objective-C files.")
      opt[String]("objc-swift-bridging-header").valueName("<name>").foreach(x => objcSwiftBridgingHeader = Some(x))
        .text("The name of Objective-C Bridging Header used in XCode's Swift projects.")
      note("")
      opt[File]("objcpp-out").valueName("<out-folder>").foreach(x => objcppOutFolder = Some(x))
        .text("The output folder for private Objective-C++ files (Generator disabled if unspecified).")
      opt[String]("objcpp-ext").valueName("<ext>").foreach(objcppExt = _)
        .text("The filename extension for Objective-C++ files (default: \"mm\")")
      opt[String]("objcpp-include-prefix").valueName("<prefix>").foreach(objcppIncludePrefix = _)
        .text("The prefix for #import of Objective-C++ header files from Objective-C++ files.")
      opt[String]("objcpp-include-cpp-prefix").valueName("<prefix>").foreach(objcppIncludeCppPrefix = _)
        .text("The prefix for #include of the main C++ header files from Objective-C++ files.")
      opt[String]("objcpp-include-objc-prefix").valueName("<prefix>").foreach(x => objcppIncludeObjcPrefixOptional = Some(x))
        .text("The prefix for #import of the Objective-C header files from Objective-C++ files (default: the same as --objcpp-include-prefix)")
      opt[String]("cpp-extended-record-include-prefix").valueName("<prefix>").foreach(cppExtendedRecordIncludePrefix = _)
        .text("The prefix path for #include of the extended record C++ header (.hpp) files")
      opt[String]("objc-extended-record-include-prefix").valueName("<prefix>").foreach(objcExtendedRecordIncludePrefix = _)
        .text("The prefix path for #import of the extended record Objective-C header (.h) files")
      opt[String]("objcpp-namespace").valueName("<prefix>").foreach(objcppNamespace = _)
        .text("The namespace name to use for generated Objective-C++ classes.")
      opt[String]("objc-base-lib-include-prefix").valueName("...").foreach(x => objcBaseLibIncludePrefix = x)
        .text("The Objective-C++ base library's include path, relative to the Objective-C++ classes.")
      note("")
      opt[File]("yaml-out").valueName("<out-folder>").foreach(x => yamlOutFolder = Some(x))
        .text("The output folder for YAML files (Generator disabled if unspecified).")
      opt[String]("yaml-out-file").valueName("<out-file>").foreach(x => yamlOutFile = Some(x))
        .text("If specified all types are merged into a single YAML file instead of generating one file per type (relative to --yaml-out).")
      opt[String]("yaml-prefix").valueName("<pre>").foreach(yamlPrefix = _)
        .text("The prefix to add to type names stored in YAML files (default: \"\").")
      note("")
      opt[File]("list-in-files").valueName("<list-in-files>").foreach(x => inFileListPath = Some(x))
        .text("Optional file in which to write the list of input files parsed.")
      opt[File]("list-out-files").valueName("<list-out-files>").foreach(x => outFileListPath = Some(x))
        .text("Optional file in which to write the list of output files produced.")
      opt[Boolean]("skip-generation").valueName("<true/false>").foreach(x => skipGeneration = x)
        .text("Way of specifying if file generation should be skipped (default: false)")

      note("\nIdentifier styles (ex: \"FooBar\", \"fooBar\", \"foo_bar\", \"FOO_BAR\", \"m_fooBar\")\n")
      identStyle("ident-java-enum",      c => { javaIdentStyle = javaIdentStyle.copy(enum = c) })
      identStyle("ident-java-field",     c => { javaIdentStyle = javaIdentStyle.copy(field = c) })
      identStyle("ident-java-type",      c => { javaIdentStyle = javaIdentStyle.copy(ty = c) })
      identStyle("ident-cpp-enum",       c => { cppIdentStyle = cppIdentStyle.copy(enum = c) })
      identStyle("ident-cpp-field",      c => { cppIdentStyle = cppIdentStyle.copy(field = c) })
      identStyle("ident-cpp-method",     c => { cppIdentStyle = cppIdentStyle.copy(method = c) })
      identStyle("ident-cpp-type",       c => { cppIdentStyle = cppIdentStyle.copy(ty = c) })
      identStyle("ident-cpp-enum-type",  c => { cppTypeEnumIdentStyle = c })
      identStyle("ident-cpp-type-param", c => { cppIdentStyle = cppIdentStyle.copy(typeParam = c) })
      identStyle("ident-cpp-local",      c => { cppIdentStyle = cppIdentStyle.copy(local = c) })
      identStyle("ident-cpp-file",       c => { cppFileIdentStyle = c })
      identStyle("ident-jni-class",      c => { jniClassIdentStyleOptional = Some(c)})
      identStyle("ident-jni-file",       c => { jniFileIdentStyleOptional = Some(c)})
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
    val objcppIncludeObjcPrefix = objcppIncludeObjcPrefixOptional.getOrElse(objcppIncludePrefix)

    // Add ObjC prefix to identstyle
    objcIdentStyle = objcIdentStyle.copy(ty = IdentStyle.prefix(objcTypePrefix,objcIdentStyle.ty))
    objcFileIdentStyle = IdentStyle.prefix(objcTypePrefix, objcFileIdentStyle)

    if (cppTypeEnumIdentStyle != null) {
      cppIdentStyle = cppIdentStyle.copy(enumType = cppTypeEnumIdentStyle)
    }

    // Parse IDL file.
    System.out.println("Parsing...")
    val inFileListWriter = if (inFileListPath.isDefined) {
      if (inFileListPath.get.getParentFile != null)
        createFolder("input file list", inFileListPath.get.getParentFile)
      Some(new BufferedWriter(new FileWriter(inFileListPath.get)))
    } else {
      None
    }
    val idl = try {
      (new Parser(idlIncludePaths)).parseFile(idlFile, inFileListWriter)
    }
    catch {
      case ex @ (_: FileNotFoundException | _: IOException) =>
        System.err.println("Error reading from --idl file: " + ex.getMessage)
        System.exit(1); return
    }
    finally {
      if (inFileListWriter.isDefined) {
        inFileListWriter.get.close()
      }
    }

    // Resolve names in IDL file, check types.
    System.out.println("Resolving...")
    resolver.resolve(meta.defaults, idl) match {
      case Some(err) =>
        System.err.println(err)
        System.exit(1); return
      case _ =>
    }

    System.out.println("Generating...")
    val outFileListWriter = if (outFileListPath.isDefined) {
      if (outFileListPath.get.getParentFile != null)
        createFolder("output file list", outFileListPath.get.getParentFile)
      Some(new BufferedWriter(new FileWriter(outFileListPath.get)))
    } else {
      None
    }
    val objcSwiftBridgingHeaderWriter = if (objcSwiftBridgingHeader.isDefined && objcOutFolder.isDefined) {
      val objcSwiftBridgingHeaderFile = new File(objcOutFolder.get.getPath, objcSwiftBridgingHeader.get + ".h")
      if (objcSwiftBridgingHeaderFile.getParentFile != null)
        createFolder("output file list", objcSwiftBridgingHeaderFile.getParentFile)
      Some(new BufferedWriter(new FileWriter(objcSwiftBridgingHeaderFile)))
    } else {
      None
    }

    val outSpec = Spec(
      javaOutFolder,
      javaPackage,
      javaClassAccessModifier,
      javaIdentStyle,
      javaCppException,
      javaAnnotation,
      javaNullableAnnotation,
      javaNonnullAnnotation,
      javaImplementAndroidOsParcelable,
      javaUseFinalForRecord,
      cppOutFolder,
      cppHeaderOutFolder,
      cppIncludePrefix,
      cppExtendedRecordIncludePrefix,
      cppNamespace,
      cppIdentStyle,
      cppFileIdentStyle,
      cppOptionalTemplate,
      cppOptionalHeader,
      cppEnumHashWorkaround,
      cppNnHeader,
      cppNnType,
      cppNnCheckExpression,
      cppUseWideStrings,
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
      objcppOutFolder,
      objcIdentStyle,
      objcFileIdentStyle,
      objcppExt,
      objcHeaderExt,
      objcIncludePrefix,
      objcExtendedRecordIncludePrefix,
      objcppIncludePrefix,
      objcppIncludeCppPrefix,
      objcppIncludeObjcPrefix,
      objcppNamespace,
      objcBaseLibIncludePrefix,
      objcSwiftBridgingHeaderWriter,
      outFileListWriter,
      skipGeneration,
      yamlOutFolder,
      yamlOutFile,
      yamlPrefix)


    try {
      val r = generate(idl, outSpec)
      r.foreach(e => System.err.println("Error generating output: " + e))
    }
    finally {
      if (outFileListWriter.isDefined) {
        outFileListWriter.get.close()
      }
      if (objcSwiftBridgingHeaderWriter.isDefined) {
        objcSwiftBridgingHeaderWriter.get.close()
      }
    }
  }
}
