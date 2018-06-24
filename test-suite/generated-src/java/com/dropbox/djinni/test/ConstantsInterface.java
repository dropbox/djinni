// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

package com.dropbox.djinni.test;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Interface containing constants */
public abstract class ConstantsInterface {
    public static final boolean BOOL_CONSTANT = true;

    public static final byte I8_CONSTANT = 1;

    public static final short I16_CONSTANT = 2;

    /** i32_constant has documentation. */
    public static final int I32_CONSTANT = 3;

    /**
     * i64_constant has long documentation.
     * (Second line of multi-line documentation.
     *   Indented third line of multi-line documentation.)
     */
    public static final long I64_CONSTANT = 4l;

    public static final float F32_CONSTANT = 5.0f;

    public static final double F64_CONSTANT = 5.0;

    @Nonnull
    public static final ConstantEnum CONST_ENUM = ConstantEnum.SOME_VALUE;

    @CheckForNull
    public static final Boolean OPT_BOOL_CONSTANT = true;

    @CheckForNull
    public static final Byte OPT_I8_CONSTANT = 1;

    /** opt_i16_constant has documentation. */
    @CheckForNull
    public static final Short OPT_I16_CONSTANT = 2;

    @CheckForNull
    public static final Integer OPT_I32_CONSTANT = 3;

    @CheckForNull
    public static final Long OPT_I64_CONSTANT = 4l;

    /**
     * opt_f32_constant has long documentation.
     * (Second line of multi-line documentation.
     *   Indented third line of multi-line documentation.)
     */
    @CheckForNull
    public static final Float OPT_F32_CONSTANT = 5.0f;

    @CheckForNull
    public static final Double OPT_F64_CONSTANT = 5.0;

    @Nonnull
    public static final String STRING_CONSTANT = "string-constant";

    @CheckForNull
    public static final String OPT_STRING_CONSTANT = "string-constant";

    @Nonnull
    public static final ConstantRecord OBJECT_CONSTANT = new ConstantRecord(
        I32_CONSTANT /* mSomeInteger */ ,
        STRING_CONSTANT /* mSomeString */ );

    // No support for null optional constants
    /** No support for optional constant records */
    // No support for constant binary, list, set, map
    public abstract void dummy();

    private static final class CppProxy extends ConstantsInterface
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

        @Override
        public void dummy()
        {
            assert !this.destroyed.get() : "trying to use a destroyed object";
            native_dummy(this.nativeRef);
        }
        private native void native_dummy(long _nativeRef);
    }
}
