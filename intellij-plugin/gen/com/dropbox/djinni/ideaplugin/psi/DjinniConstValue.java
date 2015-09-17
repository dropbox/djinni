// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DjinniConstValue extends PsiElement {

  @NotNull
  List<DjinniConstRecordMemberElement> getConstRecordMemberElementList();

  @Nullable
  DjinniConstReference getConstReference();

  @Nullable
  PsiElement getLeftBlockBrace();

  @Nullable
  PsiElement getRightBlockBrace();

  @Nullable
  PsiElement getNumberLiteral();

  @Nullable
  PsiElement getStringLiteral();

}
