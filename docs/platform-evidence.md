# 平台资料与证据边界

查阅日期：2026-10-02。M0 计划使用 Dora Android 物理设备做受控 SMB／tsnet 实验；M1 的 Pocket USB 实测目标为 Pixel 6 Pro；iPhone 17 Pro／USB-C 属于 M3。以下是官方文档和上游源码依据，尚未进行本项目的 Pocket 3、飞牛、Android 或 iPhone 实机测试。文档证明可用接口或已报告的问题，不等于本方案的组合已经验证。

## Pocket 3 有线导入

[DJI Pocket 3 官方 FAQ](https://www.dji.com/at/osmo-pocket-3/faq)列出使用 OTG 手机和 USB-C 数据线导出素材，以及通过 DJI Mimo 导出的方式。

[DJI 用户手册](https://dl.djicdn.com/downloads/DJI_Osmo_Pocket_3/UM/20250826/DJI_Osmo_Pocket_3_User_Manual_v1.0_en.pdf)第 27 页描述支持 OTG 的 Android 设备访问相机照片／视频；相机需进入相应模式。资料也说明文件传输期间不能拍照或录制。

[DJI OTG 帮助](https://repair.dji.com/help/content?customId=01700007307&lang=en&paperDocType=ARTICLE&re=US&spaceId=17)包含 Pocket 3，列出 iOS 的 OTG 操作和「文件」中的 DCIM 位置，但其具体示例使用 Lightning 适配器，不构成本项目 iPhone 17 Pro／USB-C 的兼容性证明。Lightning 不在项目范围内；USB-C 路径必须实测。

[DJI Osmo 系列 OTG 连接说明](https://support.dji.com/help/content?customId=en-us03400007307&spaceId=34&re=GB&lang=en&documentType=artical&paperDocType=paper)要求 Android 连接前在相机「下拉控制菜单 → 设置 → OTG 连接」，再用 C-to-C 线连接；之后相机作为 USB 存储出现，素材位于 DCIM，部分手机还需在通知中选择「传输文件」。

2026-10-06 指定 Pixel 6 Pro（Android 16／API 36）实测：未开启 OTG 连接时，系统日志读到 DJI `2ca3:0020`、固件 5.04，接口为 Mass Storage、RNDIS 和 DJI bulk，但设备数秒后移除，没有存储卷。用户确认开启 OTG 连接后 Pixel 能挂载为系统存储。M1 因此以系统挂载＋SAF 读取，App 只用 USB 设备列表检测 Pocket 并提示开启 OTG，不自行实现 USB Mass Storage／exFAT 读取；SAF 读取、重连和拔线结果仍以 M1 设备报告为准。

结论：有线导入有官方依据。具体手机上是否可由第三方 App 读取、重连后授权是否有效和实际文件系统表现，Android 属于 M0 结束后 M1 的实测事项，iOS 属于 M3，均不是 M0 门槛。

## Android

[USB Host 文档](https://developer.android.com/develop/connectivity/usb/host)提供接入发现与 USB 设备授权流程。接入处理可能显示系统启动／授权界面，原始 USB 访问权限与文件目录访问并不相同。

[SAF 文档](https://developer.android.com/training/data-storage/shared/documents-files)提供用户选择目录、provider 读取和持久授权。持久授权不保证被移动、删除或重新挂载的来源仍使用同一引用。

[数据传输任务选择](https://developer.android.com/develop/background-work/background-tasks/data-transfer-options)说明即时用户发起传输、可延迟任务和前台服务的不同适用范围。

[Android 15 行为变化](https://developer.android.com/about/versions/15/behavior-changes-15)说明，在适用的目标 SDK／系统条件下，后台 `dataSync` 前台服务受到时间限制，并需要处理超时。后续目标 SDK 的限制在实际开发与发布时复核。

结论：可选自动模式应当通过可见、可停止且类型适当的工作实现，不能将前台服务视作无条件永久执行权限。各 OEM 的实际接入与任务恢复需验证。

## iOS

[Apple 外接存储说明](https://support.apple.com/guide/iphone/external-storage-devices-iph95baac91f/ios)说明「文件」和受支持的应用可访问外接存储，也列出格式、转接器和供电条件。它不保证 Pocket 3 在任意手机和线材组合下被识别。

[目录访问文档](https://developer.apple.com/documentation/uikit/providing-access-to-directories)提供目录选择、security-scoped URL、bookmark 和文件协调机制；保存引用后仍需处理访问失败。

[后台 URLSession 文档](https://developer.apple.com/documentation/foundation/downloading-files-in-the-background)说明后台传输托管仅适用于 HTTP／HTTPS，后台上传需要以文件为来源。它不能直接承载任意 SMB 客户端或应用进程内的 tsnet 网络栈。

结论：M3 iOS 采用用户确定的打开 App 后运行方式。系统目录授权仍必要；iOS 锁屏与挂起后恢复连接是验收事项。

## tsnet 与移动桥接

[tsnet 文档](https://tailscale.com/docs/features/tsnet)和[源码](https://github.com/tailscale/tailscale/blob/main/tsnet/tsnet.go)描述进程内的用户态 Tailnet 节点以及 `Server.Dial` 返回的连接，可作为 Go SMB 客户端的传输入口。

[gomobile](https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile)提供 Android／iOS 绑定构建路径。[libtailscale 的 Makefile](https://github.com/tailscale/libtailscale/blob/main/Makefile)包含 iOS 构建目标，可供桥接方案参考。这不保证完整 Ferry 核心可以直接构建或具备稳定的手机生命周期行为。

[上游问题 #21353](https://github.com/tailscale/tailscale/issues/21353)报告 iOS 挂起后的 socket 恢复问题。该报告用于制定锁屏／返回前台测试，不把报告里的时间和故障当成所有设备的统一表现；开发时复核所选版本是否已处理。

结论：内嵌 tsnet 纳入首期技术目标。移动构建、登录、状态保护、恢复、包体和性能必须实际测试。

## SMB 与 OpenDAL

[go-smb2 上游](https://github.com/hirochachacha/go-smb2)演示使用已有 `net.Conn` 建立 SMB 会话，是与 tsnet 对接的候选。最终选择取决于飞牛兼容性和所需提交语义。

[OpenDAL services](https://opendal.apache.org/docs/rust/opendal/services/)列出 S3、OneDrive、WebDAV 等服务；此次查阅未找到 SMB。服务覆盖、绑定实现和移动端构建需要分别确认。

结论：首期飞牛使用独立 SMB 适配；OpenDAL 可在后续网盘阶段评估。存储服务列表不代表应用已完成对应服务的认证、后台上传或 tsnet 集成。

## 本轮环境查询与证据分层

2026-10-02 约 17:01（PRC）协调 agent 只读检查：bytedcli 已安装且认证有效；明确使用 `android`／`physical`／`idle`／`online`／`CN` 查询，返回 20 个候选（服务报告总数 1905），样本含 API 31／36、arm64-v8a。没有 occupy，没有连接 ADB，没有网络或 SMB 实验。此时存在候选不保证实施时可租；不公开 serial、连接地址或账号信息。

| 证据层 | 能说明什么 | 不能说明什么 |
| --- | --- | --- |
| 官方文档／源码 | 接口与已知约束 | Ferry 已构建或组合已运行 |
| 工具链／Dora 候选查询 | 当前环境准备情况 | 设备租用、安装、服务可达或协议成功 |
| M0 host 受控 SMB（待做） | 该客户端／服务器组合的连接与提交语义 | Dora 直连、飞牛或相机兼容性 |
| M0 Dora App 内 tsnet＋受控 SMB（待做） | 实际云端手机中的桥接和完整传输／恢复 | LAN 路径或 Pocket USB；构造素材不是相机素材 |
| M3 Dora iOS 普通文件／网络（待做） | 所测设备上的 iOS 安装／核心／前台行为 | iPhone 17 Pro／Pocket USB-C 兼容性 |
| M1 Android／M3 iOS 指定 Pocket／手机／飞牛（待做） | 该版本与线材／系统组合的实际完整链路 | 未测试机型或系统的泛化兼容性 |

M0 先确认受控服务及路由，优先使用授权测试 Tailnet；云端直接 SMB 仅在受控私有网络中可达时补测，不能笼统写“LAN／tsnet 均通过”。M3 iOS 需要 macOS／Xcode 和有效签名／安装路径，不以无签名产物或 Linux 编译作为 iOS 运行证据。

最新用户决定不在 M0–M2 做 iOS 工程验证或以其阻塞 Android。此前双平台 M1 的计划／review属于历史范围，不能作为当前计划通过证据。UI概念由指定skill生成，属于设计提案；接受图与原生运行截图、协议／USB证据必须分别记录。没有web实现时不能宣称Browser／Playwright QA已完成。
