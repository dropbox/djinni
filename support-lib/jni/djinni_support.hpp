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
#include <unordered_map>

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
 * Exception to indicate that a Java exception is pending in the JVM.
 */
class jni_exception_pending : public std::exception {};

/*
 * Throw jni_exception_pending if any Java exception is pending in the JVM.
 */
void jniExceptionCheck(JNIEnv * env);
/*
 * Set an AssertionError in env with message message, and then throw jni_exception_pending.
 */
__attribute__((noreturn))
void jniThrowAssertionError(JNIEnv * env, const char * file, int line, const char * check);

#define DJINNI_ASSERT(check, env) \
    do { \
        djinni::jniExceptionCheck(env); \
        const bool check__res = bool(check); \
        djinni::jniExceptionCheck(env); \
        if (!check__res) { \
            djinni::jniThrowAssertionError(env, __FILE__, __LINE__, #check); \
        } \
    } while(false)
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
    GlobalRef(JNIEnv * env, PointerType localRef)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, GlobalRefDeleter>(
            static_cast<PointerType>(env->NewGlobalRef(localRef)),
            GlobalRefDeleter{}
        ) {}
};

struct LocalRefDeleter { void operator() (jobject localRef) noexcept; };

template <typename PointerType>
class LocalRef : public std::unique_ptr<typename std::remove_pointer<PointerType>::type,
                                        LocalRefDeleter> {
public:
    LocalRef() {}
    LocalRef(JNIEnv * /*env*/, PointerType localRef)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, LocalRefDeleter>(
            localRef) {}
    explicit LocalRef(PointerType localRef)
        : std::unique_ptr<typename std::remove_pointer<PointerType>::type, LocalRefDeleter>(
            localRef) {}
};

/*
 * Helper for JniClassInitializer. Copied from Oxygen.
 */
template <class Key, class T>
class static_registration {
public:
    using registration_map = std::unordered_map<Key, T *>;
    static registration_map get_all() {
        const std::lock_guard<std::mutex> lock(get_mutex());
        return get_map();
    }
    static_registration(const Key & key, T * obj) : m_key(key) {
        const std::lock_guard<std::mutex> lock(get_mutex());
        get_map().emplace(key, obj);
    }
    ~static_registration() {
        const std::lock_guard<std::mutex> lock(get_mutex());
        get_map().erase(m_key);
    }
private:
    const Key m_key;
    static registration_map & get_map()   { static registration_map m; return m;   }
    static std::mutex       & get_mutex() { static std::mutex mtx;     return mtx; }
};

/*
 * Helper for JniClass. (This can't be a subclass because it needs to not be templatized.)
 */
class JniClassInitializer {
private:
    using Registration = static_registration<void *, const JniClassInitializer>;
    const std::function<void()> init;
    const Registration reg;
    JniClassInitializer(const std::function<void()> & init) : init(init), reg(this, this) {}
    template <class C> friend class JniClass;
    friend void jniInit(JavaVM *);
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
const JniClassInitializer JniClass<C>::s_initializer { allocate };

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
 * JavaProxyCache<T>::get(jobj, ...) the first time will construct a T and return a
 * shared_ptr to it, and also save a weak_ptr to the new object internally. The constructed
 * T contains a strong GlobalRef to jobj. As long as something in C++ maintains a strong
 * reference to the wrapper, future calls to get(jobj) will return the *same* wrapper object.
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
 *                        |                   weak_ptr   | <JniImplFooListener> |
 *                        |                              |______________________|
 *
 * As long as the C++ FooListener has references, the Java FooListener is kept alive.
 *
 * We use a custom unordered_map with Java objects (jobject) as keys, and JNI object
 * identity and hashing functions. This means that as long as a key is in the map,
 * we must have some other GlobalRef keeping it alive. To ensure safety, the Entry
 * destructor removes *itself* from the map - destruction order guarantees that this
 * will happen before the contained global reference becomes invalid (by destruction of
 * the GlobalRefGuard).
 */

/*
 * Look up an entry in the global JNI wrapper cache. If none is found, create one with factory,
 * save it, and return it.
 *
 * The contract of `factory` is: The parameter to factory is a local ref. The factory returns
 * a shared_ptr to the object (JniImplFooListener, in the diagram above), as well as the
 * jobject *global* ref contained inside.
 */
std::shared_ptr<void> javaProxyCacheLookup(jobject obj, std::pair<std::shared_ptr<void>,
                                                                  jobject>(*factory)(jobject));

class JavaProxyCacheEntry {
public:
    jobject getGlobalRef() {
        return m_globalRef.get();
    }

protected:
    JavaProxyCacheEntry(jobject localRef, JNIEnv * env); // env used only for construction
    JavaProxyCacheEntry(jobject localRef);

    virtual ~JavaProxyCacheEntry() noexcept;

    JavaProxyCacheEntry(const JavaProxyCacheEntry & other) = delete;
    JavaProxyCacheEntry & operator=(const JavaProxyCacheEntry & other) = delete;

private:
    const GlobalRef<jobject> m_globalRef;
};

template <class T>
class JavaProxyCache {
public:
    using Entry = JavaProxyCacheEntry;

    static std::pair<std::shared_ptr<void>, jobject> factory(jobject obj) {
        std::shared_ptr<T> ret = std::make_shared<T>(obj);
        return { ret, ret->getGlobalRef() };
    }

    /*
     * Check whether a wrapped T exists for obj. If one is found, return it; if not,
     * construct a new one with obj, save it, and return it.
     */
    static std::shared_ptr<T> get(jobject obj) {
        static_assert(std::is_base_of<JavaProxyCacheEntry, T>::value,
            "JavaProxyCache can only be used with T if T derives from Entry<T>");

        return std::static_pointer_cast<T>(javaProxyCacheLookup(obj, &factory));
    }
};

template <class I>
class JniInterfaceCppExt {
public:
    jobject _toJava(JNIEnv* jniEnv, const std::shared_ptr<I> & c) const {
        if (c == nullptr) {
            return 0;
        }
        std::unique_ptr<std::shared_ptr<I>> to_encapsulate(new std::shared_ptr<I>(c));
        jlong shared_ptr_val = static_cast<jlong>(reinterpret_cast<uintptr_t>(to_encapsulate.get()));
        jobject nativeProxy = jniEnv->NewObject(nativeProxyClass.get(), nativeProxyConstructor, shared_ptr_val);
        jniExceptionCheck(jniEnv);
        to_encapsulate.release();
        return nativeProxy;
    }

    std::shared_ptr<I> _fromJava(JNIEnv* jniEnv, jobject j) const {
        if (j == 0) {
            return nullptr;
        }
        jlong packed_raw_shared_ptr = jniEnv->GetLongField(j, nativeProxyField);
        jniExceptionCheck(jniEnv);
        return *reinterpret_cast<const std::shared_ptr<I> *>(packed_raw_shared_ptr);
    }

    JniInterfaceCppExt(const char* nativeProxyClassName) :
        nativeProxyClass(jniFindClass(nativeProxyClassName)),
        nativeProxyConstructor(jniGetMethodID(nativeProxyClass.get(), "<init>", "(J)V")),
        nativeProxyField(jniGetFieldID(nativeProxyClass.get(), "nativeRef", "J"))
    {}

    const GlobalRef<jclass> nativeProxyClass;
    const jmethodID nativeProxyConstructor;
    const jfieldID nativeProxyField;
};

template <class I, class Self>
class JniInterfaceJavaExt {
public:
    std::shared_ptr<I> _fromJava(JNIEnv* /*jniEnv*/, jobject j) const {
        if (j == 0) {
            return nullptr;
        }
        return JavaProxyCache<typename Self::JavaProxy>::get(j);
    }
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

#ifdef DBX_JNI_LOCAL_FRAME_DEBUG
    static int s_frameCount;
#endif

    JNIEnv* const m_env;
    const bool m_success;
};

jstring jniStringFromUTF8(JNIEnv * env, const std::string & str);
std::string jniUTF8FromString(JNIEnv * env, const jstring jstr);

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

private:
    const GlobalRef<jclass> m_clazz;
    const jmethodID m_staticmethValues;
    const jmethodID m_methOrdinal;
};

#define DJINNI_FUNCTION_PROLOGUE0(env_)
#define DJINNI_FUNCTION_PROLOGUE1(env_, arg1_)

// Helper for JNI_TRANSLATE_EXCEPTIONS_RETURN. Do not call directly.
void jniSetPendingFromCurrent(JNIEnv * env, const char * ctx) noexcept;

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

} // namespace djinni
