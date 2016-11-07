package com.dropbox.djinni.test;

import junit.framework.TestCase;
import java.util.ArrayList;

/**
 * Created by Sam on 04/11/2016.
 */
public class PropertyTest extends TestCase {

    public void testProperties() {

        PropertiesTestHelper testHelper = PropertiesTestHelper.createNew();

        // Test integer property.
        testHelper.setItem(1);

        assertEquals(1, testHelper.getItem());

        // Test string property.
        testHelper.setTestString("fooBar");

        assertEquals("fooBar", testHelper.getTestString());

        // Test list property.
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);

        testHelper.setTestList(list);

        for (int i = 0; i < list.size();i++) {
            assertEquals(list.get(i), testHelper.getTestList().get(i));
        }
    }

    public void testReadOnlyProperty() {

        PropertiesTestHelper testHelper = PropertiesTestHelper.createNew();

        // Test readonly property.
        assertEquals(true, testHelper.getReadOnlyBool());

        try {
            assertNull(PropertiesTestHelper.class.getDeclaredMethod("setReadOnlyBool", void.class));
        } catch (NoSuchMethodException e) {
        }
    }
}
