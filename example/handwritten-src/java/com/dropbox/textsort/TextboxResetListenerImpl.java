package com.dropbox.textsort;

import android.widget.EditText;

public class TextboxResetListenerImpl extends TextboxResetListener {

    private EditText mTextArea;

    public TextboxResetListenerImpl(EditText textArea) {
        this.mTextArea = textArea;
    }

    @Override
    public void reset(String text) {
        mTextArea.setText(text);
    }

}
