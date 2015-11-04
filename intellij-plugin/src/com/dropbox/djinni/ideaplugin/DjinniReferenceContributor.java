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

import com.dropbox.djinni.ideaplugin.psi.DjinniConstReference;
import com.dropbox.djinni.ideaplugin.psi.DjinniExternStatement;
import com.dropbox.djinni.ideaplugin.psi.DjinniImportStatement;
import com.dropbox.djinni.ideaplugin.psi.DjinniTypeReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jaetzold on 7/28/15.
 */
public class DjinniReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(DjinniTypeReference.class), new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        DjinniTypeReference typeReference = (DjinniTypeReference)element;
        String text = typeReference.getText();
        if(text != null && text.length() > 0) {
          return new PsiReference[] {new DjinniReference(element, new TextRange(0, text.length()))};
        }
        return new PsiReference[0];
      }

    });
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(DjinniConstReference.class), new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        DjinniConstReference constReference = (DjinniConstReference)element;
        String name = constReference.getName();
        if(name != null && name.length() > 0) {
          return new PsiReference[] {new DjinniValueReference(constReference, new TextRange(0, name.length()))};
        }
        return new PsiReference[0];
      }

    });
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(DjinniImportStatement.class), new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        DjinniImportStatement importStatement = (DjinniImportStatement)element;
        TextRange importRange = importStatement.getTextRange();
        TextRange pathRange = importStatement.getStringLiteral().getTextRange();
        if(pathRange.getLength() > 2) {
          TextRange rangeInElement = new TextRange((pathRange.getStartOffset() - importRange.getStartOffset()) + 1,
                                                   (pathRange.getEndOffset() - importRange.getStartOffset()) - 1);
          DjinniImportReference djinniImportReference = new DjinniImportReference(element, rangeInElement);
          return new PsiReference[] {djinniImportReference};
        }
        return new PsiReference[0];
      }
    });
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(DjinniExternStatement.class), new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        DjinniExternStatement externStatement = (DjinniExternStatement)element;
        TextRange importRange = externStatement.getTextRange();
        TextRange pathRange = externStatement.getStringLiteral().getTextRange();
        if(pathRange.getLength() > 2) {
          TextRange rangeInElement = new TextRange((pathRange.getStartOffset() - importRange.getStartOffset()) + 1,
                                                   (pathRange.getEndOffset() - importRange.getStartOffset()) - 1);
          DjinniExternReference djinniExternReference = new DjinniExternReference(element, rangeInElement);
          return new PsiReference[] {djinniExternReference};
        }
        return new PsiReference[0];
      }
    });
  }
}
