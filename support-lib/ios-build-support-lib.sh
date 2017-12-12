#!/bin/sh

# This script helps you to build apple fat binaries.
# This uses the excellent https://github.com/leetal/ios-cmake cmake platform file.

# The directory used for building
BUILD_DIR=$(pwd)/build

# The directory where library will be generated
OUTPUT_DIR=$(pwd)/out/apple

# The architecture to build for apple
# See https://github.com/leetal/ios-cmake for more informations
BUILD_APPLE_ARCHITECTURES=(OS SIMULATOR64)

# Enable disable bitcode
ENABLE_BITCODE=true

# Helper functions for building one arhcitecture
function build_apple_native_static_arch {
  echo "Building Native $1"

  cd ${BUILD_DIR}
  BUILD_PATH=build_$1
  if [ -d ${BUILD_PATH} ]; then
    rm -f ${BUILD_PATH}/*
    rmdir ${BUILD_PATH}
  fi

  mkdir -p  ${BUILD_PATH}
  cd ${BUILD_PATH}
  
  cmake ../.. -DDJINNI_WITH_OBJC=ON -DDJINNI_WITH_JNI=OFF -DDJINNI_STATIC_LIB=ON -DCMAKE_TOOLCHAIN_FILE=../cmake/ios.toolchain.cmake -DIOS_PLATFORM=$1 -DENABLE_BITCODE=${ENABLE_BITCODE} -DCMAKE_INSTALL_PREFIX=${BUILD_PATH}
  make -j 4
  
  cp ${BUILD_DIR}/${BUILD_PATH}/libdjinni_support_lib.a ${OUTPUT_DIR}/libdjinni_support_lib_$1.a
  
  cd ${BUILD_DIR}
  rm -rf ${BUILD_PATH}
}

# Start of the script
echo "Checking build directory..."
if [ -d ${BUILD_DIR} ]; then
    rm -rf ${BUILD_DIR}/*
    rmdir ${BUILD_DIR}
fi
mkdir -p ${BUILD_DIR}

echo "Checking output directory..."
if [ -d ${OUTPUT_DIR} ]; then
    rm -rf ${OUTPUT_DIR}/*
    rmdir ${OUTPUT_DIR}
fi

mkdir -p ${OUTPUT_DIR}

# Build elements

for arch in ${BUILD_APPLE_ARCHITECTURES[@]}
do
  build_apple_native_static_arch ${arch}
done

# Lipo creates a fat binary
lipo -create ${OUTPUT_DIR}/*.a -output ${OUTPUT_DIR}/libdjinni_support_lib_UNIVERSAL.a
