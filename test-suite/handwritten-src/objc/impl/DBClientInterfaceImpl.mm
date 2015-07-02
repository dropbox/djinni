#import "DBClientInterfaceImpl.h"
#import "DBClientReturnedRecord+Private.h"

static NSString *DBHelloWorld = @"Hello World!";
static NSString *DBNonAscii = @"Non-ASCII / 非 ASCII 字符";

@implementation DBClientInterfaceImpl

- (DBClientReturnedRecord *)getRecord:(int64_t)ident utf8string:(NSString *)utf8string misc:(NSString *)misc
{
    NSAssert([utf8string isEqualToString:DBHelloWorld] || [utf8string isEqualToString:DBNonAscii], @"Unexpected String");
    return [[DBClientReturnedRecord alloc] initWithRecordId:ident content:utf8string misc:misc];
}

- (double)identifierCheck:(nonnull NSData *)data r:(int32_t)r jret:(int64_t)jret
{
	(void)data;
	(void)r;
	(void)jret;
	return 0.0;
}

- (NSString *)returnStr
{
    return @"test";
}

@end
