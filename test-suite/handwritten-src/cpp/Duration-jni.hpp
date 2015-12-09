#pragma once
#include "djinni_support.hpp"
#include <cassert>
#include <chrono>

namespace djinni
{
    struct DurationJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/time/Duration") };

        const jmethodID method_ofNanos { jniGetStaticMethodID(clazz.get(), "ofNanos", "(J)Ljava/time/Duration;") };
        const jmethodID method_toNanos { jniGetMethodID(clazz.get(), "toNanos", "()J") };
    };

    // This is only a helper, trying to use it as member/param will fail
    template<class Ratio>
    struct DurationPeriod;

    using Duration_h   = DurationPeriod<std::ratio<3600>>;
    using Duration_min = DurationPeriod<std::ratio<60>>;
    using Duration_s   = DurationPeriod<std::ratio<1>>;
    using Duration_ms  = DurationPeriod<std::milli>;
    using Duration_us  = DurationPeriod<std::micro>;
    using Duration_ns  = DurationPeriod<std::nano>;

    template<class Rep, class Period>
    struct Duration;

    template<class Rep, class Ratio>
    struct Duration<Rep, DurationPeriod<Ratio>>
    {
        using CppType = std::chrono::duration<typename Rep::CppType, Ratio>;
        using JniType = jobject;

        using Boxed = Duration;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);
            const auto& data = JniClass<DurationJniInfo>::get();
            assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
            jlong nanos = jniEnv->CallLongMethod(j, data.method_toNanos);
            jniExceptionCheck(jniEnv);
            return std::chrono::duration_cast<CppType>(std::chrono::duration<jlong, std::nano>{nanos});
        }
        static LocalRef<jobject> fromCpp(JNIEnv* jniEnv, CppType c)
        {
            const auto& data = JniClass<DurationJniInfo>::get();
            jlong nanos = std::chrono::duration_cast<std::chrono::duration<jlong, std::nano>>(c).count();
            auto j = LocalRef<JniType>{jniEnv->CallStaticObjectMethod(data.clazz.get(), data.method_ofNanos, nanos)};
            jniExceptionCheck(jniEnv);
            return j;
        }
    };
}
