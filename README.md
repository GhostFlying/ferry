# Ferry

Bring camera media to your own storage.

Ferry 是一个规划中的开源移动应用：媒体设备连接手机后，根据用户设置的规则，将原始素材导入并上传到 NAS 或网盘。

**当前状态：项目方案阶段。此目录暂时没有可运行的 App，示例配置也是设计草案。**

## 首期范围

| 项目 | 首期选择 |
| --- | --- |
| 媒体设备 | DJI Osmo Pocket 3 |
| 连接方式 | USB／OTG 有线连接 |
| 首期平台 | Android，Pixel 6 Pro 为首个实测机型 |
| 紧随首期的平台 | iOS，iPhone 17 Pro／USB-C；不考虑 Lightning |
| 首个上传目标 | 飞牛 NAS 的 SMB 共享 |
| 远程访问 | 内嵌 tsnet，应用作为独立 Tailnet 节点 |
| iOS 后续运行方式 | 用户打开 App 后，自动扫描并执行已配置规则 |
| Android 运行方式 | 默认打开 App 后运行；可选设备接入自动模式及前台服务 |
| 后续顺序 | 先补全 iOS，再评估 OpenDAL 接入网盘等目标 |

首次使用需要通过系统界面授权素材目录，配置 NAS 和规则。之后在来源仍可访问、授权有效、网络和空间满足条件时自动执行。授权失效时提示重新选择目录。

首期以原文件复制、内容校验、重复连接去重和中断恢复为核心。任务进度分别显示「导入手机」「上传 NAS」「内容校验」。相机上的原文件始终保留。

## 典型流程

1. 将 Pocket 3 切换到 OTG／文件传输模式，使用数据线连接手机。
2. 打开 Ferry；Android 用户也可开启自动模式，在系统允许的接入流程中启动任务。
3. Ferry 读取已授权目录，展示匹配规则的文件和目标位置，并按开启的规则自动处理。
4. 每个文件先导入手机的受管理存储，再通过局域网或 tsnet 上传到飞牛 SMB。
5. 完成远端内容校验后记录结果，并按缓存保留策略释放手机空间。

Android 未开启自动模式时只在 App 前台运行；离开 App 后暂停，重新打开时恢复任务。Android 自动模式使用可见、可停止的前台服务，并处理系统启动限制和超时。紧随首期补全的 iOS 版本同样将导入和 SMB／tsnet 上传限制在 App 前台运行。

## 项目文档

- [产品范围和规则语义](docs/product-plan.md)
- [架构、SMB 和 tsnet 设计](docs/architecture.md)
- [分阶段实施计划与验收条件](docs/implementation-plan.md)
- [官方资料与待验证事项](docs/platform-evidence.md)
- [项目代理规则](AGENTS.md)
- [代理协作、独立审查和里程碑协议](docs/agent-workflow.md)
- [仓库初始化与治理准备计划](docs/plans/repository-bootstrap.md)
- [Pocket 3 → 飞牛配置草案](examples/pocket3-fnos.json)

配置草案默认打开 App 时运行、仅 Wi-Fi 上传、最多 32 GiB 手机副本并保留至少 4 GiB 空闲空间。主机、共享和目录授权引用均为占位值；应用尚未实现配置导入，不能直接运行此文件。

许可证已由用户确认为 [Apache-2.0](LICENSE)。Ferry 暂作为项目名；应用标识、双语使用说明和构建说明将在相应阶段补齐。
