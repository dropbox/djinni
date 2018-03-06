// preliminary
// Part of support library for djinni4python, handwritten

#include <stdio.h> // for debugging
#include "wrapper_marshal.hpp"
#include "thread_local.hpp"
#include "../djinni_common.hpp"
#include <string>
#include <iostream> // for debugging

using namespace djinni;
using namespace djinni::optionals;
using std::experimental::optional;
using std::experimental::nullopt;

// Helpers to convert from a 'handle type' to a 'optional-handle type' or the other way around
// Useful in generated code, when marshaling and unmarshaling optional structured types, as it helps differentiate
// which overload of a toCpp, fromCpp function to be used
Handle<DjinniOptionalObjectHandle> optionals::toOptionalHandle(Handle<DjinniObjectHandle> handle,
                                                                         DeleterFn<DjinniOptionalObjectHandle> deletefn) {
    return Handle<DjinniOptionalObjectHandle>((DjinniOptionalObjectHandle *) handle.release(), deletefn);
}

Handle<DjinniObjectHandle> optionals::fromOptionalHandle(Handle<DjinniOptionalObjectHandle> handle,
                                                                   DeleterFn<DjinniObjectHandle> deletefn) {
    return Handle<DjinniObjectHandle>((DjinniObjectHandle *) handle.release(), deletefn);
}

Handle<DjinniOptionalRecordHandle> optionals::toOptionalHandle(Handle<DjinniRecordHandle> handle,
                                                                         DeleterFn<DjinniOptionalRecordHandle> deletefn) {
    return Handle<DjinniOptionalRecordHandle>((DjinniOptionalRecordHandle *) handle.release(), deletefn);

}

Handle<DjinniRecordHandle> optionals::fromOptionalHandle(Handle<DjinniOptionalRecordHandle> handle,
                                                                   DeleterFn<DjinniRecordHandle> deletefn) {
    return Handle<DjinniRecordHandle>((DjinniRecordHandle *) handle.release(), deletefn);
}

// Support for Handling Exceptions
static CreateExceptionFnPtr s_djinni_create_py_from_cpp_exception;
static DeleteExceptionFnPtr s_djinni_exception___delete;

// Structure holding current exception, if any
struct ExceptionState {
    static djinni::Handle<DjinniPythonExceptionHandle> newHandle(DjinniPythonExceptionHandle * c_ptr) {
        return djinni::Handle<DjinniPythonExceptionHandle>(c_ptr, s_djinni_exception___delete);
    }

    djinni::Handle<DjinniPythonExceptionHandle> takeException() {
        auto aux = std::move(handle);
        handle = nullptr;
        return aux;
    }

    djinni::Handle<DjinniPythonExceptionHandle> handle;
};

// Current exception state
static djinni::support_lib::ThreadLocal<ExceptionState> s_exception_state;

// to be called from Python
void djinni_create_and_set_cpp_from_py_exception(DjinniPythonExceptionHandle * py_e_handle) {
    s_exception_state.get().handle = ExceptionState::newHandle(py_e_handle);
}

void _djinni_add_callback_create_py_from_cpp_exception(CreateExceptionFnPtr fct_ptr) {
    s_djinni_create_py_from_cpp_exception = fct_ptr;
}
void _djinni_add_callback_exception___delete(DeleteExceptionFnPtr fct_ptr) {
    s_djinni_exception___delete = fct_ptr;
}

void djinni::cw_default_throw_exception(djinni::Handle<DjinniPythonExceptionHandle> e_handle) {
        throw djinni::py_exception(std::move(e_handle));
}

DJINNI_WEAK_DEFINITION
void djinni::cw_throw_exception(djinni::Handle<DjinniPythonExceptionHandle> e_handle) {
     cw_default_throw_exception(std::move(e_handle));
}

// to be called from cpp to allow cpp impl to know about pyhton exception
void djinni::cw_throw_if_pending() {
    auto e_handle = s_exception_state.get().takeException();

    if (e_handle) {
        cw_throw_exception(std::move(e_handle));
    }
}

djinni::Handle<DjinniPythonExceptionHandle> djinni::cw_default_get_py_exception(const std::exception_ptr & e_ptr) noexcept {
    try {
        if (e_ptr) {
            std::rethrow_exception(e_ptr);
        }
    } catch (djinni::py_exception & py_e) { // exception generated by user python code
         return py_e.takePyException();
    } catch (const std::exception & e) { // exception generated by user cpp code
        auto e_mesg = DjinniString::fromCpp(e.what());
        return ExceptionState::newHandle(s_djinni_create_py_from_cpp_exception(e_mesg.release()));
    }
    assert(false);
}

// The argument is an exception_ptr for either a std::exception or a subclass of std::exception called py_exception
// Creates or retrieves a handle to a python exception from the exception_ptr
DJINNI_WEAK_DEFINITION
djinni::Handle<DjinniPythonExceptionHandle> djinni::cw_get_py_exception(const std::exception_ptr & e_ptr) noexcept {
    return cw_default_get_py_exception(e_ptr);
}

// Create a python exception from the exception pointer, and set it in s_exception_state to allow Python to
// notice an exception was thrown
// The argument is an exception_ptr for either a std::exception or a subclass of std::exception called py_exception
void djinni::cw_set_pending_exception(const std::exception_ptr & e_ptr) {
    s_exception_state.get().handle = cw_get_py_exception(e_ptr);
}

DjinniPythonExceptionHandle * djinni_from_python_check_and_clear_exception() {
    if (s_exception_state.get().handle) {
        return s_exception_state.get().takeException().release();
    }
    return nullptr;
}

// DJINNI PRIMITIVE OPTIONALS
// OPTIONAL INTEGERS
DjinniBoxedI8 * create_djinni_boxed_i8(int8_t pyopt) {
    return DjinniBoxedI8::newOptional(pyopt).release();
}
void delete_djinni_boxed_i8(DjinniBoxedI8 * dopt) {
    delete dopt;
}
int8_t get_djinni_boxed_i8_data(DjinniBoxedI8 * dopt) {
    return dopt->m_data;
}

DjinniBoxedI16 * create_djinni_boxed_i16(int16_t pyopt) {
    return DjinniBoxedI16::newOptional(pyopt).release();
}
void delete_djinni_boxed_i16(DjinniBoxedI16 * dopt) {
    delete dopt;
}
int16_t get_djinni_boxed_i16_data(DjinniBoxedI16 * dopt) {
    return dopt->m_data;
}

DjinniBoxedI32 * create_djinni_boxed_i32(int32_t pyopt) {
    return DjinniBoxedI32::newOptional(pyopt).release();
}
void delete_djinni_boxed_i32(DjinniBoxedI32 * dopt) {
    delete dopt;
}
int32_t get_djinni_boxed_i32_data(DjinniBoxedI32 * dopt) {
    return dopt->m_data;
}

DjinniBoxedI64 * create_djinni_boxed_i64(int64_t pyopt) {
    return DjinniBoxedI64::newOptional(pyopt).release();
}
void delete_djinni_boxed_i64(DjinniBoxedI64 * dopt) {
    delete dopt;
}
int64_t get_djinni_boxed_i64_data(DjinniBoxedI64 * dopt) {
    return dopt->m_data;
}

// OPTIONAL FLOATING POINTS
DjinniBoxedF32 * create_djinni_boxed_f32(float pyopt) {
    return DjinniBoxedF32::newOptional(pyopt).release();
}
void delete_djinni_boxed_f32(DjinniBoxedF32 * dopt) {
    delete dopt;
}
float get_djinni_boxed_f32_data(DjinniBoxedF32 * dopt) {
    return dopt->m_data;
}

DjinniBoxedF64 * create_djinni_boxed_f64(double pyopt) {
    return DjinniBoxedF64::newOptional(pyopt).release();
}
void delete_djinni_boxed_f64(DjinniBoxedF64 * dopt) {
    delete dopt;
}
double get_djinni_boxed_f64_data(DjinniBoxedF64 * dopt) {
    return dopt->m_data;
}

// OPTIONAL BOOL
DjinniBoxedBool * create_djinni_boxed_bool(bool pyopt) {
    return DjinniBoxedBool::newOptional(pyopt).release();
}
void delete_djinni_boxed_bool(DjinniBoxedBool * dopt) {
    delete dopt;
}
bool get_djinni_boxed_bool_data(DjinniBoxedBool * dopt) {
    return dopt->m_data;
}

// DJINNI STRING
DjinniString::DjinniString(std::string s) {
    this->cppstr = std::move(s);
}

DjinniString * create_djinni_string(const char * s, size_t len) {
    return DjinniString::fromCpp(std::string(s,len)).release();
}

void delete_djinni_string(DjinniString * ss) {
    delete ss;
}

const char * get_djinni_string_chars(const DjinniString * ds) {
    return ds->cppstr.c_str();
}

size_t get_djinni_string_len(const DjinniString * ds) {
    return ds->cppstr.length();
}

std::unique_ptr<DjinniString> DjinniString::fromCpp(std::string cppstr) {
     return std::unique_ptr<DjinniString>(new DjinniString(std::move(cppstr)));
}

std::string DjinniString::toCpp(std::unique_ptr<DjinniString> ds) {
    return std::move(ds->cppstr);
}

// OPTIONAL DJINNI STRING
optional<std::string> DjinniOptionalString::toCpp(std::unique_ptr<DjinniString> ds) {
    if (ds) {
        return std::move(ds->cppstr);
    }
    return nullopt;
}

std::unique_ptr<DjinniString> DjinniOptionalString::fromCpp(optional<std::string> s) {
    if (s) {
        return DjinniString::fromCpp(std::move(* s));
    }
    return nullptr;
}

// DJINNI BINARY
DjinniBinary::DjinniBinary(std::vector<uint8_t> b) {
    this->cppbinary = std::move(b);
}

DjinniBinary * create_djinni_binary(const uint8_t * b, size_t len) {
    return DjinniBinary::fromCpp(std::vector<uint8_t>(b, b+len)).release();
}

void delete_djinni_binary(DjinniBinary * db) {
    delete db;
}

const uint8_t * get_djinni_binary_uint8s(const DjinniBinary * db) {
    return db->cppbinary.data();
}

size_t get_djinni_binary_len(const DjinniBinary * db) {
  return db->cppbinary.size();
}

std::unique_ptr<DjinniBinary> DjinniBinary::fromCpp(std::vector<uint8_t> cppbinary) {
     return std::unique_ptr<DjinniBinary>(new DjinniBinary(std::move(cppbinary)));
}

std::vector<uint8_t> DjinniBinary::toCpp(std::unique_ptr<DjinniBinary> db) {
    return std::move(db->cppbinary);
}

// OPTIONAL DJINNI BINARY
std::unique_ptr<DjinniBinary> DjinniOptionalBinary::fromCpp(optional<std::vector<uint8_t>> cppbinary) {
    if (cppbinary) {
        return DjinniBinary::fromCpp(std::move(* cppbinary));
    }
   return nullptr;
}

optional<std::vector<uint8_t>> DjinniOptionalBinary::toCpp(std::unique_ptr<DjinniBinary> db) {
    if (db) {
        return std::move(db->cppbinary);
    }
    return nullopt;
}

// DJINNI DATE
auto static const POSIX_EPOCH = std::chrono::system_clock::from_time_t(0);
std::chrono::system_clock::time_point DjinniDate::toCpp(uint64_t duration) {
    return POSIX_EPOCH + std::chrono::milliseconds{duration};
}

uint64_t DjinniDate::fromCpp(std::chrono::system_clock::time_point date) {
    return std::chrono::duration_cast<std::chrono::milliseconds>(date - POSIX_EPOCH).count();
}

// OPTIONAL DJINNI DATE
std::experimental::optional<std::chrono::system_clock::time_point>
DjinniBoxedDate::toCpp(std::unique_ptr<DjinniBoxedDate> dopt) {
    if (!dopt) {
        return nullopt;
    }
    return DjinniDate::toCpp(dopt->m_data);
}

std::unique_ptr<DjinniBoxedDate>
DjinniBoxedDate::fromCpp(std::experimental::optional<std::chrono::system_clock::time_point> dopt) {
    if (!dopt) {
        return nullptr;
    }
    return newOptional(*dopt);
}

std::unique_ptr<DjinniBoxedDate>
DjinniBoxedDate::newOptional(std::chrono::system_clock::time_point data) {
    return std::unique_ptr<DjinniBoxedDate>(new DjinniBoxedDate(DjinniDate::fromCpp(data)));
}

DjinniBoxedDate * create_djinni_boxed_date(uint64_t pyopt) {
    return DjinniBoxedDate::newOptional(DjinniDate::toCpp(pyopt)).release();
}

void delete_djinni_boxed_date(DjinniBoxedDate * dopt) {
    delete dopt;
}

uint64_t get_djinni_boxed_date_data(DjinniBoxedDate * dopt) {
    return dopt->m_data;
}
