# M0／M1 独立技术计划审查

> 历史范围：本报告审查原版 M0 真相机门槛／M1 Android 范围。用户随后要求 M0 Dora 协议验证、iOS 移入 M1，原版阶段范围已被请求修订；本报告 PASS 不覆盖新版。参见 [范围修订审查](milestone-scope-revision-review.md)。原始发现、证据与 SHA 保留供追溯。

- 日期：2026-10-02。
- 类型：独立 plan review；不是实现验收。
- 计划作者：`/root/plan_milestones`；独立审查者：`/root/review_plan`。
- 模型／reasoning：以协调代理的任务分配记录为准；本报告不推断运行时配置。此行在范围修订时纠正原先未经核实的“继承配置”表述，不改变原技术审查证据。
- 结论：**PASS，可以提交用户审阅。** 初审 P1 已在计划层面整改，未发现残留 P0／P1／P2。下列技术能力仍须按计划实验，不是已经证实可用。
- 当前批准状态：M0A、M0B 均等待用户批准 M0；M1 为提前审阅的候选计划，等待 M0 必需验收及用户另行批准。内部 PASS 不改变这些状态。

## 审查范围与快照

已完整读取当前 README、产品方案、架构、平台证据和配置示例作为需求背景，并逐字读取下列最终计划。检查重点为来源与平台证据、共享核心边界、SMB 完整性、空间与大文件、DAG／文件所有权、可交付 APK、真实设备门槛和用户关口。仓库治理另见 `repository-bootstrap-review.md`。

| 审查文件 | SHA-256（uncommitted preparation） |
| --- | --- |
| `docs/implementation-plan.md` | `74f9fa760a59127bfc7c0898846d3beabd18f88c1e494e3190c571674d411797` |
| `docs/plans/m0.md` | `3196cf1719aced32b457ae7f933e909ac5bcdb9ce1d2a5d59bc680cc218431a8` |
| `docs/plans/m1.md` | `17e9ff9efef2a742bbe641bb17d720352b6e222a643cbba98d6b6a2edc7ec32b` |

本次以无提交文件哈希固定对象；提交后须映射至完整 SHA。修改计划后重审受影响内容，不能让旧快照的 PASS 覆盖新内容。M1 的 P1 契约工作包仍须按计划再独立审查具体接口。

## 初审发现与计划整改

| ID／等级 | 初审问题 | 最终计划中的处理与复核结论 |
| --- | --- | --- |
| FP-01／P1 | 候选普通 Rename 可能覆盖，笼统选择 SMB 客户端不足以满足不覆盖承诺。 | M0 A3 明确禁普通 Rename 作最终提交；锁定版本后检查公开 API／wire、必要时提出最小适配或 fork 并独立 review。排他创建、flush、读回、no-replace 和两个写入者竞争必须先在测试 SMB、再在真飞牛通过；无可行路线暂停依赖工作。计划整改通过。 |
| FP-02／P1 | Linux 实验、真实硬件和下一阶段依赖未形成明确门槛。 | M0A 与 M0B 为同一 milestone 的证据分区，硬件缺失只阻塞依赖节点；B 未完成不能宣布 M0 通过或开始 M1。B1 只读真实来源、B2 双网络真飞牛、B3 生命周期与 R0 汇总形成验收依赖。计划整改通过。 |
| FP-03／P1 | 移动桥接可能误用原生 socket／FD，iOS 可移植约束不足。 | A2／A4 规定同一 Go 产物中的 `net.Conn` 传递；桥接只传路径和小消息，定义操作 ID、错误、回调线程、取消完成、迟到事件隔离、64 位计数及所有权。X1 单列 iOS 构建／smoke，缺 macOS 明确未运行风险供用户决定；不阻止用户按已知风险优先推进 Android。计划整改通过。 |
| FP-04／P1 | SAF 重连、USB 权限和 FGS 启动条件容易被当成同一授权。 | B1 验证重连／重启授权并提交重新选择目录的降级体验；B3 验证可见 Activity／通知用户动作等入口。明确 USB attach 或 connectedDevice 声明不自带后台启动豁免；拒绝、停止、超时降级为等待打开 App。计划整改通过。 |
| FP-05／P1 | 第一份真实 APK 被推迟，CI 可能只证明 AAR 或占位 App。 | M0 A6 和 M0-08 要求含真实 Go 依赖的 arm64-v8a probe APK、debug key 签名、下载／校验／安装证据。M1 构建实际 Compose／Go／Room App，模拟器 ABI 和 bridge 调用必须验证；普通 CI 不要求私有 NAS／Tailnet 凭据。实际建设均等待对应用户批准。计划整改通过。 |
| FP-06／P1 | 本机工具链与上游依赖要求不匹配；私有目录容易被误称为加密状态。 | A1 明确 bootstrap 并锁兼容组合，不要求 latest。A2／A4 验证自有 StateStore 或等效整状态加密、Keystore、原子持久化、锁屏、备份排除、退出／重登；默认文件 Store 不算已加密。计划整改通过。 |
| FP-07／P1 | 文件完成、崩溃窗口与本地缓存验收不够具体。 | B2 增加远端提交后记录前 kill 的对账、自有临时归属、校验失败保留副本；M2 同时核算 partial／完整／保留／并发预留和未知大小／超限；大于 4 GiB 真实素材和非稀疏协议测试分开，记录参考摘要来源。计划整改通过。 |
| FP-08／P1 | M1 在真实导入前无法拥有完整哈希，却可能冻结目标路径或制造完成任务。 | M1 区分待导入意图和内容已知任务；未知哈希只显示待确定预览，不伪造目标路径。真实导入／生产 SMB 与 tsnet／自动服务分别留在 M2／M3／M4，UI 明示未实现，fixture 不进入正常任务成功记录。计划整改通过。 |
| FP-09／P1 | 技术 review、CI 或合并可能被当成用户阶段批准。 | 总计划与两个阶段计划均保留用户关口；M0 结果和 M1 新批准分开；复杂包继续独立 plan／implementation review。任务、分支、worktree、文件 owner 和依赖明确，共享构建配置单 owner。计划整改通过。 |

P2 成本与证据事项也已纳入：读回至少增加一份完整 SMB 读取；RSS／耗时绑定实际机器，云设备不证明 Pixel 性能；完成证据限校验时刻，不承诺其他 NAS 写者随后修改或掉电后的持久性。没有延期的审查发现。

## 关键上游证据

本次通过 GitHub API 读取上游实际源码并取得完整提交，不只依赖候选库 README：

- `go-smb2` 提交 `82213d0ccb227e90976528ba61cbf1223e1981e7` 的 [Share.Rename](https://github.com/hirochachacha/go-smb2/blob/82213d0ccb227e90976528ba61cbf1223e1981e7/share.go#L267) 将 `ReplaceIfExists` 设为 `1`。因此该 API 不满足本项目 no-replace 提交要求；检查文件名存在后再调用也无法修复竞争窗口。
- 同提交 [OpenFile](https://github.com/hirochachacha/go-smb2/blob/82213d0ccb227e90976528ba61cbf1223e1981e7/share.go#L138) 的排他创建分支使用 `FILE_CREATE`；[File.Sync](https://github.com/hirochachacha/go-smb2/blob/82213d0ccb227e90976528ba61cbf1223e1981e7/file.go#L87) 调用 flush。这些是候选源码能力，不构成服务器或设备运行证明。
- 同提交 [go.mod](https://github.com/hirochachacha/go-smb2/blob/82213d0ccb227e90976528ba61cbf1223e1981e7/go.mod) 为 `/v2` module、声明 Go 1.27.0；Tailscale 提交 `67f8c81e610d9f469baf648dcea24a9589b17fb6` 的 [go.mod](https://github.com/tailscale/tailscale/blob/67f8c81e610d9f469baf648dcea24a9589b17fb6/go.mod) 声明 Go 1.27.1。这里只说明不能假设本机旧工具链支持当前上游，A1 应选择并验证兼容版本组合，不要求追随最新版本。
- 同一 Tailscale 提交的 [Server.Store](https://github.com/tailscale/tailscale/blob/67f8c81e610d9f469baf648dcea24a9589b17fb6/tsnet/tsnet.go#L230) 默认使用文件 Store；[Server.Dial](https://github.com/tailscale/tailscale/blob/67f8c81e610d9f469baf648dcea24a9589b17fb6/tsnet/tsnet.go#L352) 返回 Go 连接。文件私有性、移动构建和原生 FD 可用性都不能从这两个接口自动推出。
- 本次复核 [Android FGS 后台启动限制](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)及[服务类型](https://developer.android.com/develop/background-work/services/fgs/service-types)。计划正确地区分适用类型／权限与允许启动的场景，保留目标 SDK 和真机复验。

## 本次实际验证

| 检查 | 观察／边界 |
| --- | --- |
| Git／GitHub 初始只读快照 | 本地 `main` 尚无提交、无 remote；`gh api user --jq .login` 返回 GhostFlying；当时查询 `GhostFlying/ferry` 不可解析，表示当时不可见，不绝对证明不存在。后续建仓由治理任务另行记录。 |
| 工具链 | `java -version` 为 JDK 11.0.18；`/usr/local/go/bin/go version` 为 1.24.4，`go` 不在 PATH；`sdkmanager --list_installed` 列出 API 30／33／34／35 和 NDK 21.1.*。未找到 Gradle、gomobile、xcodebuild。 |
| 设备 | `adb devices -l` 为空。未操作任何实体设备或占用 Dora。 |
| 文档静态检查 | 独立内联 `python3` 检查三个计划的 UTF-8、换行、无尾随空格、fence 配对、简单本地链接目标存在和验收 ID 唯一，全部通过；M0-01 至 M0-08、M1-01 至 M1-09 齐全。 |
| 内容与快照 | 独立读取全部三个最终计划，核对 DAG、文件所有权、验收与产品边界；Python SHA-256 与 `sha256sum` 及作者清单一致。 |

## 未验证项与用户决定

没有构建 AAR／APK／iOS Framework，没有运行 Actions、安装 App、验证原生回调或取消，没有实测 SAF、Pocket 3 OTG、供电、重连、SMB／飞牛竞争、tsnet 登录／状态加密／路由、大文件／内存或 FGS。上述能力全部由 M0 及后续相应里程碑取得运行证据。源码阅读只证明所检查版本的实现形态。

当前可冻结上述快照并发布规划 issue／PR 供用户审阅。用户明确批准 M0 后才能进行 M0A／M0B 的代码、APK Actions 和实验；M0B 未完成保持 M0 未完成。X1 无环境时如实保留 iOS 风险，交用户决定是否接受并推进 Android。M1 的 plan PASS 仅表示候选范围、依赖和验收合理，不授予提前实施权限。

## Apache-2.0 决定后的事实更新复核

同日用户明确选择 Apache-2.0。作者仅修订总计划 M5 许可证／依赖通知表述及 M1 开工前契约的许可证状态，应用标识仍待建议和用户决定。审查者读取新段落，并将两项事实替换反向还原后计算 SHA-256，结果分别与前述旧审查快照完全一致，证明没有夹带技术范围、DAG 或验收变化。M0 文件哈希不变。

结论：**PASS，技术计划结论不变；M0／M1 用户批准状态不变。** 最新快照取代前述同名文件快照：

| 文件 | 更新后 SHA-256 |
| --- | --- |
| `docs/implementation-plan.md` | `d8ed59a58144978bcbfa60821fe7d0ded40a9a91dae3fa0d66fba764ec83c717` |
| `docs/plans/m0.md` | `3196cf1719aced32b457ae7f933e909ac5bcdb9ce1d2a5d59bc680cc218431a8` |
| `docs/plans/m1.md` | `7bac0e6f6d3181334b65d132a366eb7994c27adc98b69e02c4501494aafae8df` |

标准 LICENSE 全文的独立逐字节核对及治理状态增量见 `repository-bootstrap-review.md`，不构成应用构建、平台可行性或用户实施批准。
