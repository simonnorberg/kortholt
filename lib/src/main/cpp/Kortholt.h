#ifndef KORTHOLT_H
#define KORTHOLT_H

#include <oboe/Oboe.h>
#include <PdBase.hpp>
#include <IRestartable.h>
#include <DefaultErrorCallback.h>
#include <LatencyTuningCallback.h>
#include <WaveFileWriter.h>
#include "PureDataSource.h"

class Kortholt : public IRestartable {

public:
    Kortholt(std::vector<int> cpuIds, bool stream);

    virtual ~Kortholt();

    virtual void restart() override;

    int32_t saveWaveFile(
            const char *fileName,
            const int32_t duration,
            const char *startBang,
            const char *stopBang
    );

private:
    bool isStream;
    std::mutex streamLock;
    std::shared_ptr<oboe::AudioStream> stream;
    std::shared_ptr<PureDataSource> pureDataSource;
    std::shared_ptr<LatencyTuningCallback> dataCallback;
    std::shared_ptr<DefaultErrorCallback> errorCallback;
    int32_t ticksPerBuffer;
    int32_t bufferSize;

    oboe::Result createPlaybackStream();

    void start();

    void stop();

    static int32_t calculateTicksPerBuffer();
};

#endif //KORTHOLT_H
