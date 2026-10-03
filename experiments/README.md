# M0 experiments

The M0 experiment code and evidence are built in the pinned Ferry devcontainer.
The host may start the container, forward an explicitly authorized device
connection, and retrieve redacted artifacts. It does not build, test, run
static checks, or install an APK with its own toolchain.

`toolchains.env` is the human-readable lock input. The container smoke command
must emit the resolved JDK, Android SDK platform/build-tools, NDK, Go, Gradle,
ABI, `minSdk`, source SHA, container identity, and artifact SHA-256 fields.
Missing container runtime or an unpinned toolchain is `BLOCKED`; no host
fallback is valid evidence.
