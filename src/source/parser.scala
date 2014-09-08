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

import java.io.{File, InputStreamReader, FileInputStream}

import djinni.ast.Interface.Method
import djinni.ast.Record.DerivingType.DerivingType
import djinni.syntax._
import djinni.ast._
import scala.collection.mutable
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{Position, Positional}

case class Parser() {

val visitedFiles = mutable.Set[File]()
val fileStack = mutable.Stack[File]()

private object IdlParser extends RegexParsers {
  override protected val whiteSpace = """[ \t\n\r]+""".r

  def idlFile(origin: String): Parser[IdlFile] = rep(importFile) ~ rep(typeDecl(origin)) ^^ { case imp~types => IdlFile(imp, types) }

  def importFile: Parser[File] = "@import \"" ~> filePath <~ "\"" ^^ {
    x => {
      val newPath = fileStack.top.getParent() + "/" + x
      new File(newPath)
    }
  }
  def filePath = "[^\"]*".r

  def typeDecl(origin: String): Parser[TypeDecl] = doc ~ ident ~ typeList(ident ^^ TypeParam) ~ "=" ~ typeDef ^^ {
    case doc~ident~typeParams~_~body => TypeDecl(ident, typeParams, body, doc, origin)
  }

  def ext(default: Ext) = (rep1("+" ~> ident) >> checkExts) | success(default)
  def extRecord = ext(Ext(false, false, false))
  def extInterface = ext(Ext(true, true, true))

  def checkExts(parts: List[Ident]): Parser[Ext] = {
    var foundCpp = false
    var foundJava = false
    var foundObjc = false

    for (part <- parts)
      part.name match {
        case "c" => {
          if (foundCpp) return err("Found multiple \"c\" modifiers.")
          foundCpp = true
        }
        case "j" => {
          if (foundJava) return err("Found multiple \"j\" modifiers.")
          foundJava = true
        }
        case "o" => {
          if (foundObjc) return err("Found multiple \"o\" modifiers.")
          foundObjc = true
        }
        case _ => return err("Invalid modifier \"" + part.name + "\"")
      }
    success(Ext(foundJava, foundCpp, foundObjc))
  }

  def typeDef: Parser[TypeDef] = record | enum | interface

  def record: Parser[Record] = "record" ~> extRecord ~ bracesList(field | const) ~ opt(deriving) ^^ {
    case ext~items~deriving => {
      val fields = items collect {case f: Field => f}
      val consts = items collect {case c: Const => c}
      val derivingTypes = deriving.getOrElse(Set[DerivingType]())
      Record(ext, fields, consts, derivingTypes)
    }
  }
  def field: Parser[Field] = doc ~ ident ~ ":" ~ typeRef ^^ {
    case doc~ident~_~typeRef => Field(ident, typeRef, doc)
  }
  def deriving: Parser[Set[DerivingType]] = "deriving" ~> parens(rep1sepend(ident, ",")) ^^ {
    _.map(ident => ident.name match {
      case "eq" => Record.DerivingType.Eq
      case "ord" => Record.DerivingType.Ord
      case _ => return err( s"""Unrecognized deriving type "${ident.name}"""")
    }).toSet
  }

  def enum: Parser[Enum] = "enum" ~> bracesList(enumOption) ^^ Enum.apply
  def enumOption: Parser[Enum.Option] = doc ~ ident ^^ {
    case doc~ident => Enum.Option(ident, doc)
  }

  def interface: Parser[Interface] = "interface" ~> extInterface ~ bracesList(method | const) ^^ {
    case ext~items => {
      val methods = items collect {case m: Method => m}
      val consts = items collect {case c: Const => c}
      Interface(ext, methods, consts)
    }
  }

  def staticLabel: Parser[Boolean] = ("static ".r | "".r) ^^ {
    case "static " => true
    case "" => false
  }
  def constLabel: Parser[Boolean] = ("const ".r | "".r) ^^ {
    case "const " => true
    case "" => false
  }
  def method: Parser[Interface.Method] = doc ~ staticLabel ~ constLabel ~ ident ~ parens(repsepend(field, ",")) ~ opt(ret) ^^ {
    case doc~staticLabel~constLabel~ ident~params~ret => Interface.Method(ident, params, ret, doc, staticLabel, constLabel)
  }
  def ret: Parser[TypeRef] = ":" ~> typeRef

  def boolValue: Parser[Boolean] = "([Tt]rue)|([Ff]alse)".r ^^ {s: String => s.toBoolean}
  def intValue: Parser[Long] =  """[+-]?[0-9][0-9]*""".r ^^ {s: String => s.toLong}
  def floatValue: Parser[Double] = """[+-]?[0-9]*\.[0-9]*([Ee][+-]?[0-9]*)?""".r ^^ {s: String => s.toDouble}
  def stringValue: Parser[String] = """\"([^\\\"]|(\\.))*\"""".r
  def constRef: Parser[ConstRef] = ident ^^ { ident => new ConstRef(ident) }
  def enumValue: Parser[EnumValue] = ident ~ "::" ~ ident ^^ { case ty~_~value => new EnumValue(ty, value) }
  def compositeValue: Parser[Map[String, Any]] = commaList(ident ~ "=" ~ value ^^ {
    case ident~_~value => (ident.name, value)
  }) ^^ {
    s: Seq[(String, Any)] => s.toMap
  }

  // Integer before float for compatibility; ident for enum option
  def value = floatValue | intValue | boolValue | stringValue | enumValue | constRef | compositeValue

  def const: Parser[Const] = doc ~ "const" ~ ident ~ ":" ~ typeRef ~ "=" ~ value ^^ {
    case doc~_~ident~_~typeRef~_~value => Const(ident, typeRef, value, doc)
  }

  def typeRef: Parser[TypeRef] = typeExpr ^^ TypeRef
  def typeExpr: Parser[TypeExpr] = ident ~ typeList(typeExpr) ^^ {
    case ident~typeArgs => TypeExpr(ident, typeArgs)
  }

  def ident: Parser[Ident] = pos(regex("""[A-Za-z_][A-Za-z_0-9]*""".r)) ^^ {
    case (s, p) => Ident(s, fileStack.top, p)
  }

  def doc: Parser[Doc] = rep(regex("""#[^\n\r]*""".r) ^^ (_.substring(1))) ^^ Doc

  def parens[T](inner: Parser[T]): Parser[T] = surround("(", ")", inner)
  def typeList[T](inner: Parser[T]): Parser[Seq[T]] = surround("<", ">", rep1sepend(inner, ",")) | success(Seq.empty)
  def bracesList[T](inner: Parser[T]): Parser[Seq[T]] = surround("{", "}", rep(inner <~ ";"))
  def commaList[T](inner: Parser[T]): Parser[Seq[T]] = surround("{", "}", rep1sepend(inner, ","))

  // Generic helpers

  def surround[T](left: Parser[Any], right: Parser[Any], inner: Parser[T]): Parser[T] = left ~> inner <~ right

  // Like 'repsep' and 'rep1sep' except allows an optional trailing separator.
  def repsepend[T,U](inner: Parser[T], sep: Parser[U]): Parser[Seq[T]] = rep1sepend(inner, sep) | success(Seq.empty)
  def rep1sepend[T,U](inner: Parser[T], sep: Parser[U]): Parser[Seq[T]] = rep1sep(inner, sep) <~ opt(sep)

  // To get the input line/column.
  def pos[T](inner: Parser[T]): Parser[(T, Loc)] = positioned(withPos(inner)) ^^ {
    case wp => (wp.v, toLoc(fileStack.top, wp.pos))
  }
  private case class WithPos[T](v: T) extends Positional
  private def withPos[T](inner: Parser[T]): Parser[WithPos[T]] = inner ^^ {
    case i => WithPos(i)
  }
}

def toLoc(file: File, pos: Position) = Loc(file, pos.line, pos.column)

def slurpReader(in: java.io.Reader): String = {
  var buf = new Array[Char](4 * 1024)
  var pos = 0
  while (true) {
    val space = buf.length - pos
    val read = in.read(buf, pos, space)
    if (read == -1) {
      val r = new Array[Char](pos)
      return new String(buf, 0, pos)
    }
    pos += read
    if (pos >= buf.length) {
      val newBuf = new Array[Char](buf.length * 2)
      System.arraycopy(buf, 0, newBuf, 0, pos)
      buf = newBuf
    }
  }
  throw new AssertionError("unreachable")  // stupid Scala
}

def parse(origin: String, in: java.io.Reader): Either[Error,IdlFile] = {
  val s = slurpReader(in)
  IdlParser.parseAll(IdlParser.idlFile(origin), s) match {
    case IdlParser.Success(v: IdlFile, _) => Right(v)
    case IdlParser.NoSuccess(msg, input) => Left(Error(toLoc(fileStack.top, input.pos), msg))
  }
}

def parseFile(idlFile: File): Seq[TypeDecl] = {
  visitedFiles.add(idlFile)
  fileStack.push(idlFile)
  val fin = new FileInputStream(idlFile)
  try {
    parse(idlFile.getName, new InputStreamReader(fin, "UTF-8")) match {
      case Left(err) =>
        System.err.println(err)
        System.exit(1); return null;
      case Right(idl) => {
        var types = idl.typeDecls
        idl.imports.foreach(x => {
          if (fileStack.contains(x)) {
            throw new AssertionError("Circular import detected!")
          }
          if (!visitedFiles.contains(x)) {
            types = parseFile(x) ++ types
          }
        })
        types
      }
    }
  }
  finally {
    fin.close()
    fileStack.pop()
  }
}

}
