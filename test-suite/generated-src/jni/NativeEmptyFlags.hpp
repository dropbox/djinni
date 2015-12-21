// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from enum.djinni

#pragma once

#include "djinni_support.hpp"
#include "empty_flags.hpp"

namespace djinni_generated {

class NativeEmptyFlags final : ::djinni::JniFlags {
public:
    using CppType = ::testsuite::empty_flags;
    using JniType = jobject;

    using Boxed = NativeEmptyFlags;

    static CppType toCpp(JNIEnv* jniEnv, JniType j) { return static_cast<CppType>(::djinni::JniClass<NativeEmptyFlags>::get().flags(jniEnv, j)); }
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, CppType c) { return ::djinni::JniClass<NativeEmptyFlags>::get().create(jniEnv, static_cast<unsigned>(c), 0); }

private:
    NativeEmptyFlags() : JniFlags("com/dropbox/djinni/test/EmptyFlags") {}
    friend ::djinni::JniClass<NativeEmptyFlags>;
};

}  // namespace djinni_generated
