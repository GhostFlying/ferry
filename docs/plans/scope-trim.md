# Scope trim execution plan (2026-10-03)

状态：**执行计划，等待本次文档变更的独立 review 与用户 gate；不授权 M0 实现。**

## 目标

基于 `9441e92df0d8b8b2604e59869e343ff086b9a260`，落实已批准的 scope trim：保留 M0 协议与安全证据的不可替代硬门槛，移除会把低概率边界、未接受 UI 或重复审查工作错误地变成 M0 完成前置的要求。M1 收缩到 Android `on_open` 基础恢复和一条真实生产主链路，M2 承接故障注入、自动模式和发行准备；M3/iOS 与 OpenDAL 继续是后续边界或 backlog。

## 文件范围与所有权

本任务作者独占下列文件；不修改 `AGENTS.md`、应用代码、CI、设计图、issue、PR 或 GitHub 状态：

- `docs/plans/scope-trim.md`（本执行计划，先于其余编辑写入）
- `docs/implementation-plan.md`
- `docs/plans/m0.md`
- `docs/plans/m1.md`
- `docs/plans/m2.md`
- 必要时仅做与范围收缩直接相关的 `docs/architecture.md`、`docs/product-plan.md`
- `docs/reviews/scope-trim-review-prep.md`（供后续独立 reviewer 使用的准备记录）

不重写历史 review；若需要说明旧结论的适用范围，只新增当前范围映射或引用。

## 依赖与顺序

1. 基线必须是 `9441e92`；建立独立工作树 `../ferry-worktrees/scope-trim-20261003`，先确认无用户修改。
2. 本文件落盘后，按本计划编辑阶段文档；保持每一项硬证据的可追踪 ID，不删除核心安全边界。
3. 作者完成文档交叉检查后，交给**非作者**独立 plan/revision reviewer；reviewer 需检查真实 diff 与 P1 修复，而不是只读作者摘要。
4. reviewer 结论、完整提交 SHA 与文档级验证结果交给主代理，供用户审阅；在用户明确批准前不启动 M0/M1/M2 实现、不占用 Dora、不运行 App/SMB、不同步 GitHub。

## 计划变更要求

- M0 硬 gate 只保留 V01–V05：真实 APK/Go AAR、Dora 安装/bridge、host 注入 `net.Conn` 的 SMB no-replace 与读回、Dora App 内 tsnet 到受控 SMB 至少一次完整内容 SHA-256 读回、取消/终止不误完成且本地完整副本保留。连接建立与内容传输分开判定。
- >4 GiB 压力、Dora 竞争、完整身份重试/持久安全、provider 生命周期、视觉保真改为后续或附加证据；低概率边界明确标为 `OPTIONAL`/`CONDITIONAL`/`NOT_RUN`，不得继续作为 M0 协议完成 gate。
- M0-V10 改为独立 M0-UI 结果；UI 仍必须先生成完整图、取得用户接受并经独立 UI review，但图集未全覆盖或尚未接受不能阻塞 M0 协议 gate。
- M0 采用一份 execution plan + 一次独立 plan review；保留最终 integrated/device review 与用户 gate，去除每个 task 重复的全套 plan/review 要求。
- M1 只保留 `on_open` 基础恢复、源只读/完整副本/远端读回/人工暂停、规则、真实 Pocket 3 → Pixel 6 Pro → fnOS 主链路与至少一条网络路径；完整故障注入移 M2，第二网络路径作为补充证据，AV12 改为交付报告检查项。
- 必须保留：真实 APK/Go AAR、host `net.Conn` 注入、Dora Android 物理设备内 tsnet→受控 SMB、至少一次完整内容 SHA-256 读回、连接/内容分开判定、取消/终止不误完成、no-replace、M1 源只读/完整副本/远端读回/人工暂停、Dora 新 lease/session 校验/finally 清理、凭据不进日志/仓库、`PASS`/`FAIL`/`BLOCKED`/`NOT_RUN`、设计接受与 milestone 批准分开。
- `architecture.md`/`product-plan.md` 只在必要处反映同一边界；不提前实现 iOS/OpenDAL，不扩大架构 diff。M3 细节可保留，但明确不是 M0–M2 前置。

## 验收与验证环境

### 文档验收（本任务可执行）

- 在独立工作树执行 `git diff --check`、`git diff --stat`、`git status --short`。
- 用 `rg` 检查当前计划、产品／架构文档及历史入口：M0 V01–V05、M0-UI、M1/M2 分工、`NOT_RUN`/附加条件、保留的安全证据和用户 gate 彼此一致；检查没有把 >4 GiB、竞争、完整故障注入、视觉或第二网络路径写成 M0/M1 硬 gate。
- 逐项阅读最终 diff，确认没有凭据、节点状态、真实配置、App/设备/SMB 运行证据，也没有删除历史迁移关系。

### 后续阶段验收边界（仅记录在计划中，不在本任务运行）

- M0 实现批准后，真实 APK/AAR 与 Dora physical 新 lease 实验按 V01–V05 逐项出证；host 与 Dora 的连接、内容读回分别记录。
- M1/M2/M3 按各自计划、设备和用户 gate 执行；本任务不运行任何 App、Dora、ADB、SMB 或真实传输。

## 停止条件

- 基线不是 `9441e92`、工作树已有用户修改、文件 owner 冲突或无法保持原子文档范围时停止并上报。
- 发现需要应用代码、CI、设计生成、Dora、SMB、GitHub/PR 操作才能完成收缩时停止；将其列为后续依赖，不在本提交中越权实现。
- 任何修改会删除源只读、完整副本、远端读回、人工暂停、no-replace、SHA-256、lease/session/finally、凭据隔离或状态判定边界时停止并回退该部分。
- 若独立 reviewer 发现仍有 P1 或范围矛盾，先修订文档并重新 review；不得把旧 review/旧 SHA 当作当前通过。

## 用户 gate

本提交只交付 scope-trim 文档与审查准备记录。主代理须向用户提供计划 diff、提交 SHA、独立 review、文档验证命令/结果、未测范围和远端登记影响；只有用户明确批准 M0，才可进入 Android/Go probe、真实 APK 构建和 Dora/SMB 验收。批准本次范围修订不等于批准实现、发布或 M3/iOS。

## Follow-up remediation execution plan (post-review)

基于上一提交 `d7a737ac1949075a108b434fab95d2bf3b45e83d`，本次只修复独立 reviewer 列出的 P1/P2 文档一致性问题，不扩大产品或实现范围。

### 文件范围

作者继续独占以下文件：`docs/agent-workflow.md`、`docs/plans/android-first-design-revision.md`、`docs/validation/planning/android-first-design-checkpoint.md`、`docs/plans/repository-bootstrap.md`、`docs/implementation-plan.md`、`docs/product-plan.md`、`docs/plans/m1.md`、`docs/plans/m0.md`、`docs/architecture.md`；为保持历史入口与 reviewer 清单一致，另仅更新 `docs/plans/milestone-scope-revision.md`、`docs/validation/planning/scope-revision-checkpoint.md` 顶部 history 标记及 `docs/reviews/scope-trim-review-prep.md` 的 AV05/核心映射。本文件补充本计划。禁止修改 `AGENTS.md`、设计图、应用代码、CI、GitHub 或远端登记。

### 依赖与动作

1. 先标记旧 Android-first 计划、checkpoint 和 repository-bootstrap 依据为历史／superseded，给出当前 scope-trim 映射，避免旧大文件、竞争、空间、kill gate 被执行。
2. 再同步当前实施计划和产品计划：旧 review 只作历史引用；M1 基本安全与 M2 完整故障注入边界清楚；M1 第二路径和低概率容量边界不成为核心 gate。
3. 明确 M0-V09 是强制安全收尾和失败即停止后续设备操作，但不计入 V01–V05 协议 gate；澄清 M1 统一 execution plan／一次 plan review 不排除最终 integrated/device review。
4. 给架构中的持久身份／加密 StateStore 标注阶段范围：M0 只保留协议所需最小状态，扩展持久保护在 M1/M2 条件性验证，不能把全量身份安全测试回填为 M0 gate。

### 验收与环境

仅做文档验证：`git diff --check`、UTF-8/EOF、Markdown 表格列数、本地链接、旧 gate 静态扫描和当前边界关键字断言。不得运行 App、Dora、ADB、SMB、真实传输或 GitHub 操作。

PASS 条件：旧依据文件顶部明确 `superseded/history` 且链接当前 scope-trim；agent-workflow 不再把 M0 大文件／竞争／完整中断写成当前硬 gate；M1 AV05 标为附加／条件性或明确不阻塞核心，同时保留源只读、完整副本、远端 SHA-256 和人工暂停；当前计划、产品、架构和审查准备记录互相一致；所有敏感信息边界仍保留。

### 停止条件与用户 gate

若发现需要重写历史结论、改变 M0-V01–V05、删除安全边界或同步远端登记，停止并上报。修复提交只供独立 reviewer 复核；独立 review、CI、合并或旧 PASS 不替代用户对当前范围和后续 M0 实现的批准。
