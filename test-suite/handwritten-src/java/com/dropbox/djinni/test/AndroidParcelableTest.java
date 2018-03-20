package com.dropbox.djinni.test;

import junit.framework.TestCase;
import android.os.Parcel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

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

    private void performEnumTest(Color color) {
        ArrayList<Color> list = new ArrayList<Color>();
        list.add(null);
        list.add(Color.RED);
        list.add(color);
        list.add(Color.ORANGE);

        HashSet<Color> set = new HashSet<Color>();
        set.add(color);
        set.add(Color.BLUE);

        HashMap<Color, Color> map = new HashMap<Color, Color>();
        map.put(null, color);
        map.put(Color.ORANGE, Color.RED);

        Parcel parcel = new Parcel();
        EnumUsageRecord r1 = new EnumUsageRecord(Color.RED, color, list, set, map);
        r1.writeToParcel(parcel, 0);
        parcel.flush();
        EnumUsageRecord r2 = new EnumUsageRecord(parcel);

        assertEquals(r1.getE(), r2.getE());
        assertEquals(r1.getO(), r2.getO());
        assertEquals(r1.getL(), r2.getL());
        assertEquals(r1.getS(), r2.getS());
        assertEquals(r1.getM(), r2.getM());
    }

    public void testEnum() {
        performEnumTest(null);
        performEnumTest(Color.ORANGE);
        performEnumTest(Color.VIOLET);
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
