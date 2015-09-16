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

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

#include "../proxy_cache_impl.hpp"
#include "DJIObjcWrapperCache+Private.h"
#include "DJICppWrapperCache+Private.h"

namespace djinni {

std::size_t unretained_id_hash::operator()(__unsafe_unretained id ptr) const {
    return std::hash<void*>()((__bridge void*)ptr);
}

template class ProxyCache<ObjcProxyCacheTraits>;
template class ProxyCache<CppProxyCacheTraits>;

} // namespace djinni
