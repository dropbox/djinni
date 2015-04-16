// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

#pragma once

#include "constants.hpp"
#include "djinni_support.hpp"

namespace djinni_generated { namespace jni {

class NativeConstants final
{
public:
    using CppType = ::djinni_generated::Constants;
    using JniType = jobject;

    using Boxed = NativeConstants;

    ~NativeConstants();

    static CppType toCpp(JNIEnv*, JniType);
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv*, const CppType&);

private:
    NativeConstants();
    friend ::djinni::JniClass<NativeConstants>;

    const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass("com/dropbox/djinni/test/Constants") };
    const jmethodID jconstructor { ::djinni::jniGetMethodID(clazz.get(), "<init>", "(ILjava/lang/String;)V") };
    const jfieldID field_mSomeInteger { ::djinni::jniGetFieldID(clazz.get(), "mSomeInteger", "I") };
    const jfieldID field_mSomeString { ::djinni::jniGetFieldID(clazz.get(), "mSomeString", "Ljava/lang/String;") };
};

} }  // namespace djinni_generated::jni
