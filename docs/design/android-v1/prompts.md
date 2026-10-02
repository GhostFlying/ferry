# Image Gen prompt 记录

状态：`awaiting-user-acceptance`。模式：默认 built-in `image_gen__imagegen`；初次生成 use case 为 `ui-mockup`，定向文案修订为 `text-localization`；透明背景关闭。所有图是 design fixture，不是运行证据。

## A01 — 任务主屏

```text
Use case: ui-mockup
Asset type: one complete Android app portrait screen, high-fidelity design concept only
Primary request: Design the actual task home screen for Ferry, a restrained professional Chinese media backup app. Produce ONE large readable full portrait Android screen, approximately phone ratio 9:20, edge-to-edge screen only, no device bezel and no surrounding presentation board. This is a candidate visual design, not a real app screenshot.
Style: true white background, charcoal typography, one restrained deep teal accent. Very clear Chinese sans serif text, airy but functional. Open sections and list rows separated by fine rules, not a stack of rounded cards, not a marketing hero. Native Compose feasible controls. Modest outline icons only where useful. Top Android status bar and bottom system gesture area.
Composition: header Ferry with page title 任务. Keep the following workflow clearly readable in a single complete screen. Current upload is focal; list below shows independent stages. Use exact Chinese copy below and conventional bottom navigation: 任务 / 来源 / 目标 / 规则, selected 任务. No additional tabs, metrics, decorations, or automatic-mode switch.
Text (verbatim):
Header: Ferry / 任务
Source row: SOURCE-DEMO / 来源可访问 · 只读
Context: 待导入 2 · 待上传 1
Current operation: 正在上传 / DEMO_003.mp4 / 2.4 GB / 4.8 GB / 手机完整副本已就绪 / NAS-DEMO · 内嵌 tsnet · Wi-Fi
Primary control: 暂停任务
Section: 任务列表
Rows: DEMO_004.mp4 / 导入中 · 1.2 GB / 3.6 GB
DEMO_002.mp4 / 内容校验中 · 读回 0.8 GB / 2.0 GB
DEMO_001.mp4 / 已完成 · 远端 SHA-256 已验证
DEMO_005.wav / 用户暂停 · 打开 App 不会恢复
Storage: 手机副本 9.2 GB / 可用空间 18.6 GB
Run condition: 仅在前台运行，离开后保留任务
Small footer: 设计示例，非运行证据
Constraints: Every progress value and connected/completed label is an illustrative design fixture. Keep all Chinese legible and exact. Do not show imaginary cloud services, iOS UI, background automation, auto-attach, speed/ETA, trophies, badges, private addresses, passwords, real images or real Tailscale node details. Backup workflow is readonly source -> full persistent private phone copy -> SMB upload -> remote full SHA-256 readback -> completed. Preserve camera originals. All eventual controls and text must be native code, never shipped bitmap UI.
```

其他页面使用同一 A01 图作为 style reference；下面的每页完整 prompt 在调用前追加。结果与静态检查见 `manifest.json` 与 `review-note.md`。

| ID | 实际完整 prompt | 输入角色 |
| --- | --- | --- |
| A02 | [a02.txt](prompts/a02.txt) | A01 style reference only |
| A03 | [a03.txt](prompts/a03.txt) | A01 style reference only |
| A04 | [a04.txt](prompts/a04.txt) | A01 style reference only |
| A05 | [a05.txt](prompts/a05.txt) | A01 style reference only |
| A06 | [a06.txt](prompts/a06.txt) | A01 style reference only |
| A07 | [a07.txt](prompts/a07.txt) | A01 style reference only |
| A08 | [a08.txt](prompts/a08.txt) | A01 style reference only |
| A09 | [a09.txt](prompts/a09.txt) | A01 style reference only |
| A10 | [a10.txt](prompts/a10.txt) | A01 style reference only |
| A11 | [a11.txt](prompts/a11.txt) | A01 v3 style reference only |

A03 v2 已将内部身份解释改为可操作重连／授权提示，实际 prompt 见 [a03-v2.txt](prompts/a03-v2.txt)。A04 v2 已将内部账号引用改为实际 NAS 账号用户名输入占位，见 [a04-v2.txt](prompts/a04-v2.txt)。原始工具缓存保留，旧稿工作区 PNG 已清理；最终候选版本与历史 source／hash 见 manifest。

A05 v2 纠正真实模板契约为 `{yyyy}/{mm}/{dd}/{stem}__{sha256}{ext}`，见 [a05-v2.txt](prompts/a05-v2.txt)；A06 v2 明确其他任务副本与用户释放空间动作，见 [a06-v2.txt](prompts/a06-v2.txt)。

A07 v2 不再将摘要不一致归因于连接故障，见 [a07-v2.txt](prompts/a07-v2.txt)；A09 v2 在恢复控件附近明确文件级重传，见 [a09-v2.txt](prompts/a09-v2.txt)。

A01 v3 功能一致性修订见 [a01-v3.txt](prompts/a01-v3.txt)：输入 v2 为 edit target，仅将第二网络任务改为待上传，保留单上传／单导入并行；v1／v2 不作为最终候选。

## A01 v2 — 产品网络名称修订

模式为 built-in edit；当时输入 A01 原图为 edit target，先经 view_image 检查并生成独立结果，没有覆盖原图。原始工具缓存保留；旧稿工作区 PNG 已在全部调用完成后清理，历史 source／hash 见 manifest。

```text
Use case: text-localization
Asset type: candidate Android task home concept
Input image: a01-tasks-active.png is edit target. Keep all layout, typography, hierarchy, white background, accent, exact labels, icons, list rows and state/progress data unchanged. Change only the visible network description line from "NAS-DEMO · 内嵌 tsnet · Wi-Fi" to "NAS-DEMO · 应用内 Tailscale · Wi-Fi". Ensure the new line remains fully legible within its width; no overflow, no cropping. No other content changes. Preserve review-only footer "设计示例，非运行证据"; this footer is an annotation for concept review only, not future product copy. Every DEMO name/state/progress remains a design fixture, not evidence. One whole edge-to-edge phone screen, same approximately 9:20 ratio, no bezel or presentation board.
```
