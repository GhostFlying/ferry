# M0 A1 implementation review

Status: **PASS**

This is an independent, affected-only review of the A1 toolchain and
devcontainer implementation. It does not constitute a container, APK, bridge,
device, or M0 protocol result.

## Review identity and scope

- Reviewed cumulative A1 target: `3921c4959c9b50eab5b18dae1839dfcfb77bdb1f`
- Plan-reviewed baseline: `5810acc41a519fef0be9c6c3064ba4de78f94eef`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The cumulative A1 scope contains the pinned Dockerfile and devcontainer,
`.dockerignore`, the toolchain lock and smoke script, the M0 environment
evidence, and the corresponding A1/A2 ownership and execution-rule updates.
It contains no APK, Go bridge, Android application, device harness, SMB
fixture, or transfer implementation.

## Static verification

The following checks passed without invoking a container or host toolchain:

- `git diff --check 281790b 3921c49`
- ancestry and full-SHA verification for the reviewed commits
- `bash -n scripts/ci/m0-toolchain-smoke.sh`
- `python3 -m json.tool .devcontainer/devcontainer.json`
- local Markdown-link check over the affected execution, M0, environment, and
  experiments documents: four files checked, no missing targets
- lock/manifest consistency check: Dockerfile ARG values match
  `experiments/toolchains.env`; platform-tools, compile/target SDK, apt-lock,
  and source-SHA fields are emitted by the smoke script

## Review findings

No P0, P1, or P2 finding remains.

The Dockerfile pins the digest-qualified JDK base image, Android command-line
tools, platform-tools archive and SHA-256, SDK platform, build-tools, NDK, Go,
Gradle, and explicit apt package versions. Download checks run before unpacking
the command-line tools, platform-tools, Go, and Gradle archives. The smoke
manifest reports the toolchain versions, platform-tools checksum, compile/target
SDK, minimum SDK, ABI, apt dependency lock, source SHA, and artifact hash
placeholder.

The devcontainer keeps the workspace bind mount and rebuildable Gradle/Go named
cache volumes. The host boundary permits starting the container, explicit
connection forwarding, and redacted artifact retrieval only. The smoke script
refuses execution unless `FERRY_DEVCONTAINER=1` is set and requires a full
`FERRY_SOURCE_SHA`; linked worktrees are documented to pass the host-resolved
source identity instead of following host-only Git metadata.

The plan now assigns gomobile/gobind and Gradle-wrapper pins to A2's bridge
package, so A1 does not claim those bridge-specific tools. A1 remains limited to
the pinned base/toolchain and does not introduce APK, bridge, device, SMB, tsnet,
or transfer claims. The environment record accurately preserves the observed
Docker build blocker and does not convert it into a PASS.

## Docker build evidence and untested boundaries

The implementation evidence records this exact attempted command as `BLOCKED`:

```text
docker build --pull=false --tag ferry-m0-devcontainer:20261004 --file \
  .devcontainer/Dockerfile .
```

The Docker daemon timed out resolving the digest-qualified Docker Hub manifest
at `registry-1.docker.io`. Importing the same base image with `skopeo` did not
remove the daemon's registry-resolution requirement. No host toolchain, mutable
tag, substitute container, host-built APK, or false runtime PASS was used.

This review did not retry the container build, run the smoke script, install or
build an APK/AAR, execute Go/Gradle/Android checks, connect to Dora or ADB,
occupy a lease, access SMB or tsnet, or perform USB/device/transfer validation.
Those remain blocked or untested until the documented daemon registry issue is
resolved and the plan's runtime evidence is produced.
