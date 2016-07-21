package android.os;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class Parcel {

    private FileOutputStream mOutFile;
    private FileInputStream mInFile;
    private ObjectOutputStream mOut;
    private ObjectInputStream mIn;

    public Parcel() {
        final String fname = "parcel.tmp";
        try {
            mOutFile = new FileOutputStream(fname);
            mInFile = new FileInputStream(fname);
            mOut = new ObjectOutputStream(mOutFile);
            mIn = new ObjectInputStream(mInFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final void flush() {
        try {
            mOut.flush();
            mOutFile.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final void writeString(String val) {
        try {
            mOut.writeUTF(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final String readString() {
        try {
            return mIn.readUTF();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public final void writeInt(int val) {
        try {
            mOut.writeInt(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final int readInt() {
        try {
            return mIn.readInt();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public final void writeLong(long val) {
        try {
            mOut.writeLong(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final long readLong() {
        try {
            return mIn.readLong();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public final void writeFloat(float val) {
        try {
            mOut.writeFloat(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final float readFloat() {
        try {
            return mIn.readFloat();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public final void writeDouble(double val) {
        try {
            mOut.writeDouble(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final double readDouble() {
        try {
            return mIn.readDouble();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public final void writeByte(byte val) {
        try {
            mOut.writeByte(val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final byte readByte() {
        try {
            return mIn.readByte();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public final void writeSerializable(Serializable s) {
        try {
            mOut.writeObject(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final Serializable readSerializable() {
        try {
            return (Serializable)mIn.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
};
