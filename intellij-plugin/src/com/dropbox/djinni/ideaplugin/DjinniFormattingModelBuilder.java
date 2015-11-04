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
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jaetzold on 7/31/15.
 */
public class DjinniFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
  @Override
  public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
    return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(),
                                                                   new DjinniBlock(element.getNode(), Wrap.createWrap(WrapType.NONE, false),
                                                                                   Alignment.createAlignment(),
                                                                                   createSpaceBuilder(settings),
                                                                                   Indent.getNoneIndent(), settings.OTHER_INDENT_OPTIONS.INDENT_SIZE),
                                                                   settings);
  }

  private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
    return new SpacingBuilder(settings, DjinniLanguage.INSTANCE)
      .around(DjinniTypes.EQ).spaceIf(settings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
      .before(DjinniTypes.LEFT_BLOCK_BRACE).spaceIf(settings.SPACE_BEFORE_CLASS_LBRACE)
      .after(DjinniTypes.LEFT_BLOCK_BRACE).lineBreakInCode()
      // TODO: How to define a space after the initial '#' in a comment? Do we need to add '#' as a separate token?
      .after(DjinniTypes.TYPE_DEFINITION).blankLines(settings.BLANK_LINES_AROUND_CLASS)
      .before(DjinniTypes.PLUS).spaceIf(true);
  }

  @Nullable
  @Override
  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }
}
