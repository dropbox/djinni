#include "test_helpers.hpp"
#include "client_returned_record.hpp"
#include "client_interface.hpp"
#include "token.hpp"
#include <exception>

djinni_generated::SetRecord djinni_generated::TestHelpers::get_set_record() {
    return SetRecord { {
        "StringA",
        "StringB",
        "StringC"
    } };
}

bool djinni_generated::TestHelpers::check_set_record(const SetRecord & rec) {
    return rec.set == std::unordered_set<std::string>{ "StringA", "StringB", "StringC" };
}

static const djinni_generated::PrimitiveList cPrimitiveList { { 1, 2, 3 } };

djinni_generated::PrimitiveList djinni_generated::TestHelpers::get_primitive_list() {
    return cPrimitiveList;
}

bool djinni_generated::TestHelpers::check_primitive_list(const PrimitiveList & pl) {
    return pl.list == cPrimitiveList.list;
}

static const djinni_generated::NestedCollection cNestedCollection { { {u8"String1", u8"String2"},
																  {u8"StringA", u8"StringB"} } };

djinni_generated::NestedCollection djinni_generated::TestHelpers::get_nested_collection() {
    return cNestedCollection;
}

bool djinni_generated::TestHelpers::check_nested_collection(const NestedCollection & nc) {
    return nc.set_list == cNestedCollection.set_list;
}

static const std::unordered_map<std::string, int64_t> cMap = {
    { "String1", 1 },
    { "String2", 2 },
    { "String3", 3 },
};

std::unordered_map<std::string, int64_t> djinni_generated::TestHelpers::get_map() {
    return cMap;
}

bool djinni_generated::TestHelpers::check_map(const std::unordered_map<std::string, int64_t> & m) {
    return m == cMap;
}

std::unordered_map<std::string, int64_t> djinni_generated::TestHelpers::get_empty_map() {
    return std::unordered_map<std::string, int64_t>();
}

bool djinni_generated::TestHelpers::check_empty_map(const std::unordered_map<std::string,int64_t> & m) {
    return m.empty();
}

djinni_generated::MapListRecord djinni_generated::TestHelpers::get_map_list_record() {
    return { { cMap } };
}

bool djinni_generated::TestHelpers::check_map_list_record(const MapListRecord & rec) {
    return rec.map_list.size() == 1 && rec.map_list[0] == cMap;
}

static const std::string HELLO_WORLD = "Hello World!";
static const std::string NON_ASCII = "Non-ASCII / 非 ASCII 字符";

void djinni_generated::TestHelpers::check_client_interface_ascii(const std::shared_ptr<ClientInterface> & i) {
    ClientReturnedRecord cReturnedRecord = i->get_record(5, HELLO_WORLD);
    if (cReturnedRecord.content != HELLO_WORLD) {
        std::string error_msg = "Expected String: " + HELLO_WORLD + " Actual: " + cReturnedRecord.content;
        throw std::invalid_argument(error_msg);
    }
}

void djinni_generated::TestHelpers::check_client_interface_nonascii(const std::shared_ptr<ClientInterface> & i) {
    ClientReturnedRecord cReturnedRecord = i->get_record(5, NON_ASCII);
    if (cReturnedRecord.content != NON_ASCII) {
        std::string error_msg = "Expected String: " + NON_ASCII + " Actual: " + cReturnedRecord.content;
        throw std::invalid_argument(error_msg);
    }
}

std::shared_ptr<djinni_generated::Token> djinni_generated::TestHelpers::token_id(const std::shared_ptr<Token> & in) {
    return in;
}

class CppToken : public djinni_generated::Token {};

std::shared_ptr<djinni_generated::Token> djinni_generated::TestHelpers::create_cpp_token() {
    return std::make_shared<CppToken>();
}

void djinni_generated::TestHelpers::check_cpp_token(const std::shared_ptr<Token> & in) {
    // Throws bad_cast if type is wrong
    (void)dynamic_cast<CppToken &>(*in);
}

int64_t djinni_generated::TestHelpers::cpp_token_id(const std::shared_ptr<Token> & in) {
    return reinterpret_cast<int64_t>(in.get());
}

std::experimental::optional<int32_t> djinni_generated::TestHelpers::return_none() {
    return {};
}

void djinni_generated::TestHelpers::check_enum_map(const std::unordered_map<color, std::string> & m) {
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

djinni_generated::AssortedIntegers djinni_generated::TestHelpers::assorted_integers_id(const AssortedIntegers & i) {
    return i;
}
