package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class WcharTest extends TestCase {

    private static final String STR1 = "some string with unicode \u0000, \u263A, \uD83D\uDCA9 symbols";
    private static final String STR2 = "another string with unicode \u263B, \uD83D\uDCA8 symbols";

    public void test() {
        assertEquals(WcharTestHelpers.getRecord().getS(), STR1);
        assertEquals(WcharTestHelpers.getString(), STR2);
        assertEquals(WcharTestHelpers.checkString(STR2), true);
        assertEquals(WcharTestHelpers.checkRecord(new WcharTestRec(STR1)), true);
    }
}
