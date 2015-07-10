// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from date.djinni

#include "NativeDateRecord.hpp"  // my header
#include "Marshal.hpp"

namespace djinni_generated {

NativeDateRecord::NativeDateRecord() = default;

NativeDateRecord::~NativeDateRecord() = default;

auto NativeDateRecord::fromCpp(JNIEnv* jniEnv, const CppType& c) -> ::djinni::LocalRef<JniType> {
    const auto& data = ::djinni::JniClass<NativeDateRecord>::get();
    auto r = ::djinni::LocalRef<JniType>{jniEnv->NewObject(data.clazz.get(), data.jconstructor,
                                                           ::djinni::get(::djinni::Date::fromCpp(jniEnv, c.created_at)))};
    ::djinni::jniExceptionCheck(jniEnv);
    return r;
}

auto NativeDateRecord::toCpp(JNIEnv* jniEnv, JniType j) -> CppType {
    ::djinni::JniLocalScope jscope(jniEnv, 2);
    assert(j != nullptr);
    const auto& data = ::djinni::JniClass<NativeDateRecord>::get();
    return {::djinni::Date::toCpp(jniEnv, jniEnv->GetObjectField(j, data.field_mCreatedAt))};
}

}  // namespace djinni_generated
