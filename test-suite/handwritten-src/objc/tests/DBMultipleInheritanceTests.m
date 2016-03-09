#import <XCTest/XCTest.h>

#include "DBFirstListener.h"
#include "DBSecondListener.h"
#include "DBListenerCaller.h"
#include "DBReturnOne.h"
#include "DBReturnTwo.h"

@interface Listener : NSObject <DBFirstListener, DBSecondListener>
@property bool firstCalled;
@property bool secondCalled;
@end

@implementation Listener

- (id) init {
    self = [super init];
    if (self != nil) {
        self.firstCalled = FALSE;
        self.secondCalled = FALSE;
    }
    return self;
}

- (void)first {
    self.firstCalled = TRUE;
}

- (void)second {
    self.secondCalled = TRUE;
}

@end

// test instance of ObjC class implementing two +o interfaces, passed to C++
@interface DBObjcMultipleInheritanceTest : XCTestCase
@end

@implementation DBObjcMultipleInheritanceTest {
    Listener *listener;
    DBListenerCaller *caller;
}

- (void)setUp {
    [super setUp];
    listener = [Listener new];
    caller = [DBListenerCaller init:listener secondL:listener];
}

- (void)testCallFirst {
    [caller callFirst];
    XCTAssert(listener.firstCalled);
}

- (void)testCallSecond {
    [caller callSecond];
    XCTAssert(listener.secondCalled);
}

@end

// test instance of C++ class implementing two +c interfaces, used in ObjC
@interface DBCppMultipleInheritanceTest : XCTestCase
@end

@implementation DBCppMultipleInheritanceTest

- (void)testReturnOne {
    DBReturnOne *returnOne = [DBReturnOne getInstance];
    XCTAssertEqual([returnOne returnOne], 1);
}

- (void)testReturnTwo {
    DBReturnTwo *returnTwo = [DBReturnTwo getInstance];
    XCTAssertEqual([returnTwo returnTwo], 2);
}

@end