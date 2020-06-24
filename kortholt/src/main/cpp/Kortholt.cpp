#include <cinttypes>
#include <memory>
#include <cmath>
#include "Kortholt.h"

const int STATE_CHANGE_TIMEOUT = 400;
const int DEFAULT_TICKS_PER_BUFFER = 8;

Kortholt::Kortholt(std::vector<int> cpuIds) {
    this->cpuIds = std::move(cpuIds);
    this->ticksPerBuffer = calculateTicksPerBuffer();
    oboe::AudioStreamBuilder builder = oboe::AudioStreamBuilder();
    createPlaybackStream(&builder);
}

Kortholt::~Kortholt() {
    playStream->stop(STATE_CHANGE_TIMEOUT);
}

void Kortholt::createPlaybackStream(oboe::AudioStreamBuilder *builder) {
    int bufferSize = ticksPerBuffer * pd::PdBase::blockSize();

    oboe::Result result = builder
            ->setChannelCount(oboe::ChannelCount::Stereo)
            ->setDirection(oboe::Direction::Output)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setFormatConversionAllowed(true)
            ->setChannelConversionAllowed(true)
            ->setFramesPerCallback(bufferSize)
            ->setCallback(this)
            ->openManagedStream(playStream);

    if (result == oboe::Result::OK && playStream.get() != nullptr) {
        playStream->setBufferSizeInFrames(bufferSize);

        if (playStream->getFormat() == oboe::AudioFormat::I16) {
            // Create a buffer of floats which we can render our audio data into
            int conversionBufferSize =
                    playStream->getBufferCapacityInFrames() * playStream->getChannelCount();
            conversionBuffer = std::make_unique<float[]>(static_cast<size_t>(conversionBufferSize));
        }

        pdBase = std::make_unique<pd::PdBase>();
        if (pdBase->init(0, playStream->getChannelCount(), playStream->getSampleRate(), false)) {
            pdBase->computeAudio(true);
        }

        latencyTuner = std::make_unique<oboe::LatencyTuner>(*playStream);

        playStream->start(STATE_CHANGE_TIMEOUT);
    }
}

oboe::DataCallbackResult
Kortholt::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    if (!isThreadAffinitySet) {
        setThreadAffinity();
        isThreadAffinitySet = true;
    }

    latencyTuner->tune();

    bool is16BitFormat = (audioStream->getFormat() == oboe::AudioFormat::I16);
    int32_t channelCount = audioStream->getChannelCount();

    // If the stream is 16-bit render into a float buffer then convert that buffer to 16-bit ints
    float *outputBuffer = (is16BitFormat) ? conversionBuffer.get()
                                          : static_cast<float *>(audioData);

    pdBase->processFloat(ticksPerBuffer, new float[0], outputBuffer);

    if (is16BitFormat) {
        oboe::convertFloatToPcm16(outputBuffer, static_cast<int16_t *>(audioData),
                                  numFrames * channelCount);
    }

    return oboe::DataCallbackResult::Continue;
}

void Kortholt::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) {
    if (error == oboe::Result::ErrorDisconnected) {
        oboe::AudioStreamBuilder builder = oboe::AudioStreamBuilder(*oboeStream);
        createPlaybackStream(&builder);
    }
}

int32_t Kortholt::calculateTicksPerBuffer() {
    // Calculate buffer size. A multiple of PdBase::blockSize (64) works best.
    auto blockSize = pd::PdBase::blockSize();
    auto framesPerBurst = oboe::DefaultStreamValues::FramesPerBurst;
    float bufferSize = framesPerBurst > blockSize
                       ? framesPerBurst
                       : blockSize * DEFAULT_TICKS_PER_BUFFER;
    return static_cast<int32_t>(ceil(bufferSize / blockSize)) * 2;
}

void Kortholt::setThreadAffinity() {
    pid_t current_thread_id = gettid();
    cpu_set_t cpu_set;
    CPU_ZERO(&cpu_set);

    if (cpuIds.empty()) {
        int current_cpu_id = sched_getcpu();
        CPU_SET(current_cpu_id, &cpu_set);
    } else {
        for (int cpu_id : cpuIds) {
            CPU_SET(cpu_id, &cpu_set);
        }
    }

    sched_setaffinity(current_thread_id, sizeof(cpu_set_t), &cpu_set);
}
