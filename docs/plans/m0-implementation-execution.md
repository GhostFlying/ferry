# M0 Android implementation execution plan

Status: **M0 user approval is recorded; implementation is still waiting for
independent plan review**.  This plan is based on merged main
`5a4ca005bb9d1c15f5c87b915678af979ef8867d` and the scope and toolchain constraints
in [M0 plan](m0.md), [implementation plan](../implementation-plan.md), and
[Android toolchain constraint](android-toolchain-constraint.md).  A review PASS,
CI result, or merge does not constitute the user's M0 approval.

This file defines the execution boundary for the M0 Android probe and controlled
SMB experiment.  The current task revises planning/status documents only; it does not implement
application code, build an APK, start a container, connect to a device, access
SMB, or occupy a Dora lease.

## Approval record

- Decision text: “批准当前最终规划并启动 M0 Android 受控协议实现。”
- Session reference: 本轮用户消息/response annotation 1
- Recorded: 2026-10-03, Asia/Singapore
- Approved scope: M0 Android controlled protocol gates V01–V05 only.
- Explicitly not approved: M1, M0-UI, and iOS work; each retains its own user
  gate.

The approval authorizes starting the M0 execution process, not bypassing the
independent plan review, the pinned devcontainer requirement, or any V01–V05
evidence and stop condition.

## Objective and ownership

M0 must produce a real Android probe APK containing the Go bridge, prove the
controlled host-injected `net.Conn` SMB path, and prove one complete App-internal
tsnet transfer to the controlled SMB service on a fresh Dora Android physical
device.  The remote content must be read back and compared by SHA-256.  Cancel
and process termination must not report completion without evidence, and a
completed local copy must remain available for reconciliation.

The execution owner is `/root`, which directly owns plan and implementation work
in this session.  `/root/m0_plan_reviewer_astra` is the independent review
agent; it does not implement packages.  Each package has one implementation owner and
one reviewer.  The plan author and package author cannot be the independent
plan, integrated, or device reviewer.  The coordinator records the reviewer
identity, model/reasoning, commit SHA, findings, and recheck before calling a
gate passed.

This approval/DAG revision owns only the current-status edits in
`AGENTS.md`, `README.md`, `docs/implementation-plan.md`, `docs/agent-workflow.md`,
`docs/plans/m0.md`, `docs/plans/scope-trim.md`,
`docs/plans/android-toolchain-constraint.md`,
`docs/plans/repository-bootstrap.md`, and this execution plan.  Later
implementation commits must keep the file ownership below and must not mix
unrelated UI, iOS, release, or GitHub changes into an M0 package.

## Dependencies and entry conditions

The M0 implementation DAG has only these universal entry conditions:

1. The merged base is verified as `5a4ca005bb9d1c15f5c87b915678af979ef8867d`;
   the assigned branch/worktree is recorded before the package edits.
2. The user has explicitly approved M0 and its current V01–V05 scope as recorded
   above.  Internal review, an issue, a PR, or a successful CI run cannot
   substitute for this decision.
3. A1 has an assigned owner and may write the pinned container definition without
   a local runtime.  Every build, test, and SDK-installation verification waits
   until the definition is fully pinned and a usable runtime is available, then
   runs only inside the container.  The Dockerfile must pin the `FROM` base-image digest and the
   SDK/JDK/NDK/Go/Gradle/dependency versions, or an equivalent auditable manifest
   must do so.  An unpinned Dockerfile is not a locked toolchain.  Dependency
   caches must be rebuildable volumes.  If the runtime or lock is unavailable,
   build-related gates are `BLOCKED`; there is no host-toolchain fallback.

The independent plan review remains a required process gate before implementation,
but C1 service readiness and a Dora lease are not universal implementation
dependencies.  M0-C1's authorized SMB fixture, service account, route, and
cleanup procedure are required when running V03/V04/V05 and C2.  M0-C2's fresh
Android physical lease is required only for the device experiment (V02/V04/V05
and C2).  A2/A3/A4/A5/A6 may implement and verify their pure container or
controlled-service contracts without those external resources; a missing fixture
or lease blocks only the affected experiment gate.  A3 may use a container-local
controlled SMB fixture/contract until C1 is available.  If the M0 probe UI is
implemented, its concept acceptance and independent UI plan review remain
separate from V01–V05.

## File ownership and package graph

The following ownership is exclusive.  The coordinator resolves any overlap
before an agent edits a file.

| Package | Owner-controlled files | Work and output | Dependencies |
| --- | --- | --- | --- |
| A1 toolchain/delivery | `experiments/README.md`, `experiments/toolchains.*`, devcontainer files, `docs/validation/m0/environment.md` | Write and pin the container definition and rebuildable cache volumes; emit the toolchain manifest and evidence-record format before any build/test/SDK-installation verification. A1 owns JDK, SDK/platforms, direct pinned platform-tools archive, NDK, Go, Gradle, ABI, and OS dependencies. A2 owns exact gomobile/gobind and Gradle-wrapper pins in the bridge package; A1 does not install or claim those bridge-specific tools. | Assigned owner; runtime is required only for subsequent container verification |
| A2 Go/Android bridge | `experiments/mobile-core/`, `docs/validation/m0/bridge.md` | Pin gomobile/gobind and Gradle wrapper; build the single Go AAR entrypoint around an injectable transfer backend; exercise operation IDs, 64-bit counts, structured errors, callback-thread and cancellation behavior with a controlled fake backend. A3 supplies real SMB and A4 supplies App-internal tsnet later. | A1 |
| A3 controlled SMB | `experiments/smb/`, `docs/validation/m0/smb.md` | Use an injected `net.Conn`; enforce exclusive creation, flush, readback hash, and server no-replace behavior | A1; container-local fixture/contract is sufficient before C1 |
| M0-C1 fixture/network | `experiments/test-smb/`, `docs/validation/m0/network-environment.md` | Prepare the isolated service, account, route, fixture, and cleanup evidence | Universal entry conditions plus service-owner availability; does not block pure implementation |
| A4 App-internal tsnet | `experiments/tsnet/`, `docs/validation/m0/tsnet.md` | Dial the controlled SMB service through the same Go `Server.Dial` boundary and report connection versus content separately | A2, A3; C1 is required only to run V04 |
| A5 Android probe | `experiments/android-source/`, `docs/validation/m0/android-probe.md` | Read the test source, create a reproducible input, retain the local copy, call the bridge, show progress, and expose cancel/termination state | A2, A4; C1 and a Dora lease are not implementation prerequisites; UI subtask has its own design gate |
| A6 CI delivery | `.github/workflows/m0-probe.yml`, `scripts/ci/m0-*` | Invoke the locked devcontainer, run real tests/static checks, build real arm64 debug APK/AAR, and publish redacted manifests/hashes | A2, A4, A5; C1 and a Dora lease are not required for the container workflow |
| M0-C2 device harness | `experiments/device-harness/`, `docs/validation/m0/dora-android.md` | Use the fresh lease to install/call the container-built APK, run V02/V04/V05, and clean up in `finally` | A6, C1, fresh lease; A3/A4/A5 are transitive dependencies |
| R0 integrated/device review | `docs/validation/m0/report.md` and review records | Recheck final SHA, paths, hashes, gate results, cleanup, and stop conditions | A1–A6, C1/C2 |

To keep one owner and one worktree auditable, `/root` executes A1, A2, A3, A4,
A5, and A6 sequentially in the registered worktree; no parallel implementation
tasks share that worktree.  C1 service preparation is an external prerequisite
only for its later experiment and does not block the sequential container work.
A4 starts after A2 and A3, A5 after A2 and A4, and A6 after the real bridge and
probe exist.  C2 starts only after A6, C1, and a freshly verified physical
lease, and is serialised to one device operator and one session-local lease. R0
is always performed by an agent who did not author the reviewed implementation.

Before implementation, the coordinator must populate this assignment record;
an unassigned row is a stop condition and cannot be treated as permission to
edit:

| Package | Implementation owner | Independent reviewer | Model/reasoning | Branch/worktree | Task-register/issue reference |
| --- | --- | --- | --- | --- | --- |
| Execution plan | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | This file; [task register](../implementation-plan.md) |
| M0-A1 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #3 |
| M0-A2 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #4 |
| M0-A3 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #5 |
| M0-A4 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #6 |
| M0-A5 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #7 |
| M0-A6 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #8 |
| M0-C1 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #22 |
| M0-C2 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | `feat/m0-android-protocol`; `../ferry-worktrees/m0-implementation-20261003` | [Task register](../implementation-plan.md), issue #23 |
| M0-R0 | `/root` | `/root/m0_plan_reviewer_astra` | Root: inherited session model, no override; reviewer: `gpt-6-astra/high` | Review worktree fixed to reviewed SHA | [Task register](../implementation-plan.md), issue #13 |

All package owner, reviewer, model/reasoning, branch/worktree, and
task-register/issue fields are now populated above.  A later change to those
assignments requires a coordinator update and affected review before editing.

## Container and external action boundary

All Go, Android bridge/AAR, APK, unit test, integration test, static check,
manifest generation, and SHA-256 calculation for build outputs run inside the
locked devcontainer.  The container also runs the APK installation command when
the device is reached through an explicitly forwarded connection.  The host may
only start the container, provide the explicit device connection or forwarding,
and retrieve redacted artifacts.  The host does not run installation, build,
unit-test, integration-test, or static-check commands; a host ADB result is not
build or application evidence.

The controlled SMB service, its account and network are external service
actions.  Dora lease creation, connection forwarding, device state checks, and
physical-device interaction are external device actions, while the installation
command itself is issued from the devcontainer through that forwarded connection.
The App's tsnet connection, transfer, cancellation, restart reconciliation, and
readback are device/App actions.  Reports must label these evidence sources
separately.  Missing container runtime, an unverified session ID, or an
unavailable fixture stops the corresponding gate rather than triggering a
fallback to host tools or an emulator.

The manifest and every APK/AAR artifact record, as separate fields, `minSdk` (29),
`compileSdk`, `targetSdk`, JDK, SDK platform and build-tools versions, Gradle,
NDK, Go, dependency versions and lock-file identifiers, ABI, complete source
SHA, container image or manifest identity, and artifact SHA-256.  The actual
compile/target API numbers are selected and locked inside the container before
implementation; this plan does not guess a date-dependent latest API.

## V01–V05 implementation and acceptance matrix

Each gate is recorded with the final source SHA, container/toolchain identity,
ABI, artifact SHA-256, exact command, environment, input fixture SHA-256, and
redacted logs.  A gate is `PASS`, `FAIL`, `BLOCKED`, or `NOT_RUN`; an absent
result is never inferred from another gate.

| Gate | Implementation and container action | External action | Required artifact/evidence | PASS condition and stop rule |
| --- | --- | --- | --- | --- |
| **M0-V01 build and probe** | A1/A2/A5/A6 build the real Go AAR and arm64 APK in the locked container; run unit/integration tests and static checks there; make CI fail on a failing check | Host only starts the container and retrieves redacted outputs | APK/AAR, manifest, test/static reports, source/toolchain/ABI/artifact hashes, version and debug type | A real APK contains the bridge, checks pass, and artifact SHA-256 is computed, recorded, and rechecked inside the fixed devcontainer. This is artifact-hash evidence; it does not introduce a byte-for-byte deterministic-build gate. Missing runtime, unpinned toolchain, host-built output, or failed check is `BLOCKED`/`FAIL`; do not continue with host results |
| **M0-V02 bridge on Dora** | Container invokes the installation and bridge calls through the explicit forwarded connection; parse structured progress, 64-bit counts, and errors | Dora control plane provides a fresh physical lease; verify Android version/API/ABI/network; host only starts the container and provides the explicit connection/forwarding, while lease records remain external control-plane evidence | Session ID/serial/endpoint, install/start/call logs, device properties, APK hash, structured bridge result | The container-built APK installs, starts, and returns the expected bridge result on the leased physical device. No matching session, incompatible ABI, or host-issued install is `BLOCKED`; stop device actions |
| **M0-V03 controlled SMB** | Implement/test the client protocol using a container-local controlled fixture/contract; run the gate through the injected `net.Conn` to C1, uploading/flushing/reading back the known input and rejecting a pre-existing target | Authorized C1 service and cleanup; service owner retains no-replace evidence | Service/version, fixture and input hashes, byte counts, readback hash, no-replace response, redacted errors | Full readback SHA-256 equals input and an existing target cannot be replaced. Missing C1 blocks this experiment only; container-local implementation/testing may continue. Connection-only, size-only, rename-only, or an uncontrolled service does not pass |
| **M0-V04 App-internal tsnet transfer** | Container-built bridge and probe are used; parse and record App result without substituting a host network path | On the leased Dora device the App logs in to the authorized test Tailnet and dials C1 via App-internal tsnet | Redacted route/login state, device identity, service path, bytes, input/remote SHA-256, APK/source/toolchain hashes | At least one complete file is transferred and remote readback SHA-256 matches. TCP connect without full content, direct host SMB, emulator, or unreachable service is `BLOCKED` |
| **M0-V05 cancel/termination recovery** | Generate the reproducible input in the container/test harness and parse restart/reconciliation output; preserve local completed copy | During Dora transfer cancel or terminate the App, then restart it and inspect state; clean up afterward | Cancel/termination timing, local-copy and remote-state hashes, restart state, no-false-complete result, cleanup log | Cancellation/termination never reports completion without remote evidence; completed local copy remains and restart can distinguish pending/complete. Lost local copy, false completion, or unsafe resubmit is `FAIL` and stops the gate |

M0-V06 (>4 GiB), V07 (competition), and V08 (extended identity/persistence or
provider lifecycle) are optional or conditional evidence only.  M0-V09 cleanup is
mandatory safety work but is not a protocol gate.  M0-UI is a separate design and
native screenshot result.  Pocket 3, Pixel 6 Pro, real fnOS full chain, M1/M2
features, iOS, macOS, signing, and release publication are explicitly outside
this execution plan.

## Evidence, secrets, and device lease safety

The evidence bundle contains no SMB password, Tailnet auth key, node state,
private key, signing material, or private source path.  Logs and screenshots use
redacted references and placeholders.  Short-lived secrets enter the container
or device harness through protected secret references and are revoked during
cleanup.  The input fixture is synthetic or otherwise authorized; record its
byte count and SHA-256, and record only a redacted remote path.

The device operator follows the session-isolation procedure from `AGENTS.md`:

1. Run `bytedcli --json auth status`; query only explicit Android physical
   candidates that are both idle and online.
2. Occupy one candidate for this session.  Immediately verify it with both
   `device get` and the occupied-device list, and save serial, Dora session ID,
   and connection endpoint in session-local state.
3. Before connecting, installing, calling ADB/BDC, renewing, or releasing,
   query `device get` again and require the same session ID.  A missing or
   changed ID stops all device operations; never adopt an old lease.
4. Confirm Android version/API, ABI, network, package installation, and launch.
   Use `adb -s <serial>` when an ADB operation is required, but issue the
   installation command from inside the devcontainer through the explicit
   forwarding path.
5. Stop any renewal process, revoke test nodes/accounts, delete only this
   session's test data and temporary state, release only the matching session,
   and verify that the lease disappeared from the occupied list.  Use a shell
   `trap` or equivalent `finally` on every exit path.

Cleanup failure makes the device run `BLOCKED` and stops further device actions;
it does not permit a stale lease to be released or a protocol result to be
rewritten.  The lease and service cleanup evidence is retained with the redacted
report.

## CI workflow scope

`.github/workflows/m0-probe.yml` and `scripts/ci/m0-*` are limited to the M0
probe.  They must invoke the pinned devcontainer, execute real Go/bridge/Android
tests and static checks, build a real installable arm64 debug APK plus its Go
AAR, and publish only redacted APK/AAR, manifests, SHA-256 files, and reports.
The workflow records source SHA, run identifier, version/versionCode, debug
status, ABI, min/compile/target SDK, JDK/SDK/Gradle/NDK/Go/dependency versions,
and container identity.  It must fail on failed checks or missing artifact
hashes; empty jobs, fake APKs, AAR-only claims, host-built artifacts, or skipped
checks are not valid evidence.  No M1/M2/iOS workflow or release signing gate is
introduced here.

## Stop conditions and explicit non-goals

Stop the affected implementation package and report `BLOCKED` when the
devcontainer runtime or rebuildable cache is missing; a missing pin/manifest
blocks build/test/SDK-installation verification while A1 may write the definition.
Stop only
the affected experiment gate when the C1 fixture, valid fresh lease, session
match, compatible ABI, service reachability, no-replace proof, readback hash, or
cancel reconciliation is missing; those external resources do not block pure
container or controlled-service implementation.  Do not run a host build, use
a host APK, install from host ADB, substitute an emulator for the
physical-device gate, weaken the no-replace rule, or claim a connection as a
complete transfer.  A secret or private-data exposure stops publication and
triggers cleanup before any result is reported.

M0 implementation deliberately does not include V06–V08 as hard requirements,
M0-UI, Pocket/Pixel/fnOS full chain, production task management, M1 foreground
recovery, M2 automatic mode/release preparation, or any iOS/macOS work.  These
remain separately planned and require their own user gate.  This boundary does
not add a compatibility matrix for Android 9 and below, a low-probability stress
gate, or an additional publication gate.

## Review and commit sequence

The execution sequence is:

1. Review this plan independently; resolve findings and record a review SHA.
2. Confirm the approval record and complete the independent plan review while
   preserving the current scope and gate IDs.
3. Populate each package's owner/reviewer/model/branch/worktree/task-register
   fields, then implement A1 and pass the container/toolchain review.
4. Implement A2, A3, C1, A4, and the non-UI portion of A5 in dependency order;
   keep each package in an atomic conventional commit with one file owner.
5. Integrate A6 only after the real bridge and probe exist; run the complete
   container-only build/test/static evidence path.
6. Run C2 with a fresh physical lease and `finally` cleanup; produce V02/V04/V05
   evidence and keep V03's host-fixture result separate.
7. Have R0 perform affected-only integrated/device review, recheck final source,
   toolchain, ABI, APK/AAR, fixture, and artifact hashes, and publish the M0
   report with PASS/FAIL/BLOCKED/NOT_RUN for every row.
8. Return the plan, implementation commits, evidence, unresolved limits, and
   next-stage proposal to the user.  Do not start M1 or any excluded chain until
   the user gives a separate explicit approval.

For this approval/DAG revision the permitted commit contains only the six
planning/status documents listed above, using the repository's GhostFlying
identity, a Conventional Commit message, and no co-author.  The
required checks are `git diff --check`, Markdown link-target existence checks
for the existing documents referenced above, and a final status check proving
that no other file changed.  No runtime, build, device, network, GitHub, or
application-code check is part of this commit.
