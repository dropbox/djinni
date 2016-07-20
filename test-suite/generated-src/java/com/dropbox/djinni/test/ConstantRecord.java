// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

package com.dropbox.djinni.test;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Record for use in constants */
public class ConstantRecord implements android.os.Parcelable {


    /*package*/ final int mSomeInteger;

    /*package*/ final String mSomeString;

    public ConstantRecord(
            int someInteger,
            @Nonnull String someString) {
        this.mSomeInteger = someInteger;
        this.mSomeString = someString;
    }

    public int getSomeInteger() {
        return mSomeInteger;
    }

    @Nonnull
    public String getSomeString() {
        return mSomeString;
    }

    @Override
    public String toString() {
        return "ConstantRecord{" +
                "mSomeInteger=" + mSomeInteger +
                "," + "mSomeString=" + mSomeString +
        "}";
    }


    public static final android.os.Parcelable.Creator<ConstantRecord> CREATOR
        = new android.os.Parcelable.Creator<ConstantRecord>()
    {
        @Override
        public ConstantRecord createFromParcel(android.os.Parcel in)
        {
            return new ConstantRecord(in);
        }

        @Override
        public ConstantRecord[] newArray(int size)
        {
            return new ConstantRecord[size];
        }
    };

    public ConstantRecord(android.os.Parcel in)
    {
        this.mSomeInteger = in.readInt();
        this.mSomeString = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel out, int flags) {
        out.writeInt(this.mSomeInteger);
        out.writeString(this.mSomeString);
    }

}
