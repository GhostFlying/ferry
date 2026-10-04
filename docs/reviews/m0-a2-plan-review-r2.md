# M0 A2 implementation plan review (revision 2)

Status: **PASS**

This is an independent, affected-only review of the revised A2 plan. It does
not establish any implementation, build, AAR, APK, network, or device result.

## Review identity and scope

- Reviewed plan target: `fa7c2e5d16d9d64b11e61c4aa130f2128a82af0b`
- Previous reviewed plan: `ad427c8d71b230ba38cd8014a37d40da50508b86`
- Previous plan review: `80eaab3e353508b5fef3720c40f4629e3152d38c`
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The revision updates the A2 plan and the corresponding execution/M0 ownership
rows. It changes the backend wording and ownership boundary; it does not add
application or bridge implementation code.

## Static verification

The following checks passed:

- `git diff --check 80eaab3 fa7c2e5`
- full SHA and ancestry verification for the revised plan
- local Markdown-link checks over the affected plans and their referenced
  planning documents: no missing targets
- consistency inspection of A1/A2/A3/A4 ownership, dependencies, container
  boundary, M0 V01–V05 scope, and review/stop language

No P0, P1, or P2 finding remains in this revision.

## Review findings

- The revision keeps A2 limited to the approved M0 Android protocol. The
  bridge contract, deterministic fake, AAR packaging, and validation record
  remain A2 work; SMB, tsnet, APK, Dora, Pocket USB, and complete-transfer
  results remain outside A2 claims.
- The previous cross-package injection ambiguity is removed. A2 owns the
  package-internal backend seam and a later composition file under
  `experiments/mobile-core/`; A3 and A4 own their real backend packages and
  supply them later. The plan no longer requires an exported constructor from
  the bridge package, so A2 does not claim an integration API it does not own.
- The ownership table and dependency DAG identify the later composition point,
  keep A2 dependent on A1, and preserve A4's A2/A3 dependency. The registered
  single-worktree execution order and exclusive package ownership remain
  explicit, avoiding parallel edits or an untracked shared integration file.
- The fixed devcontainer, A1 toolchain boundary, exact gomobile/gobind and
  Gradle-wrapper pin responsibility, fake-only evidence, and `BLOCKED` stop
  condition remain intact. C1 and Dora are not made prerequisites for pure A2
  implementation, and A2 does not broaden V01–V05 or introduce a deterministic
  build gate.

## Untested boundaries

This is a documentation-only review. It did not run a container, Go/Gradle or
Android build/test, generate an AAR/APK, access SMB or tsnet, connect to
Dora/ADB, occupy a lease, or perform device/USB/transfer validation. The
documented Docker registry-resolution blocker remains a runtime stop condition.
