
#include <iostream>
#include "foo_client_interface_impl.hpp"

namespace testsuite {

std::shared_ptr<FooClientInterface> FooClientInterface::create() {
    return std::make_shared<FooClientInterfaceImpl>();
}

void FooClientInterfaceImpl::set_record(const FooClientReturnedRecord & rec) {
    m_record = rec;
}

FooClientReturnedRecord FooClientInterfaceImpl::get_record() {
    return m_record;
}

void FooClientInterfaceImpl::set_extensible_record(const FooExtensibleRecord & rec) {
    m_ext_record = rec;
}

FooExtensibleRecord FooClientInterfaceImpl::get_extensible_record() {
    return m_ext_record;
}

int32_t FooClientInterfaceImpl::get_extensible_record_number2() {
    return m_ext_record.number2;
}

std::string FooClientInterfaceImpl::get_extensible_record_string2() {
    return m_ext_record.string2;
}

FooClientInterfaceImpl::FooClientInterfaceImpl(): m_record(FooClientReturnedRecord(5,"hi", FooSomeOtherRecord(1,2))),
 m_ext_record(6, "bye"){};

} // namespace testsuite
