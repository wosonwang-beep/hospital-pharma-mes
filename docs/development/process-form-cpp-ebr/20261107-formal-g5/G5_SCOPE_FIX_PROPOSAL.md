# G5 PC-only 已批准范围与正式路由校验器不一致（建议修复，不是已批准代码变更）

状态：**APPROVED PC_ONLY PATH MAPPING APPLIED / G5 STILL NOT READY**

## 2026-11-07 18:26：正式代码路径识别修复状态

本机唯一 main 的审查器已根据既有 `PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md` 记录（文件SHA保持原批准值）修正极窄的正式生产工艺根路由识别：仅接纳 `http://127.0.0.1:5173/process/packages`、`http://localhost:5173/process/packages`，保留原 `#/processes`，没有扩展到其它模块、嵌套详情或任意域名。9组正式校验器正负用例全部PASS；原正式manifest校验器从“路由/375+屏幕阅读器”三个错误变为**仅实际屏幕阅读器播报 `interaction_checks[14] must PASS`**，仍退出1。路径识别修复不是 G5 放行或最终 ACCEPTED，19项WCAG整体验证及最终人工接受仍独立保留。

旧正文以下保留作为**当时的变更前分析与验证提案历史**，不再表示尚未应用。

## 已核实事实

- 原批准范围：`docs/development/decisions/PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md`，本生产工艺任务没有手机端，正式体验矩阵为 PC 1440/1280/900；375 手机格已由用户明确撤出，不是缺陷豁免。
- 正式生产工艺路由：`http://127.0.0.1:5173/process/packages`，不是旧 mes-demo 原型的 `#/processes`。
- 现行 `.agents/skills/mes-ui-ux-audit/scripts/validate_audit_manifest.py` 仅按 `source.url.split('#')[-1].rstrip('/') == '/processes'` 识别 PC_ONLY，匹配原型路径才启用三宽矩阵。这导致正式 G5 的 `source.url` 即便附带相同已批准文档及其精确 SHA256，仍无条件要求375手机格。
- 现行校验器还检查审查来源、完整截图覆盖、每张八维评价、九维审查矩阵、19条WCAG、所有交互/几何核查、二次对抗和 blocker 闭环。**即便修复路径识别，若上述未完成仍将 FAIL。**

## 仅供治理负责人审查的最小修复建议

在保留已批准PC_ONLY引用和 SHA256 严格比对、且不能扩大到其它模块的前提下，让该任务的**正式生产工艺根路由** `/process/packages` 也符合已批准的PC_ONLY适用路径；不得自动将所有 `/process/*` 路由放行，更不得在其它任务上撤消375要求。

该修复仅是已批准范围在正式路线上的映射，不应更改审查成功条件：缺少任何适用三宽图、任意高风险问题未闭环、WCAG缺项、独立/人工验收不满足时仍判 FAIL。

在变更提案起草当轮**未修改** validator 源码、未伪造原型路由、未降低标准或将 G5 标记 PASS/READY。正式路径认可与最终人类 G5 接受仍需单独治理批准。

## 当前实证

`docs/development/process-form-cpp-ebr/20261107-formal-g5/source-final/audit-manifest.json`：当前正式源码 30 张原始截图，代表性 17 组文字对比度、各宽度键盘/焦点与真实业务 E2E 均有证据。完整 19 条WCAG和全角色登录矩阵仍有缺项。校验器实际结果 **EXIT 1 / FAIL**，不是 READY。
