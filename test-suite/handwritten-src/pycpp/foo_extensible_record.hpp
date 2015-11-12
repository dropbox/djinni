#pragma once

#include <string>

#include "foo_extensible_record_base.hpp"

namespace testsuite {

class FooExtensibleRecord final: public FooExtensibleRecordBase {
public:
    FooExtensibleRecord(int number1, std::string string1): FooExtensibleRecordBase(number1, std::move(string1)),
    number2(number1 * 2), string2(this->string1 + this->string1) {};
    int number2;
    std::string string2;
};

} // namespace testsuite
