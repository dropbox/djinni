// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DjinniRecordMemberVariable extends PsiElement {

  @NotNull
  DjinniTypeReference getTypeReference();

  @NotNull
  PsiElement getColon();

  @NotNull
  PsiElement getSemicolon();

  @NotNull
  PsiElement getIdentifier();

}
