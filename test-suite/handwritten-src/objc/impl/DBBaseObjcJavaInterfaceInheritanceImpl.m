#import "DBBaseObjcJavaInterfaceInheritanceImpl.h"

#import "DBInterfaceInheritanceConstant.h"

@implementation DBBaseObjcJavaInterfaceInheritanceImpl

- (nonnull NSString *)baseMethod
{
    return DBInterfaceInheritanceConstantBaseMethodReturnValue;
}

- (nonnull NSString *)overrideMethod
{
    return DBInterfaceInheritanceConstantBaseOverrideMethodReturnValue;
}

@end
