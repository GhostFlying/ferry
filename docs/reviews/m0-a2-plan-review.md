# M0 A2 implementation plan review

Status: **PASS**

This is an independent, affected-only review of the A2 Android bridge plan.
It reviews the plan and its execution-plan updates; it does not establish any
build, AAR, APK, network, or device result.

## Review identity and scope

- Reviewed target: `ad427c8d71b230ba38cd8014a37d40da50508b86`
- Plan-reviewed A1 baseline: `3921c4959c9b50eab5b18dae1839dfcfb77bdb1f`
- A1 implementation review: `c5f6103ae63d9c2042225390c6c3f5dcdeddc262`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The affected commit adds `docs/plans/m0-a2-implementation.md` and updates the
A2 rows in the execution and M0 plans. It does not add bridge source, an
Android project, a fake backend implementation, an AAR, an APK, or runtime
evidence.

## Static verification

The following checks passed without starting a container or using a host
language/Android toolchain:

- `git diff --check ad427c8^ ad427c8`
- full target SHA and ancestry verification against `3921c49`
- local Markdown-link check over the three affected planning documents: 13
  links checked, no missing targets
- `rg` consistency inspection of A1/A2/A3/A4 ownership, dependencies,
  container boundary, fake-backend wording, M0 V01–V05 scope, and stop rules

No P0, P1, or P2 finding remains.

## Review findings and acceptance mapping

- The plan is limited to the user-approved M0 Android protocol. A2 owns a
  platform-free Go contract, operation manager, deterministic fake, Android
  wrapper, and one AAR entrypoint; it explicitly does not claim SMB, tsnet,
  APK, Dora, Pocket USB, or complete-transfer evidence.
- A2 depends on the reviewed A1 lock and fixed devcontainer. C1 and a fresh
  Dora lease are correctly absent from the A2 dependency because they are
  needed only for later experiments. A3 owns real SMB and A4 owns App-internal
  tsnet, with A5 consuming the bridge; the execution plan keeps these package
  dependencies acyclic and serial in the registered worktree.
- Responsibility for exact `golang.org/x/mobile`/`gomobile`/`gobind` versions
  and Gradle-wrapper distribution/checksum is assigned to A2. The wrapper and
  Go module are explicitly owned files, floating `latest` is forbidden, and
  manifest output must record the selected versions. A1 remains responsible
  for the base container and general SDK/JDK/NDK/Go/Gradle/platform-tools lock.
- The operation contract covers versioned IDs, 64-bit counters, structured
  errors, callback delivery, idempotent cancellation, terminal-state
  isolation, and distinct cancellation/termination states. Acceptance vectors
  and event traces make these claims testable without treating the fake as a
  network backend.
- A2-01 through A2-03 require fixed-container evidence and explicitly become
  `BLOCKED` when the runtime is unavailable; host-toolchain results cannot
  pass. A2-04 keeps SMB, tsnet, and device evidence `NOT_RUN` until their
  owning packages and gates run. A public contract change after integration
  requires a plan update and another independent review.
- File ownership, `minSdk 29`, inherited compile/target lock, source SHA,
  artifact SHA-256, ABI, toolchain manifest, and validation-record outputs are
  stated. The inherited execution plan continues to require secret redaction,
  host/container separation, no-replace and content-hash boundaries, and the
  V01 artifact-hash rule without adding a deterministic-build gate.

## Untested boundaries

This review did not run Docker, Go, gomobile/gobind, Gradle, Android SDK
checks, or any static/build command inside the devcontainer. It did not create
or inspect an AAR/APK, run the fake or bridge tests, connect to ADB/Dora,
occupy a lease, access SMB or tsnet, or perform USB/transfer validation. The
previously documented Docker daemon registry-resolution timeout remains a
runtime blocker; this PASS is plan-only and does not convert that blocker into
an implementation or M0 gate result.
