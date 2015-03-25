//
//  DJIMarshal+Private.h
//  Djinni
//
//  Created by knejp on 20.3.15.
//  Copyright (c) 2015 Dropbox. All rights reserved.
//

#pragma once
#import <Foundation/Foundation.h>
#include <cassert>
#include <cstdint>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

namespace djinni {
	
	struct Bool
	{
		using CppType = bool;
		using ObjcType = BOOL;
		
		static CppType toCpp(ObjcType x) noexcept { return x ? true : false; }
		static ObjcType fromCpp(CppType x) noexcept { return x ? YES : NO; }

		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { assert(x); return Bool::toCpp([x boolValue]); }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithBool:Bool::fromCpp(x)]; }
		};
	};
	
	template<class Self, class T>
	struct Primitive
	{
		using CppType = T;
		using ObjcType = T;
		
		static CppType toCpp(ObjcType x) noexcept { return x; }
		static ObjcType fromCpp(CppType x) noexcept { return x; }
		
		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { assert(x); return static_cast<CppType>(Self::unbox(x)); }
			static ObjcType fromCpp(CppType x) noexcept { return Self::box(x); }
		};
	};
	class I8 : public Primitive<I8, int8_t>
	{
		friend Primitive<I8, int8_t>;
		static auto unbox(Boxed::ObjcType x) noexcept { return [x charValue]; }
		static auto box(CppType x) noexcept { return [NSNumber numberWithChar:static_cast<char>(x)]; }
	};
	class I16 : public Primitive<I16, int16_t>
	{
		friend Primitive<I16, int16_t>;
		static auto unbox(Boxed::ObjcType x) noexcept { return [x shortValue]; }
		static auto box(CppType x) noexcept { return [NSNumber numberWithShort:static_cast<short>(x)]; }
	};
	class I32 : public Primitive<I32, int32_t>
	{
		friend Primitive<I32, int32_t>;
		static auto unbox(Boxed::ObjcType x) noexcept { return [x intValue]; }
		static auto box(CppType x) noexcept { return [NSNumber numberWithInt:static_cast<int>(x)]; }
	};
	struct F64 : public Primitive<F64, double>
	{
		friend Primitive<F64, double>;
		static auto unbox(Boxed::ObjcType x) noexcept { return [x doubleValue]; }
		static auto box(CppType x) noexcept { return [NSNumber numberWithDouble:x]; }
	};
	
	template<class CppEnum, class ObjcEnum>
	struct Enum
	{
		using CppType = CppEnum;
		using ObjcType = ObjcEnum;

		static CppType toCpp(ObjcType e) noexcept { return static_cast<CppType>(e); }
		static ObjcType fromCpp(CppType e) noexcept { return static_cast<ObjcType>(e); }
		
		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { return Enum::toCpp([x integerValue]); }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithInteger:Enum::fromCpp(x)]; }
		};
	};
	
	struct String
	{
		using CppType = std::string;
		using ObjcType = NSString*;
		
		using Boxed = String;
		
		static CppType toCpp(ObjcType string)
		{
			assert(string);
			return {[string UTF8String], [string lengthOfBytesUsingEncoding:NSUTF8StringEncoding]};
		}
		static ObjcType fromCpp(const CppType& string)
		{
			assert(string.size() <= std::numeric_limits<NSUInteger>::max());
			return [[NSString alloc] initWithBytes:string.data()
											length:static_cast<NSUInteger>(string.size())
										  encoding:NSUTF8StringEncoding];
		}
	};
	
	struct Binary
	{
		using CppType = std::vector<uint8_t>;
		using ObjcType = NSData*;
		
		using Boxed = Binary;
		
		static CppType toCpp(ObjcType data)
		{
			assert(data);
			auto bytes = reinterpret_cast<const uint8_t*>(data.bytes);
			return data.length > 0 ? CppType{bytes, bytes + data.length} : CppType{};
		}
		static ObjcType fromCpp(const CppType& bytes)
		{
			assert(bytes.size() <= std::numeric_limits<NSUInteger>::max());
			// Using the pointer from .data() on an empty vector is UB
			return bytes.empty() ? [NSData data] : [NSData dataWithBytes:bytes.data()
																  length:static_cast<NSUInteger>(bytes.size())];
		}
	};
	
	template<template<class> class OptionalType, class T>
	class Optional
	{
	public:
		using CppType = OptionalType<typename T::CppType>;
		using ObjcType = typename T::Boxed::ObjcType;
		
		using Boxed = Optional;
		
		static CppType toCpp(ObjcType obj)
		{
			return obj ? CppType(T::Boxed::toCpp(obj)) : CppType();
		}
		static ObjcType fromCpp(const CppType& opt)
		{
			return opt ? T::Boxed::fromCpp(*opt) : nil;
		}
	};
	
	template<class T>
	class List
	{
		using ECppType = typename T::CppType;
		using EObjcType = typename T::Boxed::ObjcType;
		
	public:
		using CppType = std::vector<ECppType>;
		using ObjcType = NSArray*;
		
		using Boxed = List;
		
		static CppType toCpp(ObjcType array)
		{
			assert(array);
			auto v = CppType();
			v.reserve(array.count);
			for(EObjcType value in array)
				v.push_back(T::Boxed::toCpp(value));
			return v;
		}
		static ObjcType fromCpp(const CppType& v)
		{
			assert(v.size() <= std::numeric_limits<NSUInteger>::max());
			auto array = [NSMutableArray arrayWithCapacity:static_cast<NSUInteger>(v.size())];
			for(const auto& value : v)
				[array addObject:T::Boxed::fromCpp(value)];
			return array;
		}
	};
	
	template<class T>
	class Set
	{
		using ECppType = typename T::CppType;
		using EObjcType = typename T::Boxed::ObjcType;
		
	public:
		using CppType = std::unordered_set<ECppType>;
		using ObjcType = NSSet*;
		
		using Boxed = Set;
		
		static CppType toCpp(ObjcType set)
		{
			assert(set);
			auto s = CppType();
			for(EObjcType value in set)
				s.insert(T::Boxed::toCpp(value));
			return s;
		}
		static ObjcType fromCpp(const CppType& s)
		{
			assert(s.size() <= std::numeric_limits<NSUInteger>::max());
			auto set = [NSMutableSet setWithCapacity:static_cast<NSUInteger>(s.size())];
			for(const auto& value : s)
				[set addObject:T::Boxed::fromCpp(value)];
			return set;
		}
	};

	template<class Key, class Value>
	class Map
	{
		using CppKeyType = typename Key::CppType;
		using CppValueType = typename Value::CppType;
		using ObjcKeyType = typename Key::Boxed::ObjcType;
		using ObjcValueType = typename Value::Boxed::ObjcType;
		
	public:
		using CppType = std::unordered_map<CppKeyType, CppValueType>;
		using ObjcType = NSDictionary*;
		
		using Boxed = Map;
		
		static CppType toCpp(ObjcType map)
		{
			assert(map);
			auto m = CppType();
			m.reserve(map.count);
			[map enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
				m.insert(Key::Boxed::toCpp(key), Value::Boxed::toCpp(obj));
			}];
			return m;
		}
		static ObjcType fromCpp(const CppType& m)
		{
			assert(m.size() <= std::numeric_limits<NSUInteger>::max());
			auto map = [NSMutableDictionary dictionaryWithCapacity:static_cast<NSUInteger>(m.size())];
			for(const auto& kvp : m)
				[map setObject:Value::Boxed::fromCpp(kvp.second) forKey:Key::Boxed::fromCpp(kvp.first)];
			return map;
		}
	};
} // namespace djinni
