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

public class DjinniTypeReferenceImpl extends DjinniReferenceImpl implements DjinniTypeReference {

  public DjinniTypeReferenceImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) ((DjinniVisitor)visitor).visitTypeReference(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DjinniPredefinedType getPredefinedType() {
    return findChildByClass(DjinniPredefinedType.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
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
