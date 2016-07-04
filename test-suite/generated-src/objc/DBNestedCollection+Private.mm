// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from nested_collection.djinni

#import "DBNestedCollection+Private.h"
#import "djinni/support-lib/objc/DJIMarshal+Private.h"
#include <cassert>

namespace djinni_generated {

auto NestedCollection::toCpp(ObjcType obj) -> CppType
{
    assert(obj);
    return {::djinni::List<::djinni::Set<::djinni::String>>::toCpp(obj.setList)};
}

auto NestedCollection::fromCpp(const CppType& cpp) -> ObjcType
{
    return [[DBNestedCollection alloc] initWithSetList:(::djinni::List<::djinni::Set<::djinni::String>>::fromCpp(cpp.set_list))];
}

}  // namespace djinni_generated
