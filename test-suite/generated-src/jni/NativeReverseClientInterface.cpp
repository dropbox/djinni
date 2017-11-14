// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#include "NativeReverseClientInterface.hpp"  // my header
#include "Marshal.hpp"

namespace djinni_generated {

NativeReverseClientInterface::NativeReverseClientInterface() : ::djinni::JniInterface<::testsuite::ReverseClientInterface, NativeReverseClientInterface>("com/dropbox/djinni/test/ReverseClientInterface$CppProxy") {}

NativeReverseClientInterface::~NativeReverseClientInterface() = default;


CJNIEXPORT void JNICALL Java_com_dropbox_djinni_test_ReverseClientInterface_00024CppProxy_nativeDestroy(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        delete reinterpret_cast<::djinni::CppProxyHandle<::testsuite::ReverseClientInterface>*>(nativeRef);
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, )
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_ReverseClientInterface_00024CppProxy_native_1returnStr(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::ReverseClientInterface>(nativeRef);
        auto r = ref->return_str();
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_ReverseClientInterface_00024CppProxy_native_1methTakingInterface(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef, jobject j_i)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::ReverseClientInterface>(nativeRef);
        auto r = ref->meth_taking_interface(::djinni_generated::NativeReverseClientInterface::toCpp(jniEnv, j_i));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_ReverseClientInterface_00024CppProxy_native_1methTakingOptionalInterface(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef, jobject j_i)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::ReverseClientInterface>(nativeRef);
        auto r = ref->meth_taking_optional_interface(::djinni::Optional<std::experimental::optional, ::djinni_generated::NativeReverseClientInterface>::toCpp(jniEnv, j_i));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jobject JNICALL Java_com_dropbox_djinni_test_ReverseClientInterface_00024StaticNativeMethods_create(JNIEnv* jniEnv, jobject /*this*/)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::ReverseClientInterface::create();
        return ::djinni::release(::djinni_generated::NativeReverseClientInterface::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

}  // namespace djinni_generated
