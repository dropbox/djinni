// preliminary
// Part of support library for djinni4python, handwritten
// pure C header to be passed as argument to the generated cffi code (to be used for creating a pycffi lib)

#pragma once // python_cdef_ignore
#include <stdint.h> // python_cdef_ignore
#include <stdbool.h> // python_cdef_ignore

// Exceptions: relevant to both Cpp from Python and Python from Cpp
struct DjinniPythonExceptionHandle;

// Exceptions: Cpp From Python
void djinni_create_and_set_cpp_from_py_exception(struct DjinniPythonExceptionHandle * py_e);

// Exceptions: Python From Cpp
// allow creating python exceptions from cpp
typedef struct DjinniPythonExceptionHandle * (* CreateExceptionFnPtr) (struct DjinniString *);
void _djinni_add_callback_create_py_from_cpp_exception(CreateExceptionFnPtr fct_ptr); // called in cpp

typedef void (* DeleteExceptionFnPtr) (struct DjinniPythonExceptionHandle *);
void _djinni_add_callback_exception___delete(DeleteExceptionFnPtr fct_ptr); // called in cpp

struct DjinniPythonExceptionHandle * djinni_from_python_check_and_clear_exception(); // called in python

// Djinni Structs and Boxed Types and Optionals
struct DjinniBoxedI8;
struct DjinniBoxedI16;
struct DjinniBoxedI32;
struct DjinniBoxedI64;
struct DjinniBoxedF32;
struct DjinniBoxedF64;
struct DjinniBoxedBool;
struct DjinniBoxedDate;

struct DjinniOptionalString;
struct DjinniString;

struct DjinniOptionalBinary;
struct DjinniBinary;

struct DjinniOptionalObjectHandle;
struct DjinniObjectHandle; // wrapper around void*, should be left without definition
struct DjinniOptionalRecordHandle;
struct DjinniRecordHandle; // wrapper around void*, should be left without definition

// DJINNI STRING
struct DjinniString * create_djinni_string(const char * s, size_t len);
void delete_djinni_string(struct DjinniString * ss);
const char * get_djinni_string_chars(const struct DjinniString * ds);
size_t get_djinni_string_len(const struct DjinniString * ds);

// DJINNI BINARY
struct DjinniBinary * create_djinni_binary(const uint8_t * b, size_t len);
void delete_djinni_binary(struct DjinniBinary * db);
const uint8_t * get_djinni_binary_uint8s(const struct DjinniBinary *);
size_t get_djinni_binary_len(const struct DjinniBinary *);

// DJINNI PRIMITIVE OPTIONALS
// OPTIONAL INTEGERS
struct DjinniBoxedI8 * create_djinni_boxed_i8(int8_t pyopt);
void delete_djinni_boxed_i8(struct DjinniBoxedI8 * dopt);
int8_t get_djinni_boxed_i8_data(struct DjinniBoxedI8 * dopt);

struct DjinniBoxedI16 * create_djinni_boxed_i16(int16_t pyopt);
void delete_djinni_boxed_i16(struct DjinniBoxedI16 * dopt);
int16_t get_djinni_boxed_i16_data(struct DjinniBoxedI16 * dopt);

struct DjinniBoxedI32 * create_djinni_boxed_i32(int32_t pyopt);
void delete_djinni_boxed_i32(struct DjinniBoxedI32 * dopt);
int32_t get_djinni_boxed_i32_data(struct DjinniBoxedI32 * dopt);

struct DjinniBoxedI64 * create_djinni_boxed_i64(int64_t pyopt);
void delete_djinni_boxed_i64(struct DjinniBoxedI64 * dopt);
int64_t get_djinni_boxed_i64_data(struct DjinniBoxedI64 * dopt);

// OPTIONAL FLOATING POINTS
struct DjinniBoxedF32 * create_djinni_boxed_f32(float pyopt);
void delete_djinni_boxed_f32(struct DjinniBoxedF32 * dopt);
float get_djinni_boxed_f32_data(struct DjinniBoxedF32 * dopt);

struct DjinniBoxedF64 * create_djinni_boxed_f64(double pyopt);
void delete_djinni_boxed_f64(struct DjinniBoxedF64 * dopt);
double get_djinni_boxed_f64_data(struct DjinniBoxedF64 * dopt);

// OPTIONAL BOOL
struct DjinniBoxedBool * create_djinni_boxed_bool(bool pyopt);
void delete_djinni_boxed_bool(struct DjinniBoxedBool * dopt);
bool get_djinni_boxed_bool_data(struct DjinniBoxedBool * dopt);

// OPTIONAL DATE
struct DjinniBoxedDate * create_djinni_boxed_date(uint64_t pyopt);
void delete_djinni_boxed_date(struct DjinniBoxedDate * dopt);
uint64_t get_djinni_boxed_date_data(struct DjinniBoxedDate * dopt);
