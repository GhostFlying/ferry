# M0-A4 tsnet validation

Status: A4 source and deterministic lifecycle tests are implemented; the real
interactive login and C1 dial remain `NOT_RUN` until the controlled C1 service
and fresh Dora lease are available. A fake backend test cannot prove tsnet,
SMB, Dora, or transfer behavior.

Plan: [`docs/plans/m0-a4-implementation.md`](../../plans/m0-a4-implementation.md),
review `9ebd146`.

The module pins `tailscale.com v1.104.0` and is tested only inside the Ferry
devcontainer. The implementation creates and owns a temporary state directory,
passes the exact caller target to `Server.Dial`, forwards only a detected login
URL through a private callback, and does not configure an exit node, fallback
socket, system VPN, or broad address policy. A3 must close its SMB client before
the A4 client is closed by the later composition layer.

The fixed-container command is:

```sh
docker run --rm -e FERRY_DEVCONTAINER=1 \
  -e FERRY_SOURCE_SHA="$(git rev-parse HEAD)" \
  -v "$PWD:/workspace/ferry" -w /workspace/ferry \
  ferry-m0-devcontainer:recovery-test bash -lc \
  'cd experiments/tsnet && make test'
```

The deterministic test pass at source SHA
`3ab503bdf204c86e4a0b2d86a0661bbecbcd415b` covered target validation,
start/ready/dial/closed status ordering, idempotent close, startup failure
cleanup, state-directory removal, and login URL filtering. The source was
formatted and tested in the container; the generated module lock contains all
transitive sums for v1.104.0.

The real A4-02 gate requires a private login interaction, a redacted exact C1
endpoint result from `Server.Dial`, and a separate A3 upload/readback record.
Do not report a successful tsnet dial as SMB or transfer completion.
