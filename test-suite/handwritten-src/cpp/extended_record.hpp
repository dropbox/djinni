#pragma once

#include "extended_record_base.hpp"

namespace testsuite {
    
    struct ExtendedRecord : public ExtendedRecordBase
    {
        using ExtendedRecordBase::ExtendedRecordBase;
        
        ExtendedRecord();
    };
}