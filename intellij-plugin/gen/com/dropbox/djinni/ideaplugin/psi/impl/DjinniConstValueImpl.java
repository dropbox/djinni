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

public class DjinniConstValueImpl extends ASTWrapperPsiElement implements DjinniConstValue {

  public DjinniConstValueImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DjinniVisitor visitor) {
    visitor.visitConstValue(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) accept((DjinniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DjinniConstRecordMemberElement> getConstRecordMemberElementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DjinniConstRecordMemberElement.class);
  }

  @Override
  @Nullable
  public DjinniConstReference getConstReference() {
    return findChildByClass(DjinniConstReference.class);
  }

  @Override
  @Nullable
  public PsiElement getLeftBlockBrace() {
    return findChildByType(LEFT_BLOCK_BRACE);
  }

  @Override
  @Nullable
  public PsiElement getRightBlockBrace() {
    return findChildByType(RIGHT_BLOCK_BRACE);
  }

  @Override
  @Nullable
  public PsiElement getNumberLiteral() {
    return findChildByType(NUMBER_LITERAL);
  }

  @Override
  @Nullable
  public PsiElement getStringLiteral() {
    return findChildByType(STRING_LITERAL);
  }

}
