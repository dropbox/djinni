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

import com.dropbox.djinni.ideaplugin.psi.*;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Created by jaetzold on 7/29/15.
 */
public class DjinniFindUsagesProvider implements FindUsagesProvider {
  private static final DefaultWordsScanner WORDS_SCANNER =
    new DefaultWordsScanner(new DjinniLexerAdapter(),
                            TokenSet.create(DjinniTypes.IDENTIFIER), TokenSet.create(DjinniTypes.COMMENT), TokenSet.create(DjinniTypes.STRING_LITERAL, DjinniTypes.NUMBER_LITERAL));

  @Nullable
  @Override
  public WordsScanner getWordsScanner() {
    // The tutorial docs say this should return WORDS_SCANNER. But find usages works without it and with it there is sometimes
    // an error on lexing djinni files. See here for more context:
    // https://devnet.jetbrains.com/message/5537369
    return null;
  }

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof DjinniNamedElement;
  }

  @Nullable
  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @NotNull
  @Override
  public String getType(@NotNull PsiElement element) {
    if(element instanceof DjinniTypeDefinition) {
      return "djinni " +((DjinniTypeDefinition)element).getDjinniType().toString().toLowerCase(Locale.US);
    } else if(element instanceof DjinniEnumValue) {
      return "djinni enum value";
    } else if(element instanceof DjinniConstNamedValue) {
      return "djinni const value";
    } else {
      return "getType";
    }
  }

  @NotNull
  @Override
  public String getDescriptiveName(@NotNull PsiElement element) {
    if(element instanceof DjinniTypeDefinition) {
      return ((DjinniTypeDefinition)element).getTypeName();
    } else {
      return "getDescriptiveName";
    }
  }

  @NotNull
  @Override
  public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
    if(element instanceof DjinniTypeDefinition) {
      DjinniTypeDefinition typeDefinition = (DjinniTypeDefinition)element;
      return typeDefinition.getTypeName();
    } else {
      return "getNodeText";
    }
  }
}
