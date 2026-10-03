# Android M0／M1 候选设计索引

状态：`awaiting-user-acceptance`。这 11 张独立竖屏是主要完整 screen 与关键状态的概念提案，全部由 built-in Image Gen 真实生成，并逐张用 `view_image` 检查。它们不是 App 截图、运行结果或被接受的生产规格；也不表示全部 M0-A5／M1-P5 可见 UI 已无条件覆盖。

Android 最终采用 Kotlin／Jetpack Compose 原生文字、表单和控件。M2 自动模式及 M3 iOS 不在这组概念里。用户接受所覆盖的设计和批准 milestone 实现分别记录，当前两者都不由这组图片自动产生。

图内“设计示例，非运行证据”是 review-only 注释，未来 App 不实现该句。DEMO 名称、路径、摘要、时间、数量、进度、容量和状态实例都是动态 fixture；“已完成必须远端内容校验通过”“完整副本保留”“人工暂停须手动恢复”等真实行为和状态文案仍是产品要求。

| ID | 候选完整屏幕／状态 | 选定图 |
| --- | --- | --- |
| A11 | M0 初始 probe：配置入口、构造文件／只读目录、桥接检查、前置不足时开始禁用 | [完整竖屏](a11-probe-initial.png) |
| A02 | M0 读回失败：服务／网络／来源／桥接、非稀疏 5 GiB、参考摘要、重试／取消 | [完整竖屏](a02-probe-diagnostics.png) |
| A08 | M1 首次配置空态：来源／目标／规则的入口与前台工作方式 | [完整竖屏](a08-first-setup.png) |
| A01 | M1 任务主屏：单上传与单导入并行、队列、已完成／人工暂停列表状态 | [完整竖屏 v3](a01-tasks-active-v3.png) |
| A03 | M1 来源：授权失效、重新选择目录、完整副本不受来源离线影响 | [完整竖屏 v2](a03-source-authorization-v2.png) |
| A04 | M1 目标：飞牛 SMB、局域网／应用内 Tailscale、Wi-Fi 开关、连接未测 | [完整竖屏 v2](a04-target-config-v2.png) |
| A05 | M1 规则：扩展名／目录边界、完整哈希模板、未知内容路径预览 | [完整竖屏 v2](a05-rules-preview-v2.png) |
| A06 | M1 空间等待：当前文件未完整导入、其他任务副本保留、重新检查／暂停 | [完整竖屏 v2](a06-waiting-space-v2.png) |
| A07 | M1 校验失败：未完成、手机副本保留、不推断原因、重新上传／暂停 | [完整竖屏 v2](a07-verification-failure-v2.png) |
| A09 | M1 人工暂停：再次打开仍暂停，手动恢复会从头重新上传文件 | [完整竖屏 v2](a09-user-paused-v2.png) |
| A10 | M1 单独读回中：字节进度、已写入待校验、最终完成等待、可暂停 | [完整竖屏](a10-readback-active.png) |

[功能 brief](brief.md)、[实际 prompts 与修订记录](prompts.md)、[图集 manifest／尺寸／哈希／provenance](manifest.json)、[作者静态检查与覆盖边界](review-note.md)。

A11 的“配置测试服务”只展示入口。A04 是 M1 产品目标页，最多作为字段家族的参考，不能自动代替 M0 设置页／弹层。M0 设置页、规则子编辑、缓存保留设置及未展示的错误／冲突等 Ferry 可见 surface，必须各自补概念并经用户接受后才写视觉细节或实施。系统目录 picker 和外部浏览器登录属于平台 surface，本组没有伪造这些系统界面。完整未覆盖清单见 manifest。

已替代的旧图只保留生成源文件、哈希、prompt 和修正原因，不作为候选并不随仓库发布 PNG；原始 Image Gen 缓存文件保留。所有链接只指向当前选定的 11 张图。

本轮没有 native／browser 实现，没有生产 tokens，没有 UI 实施清单，也没有 Browser／Playwright、APK、设备、USB、SMB 或概念到实现的 fidelity 验收。
