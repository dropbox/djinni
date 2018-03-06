
#include "foo_listener_bf.hpp"

namespace testsuite {

class FooListenerBfImpl final: public FooListenerBf {
public:
    ~FooListenerBfImpl() {
        // std::cout<< "In Destructor of FooListenerBfImpl, internal shared_ptr (fl_in_fl) " << m_foo_listener_bf.use_count() << std::endl;
    }

    virtual void delete_fl_in_fl() override;


    virtual std::string on_string_change(const std::string & private_string) override;
    virtual std::string get_string() override;

    virtual void set_listener_bf(const std::shared_ptr<FooListenerBf> & listener) override;
    virtual std::shared_ptr<FooListenerBf> get_listener_bf() override;
    virtual void set_binary(const std::vector<uint8_t> & b) override;
    virtual std::vector<uint8_t> get_binary() override;
    virtual std::shared_ptr<FooListenerBf> send_return(const std::shared_ptr<FooListenerBf> & fl_bf) override;

    static std::shared_ptr<FooListenerBf> create();
private:
    std::string m_string;
    std::vector<uint8_t> m_prbin;

    std::shared_ptr<FooListenerBf> m_foo_listener_bf;
};

} // namespace testsuite
