//
// Copyright 2015 Dropbox, Inc.
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

#include <memory>
#include <functional>
#include <typeindex>

namespace djinni {

/*
 * The template parameters we receive here can be a number of different types: C++ smart
 * pointers, custom wrappers, or language-specific types (like ObjC's `id` / `__weak id`).
 * If custom wrapper types are used, like the `JavaWeakRef` type in the JNI library, then
 * they must implement `.get()` or `.lock()` by analogy with C++'s smart pointers.
 *
 * We assume that built-in types are pointer-compatible. This is the case with ObjC: for
 * example, __weak id pointers can be implicitly converted to __strong id, and so on.
 *
 * (The helper for .lock() is only used by proxy_cache_impl.hpp, so it's defined there.)
 */
template <typename T> static inline auto get_unowning(const T & ptr) -> decltype(ptr.get()) {
    return ptr.get();
}
template <typename T> static inline T * get_unowning(T * ptr) { return ptr; }

/*
 * ProxyCache provides a mechanism for re-using proxy objects generated in one language
 * that wrap around implementations in a different language. This is for correctness, not
 * just performance: when we pass the same object across a language boundary twice, we want
 * to get the same proxy object on the other side each time, so that identity semantics
 * behave as expected.
 *
 * ProxyCache is instantiated with a Traits class that must contain the following typedefs.
 * Examples refer to the use of ProxyCache to cache C++ wrappers around ObjC objects.
 * ProxyCache itself is generic (type-erased), though Handle is not, so we use e.g. `id`
 * and `shared_ptr<void>` rather than any specific types.
 *
 * - UnowningImplPointer:
 *       a non-owning pointer to an object being wrapped, e.g. __unsafe_unretained id
 * - OwningImplPointer:
 *       a strong owning pointer to an object being wrapped, e.g. __strong id
 * - OwningProxyPointer:
 *       a strong owning pointer to a wrapper, e.g. std::shared_ptr<void>
 * - WeakProxyPointer:
 *       a safe weak pointer to a wrapper, e.g. std::weak_ptr<void>
 * - UnowningImplPointerHash:
 *       a hasher for UnowningImplPointer, usually std::hash<UnowningImplPointer>, unless
 *       std::hash doesn't work with UnowningImplPointer in which case a custom type can be
 *       provided.
 * - UnowningImplPointerEqual:
 *       an equality predicate for UnowningImplPointer, like std::equal_to<UnowningImplPointer>.
 *       In some cases (e.g. Java) a custom equality predicate may be needed.
 *
 * Generally, ProxyCache will be explicitly instantiated in one source file with C++11's
 * `extern template` mechanism. The WeakProxyPointer, UnowningImplPointerHash, and
 * UnowningImplPointerEqual types can be incomplete except for where the explicit
 * instantiation is actually defined.
 *
 * Here's an overview of the structure:
 *
 *                           ______________  std::pair<ImplType,
 *          WeakProxyPonter |              | UnowningImplPointer>
 *           - - - - - - - -|  ProxyCache  |- - - - - - - - - -
 *          |               |              |                   |
 *          |               |______________|                   |
 *          |                                                  |
 *      ____v____        ______________          ______________v__________
 *     |         |      |              |        |                         |
 *     | (Proxy  | ===> | ProxyCache:: | =====> | (Impl object providing  |
 *     | object) |  ^   |   Handle<T>  |   T    |  actual functionality)  |
 *     |_________|  .   |______________|   ^    |_________________________|
 *                  .                     .
 *     ( can be member, base, )       ( T is a generally a specific   )
 *     ( or cross-language    )       ( owning type like id<Foo>,     )
 *     ( reference like jlong )       ( shared_ptr<Foo>, or GlobalRef )
 *
 * The cache contains a map from pair<ImplType, UnowningImplPointer>
 * to WeakProxyPointer, allowing it to answer the question: "given this
 * impl, do we already have a proxy in existence?"
 *
 * We use one map for all translated types, rather than a separate one for each type,
 * to minimize duplication of code and make it so the unordered_map is as contained as
 * possible.
 */
template <typename Traits>
class ProxyCache {
public:
    using UnowningImplPointer = typename Traits::UnowningImplPointer;
    using OwningImplPointer = typename Traits::OwningImplPointer;
    using OwningProxyPointer = typename Traits::OwningProxyPointer;
    using WeakProxyPointer = typename Traits::WeakProxyPointer;
    using UnowningImplPointerHash = typename Traits::UnowningImplPointerHash;
    using UnowningImplPointerEqual = typename Traits::UnowningImplPointerEqual;
    class Pimpl;

    /*
     * Each proxy object must own a Handle. The Handle carries a strong reference to whatever
     * the proxy wraps. When `ProxyCache::get()` creates a proxy, it also adds the proxy to
     * the global proxy cache; Handle::~Handle() removes the reference from the cache.
     *
     * The Handle can be held by the proxy in any of a number of ways: as a C++ member or
     * base, as an ObjC instance variable, or across an FFI boundary (a Java object might
     * contain the address of a Handle as a `long` and delete it in the destructor.)
     *
     * T is generally a more-specialized version of OwningImplPointer. For example, when
     * managing C++ proxies for ObjC objects, OwningImplPointer would be `id`, and the C++
     * proxy class `MyInterface` which wraps `@protocol DBMyInterface` would contain a
     * `Handle<id<DBMyInterface>>`.
     *
     * TagType should be the same type that was passed in to `get()` when this handle was
     * created. Normally this is the same as T (a specialized OwningImplPointer), but in
     * cases like Java where all object types are uniformly represented as `jobject` in C++,
     * another type may be used.
     */
    template <typename T, typename TagType = T>
    class Handle {
    public:
        template <typename... Args> Handle(Args &&... args)
            : m_cache(get_base()), m_obj(std::forward<Args>(args)...) {}
        Handle(const Handle &) = delete;
        Handle & operator=(const Handle &) = delete;
        ~Handle() { if (m_obj) cleanup(m_cache, typeid(TagType), get_unowning(m_obj)); }

        void assign(const T & obj) { m_obj = obj; }

        const T & get() const & noexcept { return m_obj; }

    private:
        const std::shared_ptr<Pimpl> m_cache;
        T m_obj;
    };

    /*
     * Function typedef for helpers passed in to allocate new objects.
     * To reduce code size, the proxy cache type-erases the objects inside it.
     *
     * An allocator function takes an OwningImplPointer to the source language object,
     * and returns a newly-created proxy.
     *
     * In Java, an OwningImplPointer does not provide the same identity semantics as the
     * underlying object. (A JNI 'jobject' can be one of a few different types of reference,
     * and JNI is structured to allow copying GCs, so an object's address might change over
     * time.) This is why ProxyCache takes hasher and comparator paramters. In particular,
     * the OwningImplPointer passed into the allocator might be a JNI local reference. The
     * allocator will create a GlobalRef to the impl object and store it in the returned proxy.
     *
     * Because we don't constrain how Handle objects are held, there's no generic way for the
     * proxy cache to get the GlobalRef out of the returned proxy object, so AllocatorFunction
     * returns a pair: the first element is the newly created proxy, and the second is an
     * UnowningImplPointer that will be used as a key in the map.
     */
    using AllocatorFunction =
        std::pair<OwningProxyPointer, UnowningImplPointer>(const OwningImplPointer &);

    /*
     * Return the existing proxy for `impl`, if any. If not, create one by calling `alloc`,
     * store a weak reference to it in the proxy cache, and return it.
     */
    static OwningProxyPointer get(const std::type_index &,
                                  const OwningImplPointer & impl,
                                  AllocatorFunction * alloc);

private:
    static void cleanup(const std::shared_ptr<Pimpl> &,
                        const std::type_index &,
                        UnowningImplPointer);
    static const std::shared_ptr<Pimpl> & get_base();
};

} // namespace djinni
