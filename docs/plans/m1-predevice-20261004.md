# M1 pre-device lifecycle implementation plan — 2026-10-04

状态：**修订后待独立计划审查**。

## 目标与依赖

完成 M1 P4/P5 的非 UI 前台上传协调子项，绑定 AV03/AV09 的单测部分。
基线为 foundation `c9dd64dd651477bc5c2b671d56a40b5d6830c18f`；实现 owner
为 `/root`，独立 reviewer 为 `/root/m1_foundation_reviewer`。当前分支
`feat/m1-android-foundation`，工作树
`/data00/home/luchengxuan/WORKSPACE/ferry-worktrees/m1-implementation-20261004`。

设备验收后置只暂停对应实验，不把 M1 整体实施停在用户最终关口。此工作引用已审
[M1 implementation plan](m1-implementation-20261004.md)，不启动 M2、UI 或
设备操作。本次 coordinator 是可注入 suspend upload action 的单 worker，
不是生产网络集成完成；后续接入真实来源、配置与 transport 后才算实际 App 流程。

## 文件范围与实现

所有实现文件由 `/root` 独占；reviewer 只审查。

- `android/app/src/main/java/io/github/ghostflying/ferry/data/OperationDao.kt`
  与 `OperationRepository.kt`：
  - 列出 `imported/waiting`、未人工暂停、具有完整副本 SHA 的可执行任务。
  - 以 id/revision/expected phase/manualPaused 条件更新 claim 为 uploading。
  - 完成/失败只接受同任务、同 revision、uploading 且未人工暂停的更新；完成还要求
    action 返回的远端 SHA 与已保存完整副本 SHA 一致。
  - onStop 将非人工暂停的活动阶段移为 waiting；人工暂停和终态保持。
  - 删除 waiting→waiting 的伪恢复方法；返回实际更新结果，CAS 失败不 dispatch。
- `android/app/src/main/java/io/github/ghostflying/ferry/lifecycle/ForegroundExecutionCoordinator.kt`：
  - 一个 CoroutineScope 中至多一个 worker，注入 `suspend (OperationEntity) -> String`
    上传动作，返回经过远端读回的 SHA。
  - onOpen 启动 eligible task 循环；重复 onOpen 不重复启动。
  - onStop 取消并等待 worker 返回后保存系统等待状态，不再 dispatch。public 生命周期
    转换用一个 Mutex 串行；worker 不获取该 Mutex，避免 cancel/join 自锁。
  - pause 先落人工暂停、再取消并等待 worker；其余任务在仍处于前台时可继续。
  - 每个 action 前/返回后检查 coroutine cancellation，迟到结果经过 DAO CAS 后
    才能落盘。取消保留完整副本，不标完成。
- `android/app/src/test/java/io/github/ghostflying/ferry/data/` 与 `lifecycle/`：
  fake DAO + coroutine barriers/counters 测试，不引入新 scheduler/test framework。
- `docs/validation/m1/checkpoint-20261004.md` 与独立 review 记录：
  记录新增模块、源码 SHA、容器结果与证据边界。

## 可判定验收

1. 手动暂停在 onStop/onOpen 后保持，paused/failed/completed 不被旧 claim/完成覆盖。
2. 只对完整副本 imported/waiting 任务 dispatch；onStop 后这些任务等待下一次 onOpen。
3. barrier 测试中并发 action 上限=1，重复 onOpen 不重复 dispatch；onStop 返回后
   新 action 启动数=0；返回前台后系统等待动作再次执行。
4. 控制读 waiting→pause→claim 的交错，CAS 返回 0 且 action 未运行；迟到 action
   结果不能把 paused/terminal 任务改 completed。
5. action 返回错误 SHA 时不完成，异常可记录 failed；完整副本不被删除。
6. 最终提交在 devcontainer 中运行 `scripts/ci/android.sh`：Go tests/vet、Android
   unit tests、strict verification 与 APK 构建通过。结果绑定最终源码 SHA。

## 停止与未测边界

不改 MainActivity/Compose、不提取 tokens、不加入 FGS/USB attach/自动接入/
SMB/tsnet/Dora 脚本；UI 等用户明确接受。取消要求 injected action 遵守 coroutine
cancellation；阻塞 Go I/O 的取消连接仍待真实 transport 接线，不宣称已完成。
没有 Pocket/Pixel/fnOS 时设备 AV03/AV09 与完整链路保持 NOT_RUN/BLOCKED。
完整进程 kill/partial-final 故障矩阵属于 M2，不在本次扩大范围。
容器不可用则 BLOCKED，不回退宿主工具链。计划审查 PASS 后实现，再独立审查。
