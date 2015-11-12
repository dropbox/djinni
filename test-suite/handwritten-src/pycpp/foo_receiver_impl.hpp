
#pragma once

#include <memory>
#include <string>

#include "foo_receiver.hpp"
#include "foo_listener.hpp"
#include "foo_listener_bf.hpp"

namespace testsuite {

class FooListener;

class FooReceiverImpl final: public FooReceiver {
public:

    ~FooReceiverImpl();

    virtual std::string set_private_string(const std::string & private_string) override;
    virtual std::string get_private_string() override;

    virtual std::string cause_changes_string_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) override;
    virtual std::vector<uint8_t> cause_changes_binary_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) override;
    virtual std::chrono::system_clock::time_point cause_changes_date_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) override;
    virtual int cause_changes_int_returned(int32_t i, float f, const std::string & s, const std::vector<uint8_t> & binar,
                                    bool b, const std::chrono::system_clock::time_point & d) override;
    virtual FooSomeOtherRecord cause_changes_record_returned(int32_t n1, int32_t n2) override;

    virtual std::experimental::optional<int32_t> cause_changes_int_optional_returned(
        std::experimental::optional<int32_t>,
        float,
        const std::experimental::optional<std::string> &,
        const std::vector<uint8_t> &,
        bool, const std::chrono::system_clock::time_point &) override;

    virtual std::experimental::optional<std::string> cause_changes_string_optional_returned(
        std::experimental::optional<int32_t>,
        float,
        const std::experimental::optional<std::string> &,
        const std::vector<uint8_t> &,
        bool, const std::chrono::system_clock::time_point &) override;

    virtual void cause_cpp_exception(const std::string & s) override;
    virtual void cause_py_exception(const std::string & s) override;
    virtual void cause_zero_division_error() override;

    virtual void add_listener(const std::shared_ptr<FooListener> & listener) override;
    virtual void add_optional_listener(const std::shared_ptr<FooListener> & listener) override;
    virtual std::shared_ptr<FooListener> get_optional_listener() override;

    // former receiver bf functions
    virtual std::string set_private_bf_string(const std::string & private_string) override;

    virtual void add_listener_bf(const std::shared_ptr<FooListenerBf> & listener) override;
    virtual std::shared_ptr<FooListenerBf> get_foo_listener_bf() override;
    virtual std::string get_listener_bf_string() override;

    virtual std::shared_ptr<FooListenerBf> send_return(const std::shared_ptr<FooListenerBf> & fl_bf) override;

    virtual void set_listener_bf_in_listener_bf(const std::shared_ptr<FooListenerBf> & listener) override;
    virtual std::shared_ptr<FooListenerBf> get_listener_bf_in_listener_bf() override;
    virtual void set_binary_in_listener_bf_in_listener_bf(const std::vector<uint8_t> & s) override;
    virtual std::vector<uint8_t> get_binary_in_listener_bf_in_listener_bf() override;
    virtual std::shared_ptr<FooListenerBf> in_listener_bf_send_return(const std::shared_ptr<FooListenerBf> & fl_bf) override;

private:
    std::shared_ptr<FooListener> m_listener;
    std::shared_ptr<FooListenerBf> m_listener_bf; // python or cpp impl
    std::shared_ptr<FooListenerBf> m_cpp_listener; // access to cpp implementation of listener

    int32_t m_pri = 0;
    std::string m_prs;
};

} // namespace testsuite
