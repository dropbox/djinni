#include <iostream> // for debugging
#include <cstdint>
#include "foo_primitives_impl.hpp"

namespace testsuite {

std::shared_ptr<FooPrimitives> FooPrimitives::create() {
    return std::make_shared<FooPrimitivesImpl>();
}

void FooPrimitivesImpl::set_int8(int8_t private_int) {
    m_pri8 = private_int;
}

int8_t FooPrimitivesImpl::get_int8() {
    return m_pri8;
}

void FooPrimitivesImpl::set_int16(int16_t private_int) {
    m_pri16 = private_int;
}

int16_t FooPrimitivesImpl::get_int16() {
    return m_pri16;
}

void FooPrimitivesImpl::set_int32(int32_t private_int) {
    m_pri32 = private_int;
}

int32_t FooPrimitivesImpl::get_int32() {
    return m_pri32;
}

void FooPrimitivesImpl::set_int64(int64_t private_int) {
    m_pri64 = private_int;
}

int64_t FooPrimitivesImpl::get_int64() {
    return m_pri64;
}

void FooPrimitivesImpl::set_float(float private_float) {
    m_prf = private_float;
}

float FooPrimitivesImpl::get_float() {
    return m_prf;
}

void FooPrimitivesImpl::set_double(double private_double) {
    m_prd = private_double;
}

double FooPrimitivesImpl::get_double() {
    return m_prd;
}

void FooPrimitivesImpl::set_bool(bool private_bool) {
    m_prb = private_bool;
}

bool FooPrimitivesImpl::get_bool() {
    return m_prb;
}

void FooPrimitivesImpl::set_string(const std::string & private_string) {
    m_prs = private_string;
}

std::string FooPrimitivesImpl::get_string() {
    return m_prs;
}

void FooPrimitivesImpl::set_binary(const std::vector<uint8_t> & private_binary) {
    m_prbin = private_binary;
}

std::vector<uint8_t> FooPrimitivesImpl::get_binary() {
    return m_prbin;
}

void FooPrimitivesImpl::set_date(const std::chrono::system_clock::time_point & private_date) {
    m_prdate = private_date;
}

std::chrono::system_clock::time_point FooPrimitivesImpl::get_date() {
    return m_prdate;
}

} // namespace testsuite
