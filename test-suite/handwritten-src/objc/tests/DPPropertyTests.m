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

    // Test integer property
    testHelper.item = 1;
    
    XCTAssert(testHelper.item == 1);
    
    // Test string property
    testHelper.testString = @"fooBar";
    
    XCTAssert([testHelper.testString isEqualToString:@"fooBar"]);
    
    //Test list property
    NSArray<NSNumber *> *list = [NSArray arrayWithObjects:@1, @2, @3, nil];
    
    testHelper.testList = list;
    
    for (unsigned int i = 0; i < list.count;i++) {
        XCTAssert([list objectAtIndex:i] == [testHelper.testList objectAtIndex:i]);
    }
}

@end
