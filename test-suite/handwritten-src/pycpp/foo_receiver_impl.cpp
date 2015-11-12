// Example cpp implementation of foo.hpp
// This file should be hand-written by a cpp developer

#include <iostream>
#include <experimental/optional>
#include "wrapper_marshal.hpp"
#include "foo_receiver_impl.hpp"
#include "foo_some_other_record.hpp"

namespace testsuite {

std::shared_ptr<FooReceiver> FooReceiver::create() {
    return std::make_shared<FooReceiverImpl>();
}

std::string FooReceiverImpl::set_private_string(const std::string & private_string) {
    m_prs = private_string;
    return m_listener->on_string_change(m_prs);
}

std::string FooReceiverImpl::cause_changes_string_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) {
    return m_listener->on_changes_string_returned(i,f,s,binar,b,d);
}

std::vector<uint8_t> FooReceiverImpl::cause_changes_binary_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) {
    return m_listener->on_changes_binary_returned(i,f,s,binar,b,d);
}

std::chrono::system_clock::time_point FooReceiverImpl::cause_changes_date_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) {
    return m_listener->on_changes_date_returned(i,f,s,binar,b,d);
}

int32_t FooReceiverImpl::cause_changes_int_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) {
    return m_listener->on_changes_int_returned(i,f,s,binar,b,d);
}

std::string FooReceiverImpl::get_private_string() {
    return m_prs;
}

void FooReceiverImpl::add_listener(const std::shared_ptr<FooListener> & listener) {
    m_listener = listener;
}

void FooReceiverImpl::add_optional_listener(const std::shared_ptr<FooListener> & listener) {
    m_listener = listener;
}

std::shared_ptr<FooListener>  FooReceiverImpl::get_optional_listener() {
    return m_listener;
}

std::experimental::optional<int32_t> FooReceiverImpl::cause_changes_int_optional_returned(
    std::experimental::optional<int32_t> i,
    float f,
    const std::experimental::optional<std::string> & s ,
    const std::vector<uint8_t> & binar,
    bool b, const std::chrono::system_clock::time_point & d) {

    return m_listener->on_changes_int_optional_returned(i,f,s,binar,b,d);
}

std::experimental::optional<std::string> FooReceiverImpl::cause_changes_string_optional_returned(
    std::experimental::optional<int32_t> i,
    float f,
    const std::experimental::optional<std::string> & s ,
    const std::vector<uint8_t> & binar,
    bool b, const std::chrono::system_clock::time_point & d) {

    return m_listener->on_changes_string_optional_returned(i,f,s,binar,b,d);
}

FooSomeOtherRecord FooReceiverImpl::cause_changes_record_returned(int32_t n1, int32_t n2) {
    return m_listener->on_changes_record_returned(n1, n2);
}

void FooReceiverImpl::cause_cpp_exception(const std::string & s) {
    throw std::runtime_error(std::move(s));
}

void FooReceiverImpl::cause_py_exception(const std::string & s) {
    try {
        m_listener->cause_py_exception(std::move(s));
    } catch (djinni::py_exception & e) {
        throw std::move(e);
    }
}

void FooReceiverImpl::cause_zero_division_error() {
    try {
        m_listener->cause_zero_division_error();
    } catch (djinni::py_exception & e) {
        throw std::move(e);
    }
}

// Receiver functions where the listener we are calling methods on has been
// Passed back and forth as an argument and return value of other methods
std::string FooReceiverImpl::set_private_bf_string(const std::string & private_string) {
    m_prs = private_string;
    return m_listener_bf->on_string_change(m_prs);
}

void FooReceiverImpl::add_listener_bf(const std::shared_ptr<FooListenerBf> & listener) {
    m_listener_bf = listener;
}

std::string FooReceiverImpl::get_listener_bf_string() {
    return m_listener_bf->get_string();
}

std::shared_ptr<FooListenerBf> FooReceiverImpl::get_foo_listener_bf() {
    if (!m_cpp_listener) {
        m_cpp_listener = FooListenerBf::create();
    }
    return m_cpp_listener;
}

std::shared_ptr<FooListenerBf> FooReceiverImpl::send_return(const std::shared_ptr<FooListenerBf> & fl_bf) {
    return fl_bf;
}

void FooReceiverImpl::set_listener_bf_in_listener_bf(const std::shared_ptr<FooListenerBf> & listener) {
    m_listener_bf->set_listener_bf(listener);
}

std::shared_ptr<FooListenerBf> FooReceiverImpl::get_listener_bf_in_listener_bf() {
    return m_listener_bf->get_listener_bf();
}

void FooReceiverImpl::set_binary_in_listener_bf_in_listener_bf(const std::vector<uint8_t> & b) {
    m_listener_bf->set_binary(b);
}

std::vector<uint8_t> FooReceiverImpl::get_binary_in_listener_bf_in_listener_bf() {
    return m_listener_bf->get_binary();
}

std::shared_ptr<FooListenerBf> FooReceiverImpl::in_listener_bf_send_return(const std::shared_ptr<FooListenerBf> & fl_bf) {
    return m_listener_bf->send_return(fl_bf);
}

FooReceiverImpl::~FooReceiverImpl() {
    // Needed for when we have a foo_listner in a foo_listener, because in our test we use the same foo_listener
    if (m_cpp_listener) {
        m_cpp_listener->delete_fl_in_fl();
    }
}

} // namespace testsuite
