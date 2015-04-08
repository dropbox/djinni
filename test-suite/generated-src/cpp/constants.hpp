// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

#pragma once

#include <cstdint>
#include <string>
#include <utility>

namespace djinni_generated {

struct Constants final
{

    static const bool BOOL_CONSTANT;

    static const int8_t I8_CONSTANT;

    static const int16_t I16_CONSTANT;

    static const int32_t I32_CONSTANT;

    static const int64_t I64_CONSTANT;

    static const double F64_CONSTANT;

    static const std::string STRING_CONSTANT;
    int32_t some_integer;
    std::string some_string;

    Constants(int32_t some_integer,
              std::string some_string)
    : some_integer(std::move(some_integer))
    , some_string(std::move(some_string))
    {}
};

}  // namespace djinni_generated
