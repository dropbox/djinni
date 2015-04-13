// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from derivings.djinni

#import "DBRecordWithNestedDerivings.h"
#include <utility>
#include <vector>


@implementation DBRecordWithNestedDerivings

- (id)initWithRecordWithNestedDerivings:(DBRecordWithNestedDerivings *)recordWithNestedDerivings
{
    if (self = [super init]) {
        _key = recordWithNestedDerivings.key;
        _rec = [[DBRecordWithDerivings alloc] initWithRecordWithDerivings:recordWithNestedDerivings.rec];
    }
    return self;
}

- (id)initWithKey:(int32_t)key
              rec:(DBRecordWithDerivings *)rec
{
    if (self = [super init]) {
        _key = key;
        _rec = rec;
    }
    return self;
}

- (BOOL)isEqual:(id)other
{
    if (![other isKindOfClass:[DBRecordWithNestedDerivings class]]) {
        return NO;
    }
    DBRecordWithNestedDerivings *typedOther = (DBRecordWithNestedDerivings *)other;
    return self.key == typedOther.key &&
            [self.rec isEqual:typedOther.rec];
}
- (NSComparisonResult)compare:(DBRecordWithNestedDerivings *)other
{
    NSComparisonResult tempResult;
    if (self.key < other.key) {
        tempResult = NSOrderedAscending;
    } else if (self.key > other.key) {
        tempResult = NSOrderedDescending;
    } else {
        tempResult = NSOrderedSame;
    }
    if (tempResult != NSOrderedSame) {
        return tempResult;
    }
    tempResult = [self.rec compare:other.rec];
    if (tempResult != NSOrderedSame) {
        return tempResult;
    }
    return NSOrderedSame;
}

@end
