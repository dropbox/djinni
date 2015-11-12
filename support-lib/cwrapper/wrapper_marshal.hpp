// preliminary
// Part of support library for djinni4python, handwritten

#pragma once
#include <string>
#include <vector>
#include <cstdint>
#include <chrono>
#include <thread>
#include <assert.h>
#include <experimental/optional>

#ifdef __cplusplus
extern "C" {
#endif

#include "wrapper_marshal.h"

#ifdef __cplusplus
}
#endif

namespace djinni {
// Deleter Function Type
template<class T>
using DeleterFn = void (T *);

// Custom deleter for unique ptr around Djinni Structs or Handles
template<class T>
class HandleDeleter {
public:
    typedef T ObjectHandle;
    HandleDeleter(DeleterFn<T> * deleter_fn): m_deleter_fn(deleter_fn) {};

    void operator() (ObjectHandle * dh) const {
        if (m_deleter_fn && dh) {
            m_deleter_fn(dh);
        }
    }
private:
    DeleterFn<T> * m_deleter_fn;
};

// Unique ptr for RAII of Djinni Structs or Handles
template<class T>
using BaseHandle = std::unique_ptr<T, HandleDeleter<T>>;

template<class T>
class Handle: public BaseHandle<T> {
public:
    // Constructors set up to avoid having nullptr with no deleter
    Handle(): BaseHandle<T>(nullptr, nullptr) {};
    Handle(T * handle, DeleterFn<T> * deleter): BaseHandle<T>(handle, deleter) {
        assert (deleter);
    }
    Handle(T * handle, std::nullptr_t) = delete;
    Handle(std::nullptr_t): BaseHandle<T>(nullptr, nullptr) {}
};

// Used to make explicit that unique ptrs around djinni wrappers are implemented using references
template<class T>
using WrapperRef = Handle<T>;

namespace optionals {
// Helpers to convert from handle to optional-handle or the other way around
Handle<DjinniOptionalObjectHandle> toOptionalHandle(Handle<DjinniObjectHandle> handle,
                                                       DeleterFn<DjinniOptionalObjectHandle> * deletefn);
Handle<DjinniObjectHandle> fromOptionalHandle(Handle<DjinniOptionalObjectHandle> handle,
                                                 DeleterFn<DjinniObjectHandle> * deletefn);

Handle<DjinniOptionalRecordHandle> toOptionalHandle(Handle<DjinniRecordHandle> handle,
                                                       DeleterFn<DjinniOptionalRecordHandle> * deletefn);
Handle<DjinniRecordHandle> fromOptionalHandle(Handle<DjinniOptionalRecordHandle> handle,
                                                 DeleterFn<DjinniRecordHandle> * deletefn);
// Optional Primitives Template
template<class CppType, class DjinniType>
class Primitive {
public:
    explicit Primitive(CppType data): m_data(data) {};

    static std::experimental::optional<CppType> toCpp(std::unique_ptr<DjinniType> dopt);
    static std::unique_ptr<DjinniType> fromCpp(std::experimental::optional<CppType> copt);

    static std::unique_ptr<DjinniType> newOptional(CppType data);
    CppType m_data;
};

// Optional Primitives toCpp
template<class CppType, class DjinniType>
std::experimental::optional<CppType> Primitive<CppType, DjinniType>::toCpp(std::unique_ptr<DjinniType> dopt) {
    if (dopt) {
        return std::move(dopt->m_data);
    }
    return std::experimental::nullopt;
}

// Optional Primitives fromCpp
template<class CppType, class DjinniType>
std::unique_ptr<DjinniType> Primitive<CppType, DjinniType>::fromCpp(std::experimental::optional<CppType> copt) {
    if (copt){
        return std::unique_ptr<DjinniType>(new DjinniType {std::move(* copt)});
    }
    return nullptr;
}

// Optional Primitives newOptional
template<class CppType, class DjinniType>
std::unique_ptr<DjinniType> Primitive<CppType, DjinniType>::newOptional(CppType data) {
    return std::unique_ptr<DjinniType>(new DjinniType {data});
}

} // namespace djinni::optional

// Throw in cpp exception coming from Python
void cw_throw_if_pending();
void cw_throw_exception(Handle<DjinniPythonExceptionHandle> e_handle); // weak
void cw_default_throw_exception(djinni::Handle<DjinniPythonExceptionHandle> e_handle);

// Register in the exception state a handle to the current exception that occured in either python or cpp user code
// to allow Python to retrieve the exception
void cw_set_pending_exception(const std::exception_ptr & e_ptr);
djinni::Handle<DjinniPythonExceptionHandle> cw_get_py_exception(const std::exception_ptr & e_ptr) noexcept; // weak
djinni::Handle<DjinniPythonExceptionHandle> cw_default_get_py_exception(const std::exception_ptr & e_ptr) noexcept;

#define CW_TRANSLATE_EXCEPTIONS_RETURN(ret) \
    catch (const std::exception &) { \
        djinni::cw_set_pending_exception(std::current_exception()); \
        return ret; \
    }

class py_exception: public std::exception {
public:
    explicit py_exception(Handle<DjinniPythonExceptionHandle> py_e_handle): m_python_exception(std::move(py_e_handle)) { };
    Handle<DjinniPythonExceptionHandle> takePyException() {
        auto aux = std::move(m_python_exception);
        m_python_exception = nullptr;
        return aux;
    }

private:
    Handle<DjinniPythonExceptionHandle> m_python_exception;
};

} // namespace djinni

// OPTIONAL INTEGERS
struct DjinniBoxedI8 : public djinni::optionals::Primitive<int8_t, DjinniBoxedI8> {
    explicit DjinniBoxedI8(int8_t data): djinni::optionals::Primitive<int8_t, DjinniBoxedI8>(data) {};
};

struct DjinniBoxedI16 : public djinni::optionals::Primitive<int16_t, DjinniBoxedI16> {
    explicit DjinniBoxedI16(int16_t data): djinni::optionals::Primitive<int16_t, DjinniBoxedI16>(data) {};
};

struct DjinniBoxedI32 : public djinni::optionals::Primitive<int32_t, DjinniBoxedI32> {
    explicit DjinniBoxedI32(int32_t data): djinni::optionals::Primitive<int32_t, DjinniBoxedI32>(data) {};
};

struct DjinniBoxedI64 : public djinni::optionals::Primitive<int64_t, DjinniBoxedI64> {
    DjinniBoxedI64(int64_t data): djinni::optionals::Primitive<int64_t, DjinniBoxedI64>(data) {};
};

// OPTIONAL FLOATING POINTS
struct DjinniBoxedF32 : public djinni::optionals::Primitive<float, DjinniBoxedF32> {
    DjinniBoxedF32(float data): djinni::optionals::Primitive<float, DjinniBoxedF32>(data) {};
};

struct DjinniBoxedF64 : public djinni::optionals::Primitive<double, DjinniBoxedF64> {
    DjinniBoxedF64(double data): djinni::optionals::Primitive<double, DjinniBoxedF64>(data) {};
};

// OPTIONAL BOOL
struct DjinniBoxedBool : public djinni::optionals::Primitive<bool, DjinniBoxedBool> {
    DjinniBoxedBool(bool data): djinni::optionals::Primitive<bool, DjinniBoxedBool>(data) {};
};

struct DjinniString {
    explicit DjinniString(std::string s);
    static std::unique_ptr<DjinniString> fromCpp(std::string cppstr);
    static std::string toCpp(std::unique_ptr<DjinniString> ds);

    std::string cppstr;
};

struct DjinniBinary {
    explicit DjinniBinary(std::vector<uint8_t>);
    static std::unique_ptr<DjinniBinary> fromCpp(std::vector<uint8_t> cppbinary);
    static std::vector<uint8_t> toCpp(std::unique_ptr<DjinniBinary> db);

    std::vector<uint8_t> cppbinary;
};

struct DjinniDate {
    static std::chrono::system_clock::time_point toCpp(uint64_t duration);
    static uint64_t fromCpp(std::chrono::system_clock::time_point date);
};

// OPTIONAL DATE
struct DjinniBoxedDate : public djinni::optionals::Primitive<uint64_t, DjinniBoxedDate> {
    DjinniBoxedDate(uint64_t data): djinni::optionals::Primitive<uint64_t, DjinniBoxedDate>(data) {};

    static std::experimental::optional<std::chrono::system_clock::time_point> toCpp(std::unique_ptr<DjinniBoxedDate> dopt);
    static std::unique_ptr<DjinniBoxedDate> fromCpp(std::experimental::optional<std::chrono::system_clock::time_point> dopt);

    static std::unique_ptr<DjinniBoxedDate> newOptional(std::chrono::system_clock::time_point data);
};

using DjinniObject = std::shared_ptr<DjinniObjectHandle>;

// OTHER OPTIONALS
struct DjinniOptionalString {
    static std::experimental::optional<std::string>  toCpp(std::unique_ptr<DjinniString> dopt);
    static std::unique_ptr<DjinniString> fromCpp(std::experimental::optional<std::string> ds);
};

struct DjinniOptionalBinary {
    static std::experimental::optional<std::vector<uint8_t>>  toCpp(std::unique_ptr<DjinniBinary> dopt);
    static std::unique_ptr<DjinniBinary> fromCpp(std::experimental::optional<std::vector<uint8_t>> ds);
};
