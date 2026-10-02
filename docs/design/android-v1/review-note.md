# Android 候选图作者静态检查

这是作者的图集生成与静态检查记录，不能替代独立 review，也不能替代用户接受。设计状态：`awaiting-user-acceptance`。

Checkpoint ID / UTC timestamp / task / milestone: DESIGN-ANDROID-V1 / 2026-10-02 11:10 UTC / M0-DESIGN1 + M1-DESIGN1 / M0 + M1 proposed
Status: awaiting_user_review
Owner: /root/android_concepts
Independent reviewer: /root/review_plan (independent report maintained outside this directory)
Model / reasoning: assigned by root; allocation is recorded by the coordinator, not inferred here
Plan: ../../plans/android-first-design-revision.md
Current base full SHA: f86441ff3caa32f9eca2b47b0d32de6a99cad0e5
Design snapshot: uncommitted preparation; exact selected PNG and prompt SHA-256 in manifest.json
Changes: functional brief -> 19 built-in generated results -> 11 selected complete portrait concepts -> static inspection and revision
Validation: all selected and edit-input images directly inspected with view_image; PNG IHDR/hash/source equality metadata checked with Python standard-library read-only byte inspection
Evidence boundary: candidate raster concepts only; no accepted design, native/browser implementation, runtime, APK, SMB, USB or device test
Next authorized action: independent artifact review and user review; no App implementation
User milestone/design decision reference: pending; never inferred from reviewer PASS

## 已检查的内容与修正

逐张检查中文文案与按钮、信息层级、导航一致性、状态与实际规则、字体可读性、白底／强调色、列表与间距、裁切、安全边界。这些是图内静态检查，不是概念与实现的 fidelity 比较。

| 检查点 | 结果／证据 |
| --- | --- |
| 主要 screen 完整性 | 11 张独立 edge-to-edge 竖屏；无拼贴、无设备框；每张可单独阅读。像素尺寸逐张见 manifest，不声称测试了对应手机 viewport |
| 导航与产品范围 | M1 固定任务／来源／目标／规则；M0 单独诊断；无早期 iOS、自动接入、OpenDAL 或网盘 |
| 数据完整性语义 | 导入先形成完整手机副本；已写入不冒充已完成；读回中／失败保留副本；不覆盖同名内容、来源只读 |
| 并发状态 | A01 v1／v2 同时画上传和另一条读回，违反单网络任务限制；v3 仅主上传与单导入并行，另一条网络任务排队；A10 是独立读回状态 |
| 用户可操作文案 | A03 改成重选目录提示；A04 改真实 NAS 用户名／主机／共享输入，Wi-Fi 用实际 switch，连接显示尚未测试 |
| 模板字段契约 | A05 v1 的额外点／单下划线错误；v2 精确为 `{yyyy}/{mm}/{dd}/{stem}__{sha256}{ext}`，与 examples/pocket3-fnos.json 一致；完整哈希变量、unknown-date 与待内容确定都可读 |
| 空间与暂停 | A06 v2 明确其他任务副本；A09 v2 明确打开仍保持暂停、恢复从头重传，不承诺字节范围续传 |
| 失败原因边界 | A07 v2 不把摘要不一致归因于连接故障；仅说明可从手机完整副本重新上传 |
| M0 初始与失败 | A11 前置未满足时禁用开始，提供来源／构造文件／桥接／连接入口；A02 示读回失败／重试／取消入口，明确测试素材与 Pocket USB 未验证 |
| 安全与 fixture | 没有真实密码、auth key、节点身份、私人素材或实际网络配置；DEMO 名称、状态、字节、摘要、容量、时间都是 fixture |

PNG 是概念资产，不可作为 UI 位图交付。以后控件与文字应通过 Kotlin／Compose 原生实现。review-only footer 不进入产品；真实状态名称与安全行为不会因为值是 fixture 而被取消。

最终文档复核已将 brief 中的旧“账号引用”改为 NAS 账号用户名输入，并将网络修订／并发修正改为已完成状态。brief 和 prompt 索引明确只保留旧稿原始工具缓存与记录，旧稿工作区 PNG 已清理；没有修改任何选定 PNG。

中文主要文案没有发现实质错字、缺字或裁切。A07 v2 的 review-only footer “证据”附近有轻微下划线样笔画，该注释不属于未来产品 UI；manifest 如实记录。A09 的“今天 09:12”和 A03／A11 的解释句为生成时补充的 fixture／相同流程内文案，已逐图检查，不当作真实操作记录。各图的局部留白、图标、状态条差异仍是候选视觉，用户接受前不抽取生产 tokens。

## 覆盖范围与后续关口

当前是主要完整 screen 与关键状态的提案，并不是全部 M0-A5／M1-P5 可见 UI 的无条件规格。A11 只展示“配置测试服务”入口；A04 的 M1 导航和字段不能直接代替 M0 设置 surface。规则子编辑、规则启停／顺序／时区／最小大小、缓存／保留配置、丢弃确认、同名冲突、其他错误与前台等待详细状态尚未图示。manifest 逐项列出未覆盖状态；增加 Ferry 自绘 surface 前必须先生成并接受对应概念。系统 picker 与外部登录属于平台页面，本轮无须穷举，也没有伪造。

本轮没有实现截图，所以 native fidelity ledger、above-the-fold implementation copy diff、Browser／Playwright、交互路径、可访问性、APK、USB、SMB、设备与性能验证均为 NOT_RUN；没有“10/10 实现还原”结论。

19 张生成结果中，8 张 superseded 工作区副本在所有 Image Gen 输入调用完成、哈希固定后移除；原始 `$CODEX_HOME/generated_images` 文件保留，完整 source／hash／prompt／原因见 manifest。仓库只发布 11 张当前候选 PNG 和文档记录，避免把逻辑错误旧稿误认作当前设计。

下一步交独立 reviewer 检查选定图、manifest 和真实变更，再交用户接受所覆盖图。用户接受前不提取生产 tokens；对应 milestone 未获明确实现批准前不写 App 代码或启动设备验收。
