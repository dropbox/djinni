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

import com.dropbox.djinni.ideaplugin.psi.DjinniTypeDefinition;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaetzold on 7/28/15.
 */
public class DjinniReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
  private String name;

  public DjinniReference(@NotNull PsiElement element, @NotNull TextRange rangeInElement) {
    super(element, rangeInElement);
    name = element.getText().substring(rangeInElement.getStartOffset(), rangeInElement.getEndOffset());
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    List<ResolveResult> result = new ArrayList<ResolveResult>();

    Project project = myElement.getProject();

    final List<DjinniTypeDefinition> typeDefinitions = DjinniUtil.findTypeDefinitionsForName(project, name, myElement);
    for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
      result.add(new PsiElementResolveResult(typeDefinition));
    }

    final List<PsiElement> externalTypes = DjinniUtil.findExternalTypeForName(project, name, myElement);
    for (PsiElement externalElement : externalTypes) {
      result.add(new PsiElementResolveResult(externalElement));
    }

    return result.toArray(new ResolveResult[result.size()]);
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

    final List<DjinniTypeDefinition> typeDefinitions = DjinniUtil.findAllTypeDefinitions(project, myElement);
    List<LookupElement> variants = new ArrayList<LookupElement>();
    for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
      if(typeDefinition.getTypeName() != null && typeDefinition.getTypeName().length() > 0) {
        variants.add(LookupElementBuilder.create(typeDefinition)
                       .withIcon(DjinniIcons.FILE)
                       .withTypeText(typeDefinition.getContainingFile().getName()));
      }
    }

    final List<PsiElement> externalTypes = DjinniUtil.findAllExternalTypes(project, myElement);
    for (PsiElement externalType : externalTypes) {
      if(externalType.getText() != null && externalType.getText().length() > 0) {
        variants.add(LookupElementBuilder.create(externalType.getText())
                       .withIcon(DjinniIcons.FILE)
                       .withTypeText(externalType.getContainingFile().getName()));
      }
    }

    return variants.toArray();
  }

  // todo: Test later whether there is a more indirect way to achieve this.
  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    if(myElement instanceof PsiNamedElement) {
      PsiNamedElement namedElement = (PsiNamedElement)myElement;
      return namedElement.setName(newElementName);
    }
    return myElement;
  }
}
