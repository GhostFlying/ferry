# Android 工具链与 devcontainer 约束计划

状态：**工具链约束文档已在 `aac686b7db0a63aed8a797b81172983cb2488ca5` 通过独立
审查 PASS；本次治理同步仍待非作者复核和用户里程碑审阅。** 详见 [scope-trim 独立
审查](../reviews/scope-trim-review.md) 与 [Android 工具链独立审查](../reviews/android-toolchain-review.md)。本计划基线仍为
`0b8c87ec4fe42088ffd0d631831547872d40ab60`；审查 PASS 只表示文档约束成立，不批准
M0 实现。本任务只修改文档，不启动应用、容器、设备或 GitHub 操作。

## 目标与边界

将 Android 的系统支持和构建证据边界写入当前 M0–M2 计划：默认
`minSdk 29`（Android 10），不处理 Android 9 及更低版本兼容问题；允许使用
较新的 `compileSdk`／`targetSdk`，但不在规划阶段擅自锁定一个随日期变化的 API
号。实现开始前，使用 devcontainer 内的稳定 SDK 工具链确定并记录实际版本。

所有构建、单测、静态检查、APK 产物和 Go／Android bridge 构建均在
devcontainer 内执行。宿主机只负责启动容器、做显式设备连接／转发和取回脱敏产物；
Dora 设备使用容器构建出的 APK，安装命令由 devcontainer 内通过该显式连接／转发
执行。宿主不执行安装、构建、单测或静态检查；连接／转发不单独构成构建或应用验收
证据。

本计划不引入 Android 9 及以下的兼容矩阵，不提前增加发布签名／渠道门槛，也不
改变 M0–M2 的设备、SMB、USB 或用户批准关口。iOS／macOS 仍属于 M3，本任务不
修改其实现范围。

## 文件范围与所有权

- 新增本文件，作为后续文档修订的执行计划和验收依据。
- 最小修改 `docs/implementation-plan.md`、`docs/plans/m0.md`、
  `docs/plans/m1.md`、`docs/plans/m2.md`、`docs/plans/repository-bootstrap.md`，
  补充统一工具链、容器内构建和阶段内外验证边界。
- 如现有“最低版本待定”或“干净 Linux 构建”表述与上述约束冲突，再最小修改
  `docs/product-plan.md`、`docs/architecture.md`；不修改代码、设计图或 GitHub 记录。
- 2026-10-03 治理收尾引用本计划在 `aac686b7db0a63aed8a797b81172983cb2488ca5`
  上的 [独立工具链审查 PASS](../reviews/android-toolchain-review.md)：仅追加项目手写
  `AGENTS.md` 的简短 Android 工具链条款，并同步 `docs/implementation-plan.md` 与
  本文件的实际审查状态。三文件归本任务单 owner；不复制完整清单，不改变范围、UI、
  验收或用户 gate。新提交仍由非作者复核，审查 PASS 不批准 M0 实现。

## 依赖与实现步骤

1. 以本文件固定目标、文件范围、验收和停止条件。
2. 对照当前阶段计划，删除或改写会把宿主机／日期相关 `latest` 版本当成证据的
   表述，统一为 devcontainer 锁定的稳定工具链；把 `minSdk 29` 和
   `compileSdk`／`targetSdk` 的“实现前确定并记录”写入 Android 范围。
3. 在各阶段列出容器内动作（构建、单测、静态检查、Go／bridge、APK 及哈希记录）
   与容器外动作（Dora／Pixel／Pocket／fnOS／SMB、ADB 显式转发、宿主保存脱敏
   产物）的边界，并保留现有验收 ID 和用户 gate。
4. 写明 devcontainer 的设计验收：Dockerfile 必须 pin `FROM` 的 base image digest
   以及 SDK／JDK／NDK／Go／Gradle／依赖版本，或使用等价的可审计 manifest；仅提供
   未 pin 的 Dockerfile 不满足锁定要求。依赖缓存使用可重建卷；toolchain manifest／
   产物清单逐项记录 `minSdk`、`compileSdk`、`targetSdk`、JDK、SDK（含平台／
   build-tools 等实际版本）、Gradle、NDK、Go、依赖（含锁文件／依赖版本）、ABI、
   完整源码 SHA、工具链版本、产物 SHA-256；缺少容器运行时即 `BLOCKED`，不得回退
   到宿主环境。
5. 做文档级一致性检查并提交单个 conventional commit；不运行代码、设备、容器或
   GitHub 命令。
6. 治理收尾先登记上述三文件范围，再将已审查的工具链约束同步到项目手写规则，
   引用 [scope-trim 审查](../reviews/scope-trim-review.md) 和
   [工具链审查](../reviews/android-toolchain-review.md) 的实际 PASS／适用提交，明确
   仍等待用户批准 M0；检查三文件 diff 和本地链接后原子提交，交非作者复核。

## 验收与证据

- `rg` 检查上述文件都明确 `minSdk 29`、devcontainer 内构建／测试／安装边界、宿主机
  仅启动容器／显式连接或转发设备／取回脱敏产物、安装命令由容器执行、Dora 使用
  容器 APK、工具链／ABI／产物哈希记录及“缺容器运行时为 BLOCKED、不得回退”；宿主
  连接／转发不单独构成构建或应用验收证据。
- 检查 M0、M1、M2 各自区分容器内验收和设备／服务外部动作；没有把容器引入低
  概率兼容矩阵或新的发布门槛。
- 检查 Markdown 链接和表格仍可读，`git diff --check` 通过；记录修改文件和
  提交的完整 SHA。上述检查只证明文档一致性，不证明容器、APK、Go、Android、
  USB、SMB、Dora 或发布验证已通过。
- 治理收尾仅修改 `AGENTS.md`、`docs/implementation-plan.md` 和本文件；规则的工具链
  条款与已审查约束一致，状态链接指向真实 PASS 记录并明确等待用户批准 M0。

## 停止条件

- 发现容器运行时、已 pin 的 Dockerfile／可审计 manifest 或可重建依赖卷尚未提供时，
  只将实现／构建节点标为 `BLOCKED`，不在宿主机尝试构建或用宿主产物替代。
- 设备安装需要执行命令时，必须由 devcontainer 通过显式设备连接／转发完成；宿主
  不执行安装、构建、单测或静态检查。否则停止对应验收并标为 `BLOCKED`。
- 发现文档修改会改变现有 M0–M2 功能范围、设备要求、数据保留、用户 gate 或
  M3 边界时停止并交主代理重新审阅；不借本任务启动实现。
- 治理收尾超出上述三文件或改变其他产品／UI 范围时停止并交主代理调整所有权；
  不修改代码、Actions、设计文件、GitHub issue／PR，也不运行任何代码、设备、Dora
  或容器验证。

## 用户 gate

本计划及其文档修订完成独立计划／实现审查后，交用户审阅当前 Android 工具链
约束和 M0–M2 验收边界。用户明确批准对应 milestone 前，不开始 Android／Go
实现、真实 APK 构建、Dora 设备验证或发布准备；内部 review、提交或 CI 不代替
用户决定。
