#include "interface_encapsulator_impl.hpp"

#include "base_cpp_interface_inheritance_impl.hpp"
#include "sub_cpp_interface_inheritance_impl.hpp"
#include "base_objc_java_interface_inheritance.hpp"
#include "sub_objc_java_interface_inheritance.hpp"

namespace testsuite {    
    
void InterfaceEncapsulatorImpl::set_cpp_object(const std::shared_ptr<BaseCppInterfaceInheritance> & object) {
    mCppObject = object;
}
    
std::shared_ptr<BaseCppInterfaceInheritance> InterfaceEncapsulatorImpl::get_cpp_object() {
    return mCppObject;
}

std::shared_ptr<BaseCppInterfaceInheritance> InterfaceEncapsulatorImpl::sub_cpp_as_base_cpp() {
    return std::make_shared<SubCppInterfaceInheritanceImpl>();
}

void InterfaceEncapsulatorImpl::set_objc_java_object(const std::shared_ptr<BaseObjcJavaInterfaceInheritance> & object) {
    mObjcJavaObject = object;
}

std::shared_ptr<BaseObjcJavaInterfaceInheritance> InterfaceEncapsulatorImpl::get_objc_java_object() {
    return mObjcJavaObject;
}

std::shared_ptr<InterfaceEncapsulator> InterfaceEncapsulator::create() {
    return std::make_shared<InterfaceEncapsulatorImpl>();
}
    
std::shared_ptr<SubObjcJavaInterfaceInheritance> InterfaceEncapsulatorImpl::cast_base_arg_to_sub(const std::shared_ptr<BaseObjcJavaInterfaceInheritance> & subAsBase) {
    auto subAsSub = std::dynamic_pointer_cast<SubObjcJavaInterfaceInheritance>(subAsBase);
    return subAsSub;
}

    
} // namespace testsuite





