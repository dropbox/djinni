#include "foo_enum_interface_impl.hpp"

namespace testsuite {

std::shared_ptr<FooEnumInterface> FooEnumInterface::create() {
    return std::make_shared<FooEnumInterfaceImpl>();
}

void FooEnumInterfaceImpl::set_enum(color some_color) {
    m_color = some_color;
}

color FooEnumInterfaceImpl::get_enum() {
    return m_color;
}

void FooEnumInterfaceImpl::set_optional_enum(std::experimental::optional<color> some_color) {
    m_optional_color = some_color;
}

std::experimental::optional<color> FooEnumInterfaceImpl::get_optional_enum() {
    return m_optional_color;
}

} // namespace testsuite
