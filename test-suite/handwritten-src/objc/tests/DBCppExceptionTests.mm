#import "DBCppException+Private.h"
#import <XCTest/XCTest.h>

#include "cpp_exception_impl.hpp"

@interface DBCppExceptionTests : XCTestCase

@end

@implementation DBCppExceptionTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testThrown {
    DBCppException* objcInterface = [DBCppException cppExceptionWithCpp:(std::make_shared<CppExceptionImpl>())];

    NSString *exceptionName = nil;
    @try {
        [objcInterface throwAnException];
    } @catch (NSException *e) {
        exceptionName = e.name;
    }
    XCTAssertEqualObjects(exceptionName, @"Exception Thrown", @"Expected same exception name, actual: %@", exceptionName);
}

- (void)testInterfaceWrapperCache {
    std::shared_ptr<CppException> cppInterface = std::make_shared<CppExceptionImpl>();
    std::weak_ptr<CppException> weakCppInterface(cppInterface);
    @autoreleasepool {
        DBCppException* objcInterface1 = [DBCppException cppExceptionWithCpp:cppInterface];
        DBCppException* objcInterface2 = [DBCppException cppExceptionWithCpp:cppInterface];
        XCTAssertEqual(objcInterface1, objcInterface2, @"The same wrapper should be returned");

        cppInterface.reset();
        XCTAssertFalse(weakCppInterface.expired(), @"The C++ interface should still have reference");
    }
    XCTAssertTrue(weakCppInterface.expired(), @"The C++ interface should have been deallocated");
}

@end
