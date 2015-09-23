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

//
// Note: This is a sample implementation which interacts with Djinni-generated code.
// You may need to adjust names or include paths to match the settings of code
// generation in your project.
//

#include "platform_threads.hpp"
#include "thread_func.hpp"

#include <functional>

namespace djinni {
namespace extension_libs {
namespace platform_threads {

/**
 * Pure C++ implementation of PlatformThreads, using std::thread.
 * Note that thread names are ignored by this class.
 *
 * This implementation abstracts away the type of pointers to thread objects.
 * By default, use CppPlatformThreads defined below. If you use non-nullable pointers,
 * you may want to instantiate CppPlatformThreadsGeneric with a different type.
 */
template <typename ThreadFuncPtr>
class CppPlatformThreadsGeneric : public PlatformThreads {
public:
    /** Implementation with default thread configuration. */
    CppPlatformThreadsGeneric() : m_func_on_thread_start(nullptr) {};

    /**
     * Implementation which will call the given function on each new thread after
     * it starts, before calling the normal ThreadFunc.
     */
    CppPlatformThreadsGeneric(std::function<void()> func_on_thread_start)
    : m_func_on_thread_start(make_shared<std::function<void()>>(std::move(func_on_thread_start))) {}

    /** Creates and starts a new thread which will call the given function. */
    virtual void create_thread(const std::string & /*name*/,
                               const ThreadFuncPtr & func) override final {
        std::thread([run_func=std::move(func), start_func=m_func_on_thread_start]() {
            if (start_func && *start_func) {
                (*start_func)();
            }
            run_func->run();
        }).detach();
    }

    /**
     * Determines whether the calling thread is the main UI thread of the
     * app.  Some platforms do not have a notion of a main thread, in which
     * case this method returns null.
     *
     * This implementation returns null since C++ doesn't have any default
     * notion of a main/UI thread.  Platform-specific subclasses may override.
     */
    virtual std::experimental::optional<bool> is_main_thread() override final {
        return nullopt;
    }

private:
    const shared_ptr<std::function<void()>> m_func_on_thread_start;
};

/**
 * Instantiation of CppPlatformThreadsGeneric with standard pointers.
 */
using CppPlatformThreads = CppPlatformThreadsGeneric<std::shared_ptr<ThreadFunc>>;

/** Standard implementation of Djinni thread_func using std::function. */
class ThreadFuncImpl final : public ThreadFunc {
public:
    /**
     * Implicit constructor allowing std::function<void()> to be passed
     * where ThreadFunc is expected.
     */
    ThreadFuncImpl(std::function<void()> func) : m_func(std::move(func)) {}

    /** Will be run on thread start.  The thread will exit when this returns. */
    virtual void run() override final {
        m_func();
    }
private:
    const std::function<void()> m_func;
};

} } } // namespace djinni::extension_libs::platform_threads
