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
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter

import scala.collection.mutable

class JNIGenerator(spec: Spec) extends Generator(spec) {

  val jniMarshal = new JNIMarshal(spec)
  val cppMarshal = new CppMarshal(spec)
  val javaMarshal = new JavaMarshal(spec)
  val jniBaseLibClassIdentStyle = IdentStyle.prefix("H", IdentStyle.camelUpper)
  val jniBaseLibFileIdentStyle = jniBaseLibClassIdentStyle

  val writeJniCppFile = writeCppFileGeneric(spec.jniOutFolder.get, spec.jniNamespace, spec.jniFileIdentStyle, spec.jniIncludePrefix) _
  def writeJniHppFile(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit = (w => {})) =
    writeHppFileGeneric(spec.jniHeaderOutFolder.get, spec.jniNamespace, spec.jniFileIdentStyle)(name, origin, includes, fwds, f, f2)

  class JNIRefs(name: String, cppPrefixOverride: Option[String]=None) {
    var jniHpp = mutable.TreeSet[String]()
    var jniCpp = mutable.TreeSet[String]()

    val cppPrefix = cppPrefixOverride.getOrElse(spec.jniIncludeCppPrefix)
    jniHpp.add("#include " + q(cppPrefix + spec.cppFileIdentStyle(name) + "." + spec.cppHeaderExt))
    jniHpp.add("#include " + q(spec.jniBaseLibIncludePrefix + "djinni_support.hpp"))
    spec.cppNnHeader match {
      case Some(nnHdr) => jniHpp.add("#include " + nnHdr)
      case _ =>
    }

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.foreach(find)
      find(tm.base)
    }
    def find(m: Meta) = for(r <- jniMarshal.references(m, name)) r match {
      case ImportRef(arg) => jniCpp.add("#include " + arg)
      case _ =>
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new JNIRefs(ident.name)
    val jniHelper = jniMarshal.helperClass(ident)
    val cppSelf = cppMarshal.fqTypename(ident, e)

    writeJniHppFile(ident, origin, Iterable.concat(refs.jniHpp, refs.jniCpp), Nil, w => {
      w.w(s"class $jniHelper final : ::djinni::JniEnum").bracedSemi {
        w.wlOutdent("public:")
        w.wl(s"using CppType = $cppSelf;")
        w.wl(s"using JniType = jobject;")
        w.wl
        w.wl(s"using Boxed = $jniHelper;")
        w.wl
        w.wl(s"static CppType toCpp(JNIEnv* jniEnv, JniType j) { return static_cast<CppType>(::djinni::JniClass<$jniHelper>::get().ordinal(jniEnv, j)); }")
        w.wl(s"static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, CppType c) { return ::djinni::JniClass<$jniHelper>::get().create(jniEnv, static_cast<jint>(c)); }")
        w.wl
        w.wlOutdent("private:")
        val classLookup = q(jniMarshal.undecoratedTypename(ident, e))
        w.wl(s"$jniHelper() : JniEnum($classLookup) {}")
        w.wl(s"friend ::djinni::JniClass<$jniHelper>;")
      }
    })
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val prefixOverride: Option[String] = if (r.ext.cpp) {
      Some(spec.cppExtendedRecordIncludePrefix)
    } else {
      None
    }
    val refs = new JNIRefs(ident.name, prefixOverride)
    r.fields.foreach(f => refs.find(f.ty))

    val jniHelper = jniMarshal.helperClass(ident)
    val cppSelf = cppMarshal.fqTypename(ident, r) + cppTypeArgs(params)

    def writeJniPrototype(w: IndentWriter) {
      writeJniTypeParams(w, params)
      w.w(s"class $jniHelper final").bracedSemi {
        w.wlOutdent("public:")
        w.wl(s"using CppType = $cppSelf;")
        w.wl(s"using JniType = jobject;")
        w.wl
        w.wl(s"using Boxed = $jniHelper;")
        w.wl
        w.wl(s"~$jniHelper();")
        w.wl
        w.wl(s"static CppType toCpp(JNIEnv* jniEnv, JniType j);")
        w.wl(s"static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c);")
        w.wl
        w.wlOutdent("private:")
        w.wl(s"$jniHelper();")
        w.wl(s"friend ::djinni::JniClass<$jniHelper>;")
        w.wl
        val classLookup = q(jniMarshal.undecoratedTypename(ident, r))
        w.wl(s"const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass($classLookup) };")
        val constructorSig = q(jniMarshal.javaMethodSignature(r.fields, None))
        w.wl(s"const jmethodID jconstructor { ::djinni::jniGetMethodID(clazz.get(), ${q("<init>")}, $constructorSig) };")
        for (f <- r.fields) {
          val javaFieldName = idJava.field(f.ident)
          val javaSig = q(jniMarshal.fqTypename(f.ty))
          w.wl(s"const jfieldID field_$javaFieldName { ::djinni::jniGetFieldID(clazz.get(), ${q(javaFieldName)}, $javaSig) };")
        }
      }
    }

    def writeJniBody(w: IndentWriter) {
      val jniHelperWithParams = jniHelper + typeParamsSignature(params)
      // Defining ctor/dtor in the cpp file reduces build times
      writeJniTypeParams(w, params)
      w.wl(s"$jniHelperWithParams::$jniHelper() = default;")
      w.wl
      writeJniTypeParams(w, params)
      w.wl(s"$jniHelperWithParams::~$jniHelper() = default;")
      w.wl

      writeJniTypeParams(w, params)
      w.w(s"auto $jniHelperWithParams::fromCpp(JNIEnv* jniEnv, const CppType& c) -> ::djinni::LocalRef<JniType>").braced{
        //w.wl(s"::${spec.jniNamespace}::JniLocalScope jscope(jniEnv, 10);")
        if(r.fields.isEmpty) w.wl("(void)c; // Suppress warnings in release builds for empty records")
        w.wl(s"const auto& data = ::djinni::JniClass<$jniHelper>::get();")
        val call = "auto r = ::djinni::LocalRef<JniType>{jniEnv->NewObject("
        w.w(call + "data.clazz.get(), data.jconstructor")
        if(r.fields.nonEmpty) {
          w.wl(",")
          writeAlignedCall(w, " " * call.length(), r.fields, ")}", f => {
            val name = idCpp.field(f.ident)
            val param = jniMarshal.fromCpp(f.ty, s"c.$name")
            s"::djinni::get($param)"
          })
        }
        else
          w.w(")}")
        w.wl(";")
        w.wl(s"::djinni::jniExceptionCheck(jniEnv);")
        w.wl(s"return r;")
      }
      w.wl
      writeJniTypeParams(w, params)
      w.w(s"auto $jniHelperWithParams::toCpp(JNIEnv* jniEnv, JniType j) -> CppType").braced {
        w.wl(s"::djinni::JniLocalScope jscope(jniEnv, ${r.fields.size + 1});")
        w.wl(s"assert(j != nullptr);")
        if(r.fields.isEmpty)
          w.wl("(void)j; // Suppress warnings in release builds for empty records")
        else
          w.wl(s"const auto& data = ::djinni::JniClass<$jniHelper>::get();")
        writeAlignedCall(w, "return {", r.fields, "}", f => {
          val fieldId = "data.field_" + idJava.field(f.ident)
          val jniFieldAccess = toJniCall(f.ty, (jt: String) => s"jniEnv->Get${jt}Field(j, $fieldId)")
          jniMarshal.toCpp(f.ty, jniFieldAccess)
        })
        w.wl(";")
      }
    }
    writeJniFiles(origin, params.nonEmpty, ident, refs, writeJniPrototype, writeJniBody)
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new JNIRefs(ident.name)
    i.methods.foreach(m => {
      m.params.foreach(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.foreach(c => {
      refs.find(c.ty)
    })

    val jniSelf = jniMarshal.helperClass(ident)
    val cppSelf = cppMarshal.fqTypename(ident, i) + cppTypeArgs(typeParams)

    val classLookup = jniMarshal.undecoratedTypename(ident, i)
    val baseType = s"::djinni::JniInterface<$cppSelf, $jniSelf>"

    def writeJniPrototype(w: IndentWriter) {
      writeJniTypeParams(w, typeParams)
      w.w(s"class $jniSelf final : $baseType").bracedSemi {
        w.wlOutdent(s"public:")
        spec.cppNnType match {
          case Some(nnPtr) =>
            w.wl(s"using CppType = ${nnPtr}<$cppSelf>;")
            w.wl(s"using CppOptType = std::shared_ptr<$cppSelf>;")
          case _ =>
            w.wl(s"using CppType = std::shared_ptr<$cppSelf>;")
            w.wl(s"using CppOptType = std::shared_ptr<$cppSelf>;")
        }
        w.wl(s"using JniType = jobject;")
        w.wl
        w.wl(s"using Boxed = $jniSelf;")
        w.wl
        w.wl(s"~$jniSelf();")
        w.wl
        if (spec.cppNnType.nonEmpty) {
          def nnCheck(expr: String): String = spec.cppNnCheckExpression.fold(expr)(check => s"$check($expr)")
          w.w("static CppType toCpp(JNIEnv* jniEnv, JniType j)").bracedSemi {
            w.wl(s"""DJINNI_ASSERT_MSG(j, jniEnv, "$jniSelf::fromCpp requires a non-null Java object");""")
            w.wl(s"""return ${nnCheck(s"::djinni::JniClass<$jniSelf>::get()._fromJava(jniEnv, j)")};""")
          }
        } else {
          w.wl(s"static CppType toCpp(JNIEnv* jniEnv, JniType j) { return ::djinni::JniClass<$jniSelf>::get()._fromJava(jniEnv, j); }")
        }
        w.wl(s"static ::djinni::LocalRef<JniType> fromCppOpt(JNIEnv* jniEnv, const CppOptType& c) { return {jniEnv, ::djinni::JniClass<$jniSelf>::get()._toJava(jniEnv, c)}; }")
        w.wl(s"static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c) { return fromCppOpt(jniEnv, c); }")
        w.wl
        w.wlOutdent("private:")
        w.wl(s"$jniSelf();")
        w.wl(s"friend ::djinni::JniClass<$jniSelf>;")
        w.wl(s"friend $baseType;")
        w.wl
        if (i.ext.java) {
          w.wl(s"class JavaProxy final : ::djinni::JavaProxyHandle<JavaProxy>, public $cppSelf").bracedSemi {
            w.wlOutdent(s"public:")
            w.wl(s"JavaProxy(JniType j);")
            w.wl(s"~JavaProxy();")
            w.wl
            for (m <- i.methods) {
              val ret = cppMarshal.fqReturnType(m.ret)
              val params = m.params.map(p => cppMarshal.fqParamType(p.ty) + " " + idCpp.local(p.ident))
              w.wl(s"$ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")} override;")
            }
            w.wl
            w.wlOutdent(s"private:")
            w.wl(s"friend ::djinni::JniInterface<$cppSelf, ${withNs(Some(spec.jniNamespace), jniSelf)}>;")
          }
          w.wl
          w.wl(s"const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass(${q(classLookup)}) };")
          for (m <- i.methods) {
            val javaMethodName = idJava.method(m.ident)
            val javaMethodSig = q(jniMarshal.javaMethodSignature(m.params, m.ret))
            w.wl(s"const jmethodID method_$javaMethodName { ::djinni::jniGetMethodID(clazz.get(), ${q(javaMethodName)}, $javaMethodSig) };")
          }
        }
      }
    }

    def writeJniBody(w: IndentWriter) {
      // Defining ctor/dtor in the cpp file reduces build times
      val baseClassParam = if (i.ext.cpp) q(classLookup+"$CppProxy") else ""
      val jniSelfWithParams = jniSelf + typeParamsSignature(typeParams)
      writeJniTypeParams(w, typeParams)
      w.wl(s"$jniSelfWithParams::$jniSelf() : $baseType($baseClassParam) {}")
      w.wl
      writeJniTypeParams(w, typeParams)
      w.wl(s"$jniSelfWithParams::~$jniSelf() = default;")
      w.wl
      if (i.ext.java) {
        writeJniTypeParams(w, typeParams)
        w.wl(s"$jniSelfWithParams::JavaProxy::JavaProxy(JniType j) : Handle(::djinni::jniGetThreadEnv(), j) { }")
        w.wl
        writeJniTypeParams(w, typeParams)
        w.wl(s"$jniSelfWithParams::JavaProxy::~JavaProxy() = default;")
        w.wl
        for (m <- i.methods) {
          val ret = cppMarshal.fqReturnType(m.ret)
          val params = m.params.map(p => cppMarshal.fqParamType(p.ty) + " c_" + idCpp.local(p.ident))
          writeJniTypeParams(w, typeParams)
          val methodNameAndSignature: String = s"${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")}"
          w.w(s"$ret $jniSelfWithParams::JavaProxy::$methodNameAndSignature").braced {
            w.wl(s"auto jniEnv = ::djinni::jniGetThreadEnv();")
            w.wl(s"::djinni::JniLocalScope jscope(jniEnv, 10);")
            w.wl(s"const auto& data = ::djinni::JniClass<${withNs(Some(spec.jniNamespace), jniSelf)}>::get();")
            val call = m.ret.fold("jniEnv->CallVoidMethod(")(r => "auto jret = " + toJniCall(r, (jt: String) => s"jniEnv->Call${jt}Method("))
            w.w(call)
            val javaMethodName = idJava.method(m.ident)
            w.w(s"Handle::get().get(), data.method_$javaMethodName")
            if(m.params.nonEmpty){
              w.wl(",")
              writeAlignedCall(w, " " * call.length(), m.params, ")", p => {
                val param = jniMarshal.fromCpp(p.ty, "c_" + idCpp.local(p.ident))
                s"::djinni::get($param)"
              })
            }
            else
              w.w(")")
            w.wl(";")
            w.wl(s"::djinni::jniExceptionCheck(jniEnv);")
            m.ret.fold()(ty => {
              (spec.cppNnCheckExpression, isInterface(ty.resolved)) match {
                case (Some(check), true) => {
                  // We have a non-optional interface, assert that we're getting a non-null value
                  val javaParams = m.params.map(p => javaMarshal.fqParamType(p.ty) + " " + idJava.local(p.ident))
                  val javaParamsString: String = javaParams.mkString("(", ",", ")")
                  val functionString: String = s"${javaMarshal.fqTypename(ident, i)}#$javaMethodName$javaParamsString"
                  w.wl(s"""DJINNI_ASSERT_MSG(jret, jniEnv, "Got unexpected null return value from function $functionString");""")
                  w.wl(s"return ${jniMarshal.toCpp(ty, "jret")};")
                }
                case _ =>
              }
              w.wl(s"return ${jniMarshal.toCpp(ty, "jret")};")
            })
          }
        }
      }
      if (i.ext.cpp) {
        // Generate CEXPORT functions for JNI to call.
        val classIdentMunged = javaMarshal.fqTypename(ident, i)
          .replaceAllLiterally("_", "_1")
          .replaceAllLiterally(".", "_")
        val prefix = "Java_" + classIdentMunged
        def nativeHook(name: String, static: Boolean, params: Iterable[Field], ret: Option[TypeRef], f: => Unit) = {
          val paramList = params.map(p => jniMarshal.paramType(p.ty) + " j_" + idJava.local(p.ident)).mkString(", ")
          val jniRetType = jniMarshal.fqReturnType(ret)
          w.wl
          val methodNameMunged = name.replaceAllLiterally("_", "_1")
          val zero = ret.fold("")(s => "0 /* value doesn't matter */")
          if (static) {
            w.wl(s"CJNIEXPORT $jniRetType JNICALL ${prefix}_$methodNameMunged(JNIEnv* jniEnv, jobject /*this*/${preComma(paramList)})").braced {
              w.w("try").bracedEnd(s" JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, $zero)") {
                w.wl(s"DJINNI_FUNCTION_PROLOGUE0(jniEnv);")
                f
              }
            }
          }
          else {
            w.wl(s"CJNIEXPORT $jniRetType JNICALL ${prefix}_00024CppProxy_$methodNameMunged(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef${preComma(paramList)})").braced {
              w.w("try").bracedEnd(s" JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, $zero)") {
                w.wl(s"DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);")
                f
              }
            }
          }
        }
        nativeHook("nativeDestroy", false, Seq.empty, None, {
          w.wl(s"delete reinterpret_cast<::djinni::CppProxyHandle<$cppSelf>*>(nativeRef);")
        })
        for (m <- i.methods) {
          val nativeAddon = if (m.static) "" else "native_"
          nativeHook(nativeAddon + idJava.method(m.ident), m.static, m.params, m.ret, {
            //w.wl(s"::${spec.jniNamespace}::JniLocalScope jscope(jniEnv, 10);")
            if (!m.static) w.wl(s"const auto& ref = ::djinni::objectFromHandleAddress<$cppSelf>(nativeRef);")
            m.params.foreach(p => {
              if (isInterface(p.ty.resolved) && spec.cppNnCheckExpression.nonEmpty) {
                // We have a non-optional interface in nn mode, assert that we're getting a non-null value
                val paramName = idJava.local(p.ident)
                val javaMethodName = idJava.method(m.ident)
                val javaParams = m.params.map(p => javaMarshal.fqParamType(p.ty) + " " + idJava.local(p.ident))
                val javaParamsString: String = javaParams.mkString("(", ", ", ")")
                val functionString: String = s"${javaMarshal.fqTypename(ident, i)}#$javaMethodName$javaParamsString"
                w.wl( s"""DJINNI_ASSERT_MSG(j_$paramName, jniEnv, "Got unexpected null parameter '$paramName' to function $functionString");""")
              }
            })
            val methodName = idCpp.method(m.ident)
            val ret = m.ret.fold("")(r => "auto r = ")
            val call = if (m.static) s"$cppSelf::$methodName(" else s"ref->$methodName("
            writeAlignedCall(w, ret + call, m.params, ")", p => jniMarshal.toCpp(p.ty, "j_" + idJava.local(p.ident)))
            w.wl(";")
            m.ret.fold()(r => w.wl(s"return ::djinni::release(${jniMarshal.fromCpp(r, "r")});"))
          })
        }
      }
    }

    writeJniFiles(origin, typeParams.nonEmpty, ident, refs, writeJniPrototype, writeJniBody)
  }

  def writeJniFiles(origin: String, allInHeader: Boolean, ident: Ident, refs: JNIRefs, writeProto: IndentWriter => Unit, writeBody: IndentWriter => Unit) {
    if (allInHeader) {
      // Template class.  Write both parts to .hpp.
      writeJniHppFile(ident, origin, Iterable.concat(refs.jniHpp, refs.jniCpp), Nil, w => {
        writeProto(w)
        w.wl
        writeBody(w)
      })
    }
    else {
      // Write prototype to .hpp and body to .cpp
      writeJniHppFile(ident, origin, refs.jniHpp, Nil, writeProto)
      writeJniCppFile(ident, origin, refs.jniCpp, writeBody)
    }
  }

  def writeJniTypeParams(w: IndentWriter, params: Seq[TypeParam]) {
    if (params.isEmpty) return
    w.wl("template " + params.map(p => "typename " + spec.jniClassIdentStyle(p.ident)).mkString("<", ", ", ">"))
  }

  def typeParamsSignature(params: Seq[TypeParam]) = if(params.isEmpty) "" else params.map(p => spec.jniClassIdentStyle(p.ident)).mkString("<", ", ", ">")

  def toJniCall(ty: TypeRef, f: String => String): String = toJniCall(ty.resolved, f, false)
  def toJniCall(m: MExpr, f: String => String, needRef: Boolean): String = m.base match {
    case p: MPrimitive => f(if (needRef) "Object" else IdentStyle.camelUpper(p.jName))
    case MString => "(jstring)" + f("Object")
    case MOptional => toJniCall(m.args.head, f, true)
    case MBinary => "(jbyteArray)" + f("Object")
    case _ => f("Object")
  }

  def cppTypeArgs(params: Seq[TypeParam]): String =
    if (params.isEmpty) "" else params.map(p => idCpp.typeParam(p.ident)).mkString("<", ", ", ">")
}
