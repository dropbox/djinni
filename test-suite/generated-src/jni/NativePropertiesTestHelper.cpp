// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from properties.djinni

#include "NativePropertiesTestHelper.hpp"  // my header
#include "Marshal.hpp"

namespace djinni_generated {

NativePropertiesTestHelper::NativePropertiesTestHelper() : ::djinni::JniInterface<::testsuite::PropertiesTestHelper, NativePropertiesTestHelper>("com/dropbox/djinni/test/PropertiesTestHelper$CppProxy") {}

NativePropertiesTestHelper::~NativePropertiesTestHelper() = default;


CJNIEXPORT void JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_nativeDestroy(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        delete reinterpret_cast<::djinni::CppProxyHandle<::testsuite::PropertiesTestHelper>*>(nativeRef);
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, )
}

CJNIEXPORT jobject JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_createNew(JNIEnv* jniEnv, jobject /*this*/)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::PropertiesTestHelper::create_new();
        return ::djinni::release(::djinni_generated::NativePropertiesTestHelper::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jint JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1getItem(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        auto r = ref->get_item();
        return ::djinni::release(::djinni::I32::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT void JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1setItem(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef, jint j_item)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        ref->set_item(::djinni::I32::toCpp(jniEnv, j_item));
        ;
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, )
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1getTestString(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        auto r = ref->get_test_string();
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT void JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1setTestString(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef, jstring j_testString)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        ref->set_test_string(::djinni::String::toCpp(jniEnv, j_testString));
        ;
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, )
}

CJNIEXPORT jobject JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1getTestList(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        auto r = ref->get_test_list();
        return ::djinni::release(::djinni::List<::djinni::I32>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT void JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1setTestList(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef, jobject j_testList)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        ref->set_test_list(::djinni::List<::djinni::I32>::toCpp(jniEnv, j_testList));
        ;
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, )
}

CJNIEXPORT jboolean JNICALL Java_com_dropbox_djinni_test_PropertiesTestHelper_00024CppProxy_native_1getReadOnlyBool(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        const auto& ref = ::djinni::objectFromHandleAddress<::testsuite::PropertiesTestHelper>(nativeRef);
        auto r = ref->get_read_only_bool();
        return ::djinni::release(::djinni::Bool::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

}  // namespace djinni_generated
