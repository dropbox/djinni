#include <iostream> // for debugging
#include <cstdint>
#include "foo_static_impl.hpp"

namespace testsuite {

std::string FooStaticImpl::s_messg;
int FooStaticImpl::s_counter;

// STATIC methods FooStatic

std::string FooStatic::update_static_messg_to(const std::string & static_string) {
    return FooStaticImpl::update_static_messg_to(static_string);
}

std::string FooStatic::get_static_messg() {
    return FooStaticImpl::get_static_messg();
}

int32_t FooStatic::update_static_counter_by(int32_t plus) {
    return FooStaticImpl::update_static_counter_by(plus);
}

int32_t FooStatic::update_static_counter_to(int32_t init) {
    return FooStaticImpl::update_static_counter_to(init);
}

int32_t FooStatic::get_static_counter() {
    return FooStaticImpl::get_static_counter();
}

std::chrono::system_clock::time_point FooStatic::date_id(const std::chrono::system_clock::time_point & d) {
    return FooStaticImpl::date_id(d);
}

std::experimental::optional<std::chrono::system_clock::time_point> FooStatic::opt_date_id(const std::experimental::optional<std::chrono::system_clock::time_point> & od) {
    return FooStaticImpl::opt_date_id(od);
}

std::experimental::optional<int64_t> FooStatic::opt_i64_id(std::experimental::optional<int64_t> oi) {
    return FooStaticImpl::opt_i64_id(oi);
}

// STATIC methods FooStaticImpl

std::string FooStaticImpl::update_static_messg_to(const std::string & static_string) {
    FooStaticImpl::s_messg = static_string;
    return FooStaticImpl::s_messg;
}

std::string FooStaticImpl::get_static_messg() {
    return FooStaticImpl::s_messg;
}

int32_t FooStaticImpl::update_static_counter_by(int32_t plus) {
    FooStaticImpl::s_counter += plus;
    return FooStaticImpl::s_counter;
}

int32_t FooStaticImpl::update_static_counter_to(int32_t init) {
    FooStaticImpl::s_counter = init;
    return FooStaticImpl::s_counter;
}

int32_t FooStaticImpl::get_static_counter() {
    return FooStaticImpl::s_counter;
}

std::chrono::system_clock::time_point FooStaticImpl::date_id(const std::chrono::system_clock::time_point & d) {
    return d;
}

std::experimental::optional<std::chrono::system_clock::time_point> FooStaticImpl::opt_date_id(const std::experimental::optional<std::chrono::system_clock::time_point> & od) {
    return od;
}

std::experimental::optional<int64_t> FooStaticImpl::opt_i64_id(std::experimental::optional<int64_t> oi) {
    return oi;
}

} // namespace testsuite
