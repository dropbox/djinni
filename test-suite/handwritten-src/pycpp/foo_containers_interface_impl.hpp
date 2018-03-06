#pragma once

#include "foo_containers_record.hpp"
#include "foo_containers_interface.hpp"
#include "foo_some_other_record.hpp"
#include <memory>

namespace testsuite {

class FooContainersInterfaceImpl final: public FooContainersInterface {
public:
    FooContainersInterfaceImpl();

    virtual void set_containers_record(const FooContainersRecord & rec) override;
    virtual FooContainersRecord get_containers_record() override;

    virtual void set_optional_containers_record(const std::experimental::optional<FooContainersRecord> & rec) override;
    virtual std::experimental::optional<FooContainersRecord> get_optional_containers_record() override;

    virtual void set_optional_map_string_int(const std::experimental::optional<std::unordered_map<std::string, int32_t>> & m) override;
    virtual std::experimental::optional<std::unordered_map<std::string, int32_t>> get_optional_map_string_int() override;

    virtual void set_optional_list_int(const std::experimental::optional<std::vector<int>>  & li) override;
    virtual std::experimental::optional<std::vector<int>>  get_optional_list_int() override;

    virtual void set_list_record(const std::vector<FooSomeOtherRecord> & lr) override;
    virtual void set_list_binary(const std::vector<std::vector<uint8_t>> & lb) override;
    virtual std::vector<std::vector<uint8_t>> get_list_binary() override;
    // virtual void set_set_record(const std::unordered_set<FooSomeOtherRecord> & sr) override;
    // virtual std::unordered_set<FooSomeOtherRecord> get_set_record() override;
    static std::shared_ptr<FooContainersInterface> create();
private:
    std::experimental::optional<std::vector<int>> m_optional_list_int;
    std::vector<int> m_list_int;
    std::vector<std::vector<uint8_t>> m_list_binary;
    std::vector<std::experimental::optional<std::vector<uint8_t>>> m_list_optional_binary;
    std::vector<std::vector<std::string>> m_list_list_string;
    std::vector<FooSomeOtherRecord> m_list_record;
    std::experimental::optional<std::unordered_map<std::string, int32_t>> m_optional_map_string_int;
    std::unordered_map<std::string, int32_t> m_map_string_int;
    std::unordered_map<std::string, std::string> m_map_string_string;
    std::unordered_map<std::experimental::optional<std::string>, std::experimental::optional<std::string>> m_map_optional_string_optional_string;
    std::unordered_map<int8_t, std::vector<std::chrono::system_clock::time_point>> m_map_int_list_date;

    std::unordered_map<int8_t, std::unordered_set<std::string>> m_map_int_set_string;
    std::unordered_map<std::experimental::optional<int32_t>, std::unordered_set<std::string>> m_map_optional_int_set_string;
    std::experimental::optional<std::unordered_set<std::string>> m_optional_set_string;
    std::unordered_set<std::string> m_set_string;
    std::unordered_set<std::experimental::optional<std::string>> m_set_optional_string;
    FooContainersRecord m_record;

    // std::unordered_set<FooSomeOtherRecord> m_set_record;

    std::experimental::optional<FooContainersRecord>  m_optional_record;
};

} // namespace testsuite
