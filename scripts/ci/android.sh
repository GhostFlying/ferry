#!/usr/bin/env bash
set -euo pipefail

: "${FERRY_DEVCONTAINER:?run Android checks in the Ferry devcontainer}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

go test ./...
go vet ./...
./scripts/build-android-bridge.sh
gradle --no-daemon --stacktrace :android:app:assembleDebug

mkdir -p dist
cp android/app/build/outputs/apk/debug/app-debug.apk dist/ferry-m1-debug.apk
sha256sum dist/ferry-m1-debug.apk > dist/ferry-m1-debug.apk.sha256
go version > dist/toolchain.txt
gradle --version >> dist/toolchain.txt
SOURCE_SHA="${FERRY_SOURCE_SHA:-unknown}"
printf 'compileSdk=35\ntargetSdk=35\nminSdk=29\nabi=arm64-v8a\nsourceSha=%s\n' "$SOURCE_SHA" >> dist/toolchain.txt
