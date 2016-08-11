package com.dropbox.djinni.test;

import javax.annotation.Nonnull;

public class BaseObjcJavaInterfaceInheritanceImpl extends BaseObjcJavaInterfaceInheritance {
    @Nonnull
    @Override
    public String baseMethod() {
        return InterfaceInheritanceConstant.BASE_METHOD_RETURN_VALUE;
    }

    @Nonnull
    @Override
    public String overrideMethod() {
        return InterfaceInheritanceConstant.BASE_OVERRIDE_METHOD_RETURN_VALUE;
    }
}
