// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil.DjinniType;
import com.intellij.navigation.ItemPresentation;

public interface DjinniTypeDefinition extends DjinniNamedElement {

  @Nullable
  DjinniDerivingParamList getDerivingParamList();

  @NotNull
  List<DjinniEnumMember> getEnumMemberList();

  @Nullable
  DjinniEnumTypeVariant getEnumTypeVariant();

  @NotNull
  List<DjinniInterfaceMember> getInterfaceMemberList();

  @Nullable
  DjinniInterfaceTypeVariant getInterfaceTypeVariant();

  @NotNull
  List<DjinniRecordMember> getRecordMemberList();

  @Nullable
  DjinniRecordTypeVariant getRecordTypeVariant();

  @NotNull
  PsiElement getEq();

  @NotNull
  PsiElement getLeftBlockBrace();

  @Nullable
  PsiElement getLeftParamBrace();

  @NotNull
  PsiElement getRightBlockBrace();

  @Nullable
  PsiElement getRightParamBrace();

  @NotNull
  PsiElement getIdentifier();

  String getTypeName();

  @NotNull
  DjinniType getDjinniType();

  String getName();

  PsiElement setName(String newName);

  @Nullable
  PsiElement getNameIdentifier();

  ItemPresentation getPresentation();

}
