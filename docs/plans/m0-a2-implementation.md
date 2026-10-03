# M0-A2 Android bridge implementation plan

Status: `proposed`; this plan is written before implementation and awaits an
independent review. It is limited to the user-approved M0 Android protocol
scope and does not start M1, M0 UI, iOS, Pocket USB, or device operations.

## Goal and boundaries

A2 will provide one Go bridge package that can be built as an Android AAR and
called by the probe. The package owns the stable operation contract and
lifecycle semantics; its transfer backend is injected so A2 can be tested with
a deterministic fake while A3 and A4 add the real SMB and App-internal tsnet
backends. A2 does not claim SMB, tsnet, APK, Dora, or complete-transfer
evidence by itself.

The bridge contract contains a versioned operation ID, 64-bit byte counters,
structured terminal errors, progress callback delivery on a documented
non-worker callback path, idempotent cancellation, and late-event isolation
after terminal completion. A completed operation is reported only after the
backend returns success; cancellation and process termination remain distinct
states for later A5 reconciliation.

## Files and ownership

- `experiments/mobile-core/go.mod`, `go.sum`, and Go source/tests: bridge
  contract, operation manager, fake backend, and unit tests.
- `experiments/mobile-core/gradle/` and `gradlew*`: pinned Gradle wrapper
  distribution used only for AAR packaging.
- `experiments/mobile-core/android/`: minimal Android library project with
  `minSdk 29`, compile/target values inherited from the A1 lock, and the
  generated Go AAR packaging entrypoint.
- `docs/validation/m0/bridge.md`: commands, contract vectors, evidence fields,
  and explicit blocked/untested boundaries.
- `docs/plans/m0-a2-implementation.md`: this plan and its review history.

No other package edits are allowed during A2. A3 owns the real SMB backend;
A4 owns the tsnet backend; A5 owns the probe UI and source adapter.

## Dependencies and environment

- A1 implementation target `3921c4959c9b50eab5b18dae1839dfcfb77bdb1f` and
  independent review `c5f6103ae63d9c2042225390c6c3f5dcdeddc262`.
- The fixed Ferry devcontainer, with `FERRY_DEVCONTAINER=1` and an explicit
  full `FERRY_SOURCE_SHA`; no host Go, Java, Gradle, Android SDK, or static
  checker is evidence.
- A2 must pin `golang.org/x/mobile` (including `gomobile`/`gobind`) in the Go
  module and pin the Gradle wrapper distribution and checksum. The exact
  versions are selected once in the implementation and emitted by the bridge
  manifest; floating `latest` is forbidden.
- Container runtime availability is required for build/test evidence. The
  current Docker daemon registry-resolution timeout remains a stop condition;
  source edits may proceed, but no A2 build or test may be called PASS until
  the fixed container runs.

## Implementation steps

1. Add the Go module and bridge contract types with explicit operation and
   terminal-state enums; keep platform-free Go code independent of Android
   `Context`, URI, service, or lifecycle objects.
2. Add the fake backend and operation manager. Test success, progress with
   64-bit counts, structured backend errors, cancellation before and during
   transfer, exactly-once terminal callback, and ignored late progress/events.
3. Add the Android library wrapper and a small Java/Kotlin-facing API surface
   that carries only strings, integers/longs, and callback interfaces suitable
   for gomobile. Do not add UI or source-provider behavior.
4. Pin gomobile/gobind and Gradle wrapper versions, generate the AAR in the
   container, and record toolchain/source/AAR SHA-256 fields.
5. Write the validation record with exact commands, expected vectors, and
   `BLOCKED` result if the container cannot run.

## Acceptance and evidence

| ID | PASS condition | Evidence |
| --- | --- | --- |
| A2-01 | Go contract tests pass in the fixed devcontainer; operation IDs, versions, 64-bit counts, structured errors, and terminal states match vectors | full source SHA, container manifest, `go test` output, test vector summary |
| A2-02 | Cancellation is idempotent; no completion is emitted after cancel/termination; late backend events do not mutate a terminal operation | race/test output and callback event trace |
| A2-03 | The Android bridge compiles to one arm64-compatible AAR in the fixed devcontainer with pinned gomobile/gobind and Gradle wrapper | AAR path, SHA-256, ABI, wrapper/module lock, toolchain manifest |
| A2-04 | Validation record separates bridge contract PASS from SMB/tsnet/device evidence, which remain `NOT_RUN` until A3/A4/A5/C2 | `docs/validation/m0/bridge.md` and commit SHA |

If the container cannot be built or entered, A2-01 through A2-03 are
`BLOCKED`, not host-toolchain passes. A fake backend never satisfies the M0
SMB or tsnet gates. Any need to change the public bridge contract after A3/A4
integration stops A2 and requires a plan update plus independent review.
