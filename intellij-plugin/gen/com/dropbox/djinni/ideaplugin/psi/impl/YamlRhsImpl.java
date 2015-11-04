// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.dropbox.djinni.ideaplugin.psi.YamlTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.dropbox.djinni.ideaplugin.psi.*;
import com.intellij.navigation.ItemPresentation;

public class YamlRhsImpl extends ASTWrapperPsiElement implements YamlRhs {

  public YamlRhsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof YamlVisitor) ((YamlVisitor)visitor).visitRhs(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getValue() {
    return findNotNullChildByType(VALUE);
  }

  public String getName() {
    return YamlPsiImplUtil.getName(this);
  }

  public ItemPresentation getPresentation() {
    return YamlPsiImplUtil.getPresentation(this);
  }

}
