# 成品入库126 T3详情的受限岗位可读名称与状态显示修复

状态：IMPLEMENTED / TARGETED CHECKS PASS / G5 PARTIAL，2026-11-12。任务来源：用户“继续”MES成品闭环，已批准DCP-PRODUCTION-FINISHED-ENTRY-001只读上下文展示；非新页面、不新增业务字段。

## 基线与证据
已从本机读取AGENTS.md、MES_DEV_RULES.md、PROJECT_BASELINE.md、MES_TASKS.md、DCP-PRODUCTION-FINISHED-ENTRY-001-APPROVED.md、FinishedDetailView.vue、ReadReference.vue、incomingModel.ts、IncomingReferencePicker.vue及CUAWright run_006。
唯一main；633条既有未提交项保留。T3页面结构、字段、按钮、状态机、API、角色/签名/审批均冻结。仓库账号11565真实GET入库126包含`finishedProduct.productSnapshot.productName`与`sourceSnapshot.productSnapshot.productName=喉咽茶合剂`。仓库无master:product:view、master:uom:view、menu:iam:users，访问`/units/3`及`/admin/users/11556`被403；不能为显示而扩大权限。CUAWright确认当前产品、单位、人员名称缺失，库位ACTIVE为英文。

## 修复范围白名单
1. `frontend/mes-web/src/views/finished/FinishedDetailView.vue`：仅在现有factName(field)中让productId优先使用单据已冻结的产品名称，兼容旧批次batch.productName。不请求额外接口、不自行猜单位/人员名称。
2. `frontend/mes-web/src/views/quality/incomingModel.ts`：沿用现有`enumLabels`转换，将已存在库位ACTIVE状态展示为“在用”，不修改业务状态值或Picker的提交value。
3. 定向前端单元测试与本机CUAWright只读回归证据；`MES_TASKS.md`记录结果。保持其它已存在未提交变更原样。

## 禁止项
不新增权限、API、字典、数据字段、DB迁移或操作按钮；不以mock冒充实际业务确认；不运行持久数据库写测试；不触发入库确认POST；不修改、重签eBR或QA结果；不修改其他模块/全局布局。

## 验收
针对产品名称和ACTIVE中文状态的单元断言、TypeScript检查，CUAWright仓库账号真实5173/8080在1440与375视口查看126及库位选择、确认签名弹窗（不提交）、无溢出、HTTP401按真实路径区分登录前预期和授权缺口。未经用户最终确认，不标ACCEPTED。

## TypeScript验证发现的构建依赖（最小无运行时修复）
- 定向单元测试`incomingModel.test.ts` 8/8通过；全量`vue-tsc -b`在`src/api/integration.ts:2`因`../types/audit`已被用户此前删除而TS2307，非本次UI逻辑引起。
- 已对照Git HEAD：被删除audit.ts中的`Page<T>`结构与已存在`src/api/http.ts`公开`Page<T>`字段完全等价（items、page、size、total）。为使本轮typecheck门禁可执行，仅允许在白名单中增加`frontend/mes-web/src/api/integration.ts`一项类型import改向已有`./http` Page，保留已删除audit.ts不恢复，所有网络与业务行为无改变。
- 这只是消除无关GxP Audit类型遗留引用；禁止新增audit功能、审计表、权限或测试替代。

## 2026-11-12 最终执行和验证
- 原T3字段`productId`通过现有`finishedProduct.productSnapshot.productName`／`sourceSnapshot.productSnapshot.productName`呈现“喉咽茶合剂”，不再要求仓库角色额外读取产品主数据；`locationId`的ACTIVE枚举映射中文“在用”，实际选择值仍原库位ID7。旧冻结字段、动作、签名不变。
- `incomingModel.test.ts`定向Vitest 8/8通过；`vue-tsc -b`通过；`vite build`成功，耗时49.05秒。打包存在大于500kB chunk警告，非编译错误。
- 已删除`src/types/audit.ts`造成`api/integration.ts`导入Page类型的旧依赖失效。仅重用已有`api/http.ts`完全等价的Page<T>定义，不恢复审计模块；全量前端类型检查和构建恢复PASS。
- CUAWright真实仓库账号11565登录正式5173/8080，run_007 exit0；PC1440与移动375均显示“喉咽茶合剂”，库位选项“成品待检区 · 在用”，签名弹窗打开且视口无横向溢出，所有业务写请求为0。结果`C:\Users\Administrator\.codex\tools\cuawright-mcp\evidence\mes-inbound-126-cuawright-20261112\final_runs\run_007\`（result.json、final_script_log.txt、11张截图）。
- 早前5条HTTP401经记录真实路径确定为`/api/v1/auth/me`4次及`/api/v1/auth/refresh`1次，发生在登录/页面重新初始化阶段；不属于产品或单位查询HTTP401。无权限的`/units/3`、`/admin/users/11556`由仓库角色直读会返回403，单位/人员显示缺名称仍是受控只读投影缺口，未擅自授予读权限。
- 产品名称及库位本轮属于小范围只读显示修复，不等于完整G5 UI/UX审查完成；textarea标签相对控件的几何中心偏差仍为PC20px/移动27px，完整审核需专门验收。
- 最新正式API独立重读：批655 PRODUCTION_COMPLETED/version5；成品入库126 SUBMITTED/version1/398000ml、confirmedBy=null，仓库11565 allowedActions CONFIRM，finishedInventory BLOCKED/finishedQuality QUARANTINE；QA结果、成品QC、FINAL归档未完成。无真实业务POST，不把前端CUAWright PASS称为仓库确认成功。
