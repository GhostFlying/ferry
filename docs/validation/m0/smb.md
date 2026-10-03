# M0-A3 controlled SMB validation

Status: A3 unit-test verification `PASS`; no real SMB connection or transfer
is claimed. The backend accepts an injected
`net.Conn` and uses go-smb2 v1.1.0. It does not open a host socket, mount a
share through the host, or use a system VPN.

Plan: `8e25dd36c50231346d21a77af8e73a75481ad5f3`; plan review `2cbbc75`.

The fixed-container unit-test command was run on 2026-10-04 in the immutable
local recovery image `ferry-m0-devcontainer:recovery-test`, source SHA
`c766c06289db1e092c250602da7f3b3ce5087f8f`, with `make test` passing. The
test uses the container's network proxy only to fetch the pinned module sums;
no SMB server was contacted. A later bridge-only Makefile change advanced the
branch to `82d9e15` without changing the SMB source or test result.

Required fixed-container command for repeatability:

```sh
docker run --rm -e FERRY_DEVCONTAINER=1 \
  -e FERRY_SOURCE_SHA="$(git rev-parse HEAD)" \
  -v "$PWD:/workspace/ferry" -w /workspace/ferry \
  ferry-m0-devcontainer:recovery-test bash -lc \
  'cd experiments/smb && make test'
```

The module lock must report go-smb2 v1.1.0 and all transitive sums. Unit tests
cover destination/path rejection, operation-owned temporary names, byte/hash
accounting, and cancellation helper behavior. A real C1 fixture must separately
record server identity, isolated share, short-lived credential reference,
connection path, temporary create, `Sync`, server-side no-replace rename,
readback size/SHA-256, and cleanup.

The caller provides the injected TCP connection and ownership transfers to a
successfully constructed `Client`; `Client.Close` unmounts, logs off, and closes
it. go-smb2 combines SMB negotiation and NTLM authentication in `DialContext`,
so those errors are reported as `authenticate`; share mounting is reported as
`connect`. The client does not guess a finer split from error text.

The go-smb2 v1.1.0 `Share.Rename` request sets SMB
`FileRenameInformation.ReplaceIfExists=0`; a final-name collision must remain a
server error. A same-size write or client-side existence check is not evidence
of no-replace behavior. `Upload` applies the per-call context to SMB share
operations and `Client.Close` unmounts the share before logging off the session
and closing the injected connection. Temporary cleanup errors are surfaced as
`cleanup` stage failures; they are not discarded. Missing container or C1
fixture is `BLOCKED`/`NOT_RUN`, not a pass.
