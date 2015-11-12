#include <iostream>
#include <string>

#include "foo_listener_bf_impl.hpp"

namespace testsuite {

std::string FooListenerBfImpl::on_string_change(const std::string & private_string) {
    m_string = private_string;
    return m_string;
}

std::shared_ptr<FooListenerBf> FooListenerBf::create() {
    return std::make_shared<FooListenerBfImpl>();
}

std::string FooListenerBfImpl::get_string() {
    return m_string;
}


void FooListenerBfImpl::set_listener_bf(const std::shared_ptr<FooListenerBf> & listener) {
    m_foo_listener_bf = listener;
}

std::shared_ptr<FooListenerBf> FooListenerBfImpl::get_listener_bf() {
    return m_foo_listener_bf;
}

void FooListenerBfImpl::set_binary(const std::vector<uint8_t> & b) {
    m_prbin = b;
}

std::vector<uint8_t> FooListenerBfImpl::get_binary() {
    return m_prbin;
}

std::shared_ptr<FooListenerBf> FooListenerBfImpl::send_return(const std::shared_ptr<FooListenerBf> & fl_bf) {
    return fl_bf;
}

void FooListenerBfImpl::delete_fl_in_fl() {
    // std::cout<< "In delete_fl_in_fl " << m_foo_listener_bf.use_count() << std::endl;
    m_foo_listener_bf = nullptr;
}

} // namespace testsuite
