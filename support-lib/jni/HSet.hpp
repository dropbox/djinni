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

class HSetJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/HashSet") };
    const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "()V") };
    const jmethodID method_add { jniGetMethodID(clazz.get(), "add", "(Ljava/lang/Object;)Z") };
    const jmethodID method_size { jniGetMethodID(clazz.get(), "size", "()I") };
    const jmethodID method_iterator { jniGetMethodID(clazz.get(), "iterator", "()Ljava/util/Iterator;") };
};

template <class HElement>
class HSet {
    using ECppType = typename HElement::CppType;
    using EJniType = typename HElement::JniType;

public:
    using CppType = std::unordered_set<ECppType>;
    using JniType = jobject;

    static std::unordered_set<ECppType> fromJava(JNIEnv* jniEnv, jobject j) {
        assert(j != nullptr);
        const auto & data = JniClass<HSetJniInfo>::get();
        const auto & iteData = JniClass<HIteJniInfo>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        jint size = jniEnv->CallIntMethod(j, data.method_size);
        std::unordered_set<ECppType> c;
        LocalRef<jobject> it(jniEnv, jniEnv->CallObjectMethod(j, data.method_iterator));
        for (jint i = 0; i < size; i++) {
            LocalRef<EJniType> je(jniEnv, static_cast<EJniType>(jniEnv->CallObjectMethod(it.get(), iteData.method_next)));
            jniExceptionCheck(jniEnv);
            c.insert(HElement::fromJava(jniEnv, je.get()));
        }
        return c;
    }

    static jobject toJava(JNIEnv* jniEnv, std::unordered_set<ECppType> c) {
        const auto & data = JniClass<HSetJniInfo>::get();
        LocalRef<jobject> j(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor));
        jniExceptionCheck(jniEnv);
        for (const auto & ce : c) {
            LocalRef<jobject> je(jniEnv, HElement::toJava(jniEnv, ce));
            jniEnv->CallBooleanMethod(j.get(), data.method_add, je.get());
            jniExceptionCheck(jniEnv);
        }
        return j.release();
    }
};

} // namespace djinni
