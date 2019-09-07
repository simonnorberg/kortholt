#include <cinttypes>
#include <memory>
#include <cmath>

#include "Kortholt.h"
#include "oboe/src/common/OboeDebug.h"

const int STATE_CHANGE_TIMEOUT = 400;

Kortholt::Kortholt() {
    ticksPerBuffer = calculateTicksPerBuffer();
    oboe::AudioStreamBuilder builder = oboe::AudioStreamBuilder();
    createPlaybackStream(&builder);
}

Kortholt::~Kortholt() {
    playStream->stop(STATE_CHANGE_TIMEOUT);
}

void Kortholt::createPlaybackStream(oboe::AudioStreamBuilder *builder) {
    int bufferSize = ticksPerBuffer * pd::PdBase::blockSize();

    oboe::Result result = builder->setChannelCount(oboe::ChannelCount::Stereo)
            ->setDirection(oboe::Direction::Output)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
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

        // Initialize libpd
        pdBase = std::make_unique<pd::PdBase>();
        if (pdBase->init(0, playStream->getChannelCount(), playStream->getSampleRate(), false)) {
            pdBase->computeAudio(true);
        }

        // Create a latency tuner which will automatically tune our buffer size.
        latencyTuner = std::make_unique<oboe::LatencyTuner>(*playStream);

        // Start the stream - the dataCallback function will start being called
        result = playStream->start(STATE_CHANGE_TIMEOUT);
        if (result != oboe::Result::OK) {
            LOGE("Error starting stream. %s", oboe::convertToText(result));
        }
    } else {
        LOGE("Failed to create stream. %s", oboe::convertToText(result));
    }
}

oboe::DataCallbackResult
Kortholt::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
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

int Kortholt::calculateTicksPerBuffer() {
    // Calculate buffer size. A multiple of PdBase::blockSize (64) works best.
    int blockSize = pd::PdBase::blockSize();
    int framesPerBurst = oboe::DefaultStreamValues::FramesPerBurst;
    float bufferSize = framesPerBurst > blockSize ? framesPerBurst : blockSize * 8;
    int ticks = static_cast<int>(ceil(bufferSize / blockSize)) * 2;
    return ticks;
}
