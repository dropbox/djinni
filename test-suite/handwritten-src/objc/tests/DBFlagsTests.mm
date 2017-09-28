//
//  DBFlagsTests.m
//  DjinniObjcTest
//
//  Created by knejp on 2.8.15.
//  Copyright (c) 2015 Dropbox, Inc. All rights reserved.
//

#import "DBAccessFlags.h"
#import "DBEmptyFlags.h"
#import "DBFlagRoundtrip.h"
#import "DJIMarshal+Private.h"

#include "access_flags.hpp"
#include "empty_flags.hpp"

#import <XCTest/XCTest.h>

using testsuite::access_flags;

@interface DBFlagsTests : XCTestCase

@end

@implementation DBFlagsTests

- (void)testOperators {
  using T = ::djinni::Enum<access_flags, DBAccessFlags>;
  XCTAssertEqual(DBAccessFlagsOwnerRead | DBAccessFlagsOwnerWrite,
                 T::fromCpp(access_flags::OWNER_READ | access_flags::OWNER_WRITE),
                 "flag mismatch");
  XCTAssertEqual(DBAccessFlagsOwnerRead & DBAccessFlagsEverybody,
                 T::fromCpp(access_flags::OWNER_READ & access_flags::EVERYBODY),
                 "flag mismatch");
  XCTAssertEqual(DBAccessFlagsOwnerRead ^ DBAccessFlagsNobody,
                 T::fromCpp(access_flags::OWNER_READ ^ access_flags::NOBODY),
                 "flag mismatch");

  XCTAssertEqual(access_flags::GROUP_READ | access_flags::GROUP_EXECUTE,
                 T::toCpp(DBAccessFlagsGroupRead | DBAccessFlagsGroupExecute),
                 "flag mismatch");
  XCTAssertEqual(access_flags::GROUP_READ & access_flags::EVERYBODY,
                 T::toCpp(DBAccessFlagsGroupRead & DBAccessFlagsEverybody),
                 "flag mismatch");
  XCTAssertEqual(access_flags::GROUP_READ ^ access_flags::NOBODY,
                 T::toCpp(DBAccessFlagsGroupRead ^ DBAccessFlagsNobody),
                 "flag mismatch");

  XCTAssertEqual(access_flags::SYSTEM_READ,
                 access_flags::SYSTEM_READ | access_flags::NOBODY,
                 "neutral flag wrong");

  XCTAssertEqual(access_flags::NOBODY,
                 access_flags::SYSTEM_WRITE & ~access_flags::SYSTEM_WRITE,
                 "bitwise not wrong");
}

- (void)testRoundtripAccess {
  DBAccessFlags flags[] = {
    DBAccessFlagsNobody,
    DBAccessFlagsEverybody,
    DBAccessFlagsOwnerRead,
    DBAccessFlagsOwnerRead | DBAccessFlagsOwnerWrite,
    DBAccessFlagsOwnerRead | DBAccessFlagsOwnerWrite | DBAccessFlagsOwnerExecute,
  };

  for(auto flag : flags) {
    XCTAssertEqual(flag, [DBFlagRoundtrip roundtripAccess:flag], "roundtrip failed");
    XCTAssertEqual(flag, [[DBFlagRoundtrip roundtripAccessBoxed:@(flag)] unsignedIntegerValue], "roundtrip failed");
  }
  XCTAssertEqual(nil, [DBFlagRoundtrip roundtripAccessBoxed:nil], "roundtrip failed");
}

- (void)testRoundtripEmpty {
  DBEmptyFlags flags[] = {
    DBEmptyFlagsNone,
    DBEmptyFlagsAll,
  };

  for(auto flag : flags) {
    XCTAssertEqual(flag, [DBFlagRoundtrip roundtripEmpty:flag], "roundtrip failed");
    XCTAssertEqual(flag, [[DBFlagRoundtrip roundtripEmptyBoxed:@(flag)] unsignedIntegerValue], "roundtrip failed");
  }
  XCTAssertEqual(nil, [DBFlagRoundtrip roundtripEmptyBoxed:nil], "roundtrip failed");
}

@end
