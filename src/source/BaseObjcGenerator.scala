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

import djinni.ast.Record.DerivingType
import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.syntax.Error
import djinni.writer.IndentWriter

import scala.collection.mutable
import scala.collection.parallel.immutable

abstract class BaseObjcGenerator(spec: Spec) extends Generator(spec) {

  val marshal = new ObjcMarshal(spec)

  object ObjcConstantType extends Enumeration {
    val ConstVariable, ConstMethod = Value
  }

  def writeObjcConstVariableDecl(w: IndentWriter, c: Const, s: String): Unit = {
    val nullability = marshal.nullability(c.ty.resolved).fold("")(" __" + _)
    val td = marshal.fqFieldType(c.ty) + nullability
    // MBinary | MList | MSet | MMap are not allowed for constants.
    w.w(s"${td} const $s${idObjc.const(c.ident)}")
  }

  /**
    * Gererate the definition of Objc constants.
    */
  def generateObjcConstants(w: IndentWriter, consts: Seq[Const], selfName: String,
                            genType: ObjcConstantType.Value) = {
    def boxedPrimitive(ty: TypeRef): String = {
      val (_, needRef) = marshal.toObjcType(ty)
      if (needRef) "@" else ""
    }

    def writeObjcConstValue(w: IndentWriter, ty: TypeRef, v: Any): Unit = v match {
      case l: Long => w.w(boxedPrimitive(ty) + l.toString)
      case d: Double if marshal.fieldType(ty) == "float" => w.w(boxedPrimitive(ty) + d.toString + "f")
      case d: Double => w.w(boxedPrimitive(ty) + d.toString)
      case b: Boolean => w.w(boxedPrimitive(ty) + (if (b) "YES" else "NO"))
      case s: String => w.w("@" + s)
      case e: EnumValue => w.w(idObjc.enum(e.ty + "_" + e.name))
      case v: ConstRef => w.w(selfName + idObjc.const (v.name))
      case z: Map[_, _] => { // Value is record
      val recordMdef = ty.resolved.base.asInstanceOf[MDef]
        val record = recordMdef.body.asInstanceOf[Record]
        val vMap = z.asInstanceOf[Map[String, Any]]
        val head = record.fields.head
        w.w(s"[[${marshal.typename(ty)} alloc] initWith${IdentStyle.camelUpper(head.ident)}:")
        writeObjcConstValue(w, head.ty, vMap.apply(head.ident))
        w.nestedN(2) {
          val skipFirst = SkipFirst()
          for (f <- record.fields) skipFirst {
            w.wl
            w.w(s"${idObjc.field(f.ident)}:")
            writeObjcConstValue(w, f.ty, vMap.apply(f.ident))
          }
        }
        w.w("]")
      }
    }

    def writeObjcConstMethImpl(c: Const, w: IndentWriter) {
      val label = "+"
      val nullability = marshal.nullability(c.ty.resolved).fold("")(" __" + _)
      val ret = marshal.fqFieldType(c.ty) + nullability
      val decl = s"$label ($ret)${idObjc.method(c.ident)}"
      writeAlignedObjcCall(w, decl, List(), "", p => ("",""))
      w.wl

      w.braced {
        var static_var = s"s_${idObjc.method(c.ident)}"
        w.w(s"static ${marshal.fqFieldType(c.ty)} const ${static_var} = ")
        writeObjcConstValue(w, c.ty, c.value)
        w.wl(";")
        w.wl(s"return $static_var;")
      }
    }

    genType match {
      case ObjcConstantType.ConstVariable => {
        for (c <- consts if marshal.canBeConstVariable(c)) {
          w.wl
          writeObjcConstVariableDecl(w, c, selfName)
          w.w(s" = ")
          writeObjcConstValue(w, c.ty, c.value)
          w.wl(";")
        }
      }
      case ObjcConstantType.ConstMethod => {
        for (c <- consts if !marshal.canBeConstVariable(c)) {
          writeObjcConstMethImpl(c, w)
          w.wl
        }
      }
    }
  }
}

