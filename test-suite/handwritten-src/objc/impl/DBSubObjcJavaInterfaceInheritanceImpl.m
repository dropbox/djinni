#import "DBSubObjcJavaInterfaceInheritanceImpl.h"

#import "DBInterfaceInheritanceConstant.h"
#import "DBBaseObjcJavaInterfaceInheritanceImpl.h"

@interface DBSubObjcJavaInterfaceInheritanceImpl ()

@property (nonnull, readonly, strong) DBBaseObjcJavaInterfaceInheritanceImpl *superImpl;

@end

@implementation DBSubObjcJavaInterfaceInheritanceImpl

@synthesize superImpl = _superImpl;
- (DBBaseObjcJavaInterfaceInheritanceImpl *)superImpl
{
    if (!_superImpl) {
        _superImpl = [[DBBaseObjcJavaInterfaceInheritanceImpl alloc] init];
    }
    return _superImpl;
}

- (nonnull NSString *)baseMethod
{
    return [self.superImpl baseMethod];
}

- (nonnull NSString *)overrideMethod
{
    return DBInterfaceInheritanceConstantSubOverrideMethodReturnValue;
}

- (nonnull NSString *)subMethod
{
    return DBInterfaceInheritanceConstantSubMethodReturnValue;
}

@end
