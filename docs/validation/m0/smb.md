# M0-A3 controlled SMB validation

Status: A3 source implementation is pending independent implementation review;
no SMB connection or transfer is claimed. The backend accepts an injected
`net.Conn` and uses go-smb2 v1.1.0. It does not open a host socket, mount a
share through the host, or use a system VPN.

Plan: `8e25dd36c50231346d21a77af8e73a75481ad5f3`; plan review `2cbbc75`.

Required fixed-container commands after the Docker registry blocker is resolved:

```sh
FERRY_SOURCE_SHA="$(git rev-parse HEAD)" devcontainer up --workspace-folder .
devcontainer exec --workspace-folder . bash -lc \
  'cd experiments/smb && go test ./...'
```

The module lock must report go-smb2 v1.1.0 and all transitive sums. Unit tests
cover destination/path rejection, operation-owned temporary names, byte/hash
accounting, and cancellation helper behavior. A real C1 fixture must separately
record server identity, isolated share, short-lived credential reference,
connection path, temporary create, `Sync`, server-side no-replace rename,
readback size/SHA-256, and cleanup.

The go-smb2 v1.1.0 `Share.Rename` request sets SMB
`FileRenameInformation.ReplaceIfExists=0`; a final-name collision must remain a
server error. A same-size write or client-side existence check is not evidence
of no-replace behavior. Missing container or C1 fixture is `BLOCKED`/`NOT_RUN`,
not a pass.
