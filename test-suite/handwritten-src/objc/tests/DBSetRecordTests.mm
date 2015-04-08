#import "DBSetRecord+Private.h"
#import <XCTest/XCTest.h>

#include "set_record.hpp"

@interface DBSetRecordTests : XCTestCase

@end

@implementation DBSetRecordTests

- (SetRecord)getCppSetRecord
{
    return SetRecord({"StringA", "StringB", "StringC"});
}

- (DBSetRecord *)getObjcSetRecord
{
    NSSet *set = [[NSSet alloc] initWithObjects:@"StringA", @"StringB", @"StringC", nil];
    DBSetRecord *objcSetRecord = [[DBSetRecord alloc] initWithSet:set];
    return objcSetRecord;
}

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testCppToObjc
{
    SetRecord cppSetRecord = [self getCppSetRecord];
    DBSetRecord *objcSetRecord = [[DBSetRecord alloc] initWithCppSetRecord:cppSetRecord];

    XCTAssertEqual([objcSetRecord.set count], (NSUInteger)3, @"Set length 3 expected, actual: %lu", (unsigned long)[objcSetRecord.set count]);
    XCTAssert([objcSetRecord.set containsObject:@"StringA"], @"\"StringA\" expected but does not exist");
    XCTAssert([objcSetRecord.set containsObject:@"StringB"], @"\"StringB\" expected but does not exist");
    XCTAssert([objcSetRecord.set containsObject:@"StringC"], @"\"StringC\" expected but does not exist");
}

-(void)testObjcToCpp
{
    DBSetRecord *objcSetRecord = [self getObjcSetRecord];
    SetRecord cppSetRecord = [objcSetRecord cppSetRecord];
    auto & cppSet = cppSetRecord.set;

    XCTAssertEqual(cppSet.size(), (NSUInteger)3, @"Set length 3 expected, actual: %zd", cppSet.size());
    XCTAssertNotEqual(cppSet.find("StringA"), cppSet.end(), @"\"StringA\" expected but does not exist");
    XCTAssertNotEqual(cppSet.find("StringB"), cppSet.end(), @"\"StringB\" expected but does not exist");
    XCTAssertNotEqual(cppSet.find("StringC"), cppSet.end(), @"\"StringC\" expected but does not exist");
}

@end
