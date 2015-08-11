package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class PrimitivesTest extends TestCase {

    public void testPrimitives() {
        AssortedPrimitives p = new AssortedPrimitives(true, (byte)123, (short)20000, 1000000000, 1234567890123456789L, 1.23f, 1.23d,
                                                      true, (byte)123, (short)20000, 1000000000, 1234567890123456789L, 1.23f, 1.23d);
        assertEquals(p, TestHelpers.assortedPrimitivesId(p));
    }

}
