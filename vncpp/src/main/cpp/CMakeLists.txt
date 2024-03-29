#
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Build a static lib and copy it into distribution place for independent app to use
# we could not delete the bin directory is because android studio would error out
# when it checks target's existance. Only distribution place is used as import to app

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

set(vncmain_lib-DIR ${CMAKE_CURRENT_SOURCE_DIR}/lib)
set(vncmain_src-DIR ${CMAKE_CURRENT_SOURCE_DIR}/src)
set(vncmain_include-DIR ${CMAKE_CURRENT_SOURCE_DIR}/include)
set(libvnc_sr-DIR ${CMAKE_CURRENT_SOURCE_DIR}/libvnc)

add_library(lib-nettle STATIC IMPORTED)
set_target_properties(lib-nettle PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libnettle.a)

add_library(lib-hogweed STATIC IMPORTED)
set_target_properties(lib-hogweed PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libhogweed.a)

add_library(lib-gmp STATIC IMPORTED)
set_target_properties(lib-gmp PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libgmp.a)

add_library(lib-tasn1 STATIC IMPORTED)
set_target_properties(lib-tasn1 PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libtasn1.a)

add_library(lib-gnutls_ext STATIC IMPORTED)
set_target_properties(lib-gnutls_ext PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libgnutls_ext.a)

add_library(lib-miniopencdk STATIC IMPORTED)
set_target_properties(lib-miniopencdk PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libminiopencdk.a)

add_library(lib-minitasn1 STATIC IMPORTED)
set_target_properties(lib-minitasn1 PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libminitasn1.a)

add_library(lib-gnutls_auth STATIC IMPORTED)
set_target_properties(lib-gnutls_auth PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libgnutls_auth.a)

add_library(lib-gnutls_alg STATIC IMPORTED)
set_target_properties(lib-gnutls_alg PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libgnutls_alg.a)

add_library(lib-accelerated STATIC IMPORTED)
set_target_properties(lib-accelerated PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libaccelerated.a)

add_library(lib-gnutlsxx STATIC IMPORTED)
set_target_properties(lib-gnutlsxx PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libgnutlsxx.a)

add_library(lib-crypto STATIC IMPORTED)
set_target_properties(lib-crypto PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libcrypto.a)

add_library(lib-gnutls STATIC IMPORTED)
set_target_properties(lib-gnutls PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/gnutls/libgnutls.a)
add_dependencies( # Specifies the target library.
                  lib-gnutls
                  # Add the dependency library to the gnutls library
                  lib-tasn1
                  lib-nettle
                  lib-hogweed
                  )

add_library(lib-gpg-error STATIC IMPORTED)
set_target_properties(lib-gpg-error PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libgpg-error.a)

add_library(lib-gcrypt STATIC IMPORTED)
set_target_properties(lib-gcrypt PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libgcrypt.a)
add_dependencies( # Specifies the target library.
                  lib-gcrypt

                  # Add the dependency library to the gcrypt library
                  lib-gpg-error)

add_library(lib-jpeg STATIC IMPORTED)
set_target_properties(lib-jpeg PROPERTIES IMPORTED_LOCATION
    ${vncmain_lib-DIR}/libjpeg.a)

add_library(lib-vncclient STATIC
            ${libvnc_sr-DIR}/common/minilzo.c
            ${libvnc_sr-DIR}/libvncclient/tls_none.c
            ${libvnc_sr-DIR}/libvncclient/cursor.c
            ${libvnc_sr-DIR}/libvncclient/listen.c
            ${libvnc_sr-DIR}/libvncclient/vncviewer.c
            ${libvnc_sr-DIR}/libvncclient/rfbproto.c
            ${libvnc_sr-DIR}/libvncclient/sockets.c
    )
target_include_directories(lib-vncclient PRIVATE
                           ${libvnc_sr-DIR}
                           ${libvnc_sr-DIR}/common
                           ${vncmain_include-DIR}/jpeg
                           ${vncmain_include-DIR}/gcrypt
                           )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

find_library( # Sets the name of the path variable.
              z-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              z )

add_library(vncmain SHARED
	       	 ${vncmain_src-DIR}/model/screen/ClientScreen.cpp
			 ${vncmain_src-DIR}/model/comunication/HandlerRFB.cpp
			 ${vncmain_src-DIR}/model/comunication/ClientConnectionRFB.cpp
			 ${vncmain_src-DIR}/model/Vnc.cpp
			 ${vncmain_src-DIR}/ObservableJNI.cpp
			 ${vncmain_src-DIR}/JavaBridge.cpp)

target_include_directories(vncmain PRIVATE
                           ${vncmain_src-DIR}
                           ${libvnc_sr-DIR})


target_link_libraries( # Specifies the target library.
                       vncmain

                       # Links the target library to the vncmain library
                       lib-vncclient
                       lib-gcrypt
                       lib-jpeg
                       lib-gnutls
                       lib-tasn1
                       lib-nettle
                       lib-hogweed
                       lib-gmp
                       lib-gpg-error
                       ${log-lib}
                       ${z-lib}
                       )