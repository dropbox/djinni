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

import djinni.ast.TypeDef
import scala.collection.immutable

package object meta {

case class MExpr(base: Meta, args: Seq[MExpr])

abstract sealed class Meta
{
  val numParams: Int
}

case class MParam(name: String) extends Meta { val numParams = 0 }
case class MDef(name: String, override val numParams: Int, defType: DefType, body: TypeDef) extends Meta
abstract sealed class MOpaque extends Meta { val idlName: String }

abstract sealed class DefType
case object DEnum extends DefType
case object DInterface extends DefType
case object DRecord extends DefType

case class MPrimitive(_idlName: String, jName: String, jniName: String, cName: String, jBoxed: String, jSig: String, objcName: String, objcBoxed: String) extends MOpaque { val numParams = 0; val idlName = _idlName }
case object MString extends MOpaque { val numParams = 0; val idlName = "string" }
case object MBinary extends MOpaque { val numParams = 0; val idlName = "binary" }
case object MOptional extends MOpaque { val numParams = 1; val idlName = "optional" }
case object MList extends MOpaque { val numParams = 1; val idlName = "list" }
case object MSet extends MOpaque { val numParams = 1; val idlName = "set" }
case object MMap extends MOpaque { val numParams = 2; val idlName = "map" }

val defaults: Map[String,MOpaque] = immutable.HashMap(
  ("i8",   MPrimitive("i8",   "byte",    "jbyte",    "int8_t",  "Byte",    "B", "int8_t",  "NSNumber")),
  ("i16",  MPrimitive("i16",  "short",   "jshort",   "int16_t", "Short",   "S", "int16_t", "NSNumber")),
  ("i32",  MPrimitive("i32",  "int",     "jint",     "int32_t", "Integer", "I", "int32_t", "NSNumber")),
  ("i64",  MPrimitive("i64",  "long",    "jlong",    "int64_t", "Long",    "J", "int64_t", "NSNumber")),
  ("f64",  MPrimitive("f64",  "double",  "jdouble",  "double",  "Double",  "D", "double",  "NSNumber")),
  ("bool", MPrimitive("bool", "boolean", "jboolean", "bool",    "Boolean", "Z", "BOOL",    "NSNumber")),
  ("string", MString),
  ("binary", MBinary),
  ("optional", MOptional),
  ("list", MList),
  ("set", MSet),
  ("map", MMap))
}
