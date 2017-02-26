// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from foo_duplicate_file_creation.djinni

#include <iostream> // for debugging
#include <cassert>
#include "wrapper_marshal.hpp"
#include "Foo_Callback.hpp"

#include "cw__Foo_Callback.hpp"
#include "dh__Foo_Record.hpp"
#include "dh__list_record_Foo_Record.hpp"

std::shared_ptr<::testsuite::FooCallback> DjinniWrapperFooCallback::get(djinni::WrapperRef<DjinniWrapperFooCallback> dw) {
    if (dw) {
        return dw->wrapped_obj;
    }
    return nullptr;
}

void Foo_Callback___wrapper_add_ref(DjinniWrapperFooCallback * dh) {
    dh->ref_count.fetch_add(1);
}
void Foo_Callback___wrapper_dec_ref(DjinniWrapperFooCallback * dh) {
    const size_t ref = dh->ref_count.fetch_sub(1);
    if (ref == 1) {// value before sub is returned
        delete dh;
    }
}
djinni::Handle<DjinniWrapperFooCallback> DjinniWrapperFooCallback::wrap(std::shared_ptr<::testsuite::FooCallback> obj) {
    if (obj)
        return djinni::Handle<DjinniWrapperFooCallback>(new DjinniWrapperFooCallback{ std::move(obj) }, Foo_Callback___wrapper_dec_ref);
    return nullptr;
}

void cw__Foo_Callback_methodA(DjinniWrapperFooCallback * djinni_this, DjinniObjectHandle * records) {
    djinni::Handle<DjinniObjectHandle> _records(records, list_record_Foo_Record___delete);
    try {
        djinni_this->wrapped_obj->methodA(DjinniListRecordFooRecord::toCpp(std::move(_records)));
    } CW_TRANSLATE_EXCEPTIONS_RETURN();
}

void cw__Foo_Callback_methodB(DjinniWrapperFooCallback * djinni_this, DjinniObjectHandle * records) {
    djinni::Handle<DjinniObjectHandle> _records(records, list_record_Foo_Record___delete);
    try {
        djinni_this->wrapped_obj->methodB(DjinniListRecordFooRecord::toCpp(std::move(_records)));
    } CW_TRANSLATE_EXCEPTIONS_RETURN();
}
