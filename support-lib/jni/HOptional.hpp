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

namespace djinni {

template <template <class> class Optional, class HElement>
class HOptional {
    using ECppType = typename HElement::CppType;
    using EJniType = typename HElement::JniType;

public:
    using CppType = Optional<ECppType>;
    using JniType = EJniType;

    static CppType fromJava(JNIEnv* jniEnv, JniType j) {
        if (j != nullptr) {
            return HElement::fromJava(jniEnv, j);
        }
        return {};
    }

    static JniType toJava(JNIEnv* jniEnv, const CppType & c) {
        if (c) {
            return HElement::toJava(jniEnv, *c);
        }
        return nullptr;
    }
};

} // namespace djinni
