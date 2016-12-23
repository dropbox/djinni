// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from duration.djinni

#include "NativeTestDuration.hpp"  // my header
#include "Duration-jni.hpp"
#include "djinni/jni/Marshal.hpp"

namespace djinni_generated {

NativeTestDuration::NativeTestDuration() : ::djinni::JniInterface<::testsuite::TestDuration, NativeTestDuration>("com/dropbox/djinni/test/TestDuration$CppProxy") {}

NativeTestDuration::~NativeTestDuration() = default;


CJNIEXPORT void JNICALL Java_com_dropbox_djinni_test_TestDuration_00024CppProxy_nativeDestroy(JNIEnv* jniEnv, jobject /*this*/, jlong nativeRef)
{
    try {
        DJINNI_FUNCTION_PROLOGUE1(jniEnv, nativeRef);
        delete reinterpret_cast<::djinni::CppProxyHandle<::testsuite::TestDuration>*>(nativeRef);
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, )
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_TestDuration_hoursString(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I32, ::djinni::Duration_h>::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::hoursString(::djinni::Duration<::djinni::I32, ::djinni::Duration_h>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_TestDuration_minutesString(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I32, ::djinni::Duration_min>::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::minutesString(::djinni::Duration<::djinni::I32, ::djinni::Duration_min>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_TestDuration_secondsString(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I32, ::djinni::Duration_s>::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::secondsString(::djinni::Duration<::djinni::I32, ::djinni::Duration_s>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_TestDuration_millisString(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I32, ::djinni::Duration_ms>::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::millisString(::djinni::Duration<::djinni::I32, ::djinni::Duration_ms>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_TestDuration_microsString(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I32, ::djinni::Duration_us>::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::microsString(::djinni::Duration<::djinni::I32, ::djinni::Duration_us>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jstring JNICALL Java_com_dropbox_djinni_test_TestDuration_nanosString(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I32, ::djinni::Duration_ns>::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::nanosString(::djinni::Duration<::djinni::I32, ::djinni::Duration_ns>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::String::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I32, ::djinni::Duration_h>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_hours(JNIEnv* jniEnv, jobject /*this*/, jint j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::hours(::djinni::I32::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::I32, ::djinni::Duration_h>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I32, ::djinni::Duration_min>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_minutes(JNIEnv* jniEnv, jobject /*this*/, jint j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::minutes(::djinni::I32::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::I32, ::djinni::Duration_min>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I32, ::djinni::Duration_s>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_seconds(JNIEnv* jniEnv, jobject /*this*/, jint j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::seconds(::djinni::I32::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::I32, ::djinni::Duration_s>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I32, ::djinni::Duration_ms>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_millis(JNIEnv* jniEnv, jobject /*this*/, jint j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::millis(::djinni::I32::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::I32, ::djinni::Duration_ms>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I32, ::djinni::Duration_us>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_micros(JNIEnv* jniEnv, jobject /*this*/, jint j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::micros(::djinni::I32::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::I32, ::djinni::Duration_us>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I32, ::djinni::Duration_ns>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_nanos(JNIEnv* jniEnv, jobject /*this*/, jint j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::nanos(::djinni::I32::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::I32, ::djinni::Duration_ns>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::F64, ::djinni::Duration_h>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_hoursf(JNIEnv* jniEnv, jobject /*this*/, jdouble j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::hoursf(::djinni::F64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::F64, ::djinni::Duration_h>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::F64, ::djinni::Duration_min>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_minutesf(JNIEnv* jniEnv, jobject /*this*/, jdouble j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::minutesf(::djinni::F64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::F64, ::djinni::Duration_min>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::F64, ::djinni::Duration_s>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_secondsf(JNIEnv* jniEnv, jobject /*this*/, jdouble j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::secondsf(::djinni::F64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::F64, ::djinni::Duration_s>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::F64, ::djinni::Duration_ms>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_millisf(JNIEnv* jniEnv, jobject /*this*/, jdouble j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::millisf(::djinni::F64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::F64, ::djinni::Duration_ms>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::F64, ::djinni::Duration_us>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_microsf(JNIEnv* jniEnv, jobject /*this*/, jdouble j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::microsf(::djinni::F64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::F64, ::djinni::Duration_us>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::F64, ::djinni::Duration_ns>::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_nanosf(JNIEnv* jniEnv, jobject /*this*/, jdouble j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::nanosf(::djinni::F64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Duration<::djinni::F64, ::djinni::Duration_ns>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT ::djinni::Duration<::djinni::I64, ::djinni::Duration_s>::Boxed::JniType JNICALL Java_com_dropbox_djinni_test_TestDuration_box(JNIEnv* jniEnv, jobject /*this*/, jlong j_count)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::box(::djinni::I64::toCpp(jniEnv, j_count));
        return ::djinni::release(::djinni::Optional<std::experimental::optional, ::djinni::Duration<::djinni::I64, ::djinni::Duration_s>>::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

CJNIEXPORT jlong JNICALL Java_com_dropbox_djinni_test_TestDuration_unbox(JNIEnv* jniEnv, jobject /*this*/, ::djinni::Duration<::djinni::I64, ::djinni::Duration_s>::Boxed::JniType j_dt)
{
    try {
        DJINNI_FUNCTION_PROLOGUE0(jniEnv);
        auto r = ::testsuite::TestDuration::unbox(::djinni::Optional<std::experimental::optional, ::djinni::Duration<::djinni::I64, ::djinni::Duration_s>>::toCpp(jniEnv, j_dt));
        return ::djinni::release(::djinni::I64::fromCpp(jniEnv, r));
    } JNI_TRANSLATE_EXCEPTIONS_RETURN(jniEnv, 0 /* value doesn't matter */)
}

}  // namespace djinni_generated
