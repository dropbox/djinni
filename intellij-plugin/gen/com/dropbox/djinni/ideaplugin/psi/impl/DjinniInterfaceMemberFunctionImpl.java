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

public class DjinniInterfaceMemberFunctionImpl extends ASTWrapperPsiElement implements DjinniInterfaceMemberFunction {

  public DjinniInterfaceMemberFunctionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) ((DjinniVisitor)visitor).visitInterfaceMemberFunction(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DjinniInterfaceFunctionParamList getInterfaceFunctionParamList() {
    return findChildByClass(DjinniInterfaceFunctionParamList.class);
  }

  @Override
  @Nullable
  public DjinniTypeReference getTypeReference() {
    return findChildByClass(DjinniTypeReference.class);
  }

  @Override
  @Nullable
  public PsiElement getColon() {
    return findChildByType(COLON);
  }

  @Override
  @NotNull
  public PsiElement getLeftParamBrace() {
    return findNotNullChildByType(LEFT_PARAM_BRACE);
  }

  @Override
  @NotNull
  public PsiElement getRightParamBrace() {
    return findNotNullChildByType(RIGHT_PARAM_BRACE);
  }

  @Override
  @NotNull
  public PsiElement getSemicolon() {
    return findNotNullChildByType(SEMICOLON);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

}
