// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from example.djinni

#import "TXSSortItems+Private.h"
#import "TXSSortItems.h"
#import "DJICppWrapperCache+Private.h"
#import "DJIError.h"
#import "DJIMarshal+Private.h"
#import "TXSItemList+Private.h"
#import "TXSSortItems+Private.h"
#import "TXSTextboxListener+Private.h"
#include <exception>
#include <utility>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@interface TXSSortItems ()

@property (nonatomic, readonly) ::djinni::DbxCppWrapperCache<::textsort::SortItems>::Handle cppRef;

- (id)initWithCpp:(const std::shared_ptr<::textsort::SortItems>&)cppRef;

@end

namespace djinni_generated {

auto SortItems::toCpp(ObjcType objc) -> CppType
{
    return objc ? objc.cppRef.get() : nullptr;
}

auto SortItems::fromCpp(const CppType& cpp) -> ObjcType
{
    return !cpp ? nil : ::djinni::DbxCppWrapperCache<::textsort::SortItems>::getInstance().get(cpp, [] (const auto& p)
    {
        return [[TXSSortItems alloc] initWithCpp:p];
    });
}

}  // namespace djinni_generated

@implementation TXSSortItems

- (id)initWithCpp:(const std::shared_ptr<::textsort::SortItems>&)cppRef
{
    if (self = [super init]) {
        _cppRef.assign(cppRef);
    }
    return self;
}

- (void)sort:(TXSSortOrder)order
       items:(TXSItemList *)items {
    try {
        _cppRef.get()->sort(::djinni::Enum<::textsort::sort_order, TXSSortOrder>::toCpp(order),
                            ::djinni_generated::ItemList::toCpp(items));
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

+ (TXSSortItems *)createWithListener:(id<TXSTextboxListener>)listener {
    try {
        auto r = ::textsort::SortItems::create_with_listener(::djinni_generated::TextboxListener::toCpp(listener));
        return ::djinni_generated::SortItems::fromCpp(r);
    } DJINNI_TRANSLATE_EXCEPTIONS()
}

@end
