// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from derivings.djinni

#include "NativeRecordWithDerivings.hpp"  // my header
#include "HI32.hpp"
#include "HString.hpp"

namespace djinni { namespace jni {

jobject NativeRecordWithDerivings::toJava(JNIEnv* jniEnv, ::djinni::cpp::RecordWithDerivings c) {
    jint j_key1 = ::djinni::HI32::Unboxed::toJava(jniEnv, c.key1);
    djinni::LocalRef<jstring> j_key2(jniEnv, ::djinni::HString::toJava(jniEnv, c.key2));
    const auto & data = djinni::JniClass<::djinni::jni::NativeRecordWithDerivings>::get();
    jobject r = jniEnv->NewObject(data.clazz.get(), data.jconstructor, j_key1, j_key2.get());
    djinni::jniExceptionCheck(jniEnv);
    return r;
}

::djinni::cpp::RecordWithDerivings NativeRecordWithDerivings::fromJava(JNIEnv* jniEnv, jobject j) {
    assert(j != nullptr);
    const auto & data = djinni::JniClass<::djinni::jni::NativeRecordWithDerivings>::get();
    return ::djinni::cpp::RecordWithDerivings(
        ::djinni::HI32::Unboxed::fromJava(jniEnv, jniEnv->GetIntField(j, data.field_mKey1)),
        ::djinni::HString::fromJava(jniEnv, djinni::LocalRef<jstring>(jniEnv, static_cast<jstring>(jniEnv->GetObjectField(j, data.field_mKey2))).get()));
}

} }  // namespace djinni::jni
