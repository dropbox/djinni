package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.ArrayList;

public class PrimitiveListTest extends TestCase {

    private PrimitiveList jPrimitiveList;

    @Override
    protected void setUp() {
        ArrayList<Long> list = new ArrayList<Long>();
        list.add((long)1);
        list.add((long)2);
        list.add((long)3);
        jPrimitiveList = new PrimitiveList(list);
    }

    public void testJavaPrimitiveListToCpp()
    {
        assertTrue("checkPrimitiveList", TestHelpers.checkPrimitiveList(jPrimitiveList));
    }

    public void testCppPrimitiveListToJava()
    {
        PrimitiveList converted = TestHelpers.getPrimitiveList();
        assertEquals(converted.getList(), jPrimitiveList.getList());
    }
}
