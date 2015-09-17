// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DjinniInterfaceMemberFunction extends PsiElement {

  @Nullable
  DjinniInterfaceFunctionParamList getInterfaceFunctionParamList();

  @Nullable
  DjinniTypeReference getTypeReference();

  @Nullable
  PsiElement getColon();

  @NotNull
  PsiElement getLeftParamBrace();

  @NotNull
  PsiElement getRightParamBrace();

  @NotNull
  PsiElement getSemicolon();

  @NotNull
  PsiElement getIdentifier();

}
