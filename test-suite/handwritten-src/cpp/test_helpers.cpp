#include "test_helpers.hpp"
#include "client_returned_record.hpp"
#include "client_interface.hpp"
#include <exception>

SetRecord TestHelpers::get_set_record() {
    return SetRecord { {
        "StringA",
        "StringB",
        "StringC"
    } };
}

bool TestHelpers::check_set_record(const SetRecord & rec) {
    return rec.set == std::unordered_set<std::string>{ "StringA", "StringB", "StringC" };
}

static const PrimitiveList cPrimitiveList { { 1, 2, 3 } };

PrimitiveList TestHelpers::get_primitive_list() {
    return cPrimitiveList;
}

bool TestHelpers::check_primitive_list(const PrimitiveList & pl) {
    return pl.list == cPrimitiveList.list;
}

static const NestedCollection cNestedCollection { { {u8"String1", u8"String2"},
                                                    {u8"StringA", u8"StringB"} } };

NestedCollection TestHelpers::get_nested_collection() {
    return cNestedCollection;
}

bool TestHelpers::check_nested_collection(const NestedCollection & nc) {
    return nc.set_list == cNestedCollection.set_list;
}

static const std::unordered_map<std::string, int64_t> cMap = {
    { "String1", 1 },
    { "String2", 2 },
    { "String3", 3 },
};

std::unordered_map<std::string, int64_t> TestHelpers::get_map() {
    return cMap;
}

bool TestHelpers::check_map(const std::unordered_map<std::string, int64_t> & m) {
    return m == cMap;
}

std::unordered_map<std::string, int64_t> TestHelpers::get_empty_map() {
    return std::unordered_map<std::string, int64_t>();
}

bool TestHelpers::check_empty_map(const std::unordered_map<std::string,int64_t> & m) {
    return m.empty();
}

MapListRecord TestHelpers::get_map_list_record() {
    return { { cMap } };
}

bool TestHelpers::check_map_list_record(const MapListRecord & rec) {
    return rec.map_list.size() == 1 && rec.map_list[0] == cMap;
}

static const std::string HELLO_WORLD = "Hello World!";
static const std::string NON_ASCII = "Non-ASCII / 非 ASCII 字符";

void TestHelpers::check_client_interface_ascii(const std::shared_ptr<ClientInterface> & i) {
    ClientReturnedRecord cReturnedRecord = i->get_record(HELLO_WORLD);
    if (cReturnedRecord.content != HELLO_WORLD) {
        std::string error_msg = "Expected String: " + HELLO_WORLD + " Actual: " + cReturnedRecord.content;
        throw std::invalid_argument(error_msg);
    }
}

void TestHelpers::check_client_interface_nonascii(const std::shared_ptr<ClientInterface> & i) {
    ClientReturnedRecord cReturnedRecord = i->get_record(NON_ASCII);
    if (cReturnedRecord.content != NON_ASCII) {
        std::string error_msg = "Expected String: " + NON_ASCII + " Actual: " + cReturnedRecord.content;
        throw std::invalid_argument(error_msg);
    }
}

std::experimental::optional<int32_t> TestHelpers::return_none() {
    return {};
}

void TestHelpers::check_enum_map(const std::unordered_map<color, std::string> & m) {
    std::unordered_map<color, std::string> expected = {
        { color::RED,    "red"    },
        { color::ORANGE, "orange" },
        { color::YELLOW, "yellow" },
        { color::GREEN,  "green"  },
        { color::BLUE,   "blue"   },
        { color::INDIGO, "indigo" },
        { color::VIOLET, "violet" },
    };

    if (m != expected) {
        throw std::invalid_argument("map mismatch");
    }
}

AssortedIntegers TestHelpers::assorted_integers_id(const AssortedIntegers & i) {
    return i;
}
