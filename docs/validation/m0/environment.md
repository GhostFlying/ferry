# M0 container environment

Status: A1 toolchain verification `PASS` for the immutable local recovery
image; the default upstream-registry build remains `BLOCKED` in this network
environment. This document records only the pinned container boundary, not an
APK, device, SMB, tsnet, or transfer pass.

On 2026-10-04, the default command
`docker build --pull=false --tag ferry-m0-devcontainer:20261004 --file
.devcontainer/Dockerfile .` could not resolve the pinned Docker Hub manifest
because the Docker daemon timed out reaching `registry-1.docker.io`. The
verified upstream image was separately inspected and imported, and the local
build then succeeded with this immutable config digest override:

```sh
docker build --pull=false \
  --build-arg FERRY_BASE_IMAGE=sha256:b2a5b9c58fedbd66afc3b58fc99d7526673b6f45fbb1b25f2cda00933a293af3 \
  --tag ferry-m0-devcontainer:recovery-test \
  --file .devcontainer/Dockerfile .
```

This is the same verified Linux/amd64 base image chain recorded below, not an
unpinned tag or a substitute image. CI retains the upstream digest-qualified
`FROM`; the local override is a documented recovery path only.

The source of truth is [`.devcontainer/Dockerfile`](../../../.devcontainer/Dockerfile)
and [`experiments/toolchains.env`](../../../experiments/toolchains.env). The base
image is pinned by digest. The upstream image index is
`sha256:43f4431cc895d37ceb115e2e1f160545ec45caee9c1cb4246fc6bb879d58aeda`,
its Linux/amd64 manifest is
`sha256:8d242405506ad1085e39f1ca80ec76f0812f61073efe76129607e39f043ccbb9`,
and its config/image ID is
`sha256:b2a5b9c58fedbd66afc3b58fc99d7526673b6f45fbb1b25f2cda00933a293af3`.
Android command-line tools, platform-tools 37.0.1
(direct archive with SHA-256), platform 35, build-tools 35.0.0, NDK
27.2.12479018, compileSdk/targetSdk 35, Go 1.27.1, and Gradle 8.10.2 are pinned
with download SHA-256 values where applicable. Android `minSdk` is 29 and the
initial artifact ABI is `arm64-v8a`.

The devcontainer uses rebuildable named volumes for Gradle and Go caches. The
successful local recovery smoke command on 2026-10-04 used source SHA
`2415047d10224308e49b81178d004c672018187c` and reported JDK 17.0.20.1,
Android platform 35, build-tools 35.0.0, NDK 27.2.12479018, compile/target
SDK 35, Go 1.27.1, Gradle 8.10.2, SDK manager 1.0.16500706, minSdk 29 and
ABI arm64-v8a. The container smoke command is
[`scripts/ci/m0-toolchain-smoke.sh`](../../../scripts/ci/m0-toolchain-smoke.sh).
It refuses to run unless `FERRY_DEVCONTAINER=1` is set and emits a redacted
toolchain manifest. Its `artifact_sha256=NOT_APPLICABLE` field applies only to
the smoke check; A2 records the AAR hash separately.

Verification status is recorded as `PASS`, `FAIL`, `BLOCKED`, or
`NOT_RUN`, with the container image identity, source SHA, toolchain fields, and
artifact SHA-256. No host JDK, SDK, NDK, Go, Gradle, or host-built artifact can
be used as evidence. gomobile/gobind and the Gradle wrapper are intentionally
deferred to A2, where the bridge package must pin and report them before bridge
construction.

For a bound Git worktree, its `.git` file points at host-only metadata. Set
`FERRY_SOURCE_SHA="$(git rev-parse HEAD)"` before starting the devcontainer
(the devcontainer definition forwards it), or pass the same full SHA when
running the smoke script directly. The smoke script uses that explicit source
identity; normal checkouts may resolve their mounted Git metadata directly.
Reading Git metadata on the host only identifies the source and is not a
toolchain verification step.
