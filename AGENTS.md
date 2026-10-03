# Ferry 项目代理规则

这是手写的项目规则，适用于本仓库及其工作树。遵守适用的上级 AGENTS 规则；不要编辑全局生成的指令输出。用户在当前会话中的明确指令优先于本文件。

## 产品范围与事实边界

- 产品目标是 Pocket 3 经 USB／OTG 进入手机受管理完整副本，再上传飞牛 SMB，支持内嵌 tsnet。指定 USB 验收目标为 Android／Pixel 6 Pro 和 iOS／iPhone 17 Pro USB-C；不考虑 Lightning。
- 当前阶段为 M0 云端协议与 Android probe、M1 Android 可用前台基础版及指定 Pocket／Pixel／飞牛链路、M2 Android 自动模式／恢复与 Android 发行准备、M3 才开始 iOS 前台版／指定 Pocket USB 验收及双平台交付。M2 的 Android 发行安排是当前待审方案，不表示用户已授权发布；OpenDAL／网盘仍为未排期 backlog。
- M0 以受控 SMB 服务和 Dora Android 物理设备取得连接／完整传输证据；Pocket／Pixel／真实飞牛链路不属于 M0 完成前提。Android 指定链路经 M0 后的用户关口进入 M1，iPhone USB-C 链路在 M3；云端协议成功不证明 OTG 或飞牛兼容性。
- Android 默认 `on_open`，可选 `on_attach` 在 M2 使用已实测的系统接入流程和可见、可停止的前台服务；M1 前台恢复通过不表示自动模式通过。M3 iOS 采用 `on_open`，不提前承诺早期 iOS 运行支持。
- M0–M2 只实现 Android，不加入 macOS／Xcode、iOS bridge／签名／安装或 iOS 回归关口，不为未来 iOS 预先实现平台抽象或生产接口。保留 Android 所需的正常 Go 模块边界和确定性规则向量即可。
- M3 iOS 必须交付真实可安装的前台 App、原生来源／持久状态／完整副本和实际传输，不以 iOS 标签、桥接 spike 或仅 Framework 代替。到 M3 才开始 iOS 设计、工程和实现；macOS／Xcode、签名安装方式和指定 USB 设备缺失时，对应验收保持阻塞／未测。
- 保留相机原文件；“已完成”要求远端内容校验通过。不得通过大小相同、写入成功或虚拟设备结果声称真实素材链路已通过。
- 当前状态和阶段入口见 [实施计划](docs/implementation-plan.md)；实现不得自行扩大范围或将未验证的平台能力写成支持承诺。

## Android 工具链

- Android 默认 `minSdk 29`（Android 10+），不处理 Android 9 及更低版本兼容问题；允许较新的稳定 `compileSdk`／`targetSdk`，在实现开始时由 devcontainer 固定并记录实际版本。
- M0–M2 的 build、单测、静态检查、Go／Android bridge、APK 构建和安装命令均在 devcontainer 内使用容器工具链执行，不回退宿主工具链。宿主仅启动容器、提供显式设备连接／转发和取回脱敏产物，不执行安装、构建、测试或静态检查。
- Dockerfile pin、可审计 manifest、逐项工具链／产物记录及缺少容器运行时的 `BLOCKED` 条件见 [Android 工具链与 devcontainer 约束计划](docs/plans/android-toolchain-constraint.md)。

## 所有 UI 先生成概念并取得接受

- 任何 UI 方案必须使用用户指定的 `build-web-apps:frontend-app-builder` skill，并通过 Image Gen 生成完整主屏和必要状态／细节概念。覆盖 M0 probe、M1 Android 产品、M2 新增设置／通知与 M3 iOS；不得以文字描述、手绘代码或局部图代替所需完整概念，不得把设计图当作真实 App 截图。
- 实施顺序为功能 brief → skill／Image Gen 完整 screen／states → 用户明确接受覆盖的设计 → 从接受图提取 tokens／组件／文案／交互清单 → 独立 UI 实施计划 review → 在已批准 milestone 内实现 → 实际 App 截图与概念图逐项保真及功能验收。概念未被用户接受前，不先写具体布局／字体／间距／tokens 等视觉实施方案或 UI 代码。
- 保存概念路径、内容 SHA-256、screen／state 清单、fixture 说明和用户决定原文／时间／接受范围。概念是设计建议，不是设备截图或功能证据；接受图成为当前 spec。图数由覆盖和可读性决定，缺失、模糊或新增的可见状态先补图并取得接受。
- 设计接受和 milestone 实施批准是两个关口，分别记录。接受 Android 产品图不等于接受 probe 图，不自动覆盖未来 M2／M3 UI；内部 review、生成图和用户批准规划都不能默认为设计已接受。非 UI 核心工作只受其本身依赖和用户阶段批准约束，不为等待视觉接受阻塞无关节点。
- 保留项目原生框架 Android Kotlin／Compose、M3 iOS Swift／SwiftUI。该选择依据用户原生产品目标与当前架构，适用 skill 对已有／指定框架的约定，不改为 React／WebView；native 验证适配须写入计划并供用户与独立 reviewer 审阅。
- 原生适配以实际运行 App 的真机截图代替浏览器 render，记录屏幕／状态、设备／系统、源码、尺寸／缩放、主题和必要输入条件；同一 QA 轮次用 `view_image` 直接查看已接受概念与最新对应截图。至少核对文案、层级／布局、字体、色彩、间距／容器五类，并检查图标、控件状态、裁切、可访问字号和交互；记录可见文案 diff、fidelity ledger、修复证据和用户接受的偏差。可修复的实质视觉差异必须修复后再交付，功能通过不能代替保真通过。
- Browser／Playwright 规则只报告实际网页 artifact 的浏览器验证。若有网页预览，遵循 Browser 优先／不可用时说明 Playwright fallback，并标明只验证该预览；无网页实现时写 Browser QA `NOT_APPLICABLE`，不能称 skill 的网页验收已通过。native 截图／操作也不能证明 USB、传输或生命周期门槛，相关真机实验另行验收。

## 角色与模型

- 主代理只协调任务、维护依赖图、分配文件所有权、汇总证据并报告进度，不编写应用业务代码。实现、验证及仓库基础设施由分配到对应任务的代理完成。
- 用户已授权本项目使用多代理与多工作树。优先使用 `gpt-6-astra`、`high` 进行复杂规划与独立审查，使用 `gpt-6.1-sol`、`xhigh` 实现；具体分配和模型由主代理记录，遵守当前工具及用户允许的配置。
- 复杂任务必须按“落文件的计划 → 独立计划审查 → 实现 → 独立实现审查与验收”执行。审查代理不得是该计划／变更的作者，也不得实现同一变更；实现代理的自检不算独立审查。
- 每个任务开始前将实现计划写入文件。计划至少列出文件范围、依赖、验收、设备／工具条件和停止条件。简单任务可引用已经审查且覆盖该任务的阶段计划。
- 每个当前 milestone 都必须明确“做什么 → 产物 → 验证环境与步骤 → 可判定 PASS 条件 → 证据 → 阻塞／停止条件 → 用户关口”，并映射到可追踪任务；此规则适用于所有阶段，不限 M0／M1。缺少具体工作或验收的标题／愿景不是可实施计划，未排期 backlog 不能当作已批准 milestone。
- 独立审查要检查目标提交和真实变更，记录严重等级、证据、修复项及复核结果；不能只依据实现代理的摘要给出通过。

## 用户里程碑关口

- 本轮以及每个后续里程碑都必须交用户审阅。主代理提供计划／PR、提交、验证证据、未测范围、未决项和下一阶段范围，等待用户明确决定。
- 独立技术审查通过、CI 通过、PR 合并、任务 issue 关闭或用户未回复，都不能代替用户批准下一里程碑。
- 本轮已授权准备公共仓库、治理文档、任务登记和规划 PR；用户已批准 M0 Android 受控协议 V01–V05，但 M0 实现仍须独立 plan review；M1、M0-UI 与 iOS 仍待各自用户关口。
- 当前规划交付后，M0 仅在独立 plan review 完成后开始 Android／Go 探针、真实 APK 构建和对应 Actions；后续阶段同样执行各自用户关口。
- 在用户审批记录中保留原始决定的会话引用／时间和批准范围。只接受用户明确给出的决定，不推定批准，不把内部 reviewer 标成用户审批人。
- 范围、验收、平台保证、数据保留／破坏行为或里程碑依赖发生实质变化时，更新计划并重新独立审查；涉及用户已批准范围的变化还须重新提交用户决定。
- 当前 M0 Android 优先／iOS 延至 M3／UI 先出图范围已获 M0 用户批准；M0 仍须独立 plan review，M1、M0-UI 与 iOS 仍须各自新计划、新概念与用户关口。旧 M1 双平台方案及旧审批问题被替代，不能解释为当前范围授权。旧 PASS 不覆盖修订后 head；旧报告保留原结论、原 SHA，并明确历史适用范围。

## DAG、工作树与文件所有权

- 任务在 GitHub issue 和阶段计划中登记稳定 ID、milestone、依赖、分支、目标 base、工作树路径、负责代理、文件所有权及验收。依赖关系构成无环图；开始实施前依赖必须满足，准备性调查须注明不依赖的范围。
- 每个并行实现任务使用独立分支和工作树，推荐路径为仓库相邻的 `../ferry-worktrees/<task-id>-<slug>/`。本项目的授权不改变其他仓库的工作树规则。
- 不让两个代理同时修改同一文件。纯规划准备／修订可以在协调代理记录明确文件所有权后共享目录；实现任务使用独立工作树。冲突先交主代理调整所有权，再继续。
- 创建工作树前核实分支和基线；不得覆盖已有工作树、清理其他会话文件或丢弃用户修改。审查工作树固定到待审查提交。
- PR 标明依赖 PR 与目标 base。依赖合并或基线变化后更新依赖者，并重新执行受影响的验证；不要用旧基线的通过结果表示新提交已通过。
- 不为绕过用户里程碑关口提前启动依赖阶段；同一已批准里程碑内的无依赖任务可以并行。
- 阶段迁移保留原 issue、历史任务 ID、旧范围及引用 SHA，登记 moved-to／superseded-by／拆分后的真实任务关系。不能删去历史、静默复用 ID 或关闭为已完成；拆分入口明确为非实施 meta issue，不计入子任务验收。

## Checkpoint 与 GitHub 记录

- 使用 [协作协议](docs/agent-workflow.md) 的 checkpoint 格式。至少在计划审查、重要结论、阻塞、实现审查、验证和用户关口记录一次。
- 主代理持续向用户报告当前结论、下一步和阻塞；GitHub issue／PR 保留可追踪进度、计划链接、完整提交 SHA、验证命令／环境、产物和证据边界。
- GitHub milestone 说明与当前阶段文件一致，包含交付、通过判定和证据；每个 task 列实际工作、产物、对应验收 ID、环境和证据，以及真实依赖和反向 Blocks。范围变化后同步当前登记，历史范围单独标注，不能保留矛盾的旧执行要求。
- 空仓库没有提交时，以文件清单和逐文件 SHA-256 固定待审查内容，标记 `uncommitted preparation`；首个提交后改用完整提交 SHA。修改会使对应快照失效，须重新核对受影响文件。
- 技术阻塞上报所缺条件、已做调查、可独立推进的范围及需要用户做的具体决定。设备不可达或工具缺失不得静默变成通过。
- 用户已授权的项目 issue／PR 记录可以按任务创建和更新；不得未经明确授权联系其他人、邀请 reviewer、添加成员或 `@` 他人。

## Git、PR 与发布产物

- 使用仓库已有的正确 Git 身份；无本地身份时使用用户全局配置。提交前只读核实实际作者和提交者。不得写入或覆盖 `user.name`／`user.email`，不得使用 agent、bot、tool 身份或 co-author。
- 每个提交只表达一个可审查目的。采用 Conventional Commits，例如 `docs: define the Pixel 6 Pro source probe acceptance`、`feat: retain imported files until SMB readback passes`、`fix: preserve user pause after app restart`。
- `type` 必须全小写；常用 `feat`、`fix`、`docs`、`test`、`refactor`、`build`、`ci`、`chore`。描述具体结果，避免 `update`、`changes`、`misc` 等空泛描述。
- PR 使用仓库模板，关联 issue、计划、依赖、完整提交 SHA、独立审查结论、验证和未测范围。变更后使描述与最终实现一致。
- 公共仓库创建前核实登录账号、目标、可见性和是否存在；若 `GhostFlying/ferry` 已存在或状态不明确，上报主代理，不接管、覆写或重用既有仓库。
- 规划阶段只可提供文档验证 CI。存在真实 Android 应用且用户批准对应里程碑后，Actions 必须构建可安装的真实 APK；不得用空 job、假 APK、仅 AAR 或跳过构建表示 Android 交付成功。
- APK 产物标明完整源码 SHA、构建 run、版本号／版本代码、ABI、debug／release 类型、工具链版本和 SHA-256。首次 debug APK 明确不是正式发布；发布签名密钥不得进入仓库或普通日志。
- M3 iOS 产物同样关联完整源码 SHA、工具链、包／安装路径、签名方式和内容哈希，并验证实际安装／启动及共享核心调用；裸 IPA、签名未满足的包或仅 Framework 不算可安装 App。
- 许可证、应用标识和正式分发渠道在用户决定后记录；许可证待定时不添加擅自选择的 LICENSE，不将仓库公开等同于许可证已确定。
- 用户已在 2026-10-02 的项目会话中选择 Apache-2.0，标准全文见 [LICENSE](LICENSE)。此选择不批准应用实现、应用标识或正式分发渠道。

## 安全、设备与验证

- 公共文档、issue、PR、产物、日志和截图不得包含 SMB 密码、Tailnet auth key／节点状态、令牌、私钥、签名文件或真实私人配置。示例使用占位值和凭据引用；报告公开前审查内容。
- USB／OTG、供电、拔线重连、锁屏、硬件相关性能及真实 Pocket 3 → 手机 → 飞牛链路必须使用相应实体设备。云设备、模拟来源和测试 SMB 只证明对应的测试环境。
- M0 用户指定 Dora Android 物理设备，不以模拟器代替该门槛。先明确受控服务 owner、测试共享、限域短期账号、测试数据、云设备可达路径和清理；不假设云设备与开发机／家庭 NAS 同网段。连接成功与完整传输分别判定，Dora 经 tsnet 通过不记为 Dora 局域网通过。
- 云端物理设备结果绑定实际机型和网络环境，不代表指定 Pixel／iPhone 的 Pocket USB、供电或性能。M0／M1 为 Android 范围；M3 iOS 云设备的安装还依赖可用签名／设备注册方式。
- 没有合适本地设备时，按上级规则通过 Dora 查询明确 OS 与设备类型。每个会话从新查询的 idle＋online 设备建立自己的新 lease，记录 serial、session ID 和连接地址；任何连接、ADB／BDC、续租或释放前重新验证 session ID。
- 不接管此前已占用的设备，即使占用者是当前用户。自动化使用 `trap`／`finally`，只释放 session ID 仍一致的本会话 lease，并核实 occupied 列表已移除；USB 实验不能以云模拟替代。
- 实测记录手机型号／系统／API／ABI、相机固件、线材、microSD 文件系统、应用完整 SHA、测试步骤、结果和证据出处。iOS 构建需要真实可用的 macOS／Xcode；Linux 结果不能证明 iOS 构建成功。
- 运行与变更相称的单元、集成和设备验证，完整执行已约定的验收。验证结果区分通过、失败、阻塞和未测，不把 pending 写成 failed 或 passed。
- C++ 代码遵守 Google C++ Style Guide。跨平台共享核心不得依赖 Android URI、Context、服务对象或平台生命周期。
