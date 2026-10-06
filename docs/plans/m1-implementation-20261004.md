# M1 Android implementation plan — 2026-10-04

状态：**已获用户批准，实施进行中。**

用户在 2026-10-04 明确批准 M1 计划；计划 PR #43 已合并到 `main`，合并提交为 `d011ca5fe3751b56ac951e0b51c3eba461973daf`。本文件是开始编写应用代码前的实现计划，覆盖 M1 计划中的 P1–P6、M1-D1、M1-T1、P5 和交付准备。Pocket／Pixel／fnOS 真实设备验证只有在可安装 APK 和对应服务条件具备后执行；未执行的设备结果保持 `NOT_RUN`／`BLOCKED`。

## 目标与停止条件

交付 Android `minSdk 29` 的 `on_open` 前台 App、可测试的 Go 核心契约、只读来源导入、完整副本保留、确定性规则、持久任务状态、人工暂停／系统等待、第一网络路径传输契约、devcontainer 构建和真实 debug APK。M1 不实现 FGS／自动接入、不实现 iOS、不把第二网络路径或完整故障矩阵变成硬门槛。

遇到以下条件立即停止受影响任务并记录证据：容器运行时或锁定工具链不可用；共享构建配置发生跨任务冲突；没有清晰的文件 owner；Go／Android bridge 契约无法保持结构化错误和取消完成；无法证明完整副本与远端 SHA-256 读回；没有真实 Pocket／Pixel／fnOS 或受控 SMB 条件时不得声称设备链路通过。

## 依赖图与执行顺序

1. **P1 contracts**：冻结 Android 数据／错误／状态契约、规则向量和第一网络路径假设。
2. **P2 bootstrap**：建立 Kotlin／Compose Android 工程、唯一 Go module、bridge 入口、devcontainer 和可安装 debug APK。
3. **P3 rules** 与 **P4 state**：在 P2 骨架可构建后分别实现纯 Go 规则、Android Room／Keystore／状态迁移。
4. **M1-D1 source** 与 **M1-T1 transport**：使用已冻结契约实现只读导入／spool／哈希和第一网络路径的上传／读回／no-replace 适配；两者不得编辑同一文件。
5. **P5 UI＋executor**：只有已接受的 M1 概念和 UI plan review 覆盖的界面才实现；接入真实核心，保持人工暂停和 `on_open` 恢复语义。
6. **P6 delivery**：接收 P2 的 devcontainer 文件所有权，在容器内执行单测、静态检查、APK 构建和产物清单。
7. **M1-V1／B1／B2**：最终候选 SHA 固定后，再分别记录 Pocket／Pixel 来源和 fnOS 第一网络路径；没有设备或服务条件时只记录阻塞。
8. **P7 独立实现审查**：由非作者 reviewer 检查最终提交、真实变更和证据；主代理修复后再提交用户 M1 结果，不能直接打开 M2。

## 文件范围与交付物

| 阶段 | 文件范围 | 交付物 |
| --- | --- | --- |
| P1 | `docs/contracts/`、`docs/validation/m1/environment.md` | 版本化契约、字段／错误／状态说明、规则 fixtures、设备与服务前提 |
| P2 | `android/`、`go/`、`mobile/`、`devcontainer/`、`scripts/build-android.*` | Android skeleton、Go bridge 入口、锁定工具链 manifest、可安装 debug APK |
| P3 | `go/core/model/`、`go/rules/`、`go/planner/`、`go/mobile/contract/` | 确定性匹配、排除优先、路径计划、规则向量和结构化错误 |
| P4 | `android/app/src/main/.../data/`、`model/`、`security/` | Room schema／迁移、revision、操作意图、人工暂停、Keystore 保护的凭据引用 |
| M1-D1 | `android/app/src/main/.../source/`、`spool/` | SAF 只读导入、流式 SHA-256、完整／partial 副本、空间检查和启动对账 |
| M1-T1 | `go/storage/`、`go/network/`、`mobile/transport/` | 临时远端对象、flush／读回／no-replace、取消和文件级恢复契约 |
| P5 | `android/app/src/main/.../ui/`、`navigation/`、`lifecycle/`、`execution/`、`res/` | 接受图覆盖的 Compose UI、单一前台 executor、`on_open` 和暂停语义 |
| P6 | `.github/workflows/android.yml`、`scripts/ci/`、`docs/build-android.md` | Actions 构建、源码／工具链／ABI／APK SHA-256 清单和安装步骤 |
| B1/B2/V1 | `docs/validation/m1/` | 真实设备／服务报告、未测边界、清理记录、M2 迁移和最终验收报告 |

## 环境与验证

- 所有 Go／Android／bridge 构建、单测、静态检查和 APK 生成在固定 devcontainer 内执行；宿主只启动容器、提供显式设备连接／转发和取回脱敏产物。
- Android 固定 `minSdk 29`；容器实际 `compileSdk`、`targetSdk`、JDK、SDK／build-tools、NDK、Go、Gradle、ABI 和依赖版本写入 manifest。
- 先运行容器内单测与静态检查，再构建 debug APK；APK 必须记录完整源码 SHA、版本、ABI 和 SHA-256。
- 设备实验前重新查询并占用本会话自己的 Dora lease；每次 ADB／连接／释放前校验 session ID，使用 `trap`／`finally` 清理。没有真实 Pocket／Pixel／fnOS 条件时停止设备步骤，不用模拟器或普通 provider 替代指定 USB。
- 验收区分 `PASS`、`FAIL`、`BLOCKED`、`NOT_RUN`。文档、单测和 APK 结果不能替代 USB、SMB、远端读回或 native fidelity 证据。

## 实施验收

- **I01**：容器可重建 Android skeleton 和 Go bridge；`minSdk 29`、ABI、源码 SHA 和产物清单一致。
- **I02**：规则 fixtures 对 first-match、排除优先、大小写、目录边界、时区／缺失时间和未知 hash 给出确定结果。
- **I03**：状态迁移和 Room 事务保留 revision、人工暂停、系统等待；`on_open` 不解除人工暂停。
- **I04**：来源导入只读，完整副本和 partial 副本可区分；流式 SHA-256、空间检查和启动对账可测试。
- **I05**：传输适配器只在临时对象完整写入、flush、远端 SHA-256 回读及 no-replace 成功后报告完成。
- **I06**：Compose UI 只实现已接受概念覆盖的状态；前台离开停止新操作，返回恢复系统等待。
- **I07**：Actions／devcontainer 在容器内完成检查并产出可安装 debug APK 与清单。
- **I08**：Pocket／Pixel／fnOS 真实链路逐项记录 PASS 或真实阻塞；不能用测试 fixture、Dora 普通 provider 或小文件结果代替。

## 证据与后续关口

每个实现提交保持单一目的并使用小写 Conventional Commit。每个阶段完成时记录提交 SHA、容器 manifest、命令、测试结果、未测范围和文件 owner 交接。实现代码完成后启动独立实现审查；独立 reviewer 不能修改实现或把自检当作审查。只有最终审查通过并提交用户 M1 结果后，才进入 M2 用户 review；本计划不授权自动模式、完整故障矩阵或 iOS。
