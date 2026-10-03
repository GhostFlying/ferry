#!/usr/bin/env bash
set -euo pipefail

if [[ "${FERRY_DEVCONTAINER:-}" != "1" ]]; then
  echo 'FERRY_DEVCONTAINER=1 is required; refusing host-toolchain execution' >&2
  exit 2
fi

source experiments/toolchains.env
source_sha="${FERRY_SOURCE_SHA:-}"
if [[ -z "$source_sha" ]]; then
  source_sha="$(git rev-parse HEAD)"
fi
if [[ ! "$source_sha" =~ ^[0-9a-f]{40}$ ]]; then
  echo 'FERRY_SOURCE_SHA must be a full Git commit SHA' >&2
  exit 2
fi

printf '%s\n' 'FERRY_TOOLCHAIN_MANIFEST'
printf 'source_sha=%s\n' "$source_sha"
printf 'base_image=%s\n' "$BASE_IMAGE"
printf 'base_image_index_sha256=%s\n' "$BASE_IMAGE_INDEX_SHA256"
printf 'base_image_manifest_sha256=%s\n' "$BASE_IMAGE_MANIFEST_SHA256"
printf 'base_image_config_sha256=%s\n' "$BASE_IMAGE_CONFIG_SHA256"
printf 'jdk=%s\n' "$JDK_VERSION"
printf 'android_cmdline_tools=%s\n' "$ANDROID_CMDLINE_TOOLS_VERSION"
printf 'android_platform_tools=%s\n' "$ANDROID_PLATFORM_TOOLS_VERSION"
printf 'android_platform_tools_sha256=%s\n' "$ANDROID_PLATFORM_TOOLS_SHA256"
printf 'android_sdk_platform=android-%s\n' "$ANDROID_PLATFORM_VERSION"
printf 'android_build_tools=%s\n' "$ANDROID_BUILD_TOOLS_VERSION"
printf 'android_ndk=%s\n' "$ANDROID_NDK_VERSION"
printf 'compile_sdk=%s\n' "$COMPILE_SDK"
printf 'target_sdk=%s\n' "$TARGET_SDK"
printf 'go=%s\n' "$GO_VERSION"
printf 'gradle=%s\n' "$GRADLE_VERSION"
printf 'min_sdk=%s\n' "$MIN_SDK"
printf 'abi=%s\n' "$ABI"
printf 'apt_dependencies=%s\n' "$APT_DEPENDENCIES"
printf 'artifact_sha256=NOT_APPLICABLE\n'
printf 'java_version='; java -version 2>&1 | head -1
printf 'go_version='; go version
printf 'gradle_version='; gradle --version | awk '/Gradle / { print $2; exit }'
printf 'sdkmanager_version='; sdkmanager --version
printf '%s\n' 'FERRY_TOOLCHAIN_MANIFEST_END'
