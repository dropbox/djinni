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

template <class JniType>
JniType jniUnboxMethodCall(JNIEnv* jniEnv, jmethodID method, jobject thiz);

template <class Self, class _CppType, class UnboxedJniType>
class Primitive {
public:
    using CppType = _CppType;
    using JniType = jobject;

    static CppType fromJava(JNIEnv* jniEnv, jobject j) {
        assert(j != nullptr);
        const auto & data = JniClass<Self>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        UnboxedJniType jni = jniUnboxMethodCall<UnboxedJniType>(jniEnv, data.method_unbox, j);
        CppType c = Primitive::Unboxed::fromJava(jniEnv, jni);
        return c;
    }

    static jobject toJava(JNIEnv* jniEnv, CppType c) {
        const auto & data = JniClass<Self>::get();
        UnboxedJniType jni = Primitive::Unboxed::toJava(jniEnv, c);
        jobject j = jniEnv->CallStaticObjectMethod(data.clazz.get(), data.method_box, jni);
        jniExceptionCheck(jniEnv);
        return j;
    }

    class Unboxed {
    public:
        static CppType fromJava(JNIEnv* /*jniEnv*/, UnboxedJniType j) {
            return static_cast<CppType>(j);
        }

        static UnboxedJniType toJava(JNIEnv* /*jniEnv*/, CppType c) {
            return static_cast<UnboxedJniType>(c);
        }
    };

protected:
    Primitive(
            const char* javaClassSpec,
            const char* staticBoxMethod,
            const char* staticBoxMethodSignature,
            const char* unboxMethod,
            const char* unboxMethodSignature) :
        clazz(jniFindClass(javaClassSpec)),
        method_box(jniGetStaticMethodID(clazz.get(), staticBoxMethod, staticBoxMethodSignature)),
        method_unbox(jniGetMethodID(clazz.get(), unboxMethod, unboxMethodSignature)) {}

private:
    const GlobalRef<jclass> clazz;
    const jmethodID method_box;
    const jmethodID method_unbox;
};

template <>
inline
jint jniUnboxMethodCall<jint>(JNIEnv* jniEnv, jmethodID method, jobject thiz) {
    jint ret = jniEnv->CallIntMethod(thiz, method);
    jniExceptionCheck(jniEnv);
    return ret;
}

template <>
inline
jlong jniUnboxMethodCall<jlong>(JNIEnv* jniEnv, jmethodID method, jobject thiz) {
    jlong ret = jniEnv->CallLongMethod(thiz, method);
    jniExceptionCheck(jniEnv);
    return ret;
}

template <>
inline
jboolean jniUnboxMethodCall<jboolean>(JNIEnv* jniEnv, jmethodID method, jobject thiz) {
    jboolean ret = jniEnv->CallBooleanMethod(thiz, method);
    jniExceptionCheck(jniEnv);
    return ret;
}

template <>
inline
jdouble jniUnboxMethodCall<jdouble>(JNIEnv* jniEnv, jmethodID method, jobject thiz) {
    jdouble ret = jniEnv->CallDoubleMethod(thiz, method);
    jniExceptionCheck(jniEnv);
    return ret;
}

} // namespace djinni
