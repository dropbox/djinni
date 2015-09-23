// Example cpp implementation of foo.hpp
// This file should be hand-written by a cpp developer

#include <stdio.h>
#include "foo_interface_impl.hpp"

namespace testsuite {

std::shared_ptr<FooInterface> FooInterface::create() {
    return std::make_shared<FooInterfaceImpl>();
}

int32_t FooInterfaceImpl::int32_inverse(int32_t x) {
    return -x;
}

void FooInterfaceImpl::set_private_int32(int32_t pri) {
    m_pri = pri;
}

int32_t FooInterfaceImpl::get_private_int32() {
    return m_pri;
}

void FooInterfaceImpl::set_private_string(const std::string& prs) {
    m_prs = prs;
}

std::string FooInterfaceImpl::get_private_string() {
    return m_prs;
}

std::string FooInterfaceImpl::get_set_strings(const std::string& ps1, const std::string & ps2) {
    m_prs = ps2;
    return m_prs;
}

std::shared_ptr<FooPrimitives> FooInterfaceImpl::get_foo_primitives() {
    if (!m_foo_primitives) {
        m_foo_primitives = FooPrimitives::create();
    }
    return m_foo_primitives;
}

} // namespace testsuite
