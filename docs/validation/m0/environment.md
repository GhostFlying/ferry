# M0 container environment

Status: A1 implementation in progress. This document records the pinned
container boundary; it is not APK, bridge, device, SMB, tsnet, or transfer
evidence.

Current verification result: `BLOCKED`. On 2026-10-04, the exact command
`docker build --pull=false --tag ferry-m0-devcontainer:20261004 --file
.devcontainer/Dockerfile .` could not resolve the pinned Docker Hub manifest
because the Docker daemon timed out reaching `registry-1.docker.io`. The same
base image was retrieved through `skopeo` and imported into the daemon, but the
digest-qualified `FROM` still required registry resolution. No host toolchain,
unpinned tag, host-built APK, or substitute container was used; the build must
be retried once daemon registry access is available.

The source of truth is [`.devcontainer/Dockerfile`](../../.devcontainer/Dockerfile)
and [`experiments/toolchains.env`](../../experiments/toolchains.env). The base
image is pinned by digest. Android command-line tools, platform 35,
build-tools 35.0.0, NDK 27.2.12479018, Go 1.27.1, and Gradle 8.10.2 are pinned
with download SHA-256 values where applicable. Android `minSdk` is 29 and the
initial artifact ABI is `arm64-v8a`.

The devcontainer uses rebuildable named volumes for Gradle and Go caches. The
container smoke command is
[`scripts/ci/m0-toolchain-smoke.sh`](../../scripts/ci/m0-toolchain-smoke.sh).
It refuses to run unless `FERRY_DEVCONTAINER=1` is set and emits a redacted
toolchain manifest. A source SHA and `artifact_sha256=NOT_APPLICABLE` are
expected until a real APK/AAR exists; this is not a build pass.

Verification status will be recorded as `PASS`, `FAIL`, `BLOCKED`, or
`NOT_RUN`, with the container image identity, source SHA, toolchain fields, and
artifact SHA-256. No host JDK, SDK, NDK, Go, Gradle, or host-built artifact can
be used as evidence.
