#import <Foundation/Foundation.h>
#import "DBTestHelpers.h"
#import "DBTestDuration.h"
#import <XCTest/XCTest.h>

@interface DBDurationTests : XCTestCase

@end

@implementation DBDurationTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)test
{
    XCTAssertEqual([[DBTestDuration hoursString:3600] compare:@"1"], NSOrderedSame);
    XCTAssertEqual([[DBTestDuration minutesString:60] compare:@"1"], NSOrderedSame);
    XCTAssertEqual([[DBTestDuration secondsString:1] compare:@"1"], NSOrderedSame);
    XCTAssertEqual([[DBTestDuration millisString:0.001] compare:@"1"], NSOrderedSame);
    XCTAssertEqual([[DBTestDuration microsString:0.000001] compare:@"1"], NSOrderedSame);
    XCTAssertEqual([[DBTestDuration nanosString:0.000000001] compare:@"1"], NSOrderedSame);

    XCTAssertEqualWithAccuracy([DBTestDuration hours:1], 3600, 0.001);
    XCTAssertEqualWithAccuracy([DBTestDuration minutes:1], 60, 0.001);
    XCTAssertEqualWithAccuracy([DBTestDuration seconds:1], 1, 0.001);
    XCTAssertEqualWithAccuracy([DBTestDuration millis:1], 0.001, 0.000001);
    XCTAssertEqualWithAccuracy([DBTestDuration micros:1], 0.000001, 0.000000001);
    XCTAssertEqualWithAccuracy([DBTestDuration nanos:1], 0.000000001, 0.000000000001);

    XCTAssertEqualWithAccuracy([DBTestDuration hoursf:1.5], 5400, 0.001);
    XCTAssertEqualWithAccuracy([DBTestDuration minutesf:1.5], 90, 0.001);
    XCTAssertEqualWithAccuracy([DBTestDuration secondsf:1.5], 1.5, 0.001);
    XCTAssertEqualWithAccuracy([DBTestDuration millisf:1.5], 0.0015, 0.000001);
    XCTAssertEqualWithAccuracy([DBTestDuration microsf:1.5], 0.0000015, 0.000000001);
    XCTAssertEqualWithAccuracy([DBTestDuration nanosf:1.0], 0.000000001, 0.000000000001);

    XCTAssertEqual([[DBTestDuration box:1.0] intValue],1);
    XCTAssertEqual([DBTestDuration box:-1.0], nil);

    XCTAssertEqual([DBTestDuration unbox:[NSNumber numberWithDouble:1.0]], 1);
    XCTAssertEqual([DBTestDuration unbox:nil], -1);
}

@end
