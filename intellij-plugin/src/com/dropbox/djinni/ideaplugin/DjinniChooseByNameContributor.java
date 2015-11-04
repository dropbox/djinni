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
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaetzold on 7/29/15.
 */
public class DjinniChooseByNameContributor implements ChooseByNameContributor {
  @NotNull
  @Override
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    List<String> result = new ArrayList<String>();

    List<DjinniTypeDefinition> typeDefinitions = DjinniUtil.findAllTypeDefinitions(project, null);
    for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
      result.add(typeDefinition.getTypeName());
    }

    final List<PsiElement> externalTypes = DjinniUtil.findAllExternalTypes(project, null);
    for (PsiElement externalType : externalTypes) {
      String text = externalType.getText();
      if(text != null && text.length() > 0) {
        result.add(text);
      }
    }

    return ArrayUtil.toStringArray(result);
  }

  @NotNull
  @Override
  public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
    List<PsiElement> result = new ArrayList<PsiElement>();

    result.addAll(DjinniUtil.findTypeDefinitionsForName(project, name, null));
    result.addAll(DjinniUtil.findExternalTypeForName(project, name, null));

    //noinspection SuspiciousToArrayCall
    return result.toArray(new NavigationItem[result.size()]);
  }
}
