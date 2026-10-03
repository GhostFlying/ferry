# Android 工具链约束独立复核

日期：2026-10-03。类型：非作者的 affected-only 文档复核；不是 M0 实现验收、设备验收或用户 milestone 批准。

## 复核对象与基线

本记录固定复核链：

```text
0b8c87ec4fe42088ffd0d631831547872d40ab60  基线
0a4f89c2f658c2e4f396380a27e637fe9868f5ab  初始工具链修订
ed76c43a1e13869c7ae9f06c5cd36df0a92f8810  第一轮修订
 aac686b7db0a63aed8a797b81172983cb2488ca5  最终修订（本次对象）
```

本次只审查 `ed76c43` 到 `aac686b` 的受影响文件，并回读最终版本相关上下文：

- `docs/architecture.md`
- `docs/implementation-plan.md`
- `docs/plans/android-toolchain-constraint.md`
- `docs/plans/m0.md`
- `docs/plans/m1.md`
- `docs/plans/m2.md`
- `docs/plans/repository-bootstrap.md`

最终修订仅触及上述 7 个规划文档；没有应用代码、Actions、设备配置或许可证变更。

## 修复核对

### 宿主边界：PASS

`docs/implementation-plan.md:19-21`、`docs/architecture.md:24`、`docs/plans/android-toolchain-constraint.md:14-16` 和 `docs/plans/repository-bootstrap.md:30-34` 统一规定：

- 宿主只启动容器、建立显式设备连接／转发并取回脱敏产物。
- APK 安装命令由 devcontainer 通过显式连接／转发执行。
- 宿主不执行安装、构建、单测或静态检查，也不使用宿主 JDK／SDK／NDK／Go／Gradle。
- 连接／转发本身不单独构成构建或应用验收证据。

M0、M1、M2 的阶段动作与验收行同步了这条边界：`m0.md:47,70`、`m1.md:56,80`、`m2.md:46,64`。缺少容器运行时仍为 `BLOCKED`，没有宿主回退路径。

### 固定工具链：PASS

`docs/implementation-plan.md:13-14`、`docs/plans/android-toolchain-constraint.md:41-46`、`docs/plans/repository-bootstrap.md:24-29` 及 M0/M1/M2 工作包要求 Dockerfile pin `FROM` 基础镜像 digest，并 pin SDK、JDK、NDK、Go、Gradle 和依赖版本，或使用等价的可审计 pinned manifest；未 pin 的 Dockerfile 不满足锁定要求。

### 证据字段：PASS

最终清单逐项记录：

- `minSdk`、`compileSdk`、`targetSdk`
- JDK
- Android SDK（含 platform/build-tools 等实际版本）
- NDK、Go、Gradle
- 依赖（含锁文件／依赖版本）
- ABI
- 完整源码 SHA
- 工具链版本
- APK/AAR 产物 SHA-256

字段在总计划 `implementation-plan.md:15-16`、架构 `architecture.md:24`、约束计划 `android-toolchain-constraint.md:43-47`、M0 `m0.md:13,24,30`、M1 `m1.md:43` 和 M2 `m2.md:17,40` 重复对齐。

## 范围与 gate 复核

没有引入 Android 9 及以下兼容矩阵、性能阈值、额外发布门槛或新的设备要求。M0 仍只有 V01–V05 协议硬 gate，V06–V08 仍为附加／条件性结果，V09 仍是安全收尾；M1 的第二网络、容量和完整故障矩阵仍按原计划分层并迁至 M2；M2 的完整故障矩阵、自动模式和发行准备边界未扩大。Dockerfile、清单和容器运行时要求只约束构建证据，缺失时标记 `BLOCKED`。

许可证、设计图接受与 milestone 用户批准仍保持独立；本修订没有将内部 review、提交或 CI 结果当作用户批准，也没有削弱凭据不泄露、完整内容 SHA-256、no-replace、源只读或完整副本保留边界。

历史计划中可能出现的 “clean Linux/runner” 文字仍位于明确标注为 `历史／superseded` 的文件，不属于当前 M0–M2 执行依据。

## 验证与未测边界

已执行：

- 读取 `ed76c43` 到 `aac686b` 的真实 diff，并回读最终 7 个受影响文档。
- `git diff --check 0b8c87e aac686b`：通过。
- 静态核对宿主职责、容器安装路径、pin 要求、证据字段、阶段 gate 和历史 clean-runner 指针。
- 确认 reviewer 工作树固定在 `aac686b7db0a63aed8a797b81172983cb2488ca5`，作者文件无修改。

未执行且本记录不能宣称通过：

- devcontainer 构建、APK/AAR、Go、Gradle、静态检查或单测。
- ADB、Dora、Pixel、Pocket、fnOS、SMB、Tailnet、USB、真实传输或发布签名。
- 用户设计接受、M0/M1/M2 milestone 批准或正式发布。

## 最终结论

**PASS。** `aac686b7db0a63aed8a797b81172983cb2488ca5` 已修复此前宿主边界、Dockerfile pin 和逐项工具链／产物证据字段问题；affected-only 文档范围检查未发现新的 P0/P1/P2 问题。

本 PASS 只表示工具链约束文档在该提交上通过独立静态复核，不表示 Android 实现、APK、Dora/SMB/USB、设备传输、UI 保真、发布签名或任何用户 milestone 已获批准。
