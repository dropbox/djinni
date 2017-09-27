// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from enum.djinni

package com.dropbox.djinni.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class EnumUsageRecord implements android.os.Parcelable {


    /*package*/ final Color mE;

    /*package*/ final Color mO;

    /*package*/ final ArrayList<Color> mL;

    /*package*/ final HashSet<Color> mS;

    /*package*/ final HashMap<Color, Color> mM;

    public EnumUsageRecord(
            @Nonnull Color e,
            @CheckForNull Color o,
            @Nonnull ArrayList<Color> l,
            @Nonnull HashSet<Color> s,
            @Nonnull HashMap<Color, Color> m) {
        this.mE = e;
        this.mO = o;
        this.mL = l;
        this.mS = s;
        this.mM = m;
    }

    @Nonnull
    public Color getE() {
        return mE;
    }

    @CheckForNull
    public Color getO() {
        return mO;
    }

    @Nonnull
    public ArrayList<Color> getL() {
        return mL;
    }

    @Nonnull
    public HashSet<Color> getS() {
        return mS;
    }

    @Nonnull
    public HashMap<Color, Color> getM() {
        return mM;
    }

    @Override
    public String toString() {
        return "EnumUsageRecord{" +
                "mE=" + mE +
                "," + "mO=" + mO +
                "," + "mL=" + mL +
                "," + "mS=" + mS +
                "," + "mM=" + mM +
        "}";
    }


    public static final android.os.Parcelable.Creator<EnumUsageRecord> CREATOR
        = new android.os.Parcelable.Creator<EnumUsageRecord>()
    {
        @Override
        public EnumUsageRecord createFromParcel(android.os.Parcel in)
        {
            return new EnumUsageRecord(in);
        }

        @Override
        public EnumUsageRecord[] newArray(int size)
        {
            return new EnumUsageRecord[size];
        }
    };

    public EnumUsageRecord(android.os.Parcel in)
    {
        this.mE = Color.values()[in.readInt()];
        if (in.readByte() == 0)
        {
            this.mO = null;
        }
        else
        {
            this.mO = Color.values()[in.readInt()];
        }
        this.mL = new ArrayList<Color>();
        in.readList(this.mL, getClass().getClassLoader());
        ArrayList<Color> mSTemp = new ArrayList<Color>();
        in.readList(mSTemp, getClass().getClassLoader());
        this.mS = new HashSet<Color>(mSTemp);
        this.mM = new HashMap<Color, Color>();
        in.readMap(this.mM, getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel out, int flags) {
        out.writeInt(this.mE.ordinal());
        if (this.mO != null)
        {
            out.writeByte((byte)1);
            out.writeInt(this.mO.ordinal());
        }
        else
        {
            out.writeByte((byte)0);
        }
        out.writeList(this.mL);
        out.writeList(new ArrayList<Color>(this.mS));
        out.writeMap(this.mM);
    }

}
