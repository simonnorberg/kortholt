# Kortholt [![Android CI](https://github.com/simonnorberg/kortholt/workflows/Android%20CI/badge.svg)](https://github.com/simonnorberg/kortholt/actions) [![Maven Central](https://img.shields.io/maven-central/v/net.simno.kortholt/kortholt)](https://search.maven.org/artifact/net.simno.kortholt/kortholt)

Pure Data for Android with [libpd](https://github.com/libpd/libpd) and [Oboe](https://github.com/google/oboe).

| Features     |     |
|--------------|-----|
| Audio output | ✅   |
| Audio input  | ❌   |
| MIDI         | ❌   |

## Build

    git clone https://github.com/simonnorberg/kortholt.git
    cd kortholt
    git submodule update --init --recursive
    ./gradlew build

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

See [Sample](https://github.com/simonnorberg/kortholt/tree/main/sample) for detailed usage.

## Download

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation "net.simno.kortholt:kortholt:1.12.0"
}
```

## License

    Copyright 2019 Simon Norberg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
