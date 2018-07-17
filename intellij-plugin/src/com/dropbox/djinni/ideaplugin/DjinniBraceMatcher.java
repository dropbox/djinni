package com.dropbox.djinni.ideaplugin;

import com.dropbox.djinni.ideaplugin.psi.DjinniTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DjinniBraceMatcher implements PairedBraceMatcher {
  public static @NotNull
  BracePair[] PAIRS = new BracePair[]{
      new BracePair(DjinniTypes.LEFT_BLOCK_BRACE, DjinniTypes.RIGHT_BLOCK_BRACE, false),
      new BracePair(DjinniTypes.LEFT_GENERICS_BRACE, DjinniTypes.RIGHT_GENERICS_BRACE, false),
      new BracePair(DjinniTypes.LEFT_PARAM_BRACE, DjinniTypes.RIGHT_PARAM_BRACE, false),
  };

  @NotNull
  @Override
  public BracePair[] getPairs() {
    return PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
