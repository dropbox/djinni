#import <Foundation/Foundation.h>
#import "DBWcharTestHelpers.h"
#import "DBWcharTestRec.h"
#import <XCTest/XCTest.h>

@interface DBWcharTests : XCTestCase

@end

@implementation DBWcharTests

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
    NSString *str1 = @"some string with unicode \0, \u263A, \U0001F4A9 symbols";
    NSString *str2 = @"another string with unicode \u263B, \U0001F4A8 symbols";

    XCTAssertEqualObjects([[DBWcharTestHelpers getRecord] s], str1);
    XCTAssertEqualObjects([DBWcharTestHelpers getString], str2);
    XCTAssertTrue([DBWcharTestHelpers checkString:str2]);
    XCTAssertTrue([DBWcharTestHelpers checkRecord:[[DBWcharTestRec alloc] initWithS:str1]]);
}

@end
