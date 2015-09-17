// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.dropbox.djinni.ideaplugin.psi.impl.*;

public interface DjinniTypes {

  IElementType BASIC_TYPE = new DjinniElementType("BASIC_TYPE");
  IElementType CONST_MEMBER = new DjinniElementType("CONST_MEMBER");
  IElementType CONST_NAMED_VALUE = new DjinniElementType("CONST_NAMED_VALUE");
  IElementType CONST_RECORD_MEMBER_ELEMENT = new DjinniElementType("CONST_RECORD_MEMBER_ELEMENT");
  IElementType CONST_REFERENCE = new DjinniElementType("CONST_REFERENCE");
  IElementType CONST_VALUE = new DjinniElementType("CONST_VALUE");
  IElementType DERIVING_PARAM = new DjinniElementType("DERIVING_PARAM");
  IElementType DERIVING_PARAM_LIST = new DjinniElementType("DERIVING_PARAM_LIST");
  IElementType ENUM_MEMBER = new DjinniElementType("ENUM_MEMBER");
  IElementType ENUM_TYPE_VARIANT = new DjinniElementType("ENUM_TYPE_VARIANT");
  IElementType ENUM_VALUE = new DjinniElementType("ENUM_VALUE");
  IElementType EXTERN_STATEMENT = new DjinniElementType("EXTERN_STATEMENT");
  IElementType GENERATOR = new DjinniElementType("GENERATOR");
  IElementType GENERIC_BASIC_TYPE = new DjinniElementType("GENERIC_BASIC_TYPE");
  IElementType GENERIC_BASIC_TYPE_DUAL_PARAMETER = new DjinniElementType("GENERIC_BASIC_TYPE_DUAL_PARAMETER");
  IElementType GENERIC_BASIC_TYPE_SINGLE_PARAMETER = new DjinniElementType("GENERIC_BASIC_TYPE_SINGLE_PARAMETER");
  IElementType IMPORT_STATEMENT = new DjinniElementType("IMPORT_STATEMENT");
  IElementType INTERFACE_FUNCTION_PARAM = new DjinniElementType("INTERFACE_FUNCTION_PARAM");
  IElementType INTERFACE_FUNCTION_PARAM_LIST = new DjinniElementType("INTERFACE_FUNCTION_PARAM_LIST");
  IElementType INTERFACE_MEMBER = new DjinniElementType("INTERFACE_MEMBER");
  IElementType INTERFACE_MEMBER_FUNCTION = new DjinniElementType("INTERFACE_MEMBER_FUNCTION");
  IElementType INTERFACE_TYPE_VARIANT = new DjinniElementType("INTERFACE_TYPE_VARIANT");
  IElementType PREDEFINED_TYPE = new DjinniElementType("PREDEFINED_TYPE");
  IElementType RECORD_MEMBER = new DjinniElementType("RECORD_MEMBER");
  IElementType RECORD_MEMBER_VARIABLE = new DjinniElementType("RECORD_MEMBER_VARIABLE");
  IElementType RECORD_TYPE_VARIANT = new DjinniElementType("RECORD_TYPE_VARIANT");
  IElementType TYPE_DEFINITION = new DjinniElementType("TYPE_DEFINITION");
  IElementType TYPE_REFERENCE = new DjinniElementType("TYPE_REFERENCE");

  IElementType AT = new DjinniTokenType("@");
  IElementType COLON = new DjinniTokenType(":");
  IElementType COMMENT = new DjinniTokenType("comment");
  IElementType EQ = new DjinniTokenType("=");
  IElementType IDENTIFIER = new DjinniTokenType("identifier");
  IElementType LEFT_BLOCK_BRACE = new DjinniTokenType("{");
  IElementType LEFT_GENERICS_BRACE = new DjinniTokenType("<");
  IElementType LEFT_PARAM_BRACE = new DjinniTokenType("(");
  IElementType LIST = new DjinniTokenType("list");
  IElementType LIST_SEPARATOR = new DjinniTokenType(",");
  IElementType MAP = new DjinniTokenType("map");
  IElementType NUMBER_LITERAL = new DjinniTokenType("number_literal");
  IElementType OPTIONAL = new DjinniTokenType("optional");
  IElementType PLUS = new DjinniTokenType("+");
  IElementType RIGHT_BLOCK_BRACE = new DjinniTokenType("}");
  IElementType RIGHT_GENERICS_BRACE = new DjinniTokenType(">");
  IElementType RIGHT_PARAM_BRACE = new DjinniTokenType(")");
  IElementType SEMICOLON = new DjinniTokenType(";");
  IElementType SET = new DjinniTokenType("set");
  IElementType SPACE = new DjinniTokenType("space");
  IElementType STRING_LITERAL = new DjinniTokenType("string_literal");
  IElementType TEXT = new DjinniTokenType("text");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == BASIC_TYPE) {
        return new DjinniBasicTypeImpl(node);
      }
      else if (type == CONST_MEMBER) {
        return new DjinniConstMemberImpl(node);
      }
      else if (type == CONST_NAMED_VALUE) {
        return new DjinniConstNamedValueImpl(node);
      }
      else if (type == CONST_RECORD_MEMBER_ELEMENT) {
        return new DjinniConstRecordMemberElementImpl(node);
      }
      else if (type == CONST_REFERENCE) {
        return new DjinniConstReferenceImpl(node);
      }
      else if (type == CONST_VALUE) {
        return new DjinniConstValueImpl(node);
      }
      else if (type == DERIVING_PARAM) {
        return new DjinniDerivingParamImpl(node);
      }
      else if (type == DERIVING_PARAM_LIST) {
        return new DjinniDerivingParamListImpl(node);
      }
      else if (type == ENUM_MEMBER) {
        return new DjinniEnumMemberImpl(node);
      }
      else if (type == ENUM_TYPE_VARIANT) {
        return new DjinniEnumTypeVariantImpl(node);
      }
      else if (type == ENUM_VALUE) {
        return new DjinniEnumValueImpl(node);
      }
      else if (type == EXTERN_STATEMENT) {
        return new DjinniExternStatementImpl(node);
      }
      else if (type == GENERATOR) {
        return new DjinniGeneratorImpl(node);
      }
      else if (type == GENERIC_BASIC_TYPE) {
        return new DjinniGenericBasicTypeImpl(node);
      }
      else if (type == GENERIC_BASIC_TYPE_DUAL_PARAMETER) {
        return new DjinniGenericBasicTypeDualParameterImpl(node);
      }
      else if (type == GENERIC_BASIC_TYPE_SINGLE_PARAMETER) {
        return new DjinniGenericBasicTypeSingleParameterImpl(node);
      }
      else if (type == IMPORT_STATEMENT) {
        return new DjinniImportStatementImpl(node);
      }
      else if (type == INTERFACE_FUNCTION_PARAM) {
        return new DjinniInterfaceFunctionParamImpl(node);
      }
      else if (type == INTERFACE_FUNCTION_PARAM_LIST) {
        return new DjinniInterfaceFunctionParamListImpl(node);
      }
      else if (type == INTERFACE_MEMBER) {
        return new DjinniInterfaceMemberImpl(node);
      }
      else if (type == INTERFACE_MEMBER_FUNCTION) {
        return new DjinniInterfaceMemberFunctionImpl(node);
      }
      else if (type == INTERFACE_TYPE_VARIANT) {
        return new DjinniInterfaceTypeVariantImpl(node);
      }
      else if (type == PREDEFINED_TYPE) {
        return new DjinniPredefinedTypeImpl(node);
      }
      else if (type == RECORD_MEMBER) {
        return new DjinniRecordMemberImpl(node);
      }
      else if (type == RECORD_MEMBER_VARIABLE) {
        return new DjinniRecordMemberVariableImpl(node);
      }
      else if (type == RECORD_TYPE_VARIANT) {
        return new DjinniRecordTypeVariantImpl(node);
      }
      else if (type == TYPE_DEFINITION) {
        return new DjinniTypeDefinitionImpl(node);
      }
      else if (type == TYPE_REFERENCE) {
        return new DjinniTypeReferenceImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
