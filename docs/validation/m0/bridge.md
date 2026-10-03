# M0-A2 bridge validation

Status: A2 implementation source is written; no bridge build or AAR is
claimed yet because the fixed devcontainer remains blocked. The A2 scope is the versioned Go operation contract and Android
bridge packaging around an injectable backend. The fake backend in unit tests
cannot satisfy SMB, tsnet, APK, Dora, or transfer gates.

Plan target: `fa7c2e5d16d9d64b11e61c4aa130f2128a82af0b`; the revised plan review
is pending. The superseded plan review `80eaab3e353508b5fef3720c40f4629e3152d38c`
does not cover this revision. Implementation source is on the current branch
and awaits A2 implementation review.

Required container commands, once the Docker registry blocker is resolved:

```sh
FERRY_SOURCE_SHA="$(git rev-parse HEAD)" devcontainer up --workspace-folder .
devcontainer exec --workspace-folder . bash -lc \
  'cd experiments/mobile-core && make test && make aar && make manifest'
```

The container must report the pinned Go/x/mobile, gomobile/gobind, Gradle
wrapper distribution and checksum, source SHA, ABI, and AAR SHA-256 through
`experiments/mobile-core/manifest.sh`. Host Go,
Gradle, Java, Android SDK, or host-built AAR output is not evidence.

Expected vectors are: successful fake transfer emits progress then one complete;
cancellation emits one `cancelled` terminal error and no complete; structured
backend errors preserve their code/message; invalid progress is ignored; and
late progress after terminal state is ignored. Callback methods are invoked by
the bridge's dedicated per-operation dispatcher goroutine, never directly by
the backend worker; a future Android adapter must marshal them to its required
executor. SMB and tsnet results remain
`NOT_RUN` until A3/A4 integration.
