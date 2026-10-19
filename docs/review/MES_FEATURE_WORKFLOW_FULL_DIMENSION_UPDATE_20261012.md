# MES 全维度 UI & UX 工作流更新 — 2026-10-12

1. **基线依据**：实际读取 AGENTS.md、MES_DEV_RULES.md、现有两个技能、审查矩阵、清单格式和门禁。保留 v1.0.23、UI V2/T1–T6；用户明确要求禁止偷懒/简化/只查表面问题，全部 B端页面及列表/查询栏/表单/弹窗/抽屉/详情完整覆盖。参考并实际核对 W3C WCAG 2.2：https://www.w3.org/TR/WCAG22/ 。
2. **设计差异**：新增排版布局、字体层级、留白对齐、表单、表格列表、查询筛选栏、组件交互、组件多状态、基础 WCAG 2.2 九维矩阵；每个 surface 全覆盖，整维度不能 N/A/SKIP。19项 WCAG 最低基础准则有检查证据，适用其它准则须追加，不声明完整认证。八组知识检索、逐图八类检查、交互/恢复、DOM及对抗复查保留；G1仅设计审查并登记动态待测，G2/G5门禁实际运行清单校验器，缺维度/未运行/缺证据阻断。每批20轮后报告并经真实批准重开计数规则保留，其它停止条件不变。
3. **修改范围**：本次更新14个既有文件（审查技能在本次启动时已存在但未跟踪，保留其原有内容）；实际列表如下。未修改全仓 AGENTS/MES_DEV_RULES、业务源码、UI、数据库、迁移或启用CI。

- .agents/skills/mes-feature-workflow/SKILL.md
- .agents/skills/mes-feature-workflow/references/workflow-v2-source.md
- .agents/skills/mes-feature-workflow/references/tool-handoff.md
- .agents/skills/mes-feature-workflow/scripts/workflow.py
- .agents/skills/mes-feature-workflow/scripts/tests/test_workflow.py
- .agents/skills/mes-ui-ux-audit/SKILL.md
- .agents/skills/mes-ui-ux-audit/references/review-matrix.md
- .agents/skills/mes-ui-ux-audit/references/manifest-schema.md
- .agents/skills/mes-ui-ux-audit/scripts/validate_audit_manifest.py
- docs/governance/MES_FEATURE_WORKFLOW.md
- docs/governance/MES_GATE_POLICY.md
- docs/templates/UI_REVIEW_TEMPLATE.md
- docs/templates/PROTOTYPE_ACCEPTANCE_TEMPLATE.md
- docs/templates/INTEGRATION_ACCEPTANCE_TEMPLATE.md

4. **验收证据**：两技能 quick_validate exit=0；scoped diff check exit=0；实际 Codex skills/list 识别PASS，显式调用保留。工作流正反测试 57项全通过，failures/errors/skipped=0，耗时116.031秒；每个九维漏项（G2/G5）、缺WCAG、整维度N/A/NOT RUN及伪填PASS但缺证据均被拒绝。测试为工作流模拟fixture，不是实际产品体验验收。

- docs/review/evidence/mes-feature-workflow-full-dimension-20261012-selftest.json
- docs/review/evidence/mes-feature-workflow-full-dimension-20261012-selftest.xml
- docs/review/evidence/mes-feature-workflow-full-dimension-20261012-discovery.json
- docs/review/evidence/mes-feature-workflow-full-dimension-20261012-validation.json

5. **结论**：工作流更新 READY FOR ACCEPTANCE。未执行真实功能/原型审查，业务G1–G5 NOT RUN；后续仍需真实浏览器与有效人类确认。机器只检验覆盖/证据/绑定，不能判断审美或证明所有操作确已执行。无发现的阻断性工作流问题；历史验收记录保留。
