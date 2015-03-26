#import "DBMapRecord+Private.h"
#import "DBMapListRecord+Private.h"
#import <XCTest/XCTest.h>

#include "map_record.hpp"

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
    djinni_generated::MapRecord cppMapRecord([self getCppMap]);
	DBMapRecord *objcMapRecord = djinni_generated::objc::MapRecord::fromCpp(cppMapRecord);
    [self checkObjcMap:objcMapRecord.map];
}

- (void)testObjcToCpp
{
    DBMapRecord *objcMapRecord = [[DBMapRecord alloc] initWithMap:[self getObjcMap]];
    djinni_generated::MapRecord cppMapRecord = djinni_generated::objc::MapRecord::toCpp(objcMapRecord);
    [self checkCppMap:cppMapRecord.map];
}

- (void)testCppToObjcEmpty
{
    djinni_generated::MapRecord cppMapRecord{ {} };
    DBMapRecord *objcMapRecord = djinni_generated::objc::MapRecord::fromCpp(cppMapRecord);

    XCTAssertEqual((int)[objcMapRecord.map count], 0, @"Count 0 expected, actual: %lu", (unsigned long)[objcMapRecord.map count]);
}

- (void)testObjcToCppEmpty
{
    DBMapRecord *objcMapRecord = [[DBMapRecord alloc] initWithMap:[[NSMutableDictionary alloc] init]];
    djinni_generated::MapRecord cppMapRecord = djinni_generated::objc::MapRecord::toCpp(objcMapRecord);
    auto & cppMap = cppMapRecord.map;
    XCTAssertEqual((int)cppMap.size(), 0, @"Count 0 expected, actual: %zd", cppMap.size());
}

- (void)testCppMapListToObjc
{
    djinni_generated::MapListRecord cppMapListRecord{ { [self getCppMap] } };
    DBMapListRecord *objcMapListRecord = djinni_generated::objc::MapListRecord::fromCpp(cppMapListRecord);
    NSArray *objcMapList = objcMapListRecord.mapList;
    XCTAssertEqual((int)[objcMapList count], 1, @"List with 1 map expected, actual no: %lu", (unsigned long)[objcMapList count]);
    [self checkObjcMap:[objcMapList objectAtIndex:0]];
}

- (void)testObjcMapListToCpp
{
    NSMutableArray *objcMapList = [[NSMutableArray alloc] initWithObjects:[self getObjcMap], nil];
    DBMapListRecord *objcMapListRecord = [[DBMapListRecord alloc] initWithMapList:objcMapList];
    auto cppMapListRecord = djinni_generated::objc::MapListRecord::toCpp(objcMapListRecord);
    auto & cppMapList = cppMapListRecord.map_list;
    XCTAssertEqual((int)cppMapList.size(), 1, @"List with 1 map expected, actual no: %zd", cppMapList.size());
    [self checkCppMap:cppMapList[0]];
}

- (void)checkCppMap:(const std::unordered_map<std::string, int64_t>)cppMap
{
    XCTAssertEqual((int)cppMap.size(), 3, @"Count 3 expected, actual: %zd", cppMap.size());
    XCTAssertEqual(cppMap.at("String1"), 1, @"\"String1 -> 1\" expected");
    XCTAssertEqual(cppMap.at("String2"), 2, @"\"String2 -> 2\" expected");
    XCTAssertEqual(cppMap.at("String3"), 3, @"\"String3 -> 3\" expected");
}

- (void)checkObjcMap:(NSDictionary *)objcMap
{
    XCTAssertEqual((int)[objcMap count], 3, @"Count 3 expected, actual: %lu", (unsigned long)[objcMap count]);
    XCTAssertEqual([objcMap objectForKey:@"String1"], [NSNumber numberWithLongLong:1], @"\"String1 -> 1\" expected");
    XCTAssertEqual([objcMap objectForKey:@"String2"], [NSNumber numberWithLongLong:2], @"\"String2 -> 2\" expected");
    XCTAssertEqual([objcMap objectForKey:@"String3"], [NSNumber numberWithLongLong:3], @"\"String3 -> 3\" expected");
}

- (std::unordered_map<std::string, int64_t>)getCppMap
{
    return {
        {"String1", 1},
        {"String2", 2},
        {"String3", 3},
    };
}

- (NSMutableDictionary *)getObjcMap
{
    NSMutableDictionary *objcMap = [[NSMutableDictionary alloc] initWithCapacity:3];
    [objcMap setObject:[NSNumber numberWithLongLong:1] forKey:@"String1"];
    [objcMap setObject:[NSNumber numberWithLongLong:2] forKey:@"String2"];
    [objcMap setObject:[NSNumber numberWithLongLong:3] forKey:@"String3"];
    return objcMap;
}


@end
