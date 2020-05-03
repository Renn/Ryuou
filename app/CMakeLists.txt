# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
set(JNILIBS ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
set(CPP_SOURCE ${CMAKE_SOURCE_DIR}/src/main/cpp)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${CPP_SOURCE}/native-lib.cpp)

# ==================================================
# ffmpeg related headers
include_directories(${JNILIBS}/include)

# ffmpeg related shared libraries
# for encoding and decoding
add_library(avcodec
        SHARED
        IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libavcodec.so)

# for interaction with multimedia devices
add_library(avdevice
        SHARED
        IMPORTED)
set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libavdevice.so)

# for processing pixel format of frame
add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libavfilter.so)

# for formatting
add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libavformat.so)

# utilities
add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libavutil.so)

# for resampling audio, changing format and mixing
add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libswresample.so)

# for changing scale
add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale PROPERTIES IMPORTED_LOCATION
        ${JNILIBS}/${CMAKE_ANDROID_ARCH_ABI}/libswscale.so)

# ==================================================

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries(
        native-lib
        ${log-lib}
        avcodec
        avdevice
        avfilter
        avformat
        avutil
        swresample
        swscale
)