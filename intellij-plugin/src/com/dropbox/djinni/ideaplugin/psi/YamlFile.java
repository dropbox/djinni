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
package com.dropbox.djinni.ideaplugin.psi;

import com.dropbox.djinni.ideaplugin.YamlFileType;
import com.dropbox.djinni.ideaplugin.YamlLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by jaetzold on 7/22/15.
 */
public class YamlFile extends PsiFileBase {
  private boolean overrideValid;

  public YamlFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, YamlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return YamlFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Djinni Yaml File";
  }

  @Override
  public Icon getIcon(int flags) {
    return super.getIcon(flags);
  }

  @Override
  public boolean isValid() {
    return overrideValid || super.isValid();
  }

  // Since we're not supporting complete yaml, but only referring to a psiElement with an external name we can't register ourselves completely for .yaml support
  // This seems to leave our YamlFile in an invalid state. So allow override this here (Which is used for references).
  // Don't know in what cases this will make things go wrong though...
  public void setOverrideValid() {
    overrideValid = true;
  }
}
