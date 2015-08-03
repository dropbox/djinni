//
// Copyright 2015 Slack Technologies, Inc.
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

#include <memory>
#include <mutex>
#include <unordered_map>
#include <inspectable.h>

namespace djinni {

template <class T>
class CxWrapperCache {
public:
    static const std::shared_ptr<CxWrapperCache> & getInstance() {
        static const std::shared_ptr<CxWrapperCache> instance(new CxWrapperCache);
        // Return by const-ref. This is safe to call any time except during static destruction.
        // Returning by reference lets us avoid touching the refcount unless needed.
        return instance;
    }

    std::shared_ptr<T> get(Platform::Object^ cxRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        std::shared_ptr<T> ret;
		auto it = m_mapping.find(reinterpret_cast<IInspectable*>(cxRef));
        if (it != m_mapping.end()) {
            ret = std::static_pointer_cast<T>(it->second.lock());
            if (ret == nullptr) {
                ret = new_wrapper(cxRef);
            }
        } else {
            ret = new_wrapper(cxRef);
        }
        return ret;
    }

    void remove(Platform::Object^ cxRef) {
        std::unique_lock<std::mutex> lock(m_mutex);
        m_mapping.erase(reinterpret_cast<IInspectable*>(cxRef));
    }

    class Handle {
    public:
        Handle(Platform::Object^ cx) : _cx(cx) { };
        ~Handle() {
            if (_cx) {
                _cache->remove(_cx);
            }
        }
        Platform::Object^ get() const { return _cx; }

    private:
        const std::shared_ptr<CxWrapperCache> _cache = getInstance();
        Platform::Object^ _cx;
    };


private:
    std::unordered_map<IInspectable*, std::weak_ptr<void>> m_mapping;
    std::mutex m_mutex;

	std::shared_ptr<T> new_wrapper(Platform::Object^ cxRef) {
		auto ret = std::shared_ptr<T>(new T(cxRef));
        std::weak_ptr<void> ptr(std::static_pointer_cast<void>(ret));
        m_mapping[reinterpret_cast<IInspectable*>(cxRef)] = ptr;
        return ret;
    }

};

} // namespace djinni
