// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#pragma once

#include "../../handwritten-src/cpp/optional.hpp"
#include "project_export.hpp"
#include <cstdint>
#include <memory>
#include <string>
#include <vector>

namespace testsuite {

struct ClientReturnedRecord;

/** Client interface */
class PROJECT_EXPORT ClientInterface {
public:
    virtual ~ClientInterface() {}

    /** Returns record of given string */
    virtual ClientReturnedRecord get_record(int64_t record_id, const std::string & utf8string, const std::experimental::optional<std::string> & misc) = 0;

    virtual double identifier_check(const std::vector<uint8_t> & data, int32_t r, int64_t jret) = 0;

    virtual std::string return_str() = 0;

    virtual std::string meth_taking_interface(const std::shared_ptr<ClientInterface> & i) = 0;

    virtual std::string meth_taking_optional_interface(const std::shared_ptr<ClientInterface> & i) = 0;
};

}  // namespace testsuite
