---
name: mes-ui-ux-audit
description: Execute a complete, evidence-backed UI/UX audit for Hospital Pharmaceutical MES pages using the installed ui-ux-pro-max knowledge base. Use for prototype review, formal-page visual acceptance, redesign review, or when a previous UI audit missed layout, interaction, responsive, or accessibility defects.
---

# MES UI/UX Complete Audit

Use this skill to prevent a functional or geometry-only check from being reported as a product-experience PASS. It supplements the repository DESIGN, VISUAL, and ACCEPTANCE gates; it does not replace human approval or frozen MES contracts.

## Full-dimension B端 UI & UX review — mandatory user instruction 2026-10-12

执行完整全维度 UI & UX 综合评审。严禁偷懒，严禁简化评审流程，严禁只识别一眼可见的表面问题；必须对照完整 B端 UI & UX 检查规范，不能跳过任何评审维度。适用于本技能范围内所有 B端后台页面及其列表、查询栏、表单、弹窗、抽屉、详情页，不限业务场景；不得按模块名称或页面复杂度降低标准。

在每个页面及弹窗/抽屉等独立 surface 上逐项登记以下九个维度：排版布局、字体层级、留白与元素对齐、表单规范、表格列表规范、查询筛选栏、组件交互、组件多状态、基础无障碍 WCAG 2.2。执行 [全维度规范及检查项](references/review-matrix.md#full-dimension-b端-checklist)，完成 `dimension_reviews` 和 `wcag22_checks`，并结合全部适用状态/视口逐图、逐操作、DOM测量及对抗式二次审查。八组知识检索及原有逐图八类检查继续执行，不能用九维摘要替代。

所有维度必须留下具体观察、规范依据、操作/测量方法、证据及结论。整维度不得 N/A/SKIP；若某子项所需控件不存在，先核对需求/详细设计和实际页面，记录不存在的证据及原因，不能凭方便略过。尚未执行、环境不可达或证据不足为 NOT RUN/IN PROGRESS，不能写 PASS。WCAG 2.2 是基础 A/AA 检查范围，不因截图或自动检查通过宣称完整合规认证。

普通 UI 时间预算或停止条件到达时，保留未完成维度并报告阻断；不得以预算为由缩减矩阵再宣称完整评审。G2/G5 体验整改按用户确认的每批20轮、报告后经真实批准再开新批规则；其余停止条件保留。

## Source reading

Before auditing, read `AGENTS.md`, `docs/development/MES_DEV_RULES.md`, the applicable DESIGN LOCK, Global UI V2, the selected T1–T6 template, and the exact feature requirements and detailed-design files. Read the installed `C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md` and record its SHA256. If any mandatory source is unavailable, stop.

Read [references/external-sources.md](references/external-sources.md) when the audit covers forms, validation, accessibility, or heuristic evaluation. These sources inform the audit method; they do not replace Global UI V2 or frozen MES contracts.

Use `product-design:audit` only to structure browser capture when useful. The reviewer is the actual executing agent, recorded as `Codex SELF-REVIEW` or `ChatGPT SELF-REVIEW`, using this skill and `ui-ux-pro-max`; do not describe either skill as an independent person or RPC.

## Audit workflow

### 1. Freeze scope and coverage before viewing results

Create an audit manifest using [references/manifest-schema.md](references/manifest-schema.md). Enumerate every page, modal, drawer, important state, and required viewport before capture. The default MES viewports are 1440, 1280, 900, and 375 pixels. Include at least:

- default, filled, focus, validation error, server/duplicate error, success, empty, long-content, disabled/read-only, and loading states when applicable;
- entry, query/reset, create, edit, view, save, cancel/close, return navigation, and destructive confirmation flows when present;
- role/permission variants and real 5173/8080 states during formal acceptance.

Missing coverage is `NOT RUN`, never PASS.

### 2. Query the complete relevant ui-ux-pro-max knowledge set

Run the installed `search.py` with system Python. Save raw output in the audit evidence directory. Do not use `--persist`; Global UI V2 remains the visual authority.

Run at least one focused query for each applicable group:

1. product and style fit;
2. layout, responsive reflow, overflow, long identifiers;
3. forms, labels, helper text, errors, and recovery;
4. tables/lists, row actions, density, and empty states;
5. modal/drawer focus and continuous-entry feedback;
6. accessibility, keyboard, focus appearance, target size, and state announcements;
7. typography, color, and hierarchy;
8. the implementation stack (`--stack vue` for this repository).

Record the query, returned rules, selected rule IDs or exact issue names, and why each applies or does not apply. A narrow search aimed only at the already suspected defect is incomplete.

### 3. Visual-first screenshot pass

Capture the real rendered surface before reading automated PASS/FAIL output. Inspect every saved screenshot at original resolution. Review the screenshot as a whole, not only the changed control.

For every screenshot complete all categories in [references/review-matrix.md](references/review-matrix.md): hierarchy, grid/alignment, spacing/density, typography/color, control grouping, action placement, content completeness, responsive behavior, and visible accessibility cues.

For forms, draw or calculate the shared vertical anchors. Labels and controls in the same column must form stable bands. A full-width row must align to an approved anchor; it must not begin at an arbitrary midpoint, leave an unexplained empty half-row, or make its label float between columns. Dividers must not cross controls or appear to belong to the wrong section.

Automated geometry cannot overrule visible evidence. If a screenshot looks broken, awkward, unbalanced, clipped, or confusing, record an issue even when overlap and overflow assertions pass.

### 4. Interaction and recovery pass

Operate the complete user task in the approved browser. Verify discoverability, query/reset behavior, focus order, keyboard operation, validation, error recovery, save feedback, repeat actions, cancel/return behavior, and whether the changed record is visible after success. Inspect console errors and the actual DOM state after each important action.

A message saying that a record was added does not prove that the user can see or locate it. Verify the resulting row, count, focus, and next-action readiness.

### 5. DOM geometry pass

Measure, at every required viewport:

- document and container overflow;
- label and control vertical intersection;
- repeated label/control left anchors and right edges;
- row height versus control height;
- gaps between controls, dividers, descriptions, and action bars;
- modal bounds and internal scroll regions;
- visibility of primary and row actions without undisclosed horizontal scrolling;
- 44×44px mobile targets where applicable;
- long CODE/name wrapping or truncation with an accessible full value.

Flag an alignment outlier above 8px unless the DESIGN LOCK explicitly authorizes it. For a two-column form, compare every ordinary row and every spanning row to the same anchor map. Do not limit checks to collision detection.

### 6. Adversarial second pass

After the first findings are recorded, review all screenshots again with the question: “What would make a user call this page unclear, ugly, or difficult?” Search specifically for at least one defect in alignment, whitespace, hierarchy, action discoverability, and recovery. If none exists, document concrete evidence for each category rather than writing “looks good.”

When the same agent implemented the UI, label the result `SELF-REVIEW`. Do not call it independent review. Human approval of the exact version remains mandatory.

### 7. Verdict and repair loop

Use these verdicts:

- `FAIL`: any critical/high issue, broken visual composition, missing required screenshot, incomplete core flow, or manifest validation failure;
- `IN PROGRESS`: repairs are underway or required evidence is incomplete;
- `PASS FOR PROTOTYPE`: all prototype coverage is complete, every screenshot category is signed, and no blocking issue remains;
- `READY FOR ACCEPTANCE`: formal 5173/8080 audit and all repository gates pass; human acceptance is still required.

A zero-finding audit is not automatically credible. It must include the completed screenshot matrix and adversarial-pass evidence.

For each repair, preserve before/after screenshots, issue ID, changed files, exact source hash, and recheck result. Re-run every affected viewport/state. Do not change approved requirements, thresholds, or assertions to force PASS. Follow repository time and stop limits; for mes-feature-workflow G2/G5 experience repairs, use the user-approved 20-round batch continuation rule above.

### 8. Validate the manifest

Run:

```powershell
python .agents/skills/mes-ui-ux-audit/scripts/validate_audit_manifest.py <audit-manifest.json>
```

The validator checks coverage, screenshot existence, per-screenshot review categories, open blocking issues, provenance, and verdict consistency. Validator PASS is necessary but not sufficient; the screenshots must still be inspected by the reviewer and then by the human approver.

### 9. Bind formal audits to integration acceptance

For a formal 5173/8080 acceptance, run this audit as the required G5 `ui-ux-audit` test suite. Put the validated manifest reference and `READY FOR ACCEPTANCE` verdict in `integration_acceptance.ui_ux_audit`. The reference must exactly match `product_experience_review.audit_manifest`; do not attach a summary, copied manifest, or prototype audit. If the suite did not run, either reference differs, or the verdict is not `READY FOR ACCEPTANCE`, the whole integration acceptance is `FAIL` even when API, E2E, and visual-comparison suites pass.

## Required report language

Name the actual executor as `Codex SELF-REVIEW` or `ChatGPT SELF-REVIEW`, followed by `using mes-ui-ux-audit and ui-ux-pro-max`; select the real executor, never rename an old report or claim independent QA. This identity compatibility does not grant human approval or relax any quality gate. State which parts were visual inspection, browser interaction, and automated geometry. Never say “UI UX Pro Max approved” or “Product Design approved” as if either were a person.
