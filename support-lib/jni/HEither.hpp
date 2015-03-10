#pragma once

#include "djinni_support.hpp"

#include <utility>

#ifndef HEITHER_JCLASSNAME
static_assert(false, "Missing definition for Java class for either");
#endif

namespace djinni {

class HEitherJniInfo {
public:
    const GlobalRef<jclass> clazz { jniFindClass(HEITHER_JCLASSNAME) };
    const jmethodID method_asLeft { jniGetStaticMethodID(clazz.get(), "asLeft", "(Ljava/lang/Object;)Lcom/wattpad/util/Either;") };
    const jmethodID method_asRight { jniGetStaticMethodID(clazz.get(), "asRight", "(Ljava/lang/Object;)Lcom/wattpad/util/Either;") };
    const jmethodID method_left { jniGetMethodID(clazz.get(), "left", "()Ljava/lang/Object;") };
    const jmethodID method_right { jniGetMethodID(clazz.get(), "right", "()Ljava/lang/Object;") };
};

template <template <class, class> class Either, class HLeft, class HRight>
class HEither {
    using LCppType = typename HLeft::CppType;
    using LJniType = typename HLeft::JniType;
    using RCppType = typename HRight::CppType;
    using RJniType = typename HRight::JniType;

public:
    using CppType = Either<LCppType, RCppType>;
    using JniType = jobject;

    static CppType fromJava(JNIEnv* jniEnv, jobject j) {
        assert(j != nullptr);
        const auto & data = JniClass<HEitherJniInfo>::get();
        assert(jniEnv->IsInstanceOf(j, data.clazz.get()));
        LocalRef<LJniType> left(jniEnv, static_cast<LJniType>(jniEnv->CallObjectMethod(j, data.method_left)));
        LocalRef<RJniType> right(jniEnv, static_cast<RJniType>(jniEnv->CallObjectMethod(j, data.method_right)));
        DJINNI_ASSERT(left || right, jniEnv);

        if (left) {
            return CppType(HLeft::fromJava(jniEnv, left.get()));
        }
        else {
            return CppType(HRight::fromJava(jniEnv, right.get()));
        }
    }

    static JniType toJava(JNIEnv* jniEnv, const CppType & c) {
        const auto & data = JniClass<HEitherJniInfo>::get();
        LocalRef<JniType> j;
        if (c.isLeft()) {
            LocalRef<LJniType> left(jniEnv, HLeft::toJava(jniEnv, c.left()));
            j = LocalRef<JniType>(jniEnv, jniEnv->CallStaticObjectMethod(data.clazz.get(), data.method_asLeft, left.get()));
        }
        else {
            LocalRef<RJniType> right(jniEnv, HRight::toJava(jniEnv, c.right()));
            j = LocalRef<JniType>(jniEnv, jniEnv->CallStaticObjectMethod(data.clazz.get(), data.method_asRight, right.get()));
        }
        jniExceptionCheck(jniEnv);
        return j.release();
    }
};

} // namespace djinni
