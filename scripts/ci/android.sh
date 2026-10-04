#!/usr/bin/env bash
set -euo pipefail

: "${FERRY_DEVCONTAINER:?run Android checks in the Ferry devcontainer}"
: "${FERRY_SOURCE_SHA:?pass full source SHA from the host into the devcontainer}"
: "${FERRY_CONTAINER_IMAGE_ID:?pass verified container image ID into the devcontainer}"
[[ "$FERRY_SOURCE_SHA" =~ ^[0-9a-f]{40}$ ]] || { echo "FERRY_SOURCE_SHA must be a 40-hex commit" >&2; exit 2; }
[[ "$FERRY_CONTAINER_IMAGE_ID" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "FERRY_CONTAINER_IMAGE_ID must be an image digest" >&2; exit 2; }
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

go test ./...
go vet ./...
./scripts/build-android-bridge.sh
gradle --no-daemon --stacktrace --write-verification-metadata sha256 --write-locks :android:app:assembleDebug

mkdir -p dist
cp android/app/build/outputs/apk/debug/app-debug.apk dist/ferry-m1-debug.apk
sha256sum dist/ferry-m1-debug.apk > dist/ferry-m1-debug.apk.sha256
go version > dist/toolchain.txt
gradle --version >> dist/toolchain.txt
SOURCE_SHA="${FERRY_SOURCE_SHA:-unknown}"
printf 'compileSdk=35\ntargetSdk=35\nminSdk=29\nabi=arm64-v8a\nsourceSha=%s\ncontainerImage=%s\ngomobileVersion=%s\n' "$SOURCE_SHA" "$FERRY_CONTAINER_IMAGE_ID" "v0.0.0-20260908204917-8b95e45f8d3e" >> dist/toolchain.txt
sdkmanager --list | sed -n '/Installed packages:/,/Available Packages:/p' > dist/android-sdk-installed.txt
cp gradle/verification-metadata.xml dist/gradle-verification-metadata.xml
cp android/app/gradle.lockfile dist/gradle.lockfile
