#import "DBConstants.h"

#import <XCTest/XCTest.h>

@interface DBConstantTests : XCTestCase

@end

@implementation DBConstantTests

- (void)methodThatTakesString:(__unused NSString *)string
{
}

- (void)testCallingMethodThatTakesStringWithConstant
{
    [self methodThatTakesString:DBConstantsStringConstant];
}

@end
