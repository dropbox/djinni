// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from single_language_interfaces.djinni

#import "DBUsesSingleLanguageListeners+Private.h"
#import "DBUsesSingleLanguageListeners.h"
#import "DBJavaOnlyListener+Private.h"
#import "DBObjcOnlyListener+Private.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "DJIObjcWrapperCache+Private.h"
#include <exception>
#include <stdexcept>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

__attribute__((visibility ("default")))
@interface DBUsesSingleLanguageListenersCppProxy : NSObject<DBUsesSingleLanguageListeners>

- (id)initWithCpp:(const std::shared_ptr<::testsuite::UsesSingleLanguageListeners>&)cppRef;

@end

@implementation DBUsesSingleLanguageListenersCppProxy {
    ::djinni::CppProxyCache::Handle<std::shared_ptr<::testsuite::UsesSingleLanguageListeners>> _cppRefHandle;
}

- (id)initWithCpp:(const std::shared_ptr<::testsuite::UsesSingleLanguageListeners>&)cppRef
{
    if (self = [super init]) {
        _cppRefHandle.assign(cppRef);
    }
    return self;
}

- (void)callForObjC:(nullable id<DBObjcOnlyListener>)l {
    try {
        _cppRefHandle.get()->callForObjC(::djinni_generated::ObjcOnlyListener::toCpp(l));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

- (nullable id<DBObjcOnlyListener>)returnForObjC {
    try {
        auto objcpp_result_ = _cppRefHandle.get()->returnForObjC();
        return ::djinni_generated::ObjcOnlyListener::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

- (void)callForJava:(nullable DBJavaOnlyListener *)l {
    try {
        _cppRefHandle.get()->callForJava(::djinni_generated::JavaOnlyListener::toCpp(l));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

- (nullable DBJavaOnlyListener *)returnForJava {
    try {
        auto objcpp_result_ = _cppRefHandle.get()->returnForJava();
        return ::djinni_generated::JavaOnlyListener::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

namespace djinni_generated {

class UsesSingleLanguageListeners::ObjcProxy final
: public ::testsuite::UsesSingleLanguageListeners
, private ::djinni::ObjcProxyBase<ObjcType>
{
    friend class ::djinni_generated::UsesSingleLanguageListeners;
public:
    using ObjcProxyBase::ObjcProxyBase;
    void callForObjC(const std::shared_ptr<::testsuite::ObjcOnlyListener> & c_l) override
    {
        @autoreleasepool {
            [djinni_private_get_proxied_objc_object() callForObjC:(::djinni_generated::ObjcOnlyListener::fromCpp(c_l))];
        }
    }
    std::shared_ptr<::testsuite::ObjcOnlyListener> returnForObjC() override
    {
        @autoreleasepool {
            auto objcpp_result_ = [djinni_private_get_proxied_objc_object() returnForObjC];
            return ::djinni_generated::ObjcOnlyListener::toCpp(objcpp_result_);
        }
    }
    void callForJava(const std::shared_ptr<::testsuite::JavaOnlyListener> & c_l) override
    {
        @autoreleasepool {
            [djinni_private_get_proxied_objc_object() callForJava:(::djinni_generated::JavaOnlyListener::fromCpp(c_l))];
        }
    }
    std::shared_ptr<::testsuite::JavaOnlyListener> returnForJava() override
    {
        @autoreleasepool {
            auto objcpp_result_ = [djinni_private_get_proxied_objc_object() returnForJava];
            return ::djinni_generated::JavaOnlyListener::toCpp(objcpp_result_);
        }
    }
};

}  // namespace djinni_generated

namespace djinni_generated {

auto UsesSingleLanguageListeners::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    if ([(id)objc isKindOfClass:[DBUsesSingleLanguageListenersCppProxy class]]) {
        return ((DBUsesSingleLanguageListenersCppProxy*)objc)->_cppRefHandle.get();
    }
    return ::djinni::get_objc_proxy<ObjcProxy>(objc);
}

auto UsesSingleLanguageListeners::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    if (auto cppPtr = dynamic_cast<ObjcProxy*>(cpp.get())) {
        return cppPtr->djinni_private_get_proxied_objc_object();
    }
    return ::djinni::get_cpp_proxy<DBUsesSingleLanguageListenersCppProxy>(cpp);
}

}  // namespace djinni_generated

@end
