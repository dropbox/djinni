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
			static CppType toCpp(ObjcType x) noexcept { return [x boolValue] ? true : false; }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithChar:x ? YES : NO]; }
		};
	};
	
	template<class T>
	struct Primitive
	{
		using CppType = T;
		using ObjcType = T;
		
		static CppType toCpp(ObjcType x) noexcept { return x; }
		static ObjcType fromCpp(CppType x) noexcept { return x; }
	};
	struct I8 : public Primitive<int8_t>
	{
		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { return [x charValue]; }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithChar:x]; }
		};
	};
	struct I16 : public Primitive<int16_t>
	{
		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { return [x shortValue]; }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithShort:x]; }
		};
	};
	struct I32 : public Primitive<int32_t>
	{
		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { return [x intValue]; }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithInt:x]; }
		};
	};
	struct F64 : public Primitive<double>
	{
		struct Boxed
		{
			using ObjcType = NSNumber*;
			static CppType toCpp(ObjcType x) noexcept { return [x doubleValue]; }
			static ObjcType fromCpp(CppType x) noexcept { return [NSNumber numberWithDouble:x]; }
		};
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
			return {[string UTF8String], [string lengthOfBytesUsingEncoding:NSUTF8StringEncoding]};
		}
		static ObjcType fromCpp(const CppType& string)
		{
			assert(string.size() <= std::numeric_limits<NSUInteger>::max());
			// Using the pointer from .data() on an empty string is UB
			return string.empty() ? @"" : [[NSString alloc] initWithBytes:string.data() length:string.size() encoding:NSUTF8StringEncoding];
		}
	};
	
	struct Binary
	{
		using CppType = std::vector<uint8_t>;
		using ObjcType = NSData*;
		
		using Boxed = Binary;
		
		static CppType toCpp(ObjcType data)
		{
			auto bytes = reinterpret_cast<const uint8_t*>(data.bytes);
			return data.length > 0 ? CppType{bytes, bytes + data.length} : CppType{};
		}
		static ObjcType fromCpp(const CppType& bytes)
		{
			assert(bytes.size() <= std::numeric_limits<NSUInteger>::max());
			// Using the pointer from .data() on an empty vector is UB
			return bytes.empty() ? [NSData data] : [NSData dataWithBytes:bytes.data() length:bytes.size()];
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
			auto v = CppType();
			v.reserve(array.count);
			for(EObjcType value in array)
				v.push_back(T::Boxed::toCpp(value));
			return v;
		}
		static ObjcType fromCpp(const CppType& v)
		{
			assert(v.size() <= std::numeric_limits<NSUInteger>::max());
			auto array = [NSMutableArray arrayWithCapacity:v.size()];
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
			auto s = CppType();
			for(EObjcType value in set)
				s.insert(T::Boxed::toCpp(value));
			return s;
		}
		static ObjcType fromCpp(const CppType& s)
		{
			assert(s.size() <= std::numeric_limits<NSUInteger>::max());
			auto set = [NSMutableSet setWithCapacity:s.size()];
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
			auto map = [NSMutableDictionary dictionaryWithCapacity:m.size()];
			for(const auto& kvp : m)
				[map setObject:Value::Boxed::fromCpp(kvp.second) forKey:Key::Boxed::fromCpp(kvp.first)];
			return map;
		}
	};
} // namespace djinni
