#include "base_cpp_interface_inheritance_impl.hpp"
#include "interface_inheritance_constant.hpp"

namespace testsuite {
    
std::string BaseCppInterfaceInheritanceImpl::base_method() {
    return InterfaceInheritanceConstant::BASE_METHOD_RETURN_VALUE;
}
    
std::string BaseCppInterfaceInheritanceImpl::override_method() {
    return InterfaceInheritanceConstant::BASE_OVERRIDE_METHOD_RETURN_VALUE;
}
    
std::shared_ptr<BaseCppInterfaceInheritance> BaseCppInterfaceInheritance::create() {
    return std::make_shared<BaseCppInterfaceInheritanceImpl>();
}
    
} // namespace testsuite
