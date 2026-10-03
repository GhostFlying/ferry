# M0-A2 bridge validation

Status: A2 container verification `PASS` for the versioned Go operation
contract and Android bridge packaging. The fake backend in unit tests and the
generated AAR cannot satisfy SMB, tsnet, APK, Dora, or transfer gates.

Plan target: `fa7c2e5d16d9d64b11e61c4aa130f2128a82af0b`; implementation source is
on the current branch and awaits independent implementation review.

On 2026-10-04, in the immutable local recovery image
`ferry-m0-devcontainer:recovery-test`, source SHA
`82d9e15547448b4ad49d78454ec53c42c37eb75a`, `make test` passed and
`make aar && make manifest` passed. The manifest reported gomobile/gobind
`v0.0.0-20260908204917-8b95e45f8d3e`, Gradle wrapper 8.10.2 with distribution
SHA-256 `31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26`,
arm64-v8a, and AAR SHA-256
`1604f60fc00629dcdb48ebbf22f21f671d464649a7cc62b29feb22d905f4d2b5`.

The exact local-recovery command used for this run was:

```sh
docker run --rm -e FERRY_DEVCONTAINER=1 \
  -e FERRY_SOURCE_SHA="$(git rev-parse HEAD)" \
  -v "$PWD:/workspace/ferry" -w /workspace/ferry \
  ferry-m0-devcontainer:recovery-test bash -lc \
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
