// This is a generated file. Not intended for manual editing.
package com.dropbox.djinni.ideaplugin.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.dropbox.djinni.ideaplugin.psi.YamlTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class YamlParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ENTRY) {
      r = entry(b, 0);
    }
    else if (t == LHS) {
      r = lhs(b, 0);
    }
    else if (t == RHS) {
      r = rhs(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return simpleFile(b, l + 1);
  }

  /* ********************************************************** */
  // (lhs? SEPARATOR rhs?) | lhs
  public static boolean entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry")) return false;
    if (!nextTokenIs(b, "<entry>", KEY, SEPARATOR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<entry>");
    r = entry_0(b, l + 1);
    if (!r) r = lhs(b, l + 1);
    exit_section_(b, l, m, ENTRY, r, false, null);
    return r;
  }

  // lhs? SEPARATOR rhs?
  private static boolean entry_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = entry_0_0(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && entry_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // lhs?
  private static boolean entry_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_0_0")) return false;
    lhs(b, l + 1);
    return true;
  }

  // rhs?
  private static boolean entry_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_0_2")) return false;
    rhs(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // entry|COMMENT|CRLF
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = entry(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, CRLF);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // KEY
  public static boolean lhs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhs")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEY);
    exit_section_(b, m, LHS, r);
    return r;
  }

  /* ********************************************************** */
  // VALUE
  public static boolean rhs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs")) return false;
    if (!nextTokenIs(b, VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    exit_section_(b, m, RHS, r);
    return r;
  }

  /* ********************************************************** */
  // item_*
  static boolean simpleFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleFile", c)) break;
      c = current_position_(b);
    }
    return true;
  }

}
