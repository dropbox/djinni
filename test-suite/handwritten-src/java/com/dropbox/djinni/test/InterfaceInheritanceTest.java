package com.dropbox.djinni.test;

import junit.framework.TestCase;

public class InterfaceInheritanceTest extends TestCase {
    public void testCppBaseClass() {
        BaseCppInterfaceInheritance base = BaseCppInterfaceInheritance.create();

        assertNotNull(base);
        assertTrue(base instanceof BaseCppInterfaceInheritance);

        assertTrue(base.baseMethod().equals(InterfaceInheritanceConstant.BASE_METHOD_RETURN_VALUE));
        assertTrue(base.overrideMethod().equals(InterfaceInheritanceConstant.BASE_OVERRIDE_METHOD_RETURN_VALUE));
    }

    public void testCppSubClassInheritance() {
        SubCppInterfaceInheritance sub = SubCppInterfaceInheritance.create();

        assertNotNull(sub);
        assertTrue(sub instanceof SubCppInterfaceInheritance);
        assertTrue(sub instanceof BaseCppInterfaceInheritance);

        assertTrue(sub.baseMethod().equals(InterfaceInheritanceConstant.BASE_METHOD_RETURN_VALUE));
        assertTrue(sub.overrideMethod().equals(InterfaceInheritanceConstant.SUB_OVERRIDE_METHOD_RETURN_VALUE));
        assertTrue(sub.subMethod().equals(InterfaceInheritanceConstant.SUB_METHOD_RETURN_VALUE));

        BaseCppInterfaceInheritance subAsBase = (BaseCppInterfaceInheritance)sub;
        assertTrue(subAsBase.baseMethod().equals(InterfaceInheritanceConstant.BASE_METHOD_RETURN_VALUE));
        assertTrue(subAsBase.overrideMethod().equals(InterfaceInheritanceConstant.SUB_OVERRIDE_METHOD_RETURN_VALUE));
    }

    public void testCppSubClassEncapsulation() {
        InterfaceEncapsulator encapsulator = InterfaceEncapsulator.create();

        BaseCppInterfaceInheritance base = BaseCppInterfaceInheritance.create();
        encapsulator.setCppObject(base);
        assertTrue(encapsulator.getCppObject() instanceof BaseCppInterfaceInheritance);

        SubCppInterfaceInheritance sub = SubCppInterfaceInheritance.create();
        encapsulator.setCppObject(sub);
        assertTrue(encapsulator.getCppObject() instanceof SubCppInterfaceInheritance);
        assertTrue(encapsulator.subCppAsBaseCpp() instanceof SubCppInterfaceInheritance);
    }

    public void testJavaBaseClass() {
        BaseObjcJavaInterfaceInheritance base = new BaseObjcJavaInterfaceInheritanceImpl();

        assertNotNull(base);
        assertTrue(base instanceof BaseObjcJavaInterfaceInheritance);

        assertTrue(base.baseMethod().equals(InterfaceInheritanceConstant.BASE_METHOD_RETURN_VALUE));
        assertTrue(base.overrideMethod().equals(InterfaceInheritanceConstant.BASE_OVERRIDE_METHOD_RETURN_VALUE));
    }

    public void testJavaSubClassInheritance() {
        SubObjcJavaInterfaceInheritance sub = new SubObjcJavaInterfaceInheritanceImpl();

        assertNotNull(sub);
        assertTrue(sub instanceof SubObjcJavaInterfaceInheritance);
        assertTrue(sub instanceof BaseObjcJavaInterfaceInheritance);

        assertTrue(sub.baseMethod().equals(InterfaceInheritanceConstant.BASE_METHOD_RETURN_VALUE));
        assertTrue(sub.overrideMethod().equals(InterfaceInheritanceConstant.SUB_OVERRIDE_METHOD_RETURN_VALUE));
        assertTrue(sub.subMethod().equals(InterfaceInheritanceConstant.SUB_METHOD_RETURN_VALUE));
    }

    public void testJavaSubClassEncapsulation() {
        InterfaceEncapsulator encapsulator = InterfaceEncapsulator.create();

        BaseObjcJavaInterfaceInheritance base = new BaseObjcJavaInterfaceInheritanceImpl();
        encapsulator.setObjcJavaObject(base);

        BaseObjcJavaInterfaceInheritance encappedBase = encapsulator.getObjcJavaObject();
        assertTrue(encappedBase instanceof BaseObjcJavaInterfaceInheritance);
        assertTrue(encappedBase instanceof BaseObjcJavaInterfaceInheritanceImpl);

        SubObjcJavaInterfaceInheritance sub = new SubObjcJavaInterfaceInheritanceImpl();
        encapsulator.setObjcJavaObject(sub);

        BaseObjcJavaInterfaceInheritance encappedSub = encapsulator.getObjcJavaObject();
        assertTrue(encappedSub instanceof BaseObjcJavaInterfaceInheritance);
        assertTrue(encappedSub instanceof SubObjcJavaInterfaceInheritance);
        assertTrue(encappedSub instanceof SubObjcJavaInterfaceInheritanceImpl);
        assertFalse(encappedSub instanceof BaseObjcJavaInterfaceInheritanceImpl);
    }

    public void testJavaSubClassCasting() {
        InterfaceEncapsulator encapsulator = InterfaceEncapsulator.create();

        BaseObjcJavaInterfaceInheritance base = new BaseObjcJavaInterfaceInheritanceImpl();
        Object castBase = encapsulator.castBaseArgToSub(base);
        assertNull(castBase);

        // FIXME: This test will fail. When castBaseArgToSub is called, a C++ object will be created to
        //        represent the Java object. Since castBaseArgToSub takes a DBBaseObjJavaInterfaceInheritance
        //        argument, the C++ object that is created will be BaseObjcJavaInterfaceInheritance object,
        //        slicing off all the additional members of the subtype and making it impossible to cast
        //        back to the provided subtype.
        // SubObjcJavaInterfaceInheritance sub = new SubObjcJavaInterfaceInheritanceImpl();
        // Object castSub = encapsulator.castBaseArgToSub(sub);
        // assertNotNull(castSub);
        // assertTrue(castSub instanceof SubObjcJavaInterfaceInheritance);
    }
}