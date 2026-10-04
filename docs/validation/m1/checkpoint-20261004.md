# M1 implementation checkpoint — 2026-10-04

Status: **pre-device foundation implementation review PASS; accepted-surface UI plan under review; device/service gates pending.**

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
  readback, SHA-256, exclusive final create and cancellation boundaries have
  generic unit coverage. The live fnOS SMB adapter behavior and crash-time
  final partial recovery remain unverified; complete kill recovery is deferred
  to M2.
- P6 dependency locking, Gradle verification metadata and Actions container
  build workflow.
- P5 pre-device foreground coordinator: one worker, revision／phase CAS claim and
  completion, manual pause persistence, cancellation wait, late-result isolation and
  repeated `onOpen` protection. This is coordination with an injected action; it is
  not a claim that the production SAF／SMB action is wired end to end.
- The user accepted the shown M1 concepts A01 and A03–A10 on 2026-10-04. The
  accepted-surface UI implementation plan is recorded separately; no native UI
  implementation or fidelity result is claimed yet.

## Container evidence

The following command ran in the Ferry devcontainer image, not on the host
toolchain:

```text
./scripts/ci/android.sh
```

Source SHA: `a3d5779428405ad6c3da345416c395b8aea2b8db`.

Container image ID: `sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`.

Toolchain: JDK 17.0.20.1, Go 1.27.1, Gradle 8.10.2, compile／target SDK 35,
build-tools 35.0.0, minSdk 29, arm64-v8a.

Results: Go unit tests PASS, `go vet ./...` PASS, gomobile AAR generation
PASS, Android unit tests PASS, strict dependency verification PASS,
`:android:app:assembleDebug` PASS. APK SHA-256:
`b981402a4b88ab0ef8e86f873d61bd0cb446f9019f3dca36bf498f122aff49b6`.
The provenance bundle includes the committed `gradle.lockfile` and
`verification-metadata.xml`, including Gradle module metadata checksums; the
build does not write dependency trust data.

GitHub Actions run [37179125508](https://github.com/GhostFlying/ferry/actions/runs/37179125508)
also passed from source SHA `22e6ebc4c3927e23899a6e5add3a6e57ee3aa83c`, using
the pinned CI image and the same strict read-only verification workflow. The
local recovery APK above remains the artifact whose hash is recorded in this
checkpoint; the Actions artifact is separately provenance-bound to its run.

The APK is still a technical launch shell pending native implementation of the
accepted M1 surface. It is not native fidelity evidence.

## Not run or blocked

- No Dora lease, ADB installation or Pixel 6 Pro run.
- No Pocket 3 USB／OTG source authorization or real素材 import.
- No fnOS SMB credentials, server session, remote readback or no-replace
  end-to-end run.
- No live SMB adapter fixture run; generic storage tests do not prove fnOS
  server behavior.
- No M1 native UI fidelity comparison; accepted concept coverage is recorded, but
  the UI implementation plan is still awaiting independent review.
- Docker daemon could not resolve the pinned upstream base manifest in this
  environment; the local recovery image was used for the container evidence.

These boundaries are evidence limits, not claims that the omitted gates pass.
The next implementation step is to complete the independently reviewed accepted
UI surface. The specified device/service gates run when the user supplies the
environment.
