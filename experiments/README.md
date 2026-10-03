# M0 experiments

The M0 experiment code and evidence are built in the pinned Ferry devcontainer.
The host may start the container, forward an explicitly authorized device
connection, and retrieve redacted artifacts. It does not build, test, run
static checks, or install an APK with its own toolchain.

For a linked Git worktree, start the container with an explicit source identity
so the container does not follow the host-only `.git` pointer:

```sh
FERRY_SOURCE_SHA="$(git rev-parse HEAD)" devcontainer up --workspace-folder .
```

The same full SHA must be passed as `FERRY_SOURCE_SHA` when invoking the smoke
script directly. A missing or non-full SHA is a stop condition.

`toolchains.env` is the human-readable lock input. The container smoke command
must emit the resolved JDK, Android SDK platform/platform-tools/build-tools, NDK,
Go, Gradle, ABI, `minSdk`, apt dependency lock, source SHA, container identity,
and artifact SHA-256 fields. A2 adds the pinned gomobile/gobind and Gradle
wrapper fields when the bridge package is introduced.
Missing container runtime or an unpinned toolchain is `BLOCKED`; no host
fallback is valid evidence.
