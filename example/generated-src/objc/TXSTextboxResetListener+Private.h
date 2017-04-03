// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from example.djinni

#include "textbox_reset_listener.hpp"
#include <memory>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@protocol TXSTextboxResetListener;

namespace djinni_generated {

class TextboxResetListener
{
public:
    using CppType = std::shared_ptr<::textsort::TextboxResetListener>;
    using CppOptType = std::shared_ptr<::textsort::TextboxResetListener>;
    using ObjcType = id<TXSTextboxResetListener>;

    using Boxed = TextboxResetListener;

    static CppType toCpp(ObjcType objc);
    static ObjcType fromCppOpt(const CppOptType& cpp);
    static ObjcType fromCpp(const CppType& cpp) { return fromCppOpt(cpp); }

private:
    class ObjcProxy;
};

}  // namespace djinni_generated
