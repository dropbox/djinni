// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from map.djinni

#include "NativeMapListRecord.hpp"  // my header
#include "djinni/support-lib/jni/Marshal.hpp"

namespace djinni_generated {

NativeMapListRecord::NativeMapListRecord() = default;

NativeMapListRecord::~NativeMapListRecord() = default;

auto NativeMapListRecord::fromCpp(JNIEnv* jniEnv, const CppType& c) -> ::djinni::LocalRef<JniType> {
    const auto& data = ::djinni::JniClass<NativeMapListRecord>::get();
    auto r = ::djinni::LocalRef<JniType>{jniEnv->NewObject(data.clazz.get(), data.jconstructor,
                                                           ::djinni::get(::djinni::List<::djinni::Map<::djinni::String, ::djinni::I64>>::fromCpp(jniEnv, c.map_list)))};
    ::djinni::jniExceptionCheck(jniEnv);
    return r;
}

auto NativeMapListRecord::toCpp(JNIEnv* jniEnv, JniType j) -> CppType {
    ::djinni::JniLocalScope jscope(jniEnv, 2);
    assert(j != nullptr);
    const auto& data = ::djinni::JniClass<NativeMapListRecord>::get();
    return {::djinni::List<::djinni::Map<::djinni::String, ::djinni::I64>>::toCpp(jniEnv, jniEnv->GetObjectField(j, data.field_mMapList))};
}

}  // namespace djinni_generated
