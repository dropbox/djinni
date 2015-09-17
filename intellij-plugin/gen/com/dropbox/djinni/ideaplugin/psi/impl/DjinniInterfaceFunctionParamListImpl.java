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

public class DjinniInterfaceFunctionParamListImpl extends ASTWrapperPsiElement implements DjinniInterfaceFunctionParamList {

  public DjinniInterfaceFunctionParamListImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DjinniVisitor) ((DjinniVisitor)visitor).visitInterfaceFunctionParamList(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public DjinniInterfaceFunctionParam getInterfaceFunctionParam() {
    return findNotNullChildByClass(DjinniInterfaceFunctionParam.class);
  }

  @Override
  @Nullable
  public DjinniInterfaceFunctionParamList getInterfaceFunctionParamList() {
    return findChildByClass(DjinniInterfaceFunctionParamList.class);
  }

  @Override
  @Nullable
  public PsiElement getListSeparator() {
    return findChildByType(LIST_SEPARATOR);
  }

}
