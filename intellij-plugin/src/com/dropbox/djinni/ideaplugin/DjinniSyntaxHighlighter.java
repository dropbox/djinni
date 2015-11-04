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
package com.dropbox.djinni.ideaplugin;

import com.dropbox.djinni.ideaplugin.psi.DjinniTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Created by jaetzold on 7/23/15.
 */
public class DjinniSyntaxHighlighter extends SyntaxHighlighterBase {

  public static final TextAttributesKey STRING_LITERAL = createTextAttributesKey("DJINNI_STRING_LITERAL", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey NUMBER_LITERAL = createTextAttributesKey("DJINNI_NUMBER_LITERAL", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("DJINNI_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey COMMENT = createTextAttributesKey("DJINNI_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("DJINNI_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

  private static final TextAttributesKey[] STRING_LITERAL_KEYS = new TextAttributesKey[]{STRING_LITERAL};
  private static final TextAttributesKey[] NUMBER_LITERAL_KEYS = new TextAttributesKey[]{NUMBER_LITERAL};
  private static final TextAttributesKey[] IDENTIFIER_KEYS = new TextAttributesKey[]{IDENTIFIER};
  private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
  private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
  private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[]{};

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new DjinniLexerAdapter();
  }

  @NotNull
  @Override
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    if (tokenType.equals(DjinniTypes.STRING_LITERAL)) {
      return STRING_LITERAL_KEYS;
    } else if (tokenType.equals(DjinniTypes.NUMBER_LITERAL)) {
      return NUMBER_LITERAL_KEYS;
    } else if (tokenType.equals(DjinniTypes.IDENTIFIER)) {
      return IDENTIFIER_KEYS;
    } else if (tokenType.equals(DjinniTypes.COMMENT)) {
      return COMMENT_KEYS;
    } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
      return BAD_CHAR_KEYS;
    } else {
      return EMPTY_KEYS;
    }
  }
}
