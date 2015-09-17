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
import com.dropbox.djinni.ideaplugin.psi.impl.DjinniPsiImplUtil.DjinniType;
import com.intellij.navigation.ItemPresentation;

public class DjinniTypeDefinitionImpl extends DjinniNamedElementImpl implements DjinniTypeDefinition {

  public DjinniTypeDefinitionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) ((DjinniVisitor)visitor).visitTypeDefinition(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DjinniDerivingParamList getDerivingParamList() {
    return findChildByClass(DjinniDerivingParamList.class);
  }

  @Override
  @NotNull
  public List<DjinniEnumMember> getEnumMemberList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DjinniEnumMember.class);
  }

  @Override
  @Nullable
  public DjinniEnumTypeVariant getEnumTypeVariant() {
    return findChildByClass(DjinniEnumTypeVariant.class);
  }

  @Override
  @NotNull
  public List<DjinniInterfaceMember> getInterfaceMemberList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DjinniInterfaceMember.class);
  }

  @Override
  @Nullable
  public DjinniInterfaceTypeVariant getInterfaceTypeVariant() {
    return findChildByClass(DjinniInterfaceTypeVariant.class);
  }

  @Override
  @NotNull
  public List<DjinniRecordMember> getRecordMemberList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DjinniRecordMember.class);
  }

  @Override
  @Nullable
  public DjinniRecordTypeVariant getRecordTypeVariant() {
    return findChildByClass(DjinniRecordTypeVariant.class);
  }

  @Override
  @NotNull
  public PsiElement getEq() {
    return findNotNullChildByType(EQ);
  }

  @Override
  @NotNull
  public PsiElement getLeftBlockBrace() {
    return findNotNullChildByType(LEFT_BLOCK_BRACE);
  }

  @Override
  @Nullable
  public PsiElement getLeftParamBrace() {
    return findChildByType(LEFT_PARAM_BRACE);
  }

  @Override
  @NotNull
  public PsiElement getRightBlockBrace() {
    return findNotNullChildByType(RIGHT_BLOCK_BRACE);
  }

  @Override
  @Nullable
  public PsiElement getRightParamBrace() {
    return findChildByType(RIGHT_PARAM_BRACE);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

  public String getTypeName() {
    return DjinniPsiImplUtil.getTypeName(this);
  }

  @NotNull
  public DjinniType getDjinniType() {
    return DjinniPsiImplUtil.getDjinniType(this);
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

  public ItemPresentation getPresentation() {
    return DjinniPsiImplUtil.getPresentation(this);
  }

}
