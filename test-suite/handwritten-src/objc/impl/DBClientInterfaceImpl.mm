#import "DBClientInterfaceImpl.h"
#import "DBClientReturnedRecord+Private.h"

static NSString *DBHelloWorld = @"Hello World!";
static NSString *DBNonAscii = @"Non-ASCII / 非 ASCII 字符";

@implementation DBClientInterfaceImpl

- (DBClientReturnedRecord *)getRecord:(int64_t)ident utf8string:(NSString *)utf8string
{
    NSAssert([utf8string isEqualToString:DBHelloWorld] || [utf8string isEqualToString:DBNonAscii], @"Unexpected String");
    return [[DBClientReturnedRecord alloc] initWithRecordId:ident content:utf8string];
}

@end
