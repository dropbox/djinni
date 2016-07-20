package djinni

import djinni.ast._
import djinni.ast.Record.DerivingType.DerivingType
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter
import java.util.{Map => JMap}
import scala.collection.JavaConversions._
import scala.collection.mutable

class YamlGenerator(spec: Spec) extends Generator(spec) {

  val cppMarshal = new CppMarshal(spec)
  val objcMarshal = new ObjcMarshal(spec)
  val objcppMarshal = new ObjcppMarshal(spec)
  val javaMarshal = new JavaMarshal(spec)
  val jniMarshal = new JNIMarshal(spec)

  case class QuotedString(str: String) // For anything that migt require escaping

  private def writeYamlFile(name: String, origin: String, f: IndentWriter => Unit): Unit = {
    createFile(spec.yamlOutFolder.get, name, out => new IndentWriter(out, "  "), w => {
      w.wl("# AUTOGENERATED FILE - DO NOT MODIFY!")
      w.wl("# This file generated by Djinni from " + origin)
      f(w)
    })
  }

  private def writeYamlFile(tds: Seq[InternTypeDecl]): Unit = {
    val origins = tds.map(_.origin).distinct.sorted.mkString(", ")
    writeYamlFile(spec.yamlOutFile.get, origins, w => {
      // Writing with SnakeYAML creates loads of cluttering and unnecessary tags, so write manually.
      // We're not doing anything complicated anyway and it's good to have human readable output.
      for(td <- tds) {
        w.wl("---")
        write(w, td)
      }
    })
  }

  private def writeYamlFile(ident: String, origin: String, td: InternTypeDecl): Unit =
    writeYamlFile(spec.yamlPrefix + ident + ".yaml", origin, w => {
      write(w, td)
  })

  private def write(w: IndentWriter, td: TypeDecl) {
    write(w, preamble(td))
    w.wl("cpp:").nested { write(w, cpp(td)) }
    w.wl("objc:").nested { write(w, objc(td)) }
    w.wl("objcpp:").nested { write(w, objcpp(td)) }
    w.wl("java:").nested { write(w, java(td)) }
    w.wl("jni:").nested { write(w, jni(td)) }
  }

  private def write(w: IndentWriter, m: Map[String, Any]) {
    for((k, v) <- m) {
      w.w(k + ": ")
      v match {
        case s: String => write(w, s)
        case s: QuotedString => write(w, s)
        case m: Map[_, _] => w.wl.nested { write(w, m.asInstanceOf[Map[String, Any]]) }
        case s: Seq[_] => write(w, s)
        case b: Boolean => write(w, b)
        case _ => throw new AssertionError("unexpected map value")
      }
    }
  }

  private def write(w: IndentWriter, s: Seq[Any]) {
    // The only arrays we have are small enough to use flow notation
    w.wl(s.mkString("[", ",", "]"))
  }

  private def write(w: IndentWriter, b: Boolean) {
    w.wl(if(b) "true" else "false")
  }

  private def write(w: IndentWriter, s: String) {
    if(s.isEmpty) w.wl(q("")) else w.wl(s)
  }

  private def write(w: IndentWriter, s: QuotedString) {
    if(s.str.isEmpty) w.wl(q("")) else w.wl("'" + s.str.replaceAllLiterally("'", "''") + "'")
  }

  private def preamble(td: TypeDecl) = Map[String, Any](
    "name" -> (spec.yamlPrefix + td.ident.name),
    "typedef" -> QuotedString(typeDef(td)),
    "params" -> td.params.collect { case p: TypeParam => p.ident.name },
    "prefix" -> spec.yamlPrefix
  )

  private def typeDef(td: TypeDecl) = {
    def ext(e: Ext): String = (if(e.cpp) " +c" else "") + (if(e.objc) " +o" else "") + (if(e.java) " +j" else "")
    def deriving(r: Record) = {
      if(r.derivingTypes.isEmpty) {
        ""
      } else {
        r.derivingTypes.collect {
          case Record.DerivingType.Eq => "eq"
          case Record.DerivingType.Ord => "ord"
        }.mkString(" deriving(", ", ", ")")
      }
    }
    td.body match {
      case i: Interface => "interface" + ext(i.ext)
      case r: Record => "record" + ext(r.ext) + deriving(r)
      case e: Enum => "enum"
    }
  }

  private def cpp(td: TypeDecl) = Map[String, Any](
    "typename" -> QuotedString(cppMarshal.fqTypename(td.ident, td.body)),
    "header" -> QuotedString(cppMarshal.include(td.ident)),
    "byValue" -> cppMarshal.byValue(td)
  )

  private def objc(td: TypeDecl) = Map[String, Any](
    "typename" -> QuotedString(objcMarshal.fqTypename(td.ident, td.body)),
    "header" -> QuotedString(objcMarshal.include(td.ident)),
    "boxed" -> QuotedString(objcMarshal.boxedTypename(td)),
    "pointer" -> objcMarshal.isPointer(td),
    "hash" -> QuotedString("%s.hash")
  )

  private def objcpp(td: TypeDecl) = Map[String, Any](
    "translator" -> QuotedString(objcppMarshal.helperName(mexpr(td))),
    "header" -> QuotedString(objcppMarshal.include(meta(td)))
  )

  private def java(td: TypeDecl) = Map[String, Any](
    "typename" -> QuotedString(javaMarshal.fqTypename(td.ident, td.body)),
    "boxed" -> QuotedString(javaMarshal.fqTypename(td.ident, td.body)),
    "reference" -> javaMarshal.isReference(td),
    "generic" -> true,
    "hash" -> QuotedString("%s.hashCode()"),
    "writeToParcel" -> QuotedString("%s.writeToParcel(out, flags)"),
    "readFromParcel" -> QuotedString("new %s(in)")
  )

  private def jni(td: TypeDecl) = Map[String, Any](
    "translator" -> QuotedString(jniMarshal.helperName(mexpr(td))),
    "header" -> QuotedString(jniMarshal.include(td.ident)),
    "typename" -> jniMarshal.fqParamType(mexpr(td)),
    "typeSignature" -> QuotedString(jniMarshal.fqTypename(td.ident, td.body))
  )

  // TODO: there has to be a way to do all this without the MExpr/Meta conversions?
  private def mexpr(td: TypeDecl) = MExpr(meta(td), List())

  private def meta(td: TypeDecl) = {
    val defType = td.body match {
      case i: Interface => DInterface
      case r: Record => DRecord
      case e: Enum => DEnum
    }
    MDef(td.ident, 0, defType, td.body)
  }

  override def generate(idl: Seq[TypeDecl]) {
    val internOnly = idl.collect { case itd: InternTypeDecl => itd }.sortWith(_.ident.name < _.ident.name)
    if(spec.yamlOutFile.isDefined) {
      writeYamlFile(internOnly)
    } else {
      for(td <- internOnly) {
        writeYamlFile(td.ident, td.origin, td)
      }
    }
  }

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    // unused
  }

  override def generateInterface(origin: String, ident: Ident, doc: Doc, typeParams: Seq[TypeParam], i: Interface) {
    // unused
  }

  override def generateRecord(origin: String, ident: Ident, doc: Doc, params: Seq[TypeParam], r: Record) {
    // unused
  }
}

object YamlGenerator {
  def metaFromYaml(td: ExternTypeDecl) = MExtern(
    td.ident.name.stripPrefix(td.properties("prefix").toString), // Make sure the generator uses this type with its original name for all intents and purposes
    td.params.size,
    defType(td),
    td.body,
    MExtern.Cpp(
      nested(td, "cpp")("typename").toString,
      nested(td, "cpp")("header").toString,
      nested(td, "cpp")("byValue").asInstanceOf[Boolean]),
    MExtern.Objc(
      nested(td, "objc")("typename").toString,
      nested(td, "objc")("header").toString,
      nested(td, "objc")("boxed").toString,
      nested(td, "objc")("pointer").asInstanceOf[Boolean],
      nested(td, "objc")("hash").toString),
    MExtern.Objcpp(
      nested(td, "objcpp")("translator").toString,
      nested(td, "objcpp")("header").toString),
    MExtern.Java(
      nested(td, "java")("typename").toString,
      nested(td, "java")("boxed").toString,
      nested(td, "java")("reference").asInstanceOf[Boolean],
      nested(td, "java")("generic").asInstanceOf[Boolean],
      nested(td, "java")("hash").toString,
      nested(td, "java")("writeToParcel").toString,
      nested(td, "java")("readFromParcel").toString),
    MExtern.Jni(
      nested(td, "jni")("translator").toString,
      nested(td, "jni")("header").toString,
      nested(td, "jni")("typename").toString,
      nested(td, "jni")("typeSignature").toString)
  )

  private def nested(td: ExternTypeDecl, key: String) = {
    td.properties.get(key).collect { case m: JMap[_, _] => m.collect { case (k: String, v: Any) => (k, v) } } getOrElse(Map[String, Any]())
  }

  private def defType(td: ExternTypeDecl) = td.body match {
    case i: Interface => DInterface
    case r: Record => DRecord
    case e: Enum => DEnum
  }
}
