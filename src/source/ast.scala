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

package djinni.ast

import java.io.File
import djinni.ast.Record.DerivingType.DerivingType
import djinni.meta.MExpr
import djinni.syntax.Loc

case class IdlFile(imports: Seq[File], typeDecls: Seq[TypeDecl])

case class Ident(name: String, file: File, loc: Loc)
class ConstRef(ident: Ident) extends Ident(ident.name, ident.file, ident.loc)
class EnumValue(val ty: Ident, ident: Ident) extends Ident(ident.name, ident.file, ident.loc)

case class TypeParam(ident: Ident)

case class Doc(lines: Seq[String])

case class TypeDecl(ident: Ident, params: Seq[TypeParam], body: TypeDef, doc: Doc, origin: String)

case class Ext(java: Boolean, cpp: Boolean, objc: Boolean)

case class TypeRef(expr: TypeExpr) {
  var resolved: MExpr = null
}
case class TypeExpr(ident: Ident, args: Seq[TypeExpr])

sealed abstract class TypeDef

case class Const(ident: Ident, ty: TypeRef, value: Any, doc: Doc)

case class Enum(options: Seq[Enum.Option]) extends TypeDef
object Enum {
  case class Option(ident: Ident, doc: Doc)
}

case class Record(ext: Ext, fields: Seq[Field], consts: Seq[Const], derivingTypes: Set[DerivingType]) extends TypeDef
object Record {
  object DerivingType extends Enumeration {
    type DerivingType = Value
    val Eq, Ord = Value
  }
}

case class Interface(ext: Ext, methods: Seq[Interface.Method], consts: Seq[Const]) extends TypeDef
object Interface {
  case class Method(ident: Ident, params: Seq[Field], ret: Option[TypeRef], doc: Doc, static: Boolean, const: Boolean)
}

case class Field(ident: Ident, ty: TypeRef, doc: Doc)
