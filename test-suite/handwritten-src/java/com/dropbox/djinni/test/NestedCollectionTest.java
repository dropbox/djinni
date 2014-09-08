package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NestedCollectionTest extends TestCase {

    private NestedCollection jNestedCollection;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HashSet<String> jSet1 = new HashSet<String>();
        jSet1.add("String1");
        jSet1.add("String2");
        HashSet<String> jSet2 = new HashSet<String>();
        jSet2.add("StringA");
        jSet2.add("StringB");
        ArrayList<HashSet<String>> jList = new ArrayList<HashSet<String>>();
        jList.add(jSet1);
        jList.add(jSet2);
        jNestedCollection = new NestedCollection(jList);
    }

    public void testCppNestedRecordToJavaNestedCollection() {
        NestedCollection converted = TestHelpers.getNestedCollection();
        assertEquals("List expected to be equivalent", jNestedCollection.getSetList(), converted.getSetList());
    }

    public void testJavaNestedRecordToCppNestedCollection() {
        assertTrue("checkNestedCollection", TestHelpers.checkNestedCollection(jNestedCollection));
    }
}
