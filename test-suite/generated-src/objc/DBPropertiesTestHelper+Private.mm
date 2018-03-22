// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from properties.djinni

#import "DBPropertiesTestHelper+Private.h"
#import "DBPropertiesTestHelper.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "DJIMarshal+Private.h"
#include <exception>
#include <stdexcept>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@interface DBPropertiesTestHelper ()

- (id)initWithCpp:(const std::shared_ptr<::testsuite::PropertiesTestHelper>&)cppRef;

@end

@implementation DBPropertiesTestHelper {
    ::djinni::CppProxyCache::Handle<std::shared_ptr<::testsuite::PropertiesTestHelper>> _cppRefHandle;
}

- (id)initWithCpp:(const std::shared_ptr<::testsuite::PropertiesTestHelper>&)cppRef
{
    if (self = [super init]) {
        _cppRefHandle.assign(cppRef);
    }
    return self;
}

- (nonnull NSString *)otherMethod:(nonnull NSString *)argument {
    try {
        auto objcpp_result_ = _cppRefHandle.get()->other_method(::djinni::String::toCpp(argument));
        return ::djinni::String::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (nullable DBPropertiesTestHelper *)createNew {
    try {
        auto objcpp_result_ = ::testsuite::PropertiesTestHelper::create_new();
        return ::djinni_generated::PropertiesTestHelper::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

- (int32_t)item
{
    auto objcpp_result_ = _cppRefHandle.get()->get_item();
    return ::djinni::I32::fromCpp(objcpp_result_);
}
- (void)setItem:(int32_t)item
{
    _cppRefHandle.get()->set_item(::djinni::I32::toCpp(item));
}

- (NSString *)testString
{
    auto objcpp_result_ = _cppRefHandle.get()->get_test_string();
    return ::djinni::String::fromCpp(objcpp_result_);
}
- (void)setTestString:(NSString *)test_string
{
    _cppRefHandle.get()->set_test_string(::djinni::String::toCpp(test_string));
}

- (NSArray<NSNumber *> *)testList
{
    auto objcpp_result_ = _cppRefHandle.get()->get_test_list();
    return ::djinni::List<::djinni::I32>::fromCpp(objcpp_result_);
}
- (void)setTestList:(NSArray<NSNumber *> *)test_list
{
    _cppRefHandle.get()->set_test_list(::djinni::List<::djinni::I32>::toCpp(test_list));
}

- (BOOL)readOnlyBool
{
    auto objcpp_result_ = _cppRefHandle.get()->get_read_only_bool();
    return ::djinni::Bool::fromCpp(objcpp_result_);
}

namespace djinni_generated {

auto PropertiesTestHelper::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    return objc->_cppRefHandle.get();
}

auto PropertiesTestHelper::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    return ::djinni::get_cpp_proxy<DBPropertiesTestHelper>(cpp);
}

}  // namespace djinni_generated

@end
