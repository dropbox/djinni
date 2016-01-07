// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from derivings.djinni

#import <Foundation/Foundation.h>

@interface DBRecordWithDerivings : NSObject
- (nonnull instancetype)initWithKey1:(int32_t)key1
                                key2:(nonnull NSString *)key2;
+ (nonnull instancetype)recordWithDerivingsWithKey1:(int32_t)key1
                                               key2:(nonnull NSString *)key2;

- (nonnull NSDictionary *) toDict;

@property (nonatomic, readonly) int32_t key1;

@property (nonatomic, readonly, nonnull) NSString * key2;

- (NSComparisonResult)compare:(nonnull DBRecordWithDerivings *)other;

@end
