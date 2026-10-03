# Scope trim 独立复核记录

日期：2026-10-03。类型：非作者文档变更独立复核；不是 M0 实现验收，也不是用户 milestone 批准。

## 复核对象与基线

本记录由独立 reviewer（`/root/scope_trim_reviewer`）编写，针对实际提交和完整 diff 复核：

```text
9441e92  ->  d7a737a  ->  6f2d840  ->  7761fff
基线        scope trim    历史/边界修复   M1 中断边界修复
```

- `d7a737a` 首次收缩 M0/M1 gates。
- `6f2d840` 修复治理入口、历史计划指针、M0-V09 安全收尾语义、M1 AV05 容量边界和架构阶段范围。
- `7761fff` 仅修订 `docs/product-plan.md` 第 7 项，消除 M1 基础中断行为与 M2 完整故障矩阵之间的歧义。
- 复核工作树固定在 `7761fff70aa76f8f744c66459a414485d2132276`，工作树无未提交修改。

复核读取了当前 M0/M1/M2 计划、总实施计划、产品方案、架构、协作协议、历史入口、scope-trim 执行计划及 review 准备记录，并逐项检查实际 diff。

## 初次复核发现

相对 `d7a737a` 的初次独立复核发现以下问题：

1. `docs/agent-workflow.md` 仍把旧 Android-first 计划作为当前依据，并把 M0 大文件、竞争和中断验收写成硬要求。
2. 旧 Android-first 计划、checkpoint、repository-bootstrap 入口和旧 review 指针没有明确标为历史或 superseded，可能让执行者复用旧 gates 或旧 PASS。
3. M1 核心 AV05 将容量不足、未知大小、单文件上限和保留副本占用列为核心，且未定义 `max_bytes`。
4. M0-V09 的安全清理虽为必须收尾，但原文容易被误读为协议 gate。
5. M1 计划中的 execution plan/review 关系与总计划表述略有歧义。
6. 架构中的持久身份/StateStore 描述没有明确与 M0 条件性证据分层。
7. 产品方案仍把断网、空间不足、校验、kill 和重复插拔行为放在 M1 描述中，和 M2 完整矩阵边界冲突。

## 修复复核

在 `6f2d840` 后确认：

- `agent-workflow.md` 已以 scope-trim 为当前依据，明确 M0 硬 gate 仅 V01–V05；>4 GiB、竞争和完整故障/身份矩阵是附加或 M2 结果。
- 旧计划、旧 checkpoint 和旧治理入口已标为 history/superseded，并链接当前 scope-trim；旧 review 明确为历史范围。
- M1 AV05 已标为 `OPTIONAL/CONDITIONAL`，不阻塞 AV01–AV04、AV06、AV09。
- M0-V09 已明确为强制安全收尾、失败即停止后续设备操作，但不属于 V01–V05 协议 gate。
- M1 execution plan 与最终 integrated/device review 已明确分工。
- 架构已按 M0 最小运行状态、M1 Keystore、M2 恢复矩阵、M3 Keychain 分阶段。
- `7761fff` 将产品方案第 7 项改为：M1 仅要求拔线、App 退出或权限失效时不误完成、可对账并保留完整副本；断网、空间不足、校验不一致、边界 kill、重复插拔等完整注入矩阵移至 M2，未运行项标记 `DEFERRED/NOT_RUN`。

## 低概率边界与安全底线

当前 M0/M1 计划未把以下低概率条件重新列为协议/主链路硬 gate：

- M0 >4 GiB 非稀疏压力、同步竞争、完整身份/持久安全、provider 全生命周期、未接受 UI。
- M1 第二网络、容量/未知 size/上限/保留副本压力穷举、完整断网/校验/边界 kill/重复插拔矩阵、文档重建。

仍保留且未被错误删除的安全底线：

- 真实 APK/AAR、host `net.Conn` 注入、Dora physical bridge/tsnet 受控 SMB。
- 完整内容 SHA-256 读回、连接与内容分开判定、SMB no-replace。
- 源只读、不完整副本不可上传、完整副本保留到远端读回和提交证据成立。
- 基本取消/终止不误完成、人工暂停不被 `on_open` 解除。
- Dora 新 lease/session 每次操作核验、`finally` 清理、凭据不进入日志/仓库。
- `PASS`/`FAIL`/`BLOCKED`/`NOT_RUN` 以及设计接受和 milestone 批准分开。

## 文档验证与证据边界

已执行：

- `git diff --check 6f2d840 7761fff`：通过。
- 相对 `d7a737a` 的完整 diff 与最终文件逐项阅读。
- UTF-8/EOF 检查：通过。
- 当前计划、产品/架构文档、历史入口和 review 指针静态核对。
- 当前 Markdown 相对链接检查：无缺失链接。

未执行且不能由本记录宣称通过：

- App、APK、Actions、Dora、ADB、SMB、Tailnet、真实传输。
- Pocket/Pixel/fnOS/iOS 设备验证。
- UI 生成、用户设计接受或 native fidelity 验收。
- GitHub issue/PR 更新或远端登记。

## 最终结论

**PASS。** `7761fff70aa76f8f744c66459a414485d2132276` 已修复初次复核列出的 P1/P2 文档一致性问题，当前 scope-trim 计划可合并到规划分支并交用户审阅。

本 PASS 只表示文档范围、gate 分层和安全边界在该提交上通过独立复核；不表示 M0 实现、APK、Dora/SMB 实验、M1/M2 实现或任何用户 milestone 已获批准。
