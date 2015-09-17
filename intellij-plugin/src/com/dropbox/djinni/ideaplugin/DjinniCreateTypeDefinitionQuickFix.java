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

import com.dropbox.djinni.ideaplugin.psi.DjinniElementFactory;
import com.dropbox.djinni.ideaplugin.psi.DjinniFile;
import com.dropbox.djinni.ideaplugin.psi.DjinniTypeDefinition;
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Created by jaetzold on 8/3/15.
 */
public class DjinniCreateTypeDefinitionQuickFix extends BaseIntentionAction {
  private String typeName;
  private DjinniPsiImplUtil.DjinniType djinniType;

  public DjinniCreateTypeDefinitionQuickFix(@NotNull String typeName, @NotNull DjinniPsiImplUtil.DjinniType djinniType) {
    this.typeName = typeName;
    this.djinniType = djinniType;
  }

  @NotNull
  @Override
  public String getText() {
    return "Create djinni " + djinniType.toString().toLowerCase(Locale.US);
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return "Djinni types";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return file instanceof DjinniFile;
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        PsiReference reference = file.findReferenceAt(offset);
        if (reference == null && offset > 0) {
          reference = file.findReferenceAt(offset - 1);
        }
        if (reference != null) {
          PsiElement topLevelContext = reference.getElement();
          do {
            topLevelContext = topLevelContext.getContext();
          } while (topLevelContext != null && !(topLevelContext instanceof DjinniTypeDefinition));

          if (topLevelContext != null) {
            // TODO: Try to make this a generic type creation that uses a live template where the record/interface/enum part is just done by using autocompletion
            DjinniTypeDefinition newTypeDefinition = DjinniElementFactory.createTypeDefinition(project, typeName, djinniType);
            file.addAfter(newTypeDefinition, topLevelContext);
          }
        }
      }
    });
  }
}
