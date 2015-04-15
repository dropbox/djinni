// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client_interface.djinni

#import "DBClientInterface+Private.h"
#import "DBClientInterface.h"
#import "DBClientReturnedRecord+Private.h"
#import "DJIMarshal+Private.h"
#import "DJIObjcWrapperCache+Private.h"

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

namespace djinni_generated { namespace objc {

class ClientInterface::ObjcProxy final
: public ::djinni_generated::ClientInterface
, public ::djinni::DbxObjcWrapperCache<ObjcProxy>::Handle
{
public:
    using Handle::Handle;
    ::djinni_generated::ClientReturnedRecord get_record(int64_t record_id, const std::string & utf8string) override
    {
        @autoreleasepool {
            auto r = [Handle::get() getRecord:(::djinni::I64::fromCpp(record_id))
                                   utf8string:(::djinni::String::fromCpp(utf8string))];
            return ::djinni_generated::objc::ClientReturnedRecord::toCpp(r);
        }
    }
};

auto ClientInterface::toCpp(ObjcType objc) -> CppType
{
    return objc ? ::djinni::DbxObjcWrapperCache<ObjcProxy>::getInstance()->get(objc) : nullptr;
}

auto ClientInterface::fromCpp(const CppType& cpp) -> ObjcType
{
    assert(!cpp || dynamic_cast<ObjcProxy*>(cpp.get()));
    return cpp ? static_cast<ObjcProxy&>(*cpp).Handle::get() : nil;
}

} }  // namespace djinni_generated::objc
