#include <jni.h>
#include <oboe/Oboe.h>
#include "Kortholt.h"

std::vector<int> convertJavaArrayToVector(JNIEnv *env, jintArray intArray) {
    std::vector<int> v;
    jsize length = env->GetArrayLength(intArray);
    if (length > 0) {
        jboolean isCopy;
        jint *elements = env->GetIntArrayElements(intArray, &isCopy);
        for (int i = 0; i < length; i++) {
            v.push_back(elements[i]);
        }
    }
    return v;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_net_simno_kortholt_Kortholt_nativeCreateKortholt(
        JNIEnv *env,
        jclass /*unused*/,
        jintArray jCpuIds) {
    std::vector<int> cpuIds = convertJavaArrayToVector(env, jCpuIds);
    auto *kortholt = new(std::nothrow) Kortholt(std::move(cpuIds));
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
