#include <cinttypes>
#include <memory>

#include "Kortholt.h"
#include "oboe/src/common/OboeDebug.h"

static const int32_t SAMPLE_RATE = 44100;
static const int TICKS_PER_CALLBACK = 2;

Kortholt::Kortholt() {
    oboe::AudioStreamBuilder builder = oboe::AudioStreamBuilder();
    createPlaybackStream(&builder);
}

Kortholt::~Kortholt() {
    pdBase->computeAudio(false);
    pdBase->clear();
}

void Kortholt::createPlaybackStream(oboe::AudioStreamBuilder *builder) {
    oboe::Result result = builder->setChannelCount(oboe::ChannelCount::Stereo)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setSampleRate(SAMPLE_RATE)
            ->setFramesPerCallback(TICKS_PER_CALLBACK * pd::PdBase::blockSize())
            ->setCallback(this)
            ->openManagedStream(playStream);

    if (result == oboe::Result::OK && playStream.get() != nullptr) {
        // Set the buffer size to the burst size - this will give us the minimum possible latency
        playStream->setBufferSizeInFrames(playStream->getFramesPerBurst());

        if (playStream->getFormat() == oboe::AudioFormat::I16) {
            // Create a buffer of floats which we can render our audio data into
            int conversionBufferSamples =
                    playStream->getBufferCapacityInFrames() * playStream->getChannelCount();
            LOGD("Stream format is 16-bit integers, creating a temporary buffer of %d samples"
                 " for float->int16 conversion", conversionBufferSamples);
            conversionBuffer = std::make_unique<float[]>(
                    static_cast<size_t>(conversionBufferSamples));
        }

        pdBase = std::make_unique<pd::PdBase>();
        if (pdBase->init(0, playStream->getChannelCount(), playStream->getSampleRate(), false)) {
            pdBase->computeAudio(true);
        }

        // Create a latency tuner which will automatically tune our buffer size.
        latencyTuner = std::make_unique<oboe::LatencyTuner>(*playStream);
        // Start the stream - the dataCallback function will start being called
        result = playStream->requestStart();
        if (result != oboe::Result::OK) {
            LOGE("Error starting stream. %s", oboe::convertToText(result));
        }
    } else {
        LOGE("Failed to create stream. Error: %s", oboe::convertToText(result));
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

    memset(outputBuffer, 0, static_cast<size_t>((int) numFrames * playStream->getBytesPerFrame()));
    pdBase->processFloat(numFrames / pd::PdBase::blockSize(), nullptr, outputBuffer);

    if (is16BitFormat) {
        oboe::convertFloatToPcm16(outputBuffer, static_cast<int16_t *>(audioData),
                                  numFrames * channelCount);
    }

    return oboe::DataCallbackResult::Continue;
}

void Kortholt::onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) {
    if (error == oboe::Result::ErrorDisconnected) {
        oboe::AudioStreamBuilder builder = oboe::AudioStreamBuilder(*oboeStream);
        restartStream(&builder);
    }
}

void Kortholt::restartStream(oboe::AudioStreamBuilder *builder) {
    LOGI("Restarting stream");
    createPlaybackStream(builder);
}
