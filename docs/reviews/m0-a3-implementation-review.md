# M0 A3 implementation review

Status: **PASS**

This is an independent, affected-only review of the cumulative A3 controlled
SMB implementation. It records static source and packaging checks only; it is
not an SMB, C1, tsnet, device, or transfer result.

## Review identity and scope

- Implementation target: `f3c830811197857f3bfebba2d2556f173bbc1102`
- Final plan/docs boundary target: `c887980a8f96deea138e3f869fab3baa319200d5`
- Plan reviewed before implementation: `8e25dd36c50231346d21a77af8e73a75481ad5f3`
- Plan review: `2cbbc75`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The implementation adds the standalone `experiments/smb` module, the
go-smb2-backed client and upload/readback logic, unit helpers/tests, and the
validation record. It does not add C1 credentials, a live fixture, A2
composition, tsnet, Dora, an APK, or transfer evidence.

## Static verification

The following checks passed without running host Go tests/builds, a container,
or any SMB/device operation:

- `git diff --check 3953e15 c887980`
- full SHA and ancestry verification
- `make -n -C experiments/smb test`, showing the container guard before
  `go test ./...`
- actual guard refusal with `FERRY_DEVCONTAINER` unset (exit 2); no host Go
  command was reached
- local Markdown-link check over the A3 plan, validation, execution, M0, and
  A2 plan documents: 13 links checked, no missing targets
- module/lock inspection confirming `go-smb2 v1.1.0` and its recorded sums

No P0, P1, or P2 finding remains.

## Review findings and acceptance mapping

- `NewClient` accepts an already-established `net.Conn`; successful client
  construction transfers ownership, and `Client.Close` unmounts the share,
  logs off the session, and closes the connection. It never opens a second
  socket or uses a host mount/system VPN. The plan and validation record state
  this ownership explicitly.
- The client uses `go-smb2 v1.1.0`, `OpenFile` with `O_CREATE|O_EXCL` for an
  operation-owned temporary object, streams through a SHA-256 hash and byte
  counter, calls `Sync`, closes the temp file, and uses `Share.Rename` with the
  library's server-side `ReplaceIfExists=0` request. A final-name collision is
  therefore an error rather than a client-side existence-check race.
- Readback opens the final object through the same context-bound share and
  compares byte count and SHA-256 before returning a successful `Result`.
  Cancellation/context errors are classified separately, and the per-call
  context is applied to share operations rather than only the local copy loop.
- Temporary cleanup is attempted only while the operation still owns the temp
  name. Deferred cleanup errors are surfaced as `StageCleanup` (including the
  original failure), while a successful rename clears ownership so the final
  object is never removed. Readback close errors are also surfaced.
- Typed stages cover injected-connection/share-mount, combined SMB
  negotiation/authentication (the go-smb2 `DialContext` boundary), create,
  write, flush, rename, readback, cancellation, and cleanup. The plan and
  validation record deliberately do not infer a finer transport/auth split
  from library error text.
- The standalone module records the pinned go-smb2 dependency and transitive
  sums. Its Makefile refuses host execution and the validation command runs it
  only inside the fixed devcontainer. The validation record states that C1
  server identity, credentials, no-replace collision behavior, readback, and
  cleanup remain separate future evidence.

## Untested boundaries

The fixed Docker devcontainer remains blocked by the documented daemon
registry-resolution timeout. This review did not run `go test`, static checks
inside the container, SMB negotiation, authentication, share mounting, upload,
readback, no-replace collision, cleanup failure injection, C1 fixture access,
A2 composition, tsnet, Dora/ADB, USB, APK, or transfer validation. No C1 or
M0-V03 PASS is claimed; those results remain `BLOCKED`/`NOT_RUN` until the
documented runtime and fixture conditions are satisfied.
