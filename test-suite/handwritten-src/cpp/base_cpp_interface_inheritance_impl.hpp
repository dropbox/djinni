#include "base_cpp_interface_inheritance.hpp"

namespace testsuite {
    
class BaseCppInterfaceInheritanceImpl : public BaseCppInterfaceInheritance {
public:
    BaseCppInterfaceInheritanceImpl() {}
    virtual ~BaseCppInterfaceInheritanceImpl() {}
    
    virtual std::string base_method() override;
  
    virtual std::string override_method() override;
};
    
} // namespace testsuite
