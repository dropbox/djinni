#include "sub_cpp_interface_inheritance.hpp"

namespace testsuite {
    
class SubCppInterfaceInheritanceImpl : public SubCppInterfaceInheritance {
public:
    SubCppInterfaceInheritanceImpl();
    virtual ~SubCppInterfaceInheritanceImpl() {}

    virtual std::string base_method() override;

    virtual std::string override_method() override;

    virtual std::string sub_method() override;

private:
    std::shared_ptr<BaseCppInterfaceInheritance> mSuper;
};
    
} // namespace testsuite
