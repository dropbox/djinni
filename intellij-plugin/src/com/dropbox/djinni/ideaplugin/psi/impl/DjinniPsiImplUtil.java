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
package com.dropbox.djinni.ideaplugin.psi.impl;

import com.dropbox.djinni.ideaplugin.DjinniIcons;
import com.dropbox.djinni.ideaplugin.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by jaetzold on 7/23/15.
 */
public class DjinniPsiImplUtil {
  public enum DjinniType {
    ENUM,RECORD,INTERFACE
  }
  public static String getTypeName(DjinniTypeDefinition typeDefinition) {
    return typeDefinition.getIdentifier().getText();
  }

  @NotNull
  public static DjinniType getDjinniType(DjinniTypeDefinition typeDefinition) {
    if(typeDefinition.getRecordTypeVariant() != null) {
      return DjinniType.RECORD;
    } else if(typeDefinition.getInterfaceTypeVariant() != null) {
      return DjinniType.INTERFACE;
    } else {
      return DjinniType.ENUM;
    }
  }

  public static String getName(DjinniTypeDefinition typeDefinition) {
    return getTypeName(typeDefinition);
  }

  public static PsiElement setName(DjinniTypeDefinition typeDefinition, String newName) {
    ASTNode nameNode = typeDefinition.getIdentifier().getNode();
    if (nameNode != null) {

      DjinniTypeDefinition newTypeDefinition = DjinniElementFactory.createTypeDefinition(typeDefinition.getProject(), newName, typeDefinition.getDjinniType());
      ASTNode newIdentifierNode = newTypeDefinition.getIdentifier().getNode();
      typeDefinition.getNode().replaceChild(nameNode, newIdentifierNode);
    }
    return typeDefinition;
  }

  @Nullable
  public static PsiElement getNameIdentifier(DjinniTypeDefinition typeDefinition) {
    ASTNode nameNode = typeDefinition.getIdentifier().getNode();
    if (nameNode != null) {
      return nameNode.getPsi();
    } else {
      return null;
    }
  }

  public static ItemPresentation getPresentation(final DjinniTypeDefinition typeDefinition) {
    return new ItemPresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return typeDefinition.getTypeName();
      }

      @Nullable
      @Override
      public String getLocationString() {
        PsiFile containingFile = typeDefinition.getContainingFile();
        return containingFile != null ? containingFile.getName() : null;
      }

      @Nullable
      @Override
      public Icon getIcon(boolean unused) {
        return DjinniIcons.FILE;
      }
    };
  }



  public static String getName(DjinniTypeReference typeReference) {
    return typeReference.getText();
  }

  public static PsiElement setName(DjinniTypeReference typeReference, String newName) {
    PsiElement identifier = typeReference.getIdentifier();
    ASTNode node = identifier != null ? identifier.getNode() : null;
    if (node != null) {

      DjinniTypeReference newTypeReference = DjinniElementFactory.createTypeReference(typeReference.getProject(), newName);
      if (newTypeReference != null) {
        PsiElement newIdentifier = newTypeReference.getIdentifier();
        if (newIdentifier != null) {
          ASTNode newIdentifierNode = newIdentifier.getNode();
          typeReference.getNode().replaceChild(node, newIdentifierNode);
        }
      }
    }
    return typeReference;
  }

  @Nullable
  public static PsiElement getNameIdentifier(DjinniTypeReference typeReference) {
    PsiElement identifier = typeReference.getIdentifier();
    if (identifier != null) {
      ASTNode nameNode = identifier.getNode();
      if (nameNode != null) {
        return nameNode.getPsi();
      }
    }
    return null;
  }



  public static String getName(DjinniConstReference reference) {
    return reference.getText();
  }

  public static PsiElement setName(DjinniConstReference reference, String newName) {
    PsiElement identifier = reference.getIdentifier();
    ASTNode node = identifier.getNode();
    if (node != null) {
      DjinniConstReference newReference = DjinniElementFactory.createConstReference(reference.getProject(), newName);
      if (newReference != null) {
        PsiElement newIdentifier = newReference.getIdentifier();
        ASTNode newIdentifierNode = newIdentifier.getNode();
        reference.getNode().replaceChild(node, newIdentifierNode);
      }
    }
    return reference;
  }

  @Nullable
  public static PsiElement getNameIdentifier(DjinniConstReference reference) {
    PsiElement identifier = reference.getIdentifier();
    ASTNode nameNode = identifier.getNode();
    if (nameNode != null) {
      return nameNode.getPsi();
    } else {
      return null;
    }
  }



  public static String getName(DjinniConstNamedValue namedValue) {
    return namedValue.getText();
  }

  public static PsiElement setName(DjinniConstNamedValue namedValue, String newName) {
    PsiElement identifier = namedValue.getIdentifier();
    ASTNode node = identifier.getNode();
    if (node != null) {
      DjinniConstNamedValue newValue = DjinniElementFactory.createConstNamedValue(namedValue.getProject(), newName);
      if (newValue != null) {
        PsiElement newIdentifier = newValue.getIdentifier();
        ASTNode newIdentifierNode = newIdentifier.getNode();
        namedValue.getNode().replaceChild(node, newIdentifierNode);
      }
    }
    return namedValue;
  }

  @Nullable
  public static PsiElement getNameIdentifier(DjinniConstNamedValue namedValue) {
    PsiElement identifier = namedValue.getIdentifier();
    ASTNode nameNode = identifier.getNode();
    if (nameNode != null) {
      return nameNode.getPsi();
    } else {
      return null;
    }
  }



  public static String getName(DjinniEnumValue enumValue) {
    return enumValue.getText();
  }

  public static PsiElement setName(DjinniEnumValue enumValue, String newName) {
    PsiElement identifier = enumValue.getIdentifier();
    ASTNode node = identifier.getNode();
    if (node != null) {
      DjinniEnumValue newValue = DjinniElementFactory.createEnumValue(enumValue.getProject(), newName);
      if (newValue != null) {
        PsiElement newIdentifier = newValue.getIdentifier();
        ASTNode newIdentifierNode = newIdentifier.getNode();
        enumValue.getNode().replaceChild(node, newIdentifierNode);
      }
    }
    return enumValue;
  }

  @Nullable
  public static PsiElement getNameIdentifier(DjinniEnumValue enumValue) {
    PsiElement identifier = enumValue.getIdentifier();
    ASTNode nameNode = identifier.getNode();
    if (nameNode != null) {
      return nameNode.getPsi();
    } else {
      return null;
    }
  }



  @NotNull
  public static String getName(DjinniImportStatement importStatement) {
    String stringLiteral = importStatement.getStringLiteral().getText();
    if (stringLiteral.length() > 2) {
      return stringLiteral.substring(1, stringLiteral.length() - 1);
    } else {
      return "";
    }
  }

  public static PsiElement setName(DjinniImportStatement importStatement, String newName) {
    ASTNode node = importStatement.getStringLiteral().getNode();
    if (node != null) {
      DjinniImportStatement newImportStatement = DjinniElementFactory.createImportStatement(importStatement.getProject(), newName);
      if (newImportStatement != null) {
        ASTNode newIdentifierNode = newImportStatement.getStringLiteral().getNode();
        importStatement.getNode().replaceChild(node, newIdentifierNode);
      }
    }
    return importStatement;
  }

  public static TextRange getRangeOfPath(DjinniImportStatement importStatement) {
    TextRange importRange = importStatement.getTextRange();
    TextRange pathRange = importStatement.getStringLiteral().getTextRange();
    assert pathRange.getLength() >= 2;  // if it is not enclosed by quotes it's not a string literal
    return new TextRange((pathRange.getStartOffset() - importRange.getStartOffset()) + 1,
                         (pathRange.getEndOffset() - importRange.getStartOffset()) - 1);
  }

  @NotNull
  public static String getPath(DjinniImportStatement importStatement) {
    TextRange rangeOfPath = getRangeOfPath(importStatement);
    String text = importStatement.getText();
    return text.substring(rangeOfPath.getStartOffset(), rangeOfPath.getEndOffset());
  }




  @NotNull
  public static String getName(DjinniExternStatement externStatement) {
    String stringLiteral = externStatement.getStringLiteral().getText();
    if (stringLiteral.length() > 2) {
      return stringLiteral.substring(1, stringLiteral.length() - 1);
    } else {
      return "";
    }
  }

  public static PsiElement setName(DjinniExternStatement externStatement, String newName) {
    ASTNode node = externStatement.getStringLiteral().getNode();
    if (node != null) {
      DjinniImportStatement newImportStatement = DjinniElementFactory.createImportStatement(externStatement.getProject(), newName);
      if (newImportStatement != null) {
        ASTNode newIdentifierNode = newImportStatement.getStringLiteral().getNode();
        externStatement.getNode().replaceChild(node, newIdentifierNode);
      }
    }
    return externStatement;
  }

  public static TextRange getRangeOfPath(DjinniExternStatement externStatement) {
    TextRange importRange = externStatement.getTextRange();
    TextRange pathRange = externStatement.getStringLiteral().getTextRange();
    assert pathRange.getLength() >= 2;  // if it is not enclosed by quotes it's not a string literal
    return new TextRange((pathRange.getStartOffset() - importRange.getStartOffset()) + 1,
                         (pathRange.getEndOffset() - importRange.getStartOffset()) - 1);
  }

  @NotNull
  public static String getPath(DjinniExternStatement externStatement) {
    TextRange rangeOfPath = getRangeOfPath(externStatement);
    String text = externStatement.getText();
    return text.substring(rangeOfPath.getStartOffset(), rangeOfPath.getEndOffset());
  }

}
