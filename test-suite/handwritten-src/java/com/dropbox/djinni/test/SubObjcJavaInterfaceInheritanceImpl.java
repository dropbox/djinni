package com.dropbox.djinni.test;

import javax.annotation.Nonnull;

public class SubObjcJavaInterfaceInheritanceImpl extends SubObjcJavaInterfaceInheritance {
    private BaseObjcJavaInterfaceInheritance superImpl = new BaseObjcJavaInterfaceInheritanceImpl();

    @Nonnull
    @Override
    public String baseMethod() {
        return superImpl.baseMethod();
    }

    @Nonnull
    @Override
    public String subMethod() {
        return InterfaceInheritanceConstant.SUB_METHOD_RETURN_VALUE;
    }

    @Nonnull
    @Override
    public String overrideMethod() {
        return InterfaceInheritanceConstant.SUB_OVERRIDE_METHOD_RETURN_VALUE;
    }
}
