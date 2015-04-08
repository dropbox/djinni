#import "DBNestedCollection+Private.h"
#import <XCTest/XCTest.h>

#include "nested_collection.hpp"

static NestedCollection cppNestedCollection { { {u8"String1", u8"String2"}, {u8"StringA", u8"StringB"} } };
static DBNestedCollection *objcNestedCollection = [[DBNestedCollection alloc] initWithSetList:@[
            [NSSet setWithArray:@[ @"String1", @"String2" ]],
            [NSSet setWithArray:@[ @"StringA", @"StringB" ]],
        ]];

@interface DBNestedCollectionTests : XCTestCase

@end

@implementation DBNestedCollectionTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testCppNestedCollectionToObjc
{
    DBNestedCollection *converted = [[DBNestedCollection alloc] initWithCppNestedCollection:cppNestedCollection];
    XCTAssertEqualObjects(objcNestedCollection.setList, converted.setList, @"List expected to be equivalent");
}

- (void)testObjcNestedCollectionToCpp
{
    NestedCollection converted = [objcNestedCollection cppNestedCollection];
    XCTAssertEqual(cppNestedCollection.set_list, converted.set_list, @"List expected to be equivalent");
}

@end
