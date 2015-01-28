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

#include "djinni_support.hpp"
#include <chrono>

namespace djinni {

class HDateJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass("java/util/Date") };
    const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "(J)V") };
    const jmethodID method_get_time { jniGetMethodID(clazz.get(), "getTime", "()J") };
};

class HDate {
public:
    using CppType = std::chrono::system_clock::time_point;
    using JniType = jobject;

    static std::chrono::system_clock::time_point fromJava(JNIEnv* jniEnv, jobject j) {
        static const auto POSIX_EPOCH = std::chrono::system_clock::from_time_t(0);
        assert(j != nullptr);
        const auto & data = JniClass<HDateJniInfo>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        const jlong time_millis = jniEnv->CallLongMethod(j, data.method_get_time);
        return POSIX_EPOCH + std::chrono::milliseconds{time_millis};
    }

    static jobject toJava(JNIEnv* jniEnv, std::chrono::system_clock::time_point value) {
        static const auto POSIX_EPOCH = std::chrono::system_clock::from_time_t(0);
        const auto & data = JniClass<HDateJniInfo>::get();
        const auto cpp_millis = std::chrono::duration_cast<std::chrono::milliseconds>(value - POSIX_EPOCH);
        const jlong millis = static_cast<jlong>(cpp_millis.count());
        LocalRef<jobject> j(jniEnv, jniEnv->NewObject(data.clazz.get(), data.constructor, millis));
        jniExceptionCheck(jniEnv);
        return j.release();
    }
};

} // namespace djinni
