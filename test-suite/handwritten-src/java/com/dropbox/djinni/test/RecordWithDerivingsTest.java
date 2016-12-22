package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.Date;

public class RecordWithDerivingsTest extends TestCase {

    private final RecordWithDerivings record1 = new RecordWithDerivings((byte)1, (short)2, 3, 4, 5.0f, 6.0,
            new Date(7), "String8");
    private final RecordWithDerivings record1A = new RecordWithDerivings((byte)1, (short)2, 3, 4, 5.0f, 6.0,
            new Date(7), "String8");
    private final RecordWithDerivings record2 = new RecordWithDerivings((byte)1, (short)2, 3, 4, 5.0f, 6.0,
            new Date(7), "String888");
    private final RecordWithDerivings record3 = new RecordWithDerivings((byte)111, (short)2, 3, 4, 5.0f, 6.0,
            new Date(7), "String8");

    public void testRecordOrd() {
        assertTrue(record1.compareTo(record1A) == 0);
        assertTrue(record1A.compareTo(record1) == 0);
        assertTrue(record1.compareTo(record2) < 0);
        assertTrue(record2.compareTo(record1) > 0);
        assertTrue(record1.compareTo(record3) < 0);
        assertTrue(record3.compareTo(record1) > 0);
        assertTrue(record2.compareTo(record3) < 0);
        assertTrue(record3.compareTo(record2) > 0);
    }

    public void testRecordEq() {
        assertTrue(record1.equals(record1A));
        assertTrue(record1A.equals(record1));
        assertFalse(record1.equals(record2));
        assertFalse(record2.equals(record1));
        assertFalse(record1.equals(record3));
        assertFalse(record3.equals(record1));
        assertFalse(record2.equals(record3));
        assertFalse(record3.equals(record2));

        assertTrue(record1.hashCode() == record1A.hashCode());
        assertTrue(record1.hashCode() != record2.hashCode());
        assertTrue(record1.hashCode() != record3.hashCode());
        assertTrue(record2.hashCode() != record3.hashCode());
    }

    private final RecordWithNestedDerivings nestedRecord1 = new RecordWithNestedDerivings(1, record1);
    private final RecordWithNestedDerivings nestedRecord1A = new RecordWithNestedDerivings(1, record1A);
    private final RecordWithNestedDerivings nestedRecord2 = new RecordWithNestedDerivings(1, record2);

    public void testNestedRecordOrd() {
        assertTrue(nestedRecord1.compareTo(nestedRecord1A) == 0);
        assertTrue(nestedRecord1.compareTo(nestedRecord2) < 0);
        assertTrue(nestedRecord2.compareTo(nestedRecord1) > 0);
    }

    public void testNestedRecordEq() {
        assertTrue(nestedRecord1.equals(nestedRecord1A));
        assertFalse(nestedRecord1.equals(nestedRecord2));
        assertFalse(nestedRecord2.equals(nestedRecord1));
    }

}
