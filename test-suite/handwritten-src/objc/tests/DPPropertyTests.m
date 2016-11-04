//
//  DPPropertyTests.m
//  DjinniObjcTest
//
//  Created by Samuel Hall on 03/11/2016.
//  Copyright © 2016 Dropbox, Inc. All rights reserved.
//

#import <XCTest/XCTest.h>

#include "DBPropertiesTestHelper.h"

@interface DPPropertyTests : XCTestCase

@end

@implementation DPPropertyTests

- (void)setUp {
    [super setUp];
}

- (void)tearDown {
    [super tearDown];
}

- (void)testProperties {
    
    DBPropertiesTestHelper *testHelper = [DBPropertiesTestHelper createNew];

    testHelper.item = 1;
    
    XCTAssert(testHelper.item == 1);
    
    testHelper.testString = @"fooBar";
    
    XCTAssert([testHelper.testString isEqualToString:@"fooBar"]);
}

@end
