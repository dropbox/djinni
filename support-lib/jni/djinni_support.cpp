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

#include "../djinni_common.hpp"
#include "djinni_support.hpp"
#include "../proxy_cache_impl.hpp"
#include <cassert>
#include <cstdlib>
#include <cstring>

static_assert(sizeof(jlong) >= sizeof(void*), "must be able to fit a void* into a jlong");

namespace djinni {

// Set only once from JNI_OnLoad before any other JNI calls, so no lock needed.
static JavaVM * g_cachedJVM;

/*static*/
JniClassInitializer::registration_vec & JniClassInitializer::get_vec() {
    static JniClassInitializer::registration_vec m;
    return m;
}

/*static*/
std::mutex & JniClassInitializer::get_mutex() {
    static std::mutex mtx;
    return mtx;
}

/*static*/
JniClassInitializer::registration_vec JniClassInitializer::get_all() {
    const std::lock_guard<std::mutex> lock(get_mutex());
    return get_vec();
}

JniClassInitializer::JniClassInitializer(std::function<void()> init) {
    const std::lock_guard<std::mutex> lock(get_mutex());
    get_vec().push_back(std::move(init));
}

void jniInit(JavaVM * jvm) {
    g_cachedJVM = jvm;

    try {
        for (const auto & initializer : JniClassInitializer::get_all()) {
            initializer();
        }
    } catch (const std::exception &) {
        // Default exception handling only, since non-default might not be safe if init
        // is incomplete.
        jniDefaultSetPendingFromCurrent(jniGetThreadEnv(), __func__);
    }
}

void jniShutdown() {
    g_cachedJVM = nullptr;
}

JNIEnv * jniGetThreadEnv() {
    assert(g_cachedJVM);
    JNIEnv * env = nullptr;
    const jint get_res = g_cachedJVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if (get_res != 0 || !env) {
        // :(
        std::abort();
    }

    return env;
}

static JNIEnv * getOptThreadEnv() {
    if (!g_cachedJVM) {
        return nullptr;
    }

    // Special case: this allows us to ignore GlobalRef deletions that happen after this
    // thread has been detached. (This is known to happen during process shutdown, when
    // there's no need to release the ref anyway.)
    JNIEnv * env = nullptr;
    const jint get_res = g_cachedJVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);

    if (get_res == JNI_EDETACHED) {
        return nullptr;
    }

    // Still bail on any other error.
    if (get_res != 0 || !env) {
        // :(
        std::abort();
    }

    return env;
}

void GlobalRefDeleter::operator() (jobject globalRef) noexcept {
    if (globalRef) {
        if (JNIEnv * env = getOptThreadEnv()) {
            env->DeleteGlobalRef(globalRef);
        }
    }
}

void LocalRefDeleter::operator() (jobject localRef) noexcept {
    if (localRef) {
        jniGetThreadEnv()->DeleteLocalRef(localRef);
    }
}

void jni_exception::set_as_pending(JNIEnv * env) const noexcept {
    assert(env);
    env->Throw(java_exception());
}


void jniExceptionCheck(JNIEnv * env) {
    if (!env) {
        abort();
    }
    const LocalRef<jthrowable> e(env->ExceptionOccurred());
    if (e) {
        env->ExceptionClear();
        jniThrowCppFromJavaException(env, e.get());
    }
}

DJINNI_WEAK_DEFINITION
DJINNI_NORETURN_DEFINITION
void jniThrowCppFromJavaException(JNIEnv * env, jthrowable java_exception) {
    throw jni_exception { env, java_exception };
}

namespace { // anonymous namespace to guard the struct below
struct SystemClassInfo {
    // This is a singleton class - an instance will be constructed by
    // JniClassInitializer::init_all() at library init time.
    const GlobalRef<jclass> clazz { jniFindClass("java/lang/System") };
    const jmethodID staticmethIdentityHashCode { jniGetStaticMethodID(clazz.get(),
            "identityHashCode", "(Ljava/lang/Object;)I") };
};
} // namespace

/*
 * Hasher and comparator based on Java object identity.
 */
struct JavaIdentityHash { size_t operator() (jobject obj) const; };
struct JavaIdentityEquals { bool operator() (jobject obj1, jobject obj2) const; };

size_t JavaIdentityHash::operator() (jobject obj) const {
    JNIEnv * const env = jniGetThreadEnv();
    const SystemClassInfo & sys = JniClass<SystemClassInfo>::get();
    jint res = env->CallStaticIntMethod(sys.clazz.get(), sys.staticmethIdentityHashCode, obj);
    jniExceptionCheck(env);
    return res;
}
bool JavaIdentityEquals::operator() (jobject obj1, jobject obj2) const {
    JNIEnv * const env = jniGetThreadEnv();
    const bool res = env->IsSameObject(obj1, obj2);
    jniExceptionCheck(env);
    return res;
}

void jniThrowAssertionError(JNIEnv * env, const char * file, int line, const char * check) {
    // basename() exists, but is bad (it's allowed to modify its input).
    const char * slash = strrchr(file, '/');
    const char * file_basename = slash ? slash + 1 : file;

    char buf[256];
    DJINNI_SNPRINTF(buf, sizeof buf, "djinni (%s:%d): %s", file_basename, line, check);

    const jclass cassert = env->FindClass("java/lang/Error");
    assert(cassert);
    env->ThrowNew(cassert, buf);
    assert(env->ExceptionCheck());
    const jthrowable e = env->ExceptionOccurred();
    assert(e);
    env->ExceptionClear();

    env->DeleteLocalRef(cassert);

    jniThrowCppFromJavaException(env, e);
}

GlobalRef<jclass> jniFindClass(const char * name) {
    JNIEnv * env = jniGetThreadEnv();
    DJINNI_ASSERT(name, env);
    GlobalRef<jclass> guard(env, LocalRef<jclass>(env, env->FindClass(name)).get());
    jniExceptionCheck(env);
    if (!guard) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "FindClass returned null");
    }
    return guard;
}

jmethodID jniGetStaticMethodID(jclass clazz, const char * name, const char * sig) {
    JNIEnv * env = jniGetThreadEnv();
    DJINNI_ASSERT(clazz, env);
    DJINNI_ASSERT(name, env);
    DJINNI_ASSERT(sig, env);
    jmethodID id = env->GetStaticMethodID(clazz, name, sig);
    jniExceptionCheck(env);
    if (!id) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "GetStaticMethodID returned null");
    }
    return id;
}

jmethodID jniGetMethodID(jclass clazz, const char * name, const char * sig) {
    JNIEnv * env = jniGetThreadEnv();
    DJINNI_ASSERT(clazz, env);
    DJINNI_ASSERT(name, env);
    DJINNI_ASSERT(sig, env);
    jmethodID id = env->GetMethodID(clazz, name, sig);
    jniExceptionCheck(env);
    if (!id) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "GetMethodID returned null");
    }
    return id;
}

jfieldID jniGetFieldID(jclass clazz, const char * name, const char * sig) {
    JNIEnv * env = jniGetThreadEnv();
    DJINNI_ASSERT(clazz, env);
    DJINNI_ASSERT(name, env);
    DJINNI_ASSERT(sig, env);
    jfieldID id = env->GetFieldID(clazz, name, sig);
    jniExceptionCheck(env);
    if (!id) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "GetFieldID returned null");
    }
    return id;
}

JniEnum::JniEnum(const std::string & name)
    : m_clazz { jniFindClass(name.c_str()) },
      m_staticmethValues { jniGetStaticMethodID(m_clazz.get(), "values", ("()[L" + name + ";").c_str()) },
      m_methOrdinal { jniGetMethodID(m_clazz.get(), "ordinal", "()I") }
    {}

jint JniEnum::ordinal(JNIEnv * env, jobject obj) const {
    DJINNI_ASSERT(obj, env);
    const jint res = env->CallIntMethod(obj, m_methOrdinal);
    jniExceptionCheck(env);
    return res;
}

LocalRef<jobject> JniEnum::create(JNIEnv * env, jint value) const {
    LocalRef<jobject> values(env, env->CallStaticObjectMethod(m_clazz.get(), m_staticmethValues));
    jniExceptionCheck(env);
    DJINNI_ASSERT(values, env);
    LocalRef<jobject> result(env,
                             env->GetObjectArrayElement(static_cast<jobjectArray>(values.get()),
                                                        value));
    jniExceptionCheck(env);
    return result;
}

JniFlags::JniFlags(const std::string & name)
    : JniEnum { name }
    {}

unsigned JniFlags::flags(JNIEnv * env, jobject obj) const {
    DJINNI_ASSERT(obj && env->IsInstanceOf(obj, m_clazz.get()), env);
    auto size = env->CallIntMethod(obj, m_methSize);
    jniExceptionCheck(env);
    unsigned flags = 0;
    auto it = LocalRef<jobject>(env, env->CallObjectMethod(obj, m_methIterator));
    jniExceptionCheck(env);
    for(jint i = 0; i < size; ++i) {
        auto jf = LocalRef<jobject>(env, env->CallObjectMethod(it, m_iterator.methNext));
        jniExceptionCheck(env);
        flags |= (1u << static_cast<unsigned>(ordinal(env, jf)));
    }
    return flags;
}

LocalRef<jobject> JniFlags::create(JNIEnv * env, unsigned flags, int bits) const {
    auto j = LocalRef<jobject>(env, env->CallStaticObjectMethod(m_clazz.get(), m_methNoneOf, enumClass()));
    jniExceptionCheck(env);
    unsigned mask = 1;
    for(int i = 0; i < bits; ++i, mask <<= 1) {
        if((flags & mask) != 0) {
            auto jf = create(env, static_cast<jint>(i));
            jniExceptionCheck(env);
            env->CallBooleanMethod(j, m_methAdd, jf.get());
            jniExceptionCheck(env);
        }
    }
    return j;
}

JniLocalScope::JniLocalScope(JNIEnv* p_env, jint capacity, bool throwOnError)
    : m_env(p_env)
    , m_success(_pushLocalFrame(m_env, capacity)) {
    if (throwOnError) {
        DJINNI_ASSERT(m_success, m_env);
    }
}
JniLocalScope::~JniLocalScope() {
    if (m_success) {
        _popLocalFrame(m_env, nullptr);
    }
}

bool JniLocalScope::_pushLocalFrame(JNIEnv* const env, jint capacity) {
    DJINNI_ASSERT(capacity >= 0, env);
    const jint push_res = env->PushLocalFrame(capacity);
    return 0 == push_res;
}

void JniLocalScope::_popLocalFrame(JNIEnv* const env, jobject returnRef) {
    env->PopLocalFrame(returnRef);
}

/*
 * UTF-8 and UTF-16 conversion functions from miniutf: https://github.com/dropbox/miniutf
 */

struct offset_pt {
    int offset;
    char32_t pt;
};

static constexpr const offset_pt invalid_pt = { -1, 0 };

/*
 * Decode a codepoint starting at str[i], and return the number of code units (bytes, for
 * UTF-8) consumed and the result. If no valid codepoint is at str[i], return invalid_pt.
 */
static offset_pt utf8_decode_check(const std::string & str, std::string::size_type i) {
    uint32_t b0, b1, b2, b3;

    b0 = static_cast<unsigned char>(str[i]);

    if (b0 < 0x80) {
        // 1-byte character
        return { 1, b0 };
    } else if (b0 < 0xC0) {
        // Unexpected continuation byte
        return invalid_pt;
    } else if (b0 < 0xE0) {
        // 2-byte character
        if (((b1 = str[i+1]) & 0xC0) != 0x80)
            return invalid_pt;

        char32_t pt = (b0 & 0x1F) << 6 | (b1 & 0x3F);
        if (pt < 0x80)
            return invalid_pt;

        return { 2, pt };
    } else if (b0 < 0xF0) {
        // 3-byte character
        if (((b1 = str[i+1]) & 0xC0) != 0x80)
            return invalid_pt;
        if (((b2 = str[i+2]) & 0xC0) != 0x80)
            return invalid_pt;

        char32_t pt = (b0 & 0x0F) << 12 | (b1 & 0x3F) << 6 | (b2 & 0x3F);
        if (pt < 0x800)
            return invalid_pt;

        return { 3, pt };
    } else if (b0 < 0xF8) {
        // 4-byte character
        if (((b1 = str[i+1]) & 0xC0) != 0x80)
            return invalid_pt;
        if (((b2 = str[i+2]) & 0xC0) != 0x80)
            return invalid_pt;
        if (((b3 = str[i+3]) & 0xC0) != 0x80)
            return invalid_pt;

        char32_t pt = (b0 & 0x0F) << 18 | (b1 & 0x3F) << 12
                    | (b2 & 0x3F) << 6  | (b3 & 0x3F);
        if (pt < 0x10000 || pt >= 0x110000)
            return invalid_pt;

        return { 4, pt };
    } else {
        // Codepoint out of range
        return invalid_pt;
    }
}

static char32_t utf8_decode(const std::string & str, std::string::size_type & i) {
    offset_pt res = utf8_decode_check(str, i);
    if (res.offset < 0) {
        i += 1;
        return 0xFFFD;
    } else {
        i += res.offset;
        return res.pt;
    }
}

static void utf16_encode(char32_t pt, std::u16string & out) {
    if (pt < 0x10000) {
        out += static_cast<char16_t>(pt);
    } else if (pt < 0x110000) {
        out += { static_cast<char16_t>(((pt - 0x10000) >> 10) + 0xD800),
                 static_cast<char16_t>((pt & 0x3FF) + 0xDC00) };
    } else {
        out += 0xFFFD;
    }
}

jstring jniStringFromUTF8(JNIEnv * env, const std::string & str) {

    std::u16string utf16;
    utf16.reserve(str.length()); // likely overallocate
    for (std::string::size_type i = 0; i < str.length(); )
        utf16_encode(utf8_decode(str, i), utf16);

    jstring res = env->NewString(
        reinterpret_cast<const jchar *>(utf16.data()), jsize(utf16.length()));
    DJINNI_ASSERT(res, env);
    return res;
}

template<int wcharTypeSize>
static std::u16string implWStringToUTF16(std::wstring::const_iterator, std::wstring::const_iterator)
{
    static_assert(wcharTypeSize == 2 || wcharTypeSize == 4, "wchar_t must be represented by UTF-16 or UTF-32 encoding");
    return {}; // unreachable
}

template<>
inline std::u16string implWStringToUTF16<2>(std::wstring::const_iterator begin, std::wstring::const_iterator end) {
    // case when wchar_t is represented by utf-16 encoding
    return std::u16string(begin, end);
}

template<>
inline std::u16string implWStringToUTF16<4>(std::wstring::const_iterator begin, std::wstring::const_iterator end) {
    // case when wchar_t is represented by utf-32 encoding
    std::u16string utf16;
    utf16.reserve(std::distance(begin, end));
    for(; begin != end; ++begin)
        utf16_encode(static_cast<char32_t>(*begin), utf16);
    return utf16;
}

inline std::u16string wstringToUTF16(const std::wstring & str) {
    // hide "defined but not used" warnings
    (void)implWStringToUTF16<2>;
    (void)implWStringToUTF16<4>;
    // Note: The template helper operates on iterators to work around a compiler issue we saw on Mac.
    // It triggered undefined symbols if wstring methods were called directly in the template function.
    return implWStringToUTF16<sizeof(wchar_t)>(str.cbegin(), str.cend());
}

jstring jniStringFromWString(JNIEnv * env, const std::wstring & str) {
    std::u16string utf16 = wstringToUTF16(str);
    jstring res = env->NewString(
        reinterpret_cast<const jchar *>(utf16.data()), utf16.length());
    DJINNI_ASSERT(res, env);
    return res;
}

// UTF-16 decode helpers.
static inline bool is_high_surrogate(char16_t c) { return (c >= 0xD800) && (c < 0xDC00); }
static inline bool is_low_surrogate(char16_t c)  { return (c >= 0xDC00) && (c < 0xE000); }

/*
 * Like utf8_decode_check, but for UTF-16.
 */
static offset_pt utf16_decode_check(const char16_t * str, std::u16string::size_type i) {
    if (is_high_surrogate(str[i]) && is_low_surrogate(str[i+1])) {
        // High surrogate followed by low surrogate
        char32_t pt = (((str[i] - 0xD800) << 10) | (str[i+1] - 0xDC00)) + 0x10000;
        return { 2, pt };
    } else if (is_high_surrogate(str[i]) || is_low_surrogate(str[i])) {
        // High surrogate *not* followed by low surrogate, or unpaired low surrogate
        return invalid_pt;
    } else {
        return { 1, str[i] };
    }
}

static char32_t utf16_decode(const char16_t * str, std::u16string::size_type & i) {
    offset_pt res = utf16_decode_check(str, i);
    if (res.offset < 0) {
        i += 1;
        return 0xFFFD;
    } else {
        i += res.offset;
        return res.pt;
    }
}

static void utf8_encode(char32_t pt, std::string & out) {
    if (pt < 0x80) {
        out += static_cast<char>(pt);
    } else if (pt < 0x800) {
        out += { static_cast<char>((pt >> 6)   | 0xC0),
                 static_cast<char>((pt & 0x3F) | 0x80) };
    } else if (pt < 0x10000) {
        out += { static_cast<char>((pt >> 12)         | 0xE0),
                 static_cast<char>(((pt >> 6) & 0x3F) | 0x80),
                 static_cast<char>((pt & 0x3F)        | 0x80) };
    } else if (pt < 0x110000) {
        out += { static_cast<char>((pt >> 18)          | 0xF0),
                 static_cast<char>(((pt >> 12) & 0x3F) | 0x80),
                 static_cast<char>(((pt >> 6)  & 0x3F) | 0x80),
                 static_cast<char>((pt & 0x3F)         | 0x80) };
    } else {
        out += { static_cast<char>(0xEF),
                 static_cast<char>(0xBF),
                 static_cast<char>(0xBD) }; // U+FFFD
    }
}

std::string jniUTF8FromString(JNIEnv * env, const jstring jstr) {
    DJINNI_ASSERT(jstr, env);
    const jsize length = env->GetStringLength(jstr);
    jniExceptionCheck(env);

    const auto deleter = [env, jstr] (const jchar * c) { env->ReleaseStringChars(jstr, c); };
    std::unique_ptr<const jchar, decltype(deleter)> ptr(env->GetStringChars(jstr, nullptr),
                                                        deleter);

    std::u16string str(reinterpret_cast<const char16_t *>(ptr.get()), length);
    std::string out;
    out.reserve(str.length() * 3 / 2); // estimate
    for (std::u16string::size_type i = 0; i < str.length(); )
        utf8_encode(utf16_decode(str.data(), i), out);
    return out;
}

template<int wcharTypeSize>
static std::wstring implUTF16ToWString(const char16_t * /*data*/, size_t /*length*/)
{
    static_assert(wcharTypeSize == 2 || wcharTypeSize == 4, "wchar_t must be represented by UTF-16 or UTF-32 encoding");
    return {}; // unreachable
}

template<>
inline std::wstring implUTF16ToWString<2>(const char16_t * data, size_t length) {
    // case when wchar_t is represented by utf-16 encoding
    return std::wstring(data, data + length);
}

template<>
inline std::wstring implUTF16ToWString<4>(const char16_t * data, size_t length) {
    // case when wchar_t is represented by utf-32 encoding
    std::wstring result;
    result.reserve(length);
    for (size_t i = 0; i < length; )
        result += static_cast<wchar_t>(utf16_decode(data, i));
    return result;
}

inline std::wstring UTF16ToWString(const char16_t * data, size_t length) {
    // hide "defined but not used" warnings
    (void)implUTF16ToWString<2>;
    (void)implUTF16ToWString<4>;
    return implUTF16ToWString<sizeof(wchar_t)>(data, length);
}

std::wstring jniWStringFromString(JNIEnv * env, const jstring jstr) {
    DJINNI_ASSERT(jstr, env);
    const jsize length = env->GetStringLength(jstr);
    jniExceptionCheck(env);

    const auto deleter = [env, jstr] (const jchar * c) { env->ReleaseStringChars(jstr, c); };
    std::unique_ptr<const jchar, decltype(deleter)> ptr(env->GetStringChars(jstr, nullptr),
                                                        deleter);
    const char16_t * data = reinterpret_cast<const char16_t *>(ptr.get());
    return UTF16ToWString(data, length);
}

DJINNI_WEAK_DEFINITION
void jniSetPendingFromCurrent(JNIEnv * env, const char * ctx) noexcept {
    jniDefaultSetPendingFromCurrent(env, ctx);
}

void jniDefaultSetPendingFromCurrent(JNIEnv * env, const char * /*ctx*/) noexcept {
    assert(env);
    try {
        throw;
    } catch (const jni_exception & e) {
        e.set_as_pending(env);
        return;
    } catch (const std::exception & e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }

    // noexcept will call terminate() for anything not caught above (i.e.
    // exceptions which aren't std::exception subclasses).
}

template class ProxyCache<JavaProxyCacheTraits>;

CppProxyClassInfo::CppProxyClassInfo(const char * className)
    : clazz(jniFindClass(className)),
      constructor(jniGetMethodID(clazz.get(), "<init>", "(J)V")),
      idField(jniGetFieldID(clazz.get(), "nativeRef", "J")) {
}

CppProxyClassInfo::CppProxyClassInfo() : constructor{}, idField{} {
}

CppProxyClassInfo::~CppProxyClassInfo() {
}

/*
 * Wrapper around Java WeakReference objects. (We can't use JNI NewWeakGlobalRef() because
 * it doesn't have the right semantics - see comment in djinni_support.hpp.)
 */
class JavaWeakRef {
private:
    struct JniInfo {
    public:
        const GlobalRef<jclass> clazz { jniFindClass("java/lang/ref/WeakReference") };
        const jmethodID constructor { jniGetMethodID(clazz.get(), "<init>", "(Ljava/lang/Object;)V") };
        const jmethodID method_get { jniGetMethodID(clazz.get(), "get", "()Ljava/lang/Object;") };
    };

    // Helper used by constructor
    static GlobalRef<jobject> create(JNIEnv * jniEnv, jobject obj) {
        const JniInfo & weakRefClass = JniClass<JniInfo>::get();
        LocalRef<jobject> weakRef(jniEnv, jniEnv->NewObject(weakRefClass.clazz.get(), weakRefClass.constructor, obj));
        // DJINNI_ASSERT performs an exception check before anything else, so we don't need
        // a separate jniExceptionCheck call.
        DJINNI_ASSERT(weakRef, jniEnv);
        return GlobalRef<jobject>(jniEnv, weakRef);
    }

public:
    // Constructor
    JavaWeakRef(jobject obj) : JavaWeakRef(jniGetThreadEnv(), obj) {}
    JavaWeakRef(JNIEnv * jniEnv, jobject obj) : m_weakRef(create(jniEnv, obj)) {}

    // Get the object pointed to if it's still strongly reachable or, return null if not.
    // (Analogous to weak_ptr::lock.) Returns a local reference.
    jobject lock() const {
        const auto & jniEnv = jniGetThreadEnv();
        const JniInfo & weakRefClass = JniClass<JniInfo>::get();
        LocalRef<jobject> javaObj(jniEnv->CallObjectMethod(m_weakRef.get(), weakRefClass.method_get));
        jniExceptionCheck(jniEnv);
        return javaObj.release();
    }

    // Java WeakReference objects don't have a way to check whether they're expired except
    // by upgrading them to a strong ref.
    bool expired() const {
        LocalRef<jobject> javaObj { lock() };
        return !javaObj;
    }

private:
    GlobalRef<jobject> m_weakRef;
};

template class ProxyCache<JniCppProxyCacheTraits>;

} // namespace djinni
