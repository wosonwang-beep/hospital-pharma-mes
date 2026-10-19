# MES 工作流产品体验审查更新 — 2026-10-12

本次仅更新显式调用的 mes-feature-workflow，未启动业务开发或实际页面体验验收。历史安装验收及此前需求先读更新证据保留。

1. **基线依据**：实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md、skill-creator、现有 Skill、工作流规范、门禁政策及 G2/G5 验收模板。沿用 FINAL BASELINE COMPLETE v1.0.23、Global UI V2/T1–T6。用户本次确认 G2/G5 增加 UI UX Pro Max 体验审查、按问题整改及复查；又确认每批20轮后报告，真实批准后新批从第1轮开始，历史记录保留但不累计扣减新批额度，其余停止条件保留。这是本次 Skill 体验整改范围内的明确覆盖，不修改全仓规则。
2. **设计差异**：G2 对实际运行 Vue 原型审查，G5 对真实5173/8080正式系统独立审查；要求需求/详细设计输入、美观/可读性/交互检查、问题整改前后证据、UI UX Pro Max 复查与确切源码版本确认。工具结论不代替人工批准。G2/G5 新增强制 product_experience_review 产物，脚本核对运行环境、源版本、每页面三类覆盖、证据哈希及问题闭环；不自动判断审美、不通过名称推断真实工具执行，也不自动运行或批准下一批整改。
3. **修改范围**：本次修改以下9个既有文件；新增本报告和下列验证证据。无新增 Migration，未修改数据库、业务源码、正式UI、AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md 或启用 CI；未操作业务数据。

|实际修改文件|用途|
|---|---|
|.agents/skills/mes-feature-workflow/SKILL.md|G2/G5入口与经用户确认的批次规则|
|.agents/skills/mes-feature-workflow/references/workflow-v2-source.md|第二/第五阶段体验审查、整改、复查及批次规范|
|.agents/skills/mes-feature-workflow/references/tool-handoff.md|真实工具/浏览器审查与双向问题交接|
|.agents/skills/mes-feature-workflow/scripts/workflow.py|强制体验报告与有效性检查|
|.agents/skills/mes-feature-workflow/scripts/tests/test_workflow.py|更新正向fixture，新增7项反向检查|
|docs/governance/MES_FEATURE_WORKFLOW.md|阶段交付及批准后重新计数规则|
|docs/governance/MES_GATE_POLICY.md|体验报告字段、证据绑定和脚本边界|
|docs/templates/PROTOTYPE_ACCEPTANCE_TEMPLATE.md|原型体验问题、整改、复查及批次记录|
|docs/templates/INTEGRATION_ACCEPTANCE_TEMPLATE.md|正式产品体验问题、整改、复查及批次记录|

4. **验收证据**：skill-creator quick_validate 输出 `Skill is valid!`、exit=0；scoped `git diff --check` exit=0。实际 Codex app-server skills/list 识别项目 Skill，enabled，explicit_only=true，未创建业务任务。工作流 self-test **49项全部通过**，failures/errors/skipped=0，耗时70.406秒、exit=0。包括 G1–G5 正向链及原有反向测试，新增缺体验报告阻止 G2/G5 ACCEPTED 推进、Mock/静态环境、旧源版本、未解决阻断、缺复查证据及页面/审查范围不完整等反向测试。测试仅运行隔离工作流模拟fixture，截图及测试身份明确标为模拟，不能计作真实MES体验验收。

|新增证据文件|实际状态|
|---|---|
|docs/review/evidence/mes-feature-workflow-experience-20261012-selftest.json|PASS，逐测试结果、数量、耗时及源码哈希|
|docs/review/evidence/mes-feature-workflow-experience-20261012-selftest.xml|JUnit 49/49|
|docs/review/evidence/mes-feature-workflow-experience-20261012-discovery.json|真实Codex识别PASS，显式调用保留|

5. **结论**：工作流更新 READY FOR ACCEPTANCE；无发现的阻断性工作流问题。未执行任何真实功能的 G2/G5 体验审查，业务门禁状态 NOT RUN；后续执行必须取得实际浏览器证据及有效人工确认。每批20轮后报告/批准再开新批由执行者依据本次用户指令及模板留记录，脚本不自动计数或放宽时间、设计和其它停止条件。未启动下一功能。
