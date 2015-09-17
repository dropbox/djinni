/*
 * Copyright 2015 Dropbox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dropbox.djinni.ideaplugin;

import com.dropbox.djinni.ideaplugin.psi.DjinniTypes;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jaetzold on 7/30/15.
 */
public class DjinniBlock extends AbstractBlock {
  private SpacingBuilder spacingBuilder;
  private Indent indent;
  private int spaceIndent;

  protected DjinniBlock(@NotNull ASTNode node,
                        @Nullable Wrap wrap,
                        @Nullable Alignment alignment,
                        SpacingBuilder spacingBuilder,
                        Indent indent,
                        int spaceIndent) {
    super(node, wrap, alignment);
    this.spacingBuilder = spacingBuilder;
    this.indent = indent;
    this.spaceIndent = spaceIndent;
  }

  static List<IElementType> memberTypes = Arrays.asList(DjinniTypes.ENUM_MEMBER, DjinniTypes.RECORD_MEMBER, DjinniTypes.INTERFACE_MEMBER, DjinniTypes.CONST_RECORD_MEMBER_ELEMENT);

  @Override
  protected List<Block> buildChildren() {
    List<Block> blocks = new ArrayList<Block>();
    ASTNode child = myNode.getFirstChildNode();
    IElementType previousChildType = null;

    while (child != null) {
      IElementType currentChildType = child.getElementType();
      if (currentChildType != TokenType.WHITE_SPACE && currentChildType != DjinniTypes.SPACE) {
        Indent indent;
        if (memberTypes.contains(currentChildType) && previousChildType != currentChildType) {
          // TODO: Add our own code style settings page
          indent = Indent.getNormalIndent();
        } else if(currentChildType == DjinniTypes.COMMENT && myNode.getTreeParent() != null) {
          indent = Indent.getNormalIndent();
        } else {
          indent = Indent.getNoneIndent();
        }
        Block block = new DjinniBlock(child, Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment(), spacingBuilder, indent,
                                      spaceIndent);
        blocks.add(block);
      }
      previousChildType = currentChildType;
      child = child.getTreeNext();
    }
    return blocks;
  }

  @Override
  public Indent getIndent() {
    return indent;
  }

  @Nullable
  @Override
  public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }
}
