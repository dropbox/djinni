// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

#import "DBConstantEnum.h"
#import "DBConstantRecord.h"
#import <Foundation/Foundation.h>

/** Record containing constants */
@interface DBConstants : NSObject
- (nonnull instancetype)init;
+ (nonnull instancetype)constants;

+ (DBConstantEnum)constEnum;
+ (NSNumber * __nullable)optBoolConstant;
+ (NSNumber * __nullable)optI8Constant;
/** opt_i16_constant has documentation. */
+ (NSNumber * __nullable)optI16Constant;
+ (NSNumber * __nullable)optI32Constant;
+ (NSNumber * __nullable)optI64Constant;
/**
 * opt_f32_constant has long documentation.
 * (Second line of multi-line documentation.
 *   Indented third line of multi-line documentation.)
 */
+ (NSNumber * __nullable)optF32Constant;
+ (NSNumber * __nullable)optF64Constant;
+ (DBConstantRecord * __nonnull)objectConstant;
@end

/** bool_constant has documentation. */
extern BOOL const DBConstantsBoolConstant;
// i8_constant has a comment
extern int8_t const DBConstantsI8Constant;
extern int16_t const DBConstantsI16Constant;
extern int32_t const DBConstantsI32Constant;
extern int64_t const DBConstantsI64Constant;
// f64_constant has a long comment.
// (Second line of multi-line comment.
//   Indented third line of multi-line comment.)
extern float const DBConstantsF32Constant;
/**
 * f64_constant has long documentation.
 * (Second line of multi-line documentation.
 *   Indented third line of multi-line documentation.)
 */
extern double const DBConstantsF64Constant;
extern NSString * __nonnull const DBConstantsStringConstant;
extern NSString * __nullable const DBConstantsOptStringConstant;
// No support for null optional constants
// No support for optional constant records
// No support for constant binary, list, set, map
extern BOOL const DBConstantsDummy;
