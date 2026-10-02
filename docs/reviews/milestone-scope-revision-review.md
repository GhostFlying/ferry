# Dora 与 M1 iOS 范围修订独立审查

日期：2026-10-02。作者：`/root/plan_milestones`（技术规划）、`/root/repo_governance`（治理）；独立审查者：`/root/review_plan`。模型／reasoning 以协调代理的任务分配记录为准；本报告不推断运行时配置。

## 文档变更计划审查

审查对象：[milestone-scope-revision.md](../plans/milestone-scope-revision.md)，SHA-256 `d9c0b201105c2b8edca569a64272fa1057df3f1eb606a5a69f0637d9769d9ab5`。

当时结论：**PASS，仅允许已授权的规划／治理文档修订。** 已完整读取 mini plan；没有新增 P0／P1／P2。当时尚未审查依据它改写的最终详细文档，不将这次先行结论预先用于最终方案或远端发布；最终文档审查见下文。

用户本次要求 iOS 移入 M1、SMB 可先用 Dora 云真机验证、M0 后再考虑真实相机完整链路；并要求每个保留 milestone 明确要做的事和验收标准。mini plan 对这些要求作出具体且可审阅的解释：

- M0 用 Dora Android 物理设备运行真实 probe，受控 SMB／App 内 tsnet 验证完整上传、flush、读回摘要、无覆盖、大文件和中断；不将云设备可租或连接成功冒充完整传输，也不生成 Pocket USB 证据。
- M0-C1 先证实受控服务和可达路径，Dora 直连仅在合法私有路径可用时测试；host `net.Conn` 能力与 Dora 实际路径分别记录。缺服务、账号或有效设备租约时保持相应验收未完成。
- M1 实质交付 Android／iOS 可用前台版，包含来源、spool、规则、状态、生产 SMB／tsnet 和恢复，不仅移动 iOS 标签。macOS／Xcode、签名安装、iOS 物理设备以及指定 Pocket USB 组合分列依赖，缺关键证据不宣布整个 milestone 通过。
- M0 后的用户 gate 再确认 M1 实体相机链路执行条件。此修订不是同意跳过该链路或接受未经验证的相机兼容性。
- 当前保留 M0–M3，各阶段必须展开任务／实际产物／环境／可观察通过条件／证据／阻塞与用户 gate；OpenDAL 等为未排期 backlog，不能假称已有可执行 milestone。
- 旧 B1／B2／B3／X1 和旧 M2／M3／M6 保留迁移记录，原 issue 不关闭为“已完成”。每个文件单 owner，旧 review 仅对原 SHA 有效。

细化提醒：最终文档须明确 D1／I1 与 P4／P5 的文件所有权及接口依赖，防止“双平台”任务造成重叠写入；Dora 上只实测 tsnet 时不得将 host 直连结果归为云手机双路径通过。这些已属于 mini plan 的约束，最终审查会核对落实。

当前没有进行应用、Actions、SMB 服务、Dora 占用、设备连接或真实凭据操作。内部计划审查不代替用户批准 M0。

## 最终文档变更审查

结论：**PASS，可冻结下列文件并按已有授权发布规划修订，随后交用户审阅。** 已逐段读取所有技术／治理修订以及 M2／M3 新计划，并独立核对实际文件与作者快照。无未解 P0／P1／P2，不沿用原版 PASS。

M0 的 host 受控 SMB 与 Dora Android 物理设备内嵌 tsnet 是两项明确的必需路径；Dora 直连为条件允许时补测，未测不称双路径通过。C1 只先证实 host 服务和准备拓扑，C2 在自己的新 lease 上证明云端可达和完整传输。M0 不以 Pocket／Pixel／真实飞牛为完成门槛，不产生 OTG 兼容性结论。

M1 的 iOS 已进入实际交付：单一核心桥接、独立 iOS 工程、原生来源／bookmark、完整副本、数据库／保护状态、SwiftUI、前台调度、生产 SMB／tsnet、签名安装和指定 Pocket USB 验收都有任务与依赖。P4／P5 仅 Android，I1 承担 iOS 原生实现；BUILD0 先行交付并明确所有权移交，避免目录重叠及任务依赖循环。

每个当前 milestone 的工作包均有实际产物、文件范围、环境、动作／可观察结果、证据和停止／用户关口：

| 阶段 | 任务／产物与检查重点 | 可判定验收 |
| --- | --- | --- |
| M0 | 锁定工具链、真实 probe APK／Actions、受控服务、host 协议和 Dora 物理机，独立 lease／清理 | M0-V01–V09，9 行，含安装调用、完整大文件摘要、no-replace 竞争、中断／身份恢复和释放。 |
| M1 | Android／iOS 可安装前台 App、规则／状态／来源／spool／生产传输、双平台故障与指定 USB | M1-V01–V12，12 行，含真实签名安装、相同规则向量、空间与源只读、双平台真实大文件、冲突／崩溃对账及前台恢复。 |
| M2 | Android 可选接入／FGS、通知停止、系统限制与恢复，iOS 前台回归 | M2-V01–V06，6 行，具体默认／开启／关闭／拒绝／超时／终止／锁屏动作，不用普通单测代替系统触发。 |
| M3 | 双语操作说明、独立干净构建／安装、发行清单、依赖通知、秘密检查与真实兼容性 | M3-V01–V06，6 行，按文档重建与实际安装、核心回归、哈希／版本／来源和许可核查；发行操作仍须用户决定。 |

全部共 33 行验收有唯一 ID。OpenDAL 等只列未排期 backlog，没有伪装成已获批准的执行 milestone。硬件／服务／macOS／签名缺失只阻塞受影响工作，不能降低规定门槛来标整体通过。应用标识 `io.github.ghostflying.ferry` 和 probe 后缀是随计划审阅的具体建议，不构成签名已配置或应用实施已批准的事实。

### 发现、修复与复核

| ID／等级 | 观察与要求 | 修复及独立复核 |
| --- | --- | --- |
| REV-01／P1 | 初稿 I1 依赖 P6 的 iOS 配置，而 P6 最终又依赖 I1，形成 task／issue 级循环。仅把 Mermaid 拆成检查点不能解决真实任务依赖。 | 新增独立 M1-BUILD0，依赖 P1／X1；完成后入口移交 I1、构建配置移交 P6；I1 不依赖 P6。按最终表建模的全部 15 个 M1 task 依赖程序检查无环，解项。 |
| REV-02／P2 | M2／M3 的 V1 任务行有 4 列而表头 3 列，文件范围和产物错位。 | 两表统一成 ID／依赖／文件范围／工作产物四列；独立扫描全部修订表格列数一致，解项。 |
| REV-03／P2 | C1“证明实际可达性”可能将 C2 云设备结果前置，M1 图表也有 P3 缺 P2 边及 Android 无谓等待 iOS 最终包。 | C1／C2 分工同步至详细计划与 mini plan；P2→P3 明确；B1／B2 使用 P5 集成 Android 包，P7 再对最终交付验收。表／图复核通过，解项。 |
| REV-04／P3 | checkpoint 的“没有 Dora 操作”易与协调只读 auth/list 查询混淆；审查记录曾未经核实断言模型继承。 | checkpoint 明确未占用／连接／运行设备实验；模型只引用协调任务分配记录、不推断运行时。治理文件和历史报告元数据修正已复核。 |

mini plan 的最终改动包含上述分工修正、BUILD0、发布授权边界及指向本报告的状态链接。总计划／M0 的最后状态文字也已独立复核：技术 review 指向本记录，应用实现仍等待用户批准，不把 PASS 写成实施批准。

### 最终文件快照

修订基线：`def54a4c559931078a1d23abcf0f25d2f2048541`。当前变更尚未提交时以如下逐文件 SHA-256 固定；提交后需核对远端 blob 和完整 SHA。三个历史 review 仅新增历史适用范围标注及模型元数据纠正，原发现／验收证据不改。

| 文件 | SHA-256 |
| --- | --- |
| `README.md` | `61a102f9e2dec0220650b683831f75b5d3b613bac792016d139e5b7726ef3913` |
| `docs/product-plan.md` | `fe54bc74acdb3d7ef60e5785583e9e8b127b324cd73cf60c99aeadbecb94f1f4` |
| `docs/architecture.md` | `1a760933d3e7d16881db1742d37c5c0d4260227d23f318e649010a6475ad7b0e` |
| `docs/platform-evidence.md` | `8d4cd1dd837dce165c17e81bb8189f77a27318918a0646f966f09be30e3ea09c` |
| `docs/implementation-plan.md` | `c40788bb8645e8c84724b043fab8cd79810a6d1c7b2034bda3b64a3556f3b31c` |
| `docs/plans/m0.md` | `b64895d621e41346cdf5f5764276d5fb1bc6c99a1a862c697b1d8049b1cc0587` |
| `docs/plans/m1.md` | `31c4dd2fa64cde5d18c9bed55f2a1673f833be80240c44fafce6c7dc1008582d` |
| `docs/plans/m2.md` | `feea924ca36442d53ae5ae510f5bf33878ddcc519fd838bc9a295ce7297e87b4` |
| `docs/plans/m3.md` | `547db1565fabf0151f58f4e3069c5769cdf0cc91706ea10eb6cd8e94ca34d9ec` |
| `docs/plans/milestone-scope-revision.md` | `aba676964618bf1fea15ee0b83f11555095333f43757382a0fd5e1f3eb0f4859` |
| `AGENTS.md` | `b753bc5177062aea456f98efedd1d8be0d7e7e7609e43fe54beef583f1555a64` |
| `docs/agent-workflow.md` | `8fcf1e8bac728d766347127d3c04dc121cfecfa8a4503af8979635adefaddd9d` |
| `docs/plans/repository-bootstrap.md` | `3a653510eef390118a281bb8b44c2508ad3624aa6d925d29e7787dd17141901c` |
| `docs/validation/planning/scope-revision-checkpoint.md` | `2db3f97f30a9136e4ef6240544250f141f78cf69c8cd16d9c29201f3cd3e36af` |

独立验证使用现有工具：UTF-8、末尾换行、尾随空格、fence 配对、简单本地链接目标、表格列数、验收 ID 唯一及 `git diff --check` 均通过；JSON 示例未变，语法解析通过。没有宣称外部链接／Markdown anchors／浏览器表单已验证。Dora 候选与认证查询是协调代理提供的只读环境记录，本 reviewer 未重复查询或占用设备；候选不等于 lease、网络可达或协议通过。

## 远端登记与用户状态

本次新版远端 issue／PR／DAG 同步：**待发布后独立核验**。此前针对 `b06b1fed92f6edc28f1eff56e11d12986cd27d38` 技术范围与 `def54a4c559931078a1d23abcf0f25d2f2048541` 发布结果的历史 PASS 不自动覆盖本次修订。

本轮未实现 App／Actions、启动服务、占用设备或运行传输。M0 尚待用户明确实施批准；M1–M3 仍分别受前阶段验收及用户关口约束。许可证与身份配置未变，本轮不重复将其全套验证当作新产品证据。
