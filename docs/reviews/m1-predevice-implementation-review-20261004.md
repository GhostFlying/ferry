# M1 pre-device lifecycle implementation review — 2026-10-04

审查对象：`33b4af9`、`a3d5779`（branch `feat/m1-android-foundation`）。
审查人：`/root/m1_foundation_reviewer`，只读独立审查。

## 结论

**PASS**。此前发现的 P1 暂停／派发竞态已修复，没有新的 P1/P2 正确性或范围问题。

## 证据

- `ForegroundExecutionCoordinator` 用同一 `lifecycleMutex` 串行化 pause 与
  claim／action 发布；上传 action 使用 `CoroutineStart.LAZY`，暂停在释放锁后
  `cancelAndJoin`，因此 CAS 成功后不会在暂停路径外启动未观察的 action。
- DAO 的 id／revision／phase／manualPaused CAS 条件保持不变；完成仍要求远端 SHA
  与完整副本 SHA 相等，迟到结果无法覆盖暂停状态。
- 测试覆盖暂停等待 action 结束、claim 交错 CAS、异常转 failed、终态忽略与重复
  `onOpen`、迟到结果隔离；fake DAO 的复合读写由显式锁保护。
- devcontainer `./scripts/ci/android.sh` 在最终实现提交前后均执行；最终修复后的
  本地运行：Go tests/vet、gomobile bridge、Android unit tests、strict dependency
  verification、debug APK 均 PASS。固定镜像为
  `sha256:1600111f150b1a1f54b2e02129f71711bd0eb923e4204699e0b3fcfcef28a8af`。
- GitHub Actions run `37182828273` 对远端 head `a3d5779428405ad6c3da345416c395b8aea2b8db`
  PASS。

## 边界

该审查只覆盖 pre-device lifecycle coordinator 与其测试，不证明 APK 安装、Pocket 3
USB／OTG、Pixel 6 Pro、Dora、fnOS SMB、真实读回或 UI fidelity。完整进程 kill／partial
final 故障矩阵仍属于 M2；终态迟到结果没有单独交错测试，但 DAO 终态条件与现有终态
忽略测试已审查为非阻塞 P2 建议，不扩大本任务范围。
