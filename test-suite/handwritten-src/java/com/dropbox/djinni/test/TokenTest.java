package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class TokenTest extends TestCase {

    private class JavaToken extends UserToken {
        public String whoami() { return "Java"; }
    }

    @Override
    protected void setUp() {
    }

    public void testTokens() {
        UserToken jt = new JavaToken();
        assertSame(TestHelpers.tokenId(jt), jt);
    }

    public void testNullToken() {
        assertSame(TestHelpers.tokenId(null), null);
    }

    public void testCppToken() {
        UserToken ct = TestHelpers.createCppToken();
        assertSame(TestHelpers.tokenId(ct), ct);
        TestHelpers.checkCppToken(ct);
        ct = null;
        System.gc();
        System.runFinalization();
    }

    public void testTokenType() {
        TestHelpers.checkTokenType(new JavaToken(), "Java");
        TestHelpers.checkTokenType(TestHelpers.createCppToken(), "C++");
        boolean threw = false;
        try {
            TestHelpers.checkTokenType(new JavaToken(), "foo");
        } catch (RuntimeException e) {
            threw = true;
        }
        assertTrue(threw);
        threw = false;
        try {
            TestHelpers.checkTokenType(TestHelpers.createCppToken(), "foo");
        } catch (RuntimeException e) {
            threw = true;
        }
        assertTrue(threw);
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
