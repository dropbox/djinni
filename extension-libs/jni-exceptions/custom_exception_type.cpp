/**
 * Copyright 2015 Dropbox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is an example of how to replace Djinni's default JNI exception translation functions
 * to provide custom behavior.  For this example, we show how to convert specific Java exception
 * types to custom C++ exception types.
 */

#include <exception>
#include "../../../support-lib/jni/djinni_support.hpp"

using namespace djinni;

/*
 * Custom C++ exception I want to throw in response to a Java security exception.
 * In production code, you'd want to put this in a header, so other C++ code can throw and
 * catch it.
 */
class my_security_exception : public std::runtime_error {
public:
    my_security_exception(const std::string & what_arg) : runtime_error(what_arg) {};
};

/*
 * Helper type to prepare and cache all the Java type and method lookups we need.
 */
struct SecurityExceptionClassInfo {
    const GlobalRef<jclass> clazz = djinni::jniFindClass("java/lang/SecurityException");
    const jmethodID ctor = djinni::jniGetMethodID(clazz.get(), "<init>",
                                                  "(Ljava/lang/String;)V");
    const jmethodID getMessage = djinni::jniGetMethodID(clazz.get(), "getMessage",
                                                        "()Ljava/lang/String;");
};

/*
 * We're replacing pluggable functions inside the djinni namespace.
 */
namespace djinni {

/*
 * Pluggable Djinni translation function for C++ -> Java.  Called when a call
 * from Java to C++ throws an exception.  Called inside a catch block.
 *
 * The default implementation is defined with __attribute__((weak)) so this
 * definition can replace it.
 */
void jniSetPendingFromCurrent(JNIEnv * env, const char * ctx) noexcept {
    try {
        throw;
    } catch (const my_security_exception & e) {
        const SecurityExceptionClassInfo & ci = djinni::JniClass<SecurityExceptionClassInfo>::get();
        LocalRef<jstring> jmessage { env, jniStringFromUTF8(env, e.what()) };
        LocalRef<jthrowable> jex { env, (jthrowable)env->NewObject(ci.clazz.get(), ci.ctor, jmessage.get()) };
        env->Throw(jex);
        return;
    } catch (const std::exception & e) {
        jniDefaultSetPendingFromCurrent(env, ctx);
    }
}

/*
 * Pluggable Djinni translation function for Java -> C++.  Called when a call
 * from C++ to Java throws an exception.
 *
 * The default implementation is defined with __attribute__((weak)) so this
 * definition can replace it.
 */
void jniThrowCppFromJavaException(JNIEnv * env, jthrowable java_exception) {
    const SecurityExceptionClassInfo & ci = djinni::JniClass<SecurityExceptionClassInfo>::get();
    if (env->IsInstanceOf(java_exception, ci.clazz.get())) {
        LocalRef<jstring> jmessage {
            env, (jstring)env->CallObjectMethod(java_exception, ci.getMessage) };
        throw my_security_exception(jniUTF8FromString(env, jmessage.get()));
    }
    throw jni_exception { env, java_exception };
}

} // namespace djinni
