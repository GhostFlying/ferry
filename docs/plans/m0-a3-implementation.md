# M0-A3 controlled SMB implementation plan

Status: `proposed`; write this plan before implementation and obtain an
independent review. A3 is Android-platform-independent Go code and does not
start A4 tsnet, A5 probe UI, M0-C1 credentials, Dora operations, M1, or iOS.

## Goal and boundaries

A3 will implement the controlled SMB upload backend used by the bridge. The
client receives an already-created `net.Conn`; it never opens a second socket,
uses a host mount, or relies on a system VPN. It will use the pinned
`github.com/hirochachacha/go-smb2 v1.1.0` client and expose a small upload
operation that:

1. validates a relative destination and rejects traversal/absolute paths;
2. creates a unique temporary remote name with exclusive create;
3. streams the source while computing the local SHA-256 and byte count;
4. calls SMB `Sync`, closes the temporary file, and renames it to the final
   name only when the destination is still absent;
5. reads the final object back through the same share and compares size and
   SHA-256 before returning complete; and
6. removes only the operation-owned temporary file on failure or cancellation.

The backend reports connection/authentication, remote-create, write/flush,
rename/no-replace, readback, and cancellation errors separately. It does not
delete or replace an existing final object. A unit-test seam covers path rules,
temporary-name ownership, hash accounting, and error mapping; only C1/V03
supplies real SMB service evidence.

## Files and ownership

- `experiments/smb/go.mod`, `go.sum`, and Go source/tests: SMB client,
  upload/readback logic, deterministic helper seam, and unit tests.
- `docs/validation/m0/smb.md`: exact container commands, controlled fixture
  prerequisites, gate vectors, result fields, and blocked boundaries.

A3 may not modify the bridge package, Android project, devcontainer, C1
credentials, or device harness. A later A2-owned composition file wires this
backend into `mobile-core`.

## Dependencies and environment

- A1 implementation/review PASS: `3921c4959c9b50eab5b18dae1839dfcfb77bdb1f`
  / `c5f6103ae63d9c2042225390c6c3f5dcdeddc262`.
- A2 plan/implementation PASS: plan `fa7c2e5d16d9d64b11e61c4aa130f2128a82af0b`,
  implementation `94c2df793f247b79d3828fb47c60df35a90714b7`; A3 does not
  change A2 files.
- The fixed Ferry devcontainer with explicit `FERRY_SOURCE_SHA`; no host Go,
  static checker, or network result is evidence. The current Docker daemon
  registry timeout blocks runtime checks and remains a stop condition.
- `go-smb2 v1.1.0` and all transitive module sums must be recorded in
  `go.mod`/`go.sum`; no floating dependency or `latest` is allowed.

## Implementation steps

1. Create the module and pin go-smb2. Define typed result/error values and
   path/temp-name helpers without credentials or real endpoint defaults.
2. Implement connection/session/share construction from the injected
   `net.Conn`, with context-aware dialing and explicit close/unmount cleanup.
3. Implement exclusive temporary create, streaming hash/count, `Sync`,
   close, no-replace rename, final readback hash/count, and owned-temp cleanup.
4. Add unit tests for path traversal, hash/count, temporary ownership,
   cancellation/error classification, and no-replace decision helpers.
5. Write the validation record. Once C1 exists, run a real controlled SMB
   fixture experiment; until then mark network evidence `NOT_RUN`.

## Acceptance and evidence

| ID | PASS condition | Evidence |
| --- | --- | --- |
| A3-01 | Go unit tests and static checks pass in the fixed devcontainer with go-smb2 sums locked | source SHA, container manifest, test/static output, module lock |
| A3-02 | One upload uses only the injected `net.Conn`, creates an exclusive temp, flushes, no-replace renames, and returns local/remote hashes only after readback match | operation trace, byte counts, local/remote SHA-256, error stage |
| A3-03 | Failure/cancel removes only the owned temp and never removes/replaces an existing final object | failure trace, remote listing/readback, cleanup result |
| A3-04 | Controlled C1 SMB run is separated from pure implementation and records `BLOCKED`/`NOT_RUN` when fixture or container is unavailable | `docs/validation/m0/smb.md`, fixture identity, redacted logs |

Missing container runtime, unpinned modules, absent C1 fixture, or missing
fresh device lease blocks only the corresponding runtime gate; it cannot be
converted into a pass with a host mount, mock-only result, or same-size write.
