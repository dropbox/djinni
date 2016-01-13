#import <XCTest/XCTest.h>

#include "DBFirstListener.h"
#include "DBSecondListener.h"
#include "DBListenerCaller.h"

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


@interface DBListenerCallerTest : XCTestCase
@end

@implementation DBListenerCallerTest {
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
