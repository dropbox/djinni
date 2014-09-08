package com.dropbox.textsort;

import android.widget.EditText;

import java.util.ArrayList;

public class TextboxListenerImpl extends TextboxListener {

    private EditText mTextArea;

    public TextboxListenerImpl(EditText textArea) {
        this.mTextArea = textArea;
    }

    @Override
    public void update(ItemList items) {
        ArrayList<String> list = items.getItems();
        StringBuilder builder = new StringBuilder();
        for (String str : list) {
            builder.append(str);
            builder.append("\n");
        }
        mTextArea.setText(builder);
    }
}
