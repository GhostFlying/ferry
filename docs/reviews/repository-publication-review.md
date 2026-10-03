# 公共仓库与规划 PR 独立验收

> 历史发布验收：本报告记录原版已列 head 的仓库／PR／issue 状态。用户随后要求 M0 Dora 协议验证、iOS 移入 M1；新版文件和任务 DAG 须另做 [范围修订审查](milestone-scope-revision-review.md)，本报告 PASS 不自动覆盖后续远端修改。

- 审查时间：2026-10-02 08:54 UTC；远端状态是该次检查的快照。
- 作者／实施：`/root/repo_governance`；独立审查者：`/root/review_plan`。
- 模型／reasoning：以协调代理的任务分配记录为准；本报告不推断运行时配置。此行在范围修订时纠正原先未经核实的“继承配置”表述，不改变原发布验收证据。
- 结论：**PASS，限仓库发布、规划 PR、已审文件与任务登记。** 无未解 P0／P1／P2。本报告不批准 M0／M1，也不证明 App 或设备能力可用。
- 仓库：[GhostFlying/ferry](https://github.com/GhostFlying/ferry)。
- PR：[规划 PR #21](https://github.com/GhostFlying/ferry/pull/21)。
- 已审 base：`6e95a997054215fcaadb96570b26c1be531c1f59`。
- 已审 head：`b06b1fed92f6edc28f1eff56e11d12986cd27d38`。

本次通过 `gh repo view`、`gh pr view`、GitHub Git commits／milestones／Actions API 和 `gh issue list/view` 直接取得远端事实；本地 `git show`、`git ls-tree`、`git diff --check` 以及 SHA-256 核对补充内容验证。没有仅依据实施者摘要给出通过。

## 仓库、PR 与文件

| 项目 | 独立观察 |
| --- | --- |
| 公开状态 | 仓库为 PUBLIC，默认分支为 main。 |
| main 范围 | base tree 仅 `.gitignore` 与最小 README。分别匹配已审哈希 `59c1c29e6fba06ca8fe89cefe048b9a65578fe4a6d3c43b273854577e3cdbfbf`、`20b1a7e54013904cfdba6e96516e045bf0cd5ab3825f1816c7a4bcc8890d1991`。 |
| 远端对象一致性 | GitHub API 的 base tree 为 `dfc99fcb360cc36e373e9b977a8e25a325c6a93e`、head tree 为 `e17d51f9094caa9830af2e5342509d0c1e1682fd`，均与本地相同完整 commit 的 tree 一致。 |
| PR 状态 | #21 为 OPEN，非 draft，`mergedAt=null`、`autoMergeRequest=null`；base/head 与本报告所列完整 SHA 一致。 |
| 文件范围 | PR 相对 seed 改动 18 个文件，head 总计 19 个；范围为 Markdown、issue 表单 YAML、PR 模板、JSON 草案、LICENSE 和现有 `.gitignore`，没有应用、构建脚本、workflow 或二进制。 |
| 审查快照 | 对 13 项已审文件逐一计算 Git blob 内容的 SHA-256，与治理／技术审查最新快照一致；两份 review 报告、架构、平台证据和示例再与本次会话已读取的工作文件逐字节核对一致。 |
| 完整性检查 | `git diff base...head --check` 通过；没有把未审的实现代码夹带进规划 PR。 |
| LICENSE | head 的 LICENSE 为 11358 bytes，SHA-256 `cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30`；与本次会话独立下载并逐字节核对的 [Apache 官方标准文本](https://www.apache.org/licenses/LICENSE-2.0.txt)一致。 |
| Actions | 远端 Actions runs `total_count=0`，tree 无 workflow。该事实符合当前仅准备规划、尚未批准 APK Actions 的范围，不表示 CI 或 Android 构建通过。 |

对应快照和技术整改详情见 [治理／许可证审查](repository-bootstrap-review.md)及 [M0／M1 技术计划审查](m0-m1-plan-review.md)。

## 提交、身份与配置证据边界

直接读取六个可公开到达提交的远端 author／committer metadata，全部为 `GhostFlying <4019569+GhostFlying@users.noreply.github.com>`，没有 agent／bot／tool 作者或 co-author trailer。提交标题 type 全小写、描述具体，实际文件范围与目的吻合：

| Commit | 标题／范围 |
| --- | --- |
| `6e95a997054215fcaadb96570b26c1be531c1f59` | `chore: initialize Ferry repository for public plan review`；最小 seed 两文件。 |
| `d5b95209bb721e0cbe6a0de5e4ef43dcc6ab4db9` | `docs: add the user-selected Apache-2.0 license`；仅 LICENSE。 |
| `97eedd58ffee11f2df13948c1d1027ef06dea572` | `docs: define Android-first camera import requirements`；产品、架构、平台证据、示例。 |
| `fbcb53864ee8c7050f8b10d671e7a785bad40a56` | `docs: define independent reviews and milestone approval gates`；治理和模板。 |
| `9de20aa6f857039f87e940af0141f9ca139ca363` | `docs: propose M0 probes and M1 Android acceptance`；总计划、M0／M1 与完整 README。 |
| `b06b1fed92f6edc28f1eff56e11d12986cd27d38` | `docs: record independent planning reviews and publication authorization`；审查及授权记录。 |

独立现状检查确认：仓库没有 local `user.name`，local `user.email` 为用户明确授权的 noreply 地址，effective name 为 GhostFlying。没有在本报告公开原受保护邮箱。

关于“全局身份配置未改”的前后历史，实施者提供其事务证据：修改前在内存捕获 global name/email 和 effective name，只执行一次指定的 local email 写入，随后断言 global 两键及 effective name 与原值相同，结果通过；既有条件 include 在添加 GitHub origin 后匹配，解释 effective name 的来源。**这是实施者的前后记录，不是本审查者独立持有的全局文件前后字节快照。** 本审查者能独立确认当前配置和公开提交 metadata，不能将其扩大为整个 global 文件从未变化的独立证明。

## Milestones、任务与 DAG

已在实施者完成 issue body 更新后直接读取全部 20 条 issue。#1／#2 为 OPEN、正文状态 `awaiting-user-review`；#3–#20 为 OPEN、正文状态 `planned`，未实现、未实测。M0 milestone #1 为 open，11 条任务、0 closed；M1 milestone #2 为 open，7 条任务、0 closed。

| 范围 | Issue | 已核对的依赖与关口 |
| --- | --- | --- |
| 用户计划关口／仓库准备 | [#1](https://github.com/GhostFlying/ferry/issues/1)、[#2](https://github.com/GhostFlying/ferry/issues/2) | 规划等待用户；许可证及 noreply 已决定，不再列为待选；没有提前批准 M0。 |
| M0 A1–A6 | #3–#8 | A1 后并行桥接、SMB、来源和 CI 准备；A4 最终依赖 A3，A5 桥接依赖 A2，A6 完整验收依赖 A2／A4／A5。 |
| M0 B1–B3 | #9–#11 | B1 依赖来源探针及实体来源；B2 依赖 A2–A6／B1 和飞牛／Tailnet；B3 可先研究来源生命周期，完整验收依赖 B2。 |
| M0 X1／R0 | #12／#13 | X1 依赖 A2／macOS，未运行单列 iOS 风险；R0 必需依赖 A1–A6／B1–B3，不允许 M0B 缺失时宣布 M0 通过。 |
| M1 P1–P7 | #14–#20 | P1 依赖 R0 并另需用户批准 M1；契约后建壳，规则／状态／UI／CI 可依契约并行，最终集成与 P7 等待实际实现和验收。 |

独立内联检查将每条任务的 numeric start dependencies 和 final integration dependencies 与已审计划逐项比较，并验证反向 Blocks 恰好对应、依赖图无环、任务 milestone 归属正确。每条任务包括 owner role、文件范围、计划 worktree、base、验收、停止条件，以及固定到已审 head 的计划／review 链接。具体 agents／models／reviewer 仍须开工前分配，当前没有冒充已完成分工或实现。

PR #21 的 DAG 表与 issue 依赖一致。再次读取更新后的 #1 与 PR，确认用户批准后的顺序为：记录明确批准 → 合并规划使 AGENTS／协议／已批准计划进入 main → 从该已批准 main 建 M0 工作树。现在仍不合并、不从最小 seed main 开始应用实现。

## 未验证与后续范围

没有 App／AAR／APK／iOS 构建、安装、USB／Pocket 3／供电、SMB／飞牛、tsnet、真实设备或 Dora 验收。没有进行 GitHub issue form 的浏览器渲染验证；任务正文和权限／状态来自 API。许可证与仓库准备通过不能替代上述产品验收。

本报告由治理代理以独立文档提交加入同一规划 PR。该提交应只增加本报告，随后核对最终远端 head 相对上述已审 head 只有报告差异、metadata 合规、PR 仍 OPEN；不需要让报告不断自引用自身新 SHA。若加入其他文件或改变技术内容，应重新审查受影响范围。

当前仍停在用户审阅关口：M0A／M0B 等待用户明确批准 M0，M1 等待 M0 必需验收及用户另一次明确批准。
