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


//	template<class Cppo, class CxEnum>
//	struct Enum {
//	    using CppType = CppEnum;
//	    using CxType = CxEnum;
//
//	    static CppType toCpp(CxType e) { return static_cast<CppType>(e); }
//	    static CxType fromCpp(CppType e) { return static_cast<CxType>(e); }
//
//	    struct Boxed {
//	    //yeah, I just don't know about these two lines. So...weird? How _do_ you box an enum?
//	        static CppType toCpp(CxType x) { return Enum::toCpp(static_cast<Enum::CxType>(static_cast<int64_t>(x))); }
//	        static CxType fromCpp(CppType x) { return Enum::toCx(static_cast<Enum::CppType>(static_cast<int64_t>(x))); }
//	    };
//	};

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

//	    struct Date {
//	        using CppType = std::chrono::system_clock::time_point;
//	        using CxType = Windows::Foundation::DateTime;
//
//	        using Boxed = Date;
//
//	        static CppType toCpp(CxType date) {
//	        	// date is "A 64-bit signed integer that represents a point in time as the number of 100-nanosecond
//	        	// intervals prior to or after midnight on January 1, 1601 (according to the Gregorian Calendar)."
//	        	// So helpful
//	            using namespace std::chrono;
//	            static const auto POSIX_EPOCH = system_clock::from_time_t(0);
//
//				// rather than calculate by hand the difference in time offsets between POSIX epoch and 1601, let's use a helper
//	            Windows::Globalization::Calendar^ calendar = ref new Windows::Globalization::Calendar();
//	            calendar.year = 1970;
//	            calendar.month = 1;
//	            calendar.day = 1;
//	            calendar.hour = 0;
//	            calendar.minute = 0;
//	            calendar.second = 0;
//	            calendar.nanosecond = 0;
//	            uint64_t epochDate = (date.UniversalTime - calendar.GetDateTime().UniversalTime) / 10000;
//	            auto timeIntervalSince1970 = duration<double>(epochDate);
//	            return POSIX_EPOCH + duration_cast<system_clock::duration>(timeIntervalSince1970);
//	        }
//
//	        static CxType fromCpp(const CppType& date) {
//	            using namespace std::chrono;
//	            static const auto POSIX_EPOCH = system_clock::from_time_t(0);
//	            return [NSDate dateWithTimeIntervalSince1970:duration_cast<duration<double>>(date - POSIX_EPOCH).count()];
//
//	        }
//	    };

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
	    template<template<class> class OptionalType, class T>
	    class Optional {
	    public:
	        using CppType = OptionalType<typename T::CppType>;
	        using CxType = typename T::Boxed::CxType;

	        using Boxed = Optional;

	        static CppType toCpp(CxType cx) {
	            return obj ? CppType(T::Boxed::toCpp(cx)) : CppType();
	        }

	        static CxType fromCpp(const CppType& opt) {
	            return opt ? T::Boxed::fromCpp(*opt) : nil;
	        }
	    };

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
               CppType nv;
               for(ECxType^ val : v)
               {
                   nv.push_back(T::Boxed::toCpp(val));
               }
               return nv;
           }

           static CxType^ fromCpp(const CppType& v) {
			   Platform::Collections::Vector<ECxType^>^ nv = ref new Platform::Collections::Vector<ECxType^>;
			   for (ECppType val : v)
			   {
				   nv->Append(T::Boxed::fromCpp(val));
			   }
			   return nv;
           }
		   //We ought to specialize this for types C++/Cx knows how to convert for us.
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
