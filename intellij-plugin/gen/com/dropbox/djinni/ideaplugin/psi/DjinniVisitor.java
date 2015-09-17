// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

public class DjinniVisitor extends PsiElementVisitor {

  public void visitBasicType(@NotNull DjinniBasicType o) {
    visitPsiElement(o);
  }

  public void visitConstMember(@NotNull DjinniConstMember o) {
    visitPsiElement(o);
  }

  public void visitConstNamedValue(@NotNull DjinniConstNamedValue o) {
    visitNamedElement(o);
  }

  public void visitConstRecordMemberElement(@NotNull DjinniConstRecordMemberElement o) {
    visitPsiElement(o);
  }

  public void visitConstReference(@NotNull DjinniConstReference o) {
    visitNamedElement(o);
  }

  public void visitConstValue(@NotNull DjinniConstValue o) {
    visitPsiElement(o);
  }

  public void visitDerivingParam(@NotNull DjinniDerivingParam o) {
    visitPsiElement(o);
  }

  public void visitDerivingParamList(@NotNull DjinniDerivingParamList o) {
    visitPsiElement(o);
  }

  public void visitEnumMember(@NotNull DjinniEnumMember o) {
    visitPsiElement(o);
  }

  public void visitEnumTypeVariant(@NotNull DjinniEnumTypeVariant o) {
    visitPsiElement(o);
  }

  public void visitEnumValue(@NotNull DjinniEnumValue o) {
    visitNamedElement(o);
  }

  public void visitExternStatement(@NotNull DjinniExternStatement o) {
    visitPsiNamedElement(o);
  }

  public void visitGenerator(@NotNull DjinniGenerator o) {
    visitPsiElement(o);
  }

  public void visitGenericBasicType(@NotNull DjinniGenericBasicType o) {
    visitPsiElement(o);
  }

  public void visitGenericBasicTypeDualParameter(@NotNull DjinniGenericBasicTypeDualParameter o) {
    visitPsiElement(o);
  }

  public void visitGenericBasicTypeSingleParameter(@NotNull DjinniGenericBasicTypeSingleParameter o) {
    visitPsiElement(o);
  }

  public void visitImportStatement(@NotNull DjinniImportStatement o) {
    visitPsiNamedElement(o);
  }

  public void visitInterfaceFunctionParam(@NotNull DjinniInterfaceFunctionParam o) {
    visitPsiElement(o);
  }

  public void visitInterfaceFunctionParamList(@NotNull DjinniInterfaceFunctionParamList o) {
    visitPsiElement(o);
  }

  public void visitInterfaceMember(@NotNull DjinniInterfaceMember o) {
    visitPsiElement(o);
  }

  public void visitInterfaceMemberFunction(@NotNull DjinniInterfaceMemberFunction o) {
    visitPsiElement(o);
  }

  public void visitInterfaceTypeVariant(@NotNull DjinniInterfaceTypeVariant o) {
    visitPsiElement(o);
  }

  public void visitPredefinedType(@NotNull DjinniPredefinedType o) {
    visitPsiElement(o);
  }

  public void visitRecordMember(@NotNull DjinniRecordMember o) {
    visitPsiElement(o);
  }

  public void visitRecordMemberVariable(@NotNull DjinniRecordMemberVariable o) {
    visitPsiElement(o);
  }

  public void visitRecordTypeVariant(@NotNull DjinniRecordTypeVariant o) {
    visitPsiElement(o);
  }

  public void visitTypeDefinition(@NotNull DjinniTypeDefinition o) {
    visitNamedElement(o);
  }

  public void visitTypeReference(@NotNull DjinniTypeReference o) {
    visitNamedElement(o);
  }

  public void visitNamedElement(@NotNull DjinniNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiNamedElement(@NotNull PsiNamedElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
