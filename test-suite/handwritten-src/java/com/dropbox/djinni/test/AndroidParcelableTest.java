package com.dropbox.djinni.test;

import junit.framework.TestCase;
import android.os.Parcel;
import java.util.ArrayList;
import java.util.HashSet;

public class AndroidParcelableTest extends TestCase {

    public void testAssortedPrimitives() {
        AssortedPrimitives p1 = new AssortedPrimitives(true, (byte)123, (short)20000, 1000000000, 1234567890123456789L, 1.23f, 1.23d,
                                                      true, (byte)123, (short)20000, 1000000000, 1234567890123456789L, 1.23f, 1.23d);
        Parcel parcel = new Parcel();
        p1.writeToParcel(parcel, 0);
        parcel.flush();
        AssortedPrimitives p2 = new AssortedPrimitives(parcel);
        assertEquals(p1, p2);
    }

    public void testNativeCollection() {
        HashSet<String> jSet1 = new HashSet<String>();
        jSet1.add("String1");
        jSet1.add("String2");
        HashSet<String> jSet2 = new HashSet<String>();
        jSet2.add("StringA");
        jSet2.add("StringB");
        ArrayList<HashSet<String>> jList = new ArrayList<HashSet<String>>();
        jList.add(jSet1);
        jList.add(jSet2);
        NestedCollection c1 = new NestedCollection(jList);

        Parcel parcel = new Parcel();
        c1.writeToParcel(parcel, 0);
        parcel.flush();
        NestedCollection c2 = new NestedCollection(parcel);

        assertEquals(c1.getSetList(), c2.getSetList());
    }

    private void performTestOptEnum(Color color) {
        Parcel parcel = new Parcel();
        OptColorRecord r1 = new OptColorRecord(color);
        r1.writeToParcel(parcel, 0);
        parcel.flush();
        OptColorRecord r2 = new OptColorRecord(parcel);
        assertEquals(r1.getMyColor(), r2.getMyColor());
    }

    public void testOptEnum() {
        performTestOptEnum(null);
        performTestOptEnum(Color.ORANGE);
        performTestOptEnum(Color.VIOLET);
    }

    public void testExternType() {
        DateRecord dr = new DateRecord(new java.util.Date());

        Parcel parcel = new Parcel();
        dr.writeToParcel(parcel, 0);
        parcel.flush();
        DateRecord dr2 = new DateRecord(parcel);

        assertEquals(dr, dr2);
    }
}
