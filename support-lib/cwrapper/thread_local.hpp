#pragma once
#include <map>

namespace djinni {
namespace support_lib {

/*
 * To conserve resources (pthread_key_create generally uses one of a small number of slots
 * preallocated at the base of the stack), we create a single pthread_key_t for all
 * ThreadLocal data. Each ThreadLocal<T> contains a Tag; we use the address of the Tag as
 * a key in a per-thread map to find the data.
 */
struct Tag {};

/*
 * Each thread-specific map contains pointers to Deletable objects, which we then downcast
 * as needed.
 */
struct Deletable { virtual ~Deletable() = 0; };
inline Deletable::~Deletable() {}

/*
 * Type alias for the thread-specific data map.
 */
using data_map = std::map<const Tag *, std::unique_ptr<Deletable>>;

/*
 * Helper to get the per-thread data map (creating if necessary).
 */
data_map & get_this_thread_map();

/*
 * Helper for constructors.
 */
void assert_tag_unique(const Tag *);

/*
 * ThreadLocal. Replacement for thread_local storage duration built on pthreads.
 * Intended for use until all supported compilers support thread_local storage natively.
 *
 * ThreadLocal<T> objects should only exist for the lifetime of the program (at namespace
 * scope, as a static data member, or in some other form of singleton) - it is not safe to
 * destroy a ThreadLocal. (This is because destroying a ThreadLocal would require tracking
 * down the instances of T that exist for every other thread, which would add overhead. The
 * ThreadLocal object uses an address tag in a per-thread map to track which objects belong
 * to which ThreadLocal. When a ThreadLocal is destroyed, each thread's instances will stay
 * around - but if another ThreadLocal is ever created at the same address, objects that the
 * old one owned will be confused with the new one, even if they're of a different type.)
 */
template <class T>
class ThreadLocal {
    static_assert(std::is_default_constructible<T>::value,
                  "ThreadLocal objects must be default-constructible.");

    struct Data final : public Deletable {
        T contents;
        virtual ~Data() override {}
    };

    T & get_impl() const {
        data_map & m = get_this_thread_map();
        auto it = m.find(&m_tag);
        if (it == m.end()) {
            it = m.emplace(&m_tag, std::make_unique<Data>()).first;
        }

        return static_cast<Data &>(*it->second).contents;
    }

public:
    ThreadLocal() {
        assert_tag_unique(&m_tag);
    }

    T &       get()       { return get_impl(); }
    const T & get() const { return get_impl(); }

private:
    const Tag m_tag {};
};

} } // namespace djinni::support_lib
