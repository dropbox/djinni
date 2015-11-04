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
import com.dropbox.djinni.ideaplugin.psi.DjinniTypes;
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by jaetzold on 7/29/15.
 */
public class DjinniFoldingBuilder extends FoldingBuilderEx {

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {

    List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
    Collection<DjinniTypeDefinition> typeDefinitions = PsiTreeUtil.findChildrenOfType(root, DjinniTypeDefinition.class);

    for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
      String typeName = typeDefinition.getTypeName();
      DjinniPsiImplUtil.DjinniType djinniType = typeDefinition.getDjinniType();
      if (djinniType != null) {
        final String placeholderText = typeName +" = " +djinniType.toString().toLowerCase(Locale.US) + " {...}";
        FoldingGroup group = FoldingGroup.newGroup("Djinni");
        descriptors.add(new FoldingDescriptor(
          typeDefinition.getNode(),
          new TextRange(typeDefinition.getTextRange().getStartOffset(), typeDefinition.getTextRange().getEndOffset()),
          group) {
          @Nullable
          @Override
          public String getPlaceholderText() {
            return placeholderText;
          }
        });
      }
    }

    return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    ASTNode identifier = node.findChildByType(DjinniTypes.IDENTIFIER);
    return identifier != null ? identifier.getText() : "...";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }
}
