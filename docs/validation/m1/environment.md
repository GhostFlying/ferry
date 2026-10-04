# M1 implementation environment

Status: implementation baseline, runtime evidence pending.

The implementation uses the Ferry devcontainer and never uses the host JDK,
Android SDK, Go, Gradle or dependency caches for build or test results. The
initial local image is `ferry-m0-devcontainer:recovery-test`; its image ID and
tool versions are recorded in the build manifest before the first APK is
reported. The source worktree is mounted at `/workspace/ferry` and cache
volumes are named `ferry-gradle-cache` and `ferry-go-cache`.

Locked Android values for this M1 baseline are `minSdk 29`, `compileSdk 35`,
`targetSdk 35`, Java 17, Go 1.27.1, Gradle 8.10.2 and arm64-v8a. These values
must be confirmed by the container manifest produced by the first successful
build. A container image failure blocks build evidence; it does not justify a
host build.
