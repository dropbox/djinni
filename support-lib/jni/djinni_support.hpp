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

// work-around for missing noexcept and constexpr support in MSVC prior to 2015
#if (defined _MSC_VER) && (_MSC_VER < 1900)
#  define noexcept _NOEXCEPT
#  define constexpr
#endif

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
#ifdef _MSC_VER
  __declspec(noreturn)
#else
  __attribute__((noreturn))
#endif
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
	operator PointerType() const { return this->get(); }
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
 * Proxy cache implementation. These functions are used by CppProxyHandle::~CppProxyHandle()
 * and JniInterface::_toJava, respectively. They're declared in a separate class to avoid
 * having to templatize them. This way, all the map lookup code is only generated once,
 * rather than once for each T, saving substantially on binary size. (We do something simiar
 * in the other direction too; see javaProxyCacheLookup() above.)
 *
 * The data used by this class is declared only in djinni_support.cpp, since it's global and
 * opaque to all other code.
 */
class JniCppProxyCache {
private:
    template <class T> friend class CppProxyHandle;
    static void erase(void * key);

    template <class I, class Self> friend class JniInterface;
    static jobject get(const std::shared_ptr<void> & cppObj,
                       JNIEnv * jniEnv,
                       const CppProxyClassInfo & proxyClass,
                       jobject (*factory)(const std::shared_ptr<void> &,
                                          JNIEnv *,
                                          const CppProxyClassInfo &));

    /* This "class" is basically a namespace, to make clear that get() and erase() should only
     * be used by the helper infrastructure below. */
    JniCppProxyCache() = delete;
};

template <class T>
class CppProxyHandle {
public:
    CppProxyHandle(std::shared_ptr<T> obj) : m_obj(move(obj)) {}
    ~CppProxyHandle() {
        JniCppProxyCache::erase(m_obj.get());
    }

    static const std::shared_ptr<T> & get(jlong handle) {
        return reinterpret_cast<const CppProxyHandle<T> *>(handle)->m_obj;
    }

private:
    const std::shared_ptr<T> m_obj;
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
        return JniCppProxyCache::get(c, jniEnv, m_cppProxyClass, &newCppProxy);
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
            return CppProxyHandle<I>::get(handle);
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
    template <typename S, typename = typename S::JavaProxy>
    jobject _unwrapJavaProxy(const std::shared_ptr<I> * c) const {
        if (auto proxy = dynamic_cast<typename S::JavaProxy *>(c->get())) {
            return proxy->getGlobalRef();
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
    static jobject newCppProxy(const std::shared_ptr<void> & cppObj,
                               JNIEnv * jniEnv,
                               const CppProxyClassInfo & proxyClass) {
        std::unique_ptr<CppProxyHandle<I>> to_encapsulate(
                new CppProxyHandle<I>(std::static_pointer_cast<I>(cppObj)));
        jlong handle = static_cast<jlong>(reinterpret_cast<uintptr_t>(to_encapsulate.get()));
        jobject cppProxy = jniEnv->NewObject(proxyClass.clazz.get(),
                                             proxyClass.constructor,
                                             handle);
        jniExceptionCheck(jniEnv);
        to_encapsulate.release();
        return cppProxy;
    }

    /*
     * Helpers for _fromJava above. We can only produce a C++-side proxy if the code generator
     * emitted one (if Self::JavaProxy exists).
     */
    template <typename S, typename = typename S::JavaProxy>
    std::shared_ptr<I> _getJavaProxy(jobject j) const {
        return JavaProxyCache<typename S::JavaProxy>::get(j);
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
