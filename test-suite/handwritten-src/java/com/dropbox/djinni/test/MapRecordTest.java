package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class MapRecordTest extends TestCase {

    public void testCppMapToJavaMap() {
        checkJavaMap(TestHelpers.getMap());
    }

    public void testEmptyCppMapToJavaMap() {
        assertEquals("Empty map expected", 0, TestHelpers.getEmptyMap().size());
    }

    public void testCppMapListToJavaMapList() {
        MapListRecord jMapListRecord = TestHelpers.getMapListRecord();
        ArrayList<HashMap<String, Long>> jMapList = jMapListRecord.getMapList();
        assertEquals("List with 1 map expected", 1, jMapList.size());
        checkJavaMap(jMapList.get(0));
    }

    public void testJavaMapToCppMap() {
        assertTrue("checkMap", TestHelpers.checkMap(getJavaMap()));
    }

    public void testEmptyJavaMapToCppMap() {
        assertTrue("checkEmptyMap", TestHelpers.checkEmptyMap(new HashMap<String, Long>()));
    }

    public void testJavaMapListToCppMapList() {
        ArrayList<HashMap<String, Long>> jMapList = new ArrayList<HashMap<String, Long>>();
        jMapList.add(getJavaMap());
        assertTrue("checkMapListRecord", TestHelpers.checkMapListRecord(new MapListRecord(jMapList)));
    }

    private HashMap<String, Long> getJavaMap() {
        HashMap<String, Long> jMap = new HashMap<String, Long>();
        jMap.put("String1", (long)1);
        jMap.put("String2", (long)2);
        jMap.put("String3", (long)3);
        return jMap;
    }

    private void checkJavaMap(HashMap<String, Long> jMap) {
        assertEquals("Size 3 expected for HashMap", 3, jMap.size());
        assertEquals("\"String1\"->1 expected.", jMap.get("String1").longValue(), 1);
        assertEquals("\"String2\"->2 expected.", jMap.get("String2").longValue(), 2);
        assertEquals("\"String3\"->3 expected.", jMap.get("String3").longValue(), 3);
    }

}
