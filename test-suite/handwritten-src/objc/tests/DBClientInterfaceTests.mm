#import "DBClientInterfaceImpl.h"
#import "DBClientInterfaceObjcProxy+Private.h"
#import "DBTestHelpersCppProxy.h"
#import <XCTest/XCTest.h>

@interface DBClientInterfaceTests : XCTestCase

@end

@implementation DBClientInterfaceTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testCppString
{
    [DBTestHelpersCppProxy checkClientInterfaceAscii:[[DBClientInterfaceImpl alloc] init]];
}

- (void)testCppUTF8String
{
    [DBTestHelpersCppProxy checkClientInterfaceNonascii:[[DBClientInterfaceImpl alloc] init]];
}

- (void)testObjcInterfaceWrapper
{
    __weak id <DBClientInterface> objcClientInterfaceWeak;
    @autoreleasepool {
        std::shared_ptr<ClientInterface> cppClientInterface1, cppClientInterface2;
        @autoreleasepool {
            id <DBClientInterface> objcClientInterface = [[DBClientInterfaceImpl alloc] init];
            objcClientInterfaceWeak = objcClientInterface;
            cppClientInterface1 = ::djinni_generated::ClientInterfaceObjcProxy::client_interface_with_objc(objcClientInterface);
            cppClientInterface2 = ::djinni_generated::ClientInterfaceObjcProxy::client_interface_with_objc(objcClientInterface);
            XCTAssertEqual(cppClientInterface1, cppClientInterface2);
        }
        XCTAssertNotNil(objcClientInterfaceWeak);
        cppClientInterface1.reset();
        cppClientInterface2.reset();
    }
    XCTAssertNil(objcClientInterfaceWeak);
}

@end
