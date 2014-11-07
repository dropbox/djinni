package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class IntegerTest extends TestCase {

    public void testIntegers() {
        AssortedIntegers i = new AssortedIntegers((byte)123, (short)20000, 1000000000, 1234567890123456789L,
                                                  (byte)123, (short)20000, 1000000000, 1234567890123456789L);
        assertEquals(i, TestHelpers.assortedIntegersId(i));
    }

}
