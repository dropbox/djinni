package com.dropbox.djinni.test;

import junit.framework.TestCase;

class MockConstants extends Constants {
    @Override
    public String toString() {
        return "MockConstants{}";
    }
}

public class MockRecordTest extends TestCase {

    public void testMockConstants() {
        Constants mock = new MockConstants();
        assertEquals("The toString() method should be overridden.", "MockConstants{}", mock.toString());
    }
}
