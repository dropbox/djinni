package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.HashSet;

public class SetRecordTest extends TestCase {

    public void testCppSetToJavaSet() {
        SetRecord jSetRecord = TestHelpers.getSetRecord();
        HashSet<String> jSet = jSetRecord.getSet();
        assertEquals("Size 3 expected for Set", 3, jSet.size());
        assertTrue("\"StringA\" expected but does not exist", jSet.contains("StringA"));
        assertTrue("\"StringB\" expected but does not exist", jSet.contains("StringB"));
        assertTrue("\"StringC\" expected but does not exist", jSet.contains("StringC"));
    }

    public void testJavaSetToCppSet() {
        HashSet<String> jSet = new HashSet<String>();
        HashSet<Integer> iSet = new HashSet<Integer>();
        jSet.add("StringA");
        jSet.add("StringB");
        jSet.add("StringC");
        SetRecord jSetRecord = new SetRecord(jSet, iSet);
        assertTrue("checkSetRecord", TestHelpers.checkSetRecord(jSetRecord));
    }
}
