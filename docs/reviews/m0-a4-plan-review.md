# M0 A4 tsnet and container-recovery plan review

Status: **PASS**

This is an independent, affected-only review of the A4 tsnet plan and the
associated devcontainer recovery plan. It establishes no tsnet, SMB, Docker,
Android, Dora, device, or transfer result.

## Review identity and scope

- Reviewed A4/recovery target: `62c3160cf932855d56fb425fcee3290e856b788f`
- Previous A4 plan target: `65a8392c6c2440d348dadf0304c228a4df4c884f`
- A2 implementation baseline: `94c2df793f247b79d3828fb47c60df35a90714b7`
  (review `ba00ee2`)
- A3 implementation baseline: `f3c830811197857f3bfebba2d2556f173bbc1102`
  with final ownership documentation `c887980a8f96deea138e3f869fab3baa319200d5`
  (review `3cdab7a`)
- Implementation owner: `/root`
- Independent reviewer: `/root/m0_plan_reviewer_astra` (`gpt-6-astra/high`)
- Registered branch/worktree: `feat/m0-android-protocol`,
  `../ferry-worktrees/m0-implementation-20261003`

The target revises `docs/plans/m0-a4-implementation.md`, adds
`docs/plans/m0-container-recovery.md`, and synchronizes the hand-written role
rule in `AGENTS.md`. It does not add application or network implementation.

## Static verification

The following checks passed:

- `git diff --check 65a8392 62c3160`
- full target SHA and ancestry verification
- local Markdown-link inspection for both affected plans (no missing links)
- consistency inspection against the execution DAG, A2/A3 ownership, M0
  V01–V05, C1/Dora lease gates, host/container boundary, and stop conditions
- read-only API/version inspection of the pinned `tailscale.com v1.104.0`
  package and its `go.mod`; the required `Up`, `Dial`, and `Close` APIs exist,
  and its Go 1.27.1 requirement matches the pinned A1 toolchain

No P0, P1, or P2 finding remains.

## Review findings and acceptance mapping

- A4 remains a small Android-independent Go module around one embedded
  `tsnet.Server`. It owns a caller-visible client handle, the server, and its
  own pre-created temporary state directory. The documented shutdown order is
  A3 `Client.Close`, then the A4 handle `Close`, then removal of only the
  A4-owned directory. Failed startup and dial paths are included, and close is
  idempotent without introducing a general lifecycle framework.
- The revised contract preserves the required interactive-login path through a
  private callback. An optional short-lived key is supplied by the caller from
  a protected reference; A4 does not read ambient environment keys or create
  a credential store. Login URLs, raw keys, peer data, node state, and verbose
  tsnet output are excluded from public logs and manifests.
- A4 dials the exact C1-authorized endpoint through `Server.Dial` and does not
  add an exit node, fallback socket, system VPN, host `tailscale` CLI, or broad
  address-policy engine. Connection success remains distinct from SMB upload
  and readback success. C1 and a fresh physical Dora lease are required only
  for the corresponding real experiment; fake-server and container checks can
  proceed independently.
- A2 retains ownership of later `mobile-core` composition and local module
  `require`/`replace` entries. A4 owns only `experiments/tsnet` and its
  validation record, so the A2/A3/A4 DAG and file ownership remain explicit.
- The recovery plan keeps CI on the upstream digest-qualified base image. The
  local path verifies the index → Linux/amd64 manifest → config/image-ID chain,
  uses an immutable local image-ID override, and requires the same Dockerfile
  plus smoke/Go/Android checks in the devcontainer. It explicitly rejects
  mutable tags, substitute images, host Go/Java/Android tooling, fake APKs, and
  secret/proxy leakage. The recorded Docker registry-resolution failure stays
  `BLOCKED`; it is not converted into a runtime pass.
- The hand-written `AGENTS.md` role update matches the current direct-root
  execution instruction and leaves the generated parent instructions alone.
  It does not change product scope, milestone gates, or user acceptance.

## Untested boundaries

This documentation review did not run Docker, build or enter the devcontainer,
Go tests/vet, tsnet login or dial, SMB, the C1 fixture, A2 composition, Dora or
ADB, Android, USB, or any transfer. The documented Docker daemon registry
timeout remains a runtime blocker. Interactive login, route identity, state
cleanup, complete SMB readback, and V04 evidence remain `NOT_RUN` or `BLOCKED`
until their stated environment and lease conditions are met.
