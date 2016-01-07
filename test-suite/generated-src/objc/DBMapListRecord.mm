// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from map.djinni

#import "DBMapListRecord.h"


@implementation DBMapListRecord

- (nonnull instancetype)initWithMapList:(nonnull NSArray<NSDictionary<NSString *, NSNumber *> *> *)mapList
{
    if (self = [super init]) {
        _mapList = [mapList copy];
    }
    return self;
}

+ (nonnull instancetype)mapListRecordWithMapList:(nonnull NSArray<NSDictionary<NSString *, NSNumber *> *> *)mapList
{
    return [[self alloc] initWithMapList:mapList];
}

- (NSString *)description
{
    return [NSString stringWithFormat:@"<%@ %p: dict, %@>", self.class, self, [[self toDict] description]];
}

- (NSDictionary *)toDict
{
    #define _djinni_hide_null_(_o_) ((_o_)?(_o_):([NSNull null]))
    
    return @{@"__class_name__": [self.class description], @"mapList": _djinni_hide_null_(self.mapList)};
}

@end
