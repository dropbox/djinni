// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#import <Foundation/Foundation.h>


@interface DBClientReturnedRecord : NSObject
- (id)initWithClientReturnedRecord:(DBClientReturnedRecord *)clientReturnedRecord;
- (id)initWithRecordId:(int64_t)recordId
               content:(NSString *)content;

@property (nonatomic, readonly) int64_t recordId;

@property (nonatomic, readonly) NSString * content;

@end
