// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from wchar_test.djinni

package com.dropbox.djinni.test;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface WcharTestHelpers {
    @Nonnull
    public static WcharTestRec getRecord()
    {
        return StaticNativeMethods.getRecord();
    }

    @Nonnull
    public static String getString()
    {
        return StaticNativeMethods.getString();
    }

    public static boolean checkString(@Nonnull String str)
    {
        return StaticNativeMethods.checkString(str);
    }

    public static boolean checkRecord(@Nonnull WcharTestRec rec)
    {
        return StaticNativeMethods.checkRecord(rec);
    }

    static final class StaticNativeMethods
    {

        @Nonnull
        public static native WcharTestRec getRecord();

        @Nonnull
        public static native String getString();

        public static native boolean checkString(@Nonnull String str);

        public static native boolean checkRecord(@Nonnull WcharTestRec rec);
    }

    static final class CppProxy implements WcharTestHelpers
    {
        private final long nativeRef;
        private final AtomicBoolean destroyed = new AtomicBoolean(false);

        private CppProxy(long nativeRef)
        {
            if (nativeRef == 0) throw new RuntimeException("nativeRef is zero");
            this.nativeRef = nativeRef;
        }

        private native void nativeDestroy(long nativeRef);
        public void destroy()
        {
            boolean destroyed = this.destroyed.getAndSet(true);
            if (!destroyed) nativeDestroy(this.nativeRef);
        }
        protected void finalize() throws java.lang.Throwable
        {
            destroy();
            super.finalize();
        }
    }
}
