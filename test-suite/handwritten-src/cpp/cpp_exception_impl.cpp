#include "cpp_exception_impl.hpp"
#include <exception>

using djinni::cpp::CppException;

int32_t CppExceptionImpl::throw_an_exception() {
    throw ExampleException();
}

std::shared_ptr<CppException> CppException::get() {
    return std::make_shared<CppExceptionImpl>();
}
