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
#include <mutex>
#include <unordered_map>

namespace djinni {

template <class T>
class DbxObjcWrapperCache {
public:
    static const std::shared_ptr<DbxObjcWrapperCache> & getInstance() {
        static const std::shared_ptr<DbxObjcWrapperCache> instance(new DbxObjcWrapperCache);
        // Return by const-ref. This is safe to call any time except during static destruction.
        // Returning by reference lets us avoid touching the refcount unless needed.
        return instance;
    }

    std::shared_ptr<T> get(id objcRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        std::shared_ptr<T> ret;
        auto it = m_mapping.find((__bridge void*)objcRef);
        if (it != m_mapping.end()) {
            ret = std::static_pointer_cast<T>(it->second.lock());
            if (ret == nullptr) {
                ret = new_wrapper(objcRef);
            }
        } else {
            ret = new_wrapper(objcRef);
        }
        return ret;
    }

    void remove(id objcRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        m_mapping.erase((__bridge void*)objcRef);
    }

    class Handle {
    public:
        Handle(id obj) : _obj(obj) { };
        ~Handle() {
            if (_obj) {
                _cache->remove(_obj);
            }
        }
        id get() const noexcept { return _obj; }

    private:
        const std::shared_ptr<DbxObjcWrapperCache> _cache = getInstance();
        const id _obj;
    };


private:
    std::unordered_map<void*, std::weak_ptr<void>> m_mapping;
    std::mutex m_mutex;

    std::shared_ptr<T> new_wrapper(id objcRef) {
        std::shared_ptr<T> ret = std::make_shared<T>(objcRef);
        std::weak_ptr<void> ptr(std::static_pointer_cast<void>(ret));
        m_mapping[(__bridge void*)objcRef] = ptr;
        return ret;
    }

};

} // namespace djinni
