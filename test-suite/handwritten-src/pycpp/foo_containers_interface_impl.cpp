#include <iostream>
#include "foo_containers_interface_impl.hpp"
#include "foo_some_other_record.hpp"
#include <experimental/optional>

namespace testsuite {

std::shared_ptr<FooContainersInterface> FooContainersInterface::create() {
    return std::make_shared<FooContainersInterfaceImpl>();
}

void FooContainersInterfaceImpl::set_containers_record(const FooContainersRecord & rec) {
    m_record = rec;
}

FooContainersRecord FooContainersInterfaceImpl::get_containers_record() {
    return m_record;
}

void FooContainersInterfaceImpl::set_optional_containers_record(const std::experimental::optional<FooContainersRecord> & rec) {
   if (!rec) {
        m_optional_record = std::experimental::nullopt;
   }
   else {
        m_optional_record = *rec;
   }
}

std::experimental::optional<FooContainersRecord> FooContainersInterfaceImpl::get_optional_containers_record() {
    return m_optional_record;
}

void FooContainersInterfaceImpl::set_optional_map_string_int(const std::experimental::optional<std::unordered_map<std::string, int32_t>> & m) {
    m_optional_map_string_int = m;
}

std::experimental::optional<std::unordered_map<std::string, int32_t>> FooContainersInterfaceImpl::get_optional_map_string_int() {
    return m_optional_map_string_int;
}


void FooContainersInterfaceImpl::set_optional_list_int(const std::experimental::optional<std::vector<int>>  & li) {
    m_optional_list_int = li;
}

std::experimental::optional<std::vector<int>>  FooContainersInterfaceImpl::get_optional_list_int() {
    return m_optional_list_int;
}

void FooContainersInterfaceImpl::set_list_record(const std::vector<FooSomeOtherRecord> & lr) {
    m_list_record = lr;
}

void FooContainersInterfaceImpl::set_list_binary(const std::vector<std::vector<uint8_t>> & lb) {
    m_list_binary = lb;
}

std::vector<std::vector<uint8_t>> FooContainersInterfaceImpl::get_list_binary() {
    return m_list_binary;
}

// void FooContainersInterfaceImpl::set_set_record(const std::unordered_set<FooSomeOtherRecord> & sr) {
//     m_set_record = sr;
// }

// std::unordered_set<FooSomeOtherRecord> FooContainersInterfaceImpl::get_set_record() {
//     return m_set_record;
// }

FooContainersInterfaceImpl::FooContainersInterfaceImpl():
    m_record(FooContainersRecord(m_optional_list_int, m_list_int, m_list_binary, m_list_optional_binary,
        m_list_list_string, m_list_record, m_optional_map_string_int, m_map_string_int, m_map_string_string, m_map_optional_string_optional_string, m_map_int_list_date,
        m_optional_set_string, m_set_string, m_set_optional_string, m_map_int_set_string, m_map_optional_int_set_string)),
    m_optional_record(FooContainersRecord(m_optional_list_int, m_list_int, m_list_binary, m_list_optional_binary,
        m_list_list_string, m_list_record, m_optional_map_string_int, m_map_string_int, m_map_string_string, m_map_optional_string_optional_string, m_map_int_list_date,
        m_optional_set_string, m_set_string, m_set_optional_string, m_map_int_set_string, m_map_optional_int_set_string))
     {};

} // namespace testsuite
