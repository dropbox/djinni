// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DjinniConstMember extends PsiElement {

  @NotNull
  DjinniConstNamedValue getConstNamedValue();

  @NotNull
  DjinniConstValue getConstValue();

  @NotNull
  DjinniTypeReference getTypeReference();

  @NotNull
  PsiElement getColon();

  @NotNull
  PsiElement getEq();

  @NotNull
  PsiElement getSemicolon();

}
