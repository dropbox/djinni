#include "test_helpers.hpp"
#include "client_returned_record.hpp"
#include "client_interface.hpp"
#include "user_token.hpp"
#include "assorted_primitives.hpp"
#include "color.hpp"
#include "map_list_record.hpp"
#include "nested_collection.hpp"
#include "primitive_list.hpp"
#include "set_record.hpp"
#include <exception>

namespace testsuite {

SetRecord TestHelpers::get_set_record() {
    return SetRecord { {
        "StringA",
        "StringB",
        "StringC"
    }, std::unordered_set<int32_t>{} };
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
static const std::string NON_ASCII("Non-ASCII /\0 非 ASCII 字符",
                                   sizeof("Non-ASCII /\0 非 ASCII 字符")-1);

void TestHelpers::check_client_interface_ascii(const std::shared_ptr<ClientInterface> & i) {
    ClientReturnedRecord cReturnedRecord = i->get_record(5, HELLO_WORLD, {});
    if (cReturnedRecord.content != HELLO_WORLD) {
        std::string error_msg = "Expected String: " + HELLO_WORLD + " Actual: " + cReturnedRecord.content;
        throw std::invalid_argument(error_msg);
    }
}

void TestHelpers::check_client_interface_nonascii(const std::shared_ptr<ClientInterface> & i) {
    ClientReturnedRecord cReturnedRecord = i->get_record(5, NON_ASCII, {});
    if (cReturnedRecord.content != NON_ASCII) {
        std::string error_msg = "Expected String: " + NON_ASCII + " Actual: " + cReturnedRecord.content;
        throw std::invalid_argument(error_msg);
    }
}

void TestHelpers::check_client_interface_args(const std::shared_ptr<ClientInterface> & i) {
    const std::string returned1 = i->meth_taking_interface(i);
    if ("test" != returned1) {
        std::string error_msg = "Expected String: 'test' Actual: '" + returned1 + "'";
        throw std::invalid_argument(error_msg);
    }

    const std::string returned2 = i->meth_taking_optional_interface(i);
    if ("test" != returned2) {
        std::string error_msg = "Expected String: 'test' Actual: '" + returned2 + "'";
        throw std::invalid_argument(error_msg);
    }
}

std::shared_ptr<UserToken> TestHelpers::token_id(const std::shared_ptr<UserToken> & in) {
    return in;
}

class CppToken : public UserToken {
    std::string whoami() { return "C++"; }
};

std::shared_ptr<UserToken> TestHelpers::create_cpp_token() {
    return std::make_shared<CppToken>();
}

void TestHelpers::check_cpp_token(const std::shared_ptr<UserToken> & in) {
    // Throws bad_cast if type is wrong
    (void)dynamic_cast<CppToken &>(*in);
}

int64_t TestHelpers::cpp_token_id(const std::shared_ptr<UserToken> & in) {
    return reinterpret_cast<int64_t>(in.get());
}

void TestHelpers::check_token_type(const std::shared_ptr<UserToken> &t, const std::string & type) {
    if (t->whoami() != type) {
        throw std::invalid_argument("wrong token type");
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

void TestHelpers::check_enum(color) {} // stub

AssortedPrimitives TestHelpers::assorted_primitives_id(const AssortedPrimitives & p) {
    return p;
}

std::vector<uint8_t> TestHelpers::id_binary(const std::vector<uint8_t> & v) {
    return v;
}

} // namespace testsuite
