# M0 A3 implementation plan review (revision 2)

Status: **PASS**

This is an independent, affected-only review of the revised A3 controlled SMB
plan. It does not establish any implementation, container, SMB, tsnet, Dora,
device, or transfer result.

## Review identity and scope

- Reviewed target: `8e25dd36c50231346d21a77af8e73a75481ad5f3`
- Previous plan target: `3145611bd14ac04e3d5cf1e4d57689ae57c490ee`
- A2 plan/implementation baselines: `fa7c2e5d16d9d64b11e61c4aa130f2128a82af0b`,
  `94c2df793f247b79d3828fb47c60df35a90714b7`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The revision changes only `docs/plans/m0-a3-implementation.md`. It adds the
module-integration contract, makes the server-side no-replace mechanism
explicit, and removes the unrelated Dora lease blocker.

## Static verification

The following checks passed:

- `git diff --check 3145611 8e25dd3`
- full target SHA and ancestry verification
- local Markdown-link checks over the A3 plan and referenced M0/A2 plans: 13
  links checked, no missing targets
- consistency inspection of A3 ownership, A1/A2 dependencies, C1/Dora/host
  boundaries, M0 V03/V04 scope, cleanup language, and stop conditions

No P0, P1, or P2 finding remains.

## Review findings and acceptance mapping

- The plan remains Android-platform-independent and limited to the controlled
  SMB backend. It receives an already-created `net.Conn`, does not open a
  second socket, use host mounts, or substitute a system VPN, and does not
  start A4, A5, M0-C1 credential operations, Dora, M1, or iOS work.
- `go-smb2 v1.1.0` and all transitive sums are explicitly pinned. The revised
  plan identifies `Share.Rename`'s `FileRenameInformation` with
  `ReplaceIfExists=0`, so the no-replace decision is made atomically by the
  SMB server and a destination collision is an error. The upload sequence
  still requires exclusive temporary creation, `Sync`, close, final
  readback-size/hash comparison, and deletion of only the operation-owned
  temporary object on failure or cancellation.
- A3 remains a standalone module under `experiments/smb`. After A3/A4 are
  available, the A2-owned composition updates `mobile-core/go.mod` with local
  `require`/`replace` entries and adapts the A3/A4 exported backend types to
  the package-private bridge seam. A3 does not edit A2 files, so module
  integration ownership and the sequential DAG are explicit without a shared
  worktree conflict.
- Error stages distinguish connection/authentication, remote create, write or
  flush, no-replace rename, readback, and cancellation. The acceptance matrix
  requires local/remote byte counts and SHA-256 only after readback succeeds,
  and requires cleanup evidence showing no deletion or replacement of an
  existing final object.
- C1 is required only for the real controlled SMB/V03 run. Missing container
  runtime, module locks, or C1 blocks the corresponding A3 runtime result and
  cannot be converted into a host, mock-only, same-size, or device result. A
  fresh Dora lease is correctly excluded from A3 and reserved for C2/V04.

## Untested boundaries

This is a documentation-only review. It did not run the devcontainer, Go
tests, static checks, SMB client, a controlled fixture, A2 composition, tsnet,
Dora/ADB, or any device/USB/transfer operation. The documented Docker daemon
registry-resolution timeout remains a runtime blocker; C1 service evidence,
readback hashes, no-replace collision behavior, cancellation cleanup, and all
M0 V03/V04 results remain `NOT_RUN` or `BLOCKED` until their stated conditions
are met.
