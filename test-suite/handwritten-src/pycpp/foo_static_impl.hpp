#pragma once

#include <cstdint>
#include <string>
#include <vector>
#include "foo_static.hpp"

namespace testsuite {

class FooStaticImpl final: public FooStatic {
public:
    static std::string update_static_messg_to(const std::string & static_string);

    static std::string get_static_messg();

    static int32_t update_static_counter_by(int32_t plus);

    static int32_t update_static_counter_to(int32_t init);

    static int32_t get_static_counter();

    static std::chrono::system_clock::time_point date_id(const std::chrono::system_clock::time_point & d);

    static std::experimental::optional<std::chrono::system_clock::time_point> opt_date_id(const std::experimental::optional<std::chrono::system_clock::time_point> & od);

    static std::experimental::optional<int64_t> opt_i64_id(std::experimental::optional<int64_t> oi);

private:
    static std::string s_messg;
    static int s_counter;
};

} // namespace testsuite

