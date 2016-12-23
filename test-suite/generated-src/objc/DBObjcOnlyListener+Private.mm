// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from single_language_interfaces.djinni

#import "DBObjcOnlyListener+Private.h"
#import "DBObjcOnlyListener.h"
#import "djinni/objc/DJIObjcWrapperCache+Private.h"
#include <stdexcept>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

namespace djinni_generated {

class ObjcOnlyListener::ObjcProxy final
: public ::testsuite::ObjcOnlyListener
, public ::djinni::ObjcProxyCache::Handle<ObjcType>
{
public:
    using Handle::Handle;
};

}  // namespace djinni_generated

namespace djinni_generated {

auto ObjcOnlyListener::toCpp(ObjcType objc) -> CppType
{
    if (!objc) {
        return nullptr;
    }
    return ::djinni::get_objc_proxy<ObjcProxy>(objc);
}

auto ObjcOnlyListener::fromCppOpt(const CppOptType& cpp) -> ObjcType
{
    if (!cpp) {
        return nil;
    }
    return dynamic_cast<ObjcProxy&>(*cpp).Handle::get();
}

}  // namespace djinni_generated
