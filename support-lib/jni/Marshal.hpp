//
// Copyright 2014 Dropbox, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

#pragma once

#include "djinni_support.hpp"
#include <cassert>
#include <chrono>
#include <cstdint>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

namespace djinni
{
    template<class Self, class CppT, class JniT>
    class Primitive
    {
    public:
        using CppType = CppT;
        using JniType = JniT;

        static CppType toCpp(JNIEnv* /*jniEnv*/, JniType j) noexcept { return static_cast<CppType>(j); }
        static JniType fromCpp(JNIEnv* /*jniEnv*/, CppType c) noexcept { return static_cast<JniType>(c); }

        struct Boxed
        {
            using JniType = jobject;
            static CppType toCpp(JNIEnv* jniEnv, JniType j)
            {
                assert(j != nullptr);
                const auto& data = JniClass<Self>::get();
                assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
                auto ret = Primitive::toCpp(jniEnv, Self::unbox(jniEnv, data.method_unbox, j));
                jniExceptionCheck(jniEnv);
                return ret;
            }
            static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, CppType c)
            {
                const auto& data = JniClass<Self>::get();
                auto ret = jniEnv->CallStaticObjectMethod(data.clazz.get(), data.method_box, Primitive::fromCpp(jniEnv, c));
                jniExceptionCheck(jniEnv);
                return {jniEnv, ret};
            }
        };

    protected:
        Primitive(const char* javaClassSpec,
                  const char* staticBoxMethod,
                  const char* staticBoxMethodSignature,
                  const char* unboxMethod,
                  const char* unboxMethodSignature)
        : clazz(jniFindClass(javaClassSpec))
        , method_box(jniGetStaticMethodID(clazz.get(), staticBoxMethod, staticBoxMethodSignature))
        , method_unbox(jniGetMethodID(clazz.get(), unboxMethod, unboxMethodSignature))
        {}

    private:
        const GlobalRef<jclass> clazz;
        const jmethodID method_box;
        const jmethodID method_unbox;
    };

    class Bool : public Primitive<Bool, bool, jboolean>
    {
        Bool() : Primitive("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", "booleanValue", "()Z") {}
        friend JniClass<Bool>;
        friend Primitive<Bool, bool, jboolean>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallBooleanMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    class I8 : public Primitive<I8, int8_t, jbyte>
    {
        I8() : Primitive("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", "byteValue", "()B") {}
        friend JniClass<I8>;
        friend Primitive<I8, int8_t, jbyte>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallByteMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    class I16 : public Primitive<I16, int16_t, jshort>
    {
        I16() : Primitive("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", "shortValue", "()S") {}
        friend JniClass<I16>;
        friend Primitive<I16, int16_t, jshort>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallShortMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    class I32 : public Primitive<I32, int32_t, jint>
    {
        I32() : Primitive("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", "intValue", "()I") {}
        friend JniClass<I32>;
        friend Primitive<I32, int32_t, jint>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallIntMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    class I64 : public Primitive<I64, int64_t, jlong>
    {
        I64() : Primitive("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", "longValue", "()J") {}
        friend JniClass<I64>;
        friend Primitive<I64, int64_t, jlong>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallLongMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    class F32 : public Primitive<F32, float, jfloat>
    {
        F32() : Primitive("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", "floatValue", "()F") {}
        friend JniClass<F32>;
        friend Primitive<F32, float, jfloat>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallFloatMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    class F64 : public Primitive<F64, double, jdouble>
    {
        F64() : Primitive("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", "doubleValue", "()D") {}
        friend JniClass<F64>;
        friend Primitive<F64, double, jdouble>;
        static JniType unbox(JNIEnv* jniEnv, jmethodID method, jobject j) {
            auto result = jniEnv->CallDoubleMethod(j, method);
            jniExceptionCheck(jniEnv);
            return result;
        }
    };

    struct String
    {
        using CppType = std::string;
        using JniType = jstring;

        using Boxed = String;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);
            return jniUTF8FromString(jniEnv, j);
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            return {jniEnv, jniStringFromUTF8(jniEnv, c)};
        }
    };

    struct WString
    {
        using CppType = std::wstring;
        using JniType = jstring;

        using Boxed = WString;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);
            return jniWStringFromString(jniEnv, j);
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            return {jniEnv, jniStringFromWString(jniEnv, c)};
        }
    };

    struct Binary
    {
        using CppType = std::vector<uint8_t>;
        using JniType = jbyteArray;

        using Boxed = Binary;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);

            std::vector<uint8_t> ret;
            jsize length = jniEnv->GetArrayLength(j);
            jniExceptionCheck(jniEnv);

            if (!length) {
                return ret;
            }

            {
                auto deleter = [jniEnv, j] (void* c) {
                    if (c) {
                        jniEnv->ReleasePrimitiveArrayCritical(j, c, JNI_ABORT);
                    }
                };

                std::unique_ptr<uint8_t, decltype(deleter)> ptr(
                    reinterpret_cast<uint8_t*>(jniEnv->GetPrimitiveArrayCritical(j, nullptr)),
                    deleter
                );

                if (ptr) {
                    // Construct and then move-assign. This copies the elements only once,
                    // and avoids having to initialize before filling (as with resize())
                    ret = std::vector<uint8_t>{ptr.get(), ptr.get() + length};
                } else {
                    // Something failed...
                    jniExceptionCheck(jniEnv);
                }
            }

            return ret;
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            assert(c.size() <= std::numeric_limits<jsize>::max());
            auto j = LocalRef<jbyteArray>(jniEnv, jniEnv->NewByteArray(static_cast<jsize>(c.size())));
            jniExceptionCheck(jniEnv);
            // Using .data() on an empty vector is UB
            if(!c.empty())
            {
                jniEnv->SetByteArrayRegion(j.get(), 0, jsize(c.size()), reinterpret_cast<const jbyte*>(c.data()));
            }
            return j;
        }
    };

    struct Date
    {
        using CppType = std::chrono::system_clock::time_point;
        using JniType = jobject;

        using Boxed = Date;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            static const auto POSIX_EPOCH = std::chrono::system_clock::from_time_t(0);
            assert(j != nullptr);
            const auto & data = JniClass<Date>::get();
            assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
            auto time_millis = jniEnv->CallLongMethod(j, data.method_get_time);
            jniExceptionCheck(jniEnv);
            return POSIX_EPOCH + std::chrono::milliseconds{time_millis};
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            static const auto POSIX_EPOCH = std::chrono::system_clock::from_time_t(0);
            const auto & data = JniClass<Date>::get();
            const auto cpp_millis = std::chrono::duration_cast<std::chrono::milliseconds>(c - POSIX_EPOCH);
            const jlong millis = static_cast<jlong>(cpp_millis.count());
            auto j = LocalRef<jobject>(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, millis));
            jniExceptionCheck(jniEnv);
            return j;
        }

    private:
        Date() = default;
        friend ::djinni::JniClass<Date>;

        const GlobalRef<jclass> clazz { jniFindClass("java/util/Date") };
        const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "(J)V") };
        const jmethodID method_get_time { jniGetMethodID(clazz.get(), "getTime", "()J") };
    };

    template <template <class> class OptionalType, class T>
    struct Optional
    {
        // SFINAE helper: if C::CppOptType exists, opt_type<T>(nullptr) will return
        // that type. If not, it returns OptionalType<C::CppType>. This is necessary
        // because we special-case optional interfaces to be represented as a nullable
        // std::shared_ptr<T>, not optional<shared_ptr<T>> or optional<nn<shared_ptr<T>>>.
        template <typename C> static OptionalType<typename C::CppType> opt_type(...);
        template <typename C> static typename C::CppOptType opt_type(typename C::CppOptType *);
        using CppType = decltype(opt_type<T>(nullptr));

        using JniType = typename T::Boxed::JniType;

        using Boxed = Optional;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            if (j) {
                return T::Boxed::toCpp(jniEnv, j);
            } else {
                return CppType();
            }
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const OptionalType<typename T::CppType> &c)
        {
            return c ? T::Boxed::fromCpp(jniEnv, *c) : LocalRef<JniType>{};
        }

        // fromCpp used for nullable shared_ptr
        template <typename C = T>
        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const typename C::CppOptType & cppOpt) {
            return T::Boxed::fromCppOpt(jniEnv, cppOpt);
        }
    };

    struct ListJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/ArrayList") };
        const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "(I)V") };
        const jmethodID method_add { jniGetMethodID(clazz.get(), "add", "(Ljava/lang/Object;)Z") };
        const jmethodID method_get { jniGetMethodID(clazz.get(), "get", "(I)Ljava/lang/Object;") };
        const jmethodID method_size { jniGetMethodID(clazz.get(), "size", "()I") };
    };

    template <class T>
    class List
    {
        using ECppType = typename T::CppType;
        using EJniType = typename T::Boxed::JniType;

    public:
        using CppType = std::vector<ECppType>;
        using JniType = jobject;

        using Boxed = List;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);
            const auto& data = JniClass<ListJniInfo>::get();
            assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
            auto size = jniEnv->CallIntMethod(j, data.method_size);
            jniExceptionCheck(jniEnv);
            auto c = CppType();
            c.reserve(size);
            for(jint i = 0; i < size; ++i)
            {
                auto je = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(j, data.method_get, i));
                jniExceptionCheck(jniEnv);
                c.push_back(T::Boxed::toCpp(jniEnv, static_cast<EJniType>(je.get())));
            }
            return c;
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            const auto& data = JniClass<ListJniInfo>::get();
            assert(c.size() <= std::numeric_limits<jint>::max());
            auto size = static_cast<jint>(c.size());
            auto j = LocalRef<jobject>(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, size));
            jniExceptionCheck(jniEnv);
            for(const auto& ce : c)
            {
                auto je = T::Boxed::fromCpp(jniEnv, ce);
                jniEnv->CallBooleanMethod(j, data.method_add, get(je));
                jniExceptionCheck(jniEnv);
            }
            return j;
        }
    };

    struct IteratorJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/Iterator") };
        const jmethodID method_next { jniGetMethodID(clazz.get(), "next", "()Ljava/lang/Object;") };
    };

    struct SetJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/HashSet") };
        const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "()V") };
        const jmethodID method_add { jniGetMethodID(clazz.get(), "add", "(Ljava/lang/Object;)Z") };
        const jmethodID method_size { jniGetMethodID(clazz.get(), "size", "()I") };
        const jmethodID method_iterator { jniGetMethodID(clazz.get(), "iterator", "()Ljava/util/Iterator;") };
    };

    template <class T>
    class Set
    {
        using ECppType = typename T::CppType;
        using EJniType = typename T::Boxed::JniType;

    public:
        using CppType = std::unordered_set<ECppType>;
        using JniType = jobject;

        using Boxed = Set;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);
            const auto& data = JniClass<SetJniInfo>::get();
            const auto& iteData = JniClass<IteratorJniInfo>::get();
            assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
            auto size = jniEnv->CallIntMethod(j, data.method_size);
            jniExceptionCheck(jniEnv);
            auto c = CppType();
            auto it = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(j, data.method_iterator));
            jniExceptionCheck(jniEnv);
            for(jint i = 0; i < size; ++i)
            {
                auto je = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(it, iteData.method_next));
                jniExceptionCheck(jniEnv);
                c.insert(T::Boxed::toCpp(jniEnv, static_cast<EJniType>(je.get())));
            }
            return c;
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            const auto& data = JniClass<SetJniInfo>::get();
            assert(c.size() <= std::numeric_limits<jint>::max());
            auto size = static_cast<jint>(c.size());
            auto j = LocalRef<jobject>(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, size));
            jniExceptionCheck(jniEnv);
            for(const auto& ce : c)
            {
                auto je = T::Boxed::fromCpp(jniEnv, ce);
                jniEnv->CallBooleanMethod(j, data.method_add, get(je));
                jniExceptionCheck(jniEnv);
            }
            return j;
        }
    };

    struct MapJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/HashMap") };
        const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "()V") };
        const jmethodID method_put { jniGetMethodID(clazz.get(), "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") };
        const jmethodID method_size { jniGetMethodID(clazz.get(), "size", "()I") };
        const jmethodID method_entrySet { jniGetMethodID(clazz.get(), "entrySet", "()Ljava/util/Set;") };
    };

    struct EntrySetJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/Set") };
        const jmethodID method_iterator { jniGetMethodID(clazz.get(), "iterator", "()Ljava/util/Iterator;") };
    };

    struct EntryJniInfo
    {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/Map$Entry") };
        const jmethodID method_getKey { jniGetMethodID(clazz.get(), "getKey", "()Ljava/lang/Object;") };
        const jmethodID method_getValue { jniGetMethodID(clazz.get(), "getValue", "()Ljava/lang/Object;") };
    };

    template <class Key, class Value>
    class Map
    {
        using CppKeyType = typename Key::CppType;
        using CppValueType = typename Value::CppType;
        using JniKeyType = typename Key::Boxed::JniType;
        using JniValueType = typename Value::Boxed::JniType;

    public:
        using CppType = std::unordered_map<CppKeyType, CppValueType>;
        using JniType = jobject;

        using Boxed = Map;

        static CppType toCpp(JNIEnv* jniEnv, JniType j)
        {
            assert(j != nullptr);
            const auto& data = JniClass<MapJniInfo>::get();
            const auto& entrySetData = JniClass<EntrySetJniInfo>::get();
            const auto& entryData = JniClass<EntryJniInfo>::get();
            const auto& iteData = JniClass<IteratorJniInfo>::get();
            assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
            auto size = jniEnv->CallIntMethod(j, data.method_size);
            jniExceptionCheck(jniEnv);
            auto entrySet = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(j, data.method_entrySet));
            jniExceptionCheck(jniEnv);
            auto c = CppType();
            c.reserve(size);
            auto it = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(entrySet, entrySetData.method_iterator));
            jniExceptionCheck(jniEnv);
            for(jint i = 0; i < size; ++i)
            {
                auto je = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(it, iteData.method_next));
                jniExceptionCheck(jniEnv);
                auto jKey = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(je, entryData.method_getKey));
                jniExceptionCheck(jniEnv);
                auto jValue = LocalRef<jobject>(jniEnv, jniEnv->CallObjectMethod(je, entryData.method_getValue));
                jniExceptionCheck(jniEnv);
                c.emplace(Key::Boxed::toCpp(jniEnv, static_cast<JniKeyType>(jKey.get())),
                          Value::Boxed::toCpp(jniEnv, static_cast<JniValueType>(jValue.get())));
            }
            return c;
        }

        static LocalRef<JniType> fromCpp(JNIEnv* jniEnv, const CppType& c)
        {
            const auto& data = JniClass<MapJniInfo>::get();
            assert(c.size() <= std::numeric_limits<jint>::max());
            auto size = c.size();
            auto j = LocalRef<jobject>(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, size));
            jniExceptionCheck(jniEnv);
            for(const auto& ce : c)
            {
                auto jKey = Key::Boxed::fromCpp(jniEnv, ce.first);
                auto jValue = Value::Boxed::fromCpp(jniEnv, ce.second);
                jniEnv->CallObjectMethod(j, data.method_put, get(jKey), get(jValue));
                jniExceptionCheck(jniEnv);
            }
            return j;
        }
    };

} // namespace djinni
