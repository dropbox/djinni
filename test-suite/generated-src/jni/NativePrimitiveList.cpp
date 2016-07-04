// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from primitive_list.djinni

#include "NativePrimitiveList.hpp"  // my header
#include "djinni/support-lib/jni/Marshal.hpp"

namespace djinni_generated {

NativePrimitiveList::NativePrimitiveList() = default;

NativePrimitiveList::~NativePrimitiveList() = default;

auto NativePrimitiveList::fromCpp(JNIEnv* jniEnv, const CppType& c) -> ::djinni::LocalRef<JniType> {
    const auto& data = ::djinni::JniClass<NativePrimitiveList>::get();
    auto r = ::djinni::LocalRef<JniType>{jniEnv->NewObject(data.clazz.get(), data.jconstructor,
                                                           ::djinni::get(::djinni::List<::djinni::I64>::fromCpp(jniEnv, c.list)))};
    ::djinni::jniExceptionCheck(jniEnv);
    return r;
}

auto NativePrimitiveList::toCpp(JNIEnv* jniEnv, JniType j) -> CppType {
    ::djinni::JniLocalScope jscope(jniEnv, 2);
    assert(j != nullptr);
    const auto& data = ::djinni::JniClass<NativePrimitiveList>::get();
    return {::djinni::List<::djinni::I64>::toCpp(jniEnv, jniEnv->GetObjectField(j, data.field_mList))};
}

}  // namespace djinni_generated
