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

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by jaetzold on 8/11/15.
 */
public class YamlFileType extends LanguageFileType {
  public static final YamlFileType INSTANCE = new YamlFileType();

  protected YamlFileType() {
    super(YamlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Djinni Yaml file";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Djinni Yaml external type definition file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "yaml";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return DjinniIcons.FILE;
  }
}
