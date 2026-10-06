# M1 Android accepted-surface implementation review — 2026-10-04

审查对象：`08c8b2e7c7d95c69280e07629fc1fc03b38652aa`，覆盖 `f264b77`、
`4fceb86`、`08c8b2e` 及其父提交。审查人：`/root/m1_ui_review_fallback`，独立只读审查。

## 结论

**PASS**。

## 复核范围与证据

- `onStart`／`onStop` 接入 coordinator；controller 用 mutex 串行生命周期、暂停和
  snapshot reload；shutdown 在 Room 关闭前等待 coordinator 清理且不使用主线程
  `runBlocking`。
- 无 upload action 时不 claim、不启动 action、不将 ready operation 改为 failed；
  已有的 onStop waiting 转换保持原有生命周期语义。
- `supervisorScope` 隔离普通 action 异常，异常在 await 后可持久化 failed；取消仍
  传播并由 onStop 等待。测试 fake DAO 在失败写入前检查 coroutine cancellation。
- UI 状态测试覆盖四 tab、A08 空状态、任务 phase label、读回不伪造数值进度、
  pause/reload、A03/A04/A05 配置文案和未接入动作禁用状态；测试 fixtures 不进入
  production defaults。
- Ferry devcontainer `./scripts/ci/android.sh` 最终运行 PASS：Go tests/vet、gomobile
  bridge、Android unit tests、strict dependency verification、debug APK；镜像为
  `sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`。

## 未测边界

该 PASS 不包含 native screenshot fidelity、Dora lease／ADB 安装、Pocket 3 USB／OTG、
Pixel 6 Pro、fnOS SMB、真实远端读回或完整素材链路；这些仍为 `NOT_RUN`／`BLOCKED`。
SAF picker、SMB 配置、浏览器登录、重试／恢复和 M2 自动模式在 UI 中保持禁用／待接入，
没有被实现或伪造。
