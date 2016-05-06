// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from constants.djinni

#import "DBConstantsInterface+Private.h"
#import "DBConstantsInterface.h"
#import "DBConstantRecord+Private.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "DJIMarshal+Private.h"
#include <exception>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@interface DBConstantsInterface ()

- (id)initWithCpp:(const std::shared_ptr<::testsuite::ConstantsInterface>&)cppRef;

@end

@implementation DBConstantsInterface {
    ::djinni::CppProxyCache::Handle<std::shared_ptr<::testsuite::ConstantsInterface>> _cppRefHandle;
}

- (id)initWithCpp:(const std::shared_ptr<::testsuite::ConstantsInterface>&)cppRef
{
    if (self = [super init]) {
        _cppRefHandle.assign(cppRef);
    }
    return self;
}

- (void)dummy {
    try {
        _cppRefHandle.get()->dummy();
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (NSNumber * __nullable)optBoolConstant
{
    static NSNumber * const s_optBoolConstant = @YES;
    return s_optBoolConstant;
}

+ (NSNumber * __nullable)optI8Constant
{
    static NSNumber * const s_optI8Constant = @1;
    return s_optI8Constant;
}

+ (NSNumber * __nullable)optI16Constant
{
    static NSNumber * const s_optI16Constant = @2;
    return s_optI16Constant;
}

+ (NSNumber * __nullable)optI32Constant
{
    static NSNumber * const s_optI32Constant = @3;
    return s_optI32Constant;
}

+ (NSNumber * __nullable)optI64Constant
{
    static NSNumber * const s_optI64Constant = @4;
    return s_optI64Constant;
}

+ (NSNumber * __nullable)optF32Constant
{
    static NSNumber * const s_optF32Constant = @5.0;
    return s_optF32Constant;
}

+ (NSNumber * __nullable)optF64Constant
{
    static NSNumber * const s_optF64Constant = @5.0;
    return s_optF64Constant;
}

+ (DBConstantRecord * __nonnull)objectConstant
{
    static DBConstantRecord * const s_objectConstant = [[DBConstantRecord alloc] initWithSomeInteger:DBConstantsInterfaceI32Constant
            someString:DBConstantsInterfaceStringConstant];
    return s_objectConstant;
}


namespace djinni_generated {

auto ConstantsInterface::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    return objc->_cppRefHandle.get();
}

auto ConstantsInterface::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    return ::djinni::get_cpp_proxy<DBConstantsInterface>(cpp);
}

}  // namespace djinni_generated

@end
