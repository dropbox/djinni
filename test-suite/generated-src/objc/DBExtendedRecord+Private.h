// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from test.djinni

#import "DBExtendedRecord.h"
#include "extended_record.hpp"

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@class DBExtendedRecord;

namespace djinni_generated {

struct ExtendedRecord
{
    using CppType = ::testsuite::ExtendedRecord;
    using ObjcType = DBExtendedRecord*;

    using Boxed = ExtendedRecord;

    static CppType toCpp(ObjcType objc);
    static ObjcType fromCpp(const CppType& cpp);
};

}  // namespace djinni_generated
