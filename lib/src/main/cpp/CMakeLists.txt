cmake_minimum_required(VERSION 3.22.1)
project(kortholt)

### BEGIN INCLUDE LIBPD ###

option(PD_UTILS  "Compile utilities" ON)
option(PD_EXTRA  "Compile extras" ON)
option(PD_MULTI  "Compile with multiple instance support" OFF)
option(PD_LOCALE "Set the LC_NUMERIC number format to the default C locale" ON)

set(CMAKE_C_FLAGS         "${CMAKE_C_FLAGS} -Wno-int-to-pointer-cast -Wno-pointer-to-int-cast")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS} -ffast-math -funroll-loops -fomit-frame-pointer -O3")
set(CMAKE_C_FLAGS_DEBUG   "${CMAKE_C_FLAGS} -g -O0")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-Bsymbolic")

add_definitions(-DPD=1 -DUSEAPI_DUMMY=1)

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_MACOSX_RPATH ON)

set(PD_SOURCES
    libpd/pure-data/src/d_arithmetic.c
    libpd/pure-data/src/d_array.c
    libpd/pure-data/src/d_ctl.c
    libpd/pure-data/src/d_dac.c
    libpd/pure-data/src/d_delay.c
    libpd/pure-data/src/d_fft.c
    libpd/pure-data/src/d_fft_fftsg.c
    libpd/pure-data/src/d_filter.c
    libpd/pure-data/src/d_global.c
    libpd/pure-data/src/d_math.c
    libpd/pure-data/src/d_misc.c
    libpd/pure-data/src/d_osc.c
    libpd/pure-data/src/d_resample.c
    libpd/pure-data/src/d_soundfile.c
    libpd/pure-data/src/d_soundfile_aiff.c
    libpd/pure-data/src/d_soundfile_caf.c
    libpd/pure-data/src/d_soundfile_next.c
    libpd/pure-data/src/d_soundfile_wave.c
    libpd/pure-data/src/d_ugen.c
    libpd/pure-data/src/g_all_guis.c
    libpd/pure-data/src/g_all_guis.h
    libpd/pure-data/src/g_array.c
    libpd/pure-data/src/g_bang.c
    libpd/pure-data/src/g_canvas.c
    libpd/pure-data/src/g_canvas.h
    libpd/pure-data/src/g_clone.c
    libpd/pure-data/src/g_editor.c
    libpd/pure-data/src/g_editor_extras.c
    libpd/pure-data/src/g_graph.c
    libpd/pure-data/src/g_guiconnect.c
    libpd/pure-data/src/g_io.c
    libpd/pure-data/src/g_mycanvas.c
    libpd/pure-data/src/g_numbox.c
    libpd/pure-data/src/g_radio.c
    libpd/pure-data/src/g_readwrite.c
    libpd/pure-data/src/g_rtext.c
    libpd/pure-data/src/g_scalar.c
    libpd/pure-data/src/g_slider.c
    libpd/pure-data/src/g_template.c
    libpd/pure-data/src/g_text.c
    libpd/pure-data/src/g_toggle.c
    libpd/pure-data/src/g_traversal.c
    libpd/pure-data/src/g_undo.c
    libpd/pure-data/src/g_vumeter.c
    libpd/pure-data/src/m_atom.c
    libpd/pure-data/src/m_binbuf.c
    libpd/pure-data/src/m_class.c
    libpd/pure-data/src/m_conf.c
    libpd/pure-data/src/m_glob.c
    libpd/pure-data/src/m_imp.h
    libpd/pure-data/src/m_memory.c
    libpd/pure-data/src/m_obj.c
    libpd/pure-data/src/m_pd.c
    libpd/pure-data/src/m_pd.h
    libpd/pure-data/src/m_sched.c
    libpd/pure-data/src/s_audio.c
    libpd/pure-data/src/s_audio_dummy.c
    libpd/pure-data/src/s_inter.c
    libpd/pure-data/src/s_inter_gui.c
    libpd/pure-data/src/s_loader.c
    libpd/pure-data/src/s_main.c
    libpd/pure-data/src/s_net.c
    libpd/pure-data/src/s_path.c
    libpd/pure-data/src/s_print.c
    libpd/pure-data/src/s_stuff.h
    libpd/pure-data/src/s_utf8.c
    libpd/pure-data/src/s_utf8.h
    libpd/pure-data/src/x_acoustics.c
    libpd/pure-data/src/x_arithmetic.c
    libpd/pure-data/src/x_array.c
    libpd/pure-data/src/x_connective.c
    libpd/pure-data/src/x_file.c
    libpd/pure-data/src/x_gui.c
    libpd/pure-data/src/x_interface.c
    libpd/pure-data/src/x_list.c
    libpd/pure-data/src/x_midi.c
    libpd/pure-data/src/x_misc.c
    libpd/pure-data/src/x_net.c
    libpd/pure-data/src/x_scalar.c
    libpd/pure-data/src/x_text.c
    libpd/pure-data/src/x_time.c
    libpd/pure-data/src/x_vexp.c
    libpd/pure-data/src/x_vexp.h
    libpd/pure-data/src/x_vexp_fun.c
    libpd/pure-data/src/x_vexp_if.c
)

set(PD_EXTRA_SOURCES
    libpd/pure-data/extra/bob~/bob~.c
    libpd/pure-data/extra/bonk~/bonk~.c
    libpd/pure-data/extra/choice/choice.c
    libpd/pure-data/extra/fiddle~/fiddle~.c
    libpd/pure-data/extra/loop~/loop~.c
    libpd/pure-data/extra/lrshift~/lrshift~.c
    libpd/pure-data/extra/pd~/pdsched.c
    libpd/pure-data/extra/pd~/pd~.c
    libpd/pure-data/extra/pique/pique.c
    libpd/pure-data/extra/sigmund~/sigmund~.c
    libpd/pure-data/extra/stdout/stdout.c
)

set(LIBPD_SOURCES
    libpd/libpd_wrapper/s_libpdmidi.c
    libpd/libpd_wrapper/x_libpdreceive.c
    libpd/libpd_wrapper/x_libpdreceive.h
    libpd/libpd_wrapper/z_hooks.c
    libpd/libpd_wrapper/z_hooks.h
    libpd/libpd_wrapper/z_libpd.c
)
set(LIBPD_UTILS_SOURCES
    libpd/libpd_wrapper/util/ringbuffer.c
    libpd/libpd_wrapper/util/ringbuffer.h
    libpd/libpd_wrapper/util/z_print_util.c
    libpd/libpd_wrapper/util/z_print_util.h
    libpd/libpd_wrapper/util/z_queued.c
    libpd/libpd_wrapper/util/z_queued.h
)

source_group(pd         FILES ${PD_SOURCES})
source_group(pdextra    FILES ${PD_EXTRA_SOURCES})
source_group(libpd      FILES ${LIBPD_SOURCES})
source_group(libpdutils FILES ${LIBPD_UTILS_SOURCES})

include_directories(libpd/libpd_wrapper)
include_directories(libpd/pure-data/src)

find_package(Threads REQUIRED)

# create final list of source files
set(SOURCE_FILES ${PD_SOURCES} ${LIBPD_SOURCES})
if(PD_UTILS)
    list(APPEND SOURCE_FILES ${LIBPD_UTILS_SOURCES})
endif()
if(PD_EXTRA)
    list(APPEND SOURCE_FILES ${PD_EXTRA_SOURCES})
endif()

add_library(libpd_static STATIC ${SOURCE_FILES})
add_library(libpd        SHARED ${SOURCE_FILES})

# set the output library name for libpd depending on the settings
set(LIBPD_OUTPUT_NAME     pd)
if (PD_MULTI)
    set(LIBPD_OUTPUT_NAME ${LIBPD_OUTPUT_NAME}-multi)
endif()
set_target_properties(libpd_static PROPERTIES OUTPUT_NAME ${LIBPD_OUTPUT_NAME})
set_target_properties(libpd        PROPERTIES OUTPUT_NAME ${LIBPD_OUTPUT_NAME})

# add appropriate compile definitions
set(LIBPD_COMPILE_DEFINITIONS PD_INTERNAL)
list(APPEND LIBPD_COMPILE_DEFINITIONS HAVE_UNISTD_H=1)

if(PD_EXTRA)
    list(APPEND LIBPD_COMPILE_DEFINITIONS LIBPD_EXTRA=1)
endif()

if(PD_MULTI)
    list(APPEND LIBPD_COMPILE_DEFINITIONS PDINSTANCE=1 PDTHREADS=1)
endif()

if(NOT PD_LOCALE)
    list(APPEND LIBPD_COMPILE_DEFINITIONS LIBPD_NO_NUMERIC=1)
endif()

list(APPEND LIBPD_COMPILE_DEFINITIONS HAVE_LIBDL)
target_link_libraries(libpd PUBLIC ${CMAKE_DL_LIBS})

target_compile_definitions(libpd_static PRIVATE ${LIBPD_COMPILE_DEFINITIONS})
target_compile_definitions(libpd        PRIVATE ${LIBPD_COMPILE_DEFINITIONS})

# link against necessary libraries
target_link_libraries(libpd PUBLIC ${CMAKE_THREAD_LIBS_INIT})

find_library(M_LIBRARIES m)
if(M_LIBRARIES)
    target_link_libraries(libpd PUBLIC ${M_LIBRARIES})
endif()

set(PD_NATIVE_SOURCES
    libpd/jni/z_jni_plain.c
)
add_library(pdnative SHARED ${PD_NATIVE_SOURCES})
target_include_directories(pdnative PRIVATE libpd/jni libpd/libpd_wrapper/util)
target_link_libraries(pdnative libpd)

### END INCLUDE LIBPD ###

### BEGIN INCLUDE OBOE ###

set(OBOE_DIR ./oboe)
add_subdirectory(${OBOE_DIR} ./oboe)
include_directories(
    ${OBOE_DIR}/include
    ${OBOE_DIR}/samples/shared
    ${OBOE_DIR}/samples/hello-oboe/src/main/cpp
    ${OBOE_DIR}/apps/OboeTester/app/src/main/cpp/util
)

# Debug utilities
set(DEBUG_UTILS_PATH "${OBOE_DIR}/samples/debug-utils")
set(DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)
include_directories(${DEBUG_UTILS_PATH})

### END INCLUDE OBOE ###

### KORTHOLT ###
set(APP_SOURCES
    jni_bridge.cpp
    PureDataSource.cpp
    Kortholt.cpp
    ${OBOE_DIR}/samples/hello-oboe/src/main/cpp/LatencyTuningCallback.cpp
    ${OBOE_DIR}/apps/OboeTester/app/src/main/cpp/util/WaveFileWriter.cpp
)
add_library(
    kortholt
    SHARED
    ${DEBUG_UTILS_SOURCES}
    ${APP_SOURCES}
)
target_include_directories(
    kortholt
    PRIVATE
    oboe/src/common
    libpd/cpp
    libpd/libpd_wrapper/util
)
target_link_libraries(kortholt android log libpd oboe)
target_compile_options(kortholt PRIVATE -Wall -Werror -Ofast)
