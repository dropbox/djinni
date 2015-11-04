#import "record_with_derivings.hpp"
#import "record_with_nested_derivings.hpp"

#import <XCTest/XCTest.h>

using namespace testsuite;

static RecordWithDerivings record1(1, "String1");
static RecordWithDerivings record1A(1, "String1");
static RecordWithDerivings record2(1, "String2");
static RecordWithDerivings record3(2, "String1");

static RecordWithNestedDerivings nestedRecord1(1, record1);
static RecordWithNestedDerivings nestedRecord1A(1, record1A);
static RecordWithNestedDerivings nestedRecord2(1, record2);

@interface DBRecordWithDerivingsCppTests : XCTestCase

@end

@implementation DBRecordWithDerivingsCppTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testEqual
{
    XCTAssertTrue(record1 == record1A, @"1 and 1A are expected to be equal");
    XCTAssertTrue(record1 != record2, @"1 and 2 are expected to be unequal");
    XCTAssertTrue(record1 != record3, @"1 and 2 are expected to be unequal");
    XCTAssertTrue(record2 != record3, @"1 and 2 are expected to be unequal");
}

- (void)testOrder
{
    XCTAssertTrue(record1 < record2, @"1 < 2 expected");
    XCTAssertTrue(record1 < record3, @"1 < 3 expected");
    XCTAssertTrue(record2 < record3, @"2 < 3 expected");
}

- (void)testSignsWithEqual
{
    XCTAssertTrue(record1 == record1A, @"1 == 1A. This contradicts to the fact.");
    XCTAssertTrue(record1 <= record1A, @"1 == 1A. This contradicts to the fact.");
    XCTAssertTrue(record1 >= record1A, @"1 == 1A. This contradicts to the fact.");
    XCTAssertFalse(record1 != record1A, @"1 == 1A. This contradicts to the fact.");
    XCTAssertFalse(record1 < record1A, @"1 == 1A. This contradicts to the fact.");
    XCTAssertFalse(record1 > record1A, @"1 == 1A. This contradicts to the fact.");
}

- (void)testSignsWithUnequal
{
    XCTAssertTrue(record1 != record2, @"1 < 2. This contradicts to the fact.");
    XCTAssertTrue(record1 < record2, @"1 < 2. This contradicts to the fact.");
    XCTAssertTrue(record1 <= record2, @"1 < 2. This contradicts to the fact.");
    XCTAssertFalse(record1 == record2, @"1 < 2. This contradicts to the fact.");
    XCTAssertFalse(record1 > record2, @"1 < 2. This contradicts to the fact.");
    XCTAssertFalse(record1 >= record2, @"1 < 2. This contradicts to the fact.");
}

- (void)testNestedEqual
{
    XCTAssertTrue(nestedRecord1 == nestedRecord1A, @"Nested 1 and 1A expected to be equal");
    XCTAssertTrue(nestedRecord1 != nestedRecord2, @"Nested 1 and 2 expected to be unequal");
}

- (void)testNestedOrder
{
    XCTAssertTrue(nestedRecord1 < nestedRecord2, @"1 < 2 expected");

}

@end
