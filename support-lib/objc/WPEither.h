#import <Foundation/Foundation.h>

@interface WPEither : NSObject

@property (nonatomic, readonly) BOOL isLeft;
@property (nonatomic, readonly) BOOL isRight;
@property (nonatomic, readonly, nullable) id left;
@property (nonatomic, readonly, nullable) id right;

- (nonnull instancetype)initWithLeft:(nonnull id)left;
- (nonnull instancetype)initWithRight:(nonnull id)right;

@end
