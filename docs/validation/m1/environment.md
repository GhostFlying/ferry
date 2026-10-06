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

The latest complete container run used source SHA
`128f5553f93b81b8e4893a3a2834decbf8333b65`, image ID
`sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`,
and produced an arm64-v8a debug APK with SHA-256
`83b6e317c68f6b6a81fdaf162d01d26b818463f6a7c11588654c9085365103af`.
The run executed `scripts/ci/android.sh`; Go tests, `go vet`, gomobile AAR
generation, Gradle unit tests, strict dependency verification and
`:android:app:assembleDebug` all passed. The committed lock and verification
metadata (including module metadata checksums) were consumed read-only and
copied into the provenance bundle. The APK is a debug technical shell, not
product UI or device evidence. The exact run output remains a local artifact;
public records contain hashes and redacted toolchain fields only.

GitHub Actions run
`https://github.com/GhostFlying/ferry/actions/runs/37179125508` passed from
source SHA `22e6ebc4c3927e23899a6e5add3a6e57ee3aa83c`. Its container build,
strict verification and uploaded provenance are the CI evidence; the local
recovery image result above is kept separate because it uses a different image
tag and cache environment.
