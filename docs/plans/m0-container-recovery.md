# M0 devcontainer build recovery

Status: proposed. Owner: `/root`; independent reviewer: review-only agent.
This work restores A1's container build path and verifies A2/A3. It does not
change application/platform/milestone scope or use host Go/Java/Android tools.

## Plan

The Docker daemon cannot resolve Docker Hub while host-side `skopeo` can fetch
the pinned image. The imported image lacks repository digests but has the
identical immutable config ID. Verify the pin's index -> linux/amd64 manifest
-> config chain, import that image, and let Docker build from that exact local
config ID. Add `FERRY_BASE_IMAGE` ARG with the existing upstream digest as the
default; record both index/manifest/config digests in the toolchain lock.
CI keeps the upstream default. The local override is an immutable image ID of
the same verified image, not a mutable tag or substitute toolchain.

Files: `.devcontainer/Dockerfile`, `experiments/toolchains.env`,
`experiments/README.md`, `docs/validation/m0/environment.md`, this plan.
Root also syncs the hand-written AGENTS role rule to the user's direct-root
execution instruction. Generated parent instructions are not edited.

Recorded identity:

- index: `sha256:43f4431cc895d37ceb115e2e1f160545ec45caee9c1cb4246fc6bb879d58aeda`
- linux/amd64 manifest: `sha256:8d242405506ad1085e39f1ca80ec76f0812f61073efe76129607e39f043ccbb9`
- config/image ID: `sha256:b2a5b9c58fedbd66afc3b58fc99d7526673b6f45fbb1b25f2cda00933a293af3`

Pass requires the chain to match, a successful Docker build of the same
Dockerfile with local immutable ID, and smoke plus real Go test/vet/Android
bridge checks inside that devcontainer. Source fixes exposed by these checks
are recorded as fixes and independently reviewed. Secrets/proxy credentials
are not logged or embedded in layers; Docker's predefined proxy build args
are supplied from the process environment only. Missing packages/network keep
the build BLOCKED/FAIL; no host-toolchain fallback or fake APK is permitted.
