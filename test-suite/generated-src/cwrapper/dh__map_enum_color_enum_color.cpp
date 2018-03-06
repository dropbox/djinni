// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from enum.djinni

#include <iostream> // for debugging
#include <cassert>
#include "wrapper_marshal.hpp"
#include "enum_usage_record.hpp"

#include "color.hpp"
#include "dh__color.hpp"
#include "dh__enum_usage_record.hpp"
#include "dh__list_enum_color.hpp"
#include "dh__map_enum_color_enum_color.hpp"
#include "dh__set_enum_color.hpp"
#include <experimental/optional>

static void(*s_py_callback_map_enum_color_enum_color___delete)(DjinniObjectHandle *);
void map_enum_color_enum_color_add_callback___delete(void(* ptr)(DjinniObjectHandle *)) {
    s_py_callback_map_enum_color_enum_color___delete = ptr;
}

void map_enum_color_enum_color___delete(DjinniObjectHandle * drh) {
    s_py_callback_map_enum_color_enum_color___delete(drh);
}
void optional_map_enum_color_enum_color___delete(DjinniOptionalObjectHandle *  drh) {
    s_py_callback_map_enum_color_enum_color___delete((DjinniObjectHandle *) drh);
}
static int ( * s_py_callback_map_enum_color_enum_color__get_value)(DjinniObjectHandle *, int);

void map_enum_color_enum_color_add_callback__get_value(int( * ptr)(DjinniObjectHandle *, int)) {
    s_py_callback_map_enum_color_enum_color__get_value = ptr;
}

static size_t ( * s_py_callback_map_enum_color_enum_color__get_size)(DjinniObjectHandle *);

void map_enum_color_enum_color_add_callback__get_size(size_t( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_map_enum_color_enum_color__get_size = ptr;
}

static DjinniObjectHandle * ( * s_py_callback_map_enum_color_enum_color__python_create)(void);

void map_enum_color_enum_color_add_callback__python_create(DjinniObjectHandle *( * ptr)(void)) {
    s_py_callback_map_enum_color_enum_color__python_create = ptr;
}

static void ( * s_py_callback_map_enum_color_enum_color__python_add)(DjinniObjectHandle *, int, int);

void map_enum_color_enum_color_add_callback__python_add(void( * ptr)(DjinniObjectHandle *, int, int)) {
    s_py_callback_map_enum_color_enum_color__python_add = ptr;
}

static int ( * s_py_callback_map_enum_color_enum_color__python_next)(DjinniObjectHandle *);

void map_enum_color_enum_color_add_callback__python_next(int( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_map_enum_color_enum_color__python_next = ptr;
}

djinni::Handle<DjinniObjectHandle> DjinniMapEnumColorEnumColor::fromCpp(const std::unordered_map<::testsuite::color, ::testsuite::color> & dc) {
    djinni::Handle<DjinniObjectHandle> _handle(s_py_callback_map_enum_color_enum_color__python_create(), & map_enum_color_enum_color___delete);
    for (const auto & it : dc) {
        s_py_callback_map_enum_color_enum_color__python_add(_handle.get(), int32_from_enum_color(it.first), int32_from_enum_color(it.second));
    }

    return _handle;
}

std::unordered_map<::testsuite::color, ::testsuite::color> DjinniMapEnumColorEnumColor::toCpp(djinni::Handle<DjinniObjectHandle> dh) {
    std::unordered_map<::testsuite::color, ::testsuite::color>_ret;
    size_t size = s_py_callback_map_enum_color_enum_color__get_size(dh.get());

    for (int i = 0; i < size; i++) {
        auto _key_c = s_py_callback_map_enum_color_enum_color__python_next(dh.get()); // key that would potentially be surrounded by unique pointer
        auto _val = static_cast<::testsuite::color>(s_py_callback_map_enum_color_enum_color__get_value(dh.get(), _key_c));

        auto _key = static_cast<::testsuite::color>(_key_c);
        _ret.emplace(std::move(_key), std::move(_val));
    }

    return _ret;
}

djinni::Handle<DjinniOptionalObjectHandle> DjinniMapEnumColorEnumColor::fromCpp(std::experimental::optional<std::unordered_map<::testsuite::color, ::testsuite::color>> dc) {
    if (dc == std::experimental::nullopt) {
        return nullptr;
    }
    return djinni::optionals::toOptionalHandle(DjinniMapEnumColorEnumColor::fromCpp(std::move(* dc)), optional_map_enum_color_enum_color___delete);
}

std::experimental::optional<std::unordered_map<::testsuite::color, ::testsuite::color>>DjinniMapEnumColorEnumColor::toCpp(djinni::Handle<DjinniOptionalObjectHandle> dh) {
     if (dh) {
        return std::experimental::optional<std::unordered_map<::testsuite::color, ::testsuite::color>>(DjinniMapEnumColorEnumColor::toCpp(djinni::optionals::fromOptionalHandle(std::move(dh), map_enum_color_enum_color___delete)));
    }
    return std::experimental::nullopt;
}

