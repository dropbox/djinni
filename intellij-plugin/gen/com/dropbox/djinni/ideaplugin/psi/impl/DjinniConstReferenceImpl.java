// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.dropbox.djinni.ideaplugin.psi.DjinniTypes.*;
import com.dropbox.djinni.ideaplugin.psi.*;

public class DjinniConstReferenceImpl extends DjinniReferenceImpl implements DjinniConstReference {

  public DjinniConstReferenceImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) ((DjinniVisitor)visitor).visitConstReference(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

  public String getName() {
    return DjinniPsiImplUtil.getName(this);
  }

  public PsiElement setName(String newName) {
    return DjinniPsiImplUtil.setName(this, newName);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return DjinniPsiImplUtil.getNameIdentifier(this);
  }

}
