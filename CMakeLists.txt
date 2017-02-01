cmake_minimum_required(VERSION 3.4.1)

project(djinni)

set(SRC_SHARED
  "support-lib/djinni_common.hpp"
  "support-lib/proxy_cache_interface.hpp"
  "support-lib/proxy_cache_impl.hpp"
)

set(SRC_JNI
  "support-lib/jni/djinni_main.cpp"
  "support-lib/jni/djinni_support.cpp"
  "support-lib/jni/djinni_support.hpp"
  "support-lib/jni/Marshal.hpp"
)

set(SRC_OBJC
  "support-lib/objc/DJICppWrapperCache+Private.h"
  "support-lib/objc/DJIError.h"
  "support-lib/objc/DJIError.mm"
  "support-lib/objc/DJIMarshal+Private.h"
  "support-lib/objc/DJIObjcWrapperCache+Private.h"
  "support-lib/objc/DJIProxyCaches.mm"
)

if (APPLE)

    add_library(djinni STATIC ${SRC_SHARED} ${SRC_OBJC})

    source_group("objc" FILES ${SRC_OBJC})
    target_compile_options(djinni PUBLIC "-fobjc-arc")

    target_include_directories(djinni PUBLIC "$<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>")
    install(DIRECTORY "support-lib" DESTINATION "${CMAKE_INSTALL_INCLUDEDIR}"
        FILES_MATCHING
        PATTERN "*.h"
        PATTERN "*.hpp"
    )

endif(APPLE)

source_group("" FILES ${SRC_SHARED})
