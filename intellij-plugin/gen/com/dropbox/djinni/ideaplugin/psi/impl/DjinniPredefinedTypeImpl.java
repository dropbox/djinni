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

public class DjinniPredefinedTypeImpl extends ASTWrapperPsiElement implements DjinniPredefinedType {

  public DjinniPredefinedTypeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DjinniVisitor visitor) {
    visitor.visitPredefinedType(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) accept((DjinniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DjinniBasicType getBasicType() {
    return findChildByClass(DjinniBasicType.class);
  }

  @Override
  @Nullable
  public DjinniGenericBasicType getGenericBasicType() {
    return findChildByClass(DjinniGenericBasicType.class);
  }

}
