# 未批准规划范围修订 checkpoint

> **历史／superseded：本文件保留原范围、原 gate 与原 SHA，不是当前执行依据。** 当前 Android-first 收缩范围见 [scope-trim 执行计划](../../plans/scope-trim.md) 与 [scope-trim review 准备记录](../../reviews/scope-trim-review-prep.md)；下文 M1 双平台、M0 大文件／竞争／中断等旧要求不得用于当前 M0–M2 验收。

日期：2026-10-02。状态：`awaiting_user_review`；本次仅修改规划、治理及任务登记，不是 M0／M1 实施批准。

## 用户范围决定与审查基线

用户要求 iOS 进入 M1，SMB 先通过 Dora 云端物理设备验证，M0 后再安排指定相机／手机／飞牛完整链路；并要求每个 milestone 明确工作与验收。治理与详细文档按已独立审查的 [修订计划](../../plans/milestone-scope-revision.md)分工：规划代理负责产品和阶段文档，治理代理负责规则／公开登记，审查代理独立负责审查记录。

修订起点为 `def54a4c559931078a1d23abcf0f25d2f2048541`，PR 为 [#21](https://github.com/GhostFlying/ferry/pull/21)，用户关口为 [#1](https://github.com/GhostFlying/ferry/issues/1)。旧审查只适用于其原快照；本次新文档及远端登记完成后须重新独立审查。未提交内容由逐文件 SHA-256 固定，提交后映射完整 SHA。

## 当前阶段和交付边界

| 阶段 | 工作／产物 | 通过证据与关口 |
| --- | --- | --- |
| M0 | 受控 SMB／可达网络、单一 Go 核心、真实 Android probe／APK、Dora Android 物理设备完整协议实验 | 实际安装调用、连接与大文件上传／读回／无覆盖／中断分别判定；绑定服务、设备、网络路径、源码和产物。用户 review M0 后另行批准 M1。 |
| M1 | 双平台可用前台 App、原生状态／完整副本、共享 SMB／tsnet、Android APK 与签名可安装 iOS 路径、指定 Pocket USB 验收 | macOS／Xcode、iOS 安装条件、Pixel／iPhone USB-C／相机／飞牛分别证据；缺项不把整阶段标通过。用户再批准 M2。 |
| M2 | Android 接入自动模式、通知停止、服务限制和恢复强化 | 实际入口／类型／SDK、默认关闭、停止／超时／终止／人工暂停、内容与任务一致性；独立验收后用户批准 M3。 |
| M3 | 双平台发行准备、干净环境构建／安装、Apache-2.0／依赖通知、兼容矩阵和产物 provenance | 真实包、源码／版本／哈希、复现和签名／渠道条件；用户明确发布范围，后续 backlog 另立计划。 |

上述是登记范围，运行步骤全部未执行。详细任务与验收 ID 以 [总计划](../../implementation-plan.md)和 M0–M3 阶段文件为准，不能用此摘要替代具体验收。

## 历史 issue 迁移审计

| 保留 issue／历史 ID | 当前处理 |
| --- | --- |
| #3–#8／M0-A1–A6 | 留在 M0，普通 provider／受控服务／Dora 证据替代旧相机前提；不继承任何运行通过结论。 |
| #9／M0-B1，#10／M0-B2 | 迁入 M1 的真实来源／飞牛链路，M0 后用户 gate 才执行；原编号与旧范围保留为历史。 |
| #11／M0-B3 | 非实施拆分 meta 入口：前台恢复映射 M1-V1，自动模式映射 M2-AUTO1；不关闭为已完成，不计为子任务验收。 |
| #12／M0-X1 | 迁入 M1 的必需 iOS 构建桥接，不再作为可跳过的 M0 风险 spike。 |
| #13／M0-R0 | 留 M0，验收依赖 A／C 云端协议节点，不再依赖指定 Pocket／Pixel／飞牛。 |
| #14–#20／M1-P1–P7 | 保留编号并扩展到当前双平台基础版，以新计划重新审查，不能套用旧 Android-only范围。 |

新增 M0-C1／C2、M1-BUILD0／D1／I1／T1／V1、M2-AUTO1／REC1／V1、M3-DOC1／BUILD1／V1 的 issue URL、真实数字依赖和反向 Blocks 在新范围独立文档审查后登记。旧 ID 到当前全限定 ID／stage 的映射以最终阶段表和公开 issue 历史段落为准；不删除、静默复用或关闭为完成。

M1-BUILD0 是独立的先行 iOS 工程任务；完成后最小入口所有权交 I1、构建配置所有权交 P6。I1 依赖 BUILD0，P6 承担 I1／Android 集成完成后的最终产物，不把二者设为双向 issue 依赖。B1／B2 使用 P5 集成的 Android 包，不因 P6 双平台最终产物未齐而隐含等待 iOS；P7 仍等待所有双平台交付和指定真实链路。

## 当前验证与下一步

本次只验证静态文本、简单本地链接、JSON／YAML 和阶段／任务一致性；未运行 App、Actions 或 SMB 服务，未占用 Dora、连接设备或运行设备实验，也没有处理真实凭据／素材。协调阶段可以进行只读认证／设备列表查询，其结果不表示已经取得 lease 或设备验收通过。许可证保持 Apache-2.0，沿用用户既有 GitHub 身份，不修改 Git 配置。

新范围文件及 SHA 交独立 reviewer 后，核对快照并原子提交、推送 PR #21；同步全部当前 milestone／issue 和 PR／#1，再独立核对远端当前范围与 DAG。用户明确批准规划与 M0 后才合并并从包含治理规则的 main 建立工作树；当前保持未合并和未开工。
