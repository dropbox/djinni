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
import com.intellij.openapi.util.TextRange;

public class DjinniExternStatementImpl extends DjinniImportStatementBaseImpl implements DjinniExternStatement {

  public DjinniExternStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DjinniVisitor visitor) {
    visitor.visitExternStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) accept((DjinniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getAt() {
    return findNotNullChildByType(AT);
  }

  @Override
  @NotNull
  public PsiElement getExtern() {
    return findNotNullChildByType(EXTERN);
  }

  @Override
  @NotNull
  public PsiElement getStringLiteral() {
    return findNotNullChildByType(STRING_LITERAL);
  }

  @NotNull
  public String getName() {
    return DjinniPsiImplUtil.getName(this);
  }

  public PsiElement setName(String newName) {
    return DjinniPsiImplUtil.setName(this, newName);
  }

  public TextRange getRangeOfPath() {
    return DjinniPsiImplUtil.getRangeOfPath(this);
  }

  @NotNull
  public String getPath() {
    return DjinniPsiImplUtil.getPath(this);
  }

}
