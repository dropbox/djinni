//
//  DJIMarshal+Private.h
//  Djinni
//
//  Created by knejp on 20.3.15.
//  Copyright (c) 2015 Dropbox. All rights reserved.
//

#pragma once
#import <Foundation/Foundation.h>
#include <cstdint>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

namespace djinni {

struct Bool {
    using CppType = bool;
    using ObjcType = BOOL;

    static CppType toCpp(ObjcType x) noexcept { return x ? true : false; }
    static ObjcType fromCpp(CppType x) noexcept { return x ? YES : NO; }

    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return [x boolValue] ? true : false; }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithChar:x ? YES : NO]; }
    };
};

template<class T>
struct Primitive {
    using CppType = T;
    using ObjcType = T;

    static CppType toCpp(ObjcType x) noexcept { return x; }
    static ObjcType fromCpp(CppType x) noexcept { return x; }
};

struct I8 : public Primitive<int8_t> {
    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return [x charValue]; }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithChar:x]; }
    };
};

struct I16 : public Primitive<int16_t> {
    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return [x shortValue]; }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithShort:x]; }
    };
};

struct I32 : public Primitive<int32_t> {
    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return [x intValue]; }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithInt:x]; }
    };
};

struct I64 : public Primitive<int64_t> {
    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return [x longLongValue]; }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithLongLong:x]; }
    };
};

struct F64 : public Primitive<double> {
    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return [x doubleValue]; }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithDouble:x]; }
    };
};

} // namespace djinni
