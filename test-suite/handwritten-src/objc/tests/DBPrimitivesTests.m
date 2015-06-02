
#import <Foundation/Foundation.h>
#import <XCTest/XCTest.h>

#import "DBAssortedPrimitives.h"
#import "DBTestHelpers.h"

@interface DBPrimitivesTests : XCTestCase

@end

@implementation DBPrimitivesTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testPrimitives
{
    DBAssortedPrimitives * p = [[DBAssortedPrimitives alloc]
                                    initWithB:YES
                                        eight:(int8_t)123
                                      sixteen:(int16_t)20000
                                    thirtytwo:(int32_t)1000000000
                                    sixtyfour:(int64_t)1234567890123456789L
                                   fsixtyfour:1.23L
                                           oB:[NSNumber numberWithBool:YES]
                                       oEight:[NSNumber numberWithChar:123]
                                     oSixteen:[NSNumber numberWithShort:20000]
                                   oThirtytwo:[NSNumber numberWithInt:1000000000]
                                   oSixtyfour:[NSNumber numberWithLongLong:1234567890123456789L]
                                  oFsixtyfour:[NSNumber numberWithDouble:123L]];
    XCTAssertEqualObjects(p, [DBTestHelpers assortedPrimitivesId:p]);
}

- (void)testObjcToCppConverter
{
}

- (void)testCppToObjcConverter
{
}

@end
