cmake_minimum_required(VERSION 3.6.0)

project(djinni_support_lib)

include(GNUInstallDirs)

set(SRC_SHARED
  "support-lib/djinni_common.hpp"
  "support-lib/proxy_cache_interface.hpp"
  "support-lib/proxy_cache_impl.hpp"
)

set(SRC_JNI
  "support-lib/jni/djinni_support.hpp"
  "support-lib/jni/Marshal.hpp"
  "support-lib/jni/djinni_support.cpp"
)

set(SRC_OBJC
  "support-lib/objc/DJICppWrapperCache+Private.h"
  "support-lib/objc/DJIError.h"
  "support-lib/objc/DJIMarshal+Private.h"
  "support-lib/objc/DJIObjcWrapperCache+Private.h"
  "support-lib/objc/DJIError.mm"
  "support-lib/objc/DJIProxyCaches.mm"
)

option(DJINNI_STATIC_LIB "Build Djinni support library as a static library instead of dynamic (the default)." off)
if(DJINNI_STATIC_LIB)
  add_library(djinni_support_lib STATIC ${SRC_SHARED})
else()
  add_library(djinni_support_lib SHARED ${SRC_SHARED})
endif()
source_group("" FILES ${SRC_SHARED})

set_target_properties(djinni_support_lib PROPERTIES
  CXX_STANDARD 11
  CXX_STANDARD_REQUIRED true
  CXX_EXTENSIONS false
)

# Objective-C support
option(DJINNI_WITH_OBJC "Include the Objective-C support code in Djinni support library." off)
if(DJINNI_WITH_OBJC)
  target_include_directories(djinni_support_lib PUBLIC "$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/support-lib/objc/>")
  target_sources(djinni_support_lib PRIVATE ${SRC_OBJC})
  source_group("objc" FILES ${SRC_OBJC})
  target_compile_options(djinni_support_lib PUBLIC "-fobjc-arc")
endif()

# JNI support
option(DJINNI_WITH_JNI "Include the JNI support code in Djinni support library." off)
if(DJINNI_WITH_JNI)
  if(NOT DJINNI_STATIC_LIB)
    list(APPEND SRC_JNI "support-lib/jni/djinni_main.cpp")
  endif()
  target_include_directories(djinni_support_lib PUBLIC "$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/support-lib/jni/>")
  target_sources(djinni_support_lib PRIVATE ${SRC_JNI})
  source_group("jni" FILES ${SRC_JNI})
  # Do not use the host's jni.h on Android as it is provided automatically by the NDK
  if(NOT ANDROID)
    find_package(JNI REQUIRED QUIET)
    target_include_directories(djinni_support_lib PUBLIC "${JNI_INCLUDE_DIRS}")
  endif()
endif()

if(NOT (DJINNI_WITH_OBJC OR DJINNI_WITH_JNI))
  message(FATAL_ERROR "At least one of DJINNI_WITH_OBJC or DJINNI_WITH_JNI must be enabled.")
endif()

# Store path to the "run" executable so it can be passed as argument to add_custom_command() scripts
set(DJINNI_RUN_PATH "${CMAKE_CURRENT_SOURCE_DIR}/src/run" CACHE FILEPATH "Path to the Djinni generator executable.")
