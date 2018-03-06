#pragma once

#include "foo_client_interface.hpp"
#include "foo_client_returned_record.hpp"
#include "foo_extensible_record.hpp"

namespace testsuite {

class FooClientInterfaceImpl final: public FooClientInterface {
public:
    FooClientInterfaceImpl();

    virtual void set_record(const FooClientReturnedRecord & rec) override;

    virtual FooClientReturnedRecord get_record() override;

    virtual void set_extensible_record(const FooExtensibleRecord & rec) override;

    virtual FooExtensibleRecord get_extensible_record() override;

    virtual int32_t get_extensible_record_number2() override;

    virtual std::string get_extensible_record_string2() override;

    static std::shared_ptr<FooClientInterface> create();

private:
    FooClientReturnedRecord m_record;
    FooExtensibleRecord m_ext_record;
};

} // namespace testsuite
