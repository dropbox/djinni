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

import com.dropbox.djinni.ideaplugin.psi.*;
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jaetzold on 7/27/15.
 */
public class DjinniUtil {

  @NotNull
  public static List<DjinniTypeDefinition> findTypeDefinitionsForName(Project project, String name, PsiElement visibilityContext) {
    List<DjinniTypeDefinition> typeDefinitions = findAllTypeDefinitions(project, visibilityContext);
    List<DjinniTypeDefinition> result = new ArrayList<DjinniTypeDefinition>();
    for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
      if (name.equals(typeDefinition.getTypeName())) {
        result.add(typeDefinition);
      }
    }

    return result;
  }

  @NotNull
  public static List<PsiElement> findExternalTypeForName(Project project, String name, PsiElement element) {
    List<PsiElement> result = new ArrayList<PsiElement>();
    List<PsiFile> allExternalFiles = findAllExternalFiles(project, element);

    for (PsiFile externalFile : allExternalFiles) {
      InternalSingleRootFileViewProvider viewProvider = new InternalSingleRootFileViewProvider(element.getManager(), externalFile.getVirtualFile());
      PsiFile psi = viewProvider.getPsi(YamlLanguage.INSTANCE);
      if(psi instanceof YamlFile) {
        YamlFile yamlFile = (YamlFile)psi;
        yamlFile.setOverrideValid();
        for (PsiElement yamlElement : yamlFile.getChildren()) {
          if(yamlElement instanceof YamlEntry) {
            YamlEntry yamlEntry = (YamlEntry)yamlElement;
            PsiElement key = yamlEntry.getLhs();
            PsiElement value = yamlEntry.getRhs();
            if(key != null && key.getText().equals("name") && value != null && value.getText().equals(name)) {
              result.add(value);
            }
          }
        }
      }
    }

    return result;
  }

  @NotNull
  public static List<DjinniNamedElement> findReferencableValuesWithNameAndTypename(Project project, String name, String typeName, PsiElement visibilityContext) {
    List<DjinniNamedElement> result = new ArrayList<DjinniNamedElement>();
    List<DjinniNamedElement> allReferencableValues = findReferencableValuesWithTypename(project, typeName, visibilityContext);

    for (DjinniNamedElement referencableValue : allReferencableValues) {
      String valueName = referencableValue.getName();
      if(valueName != null && valueName.equals(name)) {
        result.add(referencableValue);
      }
    }

    return result;
  }

  @NotNull
  public static List<DjinniConstNamedValue> findConstNamedValuesForName(Project project, String name, PsiElement visibilityContext) {
    List<DjinniConstNamedValue> result = new ArrayList<DjinniConstNamedValue>();
    List<DjinniConstNamedValue> allConstNamedValues = findAllConstNamedValues(project, visibilityContext);

    for (DjinniConstNamedValue constNamedValue : allConstNamedValues) {
      String constNamedValueName = constNamedValue.getName();
      if(constNamedValueName != null && constNamedValueName.equals(name)) {
        result.add(constNamedValue);
      }
    }

    return result;
  }

  public static List<DjinniTypeDefinition> findAllTypeDefinitions(Project project, PsiElement visibilityContext) {
    List<DjinniTypeDefinition> result = new ArrayList<DjinniTypeDefinition>();

    List<DjinniFile> djinniFiles = findAllDjinniFiles(project, visibilityContext);
    for (DjinniFile djinniFile : djinniFiles) {
      DjinniTypeDefinition[] typeDefinitions = PsiTreeUtil.getChildrenOfType(djinniFile, DjinniTypeDefinition.class);
      if (typeDefinitions != null) {
        result.addAll(Arrays.asList(typeDefinitions));
      }
    }

    return result;
  }

  @NotNull
  public static List<PsiElement> findAllExternalTypes(Project project, PsiElement visibilityContext) {
    List<PsiElement> result = new ArrayList<PsiElement>();

    List<PsiFile> allExternalFiles = findAllExternalFiles(project, visibilityContext);
    for (PsiFile externalFile : allExternalFiles) {
      InternalSingleRootFileViewProvider viewProvider = new InternalSingleRootFileViewProvider(visibilityContext.getManager(), externalFile.getVirtualFile());
      PsiFile psi = viewProvider.getPsi(YamlLanguage.INSTANCE);
      if(psi instanceof YamlFile) {
        YamlFile yamlFile = (YamlFile)psi;
        yamlFile.setOverrideValid();
        for (PsiElement yamlElement : yamlFile.getChildren()) {
          if(yamlElement instanceof YamlEntry) {
            YamlEntry yamlEntry = (YamlEntry)yamlElement;
            PsiElement key = yamlEntry.getLhs();
            PsiElement value = yamlEntry.getRhs();
            if(key != null && key.getText().equals("name") && value != null) {
              result.add(value);
            }
          }
        }
      }
    }

    return result;
  }

  private static List<String> builtinTypeNames = new ArrayList<String>(Arrays.asList("bool" , "i8" , "i16" , "i32" , "i64" , "f32" , "f64" , "string" , "binary" , "date", "set", "map", "list", "optional"));
  @NotNull
  public static List<String> getBuiltinTypeNames(Project project, PsiElement visibilityContext) {
    return builtinTypeNames;
  }

  @NotNull
  public static List<DjinniEnumValue> findAllEnumValues(Project project, PsiElement visibilityContext) {
    List<DjinniEnumValue> result = new ArrayList<DjinniEnumValue>();

    List<DjinniFile> djinniFiles = findAllDjinniFiles(project, visibilityContext);
    for (DjinniFile djinniFile : djinniFiles) {
      Collection<DjinniEnumValue> enumValues = getDeepChildrenOfType(djinniFile, DjinniEnumValue.class);
      result.addAll(enumValues);
    }

    return result;
  }

  @NotNull
  public static List<DjinniNamedElement> findReferencableValuesWithTypename(Project project, String typeName, PsiElement visibilityContext) {
    List<DjinniNamedElement> result = new ArrayList<DjinniNamedElement>();

    for (DjinniTypeDefinition typeDefinition : findAllTypeDefinitions(project, visibilityContext)) {
      if(typeDefinition.getDjinniType() == DjinniPsiImplUtil.DjinniType.ENUM) {
        if(typeDefinition.getTypeName().equals(typeName)) {
          Collection<DjinniEnumValue> enumValues = getDeepChildrenOfType(typeDefinition, DjinniEnumValue.class);
          result.addAll(enumValues);
        }
      }
    }

    for (DjinniConstMember constMember : findAllConstMembers(project, visibilityContext)) {
      String memberTypeName = constMember.getTypeReference().getName();
      if(memberTypeName != null && memberTypeName.equals(typeName)) {
        result.add(constMember.getConstNamedValue());
      }
    }

    return result;
  }

  @Nullable
  public static String getTypeNameOfConstReference(DjinniConstReference constReference) {
    return getTypeNameOfConstValue((DjinniConstValue)constReference.getParent());
  }

  @Nullable
  public static String getTypeNameOfConstValue(DjinniConstValue constValue) {
    PsiElement parent = constValue.getParent();
    if(parent instanceof DjinniConstMember) {
      DjinniConstMember constMember = (DjinniConstMember)parent;
      return constMember.getTypeReference().getName();
    } else if(parent instanceof DjinniConstRecordMemberElement) {
      DjinniConstRecordMemberElement recordMemberElement = (DjinniConstRecordMemberElement)parent;
      String recordTypeName = getTypeNameOfConstValue((DjinniConstValue)recordMemberElement.getParent());
      final List<DjinniTypeDefinition> typeDefinitions = findTypeDefinitionsForName(constValue.getProject(), recordTypeName, constValue);

      for (DjinniTypeDefinition typeDefinition : typeDefinitions) {
        if(typeDefinition.getDjinniType() == DjinniPsiImplUtil.DjinniType.RECORD) {
          String recordMemberName = recordMemberElement.getIdentifier().getText();
          for (DjinniRecordMember recordMember : typeDefinition.getRecordMemberList()) {
            DjinniConstMember constMember = recordMember.getConstMember();
            DjinniRecordMemberVariable variableMember = recordMember.getRecordMemberVariable();
            if(constMember != null) {
              String memberName = constMember.getConstNamedValue().getName();
              if(memberName != null && memberName.equals(recordMemberName)) {
                return constMember.getTypeReference().getName();
              }
            } else if(variableMember != null) {
              String memberName = variableMember.getIdentifier().getText();
              if(memberName.equals(recordMemberName)) {
                return variableMember.getTypeReference().getName();
              }
            }
          }
        }
      }
    }

    return null;
  }

  @NotNull
  public static List<DjinniConstMember> findAllConstMembers(Project project, PsiElement visibilityContext) {
    List<DjinniConstMember> result = new ArrayList<DjinniConstMember>();

    // If visibilityContext is null there are no const members in scope. I don't see a usecase of collecting all of them in the whole project
    if(visibilityContext != null) {
      // Only look for stuff in scope. This means it's part of the current enclosing type. There are no global constants besides enums
      while (visibilityContext != null && !(visibilityContext instanceof DjinniTypeDefinition)) {
        visibilityContext = visibilityContext.getContext();
      }
      if(visibilityContext != null) {
        // This collects all the named constant values in the record/interface.
        Collection<DjinniConstMember> constMembers = getDeepChildrenOfType(visibilityContext, DjinniConstMember.class);
        result.addAll(constMembers);
      }
    }

    return result;
  }

  @NotNull
  public static List<DjinniConstNamedValue> findAllConstNamedValues(Project project, PsiElement visibilityContext) {
    List<DjinniConstNamedValue> result = new ArrayList<DjinniConstNamedValue>();

    // If visibilityContext is null there are no const named values in scope. I don't see a usecase of collecting all of them in the whole project
    if(visibilityContext != null) {
      // Only look for stuff in scope. This means it's part of the current enclosing type. There are no global constants besides enums
      while (visibilityContext != null && !(visibilityContext instanceof DjinniTypeDefinition)) {
        visibilityContext = visibilityContext.getContext();
      }
      if(visibilityContext != null) {
        // This collects all the named constant values in the record/interface.
        Collection<DjinniConstNamedValue> constNamedValues = getDeepChildrenOfType(visibilityContext, DjinniConstNamedValue.class);
        result.addAll(constNamedValues);
      }
    }

    return result;
  }

  @NotNull
  public static List<DjinniFile> findAllDjinniFiles(Project project, PsiElement visibilityContext) {
    List<DjinniFile> result = new ArrayList<DjinniFile>();

    if (visibilityContext == null) {
      Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, DjinniFileType.INSTANCE, GlobalSearchScope.allScope(project));
      for (VirtualFile virtualFile : virtualFiles) {
        DjinniFile djinniFile = (DjinniFile)PsiManager.getInstance(project).findFile(virtualFile);
        if (djinniFile != null) {
          result.add(djinniFile);
        }
      }
    } else {
      while (visibilityContext != null && !(visibilityContext instanceof PsiFile)) {
        visibilityContext = visibilityContext.getContext();
      }

      if(visibilityContext instanceof DjinniFile) {
        List<DjinniFile> filesToCheckForImports = new ArrayList<DjinniFile>();
        filesToCheckForImports.add((DjinniFile)visibilityContext);

        while(!filesToCheckForImports.isEmpty()) {
          DjinniFile currentFile = filesToCheckForImports.remove(0);
          if(!result.contains(currentFile)) {
            result.add(currentFile);
            for (PsiElement element : currentFile.getChildren()) {
              if(element instanceof DjinniImportStatement) {
                DjinniImportStatement importStatement = (DjinniImportStatement)element;
                DjinniFile importedFile = djinniFileRelativeResolve(project, currentFile, importStatement.getPath());
                if (importedFile != null) {
                  filesToCheckForImports.add(importedFile);
                }
              }
            }
          }
        }
      }
    }

    return result;
  }

  @NotNull
  public static List<PsiFile> findAllExternalFiles(Project project, @NotNull PsiElement visibilityContext) {
    List<PsiFile> result = new ArrayList<PsiFile>();

    List<DjinniFile> visibleDjinniFiles = findAllDjinniFiles(project, visibilityContext);
    for (DjinniFile djinniFile : visibleDjinniFiles) {
      for (PsiElement element : djinniFile.getChildren()) {
        if(element instanceof DjinniExternStatement) {
          DjinniExternStatement externStatement = (DjinniExternStatement)element;
          PsiFile externalFile = fileRelativeResolve(project, djinniFile, externStatement.getPath());
          if (externalFile!= null && !result.contains(externalFile)) {
            result.add(externalFile);
          }
        }
      }
    }

    return result;
  }

  @Nullable
  public static DjinniFile djinniFileRelativeResolve(Project project, PsiFile currentFile, String relativePath) {
    PsiFile resolvedFile = fileRelativeResolve(project, currentFile, relativePath);
    if (resolvedFile instanceof DjinniFile) {
      return (DjinniFile)resolvedFile;
    }

    return null;
  }

  @Nullable
  public static PsiFile fileRelativeResolve(Project project, PsiFile currentFile, String relativePath) {
    PsiDirectory currentDirectory = currentFile.getOriginalFile().getContainingDirectory();
    VirtualFile fileByRelativePath = currentDirectory.getVirtualFile().findFileByRelativePath(relativePath);
    if (fileByRelativePath != null) {
      return PsiManager.getInstance(project).findFile(fileByRelativePath);
    }

    return null;
  }

  /** It's like {@link PsiTreeUtil#getChildrenOfType(PsiElement, Class)}, but recursive for children of children as well. */
  @NotNull
  private static <T extends PsiElement> Collection<T> getDeepChildrenOfType(@Nullable PsiElement element, @NotNull Class<T> aClass) {
    List<T> result = new ArrayList<T>();
    if (element != null) {
      getDeepChildrenOfType(element, aClass, result);
    }
    return result;
  }

  private static <T extends PsiElement> void getDeepChildrenOfType(@NotNull PsiElement element, @NotNull Class<T> aClass, List<T> result) {
    for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (aClass.isInstance(child)) {
        result.add(aClass.cast(child));
      }
    }
    for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      getDeepChildrenOfType(child, aClass, result);
    }
  }

  private static class InternalSingleRootFileViewProvider extends SingleRootFileViewProvider {
    public InternalSingleRootFileViewProvider(PsiManager manager, VirtualFile virtualFile) {
      super(manager, virtualFile, true, YamlLanguage.INSTANCE);
    }
  };
}
