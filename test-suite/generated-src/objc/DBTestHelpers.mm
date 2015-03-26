// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from test.djinni

#import "DBTestHelpers+Private.h"
#import "DBTestHelpers.h"
#import "DBAssortedIntegers+Private.h"
#import "DBClientInterface+Private.h"
#import "DBMapListRecord+Private.h"
#import "DBNestedCollection+Private.h"
#import "DBPrimitiveList+Private.h"
#import "DBSetRecord+Private.h"
#import "DBToken+Private.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "DJIMarshal+Private.h"
#include <exception>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@interface DBTestHelpers ()

@property (nonatomic, readonly) ::djinni::DbxCppWrapperCache<::djinni_generated::TestHelpers>::Handle cppRef;

- (id)initWithCpp:(const std::shared_ptr<::djinni_generated::TestHelpers>&)cppRef;

@end

namespace djinni_generated { namespace objc {

auto TestHelpers::toCpp(ObjcType objc) -> CppType
{
    return objc ? objc.cppRef.get() : nullptr;
}

auto TestHelpers::fromCpp(const CppType& cpp) -> ObjcType
{
    return !cpp ? nil : ::djinni::DbxCppWrapperCache<::djinni_generated::TestHelpers>::getInstance().get(cpp, [] (const auto& p)
    {
        return [[DBTestHelpers alloc] initWithCpp:p];
    });
}

} }  // namespace djinni_generated::objc

@implementation DBTestHelpers

- (id)initWithCpp:(const std::shared_ptr<::djinni_generated::TestHelpers>&)cppRef
{
    if (self = [super init]) {
        _cppRef.assign(cppRef);
    }
    return self;
}

+ (DBSetRecord *)getSetRecord {
    try {
        auto r = ::djinni_generated::TestHelpers::get_set_record();
        return ::djinni_generated::objc::SetRecord::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (BOOL)checkSetRecord:(DBSetRecord *)rec {
    try {
        auto r = ::djinni_generated::TestHelpers::check_set_record(::djinni_generated::objc::SetRecord::toCpp(rec));
        return ::djinni::Bool::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (DBPrimitiveList *)getPrimitiveList {
    try {
        auto r = ::djinni_generated::TestHelpers::get_primitive_list();
        return ::djinni_generated::objc::PrimitiveList::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (BOOL)checkPrimitiveList:(DBPrimitiveList *)pl {
    try {
        auto r = ::djinni_generated::TestHelpers::check_primitive_list(::djinni_generated::objc::PrimitiveList::toCpp(pl));
        return ::djinni::Bool::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (DBNestedCollection *)getNestedCollection {
    try {
        auto r = ::djinni_generated::TestHelpers::get_nested_collection();
        return ::djinni_generated::objc::NestedCollection::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (BOOL)checkNestedCollection:(DBNestedCollection *)nc {
    try {
        auto r = ::djinni_generated::TestHelpers::check_nested_collection(::djinni_generated::objc::NestedCollection::toCpp(nc));
        return ::djinni::Bool::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (NSDictionary *)getMap {
    try {
        auto r = ::djinni_generated::TestHelpers::get_map();
        return ::djinni::Map<::djinni::String, ::djinni::I64>::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (BOOL)checkMap:(NSDictionary *)m {
    try {
        auto r = ::djinni_generated::TestHelpers::check_map(::djinni::Map<::djinni::String, ::djinni::I64>::toCpp(m));
        return ::djinni::Bool::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (NSDictionary *)getEmptyMap {
    try {
        auto r = ::djinni_generated::TestHelpers::get_empty_map();
        return ::djinni::Map<::djinni::String, ::djinni::I64>::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (BOOL)checkEmptyMap:(NSDictionary *)m {
    try {
        auto r = ::djinni_generated::TestHelpers::check_empty_map(::djinni::Map<::djinni::String, ::djinni::I64>::toCpp(m));
        return ::djinni::Bool::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (DBMapListRecord *)getMapListRecord {
    try {
        auto r = ::djinni_generated::TestHelpers::get_map_list_record();
        return ::djinni_generated::objc::MapListRecord::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (BOOL)checkMapListRecord:(DBMapListRecord *)m {
    try {
        auto r = ::djinni_generated::TestHelpers::check_map_list_record(::djinni_generated::objc::MapListRecord::toCpp(m));
        return ::djinni::Bool::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (void)checkClientInterfaceAscii:(id<DBClientInterface>)i {
    try {
        ::djinni_generated::TestHelpers::check_client_interface_ascii(::djinni_generated::objc::ClientInterface::toCpp(i));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (void)checkClientInterfaceNonascii:(id<DBClientInterface>)i {
    try {
        ::djinni_generated::TestHelpers::check_client_interface_nonascii(::djinni_generated::objc::ClientInterface::toCpp(i));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (void)checkEnumMap:(NSDictionary *)m {
    try {
        ::djinni_generated::TestHelpers::check_enum_map(::djinni::Map<::djinni::Enum<::djinni_generated::color, DBColor>, ::djinni::String>::toCpp(m));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (DBToken *)tokenId:(DBToken *)t {
    try {
        auto r = ::djinni_generated::TestHelpers::token_id(::djinni_generated::objc::Token::toCpp(t));
        return ::djinni_generated::objc::Token::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (DBToken *)createCppToken {
    try {
        auto r = ::djinni_generated::TestHelpers::create_cpp_token();
        return ::djinni_generated::objc::Token::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (void)checkCppToken:(DBToken *)t {
    try {
        ::djinni_generated::TestHelpers::check_cpp_token(::djinni_generated::objc::Token::toCpp(t));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (int64_t)cppTokenId:(DBToken *)t {
    try {
        auto r = ::djinni_generated::TestHelpers::cpp_token_id(::djinni_generated::objc::Token::toCpp(t));
        return ::djinni::I64::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (NSNumber *)returnNone {
    try {
        auto r = ::djinni_generated::TestHelpers::return_none();
        return ::djinni::Optional<std::experimental::optional, ::djinni::I32>::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (DBAssortedIntegers *)assortedIntegersId:(DBAssortedIntegers *)i {
    try {
        auto r = ::djinni_generated::TestHelpers::assorted_integers_id(::djinni_generated::objc::AssortedIntegers::toCpp(i));
        return ::djinni_generated::objc::AssortedIntegers::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

@end
