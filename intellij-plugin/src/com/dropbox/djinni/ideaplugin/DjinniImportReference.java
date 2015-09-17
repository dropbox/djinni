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

import com.dropbox.djinni.ideaplugin.psi.DjinniFile;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaetzold on 8/3/15.
 */
public class DjinniImportReference  extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  private String relativePath;

  public DjinniImportReference(@NotNull PsiElement element, @NotNull TextRange rangeInElement) {
    super(element, rangeInElement);
    relativePath = element.getText().substring(rangeInElement.getStartOffset(), rangeInElement.getEndOffset());
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final PsiFile targetFile = DjinniUtil.djinniFileRelativeResolve(myElement.getProject(), myElement.getContainingFile(), relativePath);
    if (targetFile != null) {
      return new ResolveResult[] {new PsiElementResolveResult(targetFile)};
    } else {
      return ResolveResult.EMPTY_ARRAY;
    }
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    Project project = myElement.getProject();
    List<DjinniFile> djinniFiles = DjinniUtil.findAllDjinniFiles(project, null);
    List<LookupElement> variants = new ArrayList<LookupElement>();

    for (DjinniFile djinniFile : djinniFiles) {
      if(djinniFile.getName().length() > 0) {
        variants.add(LookupElementBuilder.create(djinniFile)
                       .withIcon(DjinniIcons.FILE)
                       .withTypeText(djinniFile.getName()));
      }
    }

    return variants.toArray();
  }
}
