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

import djinni.ast.Record.DerivingType
import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter

import scala.collection.mutable

class JavaGenerator(spec: Spec) extends Generator(spec) {

  var javaAnnotationHeader = spec.javaAnnotation.map(pkg => '@' + pkg.split("\\.").last)

  class JavaRefs() {
    var java = mutable.TreeSet[String]()

    spec.javaAnnotation.foreach(pkg => java.add(pkg))

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.map(find).mkString("<", ", ", ">")
      find(tm.base)
    }
    def find(m: Meta) = m match {
      case o: MOpaque =>
        o match {
          case MList =>
            java.add("java.util.ArrayList")
          case MSet =>
            java.add("java.util.HashSet")
          case MMap =>
            java.add("java.util.HashMap")
          case MDate =>
            java.add("java.util.Date")
          case _ =>
        }
      case _ =>
    }
  }

  def writeJavaFile(ident: String, origin: String, refs: Iterable[String], f: IndentWriter => Unit) {
    createFile(spec.javaOutFolder.get, idJava.ty(ident) + ".java", (w: IndentWriter) => {
      w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("// This file generated by Djinni from " + origin)
      w.wl
      spec.javaPackage.foreach(s => w.wl(s"package $s;").wl)
      if (refs.nonEmpty) {
        refs.foreach(s => w.wl(s"import $s;"))
        w.wl
      }
      f(w)
    })
  }

  def generateJavaConstants(w: IndentWriter, consts: Seq[Const]) = {

    def writeJavaConst(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(l.toString)
      case d: Double => w.w(d.toString)
      case b: Boolean => w.w(if (b) "true" else "false")
      case s: String => w.w(s)
      case e: EnumValue => {
        val enumMdef = ty.resolved.base.asInstanceOf[MDef]
        w.w(s"${idJava.ty(enumMdef.name)}.${idJava.enum(e)}")
      }
      case v: ConstRef => w.w(idJava.const(v))
      case z: Map[_, _] => { // Value is record
      val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        w.wl(s"new ${idJava.ty(recordMdef.name)}(")
        w.increase()
        // Use exact sequence
        val skipFirst = SkipFirst()
        for (f <- record.fields) {
          skipFirst {w.wl(",")}
          writeJavaConst(w, f.ty, vMap.apply(f.ident.name))
          w.w(" /* " + idJava.field(f.ident) + " */ ")
        }
        w.w(")")
        w.decrease()
      }
    }

    for (c <- consts) {
      writeDoc(w, c.doc)
      javaAnnotationHeader.foreach(w.wl)
      w.w(s"public static final ${toJavaType(c.ty)} ${idJava.const(c.ident)} = ")
      writeJavaConst(w, c.ty, c.value)
      w.wl(";")
      w.wl
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    val refs = new JavaRefs()

    writeJavaFile(ident, origin, refs.java, w => {
      writeDoc(w, doc)
      javaAnnotationHeader.foreach(w.wl)
      w.w(s"public enum ${idJava.ty(ident)}").braced {
        for (o <- e.options) {
          writeDoc(w, o.doc)
          w.wl(idJava.enum(o.ident) + ",")
        }
        w.wl(";")
      }
    })
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    val refs = new JavaRefs()

    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })
    if (i.ext.cpp) {
      refs.java.add("java.util.concurrent.atomic.AtomicBoolean")
    }

    writeJavaFile(ident, origin, refs.java, w => {
      val javaClass = idJava.ty(ident)
      val typeParamList = javaTypeParams(typeParams)
      writeDoc(w, doc)

      javaAnnotationHeader.foreach(w.wl)
      val isAbstractClass = i.ext.cpp || i.methods.exists(_.static) || i.consts.size > 0
      val classPrefix = if (isAbstractClass) "abstract class" else "interface"
      val methodPrefix = if (isAbstractClass) "abstract " else ""
      w.w(s"public $classPrefix $javaClass$typeParamList").braced {
        val skipFirst = SkipFirst()
        generateJavaConstants(w, i.consts)

        val throwException = spec.javaCppException.fold("")(" throws " + _)
        for (m <- i.methods if !m.static) {
          skipFirst { w.wl }
          writeDoc(w, m.doc)
          val ret = m.ret.fold("void")(toJavaType(_))
          val params = m.params.map(p => toJavaType(p.ty) + " " + idJava.local(p.ident))
          w.wl(s"public $methodPrefix" + ret + " " + idJava.method(m.ident) + params.mkString("(", ", ", ")") + throwException + ";")
        }
        for (m <- i.methods if m.static) {
          skipFirst { w.wl }
          writeDoc(w, m.doc)
          val ret = m.ret.fold("void")(toJavaType(_))
          val params = m.params.map(p => toJavaType(p.ty) + " " + idJava.local(p.ident))
          w.wl("public static native "+ ret + " " + idJava.method(m.ident) + params.mkString("(", ", ", ")") + ";")
        }
        if (i.ext.cpp) {
          w.wl
          javaAnnotationHeader.foreach(w.wl)
          w.wl(s"public static final class CppProxy$typeParamList extends $javaClass$typeParamList").braced {
            w.wl("private final long nativeRef;")
            w.wl("private final AtomicBoolean destroyed = new AtomicBoolean(false);")
            w.wl
            w.wl(s"private CppProxy(long nativeRef)").braced {
              w.wl("if (nativeRef == 0) throw new RuntimeException(\"nativeRef is zero\");")
              w.wl(s"this.nativeRef = nativeRef;")
            }
            w.wl
            w.wl("private native void nativeDestroy(long nativeRef);")
            w.wl("public void destroy()").braced {
              w.wl("boolean destroyed = this.destroyed.getAndSet(true);")
              w.wl("if (!destroyed) nativeDestroy(this.nativeRef);")
            }
            w.wl("protected void finalize() throws java.lang.Throwable").braced {
              w.wl("destroy();")
              w.wl("super.finalize();")
            }
            for (m <- i.methods if !m.static) { // Static methods not in CppProxy
            val ret = m.ret.fold("void")(toJavaType(_))
              val returnStmt = m.ret.fold("")(_ => "return ")
              val params = m.params.map(p => toJavaType(p.ty)+" "+idJava.local(p.ident)).mkString(", ")
              val args = m.params.map(p => idJava.local(p.ident)).mkString(", ")
              val meth = idJava.method(m.ident)
              w.wl
              w.wl(s"@Override")
              w.wl(s"public $ret $meth($params)$throwException").braced {
                w.wl("assert !this.destroyed.get() : \"trying to use a destroyed object\";")
                w.wl(s"${returnStmt}native_$meth(this.nativeRef${preComma(args)});")
              }
              w.wl(s"private native $ret native_$meth(long _nativeRef${preComma(params)});")
            }
          }
        }
      }
    })
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    val refs = new JavaRefs()
    r.fields.foreach(f => refs.find(f.ty))

    val (javaName, javaFinal) = if (r.ext.java) (ident.name + "_base", "") else (ident.name, " final")
    writeJavaFile(javaName, origin, refs.java, w => {
      writeDoc(w, doc)
      javaAnnotationHeader.foreach(w.wl)

      // HACK: Use generic base class to correctly implement Comparable interface
      val comparableFlag =
        if (r.derivingTypes.contains(DerivingType.Ord)) {
          if (r.ext.java) {
            s"<E extends ${idJava.ty(javaName)}> implements Comparable<E>"
          } else {
            s" implements Comparable<${idJava.ty(javaName)}>"
          }
        } else {
          ""
        }
      w.w(s"public$javaFinal class ${idJava.ty(javaName) + javaTypeParams(params)}$comparableFlag").braced {
        w.wl
        generateJavaConstants(w, r.consts)
        // Field definitions.
        for (f <- r.fields) {
          w.wl
          w.wl(s"/*package*/ final ${toJavaType(f.ty)} ${idJava.field(f.ident)};")
        }

        // Constructor.
        w.wl
        w.wl(s"public ${idJava.ty(javaName)}(").nestedN(2) {
          val skipFirst = SkipFirst()
          for (f <- r.fields) {
            skipFirst { w.wl(",") }
            w.w(toJavaType(f.ty) + " " + idJava.local(f.ident))
          }
          w.wl(") {")
        }
        w.nested {
          for (f <- r.fields) {
            w.wl(s"this.${idJava.field(f.ident)} = ${idJava.local(f.ident)};")
          }
        }
        w.wl("}")

        // Accessors
        for (f <- r.fields) {
          w.wl
          writeDoc(w, f.doc)
          w.w("public " + toJavaType(f.ty) + " " + idJava.method("get_" + f.ident.name) + "()").braced {
            w.wl("return " + idJava.field(f.ident) + ";")
          }
        }

        if (r.derivingTypes.contains(DerivingType.Eq)) {
          w.wl
          w.wl("@Override")
          w.w("public boolean equals(Object obj)").braced {
            w.w(s"if (!(obj instanceof ${idJava.ty(javaName)}))").braced {
              w.wl("return false;");
            }
            w.wl(s"${idJava.ty(javaName)} other = (${idJava.ty(javaName)}) obj;")
            w.w(s"return ").nestedN(2) {
              val skipFirst = SkipFirst()
              for (f <- r.fields) {
                skipFirst { w.wl(" &&") }
                f.ty.resolved.base match {
                  case MBinary => w.w(s"java.util.Arrays.equals(${idJava.field(f.ident)}, other.${idJava.field(f.ident)})")
                  case MList | MSet | MMap => w.w(s"this.${idJava.field(f.ident)}.equals(other.${idJava.field(f.ident)})")
                  case MOptional =>
                    w.w(s"((this.${idJava.field(f.ident)} == null && other.${idJava.field(f.ident)} == null) || ")
                    w.w(s"(this.${idJava.field(f.ident)} != null && this.${idJava.field(f.ident)}.equals(other.${idJava.field(f.ident)})))")
                  case MString => w.w(s"this.${idJava.field(f.ident)}.equals(other.${idJava.field(f.ident)})")
                  case t: MPrimitive => w.w(s"this.${idJava.field(f.ident)} == other.${idJava.field(f.ident)}")
                  case df: MDef => df.defType match {
                    case DRecord => w.w(s"this.${idJava.field(f.ident)}.equals(other.${idJava.field(f.ident)})")
                    case DEnum => w.w(s"this.${idJava.field(f.ident)} == other.${idJava.field(f.ident)}")
                    case _ => throw new AssertionError("Unreachable")
                  }
                  case _ => throw new AssertionError("Unreachable")
                }
              }
            }
            w.wl(";")
          }
        }

        if (r.derivingTypes.contains(DerivingType.Ord)) {
          w.wl
          w.wl("@Override")
          val tyName = if (r.ext.java) "E" else idJava.ty(javaName)
          w.w(s"public int compareTo($tyName other) ").braced {
            w.wl("int tempResult;")
            for (f <- r.fields) {
              f.ty.resolved.base match {
                case MString => w.wl(s"tempResult = this.${idJava.field(f.ident)}.compareTo(other.${idJava.field(f.ident)});")
                case t: MPrimitive =>
                  w.wl(s"if (this.${idJava.field(f.ident)} < other.${idJava.field(f.ident)}) {").nested {
                    w.wl(s"tempResult = -1;")
                  }
                  w.wl(s"} else if (this.${idJava.field(f.ident)} > other.${idJava.field(f.ident)}) {").nested {
                    w.wl(s"tempResult = 1;")
                  }
                  w.wl(s"} else {").nested {
                    w.wl(s"tempResult = 0;")
                  }
                  w.wl("}")
                case df: MDef => df.defType match {
                  case DRecord => w.wl(s"tempResult = this.${idJava.field(f.ident)}.compareTo(other.${idJava.field(f.ident)});")
                  case DEnum => w.w(s"tempResult = this.${idJava.field(f.ident)}.compareTo(other.${idJava.field(f.ident)});")
                  case _ => throw new AssertionError("Unreachable")
                }
                case _ => throw new AssertionError("Unreachable")
              }
              w.w("if (tempResult != 0)").braced {
                w.wl("return tempResult;")
              }
            }
            w.wl("return 0;")
          }
        }

      }
    })
  }

  def toJavaType(ty: TypeRef, packageName: Option[String] = None): String = toJavaType(ty.resolved, packageName)
  def toJavaType(tm: MExpr, packageName: Option[String]): String = {
    def f(tm: MExpr, needRef: Boolean): String = {
      tm.base match {
        case MOptional =>
          // HACK: We use "null" for the empty optional in Java.
          assert(tm.args.size == 1)
          val arg = tm.args.head
          arg.base match {
            case p: MPrimitive => p.jBoxed
            case MOptional => throw new AssertionError("nested optional?")
            case m => f(arg, true)
          }
        case o =>
          val args = if (tm.args.isEmpty) "" else tm.args.map(f(_, true)).mkString("<", ", ", ">")
          val base = o match {
            case p: MPrimitive => if (needRef) p.jBoxed else p.jName
            case MString => "String"
            case MDate => "Date"
            case MBinary => "byte[]"
            case MOptional => throw new AssertionError("optional should have been special cased")
            case MList => "ArrayList"
            case MSet => "HashSet"
            case MMap => "HashMap"
            case d: MDef => withPackage(packageName, idJava.ty(d.name))
            case p: MParam => idJava.typeParam(p.name)
          }
          base + args
      }
    }
    f(tm, false)
  }

  def javaTypeParams(params: Seq[TypeParam]): String =
    if (params.isEmpty) "" else params.map(p => idJava.typeParam(p.ident)).mkString("<", ", ", ">")

}
