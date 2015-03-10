#import <Foundation/Foundation.h>

@interface WPEither : NSObject

@property (nonatomic, readonly) id left;
@property (nonatomic, readonly) id right;

- (instancetype)initWithLeft:(id)left;
- (instancetype)initWithRight:(id)right;

@end
