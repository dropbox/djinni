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

#include "HIteJniInfo.hpp"
#include "djinni_support.hpp"

namespace djinni {

class HMapJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/HashMap") };
    const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "()V") };
    const jmethodID method_put { jniGetMethodID(clazz.get(), "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") };
    const jmethodID method_size { jniGetMethodID(clazz.get(), "size", "()I") };
    const jmethodID method_entrySet { jniGetMethodID(clazz.get(), "entrySet", "()Ljava/util/Set;") };
};

class HEntrySetJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/Set") };
    const jmethodID method_iterator { jniGetMethodID(clazz.get(), "iterator", "()Ljava/util/Iterator;") };
};

class HEntryJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/Map$Entry") };
    const jmethodID method_getKey { jniGetMethodID(clazz.get(), "getKey", "()Ljava/lang/Object;") };
    const jmethodID method_getValue { jniGetMethodID(clazz.get(), "getValue", "()Ljava/lang/Object;") };
};

template <class HKey, class HValue>
class HMap {
    using KCppType = typename HKey::CppType;
    using KJniType = typename HKey::JniType;
    using VCppType = typename HValue::CppType;
    using VJniType = typename HValue::JniType;

public:
    using CppType = std::unordered_map<KCppType, VCppType>;
    using JniType = jobject;

    static std::unordered_map<KCppType, VCppType> fromJava(JNIEnv* jniEnv, jobject j) {
        assert(j != nullptr);
        const auto & data = JniClass<HMapJniInfo>::get();
        const auto & entrySetData = JniClass<HEntrySetJniInfo>::get();
        const auto & entryData = JniClass<HEntryJniInfo>::get();
        const auto & iteData = JniClass<HIteJniInfo>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        jint size = jniEnv->CallIntMethod(j, data.method_size);
        LocalRef<jobject> entrySet(jniEnv, jniEnv->CallObjectMethod(j, data.method_entrySet));
        std::unordered_map<KCppType, VCppType> c;
        c.reserve(size);
        LocalRef<jobject> it(jniEnv, jniEnv->CallObjectMethod(entrySet.get(), entrySetData.method_iterator));
        for (jint i = 0; i < size; i++) {
            LocalRef<jobject> je(jniEnv, jniEnv->CallObjectMethod(it.get(), iteData.method_next));
            LocalRef<KJniType> jKey(jniEnv, static_cast<KJniType>(jniEnv->CallObjectMethod(je.get(), entryData.method_getKey)));
            LocalRef<VJniType> jValue(jniEnv, static_cast<VJniType>(jniEnv->CallObjectMethod(je.get(), entryData.method_getValue)));
            jniExceptionCheck(jniEnv);

            c.emplace(HKey::fromJava(jniEnv, jKey.get()),
                      HValue::fromJava(jniEnv, jValue.get()));
        }
        return c;
    }

    static jobject toJava(JNIEnv* jniEnv, std::unordered_map<KCppType, VCppType> c) {
        const auto & data = JniClass<HMapJniInfo>::get();
        assert(c.size() <= std::numeric_limits<jint>::max());
        jint size = c.size();
        LocalRef<jobject> j(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, size));
        jniExceptionCheck(jniEnv);
        for (const auto & ce : c) {
            LocalRef<jobject> jKey(jniEnv, HKey::toJava(jniEnv, ce.first));
            LocalRef<jobject> jValue(jniEnv, HValue::toJava(jniEnv, ce.second));
            jniEnv->CallObjectMethod(j.get(), data.method_put, jKey.get(), jValue.get());
            jniExceptionCheck(jniEnv);
        }
        return j.release();
    }
};

} // namespace djinni
