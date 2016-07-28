#include "wchar_test_helpers.hpp"
#include "wchar_test_rec.hpp"

namespace testsuite {

static const wchar_t s1[] = L"some string with unicode \u0000, \u263A, \U0001F4A9 symbols";
static const std::wstring str1(s1, sizeof(s1) / sizeof(*s1) - 1);
static const std::wstring str2 = L"another string with unicode \u263B, \U0001F4A8 symbols";

WcharTestRec WcharTestHelpers::get_record()
{
    return WcharTestRec(str1);
}

std::wstring WcharTestHelpers::get_string()
{
    return str2;
}

bool WcharTestHelpers::check_string(const std::wstring & s)
{
    return s == str2;
}

bool WcharTestHelpers::check_record(const WcharTestRec & r)
{
    return r.s == str1;
}

} // namespace testsuite
