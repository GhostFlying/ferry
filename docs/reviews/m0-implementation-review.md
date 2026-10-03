# M0 A1–A4 implementation review

Status: **PASS**

This is an independent, affected-only static review of the cumulative M0 A1,
A2, A3, and A4 implementation. It does not claim a C1 SMB transfer, tsnet
login, APK installation, Dora result, USB result, or complete M0 V04/V05
evidence.

## Review identity and scope

- Reviewed target: `16760d2295e1d8a3ce7793922941a2138683f641`
- A1/A2/A3/A4 code baseline: `81785561d3c3f5cdb3f64f5905966fcd642357bc`
- A4 target-binding and lifecycle fixes: `afc7438`, `8178556`
- A4 plan review: `9ebd146`
- A2 implementation review baseline: `94c2df7` / `ba00ee2`
- A3 implementation review baseline: `f3c8308` / `3cdab7a`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The target is documentation-only after the cumulative implementation. It
aligns the final SMB evidence SHA with the source used for the latest
container run; the reviewed implementation includes the pinned A1 container,
A2 bridge/AAR packaging, A3 SMB backend, A4 tsnet client, tests, Makefiles,
module locks, and validation records.

## Static verification

The following checks passed:

- target SHA and ancestry verification
- `git diff --check main 16760d2` and affected-range diff checks
- file-by-file inspection of the A1 Dockerfile/devcontainer/toolchain lock and
  smoke script; A2 bridge, tests, Makefile, wrapper, Android metadata and
  manifest; A3 client, tests, module locks and validation; A4 client, tests,
  Makefile, module lock and validation
- consistency inspection of plan ownership, A1–A4 DAG, source SHA fields,
  image identity, AAR hash, host/container refusal, secret handling, and
  `BLOCKED`/`NOT_RUN` boundaries
- read-only inspection of the reported fixed-container commands and their
  recorded source SHA `81785561d3c3f5cdb3f64f5905966fcd642357bc`

No P0, P1, or P2 finding remains.

## Review findings and acceptance mapping

- A1 keeps the digest-qualified base image, explicit tool/archive checksums,
  pinned SDK/NDK/Go/Gradle/apt values, immutable local recovery image path,
  and a smoke script that refuses execution without both the container marker
  and an explicit full source SHA. The recovery command uses a local immutable
  image ID while the normal/CI path retains the upstream digest. The recorded
  Docker registry timeout remains `BLOCKED`, not a false default-build pass.
- A2's bridge preserves the versioned operation contract, 64-bit counters,
  structured errors, serialized callbacks, cancellation arbitration, and late
  event isolation. The Makefile checks the container marker before tool use,
  uses the fixed container Go bin path, initializes gomobile before bind, and
  invokes the checked-in Gradle wrapper. The manifest records the module tools,
  wrapper checksum, source SHA, ABI, and AAR SHA-256.
- A3 accepts only an injected `net.Conn`, takes ownership after successful
  SMB client construction, and closes share/session/connection in order. It
  uses exclusive operation-owned temporary creation, context-aware writes,
  `Sync`, server-side no-replace rename, final readback byte/hash comparison,
  and deferred file close before temporary cleanup. Cleanup errors remain typed
  and visible; existing final objects are not replaced or deleted.
- A4 pins `tailscale.com v1.104.0` with complete sums and uses an embedded
  `tsnet.Server` with an A4-owned temporary state directory. It filters login
  URL forwarding through a private callback, suppresses verbose logging, does
  not read ambient keys or configure exit/fallback/system-VPN paths, binds
  `DialSMB` to the configured exact endpoint, validates numeric ports, rejects
  nil contexts and dialing before `Start`, and avoids calling `Server.Close`
  before startup. Startup failures merge cleanup errors; close is idempotent
  and removes only the generated state directory.
- The final records bind environment, bridge, SMB, and tsnet deterministic
  evidence to the complete source SHA `81785561d3c3f5cdb3f64f5905966fcd642357bc`.
  The bridge records AAR SHA-256
  `2c2ba1422bed047dfc6b5e22f9501a4f55734cfd629d6ae339b668f642739115`.
  Those records explicitly separate container/unit evidence from C1, tsnet
  interactive login, SMB content, APK, Dora, and device gates.

## Untested boundaries

This review did not rerun Docker, `go test`, `go vet`, gomobile/gobind, Gradle,
the AAR build, tsnet login/dial, SMB negotiation, the C1 fixture, A2/A3/A4
runtime integration, APK installation, Dora/ADB, USB, cancellation on a real
device, or remote readback. The report accepts only the redacted container
records documented in the target; real C1 transfer, interactive tsnet login,
APK/device operation, and M0 V04/V05 remain `NOT_RUN` or `BLOCKED` until their
separate environment, credential, and fresh-lease conditions are satisfied.
