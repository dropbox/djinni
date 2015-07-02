// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from derivings.djinni

#include "NativeRecordWithNestedDerivings.hpp"  // my header
#include "Marshal.hpp"
#include "NativeRecordWithDerivings.hpp"

namespace djinni_generated {

NativeRecordWithNestedDerivings::NativeRecordWithNestedDerivings() = default;

NativeRecordWithNestedDerivings::~NativeRecordWithNestedDerivings() = default;

auto NativeRecordWithNestedDerivings::fromCpp(JNIEnv* jniEnv, const CppType& c) -> ::djinni::LocalRef<JniType> {
    const auto& data = ::djinni::JniClass<NativeRecordWithNestedDerivings>::get();
    auto r = ::djinni::LocalRef<JniType>{jniEnv->NewObject(data.clazz.get(), data.jconstructor,
                                                           ::djinni::get(::djinni::I32::fromCpp(jniEnv, c.key)),
                                                           ::djinni::get(::djinni_generated::NativeRecordWithDerivings::fromCpp(jniEnv, c.rec)))};
    ::djinni::jniExceptionCheck(jniEnv);
    return r;
}

auto NativeRecordWithNestedDerivings::toCpp(JNIEnv* jniEnv, JniType j) -> CppType {
    ::djinni::JniLocalScope jscope(jniEnv, 3);
    assert(j != nullptr);
    const auto& data = ::djinni::JniClass<NativeRecordWithNestedDerivings>::get();
    return {::djinni::I32::toCpp(jniEnv, jniEnv->GetIntField(j, data.field_mKey)),
            ::djinni_generated::NativeRecordWithDerivings::toCpp(jniEnv, jniEnv->GetObjectField(j, data.field_mRec))};
}

}  // namespace djinni_generated
