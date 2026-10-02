# Android 优先与 UI 设计先行 checkpoint

日期：2026-10-02。状态：`awaiting_user_review`；本轮只修订未批准规划、生成供审阅的概念图及准备公开登记，不是 M0 实施、UI 设计接受或发布授权。

## 最新决定与审查基线

用户要求 iOS 延迟到 M3、早期不纠结双平台，以及任何 UI 方案使用 `build-web-apps:frontend-app-builder` 先生成设计图。当前执行依据为已独立 plan review PASS 的 [mini plan](../../plans/android-first-design-revision.md)，对应 SHA-256 `498e90a7fed2434078617caf19ebff746499f944783123db66a062e76d979268`。

修订起点为 `f86441ff3caa32f9eca2b47b0d32de6a99cad0e5`；[PR #21](https://github.com/GhostFlying/ferry/pull/21) 与 [用户关口 #1](https://github.com/GhostFlying/ferry/issues/1)继续承载当前审阅。此前 [M1 双平台修订 checkpoint](scope-revision-checkpoint.md)和旧 review 只记录其原快照；此次新范围、概念与登记重新独立审查，不能套旧 PASS。先前“完整 M0／仅 M0A”及上一版 Dora／M1 双平台审批问题均被当前方案替代。

## 当前阶段与验收边界

| 阶段 | 工作／实际产物 | 验收／用户关口 |
| --- | --- | --- |
| M0 | Android probe、受控 SMB／tsnet、真实 APK／Actions、Dora Android 物理协议实验 | 连接与完整大文件／读回／无覆盖／中断分路径证据，已接受诊断概念与实际截图对照；无 iOS／macOS 条件，用户批准后才实施 |
| M1 | Android on_open 前台基础版、规则／状态／副本／传输、Pocket／Pixel／飞牛链路 | 真素材摘要、权限／空间／冲突／拔线／kill／人工暂停、真实 Android 包及产品 UI 对照；不等 iOS，完成后用户另批 M2 |
| M2 | Android on_attach／恢复、新增 UI，Android 文档／许可通知／干净构建／发行候选 | 合法入口与停止／故障矩阵、接受的新增图与截图、按文档重建安装与可追踪候选；发行安排待本次用户 review，发布另需明确决定 |
| M3 | 此阶段才设计／实现 iOS 原生前台 App、桥接／工程／来源／状态保护／传输、Pocket／iPhone USB-C，双平台交付 | macOS／签名安装、iOS 云物理与指定 USB 分开证据、前台恢复与 Android 回归、真实包／双语说明／发行清单、iOS 概念对照；缺条件阻塞本阶段对应项 |

详细动作／预期／证据以最终 M0–M3 计划为准。正常 Go 模块边界和规则向量保留，M0–M2 不为未来 iOS 预先实现平台抽象、bridge／保护存储或生产接口。

## UI 与原生 skill 适配

治理代理已完整读取指定 frontend-app-builder skill。用户的原生目标和当前 Kotlin／Compose、M3 Swift／SwiftUI 架构作为框架依据；native 截图验证适配明确写入 [协作协议](../../agent-workflow.md)，随当前计划交用户 review，不伪装为完成了 skill 的网页验收。

每个阶段 DESIGN1 记录完整主屏／必要状态的图与内容哈希、fixture／覆盖、用户接受原文／时间／范围；只有接受后才提取 tokens／视觉实施清单并独立 plan review。设计接受与阶段实施批准分别记录，不能互相代替。M0 probe 与 M1 产品概念可在本轮已授权范围生成；M2 新设置／通知及 M3 iOS 到该阶段另出图。约 6–7 张是本轮覆盖估算，不是上限或完成判据。

实施后的真机截图绑定实际源码、设备／系统、尺寸／主题／状态，同一 QA 轮次 `view_image` 检查接受图与最新截图。至少核文案、层级／布局、字体、色彩、间距／容器，并保存文案 diff、fidelity ledger、修复或用户明确接受偏差；原生功能、USB／传输和视觉分别判定。无网页 artifact 不称 Browser QA 通过；实际网页预览只提供该预览证据，不代替 native。

本 checkpoint 不定义具体视觉布局／tokens，也不宣称概念已经生成、用户已经接受或 native UI 已运行。设计状态由 design agent 在其单 owner 文件中如实记录，之后独立审查覆盖、可读性及事实边界。

## 历史任务与登记迁移

| 既有 issue／稳定 ID | 当前待审处理 |
| --- | --- |
| #3–8／M0-A1–A6、#22–23／C1–C2、#13／R0 | 留 M0；删除为 iOS 提前实现接口的要求，A5 可见 UI 等 M0-DESIGN1 接受与 UI 计划，非 UI 核心节点不因此串行 |
| #9／M0-B1、#10／M0-B2 | 留 M1 Android 指定来源／飞牛链路，无 iOS 前置 |
| #11／M0-B3 | 保留非实施 split-meta；Android前台恢复仍映射 #28／M1-V1，自动模式映射 #29–30，不关闭为完成、不计生产边 |
| #12／M0-X1、#24／M1-BUILD0、#26／M1-I1 | 保留历史字面 ID，从 M1 迁 M3；旧阶段和旧范围不可静默抹去 |
| #14–18／M1-P1–P5、#25／D1、#27／T1 | M1 Android 实际契约／核心／来源／UI／传输，删除早期 iOS 生产要求；P5 UI 等 M1-DESIGN1 接受 |
| #19／M1-P6、#20／P7、#28／V1 | 仅 Android 包／综合 review／恢复验收；#28 原 iOS 验收明确移新 M3-IOSV1，去 #24／#26 未来阶段依赖 |
| #29–31／M2-AUTO1／REC1／V1 | 留 M2，仅 Android，去 iOS 回归门槛；新增 M2-DOC1／BUILD1 承接原 Android 发行准备 |
| #32–34／M3-DOC1／BUILD1／V1 | 留 M3，接真实 iOS 实施／USB 后做双平台最终交付，不能以旧发行 gate 替代新增 iOS 工作 |

新增 M3-IOSV1、M2-DOC1／BUILD1 和 M0–M3-DESIGN1，编号在最终文档／图示审查后由治理代理真实登记；当前不编造编号。所有任务保持可核对工作、产物、环境、PASS、证据和关口。X1 → BUILD0 → I1 → M3-BUILD1 → IOSV1／M3-V1 单向交接，BUILD0交应用入口给I1、构建配置给M3构建owner，不能复活循环；M1 P6／P7 只等待 Android。最终数字 start／final／reverseBlocks 逐项一致且整图无环。

## 本轮所有权、验证与下一步

planner 单 owner 技术文档与阶段任务；design agent 单 owner `docs/design/`／概念资源／设计状态；治理代理只改 AGENTS／workflow／bootstrap／本文件及稍后已授权 GitHub 登记；reviewer 单 owner 审查记录。主代理协调和报告，不编写应用业务代码。

治理草稿完成后以逐文件 SHA-256 交独立 reviewer，不提交、不推送、不修改远端阶段或 DAG；退出 active turn 腾出设计槽。最终技术文档／实际概念及审查齐后，治理代理再按协调授权核快照、分目的原子 docs／image 提交、推规划分支并同步 PR／Issue／milestone，独立审查真实远端。用户明确批准当前规划／M0 后，才合并规划并从含规则的 main 创建实现工作树；非 UI 任务按自身依赖推进。对应 UI 实施还须另行取得该范围设计的用户接受，并完成独立 UI 实施计划 review；设计接受不作为规划合并或全部非 UI 开工的共同前置。

本轮未写 App／CI、启动 SMB 服务、占用 Dora、连接设备或做传输实验；未处理真实凭据／素材。许可证 Apache-2.0 和用户既有 GitHub 提交身份不变，不改 Git 配置。当前概念／实施／运行验证都不冒报；旧 PASS 与旧未答问题不构成本次授权。
