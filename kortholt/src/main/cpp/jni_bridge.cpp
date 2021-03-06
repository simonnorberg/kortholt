#include <jni.h>
#include <oboe/Oboe.h>
#include "Kortholt.h"

std::vector<int> convertJavaArrayToVector(JNIEnv *env, jintArray intArray) {
    std::vector<int> v;
    jsize length = env->GetArrayLength(intArray);
    if (length > 0) {
        jint *elements = env->GetIntArrayElements(intArray, nullptr);
        v.insert(v.end(), &elements[0], &elements[length]);
        env->ReleaseIntArrayElements(intArray, elements, 0);
    }
    return v;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_net_simno_kortholt_Kortholt_nativeCreateKortholt(
        JNIEnv *env,
        jobject /*unused*/,
        jintArray jCpuIds) {
    std::vector<int> cpuIds = convertJavaArrayToVector(env, jCpuIds);
    auto *kortholt = new(std::nothrow) Kortholt(std::move(cpuIds));
    return reinterpret_cast<jlong>(kortholt);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_Kortholt_nativeDeleteKortholt(
        JNIEnv * /*unused*/,
        jobject /*unused*/,
        jlong kortholtHandle) {
    delete reinterpret_cast<Kortholt *>(kortholtHandle);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_Kortholt_nativeSetDefaultStreamValues(
        JNIEnv * /*unused*/,
        jobject /*unused*/,
        jint sampleRate,
        jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

} // extern "C"
