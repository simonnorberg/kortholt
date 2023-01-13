#include <jni.h>
#include <oboe/Oboe.h>
#include "Kortholt.h"

std::vector<int> convertJavaArrayToVector(
        JNIEnv *env,
        jintArray intArray
) {
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
Java_net_simno_kortholt_KortholtPlayer_nativeCreateKortholt(
        JNIEnv *env,
        jobject /*unused*/,
        jintArray jCpuIds,
        jboolean stream
) {
    std::vector<int> cpuIds = convertJavaArrayToVector(env, jCpuIds);
    auto *kortholt = new(std::nothrow) Kortholt(std::move(cpuIds), stream);
    return reinterpret_cast<jlong>(kortholt);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_KortholtPlayer_nativeDeleteKortholt(
        JNIEnv * /*unused*/,
        jobject /*unused*/,
        jlong kortholtHandle
) {
    delete reinterpret_cast<Kortholt *>(kortholtHandle);
}

JNIEXPORT void JNICALL
Java_net_simno_kortholt_KortholtPlayer_nativeSetDefaultStreamValues(
        JNIEnv * /*unused*/,
        jobject /*unused*/,
        jint sampleRate,
        jint framesPerBurst
) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

JNIEXPORT jint JNICALL
Java_net_simno_kortholt_KortholtPlayer_nativeSaveWaveFile(
        JNIEnv *env,
        jobject /*unused*/,
        jlong kortholtHandle,
        jstring fileName,
        jlong duration,
        jstring startBang,
        jstring stopBang
) {
    auto *kortholt = reinterpret_cast<Kortholt *>(kortholtHandle);
    if (kortholt != nullptr) {
        const char *name = env->GetStringUTFChars(fileName, nullptr);
        const char *start = env->GetStringUTFChars(startBang, nullptr);
        const char *stop = env->GetStringUTFChars(stopBang, nullptr);
        jint result = kortholt->saveWaveFile(name, duration, start, stop);
        env->ReleaseStringUTFChars(fileName, name);
        env->ReleaseStringUTFChars(startBang, start);
        env->ReleaseStringUTFChars(stopBang, stop);
        return result;
    }
    return 0;
}

} // extern "C"
