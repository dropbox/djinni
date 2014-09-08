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
class DbxCppWrapperCache {
public:
    static DbxCppWrapperCache & getInstance() {
        static DbxCppWrapperCache instance;
        return instance;
    }

    template <typename AllocFunc>
    id get(const std::shared_ptr<T> & cppRef, const AllocFunc & alloc) {
        std::unique_lock<std::mutex> lock(m_mutex);
        T* ptr = cppRef.get();
        auto got = m_mapping.find(ptr);
        id ret;
        if (got != m_mapping.end()) {
            ret = got->second;
            if (ret == nil) {
                ret = alloc(cppRef);
                m_mapping[ptr] = ret;
            }
        } else {
            ret = alloc(cppRef);
            m_mapping[ptr] = ret;
        }
        return ret;
    }

    void remove(const std::shared_ptr<T> & cppRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        T* ptr = cppRef.get();
        if (m_mapping[ptr] == nil) {
            m_mapping.erase(ptr);
        }
    }

private:
    std::unordered_map<T*, __weak id> m_mapping;
    std::mutex m_mutex;

    DbxCppWrapperCache() {}
};

} // namespace djinni
