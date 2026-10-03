FROM eclipse-temurin:17-jdk-jammy@sha256:43f4431cc895d37ceb115e2e1f160545ec45caee9c1cb4246fc6bb879d58aeda

ARG ANDROID_CMDLINE_TOOLS_VERSION=16111833
ARG ANDROID_CMDLINE_TOOLS_SHA256=0877a1d048fe4a24efe2eff536ca4223f7adeb58648bb81909d33c446918cfa8
ARG ANDROID_PLATFORM_VERSION=35
ARG ANDROID_BUILD_TOOLS_VERSION=35.0.0
ARG ANDROID_NDK_VERSION=27.2.12479018
ARG GO_VERSION=1.27.1
ARG GO_SHA256=63d339f0da5ab53635a56f2490a7984dfe12dfcff22ad749f63edaf590168445
ARG GRADLE_VERSION=8.10.2
ARG GRADLE_SHA256=31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_HOME=/opt/gradle \
    PATH=/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/gradle/bin:/usr/local/go/bin:/go/bin:${PATH}

RUN apt-get update \
    && apt-get install --no-install-recommends -y ca-certificates curl git unzip xz-utils bash coreutils make \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/android-sdk/cmdline-tools /opt/gradle \
    && curl -fsSL "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" -o /tmp/android-cmdline-tools.zip \
    && echo "${ANDROID_CMDLINE_TOOLS_SHA256}  /tmp/android-cmdline-tools.zip" | sha256sum -c - \
    && unzip -q /tmp/android-cmdline-tools.zip -d /opt/android-sdk/cmdline-tools \
    && mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest \
    && rm /tmp/android-cmdline-tools.zip

RUN curl -fsSL "https://go.dev/dl/go${GO_VERSION}.linux-amd64.tar.gz" -o /tmp/go.tar.gz \
    && echo "${GO_SHA256}  /tmp/go.tar.gz" | sha256sum -c - \
    && tar -C /usr/local -xzf /tmp/go.tar.gz \
    && rm /tmp/go.tar.gz

RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && echo "${GRADLE_SHA256}  /tmp/gradle.zip" | sha256sum -c - \
    && unzip -q /tmp/gradle.zip -d /opt \
    && mv "/opt/gradle-${GRADLE_VERSION}" /opt/gradle \
    && rm /tmp/gradle.zip

RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager "platform-tools" "platforms;android-${ANDROID_PLATFORM_VERSION}" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" "ndk;${ANDROID_NDK_VERSION}" \
    && yes | sdkmanager --licenses >/dev/null

WORKDIR /workspace/ferry

CMD ["bash"]
