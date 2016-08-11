#import <XCTest/XCTest.h>

#import "DBInterfaceInheritanceConstant.h"
#import "DBInterfaceEncapsulator.h"

#import "DBBaseCppInterfaceInheritance.h"
#import "DBSubCppInterfaceInheritance.h"

#import "DBBaseObjcJavaInterfaceInheritanceImpl.h"
#import "DBSubObjcJavaInterfaceInheritanceImpl.h"

@interface DBInterfaceInheritanceTests : XCTestCase

@end

@implementation DBInterfaceInheritanceTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

#pragma mark - C++ Inheritance Test Cases

- (void)testCppBaseClass
{
    DBBaseCppInterfaceInheritance *base = [DBBaseCppInterfaceInheritance create];
 
    XCTAssertNotNil(base);
    XCTAssertTrue([base isKindOfClass:[DBBaseCppInterfaceInheritance class]]);
 
    XCTAssertTrue([[base baseMethod] isEqualToString:DBInterfaceInheritanceConstantBaseMethodReturnValue]);
    XCTAssertTrue([[base overrideMethod] isEqualToString:DBInterfaceInheritanceConstantBaseOverrideMethodReturnValue]);
}

- (void)testCppSubClassInheritance
{
    DBSubCppInterfaceInheritance *sub = [DBSubCppInterfaceInheritance create];
    XCTAssertNotNil(sub);
    XCTAssertTrue([sub isKindOfClass:[DBSubCppInterfaceInheritance class]]);
    XCTAssertTrue([sub isKindOfClass:[DBBaseCppInterfaceInheritance class]]);
    
    XCTAssertTrue([[sub baseMethod] isEqualToString:DBInterfaceInheritanceConstantBaseMethodReturnValue]);
    XCTAssertTrue([[sub overrideMethod] isEqualToString:DBInterfaceInheritanceConstantSubOverrideMethodReturnValue]);
    XCTAssertTrue([[sub subMethod] isEqualToString:DBInterfaceInheritanceConstantSubMethodReturnValue]);
    
    DBBaseCppInterfaceInheritance *subAsBase = (DBBaseCppInterfaceInheritance *)sub;
    XCTAssertTrue([[subAsBase baseMethod] isEqualToString:DBInterfaceInheritanceConstantBaseMethodReturnValue]);
    XCTAssertTrue([[subAsBase overrideMethod] isEqualToString:DBInterfaceInheritanceConstantSubOverrideMethodReturnValue]);
}

- (void)testCppSubClassEncapsulation
{
    DBInterfaceEncapsulator *encapsulator = [DBInterfaceEncapsulator create];
    
    DBBaseCppInterfaceInheritance *base = [DBBaseCppInterfaceInheritance create];
    [encapsulator setCppObject:base];
    XCTAssertTrue([[encapsulator getCppObject] isKindOfClass:[DBBaseCppInterfaceInheritance class]]);
    
    DBSubCppInterfaceInheritance *sub = [DBSubCppInterfaceInheritance create];
    [encapsulator setCppObject:sub];
    XCTAssertTrue([[encapsulator getCppObject] isKindOfClass:[DBSubCppInterfaceInheritance class]]);
    XCTAssertTrue([[encapsulator subCppAsBaseCpp] isKindOfClass:[DBSubCppInterfaceInheritance class]]);
}

#pragma mark - Objective-C Inheritance Test Cases

- (void)testObjcBaseClass
{
    id<DBBaseObjcJavaInterfaceInheritance> base = [[DBBaseObjcJavaInterfaceInheritanceImpl alloc] init];
    
    XCTAssertNotNil(base);
    XCTAssertTrue([base conformsToProtocol:@protocol(DBBaseObjcJavaInterfaceInheritance)]);
    
    XCTAssertTrue([[base baseMethod] isEqualToString:DBInterfaceInheritanceConstantBaseMethodReturnValue]);
    XCTAssertTrue([[base overrideMethod] isEqualToString:DBInterfaceInheritanceConstantBaseOverrideMethodReturnValue]);
}

- (void)testObjcSubClassInheritance
{
    id<DBSubObjcJavaInterfaceInheritance> sub = [[DBSubObjcJavaInterfaceInheritanceImpl alloc] init];
    XCTAssertNotNil(sub);
    XCTAssertTrue([sub conformsToProtocol:@protocol(DBSubObjcJavaInterfaceInheritance)]);
    XCTAssertTrue([sub conformsToProtocol:@protocol(DBBaseObjcJavaInterfaceInheritance)]);
    
    XCTAssertTrue([[sub baseMethod] isEqualToString:DBInterfaceInheritanceConstantBaseMethodReturnValue]);
    XCTAssertTrue([[sub overrideMethod] isEqualToString:DBInterfaceInheritanceConstantSubOverrideMethodReturnValue]);
    XCTAssertTrue([[sub subMethod] isEqualToString:DBInterfaceInheritanceConstantSubMethodReturnValue]);
    
    id<DBBaseObjcJavaInterfaceInheritance> subAsBase = (id<DBBaseObjcJavaInterfaceInheritance>)sub;
    XCTAssertTrue([[subAsBase baseMethod] isEqualToString:DBInterfaceInheritanceConstantBaseMethodReturnValue]);
    XCTAssertTrue([[subAsBase overrideMethod] isEqualToString:DBInterfaceInheritanceConstantSubOverrideMethodReturnValue]);
}

- (void)testObjcSubClassEncapsulation
{
    DBInterfaceEncapsulator *encapsulator = [DBInterfaceEncapsulator create];
    
    id<DBBaseObjcJavaInterfaceInheritance> base = [[DBBaseObjcJavaInterfaceInheritanceImpl alloc] init];
    [encapsulator setObjcJavaObject:base];
    
    id encappedBase = [encapsulator getObjcJavaObject];
    XCTAssertTrue([encappedBase conformsToProtocol:@protocol(DBBaseObjcJavaInterfaceInheritance)]);
    XCTAssertTrue([encappedBase isKindOfClass:[DBBaseObjcJavaInterfaceInheritanceImpl class]]);
    
    id<DBSubObjcJavaInterfaceInheritance> sub = [[DBSubObjcJavaInterfaceInheritanceImpl alloc] init];
    [encapsulator setObjcJavaObject:sub];

    id encappedSub = [encapsulator getObjcJavaObject];
    XCTAssertTrue([encappedSub conformsToProtocol:@protocol(DBBaseObjcJavaInterfaceInheritance)]);
    XCTAssertTrue([encappedSub conformsToProtocol:@protocol(DBSubObjcJavaInterfaceInheritance)]);
    XCTAssertTrue([encappedSub isKindOfClass:[DBSubObjcJavaInterfaceInheritanceImpl class]]);
    XCTAssertFalse([encappedSub isKindOfClass:[DBBaseObjcJavaInterfaceInheritanceImpl class]]);
}

- (void) testObjcSubClassCasting
{
    DBInterfaceEncapsulator *encapsulator = [DBInterfaceEncapsulator create];
    
    id<DBBaseObjcJavaInterfaceInheritance> base = [[DBBaseObjcJavaInterfaceInheritanceImpl alloc] init];
    id castBase = [encapsulator castBaseArgToSub:base];
    XCTAssertNil(castBase);
    
    // FIXME: This test will fail. When castBaseArgToSub is called, a C++ object will be created to
    //        represent the Objective-C object. Since castBaseArgToSub takes a DBBaseObjJavaInterfaceInheritance
    //        argument, the C++ object that is created will be BaseObjcJavaInterfaceInheritance object,
    //        slicing off all the additional members of the subtype and making it impossible to cast
    //        back to the provided subtype.
    // id<DBSubObjcJavaInterfaceInheritance> sub = [[DBSubObjcJavaInterfaceInheritanceImpl alloc] init];
    // id castSub = [encapsulator castBaseArgToSub:sub];
    // XCTAssertNotNil(castSub);
}

@end
