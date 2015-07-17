package com.dropbox.djinni.test;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        return mySuite;
    }

    static {
        System.loadLibrary("DjinniTestNative");
    }

}
