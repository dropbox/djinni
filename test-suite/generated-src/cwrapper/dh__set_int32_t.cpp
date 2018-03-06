// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from set.djinni

#include <iostream> // for debugging
#include <cassert>
#include "wrapper_marshal.hpp"
#include "set_record.hpp"

#include "dh__set_int32_t.hpp"
#include "dh__set_record.hpp"
#include "dh__set_string.hpp"

static void(*s_py_callback_set_int32_t___delete)(DjinniObjectHandle *);
void set_int32_t_add_callback___delete(void(* ptr)(DjinniObjectHandle *)) {
    s_py_callback_set_int32_t___delete = ptr;
}

void set_int32_t___delete(DjinniObjectHandle * drh) {
    s_py_callback_set_int32_t___delete(drh);
}
void optional_set_int32_t___delete(DjinniOptionalObjectHandle *  drh) {
    s_py_callback_set_int32_t___delete((DjinniObjectHandle *) drh);
}
static size_t ( * s_py_callback_set_int32_t__get_size)(DjinniObjectHandle *);

void set_int32_t_add_callback__get_size(size_t( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_set_int32_t__get_size = ptr;
}

static DjinniObjectHandle * ( * s_py_callback_set_int32_t__python_create)(void);

void set_int32_t_add_callback__python_create(DjinniObjectHandle *( * ptr)(void)) {
    s_py_callback_set_int32_t__python_create = ptr;
}

static void ( * s_py_callback_set_int32_t__python_add)(DjinniObjectHandle *, int32_t);

void set_int32_t_add_callback__python_add(void( * ptr)(DjinniObjectHandle *, int32_t)) {
    s_py_callback_set_int32_t__python_add = ptr;
}

static int32_t ( * s_py_callback_set_int32_t__python_next)(DjinniObjectHandle *);

void set_int32_t_add_callback__python_next(int32_t( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_set_int32_t__python_next = ptr;
}

djinni::Handle<DjinniObjectHandle> DjinniSetInt32T::fromCpp(const std::unordered_set<int32_t> & dc) {
    djinni::Handle<DjinniObjectHandle> _handle(s_py_callback_set_int32_t__python_create(), & set_int32_t___delete);
    for (const auto & it : dc) {
        s_py_callback_set_int32_t__python_add(_handle.get(), it);
    }

    return _handle;
}

std::unordered_set<int32_t> DjinniSetInt32T::toCpp(djinni::Handle<DjinniObjectHandle> dh) {
    std::unordered_set<int32_t>_ret;
    size_t size = s_py_callback_set_int32_t__get_size(dh.get());

    for (int i = 0; i < size; i++) {
        auto _el = s_py_callback_set_int32_t__python_next(dh.get());
        _ret.insert(std::move(_el));
    }

    return _ret;
}

djinni::Handle<DjinniOptionalObjectHandle> DjinniSetInt32T::fromCpp(std::experimental::optional<std::unordered_set<int32_t>> dc) {
    if (dc == std::experimental::nullopt) {
        return nullptr;
    }
    return djinni::optionals::toOptionalHandle(DjinniSetInt32T::fromCpp(std::move(* dc)), optional_set_int32_t___delete);
}

std::experimental::optional<std::unordered_set<int32_t>>DjinniSetInt32T::toCpp(djinni::Handle<DjinniOptionalObjectHandle> dh) {
     if (dh) {
        return std::experimental::optional<std::unordered_set<int32_t>>(DjinniSetInt32T::toCpp(djinni::optionals::fromOptionalHandle(std::move(dh), set_int32_t___delete)));
    }
    return std::experimental::nullopt;
}

