#include <jni.h>
#include <oboe/Oboe.h>
#include "Kortholt.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_net_simno_kortholt_Kortholt_nativeCreateEngine(
        JNIEnv *env,
        jclass /*unused*/) {
    // We use std::nothrow so 'new' returns a nullptr if the engine creation fails
    auto *engine = new(std::nothrow) Kortholt();
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_Kortholt_nativeDeleteEngine(
        JNIEnv *env,
        jclass,
        jlong engineHandle) {
    delete reinterpret_cast<Kortholt *>(engineHandle);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_Kortholt_nativeSetDefaultStreamValues(
        JNIEnv *env,
        jclass type,
        jint sampleRate,
        jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

} // extern "C"
