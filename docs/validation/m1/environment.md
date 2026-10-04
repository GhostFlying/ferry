# M1 implementation environment

Status: implementation baseline; container build and unit/static checks PASS,
device/service evidence pending.

The implementation uses the Ferry devcontainer and never uses the host JDK,
Android SDK, Go, Gradle or dependency caches for build or test results. The
initial local recovery image is `ferry-m0-devcontainer:recovery-test`; its
verified image ID and tool versions are recorded in the build manifest before
an APK is reported. The upstream Dockerfile remains digest-pinned for CI; the
local daemon cannot currently resolve the pinned Docker Hub manifest, so a
local recovery image is used only for this verification session. The source
worktree is mounted at `/workspace/ferry` and cache
volumes are named `ferry-gradle-cache` and `ferry-go-cache`.

Locked Android values for this M1 baseline are `minSdk 29`, `compileSdk 35`,
`targetSdk 35`, Java 17, Go 1.27.1, Gradle 8.10.2 and arm64-v8a. These values
must be confirmed by the container manifest produced by the first successful
build. A container image failure blocks build evidence; it does not justify a
host build.

The first complete container run used source SHA
`701ce68d450f64bbf29bc1d5a573d9eab5f2fda8`, image ID
`sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`,
and produced an arm64-v8a debug APK with SHA-256
`149b87fafbc337c19b4849afba969d07f18b58e02786b2f4b66db4b59c7a7f7e`.
The run executed `scripts/ci/android.sh`; Go tests, `go vet`, gomobile AAR
generation, Gradle unit tests, dependency locks, verification metadata and
`:android:app:assembleDebug` all passed. The APK is a debug technical shell,
not product UI or device evidence. The exact run output remains a local
artifact; public records contain hashes and redacted toolchain fields only.
