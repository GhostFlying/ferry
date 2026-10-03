# M0-A4 App-internal tsnet implementation plan

Status: `proposed`; write this plan before implementation and obtain an
independent review. A4 stays within the approved M0 Android protocol scope;
it does not start A5 probe UI, M1 automation, iOS, or device operations.

## Goal and boundaries

A4 will provide a small Go module that owns an embedded `tailscale.com/tsnet`
server and returns a `net.Conn` from its `Server.Dial` path for the A3 SMB
backend. The module will pin a released Tailscale version, accept an explicit
short-lived auth-key reference/value and target address from its caller, keep
the state directory ephemeral for the M0 experiment, and close the server on
all normal and failure paths. It will report login, dial, and close stages
separately without logging keys, peer lists, or private Tailnet state.

A4 does not add a system VPN, host `tailscale` CLI dependency, direct host SMB
socket, credential storage, persistent identity, or a second network path. A
fake dialer seam covers state/error behavior in unit tests; C1/V04 is the only
real tsnet-to-SMB evidence. `tsnet` connectivity alone is not content-transfer
success.

## Files and ownership

- `experiments/tsnet/go.mod`, `go.sum`, and Go source/tests: pinned tsnet
  lifecycle, dial boundary, redacted status, and fake dialer tests.
- `docs/validation/m0/tsnet.md`: fixed-container commands, injected secret
  contract, route evidence, C1/V04 procedure, and blocked/untested boundaries.

A4 may not modify A2 bridge, A3 SMB, Android project, devcontainer, C1
credentials, or device harness. A later A2-owned composition file wires the
exported dial result into the mobile-core backend seam.

## Dependencies and environment

- A1 PASS target `3921c4959c9b50eab5b18dae1839dfcfb77bdb1f` and review
  `c5f6103ae63d9c2042225390c6c3f5dcdeddc262`.
- A2 final boundary `c887980a8f96deea138e3f869fab3baa319200d5` and A3 final
  implementation `f3c830811197857f3bfebba2d2556f173bbc1102`; A4 does not edit
  either module.
- Fixed Ferry devcontainer and explicit `FERRY_SOURCE_SHA`; no host Go or
  network result is evidence. The current Docker registry timeout blocks
  runtime checks.
- `tailscale.com v1.104.0` and transitive sums are pinned. Auth keys and
  Tailnet addresses are test inputs only and never committed or printed.

## Implementation steps

1. Add the module and pin `tailscale.com v1.104.0`; define a small `Config`
   with injected auth-key reference, hostname, state-directory policy, and
   target address, rejecting empty or malformed values.
2. Start `tsnet.Server`, call `Up` with the supplied context, and expose a
   `DialSMB` method that uses `Server.Dial(ctx, "tcp", target)`; wrap errors
   with login/dial stages and return only the connection, never credentials.
3. Implement idempotent close and cleanup for partial startup, cancellation,
   and successful dial; ensure the ephemeral state directory is removed by
   the owner after close.
4. Add fake dialer/lifecycle tests for config validation, login/dial error
   mapping, cancellation, close idempotence, and secret redaction. Do not
   claim a real Tailnet route in unit tests.
5. Write the C1/V04 validation procedure separating tsnet dial success from
   SMB content readback; mark it `NOT_RUN` until an authorized fixture and
   fresh Dora lease exist.

## Acceptance and evidence

| ID | PASS condition | Evidence |
| --- | --- | --- |
| A4-01 | Locked-container Go tests pass with tsnet module sums and no host CLI dependency | source SHA, container manifest, test/static output, module lock |
| A4-02 | App-internal tsnet starts with injected short-lived input and `Server.Dial` returns the expected target connection; status is redacted | lifecycle trace, target/route identity without secrets, stage result |
| A4-03 | Cancellation, partial startup, and close remove ephemeral state and do not leak auth key or peer data | cleanup trace, redacted logs, state-directory check |
| A4-04 | C1/V04 separately proves tsnet connection and complete SMB upload/readback SHA-256 | Dora lease, route evidence, local/remote hash, cleanup; otherwise `BLOCKED`/`NOT_RUN` |

Missing container, auth-key reference, authorized Tailnet route, C1 fixture, or
fresh Dora lease blocks only the corresponding runtime gate. A fake dialer or
successful TCP connection cannot be reported as complete SMB transfer.
