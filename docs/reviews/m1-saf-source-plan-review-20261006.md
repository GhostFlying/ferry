# M1 SAF 来源计划独立审查 — 2026-10-06

- 对象：`docs/plans/m1-saf-source-20261006.md`，以及 `docs/plans/m1.md`、`docs/platform-evidence.md`、`docs/product-plan.md` 的修订。
- 基线：`main` `de9edcd`；分支 `feat/m1-saf-source`。
- reviewer：fresh 独立 subagent，只读；作者为主代理。审查要求包括检查过度设计和对不太可能边界的过度防御。

## 第 1 轮：FAIL

P1：
- 来源阶段在 `upload == null` 时不会运行，且会在主线程执行。
- AV06 在本任务中不可达。
- `ConfigStore` 不存在。
- 单测缺少 JVM 可替换的接缝（gomobile AAR、`org.json`、`UsbManager` 在 JVM 不可用）。
- 每次打开会重导整卡，且崩溃窗口未处理。
- OTG 文字状态仍违反 UI 概念关口。
- `PocketNeedsOtg` 只会闪现。

P2：
- `SourceBinding`／Room 迁移属过度设计。
- 文件所有权超出 M1-D1。
- 续导验收不可判定。

P3：`uses-feature` 非必要；动态 receiver 可选；声明大小为 0 的处理。

## 修订

均已按发现修订：
- `ConfigStore`（SharedPreferences）替代 Room 表，并加 debug 专用注入。
- 引入 `SourceTree`／`Planner` 接口和纯函数 `PocketState`。
- 空计划不算失败。
- spool 以相对路径为键，使用独立 `.partial/` 目录，并定义跳过／重登记规则。
- 来源阶段与 uploader 无关，在 IO 线程运行。
- AV06 标为 `NOT_RUN`。
- 提示状态在会话内保持。
- OTG 提示复用 A03 v2 警示块，文案待用户确认。
- 记录所有权例外。
- 去掉 `uses-feature`。

## 第 2 轮：PASS

无剩余 P1／P2，未新增过度设计。三个可选 P3 已并入计划：
- 尺寸不符文件下次重试并计数；
- debug receiver 的 exported 与 `-n` 调用方式；
- `revision` 来源为 `ConfigStore` 保存计数。

reviewer 建议不为两张卡同名相对路径增加处理（Pocket 文件名含时间戳）。
