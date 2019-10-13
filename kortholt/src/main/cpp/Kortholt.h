#ifndef KORTHOLT_H
#define KORTHOLT_H

#include <oboe/Oboe.h>
#include <PdBase.hpp>

class Kortholt : oboe::AudioStreamCallback {

public:
    Kortholt(std::vector<int> cpuIds);

    ~Kortholt();

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error);

private:
    oboe::ManagedStream playStream;
    std::unique_ptr<oboe::LatencyTuner> latencyTuner;
    std::unique_ptr<float[]> conversionBuffer{nullptr};
    std::unique_ptr<pd::PdBase> pdBase;
    int32_t ticksPerBuffer;
    std::vector<int> cpuIds;
    std::atomic<bool> isThreadAffinitySet{false};

    void createPlaybackStream(oboe::AudioStreamBuilder *builder);

    int32_t calculateTicksPerBuffer();

    void setThreadAffinity();
};

#endif //KORTHOLT_H
