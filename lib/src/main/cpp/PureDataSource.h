#ifndef PUREDATASOURCE_H
#define PUREDATASOURCE_H

#include <IRenderableAudio.h>
#include <PdBase.hpp>

class PureDataSource : public IRenderableAudio {
public:
    PureDataSource(int32_t ticksPerBuffer);

    ~PureDataSource() = default;

    void init(int32_t sampleRate, int32_t channelCount);

    void renderAudio(float *audioData, int32_t numFrames) override;

private:
    int32_t ticksPerBuffer;
    std::unique_ptr<pd::PdBase> pdBase;
};

#endif //PUREDATASOURCE_H
