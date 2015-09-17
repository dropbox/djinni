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
import com.dropbox.djinni.ideaplugin.psi.DjinniFile;
import com.dropbox.djinni.ideaplugin.psi.DjinniImportStatement;
import com.dropbox.djinni.ideaplugin.psi.DjinniTypeReference;
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jaetzold on 7/27/15.
 */
public class DjinniAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if(element instanceof DjinniTypeReference) {
      DjinniTypeReference typeReference = (DjinniTypeReference)element;
      boolean validReference = typeReference.getPredefinedType() != null
                               || !DjinniUtil.findTypeDefinitionsForName(element.getProject(), typeReference.getText(), element).isEmpty()
                               || !DjinniUtil.findExternalTypeForName(element.getProject(), typeReference.getText(), element).isEmpty();
      if(!validReference) {
        Annotation annotation = holder.createErrorAnnotation(element, "Unresolved type");
        annotation.setNeedsUpdateOnTyping(true);
        annotation.registerFix(new DjinniCreateTypeDefinitionQuickFix(typeReference.getText(), DjinniPsiImplUtil.DjinniType.RECORD));
        annotation.registerFix(new DjinniCreateTypeDefinitionQuickFix(typeReference.getText(), DjinniPsiImplUtil.DjinniType.INTERFACE));
        annotation.registerFix(new DjinniCreateTypeDefinitionQuickFix(typeReference.getText(), DjinniPsiImplUtil.DjinniType.ENUM));
      }

    } else if(element instanceof DjinniConstReference) {
      DjinniConstReference constReference = (DjinniConstReference)element;
      String typeName = DjinniUtil.getTypeNameOfConstReference(constReference);
      boolean validReference = !DjinniUtil.findReferencableValuesWithNameAndTypename(element.getProject(), constReference.getName(), typeName, element).isEmpty();
      if(!validReference) {
        Annotation annotation = holder.createErrorAnnotation(element, "Unresolved value");
        annotation.setNeedsUpdateOnTyping(true);
      }

    } else if(element instanceof DjinniImportStatement) {
      DjinniImportStatement importStatement = (DjinniImportStatement)element;
      DjinniFile importedFile =
        DjinniUtil.djinniFileRelativeResolve(element.getProject(), element.getContainingFile(), importStatement.getPath());
      if(importedFile == null) {
        TextRange textRange = importStatement.getTextRange();
        TextRange rangeOfPathInImport = importStatement.getRangeOfPath();
        TextRange range = new TextRange(textRange.getStartOffset() + rangeOfPathInImport.getStartOffset(),
                                        textRange.getStartOffset() + rangeOfPathInImport.getEndOffset());
        holder.createErrorAnnotation(range, "File does not exist");
      }
    }
  }
}
