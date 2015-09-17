// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.dropbox.djinni.ideaplugin.psi.DjinniTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DjinniParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == BASIC_TYPE) {
      r = basicType(b, 0);
    }
    else if (t == CONST_MEMBER) {
      r = constMember(b, 0);
    }
    else if (t == CONST_NAMED_VALUE) {
      r = constNamedValue(b, 0);
    }
    else if (t == CONST_RECORD_MEMBER_ELEMENT) {
      r = constRecordMemberElement(b, 0);
    }
    else if (t == CONST_REFERENCE) {
      r = constReference(b, 0);
    }
    else if (t == CONST_VALUE) {
      r = constValue(b, 0);
    }
    else if (t == DERIVING_PARAM) {
      r = derivingParam(b, 0);
    }
    else if (t == DERIVING_PARAM_LIST) {
      r = derivingParamList(b, 0);
    }
    else if (t == ENUM_MEMBER) {
      r = enumMember(b, 0);
    }
    else if (t == ENUM_TYPE_VARIANT) {
      r = enumTypeVariant(b, 0);
    }
    else if (t == ENUM_VALUE) {
      r = enumValue(b, 0);
    }
    else if (t == EXTERN_STATEMENT) {
      r = externStatement(b, 0);
    }
    else if (t == GENERATOR) {
      r = generator(b, 0);
    }
    else if (t == GENERIC_BASIC_TYPE) {
      r = genericBasicType(b, 0);
    }
    else if (t == GENERIC_BASIC_TYPE_DUAL_PARAMETER) {
      r = genericBasicTypeDualParameter(b, 0);
    }
    else if (t == GENERIC_BASIC_TYPE_SINGLE_PARAMETER) {
      r = genericBasicTypeSingleParameter(b, 0);
    }
    else if (t == IMPORT_STATEMENT) {
      r = importStatement(b, 0);
    }
    else if (t == INTERFACE_FUNCTION_PARAM) {
      r = interfaceFunctionParam(b, 0);
    }
    else if (t == INTERFACE_FUNCTION_PARAM_LIST) {
      r = interfaceFunctionParamList(b, 0);
    }
    else if (t == INTERFACE_MEMBER) {
      r = interfaceMember(b, 0);
    }
    else if (t == INTERFACE_MEMBER_FUNCTION) {
      r = interfaceMemberFunction(b, 0);
    }
    else if (t == INTERFACE_TYPE_VARIANT) {
      r = interfaceTypeVariant(b, 0);
    }
    else if (t == PREDEFINED_TYPE) {
      r = predefinedType(b, 0);
    }
    else if (t == RECORD_MEMBER) {
      r = recordMember(b, 0);
    }
    else if (t == RECORD_MEMBER_VARIABLE) {
      r = recordMemberVariable(b, 0);
    }
    else if (t == RECORD_TYPE_VARIANT) {
      r = recordTypeVariant(b, 0);
    }
    else if (t == TYPE_DEFINITION) {
      r = typeDefinition(b, 0);
    }
    else if (t == TYPE_REFERENCE) {
      r = typeReference(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return djinniFile(b, l + 1);
  }

  /* ********************************************************** */
  // 'bool' | 'i8' | 'i16' | 'i32' | 'i64' | 'f32' | 'f64' | 'string' | 'binary' | 'date'
  public static boolean basicType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<basic type>");
    r = consumeToken(b, "bool");
    if (!r) r = consumeToken(b, "i8");
    if (!r) r = consumeToken(b, "i16");
    if (!r) r = consumeToken(b, "i32");
    if (!r) r = consumeToken(b, "i64");
    if (!r) r = consumeToken(b, "f32");
    if (!r) r = consumeToken(b, "f64");
    if (!r) r = consumeToken(b, "string");
    if (!r) r = consumeToken(b, "binary");
    if (!r) r = consumeToken(b, "date");
    exit_section_(b, l, m, BASIC_TYPE, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'const' constNamedValue COLON typeReference EQ constValue SEMICOLON
  public static boolean constMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constMember")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<const member>");
    r = consumeToken(b, "const");
    r = r && constNamedValue(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && typeReference(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && constValue(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, CONST_MEMBER, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean constNamedValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constNamedValue")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, CONST_NAMED_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BLOCK_BRACE constRecordMemberList RIGHT_BLOCK_BRACE
  static boolean constRecord(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constRecord")) return false;
    if (!nextTokenIs(b, LEFT_BLOCK_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BLOCK_BRACE);
    r = r && constRecordMemberList(b, l + 1);
    r = r && consumeToken(b, RIGHT_BLOCK_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier EQ constValue
  public static boolean constRecordMemberElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constRecordMemberElement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, EQ);
    r = r && constValue(b, l + 1);
    exit_section_(b, m, CONST_RECORD_MEMBER_ELEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // (constRecordMemberElement ',' constRecordMemberList) | (constRecordMemberElement [','])
  static boolean constRecordMemberList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constRecordMemberList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constRecordMemberList_0(b, l + 1);
    if (!r) r = constRecordMemberList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // constRecordMemberElement ',' constRecordMemberList
  private static boolean constRecordMemberList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constRecordMemberList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constRecordMemberElement(b, l + 1);
    r = r && consumeToken(b, LIST_SEPARATOR);
    r = r && constRecordMemberList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // constRecordMemberElement [',']
  private static boolean constRecordMemberList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constRecordMemberList_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constRecordMemberElement(b, l + 1);
    r = r && constRecordMemberList_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [',']
  private static boolean constRecordMemberList_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constRecordMemberList_1_1")) return false;
    consumeToken(b, LIST_SEPARATOR);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean constReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constReference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, CONST_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // string_literal | number_literal | constReference | constRecord
  public static boolean constValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<const value>");
    r = consumeToken(b, STRING_LITERAL);
    if (!r) r = consumeToken(b, NUMBER_LITERAL);
    if (!r) r = constReference(b, l + 1);
    if (!r) r = constRecord(b, l + 1);
    exit_section_(b, l, m, CONST_VALUE, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'eq' | 'ord'
  public static boolean derivingParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "derivingParam")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<deriving param>");
    r = consumeToken(b, "eq");
    if (!r) r = consumeToken(b, "ord");
    exit_section_(b, l, m, DERIVING_PARAM, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (derivingParam ',' derivingParamList) | derivingParam
  public static boolean derivingParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "derivingParamList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<deriving param list>");
    r = derivingParamList_0(b, l + 1);
    if (!r) r = derivingParam(b, l + 1);
    exit_section_(b, l, m, DERIVING_PARAM_LIST, r, false, null);
    return r;
  }

  // derivingParam ',' derivingParamList
  private static boolean derivingParamList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "derivingParamList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = derivingParam(b, l + 1);
    r = r && consumeToken(b, LIST_SEPARATOR);
    r = r && derivingParamList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // item_*
  static boolean djinniFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "djinniFile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "djinniFile", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // enumTypeVariant LEFT_BLOCK_BRACE enumMember* RIGHT_BLOCK_BRACE
  static boolean enumDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDescription")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumTypeVariant(b, l + 1);
    r = r && consumeToken(b, LEFT_BLOCK_BRACE);
    r = r && enumDescription_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_BLOCK_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // enumMember*
  private static boolean enumDescription_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDescription_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!enumMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDescription_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // enumValue SEMICOLON
  public static boolean enumMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumMember")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumValue(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, ENUM_MEMBER, r);
    return r;
  }

  /* ********************************************************** */
  // 'enum'
  public static boolean enumTypeVariant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumTypeVariant")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<enum type variant>");
    r = consumeToken(b, "enum");
    exit_section_(b, l, m, ENUM_TYPE_VARIANT, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean enumValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumValue")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ENUM_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // AT'extern' string_literal
  public static boolean externStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "externStatement")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && consumeToken(b, "extern");
    r = r && consumeToken(b, STRING_LITERAL);
    exit_section_(b, m, EXTERN_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // (PLUS 'c') | (PLUS 'j') | (PLUS 'o')
  public static boolean generator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "generator")) return false;
    if (!nextTokenIs(b, PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = generator_0(b, l + 1);
    if (!r) r = generator_1(b, l + 1);
    if (!r) r = generator_2(b, l + 1);
    exit_section_(b, m, GENERATOR, r);
    return r;
  }

  // PLUS 'c'
  private static boolean generator_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "generator_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    r = r && consumeToken(b, "c");
    exit_section_(b, m, null, r);
    return r;
  }

  // PLUS 'j'
  private static boolean generator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "generator_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    r = r && consumeToken(b, "j");
    exit_section_(b, m, null, r);
    return r;
  }

  // PLUS 'o'
  private static boolean generator_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "generator_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    r = r && consumeToken(b, "o");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // genericBasicTypeSingleParameter | genericBasicTypeDualParameter
  public static boolean genericBasicType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericBasicType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<generic basic type>");
    r = genericBasicTypeSingleParameter(b, l + 1);
    if (!r) r = genericBasicTypeDualParameter(b, l + 1);
    exit_section_(b, l, m, GENERIC_BASIC_TYPE, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // map LEFT_GENERICS_BRACE typeReference LIST_SEPARATOR typeReference RIGHT_GENERICS_BRACE
  public static boolean genericBasicTypeDualParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericBasicTypeDualParameter")) return false;
    if (!nextTokenIs(b, MAP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MAP, LEFT_GENERICS_BRACE);
    r = r && typeReference(b, l + 1);
    r = r && consumeToken(b, LIST_SEPARATOR);
    r = r && typeReference(b, l + 1);
    r = r && consumeToken(b, RIGHT_GENERICS_BRACE);
    exit_section_(b, m, GENERIC_BASIC_TYPE_DUAL_PARAMETER, r);
    return r;
  }

  /* ********************************************************** */
  // (list | set | optional) !space LEFT_GENERICS_BRACE typeReference RIGHT_GENERICS_BRACE
  public static boolean genericBasicTypeSingleParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericBasicTypeSingleParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<generic basic type single parameter>");
    r = genericBasicTypeSingleParameter_0(b, l + 1);
    r = r && genericBasicTypeSingleParameter_1(b, l + 1);
    r = r && consumeToken(b, LEFT_GENERICS_BRACE);
    r = r && typeReference(b, l + 1);
    r = r && consumeToken(b, RIGHT_GENERICS_BRACE);
    exit_section_(b, l, m, GENERIC_BASIC_TYPE_SINGLE_PARAMETER, r, false, null);
    return r;
  }

  // list | set | optional
  private static boolean genericBasicTypeSingleParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericBasicTypeSingleParameter_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LIST);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, OPTIONAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // !space
  private static boolean genericBasicTypeSingleParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericBasicTypeSingleParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !consumeToken(b, SPACE);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AT'import' string_literal
  public static boolean importStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && consumeToken(b, "import");
    r = r && consumeToken(b, STRING_LITERAL);
    exit_section_(b, m, IMPORT_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // interfaceTypeVariant LEFT_BLOCK_BRACE interfaceMember* RIGHT_BLOCK_BRACE
  static boolean interfaceDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceDescription")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = interfaceTypeVariant(b, l + 1);
    r = r && consumeToken(b, LEFT_BLOCK_BRACE);
    r = r && interfaceDescription_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_BLOCK_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // interfaceMember*
  private static boolean interfaceDescription_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceDescription_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!interfaceMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "interfaceDescription_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // identifier COLON typeReference
  public static boolean interfaceFunctionParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceFunctionParam")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && typeReference(b, l + 1);
    exit_section_(b, m, INTERFACE_FUNCTION_PARAM, r);
    return r;
  }

  /* ********************************************************** */
  // (interfaceFunctionParam ',' interfaceFunctionParamList) | interfaceFunctionParam
  public static boolean interfaceFunctionParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceFunctionParamList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = interfaceFunctionParamList_0(b, l + 1);
    if (!r) r = interfaceFunctionParam(b, l + 1);
    exit_section_(b, m, INTERFACE_FUNCTION_PARAM_LIST, r);
    return r;
  }

  // interfaceFunctionParam ',' interfaceFunctionParamList
  private static boolean interfaceFunctionParamList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceFunctionParamList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = interfaceFunctionParam(b, l + 1);
    r = r && consumeToken(b, LIST_SEPARATOR);
    r = r && interfaceFunctionParamList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // constMember | interfaceMemberFunction
  public static boolean interfaceMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceMember")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<interface member>");
    r = constMember(b, l + 1);
    if (!r) r = interfaceMemberFunction(b, l + 1);
    exit_section_(b, l, m, INTERFACE_MEMBER, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ['static'] identifier LEFT_PARAM_BRACE interfaceFunctionParamList? RIGHT_PARAM_BRACE [COLON typeReference] SEMICOLON
  public static boolean interfaceMemberFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceMemberFunction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<interface member function>");
    r = interfaceMemberFunction_0(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, LEFT_PARAM_BRACE);
    r = r && interfaceMemberFunction_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARAM_BRACE);
    r = r && interfaceMemberFunction_5(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, INTERFACE_MEMBER_FUNCTION, r, false, null);
    return r;
  }

  // ['static']
  private static boolean interfaceMemberFunction_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceMemberFunction_0")) return false;
    consumeToken(b, "static");
    return true;
  }

  // interfaceFunctionParamList?
  private static boolean interfaceMemberFunction_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceMemberFunction_3")) return false;
    interfaceFunctionParamList(b, l + 1);
    return true;
  }

  // [COLON typeReference]
  private static boolean interfaceMemberFunction_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceMemberFunction_5")) return false;
    interfaceMemberFunction_5_0(b, l + 1);
    return true;
  }

  // COLON typeReference
  private static boolean interfaceMemberFunction_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceMemberFunction_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && typeReference(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'interface' generator*
  public static boolean interfaceTypeVariant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceTypeVariant")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<interface type variant>");
    r = consumeToken(b, "interface");
    r = r && interfaceTypeVariant_1(b, l + 1);
    exit_section_(b, l, m, INTERFACE_TYPE_VARIANT, r, false, null);
    return r;
  }

  // generator*
  private static boolean interfaceTypeVariant_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceTypeVariant_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!generator(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "interfaceTypeVariant_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // importStatement | externStatement | typeDefinition
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    if (!nextTokenIs(b, "", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = importStatement(b, l + 1);
    if (!r) r = externStatement(b, l + 1);
    if (!r) r = typeDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // basicType | genericBasicType
  public static boolean predefinedType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "predefinedType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<predefined type>");
    r = basicType(b, l + 1);
    if (!r) r = genericBasicType(b, l + 1);
    exit_section_(b, l, m, PREDEFINED_TYPE, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // recordTypeVariant LEFT_BLOCK_BRACE recordMember* RIGHT_BLOCK_BRACE ['deriving' LEFT_PARAM_BRACE derivingParamList RIGHT_PARAM_BRACE]
  static boolean recordDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordDescription")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordTypeVariant(b, l + 1);
    r = r && consumeToken(b, LEFT_BLOCK_BRACE);
    r = r && recordDescription_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_BLOCK_BRACE);
    r = r && recordDescription_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // recordMember*
  private static boolean recordDescription_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordDescription_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!recordMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordDescription_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ['deriving' LEFT_PARAM_BRACE derivingParamList RIGHT_PARAM_BRACE]
  private static boolean recordDescription_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordDescription_4")) return false;
    recordDescription_4_0(b, l + 1);
    return true;
  }

  // 'deriving' LEFT_PARAM_BRACE derivingParamList RIGHT_PARAM_BRACE
  private static boolean recordDescription_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordDescription_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "deriving");
    r = r && consumeToken(b, LEFT_PARAM_BRACE);
    r = r && derivingParamList(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARAM_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // constMember | recordMemberVariable
  public static boolean recordMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordMember")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<record member>");
    r = constMember(b, l + 1);
    if (!r) r = recordMemberVariable(b, l + 1);
    exit_section_(b, l, m, RECORD_MEMBER, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier COLON typeReference SEMICOLON
  public static boolean recordMemberVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordMemberVariable")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && typeReference(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, RECORD_MEMBER_VARIABLE, r);
    return r;
  }

  /* ********************************************************** */
  // 'record' generator*
  public static boolean recordTypeVariant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeVariant")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<record type variant>");
    r = consumeToken(b, "record");
    r = r && recordTypeVariant_1(b, l + 1);
    exit_section_(b, l, m, RECORD_TYPE_VARIANT, r, false, null);
    return r;
  }

  // generator*
  private static boolean recordTypeVariant_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeVariant_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!generator(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordTypeVariant_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // identifier EQ typeDescription
  public static boolean typeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDefinition")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, EQ);
    r = r && typeDescription(b, l + 1);
    exit_section_(b, m, TYPE_DEFINITION, r);
    return r;
  }

  /* ********************************************************** */
  // enumDescription | recordDescription | interfaceDescription
  static boolean typeDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDescription")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumDescription(b, l + 1);
    if (!r) r = recordDescription(b, l + 1);
    if (!r) r = interfaceDescription(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // predefinedType | identifier
  public static boolean typeReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type reference>");
    r = predefinedType(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, TYPE_REFERENCE, r, false, null);
    return r;
  }

}
