// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#import <Foundation/Foundation.h>
@class DBReverseClientInterface;


@interface DBReverseClientInterface : NSObject

- (nonnull NSString *)returnStr;

// Testing code comments before documentation comments
// with multiple lines
// and another line
/**
 * Testing documentation comments after code comments
 * with multiple lines
 * and another line
 */
- (nonnull NSString *)methTakingInterface:(nullable DBReverseClientInterface *)i;

/**
 * Testing documentation comments before code comments
 * with multiple lines
 * and another line
 */
// Testing code comments after documentation comments
// with multiple lines
// and another line
- (nonnull NSString *)methTakingOptionalInterface:(nullable DBReverseClientInterface *)i;

+ (nullable DBReverseClientInterface *)create;

@end
