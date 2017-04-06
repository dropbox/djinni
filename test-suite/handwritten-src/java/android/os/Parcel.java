/*
 * Mock replacement for Android's implementation of android.os.Parcel
 * Used in tests to check the generation of the records that implement the parcelable interface
 */
package android.os;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public final class Parcel {

    private ByteArrayOutputStream mOutStream;
    private ObjectOutputStream mOut;
    private ObjectInputStream mIn;

    public Parcel() {
        try {
            mOutStream = new ByteArrayOutputStream();
            mOut = new ObjectOutputStream(mOutStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final void flush() {
        try {
            mOut.flush();
            mIn = new ObjectInputStream(new ByteArrayInputStream(
                mOutStream.toByteArray()));
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

    public final void writeList(List val) {
        try {
            mOut.writeInt(val.size());
            for(Object obj : val)
               mOut.writeObject(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final void readList(List outVal, ClassLoader loader) {
        try {
            int size = mIn.readInt();
            for(int i = 0; i < size; ++i)
               outVal.add(mIn.readObject());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
};
