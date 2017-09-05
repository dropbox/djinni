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

import java.util

import djinni.ast.Record.DerivingType
import djinni.ast.Record.DerivingType.DerivingType
import djinni.ast.Record.DerivingType.DerivingType
import djinni.syntax._
import djinni.ast._
import djinni.meta._
import scala.collection.immutable
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

package object resolver {

type Scope = immutable.Map[String,Meta]

def resolve(metas: Scope, idl: Seq[TypeDecl]): Option[Error] = {

  try {
    var topScope = metas

    // Load top-level names into scope
    val topLevelDupeChecker = new DupeChecker("type")
    for (typeDecl <- idl) {
      topLevelDupeChecker.check(typeDecl.ident)

      def defType = typeDecl.body match {
        case e: Enum =>
          if (!typeDecl.params.isEmpty) {
            throw Error(typeDecl.ident.loc, "enums can't have type parameters").toException
          }
          DEnum
        case r: Record => DRecord
        case i: Interface => DInterface
      }
      topScope = topScope.updated(typeDecl.ident.name, typeDecl match {
        case td: InternTypeDecl => MDef(typeDecl.ident.name, typeDecl.params.length, defType, typeDecl.body)
        case td: ExternTypeDecl => YamlGenerator.metaFromYaml(td)
      })
    }

    // Resolve everything
    for (typeDecl <- idl) {
      var scope = topScope

      // Load type parameters into scope
      val typeParamDupeChecker = new DupeChecker("type parameter")
      for (typeParam <- typeDecl.params) {
        // Check for name conflicts.
        typeParamDupeChecker.check(typeParam.ident)

        scope = scope.updated(typeParam.ident.name, MParam(typeParam.ident.name))
      }

      resolve(scope, typeDecl.body)
    }

    for (typeDecl <- idl) {
      resolveConst(typeDecl.body)
    }

  }
  catch {
    case e: Error.Exception => return Some(e.error)
  }
  None
}

private def resolve(scope: Scope, typeDef: TypeDef) {
  typeDef match {
    case e: Enum => resolveEnum(scope, e)
    case r: Record => resolveRecord(scope, r)
    case i: Interface => resolveInterface(scope, i)
  }
}

private def resolveEnum(scope: Scope, e: Enum) {
  val dupeChecker = new DupeChecker("enum option")
  for (o <- e.options) {
    dupeChecker.check(o.ident)
  }
}

private def resolveConst(typeDef: TypeDef) {
  def f(consts: Seq[Const]): Unit = {
    val resolvedConsts = new ArrayBuffer[Const]
    for (c <- consts) {
      try {
        constTypeCheck(c.ty.resolved, c.value, resolvedConsts)
      } catch {
        case e: AssertionError =>
          throw Error(c.ident.loc, e.getMessage()).toException
      }
      resolvedConsts.append(c)
    }
  }
  typeDef match {
    case e: Enum =>
    case r: Record => f(r.consts)
    case i: Interface => f(i.consts)
  }
}

// TODO: Have test for this guy
private def constTypeCheck(ty: MExpr, value: Any, resolvedConsts: Seq[Const]) {
  // Check existing consts
  if (value.isInstanceOf[ConstRef]) {
    val ref = value.asInstanceOf[ConstRef]
    resolvedConsts.map(c => {
      if (c.ident.name == ref.name) {
        if (c.ty.resolved == ty)
          return
        else
          throw Error(ref.loc, s"Const ${ref.name} does not match type").toException
      }
    })
    throw new AssertionError(s"Const ${ref.name} does not exist")
  }
  ty.base match {
    case MBinary | MList | MSet | MMap =>
      throw new AssertionError("Type not allowed for constant")
    case MString =>
      if (!value.isInstanceOf[String] ||
          !value.asInstanceOf[String].startsWith("\"") ||
          !value.asInstanceOf[String].endsWith("\""))
        throw new AssertionError("Const type mismatch: string")
    case MOptional =>
      constTypeCheck(ty.args.apply(0), value, resolvedConsts)
    case t: MPrimitive => t.idlName match {
      case "bool" =>
        if (!value.isInstanceOf[Boolean])
          throw new AssertionError("Const type mismatch: bool")
      case "i8" =>
        assert(value.isInstanceOf[Long], "Const type mismatch: i8")
        assert(value.asInstanceOf[Long].toByte == value, "Const value not a valid i8")
      case "i16" =>
        assert(value.isInstanceOf[Long], "Const type mismatch: i16")
        assert(value.asInstanceOf[Long].toShort == value, "Const value not a valid i16")
      case "i32" =>
        assert(value.isInstanceOf[Long], "Const type mismatch: i32")
        assert(value.asInstanceOf[Long].toInt == value, "Const value not a valid i32")
      case "i64" =>
        assert(value.isInstanceOf[Long], "Const type mismatch: i64")
      case "f32" => value match {
        case i: Long =>
          assert(i.toFloat == value, "Const value not a valid f32")
        case f: Double =>
          assert(f.toFloat == value, "Const value not a valid f32")
        case _ => throw new AssertionError("Const type mismatch: f32")
      }
      case "f64" => value match {
        case i: Long =>
          assert(i.toDouble == value, "Const value not a valid f64")
        case f: Double =>
        case _ => throw new AssertionError("Const type mismatch: f64")
      }
    }
    case d: MDef => d.defType match {
      case DInterface =>
        throw new AssertionError("Type not allowed for constant")
      case DRecord =>
        if (!value.isInstanceOf[Map[_, _]])
          throw new AssertionError("Record value not valid")
        val record = d.body.asInstanceOf[Record]
        val map = value.asInstanceOf[Map[String, Any]]
        for (field <- record.fields) {
          map.get(field.ident.name) match {
            case Some(v) =>
              constTypeCheck(field.ty.resolved, v, resolvedConsts)
            case None =>
              throw new AssertionError(s"Field ${field.ident.name} does not exist in const value")
          }
        }
        if (record.fields.size != map.size)
          throw new AssertionError("Record field number mismatch")
      case DEnum => {
        if (!value.isInstanceOf[EnumValue])
          throw new AssertionError(s"Const type mismatch: enum ${d.name}")
        val opt = value.asInstanceOf[EnumValue]
        if (opt.ty.name != d.name)
          throw new AssertionError(s"Const type mismatch: enum ${d.name}")
        val enum = d.body.asInstanceOf[Enum]
        val options = enum.options.map(f => f.ident.name)
        if (!options.contains(opt.name))
          throw new AssertionError(s"Const type mismatch: enum ${d.name} does not have option ${opt.name}")
      }
    }
    case e: MExtern => throw new AssertionError("Extern type not allowed for constant")
    case _ => throw new AssertionError("Const type cannot be resolved")
  }
}

private def resolveRecord(scope: Scope, r: Record) {
  val dupeChecker = new DupeChecker("record field")
  for (f <- r.fields) {
    dupeChecker.check(f.ident)
    resolveRef(scope, f.ty)
    // Deriving Type Check
    if (r.ext.any())
      if (r.derivingTypes.contains(DerivingType.Ord)) {
        throw new Error(f.ident.loc, "Cannot safely implement Ord on a record that may be extended").toException
      } else if (r.derivingTypes.contains(DerivingType.Eq)) {
        throw new Error(f.ident.loc, "Cannot safely implement Eq on a record that may be extended").toException
      }
    f.ty.resolved.base match {
      case MBinary | MList | MSet | MMap =>
        if (r.derivingTypes.contains(DerivingType.Ord))
          throw new Error(f.ident.loc, "Cannot compare collections in Ord deriving (Java limitation)").toException
      case MString =>
      case MDate =>
      case MOptional =>
        if (r.derivingTypes.contains(DerivingType.Ord))
          throw new Error(f.ident.loc, "Cannot compare optional in Ord deriving").toException
      case t: MPrimitive => t.idlName match {
        case "bool" =>
          if (r.derivingTypes.contains(DerivingType.Ord))
            throw new Error(f.ident.loc, "Cannot compare booleans in Ord deriving").toException
        case _ =>
      }
      case df: MDef => df.defType match {
        case DInterface =>
          throw new Error(f.ident.loc, "Interface reference cannot live in a record").toException
        case DRecord =>
          val record = df.body.asInstanceOf[Record]
          if (!r.derivingTypes.subsetOf(record.derivingTypes))
            throw new Error(f.ident.loc, s"Some deriving required is not implemented in record ${f.ident.name}").toException
        case DEnum =>
      }
      case e: MExtern => e.defType match {
        case DInterface =>
          throw new Error(f.ident.loc, "Interface reference cannot live in a record").toException
        case DRecord =>
          val record = e.body.asInstanceOf[Record]
          if (!r.derivingTypes.subsetOf(record.derivingTypes))
            throw new Error(f.ident.loc, s"Some deriving required is not implemented in record ${f.ident.name}").toException
        case DEnum =>
      }
      case _ => throw new AssertionError("Type cannot be resolved")
    }
  }
  // Name checking for constants. Type check only possible after resolving record field types.
  for (c <- r.consts) {
    dupeChecker.check(c.ident)
    resolveRef(scope, c.ty)
  }
}

private def resolveInterface(scope: Scope, i: Interface) {
  // Const and static methods are only allowed on +c (only) interfaces
  if (i.ext.java || i.ext.objc) {
    for (m <- i.methods) {
      if (m.static)
        throw Error(m.ident.loc, "static not allowed for +j or +o interfaces").toException
      if (m.const)
        throw Error(m.ident.loc, "const method not allowed for +j or +o +p interfaces").toException
    }
  }

  // Static+const isn't valid
  if (i.ext.cpp) {
    for (m <- i.methods) {
      if (m.static && m.const)
        throw Error(m.ident.loc, "+c method cannot be both static and const").toException
    }
  }
  val dupeChecker = new DupeChecker("method")
  for (m <- i.methods) {
    dupeChecker.check(m.ident)
    for (p <- m.params) {
      resolveRef(scope, p.ty)
    }
    m.ret match {
      case Some(ty) => resolveRef(scope, ty)
      case _ =>
    }
  }
  // Name checking for constants. Type check only possible after resolving record field types.
  for (c <- i.consts) {
    dupeChecker.check(c.ident)
    resolveRef(scope, c.ty)
  }
}

private def resolveRef(scope: Scope, r: TypeRef) {
  if (r.resolved != null) throw new AssertionError("double-resolve?")
  r.resolved = buildMExpr(scope, r.expr)
}

private def buildMExpr(scope: Scope, e: TypeExpr): MExpr = {
  scope.get(e.ident.name) match {
    case Some(meta) => {
      if (meta.numParams != e.args.length) {
        throw Error(e.ident.loc, "incorrect number of arguments to type \"" + e.ident.name
                    + "\"; expecting " + meta.numParams + ", got " + e.args.length).toException
      }
      val margs = e.args.map(buildMExpr(scope, _))
      if (meta == MOptional && margs.head.base == MOptional) {
        // HACK: In Java, we use "null" for optionals, so we don't allow nested optionals.
        throw Error(e.ident.loc, "directly nested optionals not allowed").toException
      }
      MExpr(meta, margs)
    }
    case None =>
      throw Error(e.ident.loc, "unknown type \"" + e.ident.name + "\"").toException
  }
}

private class DupeChecker(kind: String)
{
  private val names = mutable.HashMap[String,Loc]()

  def check(ident: Ident) {
    names.put(ident.name, ident.loc) match {
      case Some(existing) =>
        throw Error(ident.loc, "duplicate " + kind + " \"" + ident.name + "\" (previous definition: " + existing + ")").toException
      case _ =>
    }
  }
}

}
