#include "interface_encapsulator.hpp"

namespace testsuite {
    
class InterfaceEncapsulatorImpl : public InterfaceEncapsulator {
public:
    InterfaceEncapsulatorImpl() {}
    virtual ~InterfaceEncapsulatorImpl() {}
    
    virtual void set_cpp_object(const std::shared_ptr<BaseCppInterfaceInheritance> & object) override;
    
    virtual std::shared_ptr<BaseCppInterfaceInheritance> get_cpp_object() override;
    
    virtual std::shared_ptr<BaseCppInterfaceInheritance> sub_cpp_as_base_cpp() override;
    
    virtual void set_objc_java_object(const std::shared_ptr<BaseObjcJavaInterfaceInheritance> & object) override;
    
    virtual std::shared_ptr<BaseObjcJavaInterfaceInheritance> get_objc_java_object() override;
    
    virtual std::shared_ptr<SubObjcJavaInterfaceInheritance> cast_base_arg_to_sub(const std::shared_ptr<BaseObjcJavaInterfaceInheritance> & subAsBase) override;
    
private:
    
    std::shared_ptr<BaseCppInterfaceInheritance> mCppObject;
    std::shared_ptr<BaseObjcJavaInterfaceInheritance> mObjcJavaObject;
};
    
} // namespace testsuite
