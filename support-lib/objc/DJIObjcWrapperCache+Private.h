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

} // namespace djinni
