---
name: mes-feature-workflow
description: Explicitly invoke with $mes-feature-workflow or 使用MES标准流程开发XX功能 / 按MES标准流程执行. Govern hospital-pharma-mes menus, features, pages, T1–T6 and substantial contract changes through five ordered stages and executable G1–G5 gates. Ordinary development requests do not enable it.
---

# MES 功能开发主控

仅用于 `D:\codex\_project\gmp\hospital-pharma-mes`。用户于本次安装中明确选择 **仅显式调用**；不自动匹配任务，CI 仅保存未启用示例，未修改 AGENTS.md 或任何既有工程规则。在现有 main 工作区操作；不建立第二 checkout，不自动启动业务开发。

显式自然语言别名：`使用 MES 标准流程开发 XX 功能`、`按 MES 标准流程执行`（大小写/空格不敏感）。这是用户明确启用该技能的语义约定，不是注册了不存在的平台别名 API；平台最明确的调用形式仍是 `$mes-feature-workflow`。`route-request` 只辅助识别，不自动运行开发。未带这些明确指令的普通任务不加载此流程。

每次启动先真实读取 `AGENTS.md`、`docs/development/MES_DEV_RULES.md`、`MES_TASKS.md` 对应任务和 `docs/PROJECT_BASELINE.md` 权威路由；检查 Git 未提交修改。定位一个功能编号和任务卡，读取其必要基线章节、确认记录和文件白名单。已 ACCEPTED 的任务不能重做。业务权威仍是 v1.0.23 + 已批准限定 DCP，视觉仍是 Global UI V2/T1–T6。

完整规范见 [workflow-v2-source.md](references/workflow-v2-source.md)（用户原文，十五章均须遵守）；工程落地入口为仓库 `docs/governance/MES_FEATURE_WORKFLOW.md`、`MES_GATE_POLICY.md`、`MES_CHANGE_CONTROL.md`。治理文件不替换业务基线或 MES_TASKS.md 的任务状态。

## 固定执行顺序

1. **UI UX Pro Max**：先读取已有 token、页面目录及 T1–T6；使用已安装 `ui-ux-pro-max`，不是不存在的 `ui-ux-pro-max-skill` 别名。设计稿、字段/按钮/状态、标注、PC/Mobile、功能映射和审查齐全后取得具体版本确认，执行 G1。
2. **Product Design**：读实际安装的 Product Design router 和所需子技能。只能交付 Vue 3/TypeScript/Vite/Ant Design Vue/Pinia/Vue Router；不支持时 BLOCKED，按 [tool-handoff.md](references/tool-handoff.md) 交接并取得构建方式批准。原型源码只在真实 `D:\codex\_project\gmp\mes-demo`，5174；功能证据记录路径、提交或经批准的哈希例外，不复制另一套源码。编译、启动、真实浏览器交互、控制台、同视口截图和具体源码版本的人工确认齐备才执行 G2。
3. **详细设计冻结**：对 PRD、字段、实体/ERD、字典、API/错误码、状态机、权限、幂等、审计/GxP、Migration、测试/RTM 做一致性核对；形成唯一冻结清单和 DESIGN LOCK，再执行 G3。
4. **Codex 工程集成**：只有 G3 与前序人工确认有效时，直接复用已验收原型，替换 Mock 适配层，按冻结设计实现真实 API、权限、状态/校验及适用签名审计。保存源码继承关系；禁止重新设计 UI，执行 G4。
5. **真实集成验收**：实际 5173/8080 HTTP、角色正反例、状态流转/关联数据、数据库验证、E2E、视觉/DOM、异常边界及必要回归；形成报告和最终人类确认，执行 G5。仅自动测试通过最多 READY FOR ACCEPTANCE；只有真实最终批准可在 MES_TASKS.md 标记 ACCEPTED。

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

遵守既有 READ/DESIGN LOCK/REUSE/WRITE/VISUAL/ACCEPTANCE/ANTI-LOOP gates。普通业务任务仍最多两轮自动修复、普通 UI 十分钟、同失败提前停止；此次用户允许反复修复仅适用于工作流自身配置验收。冻结变更先 RFC、影响分析、真实批准，重开受影响阶段、更新文档、重新冻结和重测。不修改执行过的 Flyway，不接触 PROD，不覆盖持久 DEV 历史。

所有查询/表单标签与控件同一水平行，整组换行；菜单树 Checkbox/只查看/管理语义按已批准契约，不能仅加路由。设计冲突、越权/破坏性操作、依赖缺失按 AGENTS.md 停止条件报告。最后报告文件、迁移、真实测试和门禁证据、审查、债务和任务 readiness，不自动启动下一功能。
