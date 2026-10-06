#!/usr/bin/env bash
set -euo pipefail

: "${FERRY_DEVCONTAINER:?run Android checks in the Ferry devcontainer}"
: "${FERRY_SOURCE_SHA:?pass full source SHA from the host into the devcontainer}"
: "${FERRY_CONTAINER_IMAGE_ID:?pass verified container image ID into the devcontainer}"
[[ "$FERRY_SOURCE_SHA" =~ ^[0-9a-f]{40}$ ]] || { echo "FERRY_SOURCE_SHA must be a 40-hex commit" >&2; exit 2; }
[[ "$FERRY_CONTAINER_IMAGE_ID" =~ ^sha256:[0-9a-f]{64}$ ]] || { echo "FERRY_CONTAINER_IMAGE_ID must be an image digest" >&2; exit 2; }
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

# GitHub PR jobs check out the PR head explicitly. When Git metadata is
# available, fail closed if the mounted source does not match its provenance.
# The mounted checkout is owned by the host user, so trust it explicitly;
# otherwise Git refuses it and the check would be skipped silently.
git -C / config --global --add safe.directory "$ROOT_DIR"
if actual_source_sha="$(git rev-parse HEAD 2>/dev/null)"; then
  [[ "$actual_source_sha" == "$FERRY_SOURCE_SHA" ]] || {
    echo "source SHA mismatch: actual=$actual_source_sha declared=$FERRY_SOURCE_SHA" >&2
    exit 2
  }
elif [[ "${FERRY_REQUIRE_SOURCE_GIT:-}" == "1" ]]; then
  echo "cannot read the mounted source SHA" >&2
  exit 2
fi

go test ./...
go vet ./...
./scripts/build-android-bridge.sh
# CI consumes the committed dependency locks and verification metadata. A
# build must not generate new trust data in the checked-out source tree.
gradle --no-daemon --stacktrace --dependency-verification strict :android:app:testDebugUnitTest :android:app:assembleDebug

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
