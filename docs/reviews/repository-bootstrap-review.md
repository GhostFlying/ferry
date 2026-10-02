# 仓库治理准备独立审查

- 日期：2026-10-02。
- 类型：bootstrap plan 复核及静态文档／模板 implementation review。
- 作者：`/root/repo_governance`；独立审查者：`/root/review_plan`。
- 模型：审查会话继承主代理配置，未独立覆盖；具体模型／reasoning 由协调记录关联。
- 审查对象：下列 `uncommitted preparation` 文件快照。本轮读取时仓库尚无提交，因此没有可引用的完整提交 SHA。
- 结论：**PASS，限下列治理文档、模板及 README 快照。** 无未解 P0／P1／P2。此结论不是应用技术方案的最终 review，也不表示用户已批准 M0。

## 计划与范围

已先独立阅读 `docs/plans/repository-bootstrap.md`，允许其明确的治理准备范围进入实施；本次再次阅读最终修订，确认其收窄为现有工具的静态验证，没有新增校验器、Actions 或应用代码。仓库创建、提交、推送和 issue／PR 操作不属于本审查者执行范围。

实际逐文件检查了 AGENTS、协作协议、bootstrap 计划、两个 issue 表单、PR 模板和 README。作者提供的摘要仅用于定位，结论以读取的完整内容和独立检查为依据。

## 快照

| 文件 | SHA-256 |
| --- | --- |
| `AGENTS.md` | `60bfadee9d50cafac963871c26e9e5ea9b3f8be536b54de524e72b10c86f54a1` |
| `docs/agent-workflow.md` | `1b54b3c7483ab3b27768b82bab31133a890cb840d055bc09d6d81773b03644c4` |
| `docs/plans/repository-bootstrap.md` | `acae7be0d3f6dc5243110ed9af660086a0f0683f48e388769a86e858c48e0667` |
| `.github/ISSUE_TEMPLATE/task.yml` | `23bcb440b5bde86854ce3b709f66e5bb0fcba1a111d246dc325bc16c04a5e69b` |
| `.github/ISSUE_TEMPLATE/blocker.yml` | `2dbad7456fa6acd20a0018b1d9dc513c3c2d1a5adc1eac13b5b6d9238c59a3a6` |
| `.github/pull_request_template.md` | `690234e5e15fdb84a05c688e6f19f0356f58a1799a2c878da809331644c444eb` |
| `README.md` | `d7bae690522c080f030a1cce670b63363b68e365b22652bbbffecc24ba718d83` |

任何文件变化都不自动继承本次快照的 PASS。首个提交后应将快照映射至实际完整 SHA；seed README 是计划中的另一个独立内容版本，不能直接引用完整 README 的本次哈希作为其审查证明。

## 独立检查及结果

| 检查 | 结果与边界 |
| --- | --- |
| 产品与角色约束 | PASS：Android first、iOS 紧随、相机原件保留、主代理协调、实现和独立审查分离均明确。 |
| 计划与用户关口 | PASS：复杂任务先文件计划、独立计划 review、实施、独立 implementation review；本轮及阶段之间的用户决定不能被内部 PASS、CI、合并或沉默代替。 |
| DAG／worktree／所有权 | PASS：任务、依赖、基线、分支、worktree、文件所有权和独立 reviewer 可追踪；空仓库共享目录例外仅适用于明确文件所有权的规划准备；基线变化后重跑受影响检查。 |
| Git 身份与公开范围 | PASS：沿用既有用户身份，只读核实、不覆盖配置、禁止 agent／bot co-author；原子 Conventional Commit type 小写；公开内容脱敏；不未经授权联系他人。 |
| 模板字段 | PASS：任务表单 10 个非 markdown 字段、阻塞表单 8 个，ID 唯一，必需的授权、计划、DAG、所有权、验收和停止条件字段齐全；PR 对应同一证据结构。 |
| 真 APK 契约 | PASS：真实应用及对应用户授权成立后才能建设 APK Actions；AAR、文档 CI、空 job 不满足 APK 验收；产物关联 SHA、ABI、版本、工具链、run、校验值和安装启动证据。 |
| 文件与语法 | PASS：独立 `python3` 临时内联检查通过 UTF-8、末尾换行、无尾随空格；`yaml.safe_load` 解析表单并检查字段和唯一 ID；`json.loads` 解析示例。没有向仓库新增验证代码。 |
| 本地链接 | PASS：独立检查七份文件中的简单 Markdown 目标存在；跳过 fenced 示例及外部 URL，去除 fragment 后仅检查文件／目录存在。 |
| 快照一致性 | PASS：独立 `sha256sum` 与内联 Python `hashlib.sha256` 结果一致，且与作者提供的七项清单一致。 |

## 明确未验证

- 没有验证 GitHub 服务端实际渲染的 issue form 或 PR 模板，没有创建仓库／issue／PR，没有提交或推送。
- 没有运行任何 Actions、Android／iOS／Go 应用构建、APK 安装启动、SAF／USB／OTG、SMB、tsnet 或真机验证；没有占用 Dora 设备。
- 本地链接检查没有验证 Markdown 锚点、完整 Markdown 语法或外部 URL 存活。
- `examples/pocket3-fnos.json` 只验证 JSON 语法，不表示实际配置解析器存在或其语义验收通过。
- 本报告不裁定 `docs/implementation-plan.md`、`docs/plans/m0.md`、`docs/plans/m1.md` 的最终技术方案；这些文件另行独立 review。

## 授权与下一步

协调代理可依据已有用户授权安排本次仓库准备操作，实施代理仍应取得具体分工并在提交前核对快照。许可证保持待定。当前用户审阅关口继续有效；技术 PASS 不能替代用户对 M0 或下一阶段的明确批准。

## 最小 seed 追加审查

同日独立读取治理代理准备的最小 seed README 和现有 `.gitignore`，核对哈希后结论 **PASS，仅对应下列两项内容**。seed README 明确无 App／APK、完整规划通过 PR、实现等待用户批准、许可证待定；没有链接尚未提交的文件。`.gitignore` 覆盖本地凭据、运行状态和原生构建／签名文件。忽略规则不能替代公开前逐文件检查。本审查没有执行 seed 提交或远端操作。

| seed 文件 | SHA-256 |
| --- | --- |
| `README.md`（独立准备的最小内容版本） | `50f50e32c1d9c62e0e2c9e68db91ba684d945b7243beb7ad9110183bae94567d` |
| `.gitignore` | `59c1c29e6fba06ca8fe89cefe048b9a65578fe4a6d3c43b273854577e3cdbfbf` |

## 用户许可决定后的增量复核

2026-10-02，用户明确选择 Apache-2.0，并明确授权仅 Ferry 本地配置使用其 GitHub noreply 邮箱、保留 user.name 和全局配置，重建尚未发布的 seed。本节记录取代前文相应“许可证待定”状态；其余审查历史保留。

结论：**PASS，限新增 LICENSE 和以下事实更新。** 独立从 [Apache 官方文本](https://www.apache.org/licenses/LICENSE-2.0.txt) 下载并逐字节比较本地 LICENSE，11358 bytes 完全一致，SHA-256 如下。没有改写许可条款或加入额外条款。

已读取新的 seed README、AGENTS、完整 README、协作协议、bootstrap 计划、产品计划和发布 checkpoint。新 seed 无悬空链接，仍明确无 App／APK 及 milestone 用户关口。治理文件记录已选许可证，应用标识／正式分发和 M0 授权仍未确定；checkpoint 不包含受保护的旧邮箱。现有 Git 身份一般规则继续有效，用户此次明确授权仅覆盖本次指定的 local noreply 调整。

| 新增／更新内容 | SHA-256 |
| --- | --- |
| `LICENSE` | `cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30` |
| seed `README.md` | `20b1a7e54013904cfdba6e96516e045bf0cd5ab3825f1816c7a4bcc8890d1991` |
| `AGENTS.md` | `7c859542d6cde27833ba382c1a5b788ab7894ce07596fd02621789abb14d819d` |
| 完整 `README.md` | `5342fd45e039e49c4dcb90420aeeeb7ea22a3cded335b61fcfb92dc732c15c0f` |
| `docs/agent-workflow.md` | `5f2bb04c230744f00abbb2a7720c9cdfce048a678b8869433140cfa67876344f` |
| `docs/plans/repository-bootstrap.md` | `5186006a03acc8139e5434a4f80056d467f61cd5ab18621badd484a9747e6574` |
| `docs/product-plan.md` | `baee9d7c3440663987a7908ebc6a30233a77af81c83b59579dc6f09341bc4747` |
| `docs/validation/bootstrap/repository-publish.md` | `c1e1d09978b08f95793042d9dbe1654d811f7ae2a1fcc45404c9482e44edd4ec` |

独立静态格式和简单本地链接检查通过；`.gitignore` 未变。`gh repo view` 此时确认 `GhostFlying/ferry` 为 PUBLIC，默认分支名仍为空；本次增量复核不预先证明后续提交 metadata、远端 tree、issue DAG 或 PR 已验收。远端发布另行审查，不影响此前明确的应用／CI／设备未测边界。
