package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.util.EnumSet;
import java.util.HashMap;

public class EnumTest extends TestCase {

    public void testEnumKey() {
        HashMap<Color, String> m = new HashMap<Color, String>();
        m.put(Color.RED, "red");
        m.put(Color.ORANGE, "orange");
        m.put(Color.YELLOW, "yellow");
        m.put(Color.GREEN, "green");
        m.put(Color.BLUE, "blue");
        m.put(Color.INDIGO, "indigo");
        m.put(Color.VIOLET, "violet");
        TestHelpers.checkEnumMap(m);
    }

    public void testAccessFlagRoundtrip() {
        EnumSet[] flags = {
            EnumSet.noneOf(AccessFlags.class),
            EnumSet.allOf(AccessFlags.class),
            EnumSet.of(AccessFlags.OWNER_READ),
            EnumSet.of(AccessFlags.OWNER_READ, AccessFlags.OWNER_WRITE),
            EnumSet.of(AccessFlags.OWNER_READ, AccessFlags.OWNER_WRITE, AccessFlags.OWNER_EXECUTE),
        };

        for(EnumSet flag : flags) {
            assertEquals(flag, FlagRoundtrip.roundtripAccess(flag));
            assertEquals(flag, FlagRoundtrip.roundtripAccessBoxed(flag));
        }
        assertEquals(null, FlagRoundtrip.roundtripAccessBoxed(null));
    }

    public void testEmptyFlagRoundtrip() {
        EnumSet[] flags = {
            EnumSet.noneOf(EmptyFlags.class),
            EnumSet.allOf(EmptyFlags.class),
        };

        for(EnumSet flag : flags) {
            assertEquals(flag, FlagRoundtrip.roundtripEmpty(flag));
            assertEquals(flag, FlagRoundtrip.roundtripEmptyBoxed(flag));
        }
        assertEquals(null, FlagRoundtrip.roundtripEmptyBoxed(null));
    }
}
