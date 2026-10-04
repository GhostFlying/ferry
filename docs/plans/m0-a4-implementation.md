# M0-A4 App-internal tsnet implementation plan

Status: `approved for implementation`; independently reviewed `PASS` in
`9ebd146` against the revised contract at `62c3160`.
A4 implements the approved M0 Android protocol path. It does not start UI,
M1 automation, iOS, device operations, or a general network policy engine.

## Work and ownership

Use pinned `tailscale.com v1.104.0` in `experiments/tsnet/` to start an embedded
`tsnet.Server`, expose interactive login to the caller, and dial the C1 test
SMB endpoint with `Server.Dial(ctx, "tcp", target)`. Keep Go code independent
of Android UI and URI types. Own only this module, its tests/Makefile, and
`docs/validation/m0/tsnet.md`. Later A2 composition adapts the exported API
inside `mobile-core` and adds local module `require`/`replace` entries.

Dependencies: A1 pinned toolchain and A2/A3 source work. A2 reviewed target is
`94c2df793f247b79d3828fb47c60df35a90714b7` (review `ba00ee2`); the cumulative
A3 source and cleanup fix are included in `81785561d3c3f5cdb3f64f5905966fcd642357bc`
and are covered by the current implementation review. Builds/tests run in the
Ferry devcontainer only. Missing C1/lease blocks experiments rather than source
work. All module versions/sums are recorded before build evidence.

## Contract and implementation

1. A caller-owned client handle owns one tsnet server and an A4-created,
   already-existing temporary state directory. Caller sets a diagnostic
   hostname and the exact C1-authorized endpoint. No exit node, fallback
   socket, system VPN, or host CLI is configured. Validate host:port syntax;
   do not introduce broad address firewalls or additional route policies.
2. Preserve the M0 interactive-login requirement. The client exposes a login
   URL via a dedicated callback to open on the device, and emits only simple
   login/dial/closed status. Login URLs, raw auth keys, peers and node state
   must not go to public logs or manifests. An optional short-lived key may
   be supplied by the caller from a protected secret reference; A4 does not
   read ambient keys or invent secret storage.
3. `Up`/login runs under caller context; `DialSMB` uses the same embedded
   server. Dial success is a connection result, never upload completion.
4. A3 takes ownership of a successfully injected connection. Caller closes
   A3 Client first, then A4 handle. A4 handle `Close` stops its server and
   removes only its own temporary directory. Failed startup/dial paths close
   the A4 handle. `Close` is idempotent and is not concurrent with startup;
   this M0 probe does not need a generic lifecycle framework.
5. Silence tsnet debug/user logging by default, explicitly forwarding the
   login URL through the private callback. Wrap errors at login/dial/close
   boundaries and do not log raw library errors with private node state.

## Acceptance

| ID | PASS condition | Evidence and environment |
| --- | --- | --- |
| A4-01 | Fake-server tests verify lifecycle, callback/status and error stages; syntax/config checks reject missing inputs; container checks pass | pinned devcontainer, full SHA, module lock, test report |
| A4-02 | Real tsnet starts and logs in interactively, then dials the exact C1 endpoint through its `Server.Dial` | C1 + fresh Dora lease; private login interaction, redacted route/result; otherwise NOT_RUN |
| A4-03 | Success/error/cancel close sequences remove only A4-owned temporary state and keep secrets out of output | deterministic lifecycle tests; device cleanup record separately |
| A4-04 | SMB upload/readback is separately recorded with matching SHA-256 | A3/C1/C2; a TCP-only or fake-dial success cannot pass V04 |

Stop on unsupported Android build, leaked secret, failed cleanup, or erroneous
completion claim. Missing container/C1/interactive login/physical lease keeps
only the corresponding runtime check BLOCKED/NOT_RUN. Root implements; an
independent reviewer checks plan and resulting code. No milestone scope or
user acceptance criteria change.
