# 生产工艺动态表单 CPP 设计跨文档一致性检查 — 2026-10-13

结论：`PASS FOR DESIGN DOCUMENT SYNCHRONIZATION / IMPLEMENTATION NOT RUN`

## 1. 检查范围

- `docs/development/DCP-PROCESS-DYNAMIC-FORM-CPP-001-APPROVED.md`
- `docs/development/PROCESS_DYNAMIC_FORM_CPP_REQUIREMENTS.md`
- `docs/architecture/PROCESS_DYNAMIC_FORM_CPP_DETAILED_DESIGN.md`
- `docs/PROJECT_BASELINE.md`
- `MES_TASKS.md`

只读对照：FINAL BASELINE COMPLETE v1.0.23 的 PRD、eBR 动态表单详细设计、UI 详细设计，以及现有 eBR 字段、规则和安全 DSL 实现。冻结发布文件未修改。

## 2. 一致性矩阵

| 检查项 | 需求 | 详细设计 | 基线 | 结论 |
|---|---|---|---|---|
| 设计器双页签 | 表单设计、CPP规则设计 | 同一工序/表单上下文 | 已记录 | PASS |
| 字段归属 | 仅当前动态表单字段 | `form_def_id` + 字段归属校验 | 已记录 | PASS |
| 产品关系 | CPP不绑定产品 | 禁止 `product_id`，沿用生产工艺链路 | 已记录 | PASS |
| 条件能力 | AND/OR/条件组 | 条件树 → 安全 DSL | 已记录 | PASS |
| 规则预览 | 业务文本 + 系统表达式 | 同一条件树确定性生成 | 已记录 | PASS |
| 超限动作 | 最终批准四类动作 | 复用现有规则证据/提交门禁/偏差/复核机制 | 已记录 | PASS |
| 运行校验效果 | 从设计页删除 | 运行判定留在生产/eBR阶段 | 已记录 | PASS |
| 历史保护 | 不改历史冻结证据 | 新版本/快照边界 | 已记录 | PASS |
| 表达式安全 | 禁止任意代码 | 复用白名单 DSL/parser | 已记录 | PASS |

## 3. 与现有基线的关系

- 继承现有动态表单 `Form/Field/Rule`、白名单 DSL、发布冻结和运行执行原则；
- 新增的是 CPP 专用设计语义、条件树和超限动作配置，不建立第二套产品适用关系；
- 不改变现有产品、生产工艺、工序状态机、权限、签名或审计体系；
- `ebr_cpp_rule` / `condition` / `action` 仅保留为概念模型；实施复用评审已锁定为复用现有 `ebr_rule_def` 与安全 DSL，并通过追加 V064 增加 nullable `cpp_config_json` 保存可视化配置，不新增第二套 CPP 物理表；
- 未修改 FINAL BASELINE COMPLETE v1.0.23 冻结目录及其 manifest/hash。

## 4. 实施状态更新

2026-10-13 用户批准精确 Vue 原型源码后已完成正式增量实施：正式 `DefinitionDesigner` 已增加“表单设计 / CPP规则设计”双页签；后端复用 `EbrCommands.Rule`、`EbrDefinitionRules`、`EbrDsl`、`EbrEngine` 和运行证据机制；V064 已在本机 DEV 成功应用，Flyway 64 个迁移 validate PASS；`mes-ebr` 61/61 PASS；CPP可视化配置真实DB事务回滚往返 IT 1/1 PASS；前端 44/44 test files、186/186 tests PASS；mes-boot package PASS；当前源码 8080 health=200。

完整 G5 `mes-ui-ux-audit` 已完成：正式5173/8080五状态×四视口=20/20 screenshot coverage，交互重放无 console/page/API failure，无页面级横向溢出/条件控件重叠，manifest validator PASS，verdict=`READY FOR ACCEPTANCE`。当前结论为 `READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING`；用户最终人验前仍不得写 `ACCEPTED`。详见 `PROCESS_DYNAMIC_FORM_CPP_IMPLEMENTATION_20261013.md`。
