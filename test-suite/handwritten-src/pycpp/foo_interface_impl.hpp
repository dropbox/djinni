// Example cpp implementation of foo.hpp
// This file should be hand-written by a cpp developer

#pragma once

#include <string>
#include "foo_interface.hpp"
#include "foo_primitives.hpp"

namespace testsuite {

class FooInterfaceImpl final: public FooInterface {
public:
    virtual int32_t int32_inverse(int32_t x) override;

    virtual void set_private_int32(int32_t pri) override;
    virtual int32_t get_private_int32() override;

    virtual void set_private_string(const std::string& prs) override;
    virtual std::string get_private_string() override;

    virtual std::string get_set_strings(const std::string& ps1, const std::string & ps2) override;

    virtual std::shared_ptr<FooPrimitives> get_foo_primitives() override;

    static std::shared_ptr<FooInterface> create();
private:
    int32_t m_pri = 0;
    std::string m_prs;

    std::shared_ptr<FooPrimitives> m_foo_primitives;
};

} // namespace testsuite
