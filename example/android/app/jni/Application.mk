# Android makefile for textsort shared lib, jni wrapper around libtextsort C API

APP_ABI := all
APP_OPTIM := release
APP_PLATFORM := android-8
# GCC 4.8 Toolchain - requires NDK r9
NDK_TOOLCHAIN_VERSION = clang
# GNU libc++ is the only Android STL which supports C++11 features
APP_STL := c++_static
APP_BUILD_SCRIPT := jni/Android.mk
APP_MODULES := libtextsort_jni
