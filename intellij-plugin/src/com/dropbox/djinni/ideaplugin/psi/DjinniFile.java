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

import com.dropbox.djinni.ideaplugin.DjinniFileType;
import com.dropbox.djinni.ideaplugin.DjinniLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by jaetzold on 7/22/15.
 */
public class DjinniFile extends PsiFileBase {
  public DjinniFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, DjinniLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return DjinniFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Djinni File";
  }

  @Override
  public Icon getIcon(int flags) {
    return super.getIcon(flags);
  }
}
