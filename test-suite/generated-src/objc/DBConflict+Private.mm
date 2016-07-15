// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from test.djinni

#import "DBConflict+Private.h"
#import "DBConflict.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#include <exception>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");
#pragma clang diagnostic ignored "-Wdirect-ivar-access"

@interface DBConflict ()

- (id)initWithCpp:(const std::shared_ptr<::testsuite::Conflict>&)cppRef;

@end

@implementation DBConflict {
    ::djinni::CppProxyCache::Handle<std::shared_ptr<::testsuite::Conflict>> _cppRefHandle;
}

- (id)initWithCpp:(const std::shared_ptr<::testsuite::Conflict>&)cppRef
{
    if (self = [super init]) {
        _cppRefHandle.assign(cppRef);
    }
    return self;
}

namespace djinni_generated {

auto Conflict::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    return objc->_cppRefHandle.get();
}

auto Conflict::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    return ::djinni::get_cpp_proxy<DBConflict>(cpp);
}

}  // namespace djinni_generated

@end
