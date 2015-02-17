// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

#import "DBConstants+Private.h"
#import "DBConstants+Private.h"
#import <Foundation/Foundation.h>
#include <utility>

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-variable"

const BOOL DBConstantsBoolConstant = YES;

const int8_t DBConstantsI8Constant = 1;

const int16_t DBConstantsI16Constant = 2;

const int32_t DBConstantsI32Constant = 3;

const int64_t DBConstantsI64Constant = 4;

const double DBConstantsF64Constant = 5.0;

NSString * const DBConstantsStringConstant = @"string-constant";

NSNumber * const DBConstantsOptionalIntegerConstant = @1;

DBConstants * const DBConstantsObjectConstant = [[DBConstants alloc] initWithSomeInteger:DBConstantsI32Constant
        someString:DBConstantsStringConstant];

#pragma clang diagnostic pop
static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@implementation DBConstants

- (id)initWithConstants:(DBConstants *)constants
{
    if (self = [super init]) {
        _someInteger = constants.someInteger;
        _someString = [constants.someString copy];
    }
    return self;
}

- (id)initWithSomeInteger:(int32_t)someInteger someString:(NSString *)someString
{
    if (self = [super init]) {
        _someInteger = someInteger;
        _someString = [someString copy];
    }
    return self;
}

- (id)initWithCppConstants:(const Constants &)constants
{
    if (self = [super init]) {
        _someInteger = constants.some_integer;
        _someString = [[NSString alloc] initWithBytes:constants.some_string.data()
                length:constants.some_string.length()
                encoding:NSUTF8StringEncoding];
    }
    return self;
}

- (Constants)cppConstants
{
    int32_t someInteger = _someInteger;
    std::string someString([_someString UTF8String], [_someString lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
    return Constants(
            std::move(someInteger),
            std::move(someString));
}

@end
