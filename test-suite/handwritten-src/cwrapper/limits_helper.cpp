// Useful for testing limits of primitive types from Python
#include "limits_helper.hpp"

float min_f32_t() {
    return FLT_MIN;
}
float max_f32_t() {
    return FLT_MAX;
}

double min_f64_t() {
    return DBL_MIN;
}
double max_f64_t() {
    return DBL_MAX;
}

