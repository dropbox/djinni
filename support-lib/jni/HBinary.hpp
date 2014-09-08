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

#include <cstdlib>

namespace djinni {

class HBinary {
public:
    using CppType = std::vector<uint8_t>;
    using JniType = jbyteArray;

    static std::vector<uint8_t> fromJava(JNIEnv * jniEnv, jbyteArray j) {
        const auto deleter = [jniEnv, j] (void * c) {
            jniEnv->ReleasePrimitiveArrayCritical(j, c, JNI_ABORT);
        };

        std::unique_ptr<uint8_t, decltype(deleter)> ptr(
            reinterpret_cast<uint8_t *>(jniEnv->GetPrimitiveArrayCritical(j, nullptr)),
            deleter);

        jniExceptionCheck(jniEnv);

        return std::vector<uint8_t>(ptr.get(), ptr.get() + jniEnv->GetArrayLength(j));
    }

    static jbyteArray toJava(JNIEnv* jniEnv, const std::vector<uint8_t> & c) {
        LocalRef<jbyteArray> j(jniEnv, jniEnv->NewByteArray(c.size()));
        DJINNI_ASSERT(j, jniEnv);

        jniEnv->SetByteArrayRegion(j.get(), 0, c.size(),
                                   reinterpret_cast<const jbyte *>(c.data()));
        return j.release();
    }
};

} // namespace djinni
