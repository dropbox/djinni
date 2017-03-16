cmake_minimum_required(VERSION 3.6.0)

project(Djinni)

include(GNUInstallDirs)

set(SRC_SHARED
  "djinni_common.hpp"
  "proxy_cache_interface.hpp"
  "proxy_cache_impl.hpp"
)

set(SRC_JNI
  "jni/djinni_support.hpp"
  "jni/Marshal.hpp"
  "jni/djinni_support.cpp"
)

set(SRC_OBJC
  "objc/DJICppWrapperCache+Private.h"
  "objc/DJIError.h"
  "objc/DJIMarshal+Private.h"
  "objc/DJIObjcWrapperCache+Private.h"
  "objc/DJIError.mm"
  "objc/DJIProxyCaches.mm"
)

option(DJINNI_STATIC_LIB "Build Djinni as a static library instead of dynamic (the default)." off)
if(DJINNI_STATIC_LIB)
  add_library(djinni STATIC ${SRC_SHARED})
else()
  add_library(djinni SHARED ${SRC_SHARED})
endif()
source_group("" FILES ${SRC_SHARED})

set_target_properties(djinni PROPERTIES
  CXX_STANDARD 11
  CXX_STANDARD_REQUIRED true
  CXX_EXTENSIONS false
)

# Objective-C support
option(DJINNI_WITH_OBJC "Include the Objective-C support code in Djinni." off)
if(DJINNI_WITH_OBJC)
  target_include_directories(djinni PUBLIC "$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/objc/>")
  target_sources(djinni PRIVATE ${SRC_OBJC})
  source_group("objc" FILES ${SRC_OBJC})
  target_compile_options(djinni PUBLIC "-fobjc-arc")
endif()

# JNI support
option(DJINNI_WITH_JNI "Include the JNI support code in Djinni." off)
if(DJINNI_WITH_JNI)
  if(NOT DJINNI_STATIC_LIB)
    list(APPEND SRC_JNI "jni/djinni_main.cpp")
  endif()
  target_include_directories(djinni PUBLIC "$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/jni/>")
  target_sources(djinni PRIVATE ${SRC_JNI})
  source_group("jni" FILES ${SRC_JNI})
  # Do not use the host's jni.h on Android as it is provided automatically by the NDK
  if(NOT ANDROID)
    find_package(JNI REQUIRED QUIET)
    target_include_directories(djinni PUBLIC "${JNI_INCLUDE_DIRS}")
  endif()
  # Exceptions and RTTI are disabled in the NDK by default
  if(ANDROID)
    target_compile_options(djinni PRIVATE "-fexceptions" "-frtti")
  endif()
endif()

if(NOT (DJINNI_WITH_OBJC OR DJINNI_WITH_JNI))
  message(FATAL_ERROR "At least one of DJINNI_WITH_OBJC or DJINNI_WITH_JNI must be enabled.")
endif()

# Store path to the "run" executable so it can be passed as argument to add_custom_command() scripts
set(DJINNI_RUN_PATH "${CMAKE_CURRENT_SOURCE_DIR}/src/run" CACHE FILEPATH "Path to the Djinni generator executable.")
