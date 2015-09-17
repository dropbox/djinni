package com.dropbox.djinni.ideaplugin;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import static com.dropbox.djinni.ideaplugin.psi.DjinniTypes.*;

%%
/*
 * Copyright 2015 Dropbox, Inc.
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
%{
  public _DjinniLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _DjinniLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+

SPACE=[ \t\n\x0B\f\r]+
COMMENT=#.*
STRING_LITERAL=('([^'\\]|\\.)*'|\"([^\"\\]|\\\"|\\'|\\)*\")
NUMBER_LITERAL=-?([0-9]+(\.[0-9]*)?)|(\.[0-9]+)
IDENTIFIER=[:letter:][a-zA-Z_0-9]*
TEXT=[a-zA-Z_0-9]+

%%
<YYINITIAL> {
  {WHITE_SPACE}         { return com.intellij.psi.TokenType.WHITE_SPACE; }

  "="                   { return EQ; }
  ":"                   { return COLON; }
  ";"                   { return SEMICOLON; }
  ","                   { return LIST_SEPARATOR; }
  "+"                   { return PLUS; }
  "{"                   { return LEFT_BLOCK_BRACE; }
  "}"                   { return RIGHT_BLOCK_BRACE; }
  "<"                   { return LEFT_GENERICS_BRACE; }
  ">"                   { return RIGHT_GENERICS_BRACE; }
  "("                   { return LEFT_PARAM_BRACE; }
  ")"                   { return RIGHT_PARAM_BRACE; }
  "@"                   { return AT; }
  "list"                { return LIST; }
  "set"                 { return SET; }
  "optional"            { return OPTIONAL; }
  "map"                 { return MAP; }

  {SPACE}               { return SPACE; }
  {COMMENT}             { return COMMENT; }
  {STRING_LITERAL}      { return STRING_LITERAL; }
  {NUMBER_LITERAL}      { return NUMBER_LITERAL; }
  {IDENTIFIER}          { return IDENTIFIER; }
  {TEXT}                { return TEXT; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}
