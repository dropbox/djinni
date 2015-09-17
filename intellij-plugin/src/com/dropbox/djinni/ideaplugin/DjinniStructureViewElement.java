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
import com.dropbox.djinni.ideaplugin.psi.DjinniTypeDefinition;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaetzold on 7/30/15.
 */
public class DjinniStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
  private PsiElement element;

  public DjinniStructureViewElement(PsiElement element) {
    this.element = element;
  }

  @Override
  public Object getValue() {
    return element;
  }

  @Override
  public void navigate(boolean requestFocus) {
    if(element instanceof NavigationItem) {
      NavigationItem navigationItem = (NavigationItem)element;
      navigationItem.navigate(requestFocus);
    }
  }

  @Override
  public boolean canNavigate() {
    return element instanceof NavigationItem && ((NavigationItem)element).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return element instanceof NavigationItem && ((NavigationItem)element).canNavigateToSource();
  }

  @NotNull
  @Override
  public String getAlphaSortKey() {
    String name = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null;
    return name != null ? name : "";
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    ItemPresentation presentation = element instanceof NavigationItem ? ((NavigationItem)element).getPresentation() : null;
    return presentation != null ? presentation : new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return element.toString();
      }

      @Nullable
      @Override
      public String getLocationString() {
        return null;
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return null;
      }
    };
  }

  @NotNull
  @Override
  public TreeElement[] getChildren() {
    if (element instanceof DjinniFile) {
      DjinniTypeDefinition[] typeDefinitions = PsiTreeUtil.getChildrenOfType(element, DjinniTypeDefinition.class);
      if (typeDefinitions == null) {
        return EMPTY_ARRAY;
      }
      List<TreeElement> treeElements = new ArrayList<TreeElement>(typeDefinitions.length);
      for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
        treeElements.add(new DjinniStructureViewElement(typeDefinition));
      }
      return treeElements.toArray(new TreeElement[treeElements.size()]);
    } else {
      return EMPTY_ARRAY;
    }
  }
}
