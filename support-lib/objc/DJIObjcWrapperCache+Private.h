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

//  This header can only be imported to Objective-C++ source code!

#import <Foundation/Foundation.h>
#include <memory>

#include "../proxy_cache_interface.hpp"

namespace djinni {

struct unretained_id_hash { std::size_t operator()(__unsafe_unretained id ptr) const; };

struct ObjcProxyCacheTraits {
    using UnowningImplPointer = __unsafe_unretained id;
    using OwningImplPointer = __strong id;
    using OwningProxyPointer = std::shared_ptr<void>;
    using WeakProxyPointer = std::weak_ptr<void>;
    using UnowningImplPointerHash = unretained_id_hash;
    using UnowningImplPointerEqual = std::equal_to<__unsafe_unretained id>;
};

// This declares that GenericProxyCache will be instantiated separately. The actual
// explicit instantiations are in DJIProxyCaches.mm.
extern template class ProxyCache<ObjcProxyCacheTraits>;
using ObjcProxyCache = ProxyCache<ObjcProxyCacheTraits>;

template <typename CppType, typename ObjcType>
static std::shared_ptr<CppType> get_objc_proxy(ObjcType * objcRef) {
    return std::static_pointer_cast<CppType>(ObjcProxyCache::get(
        typeid(objcRef),
        objcRef,
        [] (const __strong id & objcRef) -> std::pair<std::shared_ptr<void>, __unsafe_unretained id> {
            return {
                std::make_shared<CppType>(objcRef),
                objcRef
            };
        }
    ));
}

// Private implementation base class for all ObjC proxies, which manages the
// Handle, and ensures that it is created and destroyed in a safe way, inside
// of an @autoreleasepool to avoid leaks.  Most of the complexity here is just
// necessary to gain explicit control of construction and destruction time of
// the member Handle, to make sure it's inside the @autoreleasepool block.
template <typename ObjcType>
class ObjcProxyBase {
public:
    ObjcProxyBase(ObjcType objc) {
        @autoreleasepool {
            m_djinni_private_proxy_handle.emplace(objc);
        }
    }

    ObjcProxyBase(const ObjcProxyBase&) = delete;
    ObjcProxyBase& operator=(const ObjcProxyBase&) = delete;

    // Not intended for polymorphic use, so dtor is not virtual
    ~ObjcProxyBase() {
        @autoreleasepool {
            m_djinni_private_proxy_handle.reset();
        }
    }

    // Long name to minimize likelyhood of collision with interface methods.
    ObjcType djinni_private_get_proxied_objc_object() const {
        return m_djinni_private_proxy_handle.value().get();
    }

private:
    // Simple stand-in for std::experimental::optional since the
    // support-lib doesn't know which optional implementation might be in use.
    // We can't take OptionalType as a template arg here, because there's no
    // way to implement reset() without also knowing where to find nullopt, not
    // just optional itself.  This class uses the same technique as at least
    // one optional implementation: The union ensures proper alignment, while
    // leaving construction and destruction under our direct control.
    // This class is NOT safe for general use.  It's safe only if emplace() and
    // reset() are each called exactly once.
    class OptionalObjcProxyHandle final {
    public:
        using HandleType = ::djinni::ObjcProxyCache::Handle<ObjcType>;

        OptionalObjcProxyHandle() {};
        OptionalObjcProxyHandle(const OptionalObjcProxyHandle&) = delete;
        OptionalObjcProxyHandle& operator=(const OptionalObjcProxyHandle&) = delete;
        ~OptionalObjcProxyHandle() {};

        void emplace(ObjcType objc) {
            new (&m_handle) HandleType(objc);
        }

        void reset() {
            m_handle.~HandleType();
        }

        const HandleType & value() const { return m_handle; }
    private:
        union {
            char m_dummy;
            HandleType m_handle;
        };
    };

    // Long name to minimize likelyhood of collision with interface methods.
    OptionalObjcProxyHandle m_djinni_private_proxy_handle;
};

} // namespace djinni
