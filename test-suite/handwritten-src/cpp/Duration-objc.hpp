#pragma once
#import <Foundation/Foundation.h>
#include <cassert>
#include <chrono>

namespace djinni
{
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
        using ObjcType = NSTimeInterval;

        static CppType toCpp(ObjcType dt)
        {
            return std::chrono::duration_cast<CppType>(std::chrono::duration<double>{dt});
        }
        static ObjcType fromCpp(CppType dt)
        {
            return std::chrono::duration_cast<std::chrono::duration<double>>(dt).count();
        }

        struct Boxed
        {
            using ObjcType = NSNumber*;
            static CppType toCpp(ObjcType dt)
            {
                assert(dt);
                return std::chrono::duration_cast<CppType>(Duration::toCpp([dt doubleValue]));
            }
            static ObjcType fromCpp(CppType dt)
            {
                return [NSNumber numberWithDouble:Duration::fromCpp(dt)];
            }
        };
    };
}
