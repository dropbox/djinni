package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class TokenTest extends TestCase {

    private class JavaToken extends Token {
    }

    @Override
    protected void setUp() {
    }

    public void testTokens() {
        Token jt = new JavaToken();
        assertSame(TestHelpers.tokenId(jt), jt);
    }

    public void testNullToken() {
        assertSame(TestHelpers.tokenId(null), null);
    }

    public void testCppToken() {
        Token ct = TestHelpers.createCppToken();
        assertSame(TestHelpers.tokenId(ct), ct);
        TestHelpers.checkCppToken(ct);
        ct = null;
        System.gc();
        System.runFinalization();
    }

    public void testNotCppToken() {
        boolean threw = false;
        try {
            TestHelpers.checkCppToken(new JavaToken());
        } catch (RuntimeException e) {
            threw = true;
        }
        assertTrue(threw);
        System.gc();
    }
}
