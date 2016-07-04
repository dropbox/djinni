// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

#pragma once

#include "constant_record.hpp"
#include "djinni/support-lib/jni/djinni_support.hpp"

namespace djinni_generated {

class NativeConstantRecord final {
public:
    using CppType = ::testsuite::ConstantRecord;
    using JniType = jobject;

    using Boxed = NativeConstantRecord;

    ~NativeConstantRecord();

    static CppType toCpp(JNIEnv* jniEnv, JniType j);
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c);

private:
    NativeConstantRecord();
    friend ::djinni::JniClass<NativeConstantRecord>;

    const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass("com/dropbox/djinni/test/ConstantRecord") };
    const jmethodID jconstructor { ::djinni::jniGetMethodID(clazz.get(), "<init>", "(ILjava/lang/String;)V") };
    const jfieldID field_mSomeInteger { ::djinni::jniGetFieldID(clazz.get(), "mSomeInteger", "I") };
    const jfieldID field_mSomeString { ::djinni::jniGetFieldID(clazz.get(), "mSomeString", "Ljava/lang/String;") };
};

}  // namespace djinni_generated
