#include "cpp_exception.hpp"
#include <exception>

namespace testsuite {

class ExampleException: public std::exception {
    virtual const char* what() const throw() {
        return "Exception Thrown";
    }
};

extern ExampleException EXAMPLE_EXCEPTION;

class CppExceptionImpl : public CppException {
    public:
    CppExceptionImpl() {}
    virtual ~CppExceptionImpl() {}

    virtual int32_t throw_an_exception () override;
};

} // namespace testsuite

