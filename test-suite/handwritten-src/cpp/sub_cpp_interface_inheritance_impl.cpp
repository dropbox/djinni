#include "sub_cpp_interface_inheritance_impl.hpp"
#include "base_cpp_interface_inheritance_impl.hpp"
#include "interface_inheritance_constant.hpp"

namespace testsuite {

SubCppInterfaceInheritanceImpl::SubCppInterfaceInheritanceImpl()
    : mSuper{std::make_shared<BaseCppInterfaceInheritanceImpl>()} {};

std::string SubCppInterfaceInheritanceImpl::base_method() {
    return mSuper->base_method();
}
    
std::string SubCppInterfaceInheritanceImpl::override_method() {
    return InterfaceInheritanceConstant::SUB_OVERRIDE_METHOD_RETURN_VALUE;
}

std::string SubCppInterfaceInheritanceImpl::sub_method() {
    return InterfaceInheritanceConstant::SUB_METHOD_RETURN_VALUE;
}

std::shared_ptr<SubCppInterfaceInheritance> SubCppInterfaceInheritance::create() {
    return std::make_shared<SubCppInterfaceInheritanceImpl>();
}

} // namespace testsuite
