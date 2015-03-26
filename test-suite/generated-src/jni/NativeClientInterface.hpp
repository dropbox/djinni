// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#pragma once

#include "client_interface.hpp"
#include "djinni_support.hpp"

namespace djinni_generated { namespace jni {

class NativeClientInterface final : ::djinni::JniInterface<::djinni_generated::ClientInterface, NativeClientInterface>
{
public:
    using CppType = std::shared_ptr<::djinni_generated::ClientInterface>;
    using JniType = jobject;

    using Boxed = NativeClientInterface;

    ~NativeClientInterface();

    static CppType toCpp(JNIEnv* jniEnv, JniType j) { return ::djinni::JniClass<NativeClientInterface>::get()._fromJava(jniEnv, j); }
    static ::djinni::LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c) { return {jniEnv, ::djinni::JniClass<NativeClientInterface>::get()._toJava(jniEnv, c)}; }

private:
    NativeClientInterface();
    friend ::djinni::JniClass<NativeClientInterface>;
    friend ::djinni::JniInterface<::djinni_generated::ClientInterface, NativeClientInterface>;

    class JavaProxy final
    : ::djinni::JavaProxyCacheEntry
    , public ::djinni_generated::ClientInterface
    {
    public:
        JavaProxy(JniType j);
        ~JavaProxy();

        ::djinni_generated::ClientReturnedRecord get_record(int64_t record_id, const std::string & utf8string) override;

    private:
        using ::djinni::JavaProxyCacheEntry::getGlobalRef;
        friend ::djinni::JniInterface<::djinni_generated::ClientInterface, ::djinni_generated::jni::NativeClientInterface>;
        friend ::djinni::JavaProxyCache<JavaProxy>;
    };

    const ::djinni::GlobalRef<jclass> clazz { ::djinni::jniFindClass("com/dropbox/djinni/test/ClientInterface") };
    const jmethodID method_getRecord { ::djinni::jniGetMethodID(clazz.get(), "getRecord", "(JLjava/lang/String;)Lcom/dropbox/djinni/test/ClientReturnedRecord;") };
};

} }  // namespace djinni_generated::jni
