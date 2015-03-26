#import "DBPrimitiveList+Private.h"

#import <Foundation/Foundation.h>
#import <XCTest/XCTest.h>

static djinni_generated::PrimitiveList cppPrimitiveList { { 1, 2, 3 } };
static DBPrimitiveList *objcPrimitiveList = [[DBPrimitiveList alloc] initWithList:
                                             [[NSMutableArray alloc] initWithObjects:[NSNumber numberWithLongLong:1],
                                                                                     [NSNumber numberWithLongLong:2],
                                                                                     [NSNumber numberWithLongLong:3],
                                                                                     nil]];

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

- (void)testObjcCopyConstructor
{
    DBPrimitiveList *copy = [[DBPrimitiveList alloc] initWithPrimitiveList:objcPrimitiveList];
    XCTAssertNotEqual(copy.list, objcPrimitiveList.list, @"The two NSMutableArray should not be the same object.");
    XCTAssertEqualObjects(copy.list, objcPrimitiveList.list, @"The two NSMutableArray should have the same content.");
}

- (void)testObjcToCppConverter
{
    djinni_generated::PrimitiveList convert = djinni_generated::objc::PrimitiveList::toCpp(objcPrimitiveList);
    XCTAssertEqual(convert.list, cppPrimitiveList.list, @"C++ converted list should be the same.");
}

- (void)testCppToObjcConverter
{
    DBPrimitiveList *convert = djinni_generated::objc::PrimitiveList::fromCpp(cppPrimitiveList);
    XCTAssertEqualObjects(convert.list, objcPrimitiveList.list, @"Objective-C converted list should be the same.");
}

@end
