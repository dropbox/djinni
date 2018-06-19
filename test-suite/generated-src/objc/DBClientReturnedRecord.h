// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#import <Foundation/Foundation.h>

// Record returned by a client
@interface DBClientReturnedRecord : NSObject
- (nonnull instancetype)initWithRecordId:(int64_t)recordId
                                 content:(nonnull NSString *)content
                                    misc:(nullable NSString *)misc;
+ (nonnull instancetype)clientReturnedRecordWithRecordId:(int64_t)recordId
                                                 content:(nonnull NSString *)content
                                                    misc:(nullable NSString *)misc;

@property (nonatomic, readonly) int64_t recordId;

@property (nonatomic, readonly, nonnull) NSString * content;

@property (nonatomic, readonly, nullable) NSString * misc;

@end
