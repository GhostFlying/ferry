# M1 implementation checkpoint — 2026-10-04

Status: **foundation implementation PASS; device/service gates pending.**

## Approved scope

The user approved M1 after independent plan review PASS. The implementation
branch is based on merged M0 and M1 planning commits. The current code does
not claim M1 Pocket／Pixel／fnOS or SMB runtime completion.

## Implemented

- P1 contract v1: configuration validation, safe relative paths, rule fields,
  operation phases and revision semantics.
- P2 Android Kotlin／Compose technical launch shell, Go bridge generation,
  `minSdk 29`, `compileSdk／targetSdk 35`, arm64-v8a and pinned container
  manifest inputs.
- P3 deterministic rules: exclusion priority, first matching include, segment
  boundary and rule fixtures.
- P4 Room operation state, revision-guarded pause updates and Android Keystore
  encrypted credential values.
- M1-D1 SAF read-only recursive import, streaming SHA-256, atomic partial-to-
  complete private copies, path containment, free-space checks and partial
  reconciliation.
- M1-T1 storage no-replace contract and SMB client package adapted from the
  accepted M0 transport implementation; temporary write, flush, remote
  readback, SHA-256 and cancellation boundaries have unit coverage.
- P6 dependency locking, Gradle verification metadata and Actions container
  build workflow.

## Container evidence

The following command ran in the Ferry devcontainer image, not on the host
toolchain:

```text
./scripts/ci/android.sh
```

Source SHA: `701ce68d450f64bbf29bc1d5a573d9eab5f2fda8`.

Container image ID: `sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`.

Toolchain: JDK 17.0.20.1, Go 1.27.1, Gradle 8.10.2, compile／target SDK 35,
build-tools 35.0.0, minSdk 29, arm64-v8a.

Results: Go unit tests PASS, `go vet ./...` PASS, gomobile AAR generation
PASS, Android unit tests PASS, `:android:app:assembleDebug` PASS. APK SHA-256:
`149b87fafbc337c19b4849afba969d07f18b58e02786b2f4b66db4b59c7a7f7e`.

The APK is a technical launch shell pending accepted M1 UI concepts. It is not
native fidelity evidence.

## Not run or blocked

- No Dora lease, ADB installation or Pixel 6 Pro run.
- No Pocket 3 USB／OTG source authorization or real素材 import.
- No fnOS SMB credentials, server session, remote readback or no-replace
  end-to-end run.
- No M1 native UI fidelity comparison; the concept manifest remains
  `awaiting-user-acceptance`.
- Docker daemon could not resolve the pinned upstream base manifest in this
  environment; the local recovery image was used for the container evidence.

These boundaries are evidence limits, not claims that the omitted gates pass.
The next implementation step is to complete the executor／accepted UI surface
and then run the specified device/service gates when the user supplies the
environment.
