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
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Created by jaetzold on 7/28/15.
 */
public class DjinniElementFactory {
  public static DjinniTypeDefinition createTypeDefinition(Project project, String name, DjinniPsiImplUtil.DjinniType djinniType) {
    DjinniFile djinniFile = createFile(project, name +" = " + djinniType.toString().toLowerCase(Locale.US) + " { }");
    return (DjinniTypeDefinition)djinniFile.getFirstChild();
  }

  @Nullable
  public static DjinniTypeReference createTypeReference(Project project, String name) {
    DjinniFile djinniFile = createFile(project, "dummy = record { prop: " + name + "; }");
    DjinniTypeDefinition typeDefinition = (DjinniTypeDefinition)djinniFile.getFirstChild();
    if (typeDefinition.getDjinniType() == DjinniPsiImplUtil.DjinniType.RECORD) {
      DjinniRecordMemberVariable recordMember = typeDefinition.getRecordMemberList().get(0).getRecordMemberVariable();
      if (recordMember != null) {
        return recordMember.getTypeReference();
      }
    }
    return null;
  }

  @Nullable
  public static DjinniConstReference createConstReference(Project project, String name) {
    DjinniFile djinniFile = createFile(project, "dummy = record { const prop: i32 = " + name + "; }");
    DjinniTypeDefinition typeDefinition = (DjinniTypeDefinition)djinniFile.getFirstChild();
    if (typeDefinition.getDjinniType() == DjinniPsiImplUtil.DjinniType.RECORD) {
      DjinniConstMember constMember = typeDefinition.getRecordMemberList().get(0).getConstMember();
      if (constMember != null) {
        return constMember.getConstValue().getConstReference();
      }
    }
    return null;
  }

  @Nullable
  public static DjinniEnumValue createEnumValue(Project project, String name) {
    DjinniFile djinniFile = createFile(project, "dummy = enum { " + name + "; }");
    DjinniTypeDefinition typeDefinition = (DjinniTypeDefinition)djinniFile.getFirstChild();
    if (typeDefinition.getDjinniType() == DjinniPsiImplUtil.DjinniType.ENUM) {
      return typeDefinition.getEnumMemberList().get(0).getEnumValue();
    }
    return null;
  }

  @Nullable
  public static DjinniConstNamedValue createConstNamedValue(Project project, String name) {
    DjinniFile djinniFile = createFile(project, "dummy = record { const " + name + ": i32 = 1; }");
    DjinniTypeDefinition typeDefinition = (DjinniTypeDefinition)djinniFile.getFirstChild();
    if (typeDefinition.getDjinniType() == DjinniPsiImplUtil.DjinniType.RECORD) {
      DjinniConstMember constMember = typeDefinition.getRecordMemberList().get(0).getConstMember();
      if (constMember != null) {
        return constMember.getConstNamedValue();
      }
    }
    return null;
  }

  public static DjinniFile createFile(Project project, String text) {
    String name = "dummy.Djinni";
    return (DjinniFile) PsiFileFactory.getInstance(project).createFileFromText(name, DjinniFileType.INSTANCE, text);
  }

  public static DjinniImportStatement createImportStatement(Project project, String path) {
    DjinniFile djinniFile = createFile(project, "@import \"" + path + "\"");
    return (DjinniImportStatement)djinniFile.getFirstChild();
  }
}
