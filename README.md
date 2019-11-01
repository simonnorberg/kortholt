# Kortholt [![CircleCI](https://circleci.com/gh/simonnorberg/kortholt.svg?style=svg)](https://circleci.com/gh/simonnorberg/kortholt) [![Download](https://api.bintray.com/packages/simonnorberg/maven/kortholt/images/download.svg)](https://bintray.com/simonnorberg/maven/kortholt/_latestVersion)

Pure Data for Android with [libpd](https://github.com/libpd/libpd) and [Oboe](https://github.com/google/oboe).

## Usage

```kotlin
// Load a Pure Data patch to use with Kortholt
PdBaseHelper.openPatch(context, R.raw.test, "test.pd")

// Start the audio output stream
Kortholt.create(context)

// Use PdBase to send messages to libpd
PdBase.sendBang("play")

// Stop the audio output stream
Kortholt.destroy()

// Close the Pure Data patch
PdBaseHelper.closePatch()
```

See [Sample](https://github.com/simonnorberg/kortholt/tree/master/sample) for detailed usage.

## Download

```groovy
dependencies {
    implementation 'net.simno.kortholt:kortholt:1.1.2'
}
```
