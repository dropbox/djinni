// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.dropbox.djinni.ideaplugin.psi.impl.*;

public interface YamlTypes {

  IElementType ENTRY = new YamlElementType("ENTRY");
  IElementType LHS = new YamlElementType("LHS");
  IElementType RHS = new YamlElementType("RHS");

  IElementType COMMENT = new YamlTokenType("COMMENT");
  IElementType CRLF = new YamlTokenType("CRLF");
  IElementType KEY = new YamlTokenType("KEY");
  IElementType SEPARATOR = new YamlTokenType("SEPARATOR");
  IElementType VALUE = new YamlTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ENTRY) {
        return new YamlEntryImpl(node);
      }
      else if (type == LHS) {
        return new YamlLhsImpl(node);
      }
      else if (type == RHS) {
        return new YamlRhsImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
