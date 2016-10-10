cmake_minimum_required(VERSION 3.6.0)

project(Djinni)

include(GNUInstallDirs)

set(SRC_SHARED
  "support-lib/include/djinni/djinni_common.hpp"
  "support-lib/include/djinni/proxy_cache_interface.hpp"
  "support-lib/src/proxy_cache_impl.hpp"
)
set(SRC_JNI
  "support-lib/include/djinni/jni/djinni_support.hpp"
  "support-lib/include/djinni/jni/Marshal.hpp"
  "support-lib/src/jni/djinni_main.cpp"
  "support-lib/src/jni/djinni_support.cpp"
)
set(SRC_OBJC
  "support-lib/include/djinni/objc/DJICppWrapperCache+Private.h"
  "support-lib/include/djinni/objc/DJIError.h"
  "support-lib/include/djinni/objc/DJIMarshal+Private.h"
  "support-lib/include/djinni/objc/DJIObjcWrapperCache+Private.h"
  "support-lib/src/objc/DJIError.mm"
  "support-lib/src/objc/DJIProxyCaches.mm"
)

add_library(djinni SHARED ${SRC_SHARED})
source_group("" FILES ${SRC_SHARED})

target_include_directories(djinni PUBLIC "$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/support-lib/include>")
set_target_properties(djinni PROPERTIES
  CXX_STANDARD 11
  CXX_STANDARD_REQUIRED true
  CXX_EXTENSIONS false
  )

# Objective-C support
option(DJINNI_WITH_OBJC "Include the Objective-C support code in Djinni." off)
if(DJINNI_WITH_OBJC)
  target_sources(djinni PRIVATE ${SRC_OBJC})
  source_group("objc" FILES ${SRC_OBJC})
  target_compile_options(djinni PUBLIC "-fobjc-arc")
endif()

# JNI support
option(DJINNI_WITH_JNI "Include the JNI support code in Djinni." off)
if(DJINNI_WITH_JNI)
  target_sources(djinni PRIVATE ${SRC_JNI})
  source_group("jni" FILES ${SRC_JNI})
  # Do not use the host's jni.h on Android as it is provided automatically by the NDK
  if(NOT ANDROID)
    find_package(JNI REQUIRED QUIET)
    target_include_directories(djinni
      PUBLIC
      "${JNI_INCLUDE_DIRS}"
    )
  endif()
  # Exceptions and RTTI are disabled in the NDK by default
  if(ANDROID)
    target_compile_options(djinni PRIVATE "-fexceptions" "-frtti")
  endif()
endif()

if(NOT (DJINNI_WITH_OBJC OR DJINNI_WITH_JNI))
  message(FATAL_ERROR "At least one of DJINNI_WITH_OBJC or DJINNI_WITH_JNI must be enabled.")
endif()

# Installation
install(TARGETS djinni
  RUNTIME   DESTINATION "${CMAKE_INSTALL_BINDIR}"
  LIBRARY   DESTINATION "${CMAKE_INSTALL_LIBDIR}"
  FRAMEWORK DESTINATION "${CMAKE_INSTALL_LIBDIR}"
)

option(DJINNI_FRAMEWORK "If enabled the Objective-C binary of Djinni is built as djinni.framework otherwise as djinni.dylib on Apple systems." off)
if(APPLE AND DJINNI_FRAMEWORK)
  set_target_properties(djinni PROPERTIES
    FRAMEWORK true
    XCODE_ATTRIBUTE_PRODUCT_BUNDLE_IDENTIFIER "com.dropbox.djinni"
    # OUTPUT_NAME djinni
  )
  install(DIRECTORY "support-lib/include/djinni/" DESTINATION "${CMAKE_INSTALL_LIBDIR}/djinni.framework/Header"
    FILES_MATCHING
    PATTERN "*.h"
    PATTERN "*.hpp")
else()
  target_include_directories(djinni PUBLIC "$<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>")
  install(DIRECTORY "support-lib/include/" DESTINATION "${CMAKE_INSTALL_INCLUDEDIR}"
      FILES_MATCHING
      PATTERN "*.h"
      PATTERN "*.hpp")
endif()

# Store path to the "run" executable so it can be passed as argument to add_custom_command() scripts
set(DJINNI_RUN_PATH "${CMAKE_CURRENT_SOURCE_DIR}/src/run" CACHE FILEPATH "Path to the Djinni generator executable.")
