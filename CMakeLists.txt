cmake_minimum_required(VERSION 3.22)
project(Mongoloid_Bot)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)
include_directories(src)
include_directories(src/main)
include_directories(src/main/cpp)

add_executable(Mongoloid_Bot src/main/cpp/ArduCAM_Mini_2MP_OV2640_functionsMDOED.cpp)
