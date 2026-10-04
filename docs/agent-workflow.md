# Ferry 代理协作与审查协议

状态：M0 Android 受控协议已获用户批准，执行待独立 plan review；M1、M0-UI 与 iOS 仍待各自用户关口。项目约束见 [AGENTS.md](../AGENTS.md)，阶段与产品验收见 [实施计划](implementation-plan.md)，当前执行依据见 [scope-trim 执行计划](plans/scope-trim.md) 与 [scope-trim review 准备记录](reviews/scope-trim-review-prep.md)。[Android 优先与设计先行修订计划](plans/android-first-design-revision.md) 仅保留为历史／superseded 记录。

## 授权与推进顺序

当前会话采用简化执行模式：主代理 `/root` 直接负责已批准范围内的计划修订、实现、验证和报告；只把独立 plan／implementation review 交给不参与该变更的 reviewer。复杂任务按以下顺序推进：

1. 将计划和 DAG 依赖落文件，分配任务 ID、文件所有权和独立 reviewer。
2. 独立计划审查检查需求、依赖、文件范围、平台证据、验收和停止条件；修复阻断项后复核。
3. 确认任务位于用户已批准的里程碑范围内。当前仓库／治理／issue／规划 PR 准备已获授权；M0 Android 受控协议 V01–V05 已获用户批准，但应用和 APK workflow 仍须独立 plan review；M1、M0-UI 与 iOS 仍须用户明确批准。
4. `/root` 在登记的独立工作树实施，执行任务验收，留下完整 SHA 和证据；除 review 外不再委派实现代理。
5. 独立实现审查检查真实 diff、边界和验证，复核修复；验收检查与待提交变更一致。
6. 整理 milestone 的计划、PR、证据、未决项和下一阶段范围，交用户审阅。只有明确批准后才进入下一 milestone。

内部 PASS、CI 通过、PR 合并、任务关闭和用户未回复不构成用户里程碑批准。可以完成已获授权的任务；不得把 `awaiting_user_review` 改写成 `accepted`。

当前范围为 M0 受控服务／Dora Android 物理设备协议验证、M1 Android 前台基础版及 Pocket／Pixel／飞牛链路、M2 Android 自动模式／恢复与发行准备、M3 才做 iOS 前台版／Pocket USB 和双平台交付。M0–M2 不含 iOS／macOS／签名／回归依赖，也不提前实现未来平台抽象。本次治理提交只修订规划，不启动容器、构建或设备租约；M0 可在独立 plan review 后启动，M1、M0-UI 与 iOS 仍待各自用户关口，M2 的发行安排仍是待审方案。用户已批准 M0 后，先合并规划 PR，让规则和计划进入 main，再从该已批准基线建立 M0 工作树。旧 M1 双平台审批问题已被当前范围替代。

## UI 概念、接受与原生保真协议

每个阶段的任何 UI 方案先使用 `build-web-apps:frontend-app-builder` 与 Image Gen。先记录功能 brief／必须信息和行为，不在图示及用户接受前指定布局、tokens、字体、间距或组件几何。M0 probe 诊断界面也属于 UI；M2 新设置／通知及 M3 iOS 到相应阶段另生成图，不用 M1 概念默认覆盖。

| 顺序／记录 | 必需产物和关口 |
| --- | --- |
| Brief／surface inventory | 真实流程、屏幕及状态、必须文案／控件／数据、fixture 与未验证能力、目标原生平台；完整主屏和必要细节都须可读 |
| Concept generation | skill／Image Gen 生成图、路径／SHA-256、screen／state 映射、质量自检；缺态／模糊图重新生成，不用局部裁剪代替完整参考 |
| User design acceptance | 用户决定原文／时间、接受的具体图片版本和覆盖范围；pending／revise／accepted 分明，内部 PASS 不算接受 |
| Tokens／UI implementation plan | 仅从接受图提取色彩锁定、文字／字体、图标、间距、组件／状态和交互清单；记录文件 owner、概念映射、偏差、验收及独立 plan review |
| Implementation authorization | 另查 milestone 用户批准与实际任务依赖；设计接受不批准应用实现，阶段批准也不默认为概念被接受 |
| Native fidelity QA | 实际 App 真机截图、同轮 `view_image` 概念／截图、至少五类对照、可见文案 diff、fidelity ledger、修复与复核；按状态覆盖，不能只看概览 |
| Functional／device QA | 真实控制和状态变化、安装／权限／恢复、传输／USB 各自 evidence 与 PASS；视觉／功能结果分别判定 |

本项目使用 Kotlin／Compose 与 M3 Swift／SwiftUI，基于用户原生目标和已有架构，遵守 skill 的指定／已有框架选择规则。原生验证适配须明确写入 UI 计划并供用户 review：使用实际运行 App 的真机截图对照接受图，记录尺寸不一致、系统栏／输入法／字号／主题等环境差异；无权以此声称 Browser QA 已通过。如果另有网页 artifact，才执行其 Browser 优先或说明 Playwright fallback 的网页流程，网页结果不代替 native 验证。本轮只有概念图时 native／browser 实现验证均为 `NOT_RUN` 或网页不存在时 `NOT_APPLICABLE`。

视觉验收至少逐项检查文案、信息层级／布局、字体、色彩、间距／容器，并覆盖图标、可见状态、裁切／换行、必要可访问性与实际控件行为。同一 QA 轮次用 `view_image` 查看概念和最新截图，保持内容、几何、颜色、密度和交互忠实；保存 mismatch → 概念证据 → 截图证据 → 修复／用户批准偏差的 ledger。可修复的明显差异阻止 UI 交付，构建或操作通过不免除视觉保真；新可见内容／主要状态缺概念时回到生成与接受关口。

```text
UI task / milestone / skill source:
Concept files / SHA-256 / screen-state coverage / fixture declarations:
User design decision / timestamp / accepted versions and scope (or pending):
Separate user milestone authorization (or pending):
Native framework rationale / browser adaptation and actual artifacts:
Accepted-copy inventory / extracted tokens and component plan (only after acceptance):
Independent UI plan reviewer / findings / resolutions:
Source SHA / actual device-system / dimensions-theme-input / screenshot files:
view_image comparison / at least five points / copy diff / fidelity ledger:
Functional-device result / untested scope / intentional user-approved deviations:
```

设计任务可先完成已经授权的图示建议；概念尚待接受时记录该部分状态，不伪装成整个 task／milestone 已完成。无关非 UI 核心任务不依赖视觉接受，仍受用户阶段批准、技术计划和依赖约束。概念／原生截图的必要审查证据保留可追踪来源，其他临时 QA 文件按 skill 清理。

## 每个 milestone 的工作与验收契约

所有当前阶段文件和 GitHub milestone 都必须提供下列映射；不能只写阶段目标或使用“支持、稳定、完善”作为通过条件。未排期 backlog 选入阶段时，先补同样的计划并独立审查。

| 必需字段 | 内容与判定 |
| --- | --- |
| Work／task | 实际执行的工作、全限定任务 ID、负责角色和文件所有权 |
| Artifact | 将交付的文件、App／包、报告、数据或可审查变更 |
| Environment／procedure | 指定工具链、设备、网络、账号范围、输入和可复现步骤 |
| Acceptance | 唯一验收 ID、明确预期与 PASS／FAIL 判定；实现成功、安装成功与业务／设备通过分别判断 |
| Evidence | 完整源码 SHA、命令结果、产物／哈希、脱敏日志／截图和原始摘要出处 |
| Stop／blocked | 缺少哪个条件、失败如何保留状态、哪些节点不能继续、如何解除 |
| User gate | 开工授权与阶段结果／下一阶段批准分别记录，范围改变回到用户决定 |

Task issue 映射阶段验收 ID，并说明工作、产物和验收所需证据。独立 reviewer 检查上述字段和所有阶段一致性；缺少工作或可判定验收阻止计划技术 PASS。GitHub 说明是当前计划摘要，不可保留与版本化文档冲突的旧阶段要求。

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
| Milestone criteria | 对应阶段验收 ID、任务交付产物、PASS 判定和证据链接 |
| Stop conditions | 何时上报／重新规划／等待用户决定 |

依赖满足后才开始实现。无依赖的计划、读资料和环境调查可以并行，但必须标明其范围。依赖 PR 合并后更新依赖者到新的基线，重跑受影响的验收和 review；旧提交的 PASS 不覆盖新提交。

迁移现有任务时保留 issue 编号与历史 ID，记录旧阶段／范围、旧完整 SHA、新阶段／任务、拆分关系和原因。移动或 superseded 不等于完成；拆分 meta 入口不得冒充实施任务，阶段验收只依据实际子任务。变更依赖后逐项核对 start／final integration 及反向 Blocks，并检查整张 DAG 无环。

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
Deliverable artifacts and mapped milestone acceptance IDs:
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

范围变更后的审查记录列新文件快照／完整 SHA。旧 PASS 只适用于原 head，旧报告明确标记历史范围并保留结论；新增计划、DAG 和远端登记必须重新独立审查。发布修订后 PR 与用户关口指向新的可访问计划和审查，不把历史 PASS 表示为当前通过。

## 验收与产物

验收记录绑定完整源码 SHA、命令、工具链、设备／系统、输入、期望和实际结果，并区分 `PASS`、`FAIL`、`BLOCKED`、`NOT_RUN`。必要的 USB 真机、飞牛和 tsnet 结果各自列证据来源；一般模拟器测试不证明 USB／供电或真实性能。

M0 的 Dora Android 物理设备必须先有受控 SMB 和可达拓扑，再执行真实 probe 的连接、完整内容读回和取消／终止不误完成验收；连接与内容传输分开报告。当前硬 gate 为 M0-V01–V05：真实 APK/AAR、bridge、host `net.Conn` no-replace 与读回、Dora App 内 tsnet 至少一次完整 SHA-256 读回，以及本地副本保留。>4 GiB 压力、竞争和完整故障／身份矩阵属于 `OPTIONAL`／`CONDITIONAL`／`NOT_RUN` 附加结果，不成为当前协议 gate。受控 tsnet 路径不表示局域网已测，合成文件和云端物理设备不证明 Pocket OTG／指定 Pixel／飞牛。每个租约和每次操作遵守上级独立 session ID／cleanup 规则；当前规划修订不执行这些实验。

M1 只验收 Android 实际 App、源只读／完整副本／远端读回、规则、人工暂停、`on_open` 基本恢复和指定 Pocket／Pixel／飞牛主链路；缺真实 USB／目标条件时不把完整链路标通过，不以 macOS／iOS 条件阻塞它。M1 的容量边界和基本安全行为按当前计划记录，完整故障注入（断网、校验不一致、边界 kill、重复插拔等）移至 M2；M2 只做 Android 自动模式／完整故障矩阵及发行准备。M3 才验收 iOS 原生 App、来源／副本／传输／恢复、macOS／Xcode／签名安装和指定 Pocket／iPhone USB-C，以及双平台最终交付。将来源、手机、目标、网络方式和产物分别列证据，云端普通网络／UI 结果不能替代指定相机链路。

当前规划阶段验证文档、示例 JSON、仓库准备及概念事实／覆盖／可读性；生成概念不是实现或验收。文档 CI 不能命名或报告为 Android 构建通过。真实应用存在且对应里程碑批准后，Android Actions 应构建并保存可安装的 APK；AAR 是桥接中间产物，不是 APK 验收。

```text
Artifact:
Source full SHA / PR / Actions run:
App version / version code / debug or release:
Platform / ABI / supported OS or SDK:
Android toolchain and/or macOS / Xcode / iOS toolchain versions:
Go / mobile binding versions:
APK or iOS app artifact SHA-256 / download or installation path:
iOS signing / device registration / installation prerequisites (references only):
Install/launch verification environment and result:
Device/integration tests and explicitly untested scope:
```

debug APK 明确不是正式发布。签名密钥、SMB 凭据、Tailnet 登录状态等只使用受保护的本地／CI secret 引用；不进入源码、产物、日志或截图。用户已选择 [Apache-2.0](../LICENSE)；应用标识和正式分发渠道仍须用户决定，公开仓库和构建通过不代替这些决定。

## 里程碑交给用户的审阅包

主代理提供本里程碑的最终计划、所有关联 issue／PR、最终 SHA、独立 review 与修复、验收与真实环境、APK 或其他真实产物、未测范围、未决项以及下一阶段的具体范围／停止条件。明确请求用户决定 `approve_next`、`revise` 或 `hold`，记录批准覆盖哪个 milestone 和计划版本。用户决定前保持关口，继续范围内的整理及问题修复，不开始下一阶段实现。

审阅包覆盖该 milestone 的每个验收 ID，不用少量代表性截图代替完整判定。M0 包只报告其受控服务／Dora 协议范围；M1 的 Android 基础版／真实 USB、M2 自动模式／Android 发行准备、M3 iOS／双平台交付各按当前阶段计划判定；另列设计接受和视觉对照状态。任何缺失、豁免或受限版本接受都需用户明确决定，不能因迁移任务而默认放宽。
