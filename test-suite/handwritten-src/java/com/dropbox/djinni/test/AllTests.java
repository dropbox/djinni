package com.dropbox.djinni.test;

import com.dropbox.djinni.NativeLibLoader;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.JUnitCore;

public class AllTests extends TestSuite {

    public static Test suite() {
        TestSuite mySuite = new TestSuite("Djinni Tests");
        mySuite.addTestSuite(SetRecordTest.class);
        mySuite.addTestSuite(NestedCollectionTest.class);
        mySuite.addTestSuite(MapRecordTest.class);
        mySuite.addTestSuite(PrimitiveListTest.class);
        mySuite.addTestSuite(RecordWithDerivingsTest.class);
        mySuite.addTestSuite(CppExceptionTest.class);
        mySuite.addTestSuite(ClientInterfaceTest.class);
        mySuite.addTestSuite(EnumTest.class);
        mySuite.addTestSuite(PrimitivesTest.class);
        mySuite.addTestSuite(TokenTest.class);
        mySuite.addTestSuite(DurationTest.class);
        mySuite.addTestSuite(MockRecordTest.class);
        mySuite.addTestSuite(WcharTest.class);
        mySuite.addTestSuite(AndroidParcelableTest.class);
        return mySuite;
    }

    public static void main(String[] args) throws Exception {
       NativeLibLoader.loadLibs();
       JUnitCore.main("com.dropbox.djinni.test.AllTests");
    }
}
