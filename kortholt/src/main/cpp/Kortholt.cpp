#include <cinttypes>
#include <memory>
#include <cmath>
#include "Kortholt.h"

const int DEFAULT_TICKS_PER_BUFFER = 8;

Kortholt::Kortholt(std::vector<int> cpuIds) {
    ticksPerBuffer = calculateTicksPerBuffer();
    bufferSize = ticksPerBuffer * pd::PdBase::blockSize();
    pureDataSource = std::make_shared<PureDataSource>(ticksPerBuffer);
    errorCallback = std::make_unique<DefaultErrorCallback>(*this);
    dataCallback = std::make_unique<LatencyTuningCallback>();
    dataCallback->setCpuIds(std::move(cpuIds));
    dataCallback->setThreadAffinityEnabled(true);
    start();
}

Kortholt::~Kortholt() {
    stop();
}

void Kortholt::restart() {
    stop();
    start();
}

oboe::Result Kortholt::createPlaybackStream() {
    oboe::AudioStreamBuilder builder;
    return builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setChannelCount(oboe::ChannelCount::Stereo)
            ->setDirection(oboe::Direction::Output)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setFormatConversionAllowed(true)
            ->setChannelConversionAllowed(true)
            ->setFramesPerDataCallback(bufferSize)
            ->setDataCallback(dataCallback.get())
            ->setErrorCallback(errorCallback.get())
            ->openStream(stream);
}

void Kortholt::start() {
    std::lock_guard<std::mutex> lock(streamLock);
    auto result = createPlaybackStream();
    if (result == oboe::Result::OK) {
        pureDataSource->init(stream->getSampleRate(), stream->getChannelCount());
        dataCallback->reset();
        dataCallback->setSource(pureDataSource);
        stream->setBufferSizeInFrames(bufferSize);
        stream->start();
    } else {
        LOGE("Error creating stream. Error: %s", oboe::convertToText(result));
    }
}

void Kortholt::stop() {
    std::lock_guard<std::mutex> lock(streamLock);
    if (stream && stream->getState() != oboe::StreamState::Closed) {
        stream->stop();
        stream->close();
    }
    stream.reset();
}

int32_t Kortholt::calculateTicksPerBuffer() {
    // Calculate buffer size. A multiple of PdBase::blockSize (64) works best.
    auto blockSize = pd::PdBase::blockSize();
    auto framesPerBurst = oboe::DefaultStreamValues::FramesPerBurst;
    float bufferSize = framesPerBurst > blockSize
                       ? framesPerBurst
                       : blockSize * DEFAULT_TICKS_PER_BUFFER;
    int32_t ticksPerBuffer = static_cast<int32_t>(ceil(bufferSize / blockSize)) * 2;
    LOGD("Calculated ticksPerBuffer: %d", ticksPerBuffer);
    return ticksPerBuffer;
}
