#import "DBMapRecord+Private.h"
#import "DBMapRecord.h"
#import "DBMapListRecord+Private.h"
#import "DBMapListRecord.h"
#import <XCTest/XCTest.h>

#include "map_record.hpp"

using namespace testsuite;

@interface DBMapRecordTests : XCTestCase

@end

@implementation DBMapRecordTests

- (void)setUp
{
    [super setUp];
}

- (void)tearDown
{
    [super tearDown];
}

- (void)testCppToObjc
{
    MapRecord cppMapRecord([self getCppMap], {});
    DBMapRecord *objcMapRecord = djinni_generated::MapRecord::fromCpp(cppMapRecord);
    [self checkObjcMap:objcMapRecord.map];
}

- (void)testObjcToCpp
{
    DBMapRecord *objcMapRecord = [DBMapRecord mapRecordWithMap:[self getObjcMap] imap:[NSDictionary dictionary]];
    MapRecord cppMapRecord = djinni_generated::MapRecord::toCpp(objcMapRecord);
    [self checkCppMap:cppMapRecord.map];
}

- (void)testCppToObjcEmpty
{
    MapRecord cppMapRecord{ {}, {} };
    DBMapRecord *objcMapRecord = djinni_generated::MapRecord::fromCpp(cppMapRecord);

    XCTAssertEqual([objcMapRecord.map count], (NSUInteger)0, @"Count 0 expected, actual: %lu", (unsigned long)[objcMapRecord.map count]);
}

- (void)testObjcToCppEmpty
{
    DBMapRecord *objcMapRecord = [DBMapRecord mapRecordWithMap:[NSDictionary dictionary] imap:[NSDictionary dictionary]];
    MapRecord cppMapRecord = djinni_generated::MapRecord::toCpp(objcMapRecord);
    auto & cppMap = cppMapRecord.map;
    XCTAssertEqual(cppMap.size(), (size_t)0, @"Count 0 expected, actual: %zd", cppMap.size());
}

- (void)testCppMapListToObjc
{
    MapListRecord cppMapListRecord{ { [self getCppMap] } };
    DBMapListRecord *objcMapListRecord = djinni_generated::MapListRecord::fromCpp(cppMapListRecord);
    NSArray *objcMapList = objcMapListRecord.mapList;
    XCTAssertEqual([objcMapList count], (NSUInteger)1, @"List with 1 map expected, actual no: %lu", (unsigned long)[objcMapList count]);
    [self checkObjcMap:[objcMapList objectAtIndex:0]];
}

- (void)testObjcMapListToCpp
{
    NSArray *objcMapList = @[ [self getObjcMap] ];
    DBMapListRecord *objcMapListRecord = [DBMapListRecord mapListRecordWithMapList:objcMapList];
    auto cppMapListRecord = djinni_generated::MapListRecord::toCpp(objcMapListRecord);
    auto & cppMapList = cppMapListRecord.map_list;
    XCTAssertEqual(cppMapList.size(), (size_t)1, @"List with 1 map expected, actual no: %zd", cppMapList.size());
    [self checkCppMap:cppMapList[0]];
}

- (void)checkCppMap:(const std::unordered_map<std::string, int64_t>)cppMap
{
    XCTAssertEqual(cppMap.size(), (size_t)3, @"Count 3 expected, actual: %zd", cppMap.size());
    XCTAssertEqual(cppMap.at("String1"), 1, @"\"String1 -> 1\" expected");
    XCTAssertEqual(cppMap.at("String2"), 2, @"\"String2 -> 2\" expected");
    XCTAssertEqual(cppMap.at("String3"), 3, @"\"String3 -> 3\" expected");
}

- (void)checkObjcMap:(NSDictionary *)objcMap
{
    XCTAssertEqual([objcMap count], (NSUInteger)3, @"Count 3 expected, actual: %lu", (unsigned long)[objcMap count]);
    // Must test with exact NSNumber constructor as used in DJIMarshal otherwise NSNumber comparison fails on x86_64 simulator devices
    XCTAssertEqual([objcMap objectForKey:@"String1"], @((int64_t)1), @"\"String1 -> 1\" expected");
    XCTAssertEqual([objcMap objectForKey:@"String2"], @((int64_t)2), @"\"String2 -> 2\" expected");
    XCTAssertEqual([objcMap objectForKey:@"String3"], @((int64_t)3), @"\"String3 -> 3\" expected");
}

- (std::unordered_map<std::string, int64_t>)getCppMap
{
    return {
        {"String1", 1},
        {"String2", 2},
        {"String3", 3},
    };
}

- (NSDictionary *)getObjcMap
{
    return @{ @"String1": @1, @"String2": @2, @"String3": @3 };
}

@end
