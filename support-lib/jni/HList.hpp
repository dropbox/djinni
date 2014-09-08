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
#include <set>

namespace djinni {

class HListJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/ArrayList") };
    const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "(I)V") };
    const jmethodID method_add { jniGetMethodID(clazz.get(), "add", "(Ljava/lang/Object;)Z") };
    const jmethodID method_get { jniGetMethodID(clazz.get(), "get", "(I)Ljava/lang/Object;") };
    const jmethodID method_size { jniGetMethodID(clazz.get(), "size", "()I") };
};

template <class HElement>
class HList {
    using ECppType = typename HElement::CppType;
    using EJniType = typename HElement::JniType;

public:
    using CppType = std::vector<ECppType>;
    using JniType = jobject;

    static std::vector<ECppType> fromJava(JNIEnv* jniEnv, jobject j) {
        assert(j != nullptr);
        const auto & data = JniClass<HListJniInfo>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        jint size = jniEnv->CallIntMethod(j, data.method_size);
        std::vector<ECppType> c;
        c.reserve(size);
        for (jint i = 0; i < size; i++) {
            LocalRef<EJniType> je(jniEnv, static_cast<EJniType>(jniEnv->CallObjectMethod(j, data.method_get, i)));
            jniExceptionCheck(jniEnv);
            ECppType ce = HElement::fromJava(jniEnv, je.get());
            c.push_back(std::move(ce));
        }
        return c;
    }

    static jobject toJava(JNIEnv* jniEnv, std::vector<ECppType> c) {
        const auto & data = JniClass<HListJniInfo>::get();
        assert(c.size() <= std::numeric_limits<jint>::max());
        jint size = c.size();
        LocalRef<jobject> j(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, size));
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
