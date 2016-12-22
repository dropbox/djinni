#include "extended_record.hpp"

// Validate these generated headers are compilable.
#include "record_using_extended_record.hpp"
#include "interface_using_extended_record.hpp"

using namespace testsuite;

ExtendedRecord::ExtendedRecord() : ExtendedRecordBase(true) {}
