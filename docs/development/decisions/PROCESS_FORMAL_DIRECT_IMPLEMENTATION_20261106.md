# 生产工艺正式代码直接实施授权（2026-11-06）

状态：**USER DIRECTED / FORMAL IMPLEMENTATION AUTHORIZED / G5 PENDING**

## 1. 用户指令与门禁适用

2026-11-06 用户明确要求：“继续开发到完成，对着正式代码修改”。

本记录将该指令固化为生产工艺模块的**任务级直接实施授权**：本任务不再以 mes-demo 当前精确哈希的 G2 人工签核作为正式代码修改前置条件，直接在现有正式 MES Vue/Java/MariaDB 代码上完成已确定功能。

这项授权：
- 不表示此前任何原型哈希已获批准；
- 不将 G2 未完成状态改写为 PASS；
- 不改变其它 MES 功能仍适用的 §2A 原型门禁；
- 不替代真实 5173/8080、数据库、浏览器 G5 验收和用户最终接受。

正式实现以当前正式页面与既有批准业务合同为基线，保持 UI V2/T1–T6，不重画全局视觉体系。

## 2. 本次正式交付范围

1. 工艺基本资料：工艺编码、工艺名称、当前版本标签、生效批准日期、工艺说明。版本标签和日期仅为当前工艺资料字段，不建立工艺历史版本、版本比较、提交/审批/批准记录或工艺版本生命周期。
2. 适用产品：一套生产工艺可关联多个产品。产品通过工艺间接使用工序、EBR表单及CPP定义。正式持久化并支持列表筛选、详情回显。
3. 工序：统一普通工序操作模型；新增、编辑、查看、复制、排序、前置关系、角色、设备、清场及现有完成条件保持原合同。
4. 工序 EBR 表单：每道工序关联一至多份现有 EBR 表单；复用正式模板查询、表单查看/编辑/设计路由和身份，不重复建表单模型。
5. CPP：复用现有 DefinitionDesigner/CppRuleDesigner、ebr_rule_def.cpp_config_json、安全 DSL、现有四种批准动作和运行证据链。CPP 只引用当前表单字段，不直接关联产品。

## 3. 兼容与数据边界

- 新生产工艺页面不显示 IPC/过程检验特殊类型、特殊标签、IPC 专属参数表或独立检验项目入口；检验步骤作为普通工序。
- 历史 operationType、ipcDefinitions、数据库列、API属性、旧批次快照和运行证据保留。正式页面读取和保存时必须原样保留旧兼容字段；新建工序不主动写入 IPC 专属字段。不得以新 UI 隐藏为由将旧数据写空。
- 新增 `proc_package_product` 关系表，Flyway 追加 V076；历史 `proc_package.product_id` 数据回填为适用关系，原列保留供旧消费者兼容。新 API 以 `productIds` / `applicableProducts` 为权威关系；单产品旧字段只作为兼容投影，不能用于判断完整适用范围。
- 生产处方维护/启用与生产批次下达必须验证目标产品属于工艺适用范围；无显式产品关联的通用工艺沿用既有通用规则。新快照应冻结当时适用产品范围。历史快照、已发布 EBR、签名、审计和生产事实不回写。
- 不新增权限代码、顶层菜单、独立 CPP 引擎或质量单据。

## 4. 实施白名单

仅允许修改：
- 本记录及 MES_TASKS.md；
- `backend/mes-boot/src/main/resources/db/migration/V076__process_multi_product_and_current_metadata.sql`；
- `backend/mes-process` 中 ProcessService、ProcessCommands、ProcessStore、PackageEntity、必要的关联实体/Mapper、PrescriptionService、ProcessQueryService及其直接测试；
- `backend/mes-production` 中直接的生产批次适用性校验与对应测试；
- `frontend/mes-web/src/views/process` 中 ProcessDetailView、ProcessListView、model、工序子项弹窗及对应测试；
- `docs/contracts/BASIC_CURRENT_DEFINITION_OPENAPI.json` 中对应的生产工艺请求/响应契约；
- 为实现现有 EBR/CPP 入口而确有必要的直接适配文件。

禁止修改：冻结 release 文件、历史 Flyway、历史生产快照、签名/审计记录、无关 MES 模块、全局导航/权限、其它用户未提交文件；禁止创建 worktree 或第二工作区。

## 5. 验收

- Flyway 只追加，升级前核对实际最大版本；旧单产品关系回填正确。
- 同一工艺可保存多个产品，重新读取与产品筛选结果一致；跨组织/不存在/停用产品拒绝；清空关系按通用工艺语义处理。
- 工艺名称/版本/生效批准日期/说明与工序在一个当前定义事务中保存；旧 route-only 请求不清空未提交元数据。
- 新增/编辑/查看普通工序不展示 IPC 专属UI；旧 IPC 字段在保存前后保持一致。
- 表单列表/设计器/CPP 走真实 EBR API，字段/CPP 规则保存重读；不以原型内存或 mock 作为通过证据。
- 处方创建/编辑/启用、生产下达均验证产品适用范围；历史批次快照字节不变。
- 后端定向测试、前端 typecheck/build/Vitest、真实 8080/5173 HTTP、真实 DB 保存重读及 Chromium 关键流程留证据。
- G2 仍如实记录为未完成；本次直接实施授权不等于 G5 READY/ACCEPTED。

## 6. 工作区保护

当前 main 为唯一工作区。开始时 HEAD 为 `1682fc5`、本地领先 origin/main 1 个提交；工作区存在大量未提交修改。本次只对本白名单做精确补丁，不清理、不覆盖、不 stage/commit/push 任何内容。


## 2026-11-06 正式代码实现与实测补充

正式实施已落地：V076 已在 `hospital_pharma_mes_dev` 成功迁移、76 条 Flyway 已验证；多产品关系的创建、保存、查询与历史单产品兼容均已通过事务集成测试；生产处方和批次下达适用性检查、当前工艺资料（名称/版本/生效批准日期/说明）、工序与 EBR 表单/CPP 入口已进入正式 Java/Vue 代码。

最终定向验收：Maven 20 模块 BUILD SUCCESS；前端 TypeScript + Vite 构建通过；全量前端 Vitest 249/249 PASS（111 suites）；定向 Java 24/24 PASS（ProcessOperationType 7、Prescription 3、CPP 1、Ebr 13）；真实 Chrome 生产工艺列表/详情/适用产品及工序表单操作 1 PASS。

**G5 非全通过**：Chrome CPP 表单直达用例 1 SKIP，因为本机 9 条工艺均未找到已绑定的现有 EBR 表单。不制造、篡改用户业务表单数据；由真实 MariaDB 事务集成 CppControlledRoundTripIT 证明 CPP 写入/回读。完整证据：`docs/development/process-form-cpp-ebr/20261106-formal-code/RESULT.md`。

未创建 worktree；单一 main；未 stage/commit/push；之前大量未提交源码保持原样。此轮任务级直接开发授权不等于原型 G2 已批准或 G5 产品已最终接受。
