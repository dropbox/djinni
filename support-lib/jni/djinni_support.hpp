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

#include <cassert>
#include <exception>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#include "../proxy_cache_interface.hpp"
#include "../djinni_common.hpp"
#include <jni.h>

/*
 * Djinni support library
 */

// jni.h should really put extern "C" in JNIEXPORT, but it doesn't. :(
#define CJNIEXPORT extern "C" JNIEXPORT

namespace djinni {

/*
 * Global initialization and shutdown. Call these from JNI_OnLoad and JNI_OnUnload.
 */
void jniInit(JavaVM * jvm);
void jniShutdown();

/*
 * Get the JNIEnv for the invoking thread. Should only be called on Java-created threads.
 */
JNIEnv * jniGetThreadEnv();

/*
 * Global and local reference guard objects.
 *
 * A GlobalRef<T> is constructed with a local reference; the constructor upgrades the local
 * reference to a global reference, and the destructor deletes the local ref.
 *
 * A LocalRef<T> should be constructed with a new local reference. The local reference will
 * be deleted when the LocalRef is deleted.
 */
struct GlobalRefDeleter { void operator() (jobject globalRef) noexcept; };

template <typename PointerType>
class GlobalRef : public std::unique_ptr<typename std::remove_pointer<PointerType>::type,
                                         GlobalRefDeleter> {
public:
    GlobalRef() {}
    GlobalRef(GlobalRef && obj)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, ::djinni::GlobalRefDeleter>(
            std::move(obj)
        ) {}
    GlobalRef(JNIEnv * env, PointerType localRef)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, ::djinni::GlobalRefDeleter>(
            static_cast<PointerType>(env->NewGlobalRef(localRef)),
            ::djinni::GlobalRefDeleter{}
        ) {}
};

struct LocalRefDeleter { void operator() (jobject localRef) noexcept; };

template <typename PointerType>
class LocalRef : public std::unique_ptr<typename std::remove_pointer<PointerType>::type,
                                        LocalRefDeleter> {
public:
    LocalRef() {}
    LocalRef(JNIEnv * /*env*/, PointerType localRef)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, ::djinni::LocalRefDeleter>(
            localRef) {}
    explicit LocalRef(PointerType localRef)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, LocalRefDeleter>(
            localRef) {}
    // Allow implicit conversion to PointerType so it can be passed
    // as argument to JNI functions expecting PointerType.
    // All functions creating new local references should return LocalRef instead of PointerType
    operator PointerType() const & { return this->get(); }
    operator PointerType() && = delete;
};

template<class T>
const T& get(const T& x) noexcept { return x; }
template<class T>
typename LocalRef<T>::pointer get(const LocalRef<T>& x) noexcept { return x.get(); }

template<class T>
const T& release(const T& x) noexcept { return x; }
template<class T>
typename LocalRef<T>::pointer release(LocalRef<T>& x) noexcept { return x.release(); }
template<class T>
typename LocalRef<T>::pointer release(LocalRef<T>&& x) noexcept { return x.release(); }

/*
 * Exception to indicate that a Java exception is pending in the JVM.
 */
class jni_exception : public std::exception {
    GlobalRef<jthrowable> m_java_exception;
public:
    jni_exception(JNIEnv * env, jthrowable java_exception)
        : m_java_exception(env, java_exception) {
        assert(java_exception);
    }
    jthrowable java_exception() const { return m_java_exception.get(); }

    /*
     * Sets the pending JNI exception using this Java exception.
     */
    void set_as_pending(JNIEnv * env) const noexcept;
};

/*
 * Throw if any Java exception is pending in the JVM.
 *
 * If an exception is pending, this function will clear the
 * pending state, and pass the exception to
 * jniThrowCppFromJavaException().
 */
void jniExceptionCheck(JNIEnv * env);

/*
 * Throws a C++ exception based on the given Java exception.
 *
 * java_exception is a local reference to a Java throwable, which
 * must not be null, and should no longer set as "pending" in the JVM.
 * This is called to handle errors in other JNI processing, including
 * by jniExceptionCheck().
 *
 * The default implementation is defined with __attribute__((weak)) so you
 * can replace it by defining your own version.  The default implementation
 * will throw a jni_exception containing the given jthrowable.
 */
DJINNI_NORETURN_DEFINITION
void jniThrowCppFromJavaException(JNIEnv * env, jthrowable java_exception);

/*
 * Set an AssertionError in env with message message, and then throw via jniExceptionCheck.
 */
DJINNI_NORETURN_DEFINITION
void jniThrowAssertionError(JNIEnv * env, const char * file, int line, const char * check);

#define DJINNI_ASSERT_MSG(check, env, message) \
    do { \
        ::djinni::jniExceptionCheck(env); \
        const bool check__res = bool(check); \
        ::djinni::jniExceptionCheck(env); \
        if (!check__res) { \
            ::djinni::jniThrowAssertionError(env, __FILE__, __LINE__, message); \
        } \
    } while(false)
#define DJINNI_ASSERT(check, env) DJINNI_ASSERT_MSG(check, env, #check)

/*
 * Helper for JniClass. (This can't be a subclass because it needs to not be templatized.)
 */
class JniClassInitializer {

    using registration_vec = std::vector<std::function<void()>>;
    static registration_vec get_all();

private:

    JniClassInitializer(std::function<void()> init);

    template <class C> friend class JniClass;
    friend void jniInit(JavaVM *);

    static registration_vec & get_vec();
    static std::mutex       & get_mutex();
};

/*
 * Each instantiation of this template produces a singleton object of type C which
 * will be initialized by djinni::jniInit(). For example:
 *
 * struct JavaFooInfo {
 *     jmethodID foo;
 *     JavaFooInfo() // initialize clazz and foo from jniGetThreadEnv
 * }
 *
 * To use this in a JNI function or callback, invoke:
 *
 *     CallVoidMethod(object, JniClass<JavaFooInfo>::get().foo, ...);
 *
 * This uses C++'s template instantiation behavior to guarantee that any T for which
 * JniClass<T>::get() is *used* anywhere in the program will be *initialized* by init_all().
 * Therefore, it's always safe to compile in wrappers for all known Java types - the library
 * will only depend on the presence of those actually needed.
 */
template <class C>
class JniClass {
public:
    static const C & get() {
        (void)s_initializer; // ensure that initializer is actually instantiated
        assert(s_singleton);
        return *s_singleton;
    }

private:
    static const JniClassInitializer s_initializer;
    static std::unique_ptr<C> s_singleton;

    static void allocate() {
        // We can't use make_unique here, because C will have a private constructor and
        // list JniClass as a friend; so we have to allocate it by hand.
        s_singleton = std::unique_ptr<C>(new C());
    }
};

template <class C>
const JniClassInitializer JniClass<C>::s_initializer ( allocate );

template <class C>
std::unique_ptr<C> JniClass<C>::s_singleton;

/*
 * Exception-checking helpers. These will throw if an exception is pending.
 */
GlobalRef<jclass> jniFindClass(const char * name);
jmethodID jniGetStaticMethodID(jclass clazz, const char * name, const char * sig);
jmethodID jniGetMethodID(jclass clazz, const char * name, const char * sig);
jfieldID jniGetFieldID(jclass clazz, const char * name, const char * sig);

/*
 * Helper for maintaining shared_ptrs to wrapped Java objects.
 *
 * This is used for automatically wrapping a Java object that exposes some interface
 * with a C++ object that calls back into the JVM, such as a listener. Calling
 * get_java_proxy<T>(obj) the first time will construct a T and return a shared_ptr to it, and
 * also save a weak_ptr to the new object internally. The constructed T contains a strong
 * GlobalRef to jobj. As long as something in C++ maintains a strong reference to the wrapper,
 * future calls to get(jobj) will return the *same* wrapper object.
 *
 *        Java            |           C++
 *                        |        ________________________                ___________
 *   _____________        |       |                        |              |           |
 *  |             |       |       |   JniImplFooListener   | <=========== |    Foo    |
 *  | FooListener | <============ |  : public FooListener, |  shared_ptr  |___________|
 *  |_____________|   GlobalRef   |    JavaProxyCacheEntry |
 *                        |       |________________________|
 *                        |                 ^             ______________________
 *                        |                 \            |                      |
 *                        |                  - - - - - - |    JavaProxyCache    |
 *                        |                   weak_ptr   |______________________|
 *
 * As long as the C++ FooListener has references, the Java FooListener is kept alive.
 *
 * We use a custom unordered_map with Java objects (jobject) as keys, and JNI object
 * identity and hashing functions. This means that as long as a key is in the map,
 * we must have some other GlobalRef keeping it alive. To ensure safety, the Entry
 * destructor removes *itself* from the map - destruction order guarantees that this
 * will happen before the contained global reference becomes invalid (by destruction of
 * the GlobalRef).
 */
struct JavaIdentityHash;
struct JavaIdentityEquals;
struct JavaProxyCacheTraits {
    using UnowningImplPointer = jobject;
    using OwningImplPointer = jobject;
    using OwningProxyPointer = std::shared_ptr<void>;
    using WeakProxyPointer = std::weak_ptr<void>;
    using UnowningImplPointerHash = JavaIdentityHash;
    using UnowningImplPointerEqual = JavaIdentityEquals;
};
extern template class ProxyCache<JavaProxyCacheTraits>;
using JavaProxyCache = ProxyCache<JavaProxyCacheTraits>;
template <typename T> using JavaProxyHandle = JavaProxyCache::Handle<GlobalRef<jobject>, T>;

/*
 * Cache for CppProxy objects. This is the inverse of the JavaProxyCache mechanism above,
 * ensuring that each time we pass an interface from Java to C++, we get the *same* CppProxy
 * object on the Java side:
 *
 *      Java               |            C++
 *                         |
 *    ______________       |         ________________                  ___________
 *   |              |      |        |                |                |           |
 *   | Foo.CppProxy | ------------> | CppProxyHandle | =============> |    Foo    |
 *   |______________|   (jlong)     |      <Foo>     |  (shared_ptr)  |___________|
 *           ^             |        |________________|
 *            \            |
 *        _________        |                     __________________
 *       |         |       |                    |                  |
 *       | WeakRef | <------------------------- | jniCppProxyCache |
 *       |_________|  (GlobalRef)               |__________________|
 *                         |
 *
 * We don't use JNI WeakGlobalRef objects, because they last longer than is safe - a
 * WeakGlobalRef can still be upgraded to a strong reference even during finalization, which
 * leads to use-after-free. Java WeakRefs provide the right lifetime guarantee.
 */
class JavaWeakRef;
struct JniCppProxyCacheTraits {
    using UnowningImplPointer = void *;
    using OwningImplPointer = std::shared_ptr<void>;
    using OwningProxyPointer = jobject;
    using WeakProxyPointer = JavaWeakRef;
    using UnowningImplPointerHash = std::hash<void *>;
    using UnowningImplPointerEqual = std::equal_to<void *>;
};
extern template class ProxyCache<JniCppProxyCacheTraits>;
using JniCppProxyCache = ProxyCache<JniCppProxyCacheTraits>;
template <class T> using CppProxyHandle = JniCppProxyCache::Handle<std::shared_ptr<T>>;

template <class T>
static const std::shared_ptr<T> & objectFromHandleAddress(jlong handle) {
    assert(handle);
    assert(handle > 4096);
    // Below line segfaults gcc-4.8. Using a temporary variable hides the bug.
    //const auto & ret = reinterpret_cast<const CppProxyHandle<T> *>(handle)->get();
    const CppProxyHandle<T> *proxy_handle =
        reinterpret_cast<const CppProxyHandle<T> *>(handle);
    const auto & ret = proxy_handle->get();
    assert(ret);
    return ret;
}

/*
 * Information needed to use a CppProxy class.
 *
 * In an ideal world, this object would be properly always-valid RAII, and we'd use an
 * optional<CppProxyClassInfo> where needed. Unfortunately we don't want to depend on optional
 * here, so this object has an invalid state and default constructor.
 */
struct CppProxyClassInfo {
    const GlobalRef<jclass> clazz;
    const jmethodID constructor;
    const jfieldID idField;

    CppProxyClassInfo(const char * className);
    CppProxyClassInfo();
    ~CppProxyClassInfo();

    // Validity check
    explicit operator bool() const { return bool(clazz); }
};

/*
 * Base class for Java <-> C++ interface adapters.
 *
 * I is the C++ base class (interface) being adapted; Self is the interface adapter class
 * derived from JniInterface (using CRTP). For example:
 *
 *     class NativeToken final : djinni::JniInterface<Token, NativeToken> { ... }
 */
template <class I, class Self>
class JniInterface {
public:
    /*
     * Given a C++ object, find or create a Java version. The cases here are:
     * 1. Null
     * 2. The provided C++ object is actually a JavaProxy (C++-side proxy for Java impl)
     * 3. The provided C++ object has an existing CppProxy (Java-side proxy for C++ impl)
     * 4. The provided C++ object needs a new CppProxy allocated
     */
    jobject _toJava(JNIEnv* jniEnv, const std::shared_ptr<I> & c) const {
        // Case 1 - null
        if (!c) {
            return nullptr;
        }

        // Case 2 - already a JavaProxy. Only possible if Self::JavaProxy exists.
        if (jobject impl = _unwrapJavaProxy<Self>(&c)) {
            return jniEnv->NewLocalRef(impl);
        }

        // Cases 3 and 4.
        assert(m_cppProxyClass);
        return JniCppProxyCache::get(typeid(c), c, &newCppProxy);

    }

    /*
     * Given a Java object, find or create a C++ version. The cases here are:
     * 1. Null
     * 2. The provided Java object is actually a CppProxy (Java-side proxy for a C++ impl)
     * 3. The provided Java object has an existing JavaProxy (C++-side proxy for a Java impl)
     * 4. The provided Java object needs a new JavaProxy allocated
     */
    std::shared_ptr<I> _fromJava(JNIEnv* jniEnv, jobject j) const {
        // Case 1 - null
        if (!j) {
            return nullptr;
        }

        // Case 2 - already a Java proxy; we just need to pull the C++ impl out. (This case
        // is only possible if we were constructed with a cppProxyClassName parameter.)
        if (m_cppProxyClass
                && jniEnv->IsSameObject(jniEnv->GetObjectClass(j), m_cppProxyClass.clazz.get())) {
            jlong handle = jniEnv->GetLongField(j, m_cppProxyClass.idField);
            jniExceptionCheck(jniEnv);
            return objectFromHandleAddress<I>(handle);
        }

        // Cases 3 and 4 - see _getJavaProxy helper below. JavaProxyCache is responsible for
        // distinguishing between the two cases. Only possible if Self::JavaProxy exists.
        return _getJavaProxy<Self>(j);
    }

    // Constructor for interfaces for which a Java-side CppProxy class exists
    JniInterface(const char * cppProxyClassName) : m_cppProxyClass(cppProxyClassName) {}

    // Constructor for interfaces without a Java proxy class
    JniInterface() : m_cppProxyClass{} {}

private:
    /*
     * Helpers for _toJava above. The possibility that an object is already a C++-side proxy
     * only exists if the code generator emitted one (if Self::JavaProxy exists).
     */
    template <typename S, typename JavaProxy = typename S::JavaProxy>
    jobject _unwrapJavaProxy(const std::shared_ptr<I> * c) const {
        if (auto proxy = dynamic_cast<JavaProxy *>(c->get())) {
            return proxy->JavaProxyHandle<JavaProxy>::get().get();
        } else {
            return nullptr;
        }
    }

    template <typename S>
    jobject _unwrapJavaProxy(...) const {
        return nullptr;
    }

    /*
     * Helper for _toJava above: given a C++ object, allocate a CppProxy on the Java side for
     * it. This is actually called by jniCppProxyCacheGet, which holds a lock on the global
     * C++-to-Java proxy map object.
     */
    static std::pair<jobject, void*> newCppProxy(const std::shared_ptr<void> & cppObj) {
        const auto & data = JniClass<Self>::get();
        const auto & jniEnv = jniGetThreadEnv();
        std::unique_ptr<CppProxyHandle<I>> to_encapsulate(
                new CppProxyHandle<I>(std::static_pointer_cast<I>(cppObj)));
        jlong handle = static_cast<jlong>(reinterpret_cast<uintptr_t>(to_encapsulate.get()));
        jobject cppProxy = jniEnv->NewObject(data.m_cppProxyClass.clazz.get(),
                                             data.m_cppProxyClass.constructor,
                                             handle);
        jniExceptionCheck(jniEnv);
        to_encapsulate.release();
        return { cppProxy, cppObj.get() };
    }

    /*
     * Helpers for _fromJava above. We can only produce a C++-side proxy if the code generator
     * emitted one (if Self::JavaProxy exists).
     */
    template <typename S, typename JavaProxy = typename S::JavaProxy>
    std::shared_ptr<I> _getJavaProxy(jobject j) const {
        static_assert(std::is_base_of<JavaProxyHandle<JavaProxy>, JavaProxy>::value,
            "JavaProxy must derive from JavaProxyCacheEntry");

        return std::static_pointer_cast<JavaProxy>(JavaProxyCache::get(
            typeid(JavaProxy), j,
            [] (const jobject & obj) -> std::pair<std::shared_ptr<void>, jobject> {
                auto ret = std::make_shared<JavaProxy>(obj);
                return { ret, ret->JavaProxyHandle<JavaProxy>::get().get() };
            }
        ));
    }

    template <typename S>
    std::shared_ptr<I> _getJavaProxy(...) const {
        assert(false);
        return nullptr;
    }

    const CppProxyClassInfo m_cppProxyClass;
};

/*
 * Guard object which automatically begins and ends a JNI local frame when
 * it is created and destroyed, using PushLocalFrame and PopLocalFrame.
 *
 * Local frame creation can fail. The throwOnError parameter specifies how
 * errors are reported:
 * - true (default): throws on failure
 * - false: queues a JNI exception on failure; the user must call checkSuccess()
 *
 * The JNIEnv given at construction is expected to still be valid at
 * destruction, so this class isn't suitable for use across threads.
 * It is intended for use on the stack.
 *
 * All JNI local references created within the defined scope will be
 * released at the end of the scope.  This class doesn't support
 * the jobject return value supported by PopLocalFrame(), because
 * the destructor cannot return the new reference value for the parent
 * frame.
 */
class JniLocalScope {
public:
    /*
     * Create the guard object and begin the local frame.
     *
     * @param p_env the JNIEnv for the current thread.
     * @param capacity the initial number of local references to
     *  allocate.
     */
    JniLocalScope(JNIEnv* p_env, jint capacity, bool throwOnError = true);
    bool checkSuccess() const { return m_success; }
    ~JniLocalScope();
private:
    JniLocalScope(const JniLocalScope& other);
    JniLocalScope& operator=(const JniLocalScope& other);

    static bool _pushLocalFrame(JNIEnv* const env, jint capacity);
    static void _popLocalFrame(JNIEnv* const env, jobject returnRef);

    JNIEnv* const m_env;
    const bool m_success;
};

jstring jniStringFromUTF8(JNIEnv * env, const std::string & str);
std::string jniUTF8FromString(JNIEnv * env, const jstring jstr);

jstring jniStringFromWString(JNIEnv * env, const std::wstring & str);
std::wstring jniWStringFromString(JNIEnv * env, const jstring jstr);

class JniEnum {
public:
    /*
     * Given a Java object, find its numeric value. This returns a jint, which the caller can
     * static_cast<> into the necessary C++ enum type.
     */
    jint ordinal(JNIEnv * env, jobject obj) const;

    /*
     * Create a Java value of the wrapped class with the given value.
     */
    LocalRef<jobject> create(JNIEnv * env, jint value) const;

protected:
    JniEnum(const std::string & name);
    jclass enumClass() const { return m_clazz.get(); }

private:
    const GlobalRef<jclass> m_clazz;
    const jmethodID m_staticmethValues;
    const jmethodID m_methOrdinal;
};

class JniFlags : private JniEnum {
public:
    /*
     * Given a Java EnumSet convert it to the corresponding bit pattern
     * which can then be static_cast<> to the actual enum.
     */
    unsigned flags(JNIEnv * env, jobject obj) const;

    /*
     * Create a Java EnumSet of the specified flags considering the given number of active bits.
     */
    LocalRef<jobject> create(JNIEnv * env, unsigned flags, int bits) const;

    using JniEnum::create;

protected:
    JniFlags(const std::string & name);

private:
    const GlobalRef<jclass> m_clazz { jniFindClass("java/util/EnumSet") };
    const jmethodID m_methNoneOf { jniGetStaticMethodID(m_clazz.get(), "noneOf", "(Ljava/lang/Class;)Ljava/util/EnumSet;") };
    const jmethodID m_methAdd { jniGetMethodID(m_clazz.get(), "add", "(Ljava/lang/Object;)Z") };
    const jmethodID m_methIterator { jniGetMethodID(m_clazz.get(), "iterator", "()Ljava/util/Iterator;") };
    const jmethodID m_methSize { jniGetMethodID(m_clazz.get(), "size", "()I") };

    struct {
        const GlobalRef<jclass> clazz { jniFindClass("java/util/Iterator") };
        const jmethodID methNext { jniGetMethodID(clazz.get(), "next", "()Ljava/lang/Object;") };
    } m_iterator;
};

#define DJINNI_FUNCTION_PROLOGUE0(env_)
#define DJINNI_FUNCTION_PROLOGUE1(env_, arg1_)

/*
 * Helper for JNI_TRANSLATE_EXCEPTIONS_RETURN.
 *
 * Must be called in a catch block.  Responsible for setting the pending
 * exception in JNI based on the current C++ exception.
 *
 * The default implementation is defined with __attribute__((weak)) so you
 * can replace it by defining your own version.  The default implementation
 * will call jniDefaultSetPendingFromCurrent(), which will propagate a
 * jni_exception directly into Java, or throw a RuntimeException for any
 * other std::exception.
 */
void jniSetPendingFromCurrent(JNIEnv * env, const char * ctx) noexcept;

/*
 * Helper for JNI_TRANSLATE_EXCEPTIONS_RETURN.
 *
 * Must be called in a catch block.  Responsible for setting the pending
 * exception in JNI based on the current C++ exception.
 *
 * This will call jniSetPendingFrom(env, jni_exception) if the current exception
 * is a jni_exception, or otherwise will set a RuntimeException from any
 * other std::exception.  Any non-std::exception will result in a call
 * to terminate().
 *
 * This is called by the default implementation of jniSetPendingFromCurrent.
 */
void jniDefaultSetPendingFromCurrent(JNIEnv * env, const char * ctx) noexcept;

/* Catch C++ exceptions and translate them to Java exceptions.
 *
 * All functions called by Java must be fully wrapped by an outer try...catch block like so:
 *
 * try {
 *     ...
 * } JNI_TRANSLATE_EXCEPTIONS_RETURN(env, 0)
 * ... or JNI_TRANSLATE_EXCEPTIONS_RETURN(env, ) for functions returning void
 *
 * The second parameter is a default return value to be used if an exception is caught and
 * converted. (For JNI outer-layer calls, this result will always be ignored by JNI, so
 * it can safely be 0 for any function with a non-void return value.)
 */
#define JNI_TRANSLATE_EXCEPTIONS_RETURN(env, ret) \
    catch (const std::exception &) { \
        ::djinni::jniSetPendingFromCurrent(env, __func__); \
        return ret; \
    }

/* Catch jni_exception and translate it back to a Java exception, without catching
 * any other C++ exceptions.  Can be used to wrap code which might cause JNI
 * exceptions like so:
 *
 * try {
 *     ...
 * } JNI_TRANSLATE_JAVA_EXCEPTIONS_RETURN(env, 0)
 * ... or JNI_TRANSLATE_JAVA_EXCEPTIONS_RETURN(env, ) for functions returning void
 *
 * The second parameter is a default return value to be used if an exception is caught and
 * converted. (For JNI outer-layer calls, this result will always be ignored by JNI, so
 * it can safely be 0 for any function with a non-void return value.)
 */
#define JNI_TRANSLATE_JNI_EXCEPTIONS_RETURN(env, ret) \
    catch (const ::djinni::jni_exception & e) { \
        e.set_as_pending(env); \
        return ret; \
    }

} // namespace djinni
