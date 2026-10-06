# M1 SAF 来源与 Pocket OTG 引导实现计划 — 2026-10-06

状态：**已获用户批准，实施进行中**。用户在 2026-10-06（本会话）批准按本计划实施；
OTG 提示文案未直接接受，要求先出概念图，因此该 UI 状态在概念图被接受前不实现，
`PocketState` 逻辑与测试照常实现。审查记录见 [m1-saf-source-plan-review-20261006](../reviews/m1-saf-source-plan-review-20261006.md)。

## 背景与决定

2026-10-06 在指定 Pixel 6 Pro（Android 16／API 36）上读取到 Pocket 3 的 USB
枚举（DJI `2ca3:0020`，固件 5.04，接口为 Mass Storage、RNDIS 与 DJI bulk），
但连接数秒后移除，系统未出现存储卷。原因是 Pocket 3 未进入 OTG 模式。DJI 说明要求
在相机「下拉控制菜单 → 设置 → OTG 连接」后用 C-to-C 线连接，相机随后作为 USB
存储出现，素材位于 DCIM。用户确认进入 OTG 模式后 Pixel 能挂载为系统存储。

用户决定（2026-10-06，本会话）：

- M0 不变。M0 本来不验证 Pocket 读取，V02／V04／V05 与 Pocket 无关。
- M1 来源以系统挂载＋SAF 为主路径；USB Host 只用于检测 Pocket 接入并引导用户开启
  OTG。App 不自行实现 USB Mass Storage 或 exFAT 读取。`architecture.md` 的
  来源适配原则保持不变。
- 若后续证明目标手机／ROM 上 SAF 不可用，再单独评估 App 内 USB 读取，不在本任务。

## 目标、依赖与文件所有权

完成 M1-D1：已授权目录的只读扫描、规则计划、完整副本导入和启动对账，接入前台协调器，
并补充 Pocket 检测与 OTG 提示。上传执行器接线（M1-T1 进 Android）不在本任务。

- 基线 `main` `de9edcd`（PR #44 合并）。分支 `feat/m1-saf-source`，工作树
  `/data00/home/luchengxuan/WORKSPACE/ferry-worktrees/m1-saf-source-20261006`。
- 实现 owner 为主代理，独立 reviewer 只审查。
- **所有权例外**：`m1.md` 中 M1-D1 只拥有 `source/`、`spool/`。本任务需要把来源
  阶段接入运行时，因此还修改 `data/`、`lifecycle/`、`config/`、`ui/`、
  `MainActivity.kt` 和 debug source set。当前没有其他进行中的任务持有这些文件。该例外同步记在 `m1.md` 的
  M1-D1 行。

## 实现

### 配置与来源绑定

- 新增 `config/ConfigStore.kt`：用 SharedPreferences 保存一个 `Config` JSON 和
  来源 tree URI。写入前经 `ValidateConfigJSON` 校验。M1 只有一个来源，不新建
  Room 表，不做 schema 迁移。
- tree URI（如 `…/tree/XXXX-XXXX%3ADCIM`）本身含卷 ID，持久授权只在同一卷上可用。
  所以「识别同一张卡」定义为：重连后已保存的 URI 无需重选即可枚举。
- 新增 debug 注入入口：`android/app/src/debug/java/.../DebugConfigReceiver.kt`
  和 `src/debug/AndroidManifest.xml`。它设为 `android:exported="true"`，用 `adb shell am broadcast -n <pkg>/.DebugConfigReceiver` 写入配置 JSON；
  release 构建不包含该入口，通过检查 release 合并后的 manifest 确认。

### 来源读取与计划（可在 JVM 上单测的接口）

- `source/SourceTree.kt`：接口为 `list(): List<SourceFile>` 和
  `open(relativePath): InputStream`。
  - 实现 `SafSourceTree`：用 `OpenDocumentTree` 的结果，只调用
    `takePersistableUriPermission(READ)`。
  - 使用前检查 `persistedUriPermissions` 和根目录是否可枚举。
- `source/Planner.kt`：接口为 `plan(config, files): List<SourceFile>`。
  - 实现 `BridgePlanner` 调用 gomobile 生成的 `bridge.Bridge.planJSON(String)`。
  - Go 返回 "no source files matched" 时按空计划处理，不算失败。

### 导入与续导

- `SourceImportCoordinator` 改为对计划中的文件逐个导入。
  - spool 路径以来源相对路径为键：`spool/<relativePath>`。
  - 临时文件放在独立的 `spool/.partial/` 目录。
  - 启动对账只清空 `.partial/`，可以解决 PR #44 审查中的同名冲突。
- 每个来源文件在导入前按以下规则处理：
  1. 已有任意阶段的 operation 行：跳过并计数。
  2. 完整副本已存在但没有行（rename 之后、登记之前崩溃）：重新计算 SHA 并登记，不再复制。
  3. 其余情况：复制到 `.partial/`，同时流式计算 SHA-256，`fd.sync` 后 rename 到 spool，再登记。
- 读取字节数必须等于来源声明的大小。
  - 声明大小为 0 但实际读到非零字节时，按不一致处理。
  - 不一致时删除 partial，本文件失败，继续处理下一个文件。这类文件没有 operation 行，
    下次打开时会重试；失败数量计入来源阶段状态。
- `OperationRepository.createIntent` 改为接收完整副本的 SHA-256，不再写入空值。
  `revision` 取 `ConfigStore` 每次保存配置时递增的计数，便于追溯任务使用的配置。
  登记后的行会出现在 `findEligible()` 中。
- `OperationDao` 增加按 `sourcePath` 查询。

### 前台协调器

- `runLoop` 改为两个阶段：先运行来源阶段，再运行上传阶段。
  - 来源阶段与有无 uploader 无关；`upload == null` 时只跳过上传阶段。
  - 来源阶段是 worker 循环中的一个 phase，运行在 `Dispatchers.IO`，由 worker
    Job 取消：`onStop` 的 `cancelAndJoin` 会结束它。
  - `pause` 只针对上传 action，不影响来源阶段。
- 来源阶段每次 `onOpen` 运行一次：对账 → 检查授权 → 扫描 → 计划 → 导入。

### Pocket 检测与 OTG 提示

- `source/PocketState.kt` 是纯函数：
  `(pocketSeenThisSession: Boolean, treeAccessible: Boolean) -> SourceState`。
  - Pocket 本会话出现过且目录不可访问时返回 `PocketNeedsOtg`。
  - 该状态会一直保持，直到目录可访问。原因是未开 OTG 时 Pocket 只出现几秒。
- `MainActivity` 在 `onStart` 读取 `UsbManager.deviceList`：
  - 遇到 `2ca3:0020` 时记录 `pocketSeenThisSession`。
  - 动态注册 `USB_DEVICE_ATTACHED`，只用于设置这个标记。
  - 不打开、不 claim 设备，不修改 manifest 的 USB 声明。

### UI

- 来源页把「系统目录授权待接入」替换为目录选择入口和授权状态，属于已接受的 A03 v2 范围。
- OTG 提示复用 A03 v2 已接受的「目录访问权限已失效，请重新选择」警示块，只改文案：
  - 标题：「检测到 Pocket 3，但未以 OTG 方式连接」
  - 说明：「请在相机下拉菜单「设置 → OTG 连接」后重新连接数据线。」
  - 用户要求先出概念图（2026-10-06）；概念图被接受前不实现该状态的界面。

## 可判定验收

1. JVM 单测，使用 JUnit4 和手写 fake，fake 实现 `SourceTree`、`Planner` 和 DAO：
   - 只导入计划内的文件；空计划不报错。
   - 声明大小与读取字节数不一致时，该文件失败，并且不留下完整副本。
   - 来源阶段取消后没有新的完整副本；对账后 `.partial/` 为空。
   - 第二次运行时：已有行的 N 个文件被跳过（计数 = N），只导入缺失的文件；
     无行的完整副本被重新计算 SHA 并登记，不再复制。
   - 登记行的 SHA 与完整副本一致，且出现在 `findEligible()` 中。
   - `upload == null` 时来源阶段仍然运行。
   - `PocketState` 真值表：Pocket 出现过且目录不可访问 → `PocketNeedsOtg`；
     目录可访问 → 正常；Pocket 未出现过且目录不可访问 → 需要重新选择。
2. 在 devcontainer 内运行 `scripts/ci/android.sh` 通过，Actions 也通过；结果绑定最终源码 SHA。
   - release 合并后的 manifest 中没有 `DebugConfigReceiver`。
3. 指定设备（Pixel 6 Pro＋Pocket 3，通过用户提供的无线 ADB）：
   - 未开 OTG 时连接：显示 OTG 提示。
   - 开启 OTG 后：系统挂载，SAF 能选择 DCIM，计划列出文件，导入后逐文件比较完整副本
     SHA-256 与来源读取的 SHA-256。
   - 导入中拔线（AV04）：来源文件未被修改；重启后 `.partial/` 被清空。
     重连同一张卡后无需重选目录，已完成的文件被跳过，只导入缺失部分。
   - AV06（上传到 fnOS 并读回）：`NOT_RUN`，依赖后续把 T1 上传接入 Android。

## 停止与未测边界

- 进入 OTG 模式后指定手机仍不挂载，或 SAF 无法读取 DCIM：停止设备步骤并报告，再由
  用户决定是否评估 App 内 USB 读取。
- 设备不可连接、端口变化未提供或 Pocket 不稳定：对应项 `BLOCKED`，不使用 Dora 或
  普通 provider 替代指定 USB。
- 完整故障矩阵、写完／提交边界 kill 仍属 M2。
- 计划复审 PASS 并经用户确认（含 OTG 文案）后实施；实现完成后做独立实现审查。
