// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from foo_duplicate_file_creation.djinni

#pragma once

#include <atomic>
#include <experimental/optional>
#include "Foo_Record.hpp"
#ifdef __cplusplus
extern "C" {
#endif

#include "dh__Foo_Record.h"

#ifdef __cplusplus
}
#endif
struct DjinniFooRecord {
    static djinni::Handle<DjinniRecordHandle> fromCpp(const ::testsuite::FooRecord& dr);
    static ::testsuite::FooRecord toCpp(djinni::Handle<DjinniRecordHandle> dh);
    static djinni::Handle<DjinniOptionalRecordHandle> fromCpp(std::experimental::optional<::testsuite::FooRecord> dc);
    static std::experimental::optional<::testsuite::FooRecord> toCpp(djinni::Handle<DjinniOptionalRecordHandle> dh);
};
