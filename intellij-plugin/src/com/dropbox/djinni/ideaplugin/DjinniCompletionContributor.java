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

import com.dropbox.djinni.ideaplugin.psi.DjinniTypeReference;
import com.dropbox.djinni.ideaplugin.psi.DjinniTypes;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by jaetzold on 7/27/15.
 */
public class DjinniCompletionContributor extends CompletionContributor {
  public DjinniCompletionContributor() {
    // TODO: make completion work for more complex elements, for references it already works automatically through the reference relationship
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(DjinniTypes.IDENTIFIER).withLanguage(DjinniLanguage.INSTANCE),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               PsiElement element = parameters.getPosition().getParent();
               if(element instanceof DjinniTypeReference) {
                 List<String> typeNames = DjinniUtil.getBuiltinTypeNames(parameters.getEditor().getProject(), element);
                 for (String typeName : typeNames) {
                   result.addElement(LookupElementBuilder.create(typeName));
                 }
               }
             }
           }
    );
  }
}
