# 生产工艺动态表单 CPP 原型设计锁 — 2026-10-13

状态：**APPROVED / FROZEN / FORMAL IMPLEMENTATION AUTHORIZED**

用户于 2026-10-13 19:13（UTC+8）在主会话明确回复“批准”。批准时原型源码未发生变化，继续锁定 SHA-256 `AC294FA83DBFCF0A35A2CC8BC00C310332E8878D88F04ECE8A12398E95B5C287`，允许按本记录白名单增量集成到正式 MES；不得以此批准扩大到无关页面、第二套表单设计器或第二套规则引擎。

## 1. 批准来源与范围
用户已批准：基于现有正式动态表单设计器增量修改，不推倒重做。动态表单设计器保留现有 Canvas Editor、字段库、字段属性、表单属性、签名/复核能力，并新增第二个顶层页签“CPP规则设计”。

CPP规则设计固定约束：
- 与当前动态表单关联，只引用当前表单已有字段；
- 不提供适用产品选择，产品适用关系由生产工艺维护；
- 不允许在 CPP 页签新增、删除或改字段定义；
- 支持 AND / OR、比较运算、规则预览和当前批准的四类超限动作：标记异常、禁止提交、生成偏差记录、要求 QA 复核；
- 设计页不展示“运行校验效果”，运行期判定留在 eBR/生产执行；
- 规则表达式必须由可视化配置生成安全 DSL，禁止 JavaScript / SQL / SpEL。

## 2. 原型文件与访问地址
- Vue 原型：`D:\codex\_project\gmp\mes-demo\src\features\ebr\DynamicFormCppPrototype.vue`
- 路由：`http://127.0.0.1:5174/#/ebr/dynamic-form-cpp`
- SHA-256：`AC294FA83DBFCF0A35A2CC8BC00C310332E8878D88F04ECE8A12398E95B5C287`
- 构建：`npm run build` PASS（vue-tsc + Vite）

## 3. 页面结构
### 页签一：表单设计
沿用现有动态表单职责：字段库、Canvas、字段属性。原型只用于证明 CPP 页签与现有表单设计并列，不替换正式 `DefinitionDesigner.vue` 已有交互。

### 页签二：CPP规则设计
三栏结构：
1. 左栏：本表单 CPP 规则列表；
2. 中栏：规则名称/描述、可视化条件、业务预览、系统表达式预览、超限动作；
3. 右栏：当前表单/工序/工艺上下文和当前表单可引用数值字段。

示例规则：
`灭菌温度 >= 121℃ AND 灭菌温度 <= 125℃ AND 灭菌时间 >= 20min`

业务预览：
`121℃ ≤ 灭菌温度 ≤ 125℃，且灭菌时间 ≥ 20min`

## 4. 正式实施复用结论
正式实现优先复用：
- 前端：`frontend/mes-web/src/views/ebr/DefinitionDesigner.vue`、`definition.rules`；
- 后端：`EbrCommands.Rule`、`EbrDefinitionRules`、`EbrDsl`、`EbrEngine`；
- 数据库：现有 `ebr_rule_def`；
- 运行期：现有 ON_CHANGE / ON_SAVE / ON_SUBMIT / ON_OPERATION_COMPLETE 规则执行与冻结证据。

不得直接新增第二套 CPP 规则引擎或重复产品适用关系。是否需要新增物理列/表，只能在正式实施前完成精确缺口分析后决定，并继续遵守 append-only Flyway。

## 5. 正式实施白名单（已执行）
实际实施范围：
- `frontend/mes-web/src/views/ebr/DefinitionDesigner.vue`
- `frontend/mes-web/src/views/ebr/CppRuleDesigner.vue`
- `frontend/mes-web/src/views/ebr/types.ts`
- 直接相关 eBR 后端 Rule/DSL/Definition/Runtime 文件
- `backend/mes-boot/src/main/resources/db/migration/V064__ebr_cpp_visual_rule_config.sql`
- CPP 规则单元测试及 `EbrIT#cppVisualConfigPersistsInExistingRuleRowAndRoundTrips` 事务回滚集成测试
- 本任务批准/设计锁/验收记录

禁止修改无关业务、现有产品适用关系、历史 published canonical/hash、旧 Flyway、角色权限体系、既有签名/审计证据。

## 6. 当前门禁
精确原型源码版本/哈希已获得用户明确批准，正式增量集成及 REUSE / WRITE / VISUAL / ACCEPTANCE GATE 已完成。V064/64 Flyway、前端 build+186/186、`mes-ebr` 61/61、mes-boot package、当前源码 8080 health、正式5173/8080 G5 20/20 coverage 与 manifest validator 均 PASS，状态为 `READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING`。历史 published canonical/hash、旧 Flyway、产品适用关系及既有签名/审计证据保持不变；用户最终人验前不得写 `ACCEPTED`。实现证据见 `../../review/PROCESS_DYNAMIC_FORM_CPP_IMPLEMENTATION_20261013.md`。
