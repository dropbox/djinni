#pragma once

#include <cstdint>
#include <string>
#include <vector>
#include <chrono>
#include "foo_primitives.hpp"

namespace testsuite {

class FooPrimitivesImpl final: public FooPrimitives {
public:
    virtual void set_int8(int8_t private_int) override;
    virtual int8_t get_int8() override;

    virtual void set_int16(int16_t private_int) override;
    virtual int16_t get_int16() override;

    virtual void set_int32(int32_t private_int) override;
    virtual int32_t get_int32() override;

    virtual void set_int64(int64_t private_int) override;
    virtual int64_t get_int64() override;

    virtual void set_bool(bool private_bool) override;
    virtual bool get_bool() override;

    virtual void set_float(float private_float) override;
    virtual float get_float() override;

    virtual void set_double(double private_double) override;
    virtual double get_double() override;

    virtual void set_string(const std::string & private_string) override;
    virtual std::string get_string() override;

    virtual void set_binary(const std::vector<uint8_t> & private_binary) override;
    virtual std::vector<uint8_t> get_binary() override;

    virtual void set_date(const std::chrono::system_clock::time_point & private_date) override;
    virtual std::chrono::system_clock::time_point get_date() override;

    static std::shared_ptr<FooPrimitives> create();

private:
    int8_t m_pri8 = 0;
    int16_t m_pri16 = 0;
    int32_t m_pri32 = 0;
    int64_t m_pri64 = 0;

    bool m_prb = false;

    float m_prf = 0.0;
    double m_prd = 0.0;

    std::string m_prs;

    std::vector<uint8_t> m_prbin;
    std::chrono::system_clock::time_point m_prdate;
};

} // namespace testsuite
