// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DjinniTypeReference extends DjinniNamedElement {

  @Nullable
  DjinniPredefinedType getPredefinedType();

  @Nullable
  PsiElement getIdentifier();

  String getName();

  PsiElement setName(String newName);

  @Nullable
  PsiElement getNameIdentifier();

}
