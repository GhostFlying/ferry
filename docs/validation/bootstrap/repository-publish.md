# 公共仓库准备 checkpoint

记录日期：2026-10-02。本文件记录仓库发布准备与用户的明确范围决定，不是应用实现或设备验收报告。

## 用户决定与执行范围

- 用户已授权建立公开 `GhostFlying/ferry`、治理文档、milestone／DAG issue 和供用户审阅的规划 PR。
- 当前配置邮箱受到 GitHub `GH007` 私密邮箱保护，首次 seed push 被拒绝。未将具体受保护邮箱写入公共报告，未更改账号隐私设置。
- 用户随后明确授权仅 Ferry 仓库使用其 GitHub noreply 邮箱 `4019569+GhostFlying@users.noreply.github.com`；保留用户已有 `user.name` 和全局 Git 配置。重写范围仅为尚未发布的初始 seed 提交。
- 用户随后明确选择 Apache-2.0。LICENSE 采用 [Apache 官方标准全文](https://www.apache.org/licenses/LICENSE-2.0.txt)，SHA-256 为 `cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30`。新增文本和事实状态更新须独立复核后单独提交。
- 上述决定来自当前会话，由主协调代理转达具体分工；不表示用户已批准 M0 应用代码、APK Actions 或设备实验。

## 审查与提交边界

空仓库的审查对象由逐文件 SHA-256 固定，见 [治理审查](../../reviews/repository-bootstrap-review.md)与 [M0／M1 技术计划审查](../../reviews/m0-m1-plan-review.md)。许可证决定导致 seed README 和相关事实文件变化，旧快照不自动覆盖新内容；新增内容再次独立复核。

`main` 只保留最小 seed；完整文档、模板、示例和 LICENSE 通过规划分支 PR 提供用户审阅。提交仅显式 stage 已审查文件，保持原子目的，使用用户身份，不加 agent／bot co-author。实际最终 base／head SHA、远端 URL 和提交作者将由协调代理提交独立远端验收，不能凭本文件预先宣称通过。

## 当前证据边界

已完成静态文档、JSON、YAML 与审查快照核对，并核实 GitHub 仓库 public 属性。没有任何 App／AAR／APK、Actions、安装、USB／Pocket 3、SMB／飞牛、tsnet 或真机实验结果。许可证、身份和仓库准备的通过不能代替产品验收。

许可证已决定；设备访问方式和应用标识仍待对应阶段明确。M0 开始前必须取得用户对最终 M0 计划的明确批准，M1 仍须 M0 必需验收及另一次用户批准。
