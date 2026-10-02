## Problem and resulting behavior

Describe the concrete trigger and delivered outcome. For a planning PR, state that the app and device paths remain unimplemented.

## Scope, plan and authorization

- Task issue / milestone:
- Versioned plan:
- Owned files and exclusions:
- User approval reference and approved scope, or pending:
- Next milestone gate:

## Dependencies and source

- Depends on / blocks issues and PRs, or none:
- Branch / target base branch / complete base SHA:
- Complete source SHA reviewed and validated:
- Before the first commit only: uncommitted file manifest and SHA-256 hashes:
- Owner / independent reviewers / models and reasoning:
- Worktree path:

## Independent review

- Plan review and resolved findings:
- Implementation review of the complete source SHA:
- Open P0/P1 findings: none required for technical PASS
- P2 deferrals, reviewer acceptance and tracking issues:

## Validation and evidence

| Check / command | Environment and input | Result | Evidence / artifact |
| --- | --- | --- | --- |
| | | PASS / FAIL / BLOCKED / NOT_RUN | |

State the explicitly untested scope, hardware limitations and required user/device actions. Separate simulator, local network, tsnet and actual Pocket 3 USB results.

## Artifact provenance, when applicable

- Actions run / download link:
- Complete source SHA / app version / version code:
- APK ABI / debug or release / minimum and target SDK:
- Toolchain versions / APK SHA-256:
- Install and launch verification:

Planning PRs may state `not applicable — no app or APK exists`. AAR output and document CI do not satisfy APK acceptance. Debug APKs must be described as non-release artifacts.

## Risks, recovery and milestone handoff

- Material risks and recovery/rollback:
- Plan changes and re-review:
- User decisions and next-stage scope:
- Milestone status: awaiting user review / accepted with explicit decision reference

## Delivery checklist

- [ ] Changes follow the versioned plan and explicit authorization.
- [ ] Dependencies and final target base are recorded; affected checks reran after base changes.
- [ ] Independent reviews target the final complete SHA and have no open P0/P1 findings.
- [ ] Validation distinguishes passed, failed, blocked and unrun checks.
- [ ] Public logs, screenshots, configuration and artifacts contain no credentials or private network/device details.
- [ ] Atomic commits use the user's existing Git identity and a lowercase Conventional Commit type.
- [ ] The milestone package is ready for user review; no internal PASS is presented as user approval.
