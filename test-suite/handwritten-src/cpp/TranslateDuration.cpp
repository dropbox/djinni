#include "test_duration.hpp"

namespace testsuite {

std::string TestDuration::hoursString(std::chrono::duration<int32_t, std::ratio<3600>> dt)
{
    return std::to_string(dt.count());
}

std::string TestDuration::minutesString(std::chrono::duration<int32_t, std::ratio<60>> dt)
{
    return std::to_string(dt.count());
}

std::string TestDuration::secondsString(std::chrono::duration<int32_t, std::ratio<1>> dt)
{
    return std::to_string(dt.count());
}

std::string TestDuration::millisString(std::chrono::duration<int32_t, std::milli> dt)
{
    return std::to_string(dt.count());
}

std::string TestDuration::microsString(std::chrono::duration<int32_t, std::micro> dt)
{
    return std::to_string(dt.count());
}

std::string TestDuration::nanosString(std::chrono::duration<int32_t, std::nano> dt)
{
    return std::to_string(dt.count());
}

std::chrono::duration<int32_t, std::ratio<3600>> TestDuration::hours(int32_t count)
{
    return std::chrono::duration<int32_t, std::ratio<3600>>{count};
}

std::chrono::duration<int32_t, std::ratio<60>> TestDuration::minutes(int32_t count)
{
    return std::chrono::duration<int32_t, std::ratio<60>>{count};
}

std::chrono::duration<int32_t, std::ratio<1>> TestDuration::seconds(int32_t count)
{
    return std::chrono::duration<int32_t, std::ratio<1>>{count};
}

std::chrono::duration<int32_t, std::milli> TestDuration::millis(int32_t count)
{
    return std::chrono::duration<int32_t, std::milli>{count};
}

std::chrono::duration<int32_t, std::micro> TestDuration::micros(int32_t count)
{
    return std::chrono::duration<int32_t, std::micro>{count};
}

std::chrono::duration<int32_t, std::nano> TestDuration::nanos(int32_t count)
{
    return std::chrono::duration<int32_t, std::nano>{count};
}

std::chrono::duration<double, std::ratio<3600>> TestDuration::hoursf(double count)
{
    return std::chrono::duration<double, std::ratio<3600>>{count};
}

std::chrono::duration<double, std::ratio<60>> TestDuration::minutesf(double count)
{
    return std::chrono::duration<double, std::ratio<60>>{count};
}

std::chrono::duration<double, std::ratio<1>> TestDuration::secondsf(double count)
{
    return std::chrono::duration<double, std::ratio<1>>{count};
}

std::chrono::duration<double, std::milli> TestDuration::millisf(double count)
{
    return std::chrono::duration<double, std::milli>{count};
}

std::chrono::duration<double, std::micro> TestDuration::microsf(double count)
{
    return std::chrono::duration<double, std::micro>{count};
}

std::chrono::duration<double, std::nano> TestDuration::nanosf(double count)
{
    return std::chrono::duration<double, std::nano>{count};
}

std::experimental::optional<std::chrono::duration<int64_t, std::ratio<1>>> TestDuration::box(int64_t count)
{
    using D = std::chrono::duration<int64_t, std::ratio<1>>;
    using O = std::experimental::optional<D>;
    return count < 0 ? O{} : O{D{count}};
}

int64_t TestDuration::unbox(std::experimental::optional<std::chrono::duration<int64_t, std::ratio<1>>> dt)
{
    return dt ? dt->count() : -1;
}

} // namespace testsuite
