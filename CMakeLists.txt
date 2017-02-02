cmake_minimum_required(VERSION 3.4.1)

project(djinni)

set(SUPPORT_LIB_DIR support-lib)
set(JNI_DIR ${SUPPORT_LIB_DIR}/jni)
set(OBJ_C_DIR ${SUPPORT_LIB_DIR}/objc)

set(SRC_SHARED
    "${SUPPORT_LIB_DIR}/djinni_common.hpp"
    "${SUPPORT_LIB_DIR}/proxy_cache_interface.hpp"
	"${SUPPORT_LIB_DIR}/proxy_cache_impl.hpp"
)

set(JNI_HEADERS
	"${JNI_DIR}/djinni_support.hpp"
	"${JNI_DIR}/Marshal.hpp"
)

set(JNI_SRC
	"${JNI_DIR}/djinni_main.cpp"
	"${JNI_DIR}/djinni_support.cpp"
)

set(OBJC_HEADERS
	"${OBJ_C_DIR}/DJICppWrapperCache+Private.h"
	"${OBJ_C_DIR}/DJIError.h"
	"${OBJ_C_DIR}/DJIMarshal+Private.h"
	"${OBJ_C_DIR}/DJIObjcWrapperCache+Private.h"
)

set(OBJ_C_SRC
	"${OBJ_C_DIR}/DJIError.mm"
	"${OBJ_C_DIR}/DJIProxyCaches.mm"
)

if (APPLE)
	find_library(FOUNDATION Foundation)
	add_library(djinni SHARED ${SRC_SHARED} ${OBJ_C_SRC} ${OBJC_HEADERS})
	target_link_libraries(djinni ${FOUNDATION})
	source_group("objc" FILES ${OBJ_C_SRC} ${OBJC_HEADERS})
	target_compile_options(djinni PUBLIC "-fobjc-arc")
    set(PUBLIC_HEADERS ${SRC_SHARED} ${OBJC_HEADERS})
	set_target_properties(djinni PROPERTIES
		CXX_STANDARD 11
		CXX_STANDARD_REQUIRED true
		CXX_EXTENSIONS false
		FRAMEWORK TRUE
        PUBLIC_HEADER "${PUBLIC_HEADERS}"
		XCODE_ATTRIBUTE_PRODUCT_BUNDLE_IDENTIFIER "com.dropbox.djinni"
	)
	include_directories(
		${SUPPORT_LIB_DIR}
		${OBJ_C_DIR}
	)
endif(APPLE)
source_group("cpp" FILES ${SRC_SHARED})
