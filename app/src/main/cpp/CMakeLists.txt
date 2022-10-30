# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("jni_project")

# TODO  第一步：导入头文件
include_directories("inc")
add_subdirectory(librtmp)

#批量导入所有源文件
file(GLOB allCPP *.c *.h *.cpp)

include_directories(${CMAKE_SOURCE_DIR}/x264/${CMAKE_ANDROID_ARCH_ABI}/include)




# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        jni_project

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${allCPP})

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

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# TODO  第二步：设置库文件路径  设置CMAKE_CXX_FLAGS环境变量(库的路径)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")
#静态库 FFmpeg
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/x264/${CMAKE_ANDROID_ARCH_ABI}/lib")

#add_library(getndk STATIC IMPORTED)
#set_target_properties(getndk PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libgetndk.a)

target_link_libraries( # Specifies the target library.
        jni_project

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        fmod
        fmodL
        rtmp
        x264)