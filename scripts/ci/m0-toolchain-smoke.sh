#!/usr/bin/env bash
set -euo pipefail

if [[ "${FERRY_DEVCONTAINER:-}" != "1" ]]; then
  echo 'FERRY_DEVCONTAINER=1 is required; refusing host-toolchain execution' >&2
  exit 2
fi

source experiments/toolchains.env
source_sha="$(git rev-parse HEAD)"

printf '%s\n' 'FERRY_TOOLCHAIN_MANIFEST'
printf 'source_sha=%s\n' "$source_sha"
printf 'base_image=%s\n' "$BASE_IMAGE"
printf 'jdk=%s\n' "$JDK_VERSION"
printf 'android_cmdline_tools=%s\n' "$ANDROID_CMDLINE_TOOLS_VERSION"
printf 'android_sdk_platform=android-%s\n' "$ANDROID_PLATFORM_VERSION"
printf 'android_build_tools=%s\n' "$ANDROID_BUILD_TOOLS_VERSION"
printf 'android_ndk=%s\n' "$ANDROID_NDK_VERSION"
printf 'go=%s\n' "$GO_VERSION"
printf 'gradle=%s\n' "$GRADLE_VERSION"
printf 'min_sdk=%s\n' "$MIN_SDK"
printf 'abi=%s\n' "$ABI"
printf 'artifact_sha256=NOT_APPLICABLE\n'
printf 'java_version='; java -version 2>&1 | head -1
printf 'go_version='; go version
printf 'gradle_version='; gradle --version | awk '/Gradle / { print $2; exit }'
printf 'sdkmanager_version='; sdkmanager --version
printf '%s\n' 'FERRY_TOOLCHAIN_MANIFEST_END'
