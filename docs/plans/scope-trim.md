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
- 用 `rg` 检查上述六份计划与必要架构/产品文档：M0 V01–V05、M0-UI、M1/M2 分工、`NOT_RUN`/附加条件、保留的安全证据和用户 gate 彼此一致；检查没有把 >4 GiB、竞争、完整故障注入、视觉或第二网络路径写成 M0/M1 硬 gate。
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
