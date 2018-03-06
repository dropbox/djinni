#include "thread_local.hpp"
#include <pthread.h>
#include <cassert>

namespace djinni {
namespace support_lib {

/*
 * Deleter for our key objects. Each thread's data is a pointer to a data_map, so we just
 * need to delete it.
 */
static void deleter(void * ptr) {
    delete static_cast<data_map *>(ptr);
}

/*
 * Allocate a pthread_key_t suitable for holding data_map objects. Used only by get_key().
 */
static pthread_key_t allocate_key() {
    pthread_key_t key {};
    assert(pthread_key_create(&key, deleter) == 0);
    return key;
}

/*
 * Get the global pthread_key_t used by djinni::support_lib::ThreadLocal.
 */
static pthread_key_t get_key() {
    static pthread_key_t key = allocate_key();
    return key;
}

/*
 * Get the data_map for this thread, creating it if necessary.
 */
data_map & get_this_thread_map() {
    pthread_key_t key = get_key();

    data_map * ptr = static_cast<data_map *>(pthread_getspecific(key));
    if (!ptr) {
        ptr = new data_map; // will be deleted by deleter() above
        assert(pthread_setspecific(key, ptr) == 0);
    }

    return *ptr;
}

/*
 * Helper for constructors.
 */
void assert_tag_unique(const Tag * tag) {
    // Sanity-check: make sure there's nothing with this tag already in existence on
    // this thread. This can help catch tag reuse.
    data_map & m = get_this_thread_map();
    assert(m.find(tag) == m.end());
}

} } // namespace djinni::support_lib
