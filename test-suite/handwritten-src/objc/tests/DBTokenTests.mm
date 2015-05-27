#import "DBTestHelpers.h"
#import "DBToken.h"
#import <XCTest/XCTest.h>

@interface DBTokenTests : XCTestCase

@end

@interface DBObjcToken : NSObject<DBToken>
- (NSString *)whoami;
@end
@implementation DBObjcToken
- (NSString *)whoami {
    return @"ObjC";
}
@end

@implementation DBTokenTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testTokens
{
    id<DBToken> t = [[DBObjcToken alloc] init];
    XCTAssertEqual([DBTestHelpers tokenId:t], t);
}

- (void)testNullToken
{
    XCTAssertNil([DBTestHelpers tokenId:nil]);
}

- (void)testTokenType
{
    [DBTestHelpers checkTokenType:[[DBObjcToken alloc] init] type:@"ObjC"];
    [DBTestHelpers checkTokenType:[DBTestHelpers createCppToken] type:@"C++"];
    XCTAssertThrows([DBTestHelpers checkTokenType:[[DBObjcToken alloc] init] type:@"foo"]);
    XCTAssertThrows([DBTestHelpers checkTokenType:[DBTestHelpers createCppToken] type:@"foo"]);
}

- (void)testCppToken
{
    id<DBToken> ct = [DBTestHelpers createCppToken];
    XCTAssertEqual([DBTestHelpers tokenId:ct], ct);
    [DBTestHelpers checkCppToken:ct];
    ct = nil;
}

- (void)testNotCppToken {
    XCTAssertThrows([DBTestHelpers checkCppToken:[[DBObjcToken alloc] init]]);
}

@end
