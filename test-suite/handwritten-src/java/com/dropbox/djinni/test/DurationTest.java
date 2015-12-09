package com.dropbox.djinni.test;

import junit.framework.TestCase;

import java.time.Duration;

public class DurationTest extends TestCase {

    public void test() {
        assertEquals(TestDuration.hoursString(Duration.ofHours(1)), "1");
        assertEquals(TestDuration.minutesString(Duration.ofMinutes(1)), "1");
        assertEquals(TestDuration.secondsString(Duration.ofSeconds(1)), "1");
        assertEquals(TestDuration.millisString(Duration.ofMillis(1)), "1");
        assertEquals(TestDuration.microsString(Duration.ofNanos(1000)), "1");
        assertEquals(TestDuration.nanosString(Duration.ofNanos(1)), "1");

        assertEquals(TestDuration.hours(1).toHours(), 1);
        assertEquals(TestDuration.minutes(1).toMinutes(), 1);
        assertEquals(TestDuration.seconds(1).getSeconds(), 1);
        assertEquals(TestDuration.millis(1).toMillis(), 1);
        assertEquals(TestDuration.micros(1).toNanos(), 1000);
        assertEquals(TestDuration.nanos(1).toNanos(), 1);

        assertEquals(TestDuration.hoursf(1.5).toMinutes(), 90);
        assertEquals(TestDuration.minutesf(1.5).getSeconds(), 90);
        assertEquals(TestDuration.secondsf(1.5).toMillis(), 1500);
        assertEquals(TestDuration.millisf(1.5).toNanos(), 1500 * 1000);
        assertEquals(TestDuration.microsf(1.5).toNanos(), 1500);
        assertEquals(TestDuration.nanosf(1.0).toNanos(), 1);

        assertEquals(TestDuration.box(1).getSeconds(), 1);
        assertEquals(TestDuration.box(-1), null);

        assertEquals(TestDuration.unbox(Duration.ofSeconds(1)), 1);
        assertEquals(TestDuration.unbox(null), -1);
    }
}
