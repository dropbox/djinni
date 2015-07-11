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

//  This header can only be imported to C++/Cx source code!
#pragma once

#include <memory>
#include <mutex>
#include <unordered_map>

namespace djinni {

template <class T>
class CppWrapperCache {
public:
    static const std::shared_ptr<CppWrapperCache> & getInstance() {
        static const std::shared_ptr<CppWrapperCache> instance(new CppWrapperCache);
        // Return by const-ref. This is safe to call any time except during static destruction.
        // Returning by reference lets us avoid touching the refcount unless needed.
        return instance;
    }

    template <typename AllocFunc>
    Platform::Object^ get(const std::shared_ptr<T> & cppRef, const AllocFunc & alloc) {
        std::unique_lock<std::mutex> lock(m_mutex);
        T* ptr = cppRef.get();
        auto got = m_mapping.find(ptr);
		Platform::Object^ ret;
        if (got != m_mapping.end()) {
			ret = got->second.Resolve<Platform::Object>();
            if (ret == nullptr) {
                ret = alloc(cppRef);
                m_mapping[ptr] = Platform::WeakReference(ret);
            }
        } else {
            ret = alloc(cppRef);
			m_mapping[ptr] = Platform::WeakReference(ret);
        }
        return ret;
    }

    void remove(const std::shared_ptr<T> & cppRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        T* ptr = cppRef.get();
        if (m_mapping[ptr] == nullptr) {
            m_mapping.erase(ptr);
        }
    }

    class Handle {
    public:
        Handle() = default;
        ~Handle() {
            if (_ptr) {
                _cache->remove(_ptr);
            }
        }
        void assign(const std::shared_ptr<T>& ptr) { _ptr = ptr; }
        const std::shared_ptr<T>& get() const { return _ptr; }

    private:
        const std::shared_ptr<CppWrapperCache> _cache = getInstance();
        std::shared_ptr<T> _ptr;
    };

private:
    std::unordered_map<T*, Platform::WeakReference> m_mapping;
    std::mutex m_mutex;

    CppWrapperCache() {}
};

} // namespace djinni
