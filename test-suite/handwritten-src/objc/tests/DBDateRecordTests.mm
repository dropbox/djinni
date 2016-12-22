#import "DBDateRecord+Private.h"
#include <chrono>
#include <thread>
#include "date_record.hpp"
#import <XCTest/XCTest.h>

using namespace testsuite;

@interface DBDateRecordTests : XCTestCase

@end

@implementation DBDateRecordTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

-(BOOL)datesAreAlmostEqual:(NSDate*)date1 date2:(NSDate*)date2 {
    const double time_1 = [date1 timeIntervalSince1970];
    const double time_2 = [date2 timeIntervalSince1970];

    // assert that time's are accurate to 1 microsecond
    if (time_1 > time_2) {
        return time_1 - time_2 < 1.0e-6;
    } else if (time_2 > time_1) {
        return time_2 - time_1 < 1.0e-6;
    }
    return time_1 == time_2;
}

- (void)testObjcRoundTrip
{
    NSDate *now = [NSDate date];
    DBDateRecord *date1 = [[DBDateRecord alloc] initWithCreatedAt:now];
    const auto cpp_date1 = djinni_generated::DateRecord::toCpp(date1);
    DBDateRecord *date2 = djinni_generated::DateRecord::fromCpp(cpp_date1);
    const auto cpp_date2 = djinni_generated::DateRecord::toCpp(date2);
    DBDateRecord *date3 = djinni_generated::DateRecord::fromCpp(cpp_date2);
    const auto cpp_date3 = djinni_generated::DateRecord::toCpp(date3);
    const bool cpp_is_equal = cpp_date1.created_at == cpp_date2.created_at && cpp_date2.created_at == cpp_date3.created_at;
    // cpp is a integer representation (with less precision than NSDate), so direct comparison will work
    XCTAssertTrue(cpp_is_equal);

    // We need to make a single pass through djinni to lose the precision of date1.
    // So date1 and date2 will only be close, not exactly equal.
    XCTAssertTrue([self datesAreAlmostEqual:date1.createdAt date2:date2.createdAt]);
    // However date2 and date3 will have already had precision stripped (by a pass through djinni),
    // so they can be directly compared.
    XCTAssertTrue([date2.createdAt isEqualToDate:date3.createdAt]);

}

- (void)testCppRoundTrip
{
    const auto now = std::chrono::system_clock::now();
    DateRecord cpp_date_now(now);
    DBDateRecord *objcDate = djinni_generated::DateRecord::fromCpp(cpp_date_now);
    const auto boomerang_cpp_date = djinni_generated::DateRecord::toCpp(objcDate);
    XCTAssertTrue(now == boomerang_cpp_date.created_at);
}

@end
