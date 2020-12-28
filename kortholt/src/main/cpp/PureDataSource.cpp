#include "PureDataSource.h"

PureDataSource::PureDataSource(int32_t ticksPerBuffer) {
    this->ticksPerBuffer = ticksPerBuffer;
    pdBase = std::make_unique<pd::PdBase>();
}

void PureDataSource::init(int32_t sampleRate, int32_t channelCount) {
    if (pdBase->init(0, channelCount, sampleRate, false)) {
        pdBase->computeAudio(true);
    }
}

void PureDataSource::renderAudio(float *audioData, int32_t numFrames) {
    auto *outputBuffer = static_cast<float *>(audioData);
    pdBase->processFloat(ticksPerBuffer, new float[0], outputBuffer);
}
