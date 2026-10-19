# 生产工艺动态表单 CPP 规则正式实现收尾 — 2026-10-13

状态：**READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING**

## 1. 本轮范围

按用户批准的精确原型，在既有动态表单设计器上增量实现，不推倒重做：

- 动态表单设计器顶层保留“表单设计”，新增“CPP规则设计”页签；
- CPP 固定绑定当前动态表单，只引用当前表单已有字段；
- 不配置适用产品，产品适用关系继续由生产工艺维护；
- 支持 AND / OR、条件组、业务可读预览和安全 DSL 预览；
- 本次最终批准四类超限动作：标记异常、禁止提交、生成偏差记录、要求 QA 复核；
- 设计页不提供“运行校验效果”；
- 不新增第二套表单设计器、规则引擎、产品绑定或 CPP 三张物理表。

批准原型：`mes-demo/src/features/ebr/DynamicFormCppPrototype.vue`，SHA-256 `AC294FA83DBFCF0A35A2CC8BC00C310332E8878D88F04ECE8A12398E95B5C287`。

## 2. 正式实现

前端：
- `frontend/mes-web/src/views/ebr/DefinitionDesigner.vue`
- `frontend/mes-web/src/views/ebr/CppRuleDesigner.vue`
- `frontend/mes-web/src/views/ebr/types.ts`

后端复用：
- `EbrCommands.Rule`
- `EbrDefinitionRules`
- `EbrDsl`
- `EbrEngine`
- `EbrRuntimeTransactions`
- `RuleEntity`

数据库：
- 复用 `ebr_rule_def`；
- 新增 append-only `V064__ebr_cpp_visual_rule_config.sql`；
- 仅增加 nullable `cpp_config_json LONGTEXT` 与 JSON_VALID 约束；
- 既有 `expression` 继续是经服务端校验的执行表达式；
- 历史规则 `cpp_config_json=NULL`，旧发布定义/canonical/hash 不改写。

服务端对 CPP 配置执行封闭结构、当前表单字段归属、运算符/值类型、动作白名单、条件规模、安全 DSL 重建及 `CPP_EXPRESSION_MISMATCH` 等 fail-closed 校验，不能用前端伪造 expression 绕过可视化条件树。

## 3. 运行期

- CPP 在现有规则运行框架内执行；
- ON_CHANGE / ON_SAVE 可产生警告/异常状态；
- 仅配置“禁止提交”且 ON_SUBMIT 判定失败时形成 BLOCK；
- “生成偏差记录”复用现有受控偏差 producer；
- “要求 QA 复核”要求当前表单已有复核规则，不复制 QA 权限模型；
- 运行结果继续进入既有规则执行证据、表单状态和审计链。

## 4. 自动验证

- Frontend production build：PASS。
- Frontend Vitest：44/44 test files，186/186 tests PASS。
- Backend `mes-ebr -am test`：`mes-ebr` 61/61 PASS；依赖模块同一 reactor 全部 SUCCESS。
- Native DEV 事务回滚集成验证 `EbrIT#cppVisualConfigPersistsInExistingRuleRowAndRoundTrips`：1/1 PASS；证明 `cppConfigJson` 经正式 Service → `ebr_rule_def.cpp_config_json` → 查询 DTO 完整往返，测试数据全部回滚。
- Backend `mes-boot -am -DskipTests package`：PASS。
- DEV Flyway：64 migrations validate PASS；当前 schema = V064。
- 数据库只读确认：V064 `ebr cpp visual rule config` success=1。
- 正式 8080 重启到当前源码打包产物后：`/actuator/health` HTTP 200。
- `git diff --check`（本任务代码/迁移白名单）：无 whitespace error；仅仓库既有 CRLF→LF 提示。

## 5. G5 UI/UX

最终人验前按批准设计稿再次执行视觉对齐：三栏改为更接近设计稿的字段区 / CPP规则配置 / 本表单规则结构；主配置区扩大，字段卡片和步骤卡片统一边框、间距和层级；右栏把“规则说明”压缩为一条“配置提示”，突出本表单规则清单和当前表单上下文；不改变已批准业务语义。2026-10-14 又按用户要求优化“表单设计”字段插入：字段库每个可插入字段前显示独立 `+` 图标，用户在 Canvas 定位后点击对应 `+` 即按保存的画布光标/选区插入；字段名称本身不承担插入动作。

执行者：**Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max**。

- 视觉检查：表单设计、CPP空态、校验错误、已配置、只读五类状态；
- 浏览器交互：新增规则、空值校验、字段搜索、AND/OR条件、条件组增删、业务预览、安全DSL、只读态；
- 自动几何：1440 / 1280 / 900 / 375 四视口，无页面级横向溢出，无条件行控件重叠，无嵌套交互控件；
- 正式 5173/8080 最终重放：consoleErrors=[]，failedResponses=[]；
- 明确验证：无适用产品选择、无“运行校验效果”、新规则不预填示例工艺值、只读版本不可新增；
- G5 manifest validator：**PASS**，20 screenshots / coverage=20，verdict = **READY FOR ACCEPTANCE**。

证据：
- `artifacts/cpp-ui-20261013/g5/audit-manifest.json`
- `artifacts/cpp-ui-20261013/g5/interaction.json`
- `artifacts/cpp-ui-20261013/g5/geometry.json`

当前正式 UI 源码组合 SHA-256：
`0D5B011F24A6F81B0FBC119CA688E20F79F8CE692760872F3A7A04E9234D61EB`。

## 6. 结论

正式实现、数据库迁移、规则安全、构建、回归、真实 5173/8080 与 G5 UI/UX 已达到 **READY FOR ACCEPTANCE**。

本记录不替代产品所有者最终人验；在用户明确验收前，状态不得写成 `ACCEPTED`。
