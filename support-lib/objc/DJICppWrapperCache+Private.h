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

struct CppProxyCacheTraits {
    using UnowningImplPointer = void *;
    using OwningImplPointer = std::shared_ptr<void>;
    using OwningProxyPointer = __strong id;
    using WeakProxyPointer = __weak id;
    using UnowningImplPointerHash = std::hash<void *>;
    using UnowningImplPointerEqual = std::equal_to<void *>;
};

// This declares that GenericProxyCache will be instantiated separately. The actual
// explicit instantiations are in DJIProxyCaches.mm.
extern template class ProxyCache<CppProxyCacheTraits>;
using CppProxyCache = ProxyCache<CppProxyCacheTraits>;

template <typename ObjcType, typename CppType>
ObjcType * get_cpp_proxy(const std::shared_ptr<CppType> & cppRef) {
    return CppProxyCache::get(
        cppRef,
        [] (const std::shared_ptr<void> & cppRef) -> std::pair<id, void *> {
            return {
                [[ObjcType alloc] initWithCpp:std::static_pointer_cast<CppType>(cppRef)],
                cppRef.get()
            };
        }
    );
}

} // namespace djinni
