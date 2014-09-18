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
#include <cassert>
#include <cstdlib>

static_assert(sizeof(jlong) >= sizeof(void*), "must be able to fit a void* into a jlong");

namespace djinni {

// Set only once from JNI_OnLoad before any other JNI calls, so no lock needed.
static JavaVM * g_cachedJVM;

void jniInit(JavaVM * jvm) {
    g_cachedJVM = jvm;

    try {
        for (const auto & kv : JniClassInitializer::Registration::get_all()) {
            kv.second->init();
        }
    } catch (const jni_exception_pending &) {}
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

void GlobalRefDeleter::operator() (jobject globalRef) noexcept {
    if (!globalRef || !g_cachedJVM) {
        return;
    }

    // Special case: ignore GlobalRef deletions that happen after this thread has been
    // detached. (This can happen during process shutdown, so there's no need to release the
    // ref anyway.)
    JNIEnv * env = nullptr;
    const jint get_res = g_cachedJVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);

    if (get_res == JNI_EDETACHED) {
        return;
    }

    // Still bail on any other error.
    if (get_res != 0 || !env) {
        // :(
        std::abort();
    }

    env->DeleteGlobalRef(globalRef);
}

void LocalRefDeleter::operator() (jobject localRef) noexcept {
    jniGetThreadEnv()->DeleteLocalRef(localRef);
}

void jniExceptionCheck(JNIEnv * env) {
    if (!env) {
        abort();
    }
    if (env->ExceptionCheck()) {
        throw jni_exception_pending();
    }
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
    snprintf(buf, sizeof buf, "djinni (%s:%d): %s", file_basename, line, check);

    jclass cassert = env->FindClass("java/lang/AssertionError");
    assert(cassert);
    env->ThrowNew(cassert, buf);
    assert(env->ExceptionCheck());

    env->DeleteLocalRef(cassert);
    throw jni_exception_pending {};
}

GlobalRef<jclass> jniFindClass(const char * name) {
    JNIEnv * env = jniGetThreadEnv();
    GlobalRef<jclass> guard(env, env->FindClass(name));
    jniExceptionCheck(env);
    if (!guard) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "FindClass returned null");
    }
    return guard;
}

jmethodID jniGetStaticMethodID(jclass clazz, const char * name, const char * sig) {
    JNIEnv * env = jniGetThreadEnv();
    jmethodID id = env->GetStaticMethodID(clazz, name, sig);
    jniExceptionCheck(env);
    if (!id) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "GetStaticMethodID returned null");
    }
    return id;
}

jmethodID jniGetMethodID(jclass clazz, const char * name, const char * sig) {
    JNIEnv * env = jniGetThreadEnv();
    jmethodID id = env->GetMethodID(clazz, name, sig);
    jniExceptionCheck(env);
    if (!id) {
        jniThrowAssertionError(env, __FILE__, __LINE__, "GetMethodID returned null");
    }
    return id;
}

jfieldID jniGetFieldID(jclass clazz, const char * name, const char * sig) {
    JNIEnv * env = jniGetThreadEnv();
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
    const jint res = env->CallIntMethod(obj, m_methOrdinal);
    jniExceptionCheck(env);
    return res;
}

LocalRef<jobject> JniEnum::create(JNIEnv * env, jint value) const {
    LocalRef<jobject> values(env, env->CallStaticObjectMethod(m_clazz.get(), m_staticmethValues));
    DJINNI_ASSERT(values, env);
    return LocalRef<jobject>(env, env->GetObjectArrayElement(static_cast<jobjectArray>(values.get()), value));
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
#ifdef DBX_JNI_LOCAL_FRAME_DEBUG
    JNI_DEBUG_LOGF(VERBOSE, "Pushing local frame count to %d, capacity=%d", s_frameCount+1, capacity);
#endif

    const jint push_res = env->PushLocalFrame(capacity);

#ifdef DBX_JNI_LOCAL_FRAME_DEBUG
    if (0 == push_res) {
        ++s_frameCount;
    } else {
        JNI_DEBUG_LOGF(ERROR, "%d <- PushLocalFrame(%d)", push_res, capacity);
        env->ExceptionDescribe();
    }
#endif

    return 0 == push_res;
}

void JniLocalScope::_popLocalFrame(JNIEnv* const env, jobject returnRef) {
#ifdef DBX_JNI_LOCAL_FRAME_DEBUG
    --s_frameCount;
    JNI_DEBUG_LOGF(VERBOSE, "Popping local frame count to %d.", s_frameCount);
#endif
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
static offset_pt utf16_decode_check(const std::u16string & str, std::u16string::size_type i) {
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

static char32_t utf16_decode(const std::u16string & str, std::u16string::size_type & i) {
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
    const jsize length = env->GetStringLength(jstr);
    jniExceptionCheck(env);

    const auto deleter = [env, jstr] (const jchar * c) { env->ReleaseStringChars(jstr, c); };
    std::unique_ptr<const jchar, decltype(deleter)> ptr(env->GetStringChars(jstr, nullptr),
                                                        deleter);

    std::u16string str(reinterpret_cast<const char16_t *>(ptr.get()), length);
    std::string out;
    out.reserve(str.length() * 3 / 2); // estimate
    for (std::u16string::size_type i = 0; i < str.length(); )
        utf8_encode(utf16_decode(str, i), out);
    return out;
}

__attribute__((weak))
void jniSetPendingFromCurrent(JNIEnv * env, const char * /*ctx*/) noexcept {
    try {
        throw;
    } catch (const jni_exception_pending &) {
        return;
    } catch (const std::exception & e) {
        env->ThrowNew(env->FindClass("java/lang/RuntimeException"), e.what());
    }
}

struct JavaProxyCacheState {
    std::mutex mtx;
    std::unordered_map<jobject, std::weak_ptr<void>, JavaIdentityHash, JavaIdentityEquals> m;
    int counter = 0;

    static JavaProxyCacheState & get() {
        static JavaProxyCacheState st;
        return st;
    }
};

JavaProxyCacheEntry::JavaProxyCacheEntry(jobject localRef, JNIEnv * env)
    : m_globalRef(env, localRef) {
    DJINNI_ASSERT(m_globalRef, env);
}

JavaProxyCacheEntry::JavaProxyCacheEntry(jobject localRef)
    : JavaProxyCacheEntry(localRef, jniGetThreadEnv()) {}

JavaProxyCacheEntry::~JavaProxyCacheEntry() noexcept {
    JavaProxyCacheState & st = JavaProxyCacheState::get();
    const std::lock_guard<std::mutex> lock(st.mtx);
    st.m.erase(m_globalRef.get());
}

std::shared_ptr<void> javaProxyCacheLookup(jobject obj, std::pair<std::shared_ptr<void>, jobject>(*factory)(jobject)) {
    JavaProxyCacheState & st = JavaProxyCacheState::get();
    const std::lock_guard<std::mutex> lock(st.mtx);

    const auto it = st.m.find(obj);
    if (it != st.m.end()) {
        std::shared_ptr<void> ptr = it->second.lock();
        if (ptr) {
            return ptr;
        }
    }

    // Otherwise, construct a new T, save it, and return it.
    std::pair<std::shared_ptr<void>, jobject> ret = factory(obj);
    st.m[ret.second] = ret.first;
    return ret.first;
}

} // namespace djinni
