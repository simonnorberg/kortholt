#ifndef KORTHOLT_H
#define KORTHOLT_H

#include <oboe/Oboe.h>
#include <PdBase.hpp>
#include <IRestartable.h>
#include <DefaultErrorCallback.h>
#include <LatencyTuningCallback.h>
#include "PureDataSource.h"

class Kortholt : public IRestartable {

public:
    Kortholt(std::vector<int> cpuIds);

    virtual ~Kortholt();

    virtual void restart() override;

private:
    std::mutex streamLock;
    std::shared_ptr<oboe::AudioStream> stream;
    std::shared_ptr<PureDataSource> pureDataSource;
    std::unique_ptr<LatencyTuningCallback> dataCallback;
    std::unique_ptr<DefaultErrorCallback> errorCallback;
    int32_t ticksPerBuffer;
    int32_t bufferSize;

    oboe::Result createPlaybackStream();

    void start();

    void stop();

    static int32_t calculateTicksPerBuffer();
};

#endif //KORTHOLT_H
