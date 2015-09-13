#import "DBPrimitiveList+Private.h"

#import <Foundation/Foundation.h>
#import <XCTest/XCTest.h>

using namespace testsuite;

static PrimitiveList cppPrimitiveList { { 1, 2, 3 } };
static DBPrimitiveList *objcPrimitiveList = [DBPrimitiveList primitiveListWithList:@[@1, @2, @3]];

@interface DBPrimitiveListTests : XCTestCase

@end

@implementation DBPrimitiveListTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testObjcToCppConverter
{
    PrimitiveList convert = djinni_generated::PrimitiveList::toCpp(objcPrimitiveList);
    XCTAssertEqual(convert.list, cppPrimitiveList.list, @"C++ converted list should be the same.");
}

- (void)testCppToObjcConverter
{
    DBPrimitiveList *convert = djinni_generated::PrimitiveList::fromCpp(cppPrimitiveList);
    XCTAssertEqualObjects(convert.list, objcPrimitiveList.list, @"Objective-C converted list should be the same.");
}

@end
