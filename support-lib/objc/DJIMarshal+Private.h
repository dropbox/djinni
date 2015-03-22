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
#include <string>
#include <vector>

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

template<class CppEnum, class ObjcEnum>
struct Enum {
    using CppType = CppEnum;
    using ObjcType = ObjcEnum;

    static CppType toCpp(ObjcType e) noexcept { return static_cast<CppType>(e); }
    static ObjcType fromCpp(CppType e) noexcept { return static_cast<ObjcType>(e); }

    struct Boxed {
        using ObjcType = NSNumber*;
        static CppType toCpp(ObjcType x) noexcept { return Enum::toCpp(static_cast<Enum::ObjcType>([x integerValue])); }
        static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithInteger:static_cast<NSInteger>(Enum::fromCpp(x))]; }
    };
};

struct String {
    using CppType = std::string;
    using ObjcType = NSString*;

    using Boxed = String;

    static CppType toCpp(ObjcType string) {
        return {[string UTF8String], [string lengthOfBytesUsingEncoding:NSUTF8StringEncoding]};
    }

    static ObjcType fromCpp(const CppType& string) {
        assert(string.size() <= std::numeric_limits<NSUInteger>::max());
        // Using the pointer from .data() on an empty string is UB
        return string.empty() ? @"" : [[NSString alloc] initWithBytes:string.data() length:string.size() encoding:NSUTF8StringEncoding];
    }
};

struct Binary {
    using CppType = std::vector<uint8_t>;
    using ObjcType = NSData*;

    using Boxed = Binary;

    static CppType toCpp(ObjcType data) {
        auto bytes = reinterpret_cast<const uint8_t*>(data.bytes);
        return data.length > 0 ? CppType{bytes, bytes + data.length} : CppType{};
    }

    static ObjcType fromCpp(const CppType& bytes) {
        assert(bytes.size() <= std::numeric_limits<NSUInteger>::max());
        // Using the pointer from .data() on an empty vector is UB
        return bytes.empty() ? [NSData data] : [NSData dataWithBytes:bytes.data() length:bytes.size()];
    }
};

} // namespace djinni
