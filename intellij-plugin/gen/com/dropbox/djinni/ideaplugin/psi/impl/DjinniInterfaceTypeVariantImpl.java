// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.dropbox.djinni.ideaplugin.psi.DjinniTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.dropbox.djinni.ideaplugin.psi.*;

public class DjinniInterfaceTypeVariantImpl extends ASTWrapperPsiElement implements DjinniInterfaceTypeVariant {

  public DjinniInterfaceTypeVariantImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) ((DjinniVisitor)visitor).visitInterfaceTypeVariant(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DjinniGenerator> getGeneratorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DjinniGenerator.class);
  }

}
