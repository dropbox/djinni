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

namespace djinni {

class HString {
public:
    using CppType = std::string;
    using JniType = jstring;

    static std::string fromJava(JNIEnv* jniEnv, jstring j) {
        assert(j != nullptr);
        return jniUTF8FromString(jniEnv, j);
    }

    static jstring toJava(JNIEnv* jniEnv, std::string c) {
        return jniStringFromUTF8(jniEnv, c.c_str());
    }
};

} // namespace djinni
