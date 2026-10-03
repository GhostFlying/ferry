# Scope trim independent review preparation (2026-10-03)

状态：**待非作者 reviewer 读取真实 diff；不是 review 结论，也不代表用户批准或 M0 实现批准。** 基线为 `9441e92df0d8b8b2604e59869e343ff086b9a260`，工作树为 `../ferry-worktrees/scope-trim-20261003`。

## 变更目标

本次变更修复 fresh reviewer 指出的三类 P1：M0 把附加压力／竞争／扩展生命周期／视觉结果当成协议硬门槛；M0 UI 图集覆盖和接受状态错误地阻塞协议；每个任务重复 plan/review 造成不必要的审查依赖。同步收缩 M1 与 M2 的边界：M1 保留 Android `on_open`、源只读、完整副本、远端读回、规则、人工暂停和至少一条 Pocket→Pixel→fnOS 主链路；第二网络补充；完整故障矩阵移 M2。

## 作者变更文件与所有权

- `docs/plans/scope-trim.md`：执行计划，先于其余文件写入。
- `docs/implementation-plan.md`：阶段表、DAG、UI独立结果、执行／review模型与共同边界。
- `docs/plans/m0.md`：V01–V05 硬 gate、V06–V09附加／安全、M0-UI独立矩阵。
- `docs/plans/m1.md`：核心 AV01–AV04、AV06、AV09；AV05 为附加／条件性、第二路径补充、AV08移M2、AV12报告检查项。
- `docs/plans/m2.md`：完整故障矩阵、自动模式、发行准备与 `PASS`／`FAIL`／`BLOCKED`／`NOT_RUN`。
- `docs/architecture.md`：只澄清 M1 第一网络路径与第二路径补充、完整故障移 M2；未扩大 iOS/OpenDAL 架构。
- `docs/reviews/scope-trim-review-prep.md`：本准备记录。

不修改 `AGENTS.md`、应用代码、CI、设计图、issue、PR 或 GitHub 状态；不运行 App、Dora、ADB、SMB 或真实传输。

## Follow-up review scope (based on d7a737a)

本次后续修复以 `d7a737ac1949075a108b434fab95d2bf3b45e83d` 为父提交，新增／修订范围为：`docs/agent-workflow.md`、`docs/plans/android-first-design-revision.md`、`docs/validation/planning/android-first-design-checkpoint.md`、`docs/plans/repository-bootstrap.md`、`docs/implementation-plan.md`、`docs/product-plan.md`、`docs/plans/m1.md`、`docs/plans/m0.md`、`docs/architecture.md`、`docs/plans/milestone-scope-revision.md`、`docs/validation/planning/scope-revision-checkpoint.md`、`docs/plans/scope-trim.md` 及本记录。目标是把旧依据明确标为 history/superseded，保持当前 scope-trim 映射，并把 AV05 容量边界与 M0-V09 安全收尾从协议／主链路 gate 语义中分离。

## Reviewer 检查清单

1. 核对基线和完整 diff；确认 `scope-trim.md` 在其他编辑前已落盘，文件 owner 无冲突。
2. 检查 M0 只有 V01–V05 作为协议硬 gate；V03 有 host `net.Conn`、no-replace 和读回；V04 有 Dora App 内 tsnet 至少一次完整 SHA-256 读回；V05 有取消／终止不误完成和本地副本保留。
3. 检查 >4 GiB、Dora 竞争、扩展身份／持久状态、provider 生命周期和视觉结果标为 `OPTIONAL`／`CONDITIONAL`／`NOT_RUN` 或 M0-UI 独立结果，不能再出现在 M0 协议硬 gate。
4. 检查 M0 UI 仍要求完整图、用户接受、独立 UI plan review 和 native fidelity 证据，但未覆盖／未接受图只阻塞 UI 子任务。
5. 检查执行模型为每阶段一份 execution plan＋一次独立 plan review，作者没有自审；最终 integrated/device/release review 与用户 gate 仍存在。
6. 检查 M1 只把 `on_open` 基础恢复、源只读／完整副本／远端读回／规则／人工暂停、真实 Pocket→Pixel→fnOS 主链路和第一网络路径作为核心；第二路径与 AV05 容量边界为附加／条件性，不阻塞核心；完整故障注入明确移 M2；AV12 为报告检查项。
7. 检查 Dora 新 lease/session 每次操作核验、`finally` 清理、凭据不进日志／仓库、连接与内容分开、no-replace、SHA-256、`PASS`／`FAIL`／`BLOCKED`／`NOT_RUN` 和设计接受／milestone批准分开均保留。
8. 检查 architecture/product 叙述与阶段计划无矛盾；未来 iOS／OpenDAL 没有被前置为 M0–M2 实现依赖。
9. 运行文档级验证：`git diff --check`、`git diff --stat`、UTF-8／EOF 检查、验收 ID 及本地链接静态扫描；不把静态检查写成设备／协议通过。

## 停止和用户 gate

发现 P1、硬 gate 回退、核心安全边界删除、需要运行设备／应用或需要远端登记时，停止并报告具体文件／行和阻塞。作者提交后由非作者 reviewer 记录严重级别、证据、修复和复核；主代理把最终 SHA、变更文件、命令／结果、未测范围和远端登记影响交用户。用户明确批准 M0 前不开始 Android／Go probe、真实 APK 构建或 Dora/SMB 验收。
