# Ferry 代理协作与审查协议

状态：当前规划交付的一部分，待独立审查及用户审阅。项目约束见 [AGENTS.md](../AGENTS.md)，阶段与产品验收见 [实施计划](implementation-plan.md)。

## 授权与推进顺序

主代理协调与报告，实现代理负责所分配文件，独立审查代理审查他人编写的计划和变更，不能实现同一变更。主代理不编写应用业务代码。复杂任务按以下顺序推进：

1. 将计划和 DAG 依赖落文件，分配任务 ID、文件所有权和独立 reviewer。
2. 独立计划审查检查需求、依赖、文件范围、平台证据、验收和停止条件；修复阻断项后复核。
3. 确认任务位于用户已批准的里程碑范围内。当前仓库／治理／issue／规划 PR 准备已获授权；M0 应用和 APK workflow 仍须用户明确批准。
4. 在独立工作树实施，执行任务验收，留下完整 SHA 和证据。
5. 独立实现审查检查真实 diff、边界和验证，复核修复；验收检查与待提交变更一致。
6. 整理 milestone 的计划、PR、证据、未决项和下一阶段范围，交用户审阅。只有明确批准后才进入下一 milestone。

内部 PASS、CI 通过、PR 合并、任务关闭和用户未回复不构成用户里程碑批准。可以完成已获授权的任务；不得把 `awaiting_user_review` 改写成 `accepted`。

## 任务 DAG 与工作树

阶段计划列出任务 ID、依赖和状态；GitHub issue 是进度入口，计划文件是验收及范围依据。尚未建立公共仓库时以本地记录为准，建仓后补上真实 URL，不能编造 issue／PR 编号。

| 字段 | 要求 |
| --- | --- |
| Task ID／milestone | 稳定本地 ID，建仓后对应 issue／milestone |
| Depends on／blocks | 直接前置和后继；跨 milestone 的任务必须保持用户关口 |
| Owner／reviewer | 角色、代理任务名、模型及 reasoning；reviewer 与作者独立 |
| Plan | 文件路径、版本／提交和计划 review 记录 |
| Files | 拥有的文件或目录；共享文件修改必须提前重新分配 |
| Branch／base | 实现分支、目标分支、完整基线 SHA和依赖 PR |
| Worktree | `../ferry-worktrees/<task-id>-<slug>/` 或记录的实际路径 |
| Acceptance | 可执行步骤、输入、期望、证据和硬件条件 |
| Stop conditions | 何时上报／重新规划／等待用户决定 |

依赖满足后才开始实现。无依赖的计划、读资料和环境调查可以并行，但必须标明其范围。依赖 PR 合并后更新依赖者到新的基线，重跑受影响的验收和 review；旧提交的 PASS 不覆盖新提交。

## 计划记录模板

```text
Task ID / milestone:
Status: proposed | plan_reviewed | approved_for_implementation
Problem and resulting behavior:
Approved scope and explicit exclusions:
User approval reference and scope (or pending):
Dependencies and target base:
Owner / independent plan reviewer / implementation reviewer:
Models and reasoning:
Owned files and worktree:
Implementation steps:
Acceptance steps, expected results, environment and evidence:
Device/tool/credential prerequisites (references only):
Risks, rollback or recovery:
Stop conditions and decisions requiring user input:
Plan review findings and resolution links:
```

计划审查通过只改变技术状态。若里程碑尚未获用户授权，状态停在 `plan_reviewed`；不创建真实应用实现提交。

## 独立 review 与解项

审查记录必须说明类型（plan／implementation）、作者、审查者、模型、目标文件／完整 SHA、结论、发现和证据。审查者需要看原计划、diff 和必要验证；实现者提供的摘要用于定位，不能代替审查。

| 等级 | 含义 | 处理 |
| --- | --- | --- |
| P0 | 数据丢失、密钥泄露、未经授权的破坏操作或严重安全问题 | 立即停止受影响工作；修复并独立复核后才恢复 |
| P1 | 关键需求／验收不成立、数据完整性错误、违反用户关口或没有真实构建却宣称通过 | 阻止技术 PASS／合并／阶段验收；修复后复核 |
| P2 | 有限范围的行为、可靠性或可维护性问题 | 默认修复；如延期，记录影响、跟踪 issue 和 reviewer 接受理由；影响用户验收时交用户决定 |
| P3 | 不影响验收的建议或文档改进 | 记录并按任务价值处理；不得用它遮盖高等级问题 |

```text
Review kind / date:
Author / reviewer / model:
Plan version / full target SHA (or uncommitted file manifest with SHA-256):
Conclusion: PASS | CHANGES_REQUIRED | BLOCKED
Finding ID / severity / file or criterion:
Observed behavior and reproduction/evidence:
Required change and verification:
Resolution commit (full SHA) / evidence:
Re-review outcome:
Remaining risks and explicitly untested scope:
```

每个 finding 保留稳定 ID。作者记录修复提交、验证命令与结果；reviewer 在新 SHA 上复核并解项。仍有 P0／P1 时不能 PASS。P2 延期不是自动豁免，不能改变用户已批准的验收。审查阻塞时记录所缺证据，不凭主观信心标记通过。

## Checkpoint 与 GitHub 报告

计划 review、关键实验结论、阻塞、实现 review、验收和用户关口均留下 checkpoint。主代理向用户报告结论、剩余不确定性和下一步；issue／PR 保留可追踪的事实。

```text
Checkpoint ID / UTC timestamp / task / milestone:
Status: planned | implementing | in_review | verified | blocked | awaiting_user_review | accepted
Owner / reviewer / model:
Plan URL or repository path:
Issue / PR / dependencies / target base:
Current full SHA (or uncommitted file manifest with SHA-256):
Changes and findings:
Validation command, environment, result and artifact link/hash:
Evidence boundary and untested items:
Open findings / blockers / required decision:
Next authorized action:
User milestone decision reference (only when explicit):
```

没有 commit 时如实写 `uncommitted preparation`，同时列出待审查的文件清单及每个文件的 SHA-256，review 和提交前再次核对；文件变化使其旧快照失效。首个 commit 后改用完整提交 SHA。没有公开仓库时写本地路径。部分完成不能写整阶段完成；设备模拟、局域网和 tsnet 路径分别记录。公开记录只保留脱敏证据，不包含凭据或真实私人网络配置。

Task issue 使用 [任务模板](../.github/ISSUE_TEMPLATE/task.yml)，包含范围、DAG、所有权、计划／review、验收和用户关口。阻塞使用 [阻塞模板](../.github/ISSUE_TEMPLATE/blocker.yml)。PR 使用 [PR 模板](../.github/pull_request_template.md)，给出具体行为、依赖和目标 base、独立审查、测试命令、未测范围及产物对应关系。主代理可以维护已授权的项目记录，未经明确授权不通知／邀请／`@` 其他人。

## 阻塞与计划变更

阻塞报告区分 `technical`、`environment`、`device_access`、`user_decision` 和 `authorization`，附上复现条件、已尝试的调查、影响的 DAG 节点、可独立推进范围以及解除条件。状态 `BLOCKED` 表示技术或环境条件不满足；用户尚未审批的正常关口记录 `awaiting_user_review`。

如果真实设备、NAS、macOS／Xcode、工具链或权限尚不可用，只完成不依赖它们的已授权工作。提交可运行工具与检查步骤供用户执行，并明确无人执行的步骤；不能用未运行脚本声称实测通过。

发现原计划不能满足关键能力时暂停受影响实现，在原 issue 记录问题，更新计划版本、依赖、验收、成本和替代方案，交独立 reviewer。若改变用户批准的范围、平台保证、数据行为或里程碑顺序，交用户重新决定；不靠内部 review 继续推进。

## 验收与产物

验收记录绑定完整源码 SHA、命令、工具链、设备／系统、输入、期望和实际结果，并区分 `PASS`、`FAIL`、`BLOCKED`、`NOT_RUN`。必要的 USB 真机、飞牛和 tsnet 结果各自列证据来源；一般模拟器测试不证明 USB／供电或真实性能。

当前规划阶段只验证文档、示例 JSON 和仓库准备。文档 CI 不能命名或报告为 Android 构建通过。真实应用存在且对应里程碑批准后，Android Actions 应构建并保存可安装的 APK；AAR 是桥接中间产物，不是 APK 验收。

```text
Artifact:
Source full SHA / PR / Actions run:
App version / version code / debug or release:
ABI and minimum/target SDK:
JDK / Gradle / AGP / Android SDK / Go / gomobile versions:
APK SHA-256 and download link:
Install/launch verification environment and result:
Device/integration tests and explicitly untested scope:
```

debug APK 明确不是正式发布。签名密钥、SMB 凭据、Tailnet 登录状态等只使用受保护的本地／CI secret 引用；不进入源码、产物、日志或截图。用户已选择 [Apache-2.0](../LICENSE)；应用标识和正式分发渠道仍须用户决定，公开仓库和构建通过不代替这些决定。

## 里程碑交给用户的审阅包

主代理提供本里程碑的最终计划、所有关联 issue／PR、最终 SHA、独立 review 与修复、验收与真实环境、APK 或其他真实产物、未测范围、未决项以及下一阶段的具体范围／停止条件。明确请求用户决定 `approve_next`、`revise` 或 `hold`，记录批准覆盖哪个 milestone 和计划版本。用户决定前保持关口，继续范围内的整理及问题修复，不开始下一阶段实现。
