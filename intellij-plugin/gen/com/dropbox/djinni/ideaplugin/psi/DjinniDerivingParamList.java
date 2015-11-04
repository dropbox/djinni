// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DjinniDerivingParamList extends PsiElement {

  @NotNull
  DjinniDerivingParam getDerivingParam();

  @Nullable
  DjinniDerivingParamList getDerivingParamList();

  @Nullable
  PsiElement getListSeparator();

}
