
#import <Foundation/Foundation.h>
#import <XCTest/XCTest.h>

#import "DBAssortedPrimitives.h"
#import "DBTestHelpers.h"
#import "DBEmptyRecord.h"

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
    DBAssortedPrimitives * p = [DBAssortedPrimitives assortedPrimitivesWithB:YES
                                                                       eight:(int8_t)123
                                                                     sixteen:(int16_t)20000
                                                                   thirtytwo:(int32_t)1000000000
                                                                   sixtyfour:(int64_t)1234567890123456789L
                                                                  fthirtytwo:(float)1.23
                                                                  fsixtyfour:1.23
                                                                          oB:[NSNumber numberWithBool:YES]
                                                                      oEight:[NSNumber numberWithChar:123]
                                                                    oSixteen:[NSNumber numberWithShort:20000]
                                                                  oThirtytwo:[NSNumber numberWithInt:1000000000]
                                                                  oSixtyfour:[NSNumber numberWithLongLong:1234567890123456789L]
                                                                 oFthirtytwo:[NSNumber numberWithFloat:(float)123]
                                                                 oFsixtyfour:[NSNumber numberWithDouble:123]];
    XCTAssertEqualObjects(p, [DBTestHelpers assortedPrimitivesId:p]);
}

- (void)testEmptyRecord
{
    (void)[DBEmptyRecord emptyRecord];
}

- (void)testObjcToCppConverter
{
}

- (void)testCppToObjcConverter
{
}

@end
