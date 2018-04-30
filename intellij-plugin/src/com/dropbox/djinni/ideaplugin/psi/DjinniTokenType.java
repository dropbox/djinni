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

import com.dropbox.djinni.ideaplugin.DjinniLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by jaetzold on 7/20/15.
 */
public class DjinniTokenType extends IElementType {
  public DjinniTokenType(@NotNull @NonNls String debugName) {
    super(debugName, DjinniLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "DjinniTokenType." + super.toString();
  }
}
