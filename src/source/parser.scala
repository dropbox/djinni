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

import java.io.{File, FileNotFoundException, InputStreamReader, FileInputStream, Writer}

import djinni.ast.Interface.Method
import djinni.ast.Record.DerivingType.DerivingType
import djinni.syntax._
import djinni.ast._
import java.util.{Map => JMap}
import org.yaml.snakeyaml.Yaml
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Breaks._
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{Position, Positional}

case class Parser(includePaths: List[String]) {

val visitedFiles = mutable.Set[File]()
val fileStack = mutable.Stack[File]()

private object IdlParser extends RegexParsers {
  override protected val whiteSpace = """[ \t\n\r]+""".r

  def idlFile(origin: String): Parser[IdlFile] = rep(importFileRef) ~ rep(typeDecl(origin)) ^^ { case imp~types => IdlFile(imp, types) }

  def importFileRef(): Parser[FileRef] = {
    ("@" ~> directive) ~ ("\"" ~> filePath <~ "\"") ^^ {
      case "import" ~ x => {
        new IdlFileRef(importFile(x))
      }
      case "extern" ~ x => {
        new ExternFileRef(importFile(x))
      }
    }
  }

  def importFile(fileName: String): File = {
    var file: Option[File] = None

    val path = includePaths.find(path => {
      val relPath = if (path.isEmpty) fileStack.top.getParent() else path
      val tmp = new File(relPath, fileName)
      val exists = tmp.exists
      if (exists) file = Some(tmp)
      exists
    })

    if (file.isEmpty) throw new FileNotFoundException("Unable to find file \"" + fileName + "\" at " + fileStack.top.getCanonicalPath)

    return file.get
  }

  def filePath = "[^\"]*".r

  def directive = importDirective | externDirective
  def importDirective = "import".r
  def externDirective = "extern".r

  def typeDecl(origin: String): Parser[TypeDecl] = doc ~ ident ~ typeList(ident ^^ TypeParam) ~ "=" ~ typeDef ^^ {
    case doc~ident~typeParams~_~body => InternTypeDecl(ident, typeParams, body, doc, origin)
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

  def typeDef: Parser[TypeDef] = record | enum | flags | interface

  def recordHeader = "record" ~> extRecord
  def record: Parser[Record] = recordHeader ~ bracesList(field | const) ~ opt(deriving) ^^ {
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
      case "parcelable" => Record.DerivingType.AndroidParcelable
      case _ => return err( s"""Unrecognized deriving type "${ident.name}"""")
    }).toSet
  }

  def flagsAll = "all".r
  def flagsNone = "none".r

  def enumHeader = "enum".r
  def flagsHeader = "flags".r
  def enum: Parser[Enum] = enumHeader ~> bracesList(enumOption) ^^ {
    case items => Enum(items, false)
  }
  def flags: Parser[Enum] = flagsHeader ~> bracesList(flagsOption) ^^ {
    case items => Enum(items, true)
  }

  def enumOption: Parser[Enum.Option] = doc ~ ident ^^ {
    case doc~ident => Enum.Option(ident, doc, None)
  }
  def flagsOption: Parser[Enum.Option] = doc ~ ident ~ opt("=" ~> (flagsAll | flagsNone)) ^^ {
    case doc~ident~None => Enum.Option(ident, doc, None)
    case doc~ident~Some("all") => Enum.Option(ident, doc, Some(Enum.SpecialFlag.AllFlags))
    case doc~ident~Some("none") => Enum.Option(ident, doc, Some(Enum.SpecialFlag.NoFlags))
  }

  def interfaceHeader = "interface" ~> extInterface
  def interface: Parser[Interface] = interfaceHeader ~ bracesList(method | const) ^^ {
    case ext~items => {
      val methods = items collect {case m: Method => m}
      val consts = items collect {case c: Const => c}
      Interface(ext, methods, consts)
    }
  }

  def externTypeDecl: Parser[TypeDef] = externEnum | externFlags | externInterface | externRecord
  def externEnum: Parser[Enum] = enumHeader ^^ { case _ => Enum(List(), false) }
  def externFlags: Parser[Enum] = flagsHeader ^^ { case _ => Enum(List(), true) }
  def externRecord: Parser[Record] = recordHeader ~ opt(deriving) ^^ { case ext~deriving => Record(ext, List(), List(), deriving.getOrElse(Set[DerivingType]())) }
  def externInterface: Parser[Interface] = interfaceHeader ^^ { case ext => Interface(ext, List(), List()) }

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

def parseExtern(origin: String, in: java.io.Reader): Either[Error, Seq[TypeDecl]] = {
  val yaml = new Yaml();
  val tds = mutable.MutableList[TypeDecl]()
  for(properties <- yaml.loadAll(in).collect { case doc: JMap[_, _] => doc.collect { case (k: String, v: Any) => (k, v) } }) {
    val name = properties("name").toString
    val ident = Ident(name, fileStack.top, Loc(fileStack.top, 1, 1))
    val params = properties.get("params").fold(Seq[TypeParam]())(_.asInstanceOf[java.util.ArrayList[String]].collect { case s: String => TypeParam(Ident(s.asInstanceOf[String], fileStack.top, Loc(fileStack.top, 1, 1))) })

    IdlParser.parseAll(IdlParser.externTypeDecl, properties("typedef").toString) match {
      case IdlParser.Success(ty: TypeDef, _) =>
        tds += ExternTypeDecl(ident, params, ty, properties.toMap, origin)
      case IdlParser.NoSuccess(msg, input) =>
        return Left(Error(Loc(fileStack.top, 1, 1), "'typedef' has an unrecognized value"))
    }
  }
  Right(tds)
}

def parseExternFile(externFile: File, inFileListWriter: Option[Writer]) : Seq[TypeDecl] = {
  if (inFileListWriter.isDefined) {
    inFileListWriter.get.write(externFile + "\n")
  }

  visitedFiles.add(externFile)
  fileStack.push(externFile)
  val fin = new FileInputStream(externFile)
  try {
    parseExtern(externFile.getName, new InputStreamReader(fin, "UTF-8")) match {
      case Right(x) => x
      case Left(err) => throw err.toException
    }
  }
  finally {
    fin.close()
    fileStack.pop()
  }
}

def normalizePath(path: File) : File = {
  return new File(java.nio.file.Paths.get(path.toString()).normalize().toString())
}

def parseFile(idlFile: File, inFileListWriter: Option[Writer]): Seq[TypeDecl] = {
  val normalizedIdlFile = normalizePath(idlFile)
  if (inFileListWriter.isDefined) {
    inFileListWriter.get.write(normalizedIdlFile + "\n")
  }

  visitedFiles.add(normalizedIdlFile)
  fileStack.push(normalizedIdlFile)
  val fin = new FileInputStream(normalizedIdlFile)
  try {
    parse(normalizedIdlFile.getName, new InputStreamReader(fin, "UTF-8")) match {
      case Left(err) =>
        System.err.println(err)
        System.exit(1); return null;
      case Right(idl) => {
        var types = idl.typeDecls
        idl.imports.foreach(x => {
          val normalized = normalizePath(x.file)
          if (fileStack.contains(normalized)) {
            throw new AssertionError("Circular import detected!")
          }
          if (!visitedFiles.contains(normalized)) {
            x match {
              case IdlFileRef(file) =>
                types = parseFile(normalized, inFileListWriter) ++ types
              case ExternFileRef(file) =>
                types = parseExternFile(normalized, inFileListWriter) ++ types
            }
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
