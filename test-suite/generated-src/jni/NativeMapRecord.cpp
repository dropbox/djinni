// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from map.djinni

#include "NativeMapRecord.hpp"  // my header
#include "Marshal.hpp"

namespace djinni_generated { namespace jni {

NativeMapRecord::NativeMapRecord() = default;

NativeMapRecord::~NativeMapRecord() = default;

auto NativeMapRecord::fromCpp(JNIEnv* jniEnv, const CppType& c) -> ::djinni::LocalRef<JniType>
{
    const auto& data = ::djinni::JniClass<NativeMapRecord>::get();
    auto r = ::djinni::LocalRef<JniType>{jniEnv->NewObject(data.clazz.get(), data.jconstructor,
                                                           ::djinni::Map<::djinni::String, ::djinni::I64>::fromCpp(jniEnv, c.map).get())};
    ::djinni::jniExceptionCheck(jniEnv);
    return r;
}

auto NativeMapRecord::toCpp(JNIEnv* jniEnv, JniType j) -> CppType
{
    assert(j != nullptr);
    const auto& data = ::djinni::JniClass<NativeMapRecord>::get();
    return {::djinni::Map<::djinni::String, ::djinni::I64>::toCpp(jniEnv, jniEnv->GetObjectField(j, data.field_mMap))};
}

} }  // namespace djinni_generated::jni
