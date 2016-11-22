// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from example.djinni

#import "TXSSortItems+Private.h"
#import "TXSSortItems.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "TXSItemList+Private.h"
#import "TXSSortOrder+Private.h"
#import "TXSTextboxListener+Private.h"
#include <exception>
#include <stdexcept>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");
#pragma clang diagnostic ignored "-Wdirect-ivar-access"

@interface TXSSortItems ()

- (id)initWithCpp:(const std::shared_ptr<::textsort::SortItems>&)cppRef;

@end

@implementation TXSSortItems {
    ::djinni::CppProxyCache::Handle<std::shared_ptr<::textsort::SortItems>> _cppRefHandle;
}

- (id)initWithCpp:(const std::shared_ptr<::textsort::SortItems>&)cppRef
{
    if (self = [super init]) {
        _cppRefHandle.assign(cppRef);
    }
    return self;
}

- (void)sort:(TXSSortOrder)order
       items:(nonnull TXSItemList *)items {
    try {
        _cppRefHandle.get()->sort(::djinni::Enum<::textsort::sort_order, TXSSortOrder>::toCpp(order),
                                  ::djinni_generated::ItemList::toCpp(items));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (nullable TXSSortItems *)createWithListener:(nullable id<TXSTextboxListener>)listener {
    try {
        auto objcpp_result_ = ::textsort::SortItems::create_with_listener(::djinni_generated::TextboxListener::toCpp(listener));
        return ::djinni_generated::SortItems::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (nonnull TXSItemList *)runSort:(nonnull TXSItemList *)items {
    try {
        auto objcpp_result_ = ::textsort::SortItems::run_sort(::djinni_generated::ItemList::toCpp(items));
        return ::djinni_generated::ItemList::fromCpp(objcpp_result_);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

namespace djinni_generated {

auto SortItems::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    return objc->_cppRefHandle.get();
}

auto SortItems::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    return ::djinni::get_cpp_proxy<TXSSortItems>(cpp);
}

}  // namespace djinni_generated

@end
