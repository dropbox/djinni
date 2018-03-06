#pragma once

#include "foo_enum_interface.hpp"
#include "color.hpp"
#include <memory>
#include <experimental/optional>

namespace testsuite {

class FooEnumInterfaceImpl final: public FooEnumInterface {
public:

    virtual void set_enum(color some_color) override;
    virtual color get_enum() override;

    virtual void set_optional_enum(std::experimental::optional<color> some_color) override;
    virtual std::experimental::optional<color> get_optional_enum() override;

    static std::shared_ptr<FooEnumInterface> create();
private:
    color m_color;
    std::experimental::optional<color> m_optional_color;
};

} // namespace testsuite
