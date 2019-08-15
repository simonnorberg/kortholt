#include <jni.h>
#include <oboe/Oboe.h>
#include "Kortholt.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_net_simno_kortholt_Kortholt_nativeCreateKortholt(
        JNIEnv /*unused*/,
        jclass /*unused*/) {
    auto *kortholt = new(std::nothrow) Kortholt();
    return reinterpret_cast<jlong>(kortholt);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_Kortholt_nativeDeleteKortholt(
        JNIEnv /*unused*/,
        jclass /*unused*/,
        jlong kortholtHandle) {
    delete reinterpret_cast<Kortholt *>(kortholtHandle);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_Kortholt_nativeSetDefaultStreamValues(
        JNIEnv /*unused*/,
        jclass /*unused*/,
        jint sampleRate,
        jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

} // extern "C"
