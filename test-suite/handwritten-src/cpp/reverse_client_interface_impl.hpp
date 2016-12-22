#include "reverse_client_interface.hpp"

namespace testsuite {

class ReverseClientInterfaceImpl : public ReverseClientInterface {
    public:
    ReverseClientInterfaceImpl() {}
    virtual ~ReverseClientInterfaceImpl() {}

    virtual std::string return_str() const override;

    virtual std::string meth_taking_interface(const std::shared_ptr<ReverseClientInterface> & i) override;

    virtual std::string meth_taking_optional_interface(const std::shared_ptr<ReverseClientInterface> & i) override;

    static std::shared_ptr<ReverseClientInterface> create();
};

} // namespace testsuite
