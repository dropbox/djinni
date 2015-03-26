// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#pragma once

#include <cstdint>
#include <string>
#include <utility>

namespace djinni_generated {

struct ClientReturnedRecord final {

    int64_t record_id;

    std::string content;


    ClientReturnedRecord(
            int64_t record_id,
            std::string content) :
                record_id(std::move(record_id)),
                content(std::move(content)) {
    }
};

}  // namespace djinni_generated
