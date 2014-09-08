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

#import "DJIWeakPtrWrapper+Private.h"
#import <Foundation/Foundation.h>
#include <memory>
#include <mutex>
#include <unordered_map>

namespace djinni {

template <class T>
class DbxObjcWrapperCache {
public:
    static DbxObjcWrapperCache & getInstance() {
        static DbxObjcWrapperCache instance;
        return instance;
    }

    std::shared_ptr<T> get(id objcRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        std::shared_ptr<T> ret;
        DBWeakPtrWrapper *wrapper = [m_mapping objectForKey:objcRef];
        if (wrapper != nil) {
            ret = std::static_pointer_cast<T>(wrapper.ptr.lock());
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
        DBWeakPtrWrapper *wrapper = [m_mapping objectForKey:objcRef];
        if (wrapper.ptr.expired()) {
            [m_mapping removeObjectForKey:objcRef];
        }
    }

private:
    NSMapTable *m_mapping;
    std::mutex m_mutex;

    DbxObjcWrapperCache() {
        m_mapping = [NSMapTable weakToStrongObjectsMapTable];
    }

    std::shared_ptr<T> new_wrapper(id objcRef) {
        std::shared_ptr<T> ret = std::make_shared<T>(objcRef);
        std::weak_ptr<void> ptr(std::static_pointer_cast<void>(ret));
        DBWeakPtrWrapper *wrapper = [[DBWeakPtrWrapper alloc] initWithWeakPtr:ptr];
        [m_mapping setObject:wrapper forKey:objcRef];
        return ret;
    }

};

} // namespace djinni
