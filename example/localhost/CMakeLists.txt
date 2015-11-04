cmake_minimum_required(VERSION 2.8)
project(textsort C CXX)

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
## Shared Library
##

set(support_dir ../../support-lib/jni)
set(textsort_include_dirs ../generated-src/jni/ ../generated-src/cpp/ ../handwritten-src/cpp/)

file(
  GLOB_RECURSE support_srcs
  ${support_dir}/*.cpp)

file(
  GLOB_RECURSE textsort_srcs
  ../generated-src/jni/*.cpp
  ../generated-src/cpp/*.cpp
  ../handwritten-src/cpp/*.cpp)

set(textsort_common_flags "-Wall -Werror -std=c++1y")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${textsort_common_flags}")
if(UNIX OR APPLE)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")
endif()

set(textsort_srcs ${support_srcs} ${textsort_srcs}) 

add_library(TextSortNative SHARED ${textsort_srcs})
include_directories(
  TextSortNative
  ${textsort_include_dirs}
  ${support_dir}
  ${JNI_INCLUDE_DIRS})
target_link_libraries(TextSortNative ${JNI_LIBRARIES})
install(
  TARGETS TextSortNative
  ARCHIVE DESTINATION "${LIB_INSTALL_DIR}"
  LIBRARY DESTINATION "${LIB_INSTALL_DIR}")

