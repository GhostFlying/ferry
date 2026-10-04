# M0 A2 implementation review

Status: **PASS**

This is an independent, affected-only review of the cumulative A2 bridge
implementation. It records static source and packaging checks only; it is not
a container, AAR, APK, network, or device result.

## Review identity and scope

- Reviewed implementation target: `94c2df793f247b79d3828fb47c60df35a90714b7`
- Revised plan target: `fa7c2e5d16d9d64b11e61c4aa130f2128a82af0b`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The cumulative implementation adds the platform-free Go bridge contract and
tests, the injectable internal test seam, Gradle wrapper/project metadata,
container-guarded Makefile targets, manifest generation, and the A2 validation
record. It does not add an APK, probe UI, SMB backend, tsnet backend, Dora
harness, or transfer evidence.

## Static verification

The following checks passed without invoking a host Go build/test or any
container/device operation:

- `git diff --check ad427c8 94c2df7`
- full target SHA and ancestry verification
- `bash -n experiments/mobile-core/manifest.sh experiments/mobile-core/gradlew`
- `make -n -C experiments/mobile-core test aar manifest`; all three targets
  require `FERRY_DEVCONTAINER=1`, and the AAR target invokes `./gradlew`
- Gradle wrapper archive integrity (`unzip -tq`) and metadata/hash inspection;
  the distribution URL/version/checksum are pinned
- local Markdown-link check over the affected plan, execution, M0, and bridge
  validation documents: no missing targets

No P0, P1, or P2 finding remains.

## Review findings and acceptance mapping

- `bridge.go` defines the versioned contract, operation IDs, 64-bit counters,
  structured errors, terminal states, and serialized per-operation callback
  dispatch. Cancellation now arbitrates under the operation mutex before the
  terminal state is set, so a cancel racing backend success cannot produce a
  completion after cancellation. Progress checks reject invalid values, and
  late events are ignored after cancellation or terminal delivery. The callback
  contract documents the dedicated dispatcher goroutine and leaves Android
  executor marshalling to the future adapter.
- The internal backend seam is deliberately package-private. `NewBridge()`
  uses the unavailable backend until the later A2-owned composition point
  wires A3/A4 implementations; this implementation does not falsely claim a
  cross-package constructor or real SMB/tsnet behavior.
- Makefile `test`, `aar`, and `manifest` targets all refuse host execution.
  The AAR path pins and installs the exact `golang.org/x/mobile` version for
  both gomobile and gobind, invokes the checked-in Gradle wrapper, creates the
  output directory, and generates the AAR with Android API 29. The manifest
  records separate gomobile/gobind versions, Gradle wrapper version/URL/SHA,
  source SHA, ABI, and AAR SHA-256.
- The Android project records `minSdk 29` and compile/target 35. The A2
  validation document keeps the fake backend separate from SMB, tsnet, APK,
  Dora, and transfer gates, and states that host artifacts are not evidence.
  The revised plan reference explicitly says the plan review is pending rather
  than treating the superseded plan PASS as covering the revised plan.

## Container blocker and untested boundaries

The fixed devcontainer remains unavailable because the documented Docker daemon
registry-resolution timeout has not been resolved. No A2 build/test/AAR PASS
is claimed. This review did not run `go test`, gomobile/gobind, Gradle/Android
builds, the manifest against an AAR, or any host toolchain equivalent. It did
not install an APK, connect to ADB/Dora, occupy or release a lease, access SMB
or tsnet, or perform USB, transfer, cancellation, or process-restart device
validation. Those runtime boundaries remain `BLOCKED`/`NOT_RUN` as documented.
