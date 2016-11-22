// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from test.djinni

#import "DBConflictUser+Private.h"
#import "DBConflictUser.h"
#import "DBConflict+Private.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "DJIMarshal+Private.h"
#include <exception>
#include <stdexcept>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");
#pragma clang diagnostic ignored "-Wdirect-ivar-access"

@interface DBConflictUser ()

- (id)initWithCpp:(const std::shared_ptr<::testsuite::ConflictUser>&)cppRef;

@end

@implementation DBConflictUser {
    ::djinni::CppProxyCache::Handle<std::shared_ptr<::testsuite::ConflictUser>> _cppRefHandle;
}

- (id)initWithCpp:(const std::shared_ptr<::testsuite::ConflictUser>&)cppRef
{
    if (self = [super init]) {
        _cppRefHandle.assign(cppRef);
    }
    return self;
}

- (nullable DBConflict *)Conflict {
    try {
        auto objcpp_result_ = _cppRefHandle.get()->Conflict();
        return ::djinni_generated::Conflict::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

- (BOOL)conflictArg:(nonnull NSSet<DBConflict *> *)cs {
    try {
        auto objcpp_result_ = _cppRefHandle.get()->conflict_arg(::djinni::Set<::djinni_generated::Conflict>::toCpp(cs));
        return ::djinni::Bool::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

namespace djinni_generated {

auto ConflictUser::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    return objc->_cppRefHandle.get();
}

auto ConflictUser::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    return ::djinni::get_cpp_proxy<DBConflictUser>(cpp);
}

}  // namespace djinni_generated

@end
