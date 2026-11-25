cmake_minimum_required(VERSION 3.5.0)
project(XhuTimetableHarmony)

if(DEFINED PACKAGE_FIND_FILE)
    include(${PACKAGE_FIND_FILE})
endif()

if(CMAKE_OHOS_ARCH_ABI STREQUAL "x86_64" OR OHOS_ARCH STREQUAL "x86_64")
    set(KOTLIN_ABI "x86_64")
else()
    set(KOTLIN_ABI "arm64-v8a")
endif()

set(MODULE_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../..")
list(APPEND CMAKE_PREFIX_PATH "${MODULE_ROOT}/oh_modules/@cpf-kmp-cmp/compose")
find_package(compose REQUIRED CONFIG)

add_library(entry SHARED napi_init.cpp)
target_include_directories(entry PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/include/${KOTLIN_ABI}")
target_link_libraries(entry PUBLIC
    "${MODULE_ROOT}/libs/${KOTLIN_ABI}/libkn.so"
    compose::skikobridge
    libace_napi.z.so
    libhilog_ndk.z.so
    librawfile.z.so
    libc++_shared.so
)
