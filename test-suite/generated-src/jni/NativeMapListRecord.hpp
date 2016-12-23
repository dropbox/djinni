// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from map.djinni

#pragma once

#include "djinni/jni/djinni_support.hpp"
#include "map_list_record.hpp"

namespace djinni_generated {

class NativeMapListRecord final {
public:
    using CppType = ::testsuite::MapListRecord;
    using JniType = jobject;

    using Boxed = NativeMapListRecord;

    ~NativeMapListRecord();

    static CppType toCpp(JNIEnv* jniEnv, JniType j);
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c);

private:
    NativeMapListRecord();
    friend ::djinni::JniClass<NativeMapListRecord>;

    const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass("com/dropbox/djinni/test/MapListRecord") };
    const jmethodID jconstructor { ::djinni::jniGetMethodID(clazz.get(), "<init>", "(Ljava/util/ArrayList;)V") };
    const jfieldID field_mMapList { ::djinni::jniGetFieldID(clazz.get(), "mMapList", "Ljava/util/ArrayList;") };
};

}  // namespace djinni_generated
