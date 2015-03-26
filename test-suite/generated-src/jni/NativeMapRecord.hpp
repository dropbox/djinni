// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from map.djinni

#pragma once

#include "djinni_support.hpp"
#include "map_record.hpp"

namespace djinni_generated { namespace jni {

class NativeMapRecord final
{
public:
    using CppType = ::djinni_generated::MapRecord;
    using JniType = jobject;

    using Boxed = NativeMapRecord;

    ~NativeMapRecord();

    static CppType toCpp(JNIEnv*, JniType);
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv*, const CppType&);

private:
    NativeMapRecord();
    friend ::djinni::JniClass<NativeMapRecord>;

    const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass("com/dropbox/djinni/test/MapRecord") };
    const jmethodID jconstructor { ::djinni::jniGetMethodID(clazz.get(), "<init>", "(Ljava/util/HashMap;)V") };
    const jfieldID field_mMap { ::djinni::jniGetFieldID(clazz.get(), "mMap", "Ljava/util/HashMap;") };
};

} }  // namespace djinni_generated::jni
