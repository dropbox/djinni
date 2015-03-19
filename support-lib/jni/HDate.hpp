//
// Copyright 2015 Dropbox, Inc. and WP Technology, Inc.
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

namespace djinni {

class HDateJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/Date") };
    const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "(J)V") };
    const jmethodID method_getTime { jniGetMethodID(clazz.get(), "getTime", "()J") };
};

class HDate {
public:
    using CppType = double;
    using JniType = jobject;

    static double fromJava(JNIEnv* jniEnv, jobject j) {
        assert(j != nullptr);
        const auto & data = JniClass<HDateJniInfo>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        jlong ret = jniEnv->CallLongMethod(j, data.method_getTime);
        jniExceptionCheck(jniEnv);
        return ret;
    }

    static jobject toJava(JNIEnv* jniEnv, double c) {
        const auto & data = JniClass<HDateJniInfo>::get();
        LocalRef<jobject> j(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, c));
        jniExceptionCheck(jniEnv);
        return j.release();
    }
};

} // namespace djinni
