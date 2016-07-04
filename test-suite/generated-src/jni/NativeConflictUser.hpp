// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from test.djinni

#pragma once

#include "conflict_user.hpp"
#include "djinni/support-lib/jni/djinni_support.hpp"

namespace djinni_generated {

class NativeConflictUser final : ::djinni::JniInterface<::testsuite::ConflictUser, NativeConflictUser> {
public:
    using CppType = std::shared_ptr<::testsuite::ConflictUser>;
    using CppOptType = std::shared_ptr<::testsuite::ConflictUser>;
    using JniType = jobject;

    using Boxed = NativeConflictUser;

    ~NativeConflictUser();

    static CppType toCpp(JNIEnv* jniEnv, JniType j) { return ::djinni::JniClass<NativeConflictUser>::get()._fromJava(jniEnv, j); }
    static ::djinni::LocalRef<JniType> fromCppOpt(JNIEnv* jniEnv, const CppOptType& c) { return {jniEnv, ::djinni::JniClass<NativeConflictUser>::get()._toJava(jniEnv, c)}; }
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c) { return fromCppOpt(jniEnv, c); }

private:
    NativeConflictUser();
    friend ::djinni::JniClass<NativeConflictUser>;
    friend ::djinni::JniInterface<::testsuite::ConflictUser, NativeConflictUser>;

};

}  // namespace djinni_generated
