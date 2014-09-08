package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class CppExceptionTest extends TestCase {

    private CppException cppInterface;

    @Override
    protected void setUp() {
        cppInterface = CppException.get();
    }

    public void testCppException() {
        String thrown = null;
        try {
            cppInterface.throwAnException();
        } catch (RuntimeException e) {
            thrown = e.getMessage();
        }
        assertEquals("Exception Thrown", thrown);
    }

}
