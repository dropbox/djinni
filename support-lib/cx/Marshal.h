//
//  Marshal.h
//  Djinni
//
//  Created by D.E. Goodman-Wilson on 07.08.15.
//  Copyright (c) 2015 Slack Technologies, Inc. All rights reserved.
//

#pragma once
#include <chrono>
#include <cstdint>
#include <string>
#include <locale>
#include <codecvt>
#include <unordered_set>
#include <unordered_map>
#include <vector>
#include <cassert>

#include <collection.h>

namespace djinni {

	struct Bool {
		using CppType = bool;
		using CxType = bool;

		static CppType toCpp(CxType x) { return x ? true : false; }
		static CxType fromCpp(CppType x) { return x ? true : false; }

		struct Boxed {
			using CxType = Platform::Object^;
			static CppType toCpp(CxType x) { assert(x); return Bool::toCpp((bool)x); }
			static CxType fromCpp(CppType x) { CxType cx = Bool::fromCpp(x); return cx; }
		};
	};

	template<class Self, class T> //, class CXT>
	struct Primitive {
		using CppType = T;
		using CxType = T;

		static CppType toCpp(CxType x) { return x; }
		static CxType fromCpp(CppType x) { return x; }

		struct Boxed {
			using CxType = Platform::Object^;
			static CppType toCpp(CxType x) { assert(x); return static_cast<CppType>(Self::unbox(x)); }
			static CxType fromCpp(CppType x) { return Self::box(x); }
		};
	};

	//stupid C++/Cx doesn't have int8_t; we'll pass it up as a uint8_t instead, and pray.
	class I8 : public Primitive<I8, int8_t> {
		friend Primitive<I8, int8_t>;
		static int8_t unbox(Boxed::CxType x)  { return safe_cast<uint8_t>(x); }
		static Boxed::CxType box(CppType x)  { Platform::Object^ cx = (uint8_t)x;  return cx; }
	};

	class I16 : public Primitive<I16, int16_t> {
		friend Primitive<I16, int16_t>;
		static uint16_t unbox(Boxed::CxType x)  { return safe_cast<int16_t>(x); }
		static Boxed::CxType box(CppType x)  { Platform::Object^ cx = x; return cx; }
	};

	class I32 : public Primitive<I32, int32_t> {
		friend Primitive<I32, int32_t>;
		static int32_t unbox(Boxed::CxType x)  { return safe_cast<int32_t>(x); }
		static Boxed::CxType box(CppType x)  { Platform::Object^ cx = x; return cx; }
	};

	class I64 : public Primitive<I64, int64_t> {
		friend Primitive<I64, int64_t>;
		static int64_t unbox(Boxed::CxType x)  { return safe_cast<int64_t>(x); }
		static Boxed::CxType box(CppType x)  { Platform::Object^ cx = x; return cx; }
	};

	class F32 : public Primitive<F32, float> {
		friend Primitive<F32, float>;
		static float unbox(Boxed::CxType x)  { return safe_cast<float>(x); }
		static Boxed::CxType box(CppType x)  { Platform::Object^ cx = x; return cx; }
	};

	class F64 : public Primitive<F64, double> {
		friend Primitive<F64, float>;
		static double unbox(Boxed::CxType x)  { return safe_cast<double>(x); }
		static Boxed::CxType box(CppType x)  { Platform::Object^ cx = x; return cx; }
	};

	//
	//    template<class CppEnum, class CxEnum>
	//    struct Enum {
	//        using CppType = CppEnum;
	//        using CxType = CxEnum;
	//
	//        static CppType toCpp(CxType e) noexcept { return static_cast<CppType>(e); }
	//        static CxType fromCpp(CppType e) noexcept { return static_cast<CxType>(e); }
	//
	//        struct Boxed {
	//            using CxType = NSNumber*;
	//            static CppType toCpp(CxType x) noexcept { return Enum::toCpp(static_cast<Enum::CxType>([x integerValue])); }
	//            static CxType fromCpp(CppType x) noexcept { return [NSNumber numberWithInteger:static_cast<NSInteger>(Enum::fromCpp(x))]; }
	//        };
	//    };
	//
	struct String {
		using CppType = std::string;
		using CxType = Platform::String;

		using Boxed = String;

		static CppType toCpp(CxType^ string) {
			assert(string);
			std::wstring wstring{ string->Data() };
			std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;
			return converter.to_bytes(wstring);
		}

		static CxType^ fromCpp(const CppType& string) {
			//TODO this doesn't seem to work. assert(string.size() <= std::numeric_limits<int64_t>::max());
			std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;

			return ref new Platform::String(converter.from_bytes(string).c_str());
		}
	};
	//
	//    struct Date {
	//        using CppType = std::chrono::system_clock::time_point;
	//        using CxType = NSDate*;
	//
	//        using Boxed = Date;
	//
	//        static CppType toCpp(CxType date) {
	//            using namespace std::chrono;
	//            static const auto POSIX_EPOCH = system_clock::from_time_t(0);
	//            auto timeIntervalSince1970 = duration<double>([date timeIntervalSince1970]);
	//            return POSIX_EPOCH + duration_cast<system_clock::duration>(timeIntervalSince1970);
	//        }
	//
	//        static CxType fromCpp(const CppType& date) {
	//            using namespace std::chrono;
	//            static const auto POSIX_EPOCH = system_clock::from_time_t(0);
	//            return [NSDate dateWithTimeIntervalSince1970:duration_cast<duration<double>>(date - POSIX_EPOCH).count()];
	//
	//        }
	//    };
	//
	//    struct Binary {
	//        using CppType = std::vector<uint8_t>;
	//        using CxType = NSData*;
	//
	//        using Boxed = Binary;
	//
	//        static CppType toCpp(CxType data) {
	//            assert(data);
	//            auto bytes = reinterpret_cast<const uint8_t*>(data.bytes);
	//            return data.length > 0 ? CppType{bytes, bytes + data.length} : CppType{};
	//        }
	//
	//        static CxType fromCpp(const CppType& bytes) {
	//            assert(bytes.size() <= std::numeric_limits<NSUInteger>::max());
	//            // Using the pointer from .data() on an empty vector is UB
	//            return bytes.empty() ? [NSData data] : [NSData dataWithBytes:bytes.data()
	//            length:static_cast<NSUInteger>(bytes.size())];
	//        }
	//    };
	//
	//    template<template<class> class OptionalType, class T>
	//    class Optional {
	//    public:
	//        using CppType = OptionalType<typename T::CppType>;
	//        using CxType = typename T::Boxed::CxType;
	//
	//        using Boxed = Optional;
	//
	//        static CppType toCpp(CxType obj) {
	//            return obj ? CppType(T::Boxed::toCpp(obj)) : CppType();
	//        }
	//
	//        static CxType fromCpp(const CppType& opt) {
	//            return opt ? T::Boxed::fromCpp(*opt) : nil;
	//        }
	//    };
	//
       template<class T>
       class List {
           using ECppType = typename T::CppType;
           using ECxType = typename T::Boxed::CxType;

       public:
           using CppType = std::vector<ECppType>;
		   using CxType = Windows::Foundation::Collections::IVector<ECxType^>;

           using Boxed = List;

           static CppType toCpp(CxType^ v) {
               assert(v);
               std::vector<int> nv;
               for(int val : v)
               {
                   nv.push_back(val);
               }
               return nv;
           }

           static CxType^ fromCpp(const CppType& v) {
               return ref new Platform::Collections::Vector<int>(std::move(v));
           }
       };
	//
	//    template<class T>
	//    class Set {
	//        using ECppType = typename T::CppType;
	//        using ECxType = typename T::Boxed::CxType;
	//
	//    public:
	//        using CppType = std::unordered_set<ECppType>;
	//        using CxType = NSSet*;
	//
	//        using Boxed = Set;
	//
	//        static CppType toCpp(CxType set) {
	//            assert(set);
	//            auto s = CppType();
	//            for(ECxType value in set) {
	//                s.insert(T::Boxed::toCpp(value));
	//            }
	//            return s;
	//        }
	//
	//        static CxType fromCpp(const CppType& s) {
	//            assert(s.size() <= std::numeric_limits<NSUInteger>::max());
	//            auto set = [NSMutableSet setWithCapacity:static_cast<NSUInteger>(s.size())];
	//            for(const auto& value : s) {
	//                [set addObject:T::Boxed::fromCpp(value)];
	//            }
	//            return set;
	//        }
	//    };
	//
	//    template<class Key, class Value>
	//    class Map {
	//        using CppKeyType = typename Key::CppType;
	//        using CppValueType = typename Value::CppType;
	//        using CxKeyType = typename Key::Boxed::CxType;
	//        using CxValueType = typename Value::Boxed::CxType;
	//
	//    public:
	//        using CppType = std::unordered_map<CppKeyType, CppValueType>;
	//        using CxType = NSDictionary*;
	//
	//        using Boxed = Map;
	//
	//        static CppType toCpp(CxType map) {
	//            assert(map);
	//            __block auto m = CppType();
	//            m.reserve(map.count);
	//            [map enumerateKeysAndObjectsUsingBlock:^(CxKeyType key, CxValueType obj, BOOL *) {
	//                m.emplace(Key::Boxed::toCpp(key), Value::Boxed::toCpp(obj));
	//            }];
	//            return m;
	//        }
	//
	//        static CxType fromCpp(const CppType& m) {
	//            assert(m.size() <= std::numeric_limits<NSUInteger>::max());
	//            auto map = [NSMutableDictionary dictionaryWithCapacity:static_cast<NSUInteger>(m.size())];
	//            for(const auto& kvp : m) {
	//                [map setObject:Value::Boxed::fromCpp(kvp.second) forKey:Key::Boxed::fromCpp(kvp.first)];
	//            }
	//            return map;
	//        }
	//    };
	//
} // namespace djinni
