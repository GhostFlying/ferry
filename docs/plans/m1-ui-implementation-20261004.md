# M1 Android accepted-surface UI implementation plan — 2026-10-04

状态：**独立实施计划审查 PASS，可进入实现**。

独立 reviewer：`/root/m1_foundation_reviewer`；当前计划快照 SHA-256
`38b45982a8b8048fce459e46915384788aa3824478a0c31ce8a97defcd47b834`。

## 目标与边界

把已由用户接受的 M1 Android 概念实现为 Kotlin／Jetpack Compose 原生界面，
并接入现有的持久任务状态与 `ForegroundExecutionCoordinator`。本任务只覆盖
已展示且已接受的 A01、A03–A10；M0 A02／A11、M2 自动模式、iOS、系统目录选择器、
外部浏览器登录页以及任何新增 Ferry 可见状态均不在本任务内。

接受记录见 `docs/design/android-v1/manifest.json`，概念图是视觉 spec，不是运行
证据。实现后必须用实际 APK 截图逐项核对；在真机尚未提供期间，只能记录容器构建
和截图准备状态，不能宣称 Android UI fidelity 或设备运行通过。

## 依赖、文件所有权与工具条件

- 依赖：已批准 M1；接受的 M1 concepts；`m1-predevice-20261004.md` 计划及其独立
  计划审查 PASS；现有 P1–P4、D1/T1 和 lifecycle coordinator。
- 实现 owner：`/root`；独立 reviewer：`/root/m1_foundation_reviewer`，只审查不改文件。
- 实现工作树：`../ferry-worktrees/m1-implementation-20261004/`，当前分支
  `feat/m1-android-foundation`。
- Android／Go 构建、单测、静态检查和 APK 均在固定 Ferry devcontainer 内运行；
  宿主只启动容器和取回产物。
- 文件范围：`android/app/src/main/java/io/github/ghostflying/ferry/ui/`、
  `navigation/`、`MainActivity.kt`、必要的资源／主题与对应 unit tests；允许在现有
  `OperationDao`／`OperationRepository` 增加只读 `allOperations` 查询供任务列表使用，
  不改 Room schema、Go bridge 或 SMB transport。来源／目标／规则配置接口不在本任务内。
  设计 acceptance ledger 和 checkpoint 可随实现提交更新；不编辑已生成概念位图。

## 实现内容

1. 从接受图提取并记录有限 tokens：白色背景、深蓝正文／灰蓝辅助色、teal 主操作色、
   红／橙错误与空间警告色、细分隔线、四项底部导航、状态图标和按钮层级。只保留
   图中已出现的中文文案、层级、间距与状态，不建立未来页面通用设计系统。
2. 以单一 `FerryUiState`／状态映射渲染四个底部入口：任务、来源、目标、规则；
   任务详情复用 A06/A07/A09/A10 的状态区域，首屏空状态复用 A08。状态由已有
   `OperationEntity`／repository 快照驱动，不能用固定 demo 数据作为功能实现。
3. 将已存在的动作接到真实接口：来源／目标／规则导航和人工暂停调用已有
   repository／coordinator；任务恢复、失败重试、空间重检、SAF 选择目录、SMB 保存、
   浏览器登录等没有现成接口的动作以禁用控件和“待接入”说明呈现，不新增配置 schema，
   不伪造连接成功或传输进度。来源／目标／规则页的运行时状态明确来自一个未配置的
   `UiConfigurationSnapshot`（显示“未配置／尚未选择”）；测试截图可以使用独立 fixture，
   但 fixture 值不得进入生产默认状态。
4. 保持无自动模式承诺：打开 App 才启动 `onOpen`，人工暂停不会因重进页面或再次
   `onOpen` 自动恢复；UI 不创建 foreground service、USB receiver、tsnet 登录流程。
5. 任务状态读取采用 `OperationRepository.allOperations()` 的一次性快照；ViewModel
   在页面进入、`onStart`／`onResume` 以及人工暂停等 UI 操作返回后显式 reload。当前
   coordinator 没有状态事件流，后台 worker 完成后的变化在下一次生命周期 reload
   反映；A10 只显示“内容校验中”的状态文案，不填造数字进度。
6. `phase == failed` 且 `lastError` 包含远端读回／SHA 不一致信息时映射到 A07 的
   verification-failed 详情；其他失败只在 A01 显示“任务未完成”与原始错误摘要，
   不把未知失败伪称为校验失败，也不新增失败编辑页面。
7. 为每个已接受图建立可重复的 screenshot fixture／状态输入，记录源码 SHA、设备／
   viewport（若只有容器则标记未运行）、概念图 SHA、可见文案 diff、布局／字体／色彩／
   间距／容器五类 fidelity ledger；不可由单元测试替代截图核对。

## 验收与证据

1. Compose unit／state tests 覆盖：四导航入口、A08 空状态、任务 imported／waiting／
   uploading-readback／paused／verification-failed／completed 映射；A03/A04/A05 在未配置
   snapshot 下显示明确未配置／待接入状态；暂停操作调用 repository 并保持
   `manualPaused`；重复 `onOpen` 不重复执行；页面 reload 能反映 phase 变化，且
   readback 状态不生成伪造数值进度。校验失败与未知失败分别按上述规则映射。失败
   重试、恢复、空间重检及配置保存按钮的禁用状态也必须有测试，确保不会产生虚假的
   成功状态。
2. devcontainer `./scripts/ci/android.sh` 通过，包含 Go tests/vet、bridge、严格依赖
   校验、Android unit tests 和 debug APK；产物绑定完整源码 SHA、容器镜像 ID、版本／
   ABI、APK SHA-256。
3. 有可运行 Android 环境后，按 A01、A03–A10 逐图截图并用 `view_image` 对照五类
   fidelity ledger；文案／层级／布局／字体／色彩／间距／容器的实质差异修复后再交付。
   在 Pixel／Dora／Pocket／fnOS 尚未提供时，设备安装、USB、真实导入、SMB 读回和性能
   条目保持 `NOT_RUN`／`BLOCKED`。

## 停止条件与不扩大范围

- 用户接受范围不足以覆盖新增可见页面或状态时停止，先生成概念并取得接受；不从已接受
  图推导规则编辑器、复制预算、冲突详情或 M2／M3 页面。
- 不能在 devcontainer 构建时停止，不回退宿主工具链。
- UI 不把 fixture、APK 构建、Dora 或内部 reviewer PASS 写成 USB／SMB／Pocket 链路证据。
- 完成本任务只代表 accepted-surface UI implementation；M1 最终 device/service review
  和用户 milestone gate 仍待真实环境与用户验收。
