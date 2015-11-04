#import "DBNestedCollection+Private.h"
#import <XCTest/XCTest.h>

#include "nested_collection.hpp"

using namespace testsuite;

static NestedCollection cppNestedCollection { { {u8"String1", u8"String2"}, {u8"StringA", u8"StringB"} } };
static DBNestedCollection *objcNestedCollection = [DBNestedCollection nestedCollectionWithSetList:@[
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
    DBNestedCollection *converted = djinni_generated::NestedCollection::fromCpp(cppNestedCollection);
    XCTAssertEqualObjects(objcNestedCollection.setList, converted.setList, @"List expected to be equivalent");
}

- (void)testObjcNestedCollectionToCpp
{
    NestedCollection converted = djinni_generated::NestedCollection::toCpp(objcNestedCollection);
    XCTAssertEqual(cppNestedCollection.set_list, converted.set_list, @"List expected to be equivalent");
}

@end
