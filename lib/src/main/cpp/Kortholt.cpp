#include <cinttypes>
#include <memory>
#include <cmath>
#include <fstream>
#include "Kortholt.h"

const int32_t DEFAULT_TICKS_FOR_STREAM = 8;
const int32_t DEFAULT_TICKS = 16;

class WaveOutputStream : public WaveFileOutputStream {
public:
    void write(uint8_t b) override {
        mData.push_back(b);
    }

    int32_t length() {
        return (int32_t) mData.size();
    }

    uint8_t *getData() {
        return mData.data();
    }

private:
    std::vector<uint8_t> mData;
};

Kortholt::Kortholt(std::vector<int> cpuIds, bool stream) {
    isStream = stream;
    ticksPerBuffer = stream ? calculateTicksPerBuffer() : DEFAULT_TICKS;
    bufferSize = ticksPerBuffer * pd::PdBase::blockSize();
    pureDataSource = std::make_shared<PureDataSource>(ticksPerBuffer);
    errorCallback = std::make_shared<DefaultErrorCallback>(*this);
    dataCallback = std::make_shared<LatencyTuningCallback>();
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

int32_t Kortholt::saveWaveFile(
        const char *fileName,
        const int32_t duration,
        const char *startBang,
        const char *stopBang
) {
    const int32_t sampleRate = stream->getSampleRate();
    const int32_t channelCount = stream->getChannelCount();
    const int32_t totalFrames = static_cast<int32_t>(ceil(sampleRate * (duration / 1000.0)));

    WaveOutputStream outStream;
    WaveFileWriter writer(&outStream);
    writer.setFrameRate(sampleRate);
    writer.setSamplesPerFrame(channelCount);
    writer.setBitsPerSample(24);

    int32_t framesPerBuffer = bufferSize * channelCount;
    auto *audioData = new float[framesPerBuffer];

    int32_t frameCounter = 0;
    pureDataSource->sendBang(startBang);
    while (frameCounter < totalFrames) {
        int32_t remaining = totalFrames - frameCounter;
        int32_t numFrames = (remaining > bufferSize) ? bufferSize : remaining;
        pureDataSource->renderAudio(audioData, framesPerBuffer);
        writer.write(audioData, 0, numFrames * channelCount);
        frameCounter += numFrames;
    }
    pureDataSource->sendBang(stopBang);
    writer.close();

    if (outStream.length() > 0) {
        auto file = std::ofstream(fileName, std::ios::out | std::ios::binary);
        file.write((char *) outStream.getData(), outStream.length());
        file.close();
    }

    return outStream.length();
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
        if (isStream) {
            dataCallback->reset();
            dataCallback->setSource(pureDataSource);
            stream->setBufferSizeInFrames(bufferSize);
            stream->start();
        }
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
                       : blockSize * DEFAULT_TICKS_FOR_STREAM;
    int32_t ticksPerBuffer = static_cast<int32_t>(ceil(bufferSize / blockSize)) * 2;
    return ticksPerBuffer;
}
