---
name: mes-feature-workflow
description: Explicitly invoke with $mes-feature-workflow or 使用MES标准流程开发XX功能 / 按MES标准流程执行. Govern hospital-pharma-mes menus, features, pages, T1–T6 and substantial contract changes through five ordered stages and executable G1–G5 gates. Ordinary development requests do not enable it.
---

# MES 功能开发主控

仅用于 `D:\codex\_project\gmp\hospital-pharma-mes`。用户于本次安装中明确选择 **仅显式调用**；不自动匹配任务，CI 仅保存未启用示例，未修改 AGENTS.md 或任何既有工程规则。在现有 main 工作区操作；不建立第二 checkout，不自动启动业务开发。

显式自然语言别名：`使用 MES 标准流程开发 XX 功能`、`按 MES 标准流程执行`（大小写/空格不敏感）。这是用户明确启用该技能的语义约定，不是注册了不存在的平台别名 API；平台最明确的调用形式仍是 `$mes-feature-workflow`。`route-request` 只辅助识别，不自动运行开发。未带这些明确指令的普通任务不加载此流程。

每次启动先真实读取 `AGENTS.md`、`docs/development/MES_DEV_RULES.md`、`MES_TASKS.md` 对应任务和 `docs/PROJECT_BASELINE.md` 权威路由；检查 Git 未提交修改。定位一个功能编号和任务卡，读取其必要基线章节、确认记录和文件白名单。已 ACCEPTED 的任务不能重做。业务权威仍是 v1.0.23 + 已批准限定 DCP，视觉仍是 Global UI V2/T1–T6。

完整规范见 [workflow-v2-source.md](references/workflow-v2-source.md)（用户规范及经确认的修订，十五章均须遵守）；工程落地入口为仓库 `docs/governance/MES_FEATURE_WORKFLOW.md`、`MES_GATE_POLICY.md`、`MES_CHANGE_CONTROL.md`。治理文件不替换业务基线或 MES_TASKS.md 的任务状态。

## 固定执行顺序

凡使用 UI UX Pro Max 审查，必须执行完整全维度 B端 UI & UX 综合评审：严禁偷懒、严禁简化流程、严禁只识别一眼可见的表面问题；逐项对照 `mes-ui-ux-audit/references/review-matrix.md` 完整规范。排版布局、字体层级、留白/对齐、表单、表格列表、查询筛选栏、组件交互、组件多状态、基础 WCAG 2.2 九维全部登记，不能跳过。适用于本流程所有 B端后台页面及列表/查询栏/表单/弹窗/抽屉/详情，不限业务场景。G1 核对设计规范及全部维度的设计依据，动态项记录待 G2 实测；G2/G5 执行实际浏览器全覆盖及清单校验，NOT RUN 或缺证据不得 PASS。已有八组知识检索、逐图八类检查、DOM测量、完整业务操作和对抗式复查全部保留；时间不足则报告未完成，不缩减评审。

1. **UI UX Pro Max**：实际读取该功能的需求文档和当前适用的详细设计文件，记录路径、版本及所读章节；先分析业务目标、角色、流程、字段来源/校验、状态、权限、接口和异常边界，再读取已有 token、页面目录及 T1–T6 出设计稿。关键资料缺失或存在未批准的契约冲突时停止，不凭空补业务设计；G3 仍负责后续完善与冻结。使用已安装 `ui-ux-pro-max`，不是不存在的 `ui-ux-pro-max-skill` 别名。按参考规范 §4 检查美观、可读性和操作友好性，记录具体设计依据和问题；把业务分析、需求到页面映射及质量检查交给 Product Design 原型阶段验证。设计稿、字段/按钮/状态、标注、PC/Mobile、功能映射和审查齐全后取得具体版本确认，执行 G1。
2. **Product Design + MES UI/UX 完整审查**：读实际安装的 Product Design router 和所需子技能。只能交付 Vue 3/TypeScript/Vite/Ant Design Vue/Pinia/Vue Router；不支持时 BLOCKED，按 [tool-handoff.md](references/tool-handoff.md) 交接并取得构建方式批准。原型源码只在真实 `D:\\codex\\_project\\gmp\\mes-demo`，5174；功能证据记录路径、提交或经批准的哈希例外，不复制另一套源码。原型运行后，强制执行仓库 `mes-ui-ux-audit` 技能；该技能调用 `ui-ux-pro-max` 完成全组知识检索、视觉优先逐图审查、任务/恢复操作、栅格测量和对抗式复查，并用清单校验器阻断缺证据的 PASS。执行者记录为 `Codex SELF-REVIEW`，Product Design 只承担设计/截图工作流，不能被描述成独立审查人。按规范 §5.4A–B 保存问题、修改前后证据和批次记录；编译、启动、交互、控制台、截图、体验审查闭环、清单验证及整改后具体源码版本的人工确认齐备才执行 G2。
3. **详细设计冻结**：对 PRD、字段、实体/ERD、字典、API/错误码、状态机、权限、幂等、审计/GxP、Migration、测试/RTM 做一致性核对；形成唯一冻结清单和 DESIGN LOCK，再执行 G3。
4. **Codex 工程集成**：只有 G3 与前序人工确认有效时，直接复用已验收原型，替换 Mock 适配层，按冻结设计实现真实 API、权限、状态/校验及适用签名审计。保存源码继承关系；禁止重新设计 UI，执行 G4。
5. **真实集成验收 + MES UI/UX 完整审查**：实际 5173/8080 HTTP、角色正反例、状态流转/关联数据、数据库验证、E2E、视觉/DOM、异常边界及必要回归；必须运行G5 `ui-ux-audit`套件，在正式运行页面重新执行 `mes-ui-ux-audit`，不得复制 G2 结论。Codex 在批准范围内按问题修复并重跑受影响检查，再以 `Codex SELF-REVIEW` 复查并验证审查清单；把同一清单引用和`READY FOR ACCEPTANCE`结论写入`integration_acceptance.ui_ux_audit`。套件缺失、清单不一致或结论未就绪时整个集成验收FAIL。冻结设计变更先批准、更新原型/文档并重新冻结。体验问题闭环后形成报告和最终人类确认，执行 G5。仅自动测试或工具体验验收通过最多 READY FOR ACCEPTANCE；只有真实最终批准可在 MES_TASKS.md 标记 ACCEPTED。

## 命令与边界

从仓库根目录运行：

```powershell
python .agents/skills/mes-feature-workflow/scripts/workflow.py init --feature MES-NEW-001
python .agents/skills/mes-feature-workflow/scripts/workflow.py advance --feature MES-NEW-001 --gate G1 --state IN_PROGRESS
python .agents/skills/mes-feature-workflow/scripts/workflow.py run-evidence --feature MES-NEW-001 --gate G1 --suite design-review
./scripts/validate-workflow.ps1 --feature MES-NEW-001 --gate G1
```

`init` 只创建任务骨架，不批准设计、不运行代码。填写任务卡、workflow.yaml（JSON-compatible YAML）和产物后，向有效批准主体交付审查包。缺少签名信任配置、工具、原型或批准则保持 BLOCKED/REVIEW_PENDING；不得自行生成真实审批密钥或调用 sign-approval 伪造用户批准。完整 CLI、证据格式与 CI 边界见 `docs/governance/MES_GATE_POLICY.md`。

每个阶段有 NOT_STARTED/IN_PROGRESS/REVIEW_PENDING/BLOCKED/REJECTED/ACCEPTED 状态。先前阶段必须 ACCEPTED 且重新验证有效才能开始后续阶段；通过脚本不等于业务批准。低风险简化不能用布尔 skip 绕过门禁，须签名范围决策并保留必要回归。

遵守既有 READ/DESIGN LOCK/REUSE/WRITE/VISUAL/ACCEPTANCE/ANTI-LOOP gates。用户2026-10-12明确更新：G2/G5 体验整改每批最多20轮；第20轮仍未通过时暂停并报告全部尝试、修改、验证、失败签名、根因及下一批最小范围，取得真实明确批准后新批从第1轮开始。历史批次证据保留，但不累计扣减新批20轮额度；未经批准不能以换阶段/任务/线程/代理重置计数。其余任务按既有规则；普通 UI 十分钟、同失败提前停止、禁止改断言/阈值强过等停止条件保留，继续批准不自动扩大时间预算或设计范围。冻结变更先 RFC、影响分析、真实批准，重开受影响阶段、更新文档、重新冻结和重测。不修改执行过的 Flyway，不接触 PROD，不覆盖持久 DEV 历史。

所有查询/表单标签与控件同一水平行，整组换行；菜单树 Checkbox/只查看/管理语义按已批准契约，不能仅加路由。设计冲突、越权/破坏性操作、依赖缺失按 AGENTS.md 停止条件报告。最后报告文件、迁移、真实测试和门禁证据、审查、债务和任务 readiness，不自动启动下一功能。
