cmake_minimum_required(VERSION 2.8)                                                                                                                                                    
project(djinni-test-suite C CXX)

##
## Options
##

set(LIB_INSTALL_DIR "${CMAKE_INSTALL_PREFIX}/lib"
  CACHE PATH "Installation directory for libraries (default: prefix/lib).")

##
## Global Dependencies
##

find_package(JNI)
if (NOT JNI_FOUND)
  message(
    FATAL_ERROR
	"Could not find JNI. Did you install a JDK? Set $JAVA_HOME to override")
endif()


##
## Test Suite Shared Library
##

set(support_dir ../../support-lib/jni)
set(test_include_dirs ../generated-src/jni/ ../generated-src/cpp/ ../handwritten-src/cpp/)

file(
  GLOB_RECURSE support_srcs
  ${support_dir}/*.cpp)

file(
  GLOB_RECURSE test_suite_srcs
  ../generated-src/jni/*.cpp
  ../generated-src/cpp/*.cpp
  ../handwritten-src/cpp/*.cpp)

set(test_suite_common_flags "-g -Wall -Werror -std=c++11")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${test_suite_common_flags}")
if(UNIX OR APPLE)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif()

set(test_lib_srcs ${support_srcs} ${test_suite_srcs}) 

add_library(DjinniTestNative SHARED ${test_lib_srcs})
include_directories(
  DjinniTestNative
  ${test_include_dirs}
  ${support_dir}
  ${JNI_INCLUDE_DIRS})
target_link_libraries(DjinniTestNative ${JNI_LIBRARIES})
install(
  TARGETS DjinniTestNative
  ARCHIVE DESTINATION "${LIB_INSTALL_DIR}"
  LIBRARY DESTINATION "${LIB_INSTALL_DIR}")

