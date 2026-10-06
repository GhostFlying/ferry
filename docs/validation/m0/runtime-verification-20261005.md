# Runtime verification checkpoint — 2026-10-05

状态：**host SMB protocol PASS；M1 APK smoke PASS；M0 App-internal tsnet／取消路径 NOT_RUN；M1 完整来源链路 BLOCKED。**

本记录执行的是 [runtime verification plan](../../plans/m0-runtime-verification-20261005.md)，
其 SHA-256 为 `441c577fcac0f3ad25f5d32665c9fdeaea1454a9e616e39bb4daa2ac7a8d9c80`；
该计划由独立 reviewer `/root/runtime_verification_plan_review`（`gpt-6-astra/high`）复核
并以 PASS 结案，记录见 [review](../../reviews/m0-runtime-verification-plan-review-20261005.md)。

## RV-A：devcontainer APK 构建 — PASS

- 源码：M1 Android 分支 `363814eefa802b29106787ccacddc92183b9c6f3`。
- 固定镜像：`sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`。
- 工具链：Go 1.27.1、JDK 17.0.20.1、Gradle 8.10.2、compile/target SDK 35、
  minSdk 29、`arm64-v8a`。
- APK：`dist/ferry-m1-debug.apk`，SHA-256
  `8cb9eb6e8365938f9c10969928fa8e20b4a021203ce494d8b1c092f2381cd81d`，
  versionName `0.1.0-m1`、versionCode `1`、debug 类型。
- 命令在 devcontainer 内执行：
  `./scripts/ci/android.sh`。Go 单测／vet、bridge 生成、Android unit test、依赖
  verification 和 debug APK 均通过。第一次无代理的容器尝试因下载依赖超时而停止；
  通过显式代理并使用固定容器／缓存重跑成功，未使用宿主工具链。

## RV-B：Dora Android APK smoke — PASS（有限范围）

- 新查询：`scope=public`、`os=android`、`device-type=physical`、`usage=idle`、
  `connect-state=online`；本会话 occupied 列表初始为空。
- 新建并核验本会话 lease：Pixel 6、Android 13/API 33、`arm64-v8a`。serial、
  session ID 和 ADB 地址已在 session-local 记录中保存；公共记录只保留脱敏引用。
  占用后同时执行 `device get` 和 `device list --scope occupied`，安装、启动和释放
  前均重新核对 session ID。
- devcontainer 内通过显式 ADB endpoint 执行安装：`adb install -r` 返回 `Success`；
  `pm path io.github.ghostflying.ferry` 返回已安装 APK。宿主只负责 ADB 连接／转发。
- 启动 smoke：任务空态显示 `Ferry / 任务`、`还没有任务` 和
  `完成配置后，打开 Ferry 即可开始`；底部依次切换任务／来源／目标／规则，分别
  观察到对应页面标题和未配置状态；强制停止并重新打开后任务空态仍存在。
- 设备截图：[dora-pixel6-m1-smoke.png](dora-pixel6-m1-smoke.png)，SHA-256
  `6f589867d89168a3f25906b3ccc5bac63a459d41295e0b863034a3162dd222a8`。
- 本 APK 的 upload coordinator 仍未接入真实 backend；没有任务 fixture，因此暂停
  任务动作没有运行，记为 `NOT_RUN`，不从空态 smoke 推导上传或暂停通过。
- 释放后 `device list --scope occupied` 返回空；ADB 连接已断开。

## RV-C：真实 Samba 服务上的 host SMB client — PASS

- SMB client 源码提交：`91d23b316d12e73e050904dc3c307fa010cbe301`。
  `experiments/smb/smb.go` SHA-256 为
  `66e6d10abf170e91c1b806a07f3fed2c2975cdcc01d1dec93a09a1376bfed449`；
  E2E 测试文件 `experiments/smb/e2e_test.go` SHA-256 为
  `0bb360799b1f479075ddca9c3a0093d9890a11481814c275bead5d5040a3413f`。
- 服务是本次创建的真实 Samba server，不是 in-memory/mock：镜像
  `sha256:97712505caa616c8878095323d9b46dbe174d7c8cd0034bd64b2fc79bc13e784`，
  `smbd 4.15.13-Ubuntu`。
- 配置和能力证据：`server min protocol = SMB2`、`smb ports = 445`、隔离 share
  `/srv/share`、用户 `ferry`；`smbclient -m SMB3 -L` 成功列出 `ferry` 和 `IPC$`，
  并报告 SMB1 disabled。服务只在本次运行的 loopback 端口 `127.0.0.1:1445` 提供，
  devcontainer 通过显式 host network route 访问。
- devcontainer 内执行 `TestControlledServerReadbackAndNoReplace`：输入 24 字节，
  本地和远端读回 SHA-256 均为
  `79414e560db7e5861017c280eb74d3772cfdf9cb8cd32bc2550534c51c850c47`；预存目标的
  第二次提交被 `StageRename` 拒绝，随后读回仍为 24 字节和相同 SHA-256。服务日志、
  client probe 和 Go E2E 均完成。`Client.Close` 重复调用回归测试通过，代码路径不会
  重复执行 SMB disconnect；本次没有把该回归测试写成 SMB wire-level 证据。取消期间的
  网络 I/O 与 `.part` 清理不在本次 E2E 覆盖范围内。
- 运行使用短期随机密码；测试文件、账号、Samba 容器在 `trap` 中清理，密码没有进入
  仓库或日志。
- 这只证明当前 `go-smb2` client 与 Samba 4.15 的 host 协议组合，不证明 fnOS、Dora
  路由、Pocket 3 或指定 Pixel 6 Pro 兼容性。

## 未运行与阻塞边界

| 项目 | 状态 | 原因 |
| --- | --- | --- |
| M0-V02 bridge on Dora | `NOT_RUN` | 本切片没有安装 M0 probe APK |
| M0-V04 App 内 tsnet → SMB | `NOT_RUN` | 本切片不覆盖 App 内 tsnet 登录与远端读回 |
| M0-V05 取消／终止恢复 | `NOT_RUN` | 本切片没有运行 M0 probe 的进程终止／重启对账；host SMB client 的取消期间网络 I/O 与临时清理也未纳入本次 E2E |
| M1 AV04/AV06 完整副本→远端读回 | `BLOCKED` | 当前 APK upload backend 未接入，且缺 Pocket／fnOS 实体条件 |
| Pocket 3 → Pixel 6 Pro → fnOS SMB | `BLOCKED` | 用户提供的本地真机 endpoint 保留给后续 RV-LOCAL，本轮未连接 |
| M2 自动模式／发行准备 | `NOT_RUN` | 未纳入本验证切片 |

本 checkpoint 只提交运行证据和一个关闭幂等修复；它不关闭 M0 完整 gate、不宣称
M1 完整链路通过，也不批准 M2。下一步是用户提供本地真机和 Pocket／fnOS 条件后，
另开 RV-LOCAL，按 M1 计划运行真实来源、完整手机副本、远端 SHA-256 读回和
no-replace 验收。
