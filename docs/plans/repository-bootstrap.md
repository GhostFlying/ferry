# 仓库初始化与治理准备计划

状态：原仓库准备与许可证已在历史 head 通过独立审查；当前按用户要求修订未批准规划，新范围待独立审查及用户审阅。范围为治理、GitHub 登记、文档验证和已授权概念图准备，不包含应用代码、APK／iOS 构建、服务启动或设备占用。

当前修订依据为 [scope-trim 执行计划](scope-trim.md) 与 [scope-trim review 准备记录](../reviews/scope-trim-review-prep.md)：M0 V01–V05 受控协议硬 gate，M1 Android `on_open`／完整副本／远端读回／一条真实主链路，M2 承接完整故障注入、自动模式与发行准备，M3 才开始 iOS 原生前台版／USB 验收与双平台交付。[Android 优先与设计先行修订计划](android-first-design-revision.md) 仅是历史／superseded 提案；原 M1 双平台方案及审批问题被替代，历史计划／审查保留其原文件快照，不套用于当前 head。

## 目标与文件范围

1. 新建手写的 `AGENTS.md`，固定产品范围、协调与实现角色、独立审查、里程碑用户审阅、DAG／worktree、Git 身份和原子提交、凭据及设备证据边界。
2. 新建 `docs/agent-workflow.md`，提供计划、任务、审查、阻塞和验收记录格式，并规定 GitHub issue／PR 的可追踪信息。
3. 新建 `.github/ISSUE_TEMPLATE/task.yml`、`.github/ISSUE_TEMPLATE/blocker.yml` 和 `.github/pull_request_template.md`，使上述记录可以直接提交。
4. 为 `README.md` 增加治理入口；阶段计划由对应规划任务维护。本轮仅按已登记的工具链约束同步 `docs/implementation-plan.md`、`docs/plans/m0.md`、`docs/plans/m1.md` 和 `docs/plans/m2.md`，不修改代码或构建工作流。
5. 本轮只使用现有工具验证 Markdown 文件目标、JSON 示例语法和模板 YAML；不编写校验脚本或 Actions 代码。后续文档校验工具须另有明确范围和计划审查，不能表示应用构建通过。
6. 独立实现审查和本地验收通过后，由协调代理安排原子提交、公开 `GhostFlying/ferry` 仓库、里程碑／任务 issue 和规划 PR；实施代理须收到协调代理的具体执行指令。用户已授权公开仓库建设，但 Android／Go 应用与 APK workflow 仍等待相应里程碑的用户审阅。
7. 用户已明确选择 Apache-2.0。从 Apache 官方来源取得完整标准文本，核对内容和 SHA-256，并对新增 LICENSE 及事实更新独立复核；许可证作为单独原子提交进入规划 PR。
8. 本次同步当前治理及 GitHub milestone／task／PR，保留原 issue 编号和旧 ID 迁移／拆分审计；每个阶段都明确工作、产物、环境、PASS 条件、证据、停止条件及用户关口。早期无 iOS 依赖，M3 承接真实 iOS 实施／USB；M2／M3 和每阶段设计任务也须登记，未排期 OpenDAL 不创建伪可执行阶段。
9. 所有 UI 使用指定 frontend-app-builder／Image Gen，完整 screen／states → 用户接受 → tokens／独立 UI 实施计划 → 经阶段批准实现 → 实际原生截图／view_image 保真；设计文件由 design agent 单 owner，治理只记录 protocol 和链接，不预先定义视觉细节或生成图。

## Android 工具链与 devcontainer 记录边界

当前计划规定 Android 默认 `minSdk 29`（Android 10），不处理 Android 9 及更低
版本。`compileSdk`／`targetSdk` 可以较新，但实际 API 号只能在实现开始前由
devcontainer 内锁定的稳定工具链确定并记录。未来 M0–M2 的构建、单测、静态检查、
Go／Android bridge 和 APK 产物必须在固定镜像 digest／manifest 或 Dockerfile
定义的 devcontainer 内执行；依赖缓存用可重建卷，证据记录完整源码 SHA、工具链、
ABI 和产物 SHA-256。

宿主只启动容器并保存脱敏产物；Dora 只安装容器构建出的 APK，若需 ADB 转发则
宿主仅做显式连接和设备操作。缺少容器运行时、固定定义或可重建依赖卷时，对应
构建验收为 `BLOCKED`，不得回退到宿主 JDK／SDK／NDK／Go／Gradle 缓存。M0／M1／
M2 计划分别列出容器内构建检查与设备／服务外部动作；该设计不增加低概率兼容
矩阵或额外发布门槛。

## 仓库操作与顺序

- 只读验证实际 Git 身份、`gh` 登录账号、repo／workflow 权限和目标仓库状态；目标若已存在或不能明确核实，则报告冲突，不接管或覆写。
- `main` 以一个原子 seed 提交起步，只含简洁的项目 `README.md` 和 `.gitignore`；seed README 写明规划状态，不能链接尚未提交的文件。完整 README、现有产品／架构／证据／实施提案和示例、治理／模板、详细阶段计划及独立 review 报告在规划分支按目的原子提交，通过规划 PR 给用户审阅。身份沿用用户已有配置。
- 每个提交显式列出并 stage 文件；不要使用不加限定的 `git add .`。没有提交时保存逐文件 SHA-256 作为审查快照，最终 stage 前核对当前内容与审查版本，避免并发规划修改被误提交。
- 新建公开仓库时只加入经过审查的文件。创建明确覆盖当前规划及后续实施的里程碑／任务 issue，在用户审阅的规划 PR 中记录 DAG、下一阶段范围和未决项。
- README、issue 和 PR 明确项目尚无可运行 App；用户尚未批准 M0，许可证已选择 Apache-2.0。当前外部准备不是 M0 或下一里程碑的实施授权。
- 所有公开内容检查凭据和私人信息；记录 repo URL、issue／PR URL、完整提交 SHA 和实际验证结果，不编造链接或成功结果。
- 当前仓库和 PR #21 已存在，只更新已获授权的规划分支／登记。新范围文件先独立审查，核对快照后按目的原子提交／推送，再独立核对远端真实 diff、当前 DAG、反向 Blocks 与 milestone 归属；不合并 PR、不启动实现。
- 用户明确批准当前规划与 M0 后，先合并规划 PR，将 AGENTS／协议／已批准阶段计划带入 main，再从该基线建立 M0 工作树；合并动作本身不代替用户批准。

## 验收与边界

- 所有新增协议与模板区分内部独立审查和用户里程碑授权；记录实现提交、审查提交、验证环境及明确的未测项目。
- DAG 依赖和每个工作分支／worktree 均能映射到任务；主代理只协调、汇总和报告，应用业务由实现代理完成。
- 使用现有工具验证本地链接目标、JSON 示例和模板 YAML；记录命令和真实结果。没有本轮校验器实现，不以新测试或 Actions 运行作为当前交付。
- 当前治理检查静态文件与规划文本；design agent 检查真实生成图的覆盖／可读性／fixture 边界及接受状态，最终由独立 reviewer 检查对应证据。不能宣称 Android／iOS 构建、USB、SMB、tsnet、原生 UI 或 Browser QA 已通过。
- 新登记保留 M0 受控服务／Dora Android 协议验证；M1／M2 只 Android，iOS 及 macOS／签名／USB 依赖到 M3。既有 #12／#24／#26 迁移保留历史 ID，#28 的 iOS 部分显式映射新 M3-IOSV1；每阶段设计接受与实施授权分别记录，任务／验收／反向 Blocks 与当前计划一致。历史说明可以引用旧范围，但必须标明已迁移／拆分。
- Android 工具链边界只在文档中登记；本轮不启动容器、构建、Dora 或设备验证。缺容器运行时时不能用当前机器结果替代，构建状态必须保留为 `BLOCKED`。
- 许可证采用用户已明确选择的 Apache-2.0 标准全文，不修改许可条款。未获得用户决定前不得擅自选择许可证的一般规则继续适用。
- 文档编写和独立审查阶段不进行提交／推送或修改远端登记。快照通过独立审查后，当前已获授权的文档发布由治理代理按协调指令原子提交／推送并同步现有 PR／issue；后续产品里程碑仍必须取得用户审阅决定。

## 风险与处理

- 当前本地链接检查只验证简单 Markdown 文件目标，不承担 Markdown 解析或外部链接存活检测；忽略 fenced 代码中的示例，fragment 只去除后检查目标文件／目录是否存在，不宣称锚点本身通过。
- Actions 仅在真实 Android 应用存在、对应里程碑经用户批准后才加入 APK 构建；不得以空 job 或假 APK 表示构建成功。
- 新 AGENTS 文件是项目内手写规则；不编辑全局生成的指令输出。
