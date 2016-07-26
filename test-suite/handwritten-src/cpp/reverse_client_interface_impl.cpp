#include "reverse_client_interface_impl.hpp"

namespace testsuite {

std::string ReverseClientInterfaceImpl::return_str() const {
    return "test";
}

std::string ReverseClientInterfaceImpl::meth_taking_interface(const std::shared_ptr<ReverseClientInterface> & i) {
    if (i) {
        return i->return_str();
    } else {
        return {};
    }
}

std::string ReverseClientInterfaceImpl::meth_taking_optional_interface(const std::shared_ptr<ReverseClientInterface> & i) {
    if (i) {
        return i->return_str();
    } else {
        return {};
    }
}

std::shared_ptr<ReverseClientInterface> ReverseClientInterface::create() {
    return std::make_shared<ReverseClientInterfaceImpl>();
}

} // namespace testsuite
