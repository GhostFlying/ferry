# M0 host SMB protocol evidence + M1 APK smoke/preflight — 2026-10-05

状态：执行计划。目标是补充当前已接受 M0 host SMB client 和 M1 Android APK 的有限运行证据，
不改变 milestone 范围。M1 的真实来源→手机完整副本→远端读回（AV04/AV06）、
Pocket 3、指定 Pixel 6 Pro、fnOS SMB 和 M2 自动模式／发行准备均明确保持
`NOT_RUN` 或 `BLOCKED`，本文件不能使它们通过。

## 工作包

| ID | 工作 | 产物 | 前置 |
| --- | --- | --- | --- |
| RV-01 | 在固定 devcontainer 内从当前 M1 Android 源码构建真实 debug APK，记录完整源码 SHA、镜像 digest、版本／versionCode、min／compile／target SDK、工具链、ABI 和 APK SHA-256 | APK、构建日志、脱敏 manifest | 现有 devcontainer 可用 |
| RV-02 | 先用明确 `scope public`、`os android`、`device-type physical`、`usage idle`、`connect-state online` 查询；再新建本会话 Dora lease。通过容器内 ADB 经显式连接／转发安装 RV-01 APK，核验 OS/API/ABI、安装、启动及已接受 UI 的任务／来源／目标／规则／暂停动作 | 候选查询、serial、Dora session ID、occupied list、ADB endpoint、容器内安装／启动日志、动作→预期状态→结果记录 | RV-01；新 lease |
| RV-03 | 启动隔离的**真实 SMB server（Samba 或等价实现）**，不接触私人 NAS；记录 server 镜像／版本、SMB dialect／capabilities、隔离 share／账号引用和 client→server 拓扑。在 devcontainer 内用 M0 SMB client 执行上传、flush、远端读回 SHA-256 和预存目标 no-replace | 服务版本／digest、dialect／capabilities、share／账号脱敏引用、服务端日志／拒写证据、输入字节／SHA、远端字节／SHA、cleanup 结果 | 容器运行时；临时测试账号；服务可达 |
| RV-04 | 将每个结果写入 validation checkpoint，分开记录 M0-V02/V04/V05、M1 AV04/AV06、M2 自动模式／发行准备、App smoke、host SMB、Dora→服务和 Pocket／Pixel／fnOS 完整链路；收尾在 `finally` 清理测试服务自有数据、撤销短期账号、teardown 本任务创建的 server/container，并核验 occupied 列表 | `docs/validation/m0/runtime-verification-20261005.md` | RV-02/RV-03 |

## 验收与证据边界

| 验收 ID | PASS 条件 | 不通过／停止条件 |
| --- | --- | --- |
| RV-A | APK 在固定 devcontainer 构建，产物可安装，源码／镜像／工具链／版本／ABI／哈希齐全 | 前置容器／工具链缺失：`BLOCKED`；已执行构建或产物行为不符：`FAIL`；未执行：`NOT_RUN` |
| RV-B | 候选查询、occupy 后的 `device get` 与 `device list --scope occupied` 均记录；session ID 在连接、ADB、安装和释放前均重新核对；设备完成规定动作并得到对应日志／截图（截图缺失不替代动作日志） | 前置无合适 lease、任一 session／occupied 证据缺失、session ID 不一致或 ABI 不兼容：`BLOCKED`；已执行安装／启动／UI 动作不符：`FAIL`；未执行：`NOT_RUN` |
| RV-C | 真实 SMB server 上，完整内容远端读回 SHA-256 与本地一致；已有目标不会被覆盖；测试自有文件和服务状态完成清理 | 前置服务／凭据／路由缺失或不可达：`BLOCKED`；已执行但 SHA、no-replace 或清理行为不符：`FAIL`；未执行：`NOT_RUN`。本切片不覆盖取消期间的网络 I/O 与 `.part` 清理，保持 `NOT_RUN` |
| RV-D | 每个结果绑定实际环境、源码 SHA 和产物，cleanup 记录完整；M1 AV04/AV06、M2 自动模式／发行准备、Pocket／Pixel／fnOS 明确单列状态 | 缺证据保持 `NOT_RUN`，不能由 unit test、UI、in-memory mock 或测试 SMB 推导完整链路 |

## 执行环境与停止条件

- 状态统一映射：外部前置缺失或不可达为 `BLOCKED`；已执行行为不符为 `FAIL`；
  未执行为 `NOT_RUN`；只有满足明确 PASS 条件才记 `PASS`。
- 构建、Go 测试、静态检查、APK 安装命令均在 devcontainer 内；宿主只启动容器、
  提供显式 ADB 连接／转发、运行受控服务并取回脱敏产物。容器内安装使用固定
  `adb`（来自 devcontainer platform-tools），目标通过 `adb connect <endpoint>` 或
  已记录的显式转发进入；宿主不执行 `adb install`、构建、测试或静态检查。
- RV-03 的拓扑必须写入记录：SMB server 容器监听的地址／端口只使用本次隔离网络，
  devcontainer 通过显式 host／container route 到达；若 Dora 访问同一服务需要
  Tailnet／公开入口而未提供，则 Dora→服务保持 `BLOCKED`，不得把 host 直连结果代替。
- Dora 必须先 `auth status`，再用明确 OS 和 device type 查询 idle+online；不接管
  对话中给出的旧 lease 或固定 endpoint。每次 ADB／释放前重新 `device get` 并核对
  session ID；使用 shell `trap` 只释放本会话仍匹配的 lease。
- 测试 SMB 服务必须是真实 SMB server（例如 Samba），使用隔离共享、短期账号和
  测试素材；记录 dialect／capabilities、服务端拒绝覆盖的日志或等价证据。不把
  密码、Tailnet key、私人 NAS 地址写入日志或仓库。测试服务只证明该 SMB 协议组合，
  不证明飞牛兼容性，也不证明 Dora 路由可达。
- 当前 Android APK 的上传协调器仍未接入真实 upload backend。因此 RV-B 只能验证
  安装、启动、生命周期和已接受 UI；任何“实际上传”结果必须来自 RV-C 的协议 client，
  不能从 UI smoke 推导。
- `100.120.122.141:40863` 保留给后续用户提供的本地真机实验（RV-LOCAL），本轮不
  直接连接，也不把它作为 Dora session 的替代。

## 用户关口

本计划只执行已批准 M0/M1 范围内的验证切片；完成后提交证据、未测边界和下一步
（本地真机／Pocket／fnOS）供用户验收。M0-V02/V04/V05 因本切片不覆盖固定为
`NOT_RUN`（未来若实际执行但缺 lease／服务／路由则记 `BLOCKED`）；M1 AV04/AV06
及 Pocket／Pixel／fnOS 完整链路因实体条件缺失固定为 `BLOCKED`；M2 自动模式／
发行准备因未纳入本切片固定为 `NOT_RUN`，不自动批准 M1 完整链路或 M2。
