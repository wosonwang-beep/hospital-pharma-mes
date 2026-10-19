# MES UI/UX 完整审查技能与开发门禁变更记录（2026-10-12）

## 授权与问题

用户要求把反复遗漏的页面体验问题固化为完整技能，并要求 `ui-ux-pro-max` 执行完整审查。数据字典新增页已出现肉眼可见的表单栅格断裂：描述行从页面中部开始，左侧形成无解释的大面积空白，标签与普通表单行锚点不一致；既有功能、DOM几何和 Product Design 审查仍错误给出 PASS。

责任归因：执行审查的 Codex 未建立完整覆盖矩阵、逐图视觉记录、表单锚点门禁和对抗式复查，并把技能/插件描述成了独立审查者。既有数据字典体验 PASS 撤回。

## 已批准的有界变更

本次只完善开发治理、审查技能和机器门禁，不修改业务字段、API、权限、数据库、Flyway、正式页面或原型页面。

- 新增 `.agents/skills/mes-ui-ux-audit/`，强制结合已安装 `ui-ux-pro-max`。
- 审查前枚举页面、弹窗、状态和1440/1280/900/375视口。
- 完整检索产品/风格、布局、表单、表格、弹窗、无障碍、字体颜色和Vue栈八组知识。
- 先逐张查看原始分辨率截图，再读取自动结果；逐图记录八个体验类别。
- 测量同列标签/控件及跨列行锚点；无设计依据的8px以上离群判失败。
- 实际执行完整任务、错误恢复和重复操作；再做一次对抗式复查。
- 用 `validate_audit_manifest.py` 拒绝缺截图、缺分类、缺证据、交互/几何失败、未关闭CRITICAL/HIGH或伪造PASS。
- 执行者固定披露为 `Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max`；Product Design可用于设计和截图流程，不再写成独立审查人。
- G2原型和G5正式验收的 `mes-feature-workflow` 均要求清单验证PASS。

## 参考来源

具体URL及采用原则记录在 `.agents/skills/mes-ui-ux-audit/references/external-sources.md`。参考范围为 OpenAI Skills结构与评估、WCAG 2.2/WAI表单、Ant Design表单与对齐、GOV.UK错误恢复，以及 Nielsen Norman Group启发式评估。它们补充审查方法，不覆盖Global UI V2、T1–T6或业务基线。

## 验证结果

- `quick_validate.py .agents/skills/mes-ui-ux-audit`：PASS（使用含PyYAML且启用UTF-8的本机cuawright虚拟环境）。
- `py_compile`：新清单校验器、MES工作流和工作流测试均PASS。
- 清单校验器正例：完整4视口/8查询组/8逐图类别/交互/几何/对抗式证据，exit 0。
- 清单校验器反例：删除栅格类别并保留开放HIGH问题，exit 1。
- `mes-feature-workflow` 单元测试：53项PASS，94.917秒；包含G2/G5清单门禁，以及G5缺少UI/UX套件、缺少集成绑定、清单引用不一致三类失败用例。
- `git diff --check`：目标文件PASS，仅显示既有行尾转换提示。

## 当前数据字典结论

原型功能检查结果可以保留为历史事实；既有产品体验审查 PASS 已撤回。当前状态为 `COMPLETE RE-AUDIT REQUIRED / NOT FORMALLY INTEGRATED`。必须先按新技能重新审查并修复可见栅格问题，再提交确切原型版本供人工批准。本文不构成原型批准或正式发布。

## 集成测试验收绑定补充

用户于2026-10-12进一步要求把UI/UX审查技能放入集成测试验收。G5新增必需`ui-ux-audit`测试套件；`integration_acceptance.ui_ux_audit`必须记录`PASS`、`READY FOR ACCEPTANCE`及标准审查清单引用，并与`product_experience_review.audit_manifest`完全一致。套件缺失、引用不一致或结论未就绪时，工作流直接判定整个集成验收失败，不能由API、E2E或视觉对比套件的通过抵消。
