package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.HashMap;

public class EnumTest extends TestCase {

    public void testEnumKey() {
        HashMap<Color, String> m = new HashMap<Color, String>();
        m.put(Color.RED, "red");
        m.put(Color.ORANGE, "orange");
        m.put(Color.YELLOW, "yellow");
        m.put(Color.GREEN, "green");
        m.put(Color.BLUE, "blue");
        m.put(Color.INDIGO, "indigo");
        m.put(Color.VIOLET, "violet");
        TestHelpers.checkEnumMap(m);
    }
}
