#import "WPEither.h"

@implementation WPEither

- (instancetype)initWithLeft:(id)left {
    self = [super init];
    if (self) {
      _left = left;
    }
    return self;
}

- (instancetype)initWithRight:(id)right {
    self = [super init];
    if (self) {
        _right = right;
    }
    return self;
}

@end
