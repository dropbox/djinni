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

  val jniBaseLibClassIdentStyle = IdentStyle.prefix("H", IdentStyle.camelUpper)
  val jniBaseLibFileIdentStyle = jniBaseLibClassIdentStyle

  val writeJniCppFile = writeCppFileGeneric(spec.jniOutFolder.get, Some(spec.jniNamespace), spec.jniFileIdentStyle, spec.jniIncludePrefix) _
  def writeJniHppFile(name: String, origin: String, includes: Iterable[String], fwds: Iterable[String], f: IndentWriter => Unit, f2: IndentWriter => Unit = (w => {})) =
    writeHppFileGeneric(spec.jniHeaderOutFolder.get, Some(spec.jniNamespace), spec.jniFileIdentStyle)(name, origin, includes, fwds, f, f2)

  class JNIRefs(name: String) {
    var jniHpp = mutable.TreeSet[String]()
    var jniCpp = mutable.TreeSet[String]()

    jniHpp.add("#include " + q(spec.jniIncludeCppPrefix + spec.cppFileIdentStyle(name) + "." + spec.cppHeaderExt))
    jniHpp.add("#include \"djinni_support.hpp\"")

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.map(find).mkString("<", ", ", ">")
      find(tm.base)
    }
    def find(m: Meta) = m match {
      case o: MOpaque =>
        jniCpp.add("#include " + q(spec.jniBaseLibIncludePrefix + jniBaseLibFileIdentStyle(o.idlName) + "." + spec.cppHeaderExt))
      case d: MDef =>
        jniCpp.add("#include " + q(spec.jniIncludePrefix + spec.jniFileIdentStyle(d.name) + "." + spec.cppHeaderExt))
      case p: MParam =>
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new JNIRefs(ident.name)
    val self = idCpp.enumType(ident)
    val selfQ = withNs(spec.cppNamespace, self)
    val jniClassName = spec.jniClassIdentStyle(ident)

    writeJniHppFile(ident, origin, Iterable.concat(refs.jniHpp, refs.jniCpp), Nil, w => {
      w.w(s"class $jniClassName final : djinni::JniEnum").bracedSemi {
        w.wlOutdent("public:")
        w.wl(s"using CppType = $selfQ;")
        w.wl(s"using JniType = jobject;")
        w.wl
        w.wl(s"static jobject toJava(JNIEnv* jniEnv, $selfQ c) { return djinni::JniClass<$jniClassName>::get().create(jniEnv, static_cast<int>(c)).release(); }")
        w.wl(s"static $selfQ fromJava(JNIEnv* jniEnv, jobject j) { return static_cast<$selfQ>(djinni::JniClass<$jniClassName>::get().ordinal(jniEnv, j)); }")
        w.wl
        w.wlOutdent("private:")
        val classLookup = q(toJavaClassLookup(ident, spec.javaPackage))
        w.wl(s"$jniClassName() : JniEnum($classLookup) {}")
        w.wl(s"friend class djinni::JniClass<$jniClassName>;")
      }
    })
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val refs = new JNIRefs(ident.name)
    r.fields.foreach(f => refs.find(f.ty))
    val jniClassName = spec.jniClassIdentStyle(ident)
    val fqJniClassName = withNs(Some(spec.jniNamespace), jniClassName)

    val self = idCpp.ty(ident)
    val selfQ = withNs(spec.cppNamespace, self) + cppTypeArgs(params)  // Qualified "self"

    def writeJniPrototype(w: IndentWriter) {
      writeJniTypeParams(w, params)
      w.w(s"class $jniClassName final").bracedSemi {
        w.wlOutdent("public:")
        w.wl(s"using CppType = $selfQ;")
        w.wl(s"using JniType = jobject;")
        w.wl
        w.wl(s"static jobject toJava(JNIEnv*, $selfQ);")
        w.wl(s"static $selfQ fromJava(JNIEnv*, jobject);")
        w.wl
        val classLookup = q(toJavaClassLookup(ident, spec.javaPackage))
        w.wl(s"const djinni::GlobalRef<jclass> clazz { djinni::jniFindClass($classLookup) };")
        val constructorSig = q(toJavaMethodSig(spec.javaPackage, r.fields, None))
        w.wl(s"const jmethodID jconstructor { djinni::jniGetMethodID(clazz.get(), ${q("<init>")}, $constructorSig) };")
        for (f <- r.fields) {
          val javaFieldName = idJava.field(f.ident)
          val javaSig = q(toJavaTypeSig(f.ty, spec.javaPackage))
          w.wl(s"const jfieldID field_$javaFieldName { djinni::jniGetFieldID(clazz.get(), ${q(javaFieldName)}, $javaSig) };")
        }
        w.wl
        w.wlOutdent("private:")
        w.wl(s"$jniClassName() {}")
        w.wl(s"friend class djinni::JniClass<$fqJniClassName>;")
      }
    }

    def writeJniBody(w: IndentWriter) {
      writeJniTypeParams(w, params)

      val dataVar = if (r.fields.nonEmpty) " c" else ""
      w.w(s"jobject $jniClassName::toJava(JNIEnv* jniEnv, $selfQ$dataVar)").braced{
        //w.wl(s"::${spec.jniNamespace}::JniLocalScope jscope(jniEnv, 10);")
        val jniArgs = mutable.ListBuffer[String]()
        for (f <- r.fields) {
          val jniLocal = "j_" + idCpp.local(f.ident)
          val cppFieldName = idCpp.field(f.ident)
          val jniHelperClass = toJniHelperClass(f.ty)
          val jniLocalRead = storeLocal(w, jniLocal, f.ty, s"$jniHelperClass::toJava(jniEnv, c.$cppFieldName)")
          jniArgs.append(jniLocalRead)
        }
        w.wl(s"const auto & data = djinni::JniClass<$fqJniClassName>::get();")
        val argList = jniArgs.map(", " + _).mkString("")
        w.wl(s"jobject r = jniEnv->NewObject(data.clazz.get(), data.jconstructor$argList);")
        w.wl(s"djinni::jniExceptionCheck(jniEnv);")
        w.wl(s"return r;")
      }
      w.wl
      writeJniTypeParams(w, params)
      if (r.fields.nonEmpty) {
        w.w(s"$selfQ $jniClassName::fromJava(JNIEnv* jniEnv, jobject j)").braced {
          //w.wl(s"::${spec.jniNamespace}::JniLocalScope jscope(jniEnv, 10);")
          w.wl(s"assert(j != nullptr);")
          w.wl(s"const auto & data = djinni::JniClass<$fqJniClassName>::get();")
          w.wl(s"return $selfQ(").nested {
            val skipFirst = SkipFirst()
            for (f <- r.fields) {
              skipFirst { w.wl(",") }
              val jniHelperClass = toJniHelperClass(f.ty)
              val fieldId = "data.field_" + idJava.field(f.ident)
              var jniFieldAccess = toJniCall(f.ty, (jt: String) => s"jniEnv->Get${jt}Field(j, $fieldId)")
              if (isJavaHeapObject(f.ty)) {
                val jniTy = toJniType(f.ty)
                jniFieldAccess = s"djinni::LocalRef<$jniTy>(jniEnv, $jniFieldAccess).get()"
              }
              w.w(s"$jniHelperClass::fromJava(jniEnv, $jniFieldAccess)")
            }
            w.wl(");")
          }
        }
      } else {
        w.w(s"$selfQ $jniClassName::fromJava(JNIEnv*, jobject j)").braced {
          w.wl("assert(j != nullptr);")
          w.wl("(void) j; // Suppress unused error in release build")
          w.wl(s"return $selfQ();")
        }
      }

    }

    writeJniFiles(origin, params.nonEmpty, ident, refs, writeJniPrototype, writeJniBody)
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new JNIRefs(ident.name)
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    val self = idCpp.ty(ident)
    val selfQ = withNs(spec.cppNamespace, self) + cppTypeArgs(typeParams)  // Qualified "self"

    val jniClassName = spec.jniClassIdentStyle(ident)
    val fqJniClassName = withNs(Some(spec.jniNamespace), jniClassName)
    val classLookup = toJavaClassLookup(ident, spec.javaPackage)
    val (baseClassName, baseClassArgs) = (i.ext.java, i.ext.cpp) match {
      case (true,  true) =>  throw new AssertionError("an interface cannot be both +c and +j")
      case (false, true) =>  ("JniInterfaceCppExt",  s"<$selfQ>")
      case (true,  false) => ("JniInterfaceJavaExt", s"<$selfQ, $jniClassName>")
      case (false, false) => throw new AssertionError("interface isn't implementable on either side?")
    }
    val baseType = s"djinni::"+baseClassName+baseClassArgs

    def writeJniPrototype(w: IndentWriter) {
      writeJniTypeParams(w, typeParams)
      w.w(s"class $jniClassName final : $baseType").bracedSemi {
        w.wlOutdent(s"public:")
        w.wl(s"using CppType = std::shared_ptr<$selfQ>;")
        w.wl(s"using JniType = jobject;")
        w.wl
        if (!i.ext.java) {
          w.wl(s"static jobject toJava(JNIEnv* jniEnv, std::shared_ptr<$selfQ> c) { return djinni::JniClass<$fqJniClassName>::get()._toJava(jniEnv, c); }")
        }
        w.wl(s"static std::shared_ptr<$selfQ> fromJava(JNIEnv* jniEnv, jobject j) { return djinni::JniClass<$fqJniClassName>::get()._fromJava(jniEnv, j); }")
        w.wl
        if (i.ext.java) {
          w.wl(s"const djinni::GlobalRef<jclass> clazz { djinni::jniFindClass(${q(classLookup)}) };")
          for (m <- i.methods) {
            val javaMethodName = idJava.method(m.ident)
            val javaMethodSig = q(toJavaMethodSig(spec.javaPackage, m.params, m.ret))
            w.wl(s"const jmethodID method_$javaMethodName { djinni::jniGetMethodID(clazz.get(), ${q(javaMethodName)}, $javaMethodSig) };")
          }
        }
        if (i.ext.java) {
          w.wl
          w.w(s"class JavaProxy final : djinni::JavaProxyCacheEntry, public ${withNs(spec.cppNamespace, idCpp.ty(ident))}").bracedSemi {
            w.wlOutdent(s"public:")
            w.wl(s"JavaProxy(jobject obj);")
            for (m <- i.methods) {
              val ret = m.ret.fold("void")(toCppType(_, spec.cppNamespace))
              val params = m.params.map(p => "const " + toCppType(p.ty, spec.cppNamespace) + " & " + idCpp.local(p.ident))
              w.wl(s"virtual $ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")} override;")
            }
            w.wl
            w.wlOutdent(s"private:")
            w.wl(s"using djinni::JavaProxyCacheEntry::getGlobalRef;")
            w.wl(s"friend class djinni::JniInterfaceJavaExt<$selfQ, $fqJniClassName>;")
            w.wl(s"friend class djinni::JavaProxyCache<JavaProxy>;")
          }
        }
        w.wl
        w.wlOutdent("private:")
        w.wl(s"$jniClassName();")
        w.wl(s"friend class djinni::JniClass<$fqJniClassName>;")
      }
    }

    def writeJniBody(w: IndentWriter) {
      val baseClassParam = if (i.ext.cpp) q(classLookup+"$NativeProxy") else ""
      writeJniTypeParams(w, typeParams)
      w.wl(s"$jniClassName::$jniClassName() : $baseType($baseClassParam) {}")
      w.wl
      if (i.ext.java) {
        writeJniTypeParams(w, typeParams)
        w.wl(s"$jniClassName::JavaProxy::JavaProxy(jobject obj) : JavaProxyCacheEntry(obj) {}")

        for (m <- i.methods) {
          w.wl
          val ret = m.ret.fold("void")(toCppType(_, spec.cppNamespace))
          val params = m.params.map(p => "const " + toCppType(p.ty, spec.cppNamespace) + " & c_" + idCpp.local(p.ident))
          writeJniTypeParams(w, typeParams)
          w.w(s"$ret $jniClassName::JavaProxy::JavaProxy::${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")}").bracedSemi {
            w.wl(s"JNIEnv * const jniEnv = djinni::jniGetThreadEnv();")
            w.wl(s"djinni::JniLocalScope jscope(jniEnv, 10);")
            val jniArgs = mutable.ListBuffer[String]()
            for (p <- m.params) {
              val cppParamName = "c_" + idCpp.local(p.ident)
              val jniLocal = "j_" + idCpp.local(p.ident)
              val jniHelperClass = toJniHelperClass(p.ty)
              val jniLocalRead = storeLocal(w, jniLocal, p.ty, s"$jniHelperClass::toJava(jniEnv, $cppParamName)")
              jniArgs.append(jniLocalRead)
            }
            w.wl(s"const auto & data = djinni::JniClass<$fqJniClassName>::get();")
            val argList = jniArgs.map(", "+_).mkString("")
            val javaMethodName = idJava.method(m.ident)
            m.ret match {
              case Some(r) =>
                val jniCall = toJniCall(r, (jt: String) => s"jniEnv->Call${jt}Method(getGlobalRef(), data.method_$javaMethodName$argList)")
                val jretRead = storeLocal(w, "jret", r, jniCall)
                w.wl(s"djinni::jniExceptionCheck(jniEnv);")
                val jniHelperClass = toJniHelperClass(r)
                w.wl(s"return $jniHelperClass::fromJava(jniEnv, $jretRead);")
              case None =>
                w.wl(s"jniEnv->CallVoidMethod(getGlobalRef(), data.method_$javaMethodName$argList);")
                w.wl(s"djinni::jniExceptionCheck(jniEnv);")
            }
          }
        }
      }
      if (i.ext.cpp) {
        w.wl(s"using namespace ::${spec.jniNamespace};")
        // Generate CEXPORT functions for JNI to call.
        val classIdentMunged = withPackage(spec.javaPackage, idJava.ty(ident))
          .replaceAllLiterally("_", "_1")
          .replaceAllLiterally(".", "_")
        val prefix = "Java_" + classIdentMunged
        def nativeHook(name: String, static: Boolean, params: Iterable[Field], ret: Option[TypeRef], f: => Unit) = {
          val paramList = params.map(p => toJniType(p.ty) + " j_" + idJava.local(p.ident)).mkString(", ")
          val jniRetType = ret.fold("void")(toJniType)
          w.wl
          val methodNameMunged = name.replaceAllLiterally("_", "_1")
          if (static) {
            w.wl(s"CJNIEXPORT $jniRetType JNICALL ${prefix}_$methodNameMunged(JNIEnv* jniEnv, jobject /*this*/${preComma(paramList)})").braced {
              w.w("try").bracedEnd(s" JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, ${
                ret.fold("")(s => "0  /* value doesn't matter */ ")
              })") {
                w.wl(s"DJINNI_FUNCTION_PROLOGUE0(jniEnv);")
                f
              }
            }
          }
          else {
            w.wl(s"CJNIEXPORT $jniRetType JNICALL ${prefix}_00024NativeProxy_$methodNameMunged(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef${preComma(paramList)})").braced {
              val zero = "0 /* value doesn't matter*/"
              w.w("try").bracedEnd(s" JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, ${ret.fold("")(s => zero)})") {
                w.wl(s"DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);")
                f
              }
            }
          }
        }
        nativeHook("nativeDestroy", false, Seq.empty, None, {
          w.wl(s"delete reinterpret_cast<std::shared_ptr<$selfQ>*>(nativeRef);")
        })
        for (m <- i.methods) {
          val nativeAddon = if (m.static) "" else "native_"
          nativeHook(nativeAddon + idJava.method(m.ident), m.static, m.params, m.ret, {
            //w.wl(s"::${spec.jniNamespace}::JniLocalScope jscope(jniEnv, 10);")
              if (!m.static) w.wl(s"const std::shared_ptr<$selfQ> & ref = *reinterpret_cast<const std::shared_ptr<$selfQ>*>(nativeRef);")
            for (p <- m.params) {
              val jniHelperClass = toJniHelperClass(p.ty)
              val cppType = toCppType(p.ty, spec.cppNamespace)
              val localVar = "c_" + idCpp.local(p.ident)
              val paramName = "j_" + idJava.local(p.ident)
              w.wl(s"$cppType $localVar = $jniHelperClass::fromJava(jniEnv, $paramName);")
              if (isJavaHeapObject(p.ty)) {
                w.wl(s"jniEnv->DeleteLocalRef($paramName);")
              }
            }
            val callArgs = m.params.map(p => "c_" + idCpp.local(p.ident)).mkString(", ")
            val methodName = idCpp.method(m.ident)
            val callExpr = if (m.static) s"$selfQ::$methodName($callArgs)" else s"ref->$methodName($callArgs)"
            w.wl
            m.ret match {
              case Some(r) =>
                val cppRetType = toCppType(r, spec.cppNamespace)
                val jniHelperClass = toJniHelperClass(r)
                w.wl(s"$cppRetType cr = $callExpr;")
                w.wl
                w.wl(s"return $jniHelperClass::toJava(jniEnv, cr);")
              case None =>
                w.wl(s"$callExpr;")
            }
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

  def toJavaClassLookup(name: String, javaPackage: Option[String]): String = {
    val javaClassName = idJava.ty(name)
    javaPackage.fold(javaClassName)(p => p.replaceAllLiterally(".", "/") + "/" + javaClassName)
  }

  def toJavaTypeSig(ty: TypeRef, javaPackage: Option[String] = None): String = {
    def f(e: MExpr): String = e.base match {
      case o: MOpaque => o match {
        case p: MPrimitive => p.jSig
        case MString => "Ljava/lang/String;"
        case MBinary => "[B"
        case MOptional =>  e.args.head.base match {
          case p: MPrimitive => s"Ljava/lang/${p.jBoxed};"
          case MOptional => throw new AssertionError("nested optional?")
          case m => f(e.args.head)
        }
        case MList => "Ljava/util/ArrayList;"
        case MSet => "Ljava/util/HashSet;"
        case MMap => "Ljava/util/HashMap;"
      }
      case MParam(_) => "Ljava/lang/Object;"
      case d: MDef => s"L${toJavaClassLookup(d.name, javaPackage)};"
    }
    f(ty.resolved)
  }

  def toJavaMethodSig(javaPackage: Option[String], params: Iterable[Field], ret: Option[TypeRef]) = {
    params.map(f => toJavaTypeSig(f.ty, javaPackage)).mkString("(", "", ")") + ret.fold("V")(toJavaTypeSig(_, javaPackage))
  }

  def writeJniTypeParams(w: IndentWriter, params: Seq[TypeParam]) {
    if (params.isEmpty) return
    w.wl("template " + params.map(p => "typename " + spec.jniClassIdentStyle(p.ident)).mkString("<", ", ", ">"))
  }

  def toJniHelperClass(ty: TypeRef, namespace: Option[String] = None): String = toJniHelperClass(ty.resolved, namespace)
  def toJniHelperClass(tm: MExpr, namespace: Option[String]): String = {
    def base(m: Meta, needRef: Boolean): String = m match {
      case o: MOpaque =>
        val name = o match {
          case _: MPrimitive if !needRef => o.idlName + "::Unboxed"
          case _ => o.idlName
        }
        withNs(Some("djinni"), jniBaseLibClassIdentStyle(name))
      case d: MDef => spec.jniClassIdentStyle(d.name)
      case p: MParam => idCpp.typeParam(p.name)
    }
    def expr(tm: MExpr, needRef: Boolean): String = {
      val args = if (tm.args.isEmpty) "" else tm.args.map(expr(_, true)).mkString("<", ", ", ">")
      base(tm.base, needRef) + args
    }
    // Hacky special case - HOptional needs an extra parameter that's the optional impl in use
    tm.base match {
      case MOptional => base(tm.base, false) + "<" + spec.cppOptionalTemplate + ", " + expr(tm.args.head, true) + ">"
      case _ => expr(tm, false)
    }
  }

  def toJniType(ty: TypeRef): String = toJniType(ty.resolved, false)
  def toJniType(m: MExpr, needRef: Boolean): String = m.base match {
    case p: MPrimitive => if (needRef) "jobject" else p.jniName
    case MString => "jstring"
    case MOptional => toJniType(m.args.head, true)
    case MBinary => "jbyteArray"
    case tp: MParam => spec.jniClassIdentStyle(tp.name) + "::JniType"
    case _ => "jobject"
  }

  def toJniCall(ty: TypeRef, f: String => String): String = toJniCall(ty.resolved, f, false)
  def toJniCall(m: MExpr, f: String => String, needRef: Boolean): String = m.base match {
    case p: MPrimitive => f(if (needRef) "Object" else IdentStyle.camelUpper(p.jName))
    case MString => s"static_cast<jstring>(${f("Object")})"
    case MOptional => toJniCall(m.args.head, f, true)
    case MBinary => s"static_cast<jbyteArray>(${f("Object")})"
    case _ => f("Object")
  }

  def isJavaHeapObject(ty: TypeRef): Boolean = isJavaHeapObject(ty.resolved.base)
  def isJavaHeapObject(m: Meta): Boolean = m match {
    case _: MPrimitive => false
    case _ => true
  }

  def storeLocal(w: IndentWriter, name: String, ty: TypeRef, expr: String) = {
    val jniTy = toJniType(ty)
    if (isJavaHeapObject(ty)) {
      w.wl(s"djinni::LocalRef<$jniTy> $name(jniEnv, $expr);")
      s"$name.get()"
    } else {
      w.wl(s"$jniTy $name = $expr;")
      name
    }
  }

  def cppTypeArgs(params: Seq[TypeParam]): String =
    if (params.isEmpty) "" else params.map(p => idCpp.typeParam(p.ident)).mkString("<", ", ", ">")

}
