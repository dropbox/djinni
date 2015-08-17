#import "WPEither.h"

@implementation WPEither

@dynamic isLeft;
@dynamic isRight;

- (nonnull instancetype)initWithLeft:(nonnull id)left {
    NSParameterAssert(left != nil);
    self = [super init];
    if (self) {
      _left = left;
    }
    return self;
}

- (nonnull instancetype)initWithRight:(nonnull id)right {
    NSParameterAssert(right != nil);
    self = [super init];
    if (self) {
        _right = right;
    }
    return self;
}

- (BOOL)isLeft {
    return _left != nil;
}

- (BOOL)isRight {
    return _right != nil;
}

- (nullable instancetype)init __attribute((noreturn)) {
    [NSException raise:NSInternalInconsistencyException
                format:@"Invalid initializer"];
    return nil;
}

@end
