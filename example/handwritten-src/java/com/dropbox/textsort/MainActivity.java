package com.dropbox.textsort;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {

    private EditText text;
    private SortItems sortItemsInterface;
    private TextboxListener textboxListener;

    static {
        System.loadLibrary("textsort_jni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (EditText) findViewById(R.id.editText);
        textboxListener = new TextboxListenerImpl(text);
        // Call JNI to initiate the SortItems object from the given textboxListener and translate to Java
        sortItemsInterface = SortItems.createWithListener(textboxListener);
    }

    public void sort(View view) {
        String str = text.getText().toString();
        ArrayList<String> items = new ArrayList<String>(Arrays.asList(str.split("\n")));
        ItemList itemList = new ItemList(items);
        sortItemsInterface.sort(itemList);
    }

}
