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

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Created by jaetzold on 7/23/15.
 */
public class DjinniColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[] {
    new AttributesDescriptor("Comment", DjinniSyntaxHighlighter.COMMENT),
    new AttributesDescriptor("Identifier", DjinniSyntaxHighlighter.IDENTIFIER),
    new AttributesDescriptor("String literal", DjinniSyntaxHighlighter.STRING_LITERAL),
    new AttributesDescriptor("Number literal", DjinniSyntaxHighlighter.NUMBER_LITERAL),
  };


  @Nullable
  @Override
  public Icon getIcon() {
    return DjinniIcons.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new DjinniSyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "# Multi-line comments can be added here. This comment will be propagated\n" +
           "# to each generated definition.\n" +
           "my_enum = enum {\n" +
           "    option1;\n" +
           "    option2;\n" +
           "    option3;\n" +
           "}\n" +
           "\n" +
           "my_record = record {\n" +
           "    id: i32;\n" +
           "    info: string;\n" +
           "    store: set<string>;\n" +
           "    hash: map<string, i32>;\n" +
           "\n" +
           "    values: list<another_record>;\n" +
           "\n" +
           "    # Comments can also be put here\n" +
           "\n" +
           "    # Constants can be included\n" +
           "    const string_const: string = \"Constants can be put here\";\n" +
           "    const min_value: another_record = {\n" +
           "        key1 = 0,\n" +
           "        key2 = \"\",\n" +
           "    };\n" +
           "}\n" +
           "\n" +
           "another_record = record {\n" +
           "    key1: i32;\n" +
           "    key2: string;\n" +
           "} deriving (eq, ord)\n" +
           "\n" +
           "# This interface will be implemented in C++ and can be called from any language.\n" +
           "my_cpp_interface = interface +c {\n" +
           "    method_returning_nothing(value: i32);\n" +
           "    method_returning_some_type(key: string): another_record;\n" +
           "    static get_version(): i32;\n" +
           "\n" +
           "    # Interfaces can also have constants\n" +
           "    const version: i32 = 1;\n" +
           "}\n" +
           "\n" +
           "# This interface will be implemented in Java and ObjC and can be called from C++.\n" +
           "my_client_interface = interface +j +o {\n" +
           "    log_string(str: string): bool;\n" +
           "}\n";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Djinni";
  }
}
