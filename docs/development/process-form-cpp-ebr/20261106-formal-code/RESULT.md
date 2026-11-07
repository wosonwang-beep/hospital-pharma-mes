# 生产工艺正式代码开发与回归验收记录（2026-11-06）

**状态：FORMAL CODE IMPLEMENTED / TARGETED REGRESSION PASS / G5 PARTIAL（CPP 真实浏览器场景未覆盖）**

## 范围与授权
- 依用户“继续开发到完成，对着正式代码修改”直接修改本机唯一 main 正式 MES 代码；未转换为 mes-demo 原型或新增 worktree。
- 授权决策：`docs/development/decisions/PROCESS_FORMAL_DIRECT_IMPLEMENTATION_20261106.md`。
- 该指令是任务级正式实施授权，**不是 G2 原型精确哈希批准**；G2 仍未完成。不得将下述验证称为 G5 全量通过。
- 仓库：`D:\codex\_project\gmp\hospital-pharma-mes`；本轮开始 HEAD `1682fc5`，最终仍为该 HEAD，本地 main ahead origin/main 1，未新增工作区、未 stage/commit/push、未清理既有未提交改动。

## 正式功能实现
1. `proc_package` 当前工艺资料具备名称、版本标签、生效批准日期和说明；无工艺历史版本/审批记录。
2. Flyway 仅追加 `V076__process_multi_product_and_current_metadata.sql`；`proc_package_product` 持久化工艺-适用产品多关联，回填旧单产品；旧 `product_id` 仅兼容投影。无变化关联保存保留原关联身份。
3. 正式 GET/POST/PUT 工艺 API 回显 `productIds`、`applicableProducts`、版本/日期；产品筛选支持多关联；非法/跨组织/停用产品受校验。
4. 生产处方创建/修改/启用及生产批次下达验证选用工艺是否适用于产品；新批次快照冻结当前 `applicableProductIds` 与工艺资料；不回写历史快照。
5. 正式 Vue 工艺页面：普通工序，清单上移/下移/复制、前置依赖、完成规则与参数弹窗；工序表单为默认焦点，表单数量、真实模板来源、表单编辑及 CPP 规则设计的直接入口；适用产品专页签和多选弹窗；保留历史 `operationType`、`ipcDefinitions` 兼容，但隐藏旧 IPC 专属控件，不主动创建新 IPC 记录。
6. EBR 设计器 `initialTab='cpp'` 支持从工序表单直接打开现有 CPP 可视化设计器；规则仍保存至 `ebr_rule_def.cpp_config_json`，沿用安全 DSL 与既有批准动作，没有复制规则引擎。
7. EBR schema2 表单形状校验错误统一按既有 `LINT_FAILED` 输出 422 和字段路径。无改动签名、审计和生产记录。

## 验证结果
| 验证项 | 结果 |
|---|---|
| Java 21 Maven 20 模块 `-DskipTests package` | BUILD SUCCESS |
| Vue3/TS `npm run build`（vue-tsc + Vite） | PASS |
| 全前端 Vitest | **249/249 PASS，111 suites** |
| 后端集成：ProcessOperationTypePersistenceIT | **7/7 PASS** |
| 后端集成：ProductionPrescriptionIT | **3/3 PASS** |
| 后端集成：CppControlledRoundTripIT | **1/1 PASS** |
| 后端集成：EbrIT | **13/13 PASS** |
| 定向后端合计 | **24/24 PASS** |
| Flyway | **76 migrations validated；V076 在 `hospital_pharma_mes_dev` 已应用，重启为最新** |
| 本机后端 `http://127.0.0.1:8080/actuator/health` | HTTP 200, `{"status":"UP"}` |
| 本机前端 `http://127.0.0.1:5173/process/packages` | HTTP 200 |
| Playwright 调用本机真实 Chrome | **1 PASS / 1 SKIP / 0 FAIL**，本机列表 9 条，生产工艺列表/详情、适用产品、工序表单交互成功 |
| `git diff --check` 定向白名单 | EXIT 0 |

验证使用真实本机后端、MariaDB 与 Chrome；前端单元测试为 jsdom，后端集成测试回滚事务，不以伪造的 API 应答代替持久化验收。

## 未闭环与注意事项
- **G5 仍为 PARTIAL，未宣布软件最终 ACCEPTED。** 本机 9 条工艺均未发现可供该浏览器用例操作的已绑定 EBR 表单；真实 Chrome CPP 直达场景 **SKIP**，不能计 PASS。真实 EBR/CPP API 数据库存储、读取、规则/版本校验已有 `CppControlledRoundTripIT` 通过。
- 本轮未改动现存已发布或历史生产数据，未为浏览器覆盖而任意绑定用户业务工艺/制造测试数据。
- Flyway 对 MariaDB 13.0 报告支持版本推荐及一次重复索引警告；迁移成功、服务健康。
- Git 单一工作区存在大量先前未提交改动；不能用本轮测试状态推断所有其它模块已验收。未 stage/commit/push，原有改动保持原状。

## 复验命令
```powershell
cd D:\codex\_project\gmp\hospital-pharma-mes
mvn -pl backend\mes-boot -am "-Dtest=ProcessOperationTypePersistenceIT,ProductionPrescriptionIT,CppControlledRoundTripIT,EbrIT" "-Dsurefire.failIfNoSpecifiedTests=false" test
cd frontend\mes-web
npm.cmd run build
npx.cmd vitest run
npx.cmd playwright test e2e/process-formal-live.spec.ts --project=chromium-desktop --reporter=line
```


## 2026-11-06 20:13 CST：正式 Chrome / MariaDB 真实 CPP 闭环补验

**本段覆盖并关闭上文“无已绑定 EBR 表单，Chrome CPP 直达 SKIP”的特定缺口**，但不替代完整 G5 UI/UX 清单及用户最终接受。

在 `hospital_pharma_mes_dev` 本机开发库使用正式 5173 Vue / 8080 Spring Boot / MariaDB 建立有真实业务语义的隔离交付核验记录。记录的业务名称、编码、工序、字段、规则均不包含“测试/演示/模拟/demo/test”字样；身份和创建目的只在本报告元数据标注。**这些记录保留在开发库，并非生产事实或获 GMP 批准的数据；未删除或修改既有工艺/EBR 业务记录。**

- 生产工艺 `829`，编码 `KCL30-CPP-26110601`，名称“氯化钾溶液配制关键参数控制工艺”。通过正式新增 T2 创建、工序 `OP1`“溶液配制”保存并在正式详情页回读；创建 HTTP 200。
- EBR 草稿模板 `663`，编码 `KCL30-OP1-EBR-261106`，名称“氯化钾溶液配制过程记录”。从该工序“新建工序表单”进入，Canvas 中插入 `NUMBER` 字段 `MIX_TEMP`“配制温度”，实际受控 PUT HTTP 200，刷新后 `OP1_RECORD` 关联、1 字段回读。
- 从正式工艺页面实际表单行点击“定义 CPP 规则”，准确打开模板 `663`、表单 `OP1_RECORD`、CPP 页面，自动列出 `MIX_TEMP` 字段。
- 在 CPP 页面新增并保存规则 `CPP_OP1_RECORD_1`“配制温度控制范围”：`20 ≤ 配制温度 ≤ 25`，动作 `MARK_EXCEPTION`（标记异常）及 `BLOCK_SUBMIT`（禁止提交）。先“保存规则配置”，再“保存草稿”，正式 PUT HTTP 200。
- **强制重载真实页面和 HTTP GET 回读**，逐项核验：规则归属 `OP1_RECORD`，字段引用 `MIX_TEMP`，条件 `[[">=","20"],["<=","25"]]`，`cppConfigJson` 规则名/动作和服务端表达式保持一致，前端预览仍显示 `20 ≤ 配制温度 ≤ 25`。浏览器响应 200，未使用 Mock。
- 完成脚本 `frontend/mes-web/e2e/process-formal-live.spec.ts` 已从固定 ID 导航改为按业务编码真实列表查询，动态读取模板 ID；已有规则时只执行完整 GET 与重读断言，避免重复创建业务规则。最新真实 Chrome E2E：**2 PASS / 0 FAIL / 0 SKIP（6.9s）**，从原 1 PASS / 1 SKIP 升级为实际全部运行。
- 当前源哈希：ProcessDetailView.vue SHA256 `868A12AB72DA2450FAAA92220B21C820D150D0D3BFBB6C41DFC1E44840D2D456`；DefinitionDesigner.vue `4E8F85674467020EB055550A60E65CC0C7ADE728DF0FA39C6BB66A9AB2A3245B`；CppRuleDesigner.vue `9C64523B14FB3FB24E2E4B98F04560F7637768F7533E1867D3A8549FF196EA75`；浏览器脚本 `A0AF26158B34DA8DFE727719C6BD878092A8DEEAB2E081BB544488E9B83FE125`。定向 `git diff --check` EXIT 0，main HEAD `1682fc5`，本轮未另建工作区或擅自提交/推送。

**准确状态：正式代码及功能链路已经实测跑通，CPP 浏览器缺口已关闭；整体 G5 仍为 PARTIAL / NOT ACCEPTED**，因为全视口逐图视觉审查/有效 G5 UI-UX manifest、其它场景覆盖矩阵及用户最终接受尚未形成。不得用这 2 个专项 Chrome PASS 代替系统全量验收。


## 2026-11-06 21:32 CST — 正式 G5 只读全页采集与产品选择弹窗整改

执行人：ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。本轮实际重新读取本机AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、原批准决策、PC_ONLY与Global UI V2/T1–T6及两套UI知识技能。本地唯一main、HEAD 1682fc5；不清理既有562条左右未提交工作区，不新建worktree，不提交或推送。

**完成的正式交付修正**：在 `frontend/mes-web/src/views/process/ProcessDetailView.vue` 中，适用产品选择弹窗不再沿用 Ant 默认「取消→确认」，显式 footer 改为「确认→取消」，并在picker内部增加84px受控下拉空间，修复900宽度展开选项时压住底部按钮导致鼠标无法点取消的问题。修复前后原始截图为 `g5-readonly/900-product-picker-before.png`、`g5-readonly/900-product-picker-after.png`；后图确认下拉展开时确认和取消均完整可见。

真实Chrome验证：多选弹窗加载后台 `/api/v1/products` 返回ACTIVE实产品 `KCL30`、`HYTEA100`，仅选用业务相符的氯化钾溶液 `KCL30`（id 1）关联独立DEV工艺829，不把喉咽茶合剂错误关联。经过取消保留原关系、确认单产品、父工艺 PUT 200、重新进入详情，服务端 `productIds=['1']`、`applicableProducts[0].productCode=KCL30`，UI产品卡读回一致。浏览器脚本调整为重复运行仍可操作，不重复新增工艺/模板/规则。本轮 **Playwright正式Chrome 3/3 PASS、0 SKIP、0 FAIL（14s）**。全前端 Vitest **249/249 PASS，111 files**；vue-tsc+Vite构建 PASS，`git diff --check` EXIT0。后端此次零源码改动，继承上轮Java定向24/24 PASS及Flyway V076成功证据。

G5只读实审：在5173/8080/MariaDB真实环境对列表、筛选、创建/必填按钮禁用、详情表单/产品、编辑表单/产品、EBR CPP和Canvas，按1440/1280/900共10状态×3宽采**30张原始全页截图**与DOM几何，资料 `g5-readonly/browser-capture.json`，全部documentWidth与viewport一致、无全页横向溢出；业务API非401错误和JS pageerror均0。按身份启动的3套Chrome浏览器上下文共记录24次 `auth/me` / `auth/refresh` 401挑战，已单列原始证据 `authChallenges`，没有假装响应不存在。正文多行textarea标签的中心线相差19px，需要区分有意顶部对齐和真实错误，不可只凭数字推定PASS。

8组ui-ux-pro-max检索真实运行 exit0，知识证据在 `g5-readonly/knowledge/`。已生成 `g5-readonly/audit-manifest.json`，绑定当前正式 ProcessDetailView.vue SHA256 `DF080891D4289BB939B5C500F6968F8186D00A3B9DF39E10C40F278F7B2C263C`、DefinitionDesigner与CppRuleDesigner相关源码hash；登记30张截图、6个业务表面、54条九维条目、19项WCAG，未完成逐张全维及对抗第二遍全部标记 NOT RUN；**validator真实退出1 FAIL**，绝不写G5 PASS/READY。

独立阻断：项目现行校验器将PC_ONLY批准例外仅限定原型 `#/processes`，未包括正式 `/process/packages`；尽管本任务已明确批准PC_ONLY，正式manifest仍被拒绝并要求375手机覆盖。此校验器口径不应被执行者私改或伪造路由绕过，需以真实授权修订或补齐适用范围记录。其余阻断是30图逐图八类二次、正式全权限/409/403及缩放完整审查尚未完成；不改变这些验收断言及NOT RUN状态。历史G2哈希仍未获人类签核；正式特定任务直接实施授权不等于G2 PASS。G5总体状态 **IN PROGRESS / NOT READY / NOT ACCEPTED**。

当前原计划任务开窗21:05至21:35（普通UI30分钟预算）；本轮21:32收尾，停止继续新增源码与测试。用户已有业务交付链路可运行，但G5法定门禁尚缺证据和校验器适用确认。所有报告记录在仓库原目录。


## 2026-11-06 21:39–22:04 CST — 正式 G5 403/409 + 表单遮挡 + PC等效缩放专项整改

执行：**ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max**。重新实际读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、Global UI V2/T1-T6、PC_ONLY确认、直接实施授权、正式代码及两套审查技能。仅当前唯一 main，当前 HEAD `1682fc5`，开始工作区564状态，最终569项（包含本轮新增脚本/证据）。未新建worktree、未stage/commit/push、未清理历史或修改其它模块。

### 修复与真实验证

1. **误导性409恢复提示（正式代码）**：`ProcessDetailView.vue` 原 `failure()` 对所有 HTTP409 均置 `conflict=true`，导致工艺编码重复新增时，除了“工艺编码已存在”还显示“记录已发生变化、重新加载”的假冲突。严格按错误码 `DUPLICATE_CODE` 排除此分支，保留真实冲突恢复和创建字段原值。现有重复业务编码 `KCL30-CPP-26110601` 的真实后端 POST 得409，Chrome断言业务提示出现、假冲突提示不存在、编码/工艺名称及当前新增页不丢失；0新增成功记录。
2. **真实角色权限负例（无Mock）**：使用既存仓管角色 `demo.warehouse` 的实际登录获得正式JWT，后端 `/auth/me` 中无 `process:package:view`；带该角色token直接 GET `/process-packages/829` 返回 HTTP403，Chrome验证禁止直接进入该受保护详情。测试未修改账户、角色、权限或业务记录。
3. **正式工序表单底栏遮挡**：`ProcessDetailView.vue` 原 `workspace-footer position:sticky;bottom:0` 在详情/编辑首屏把页内操作条覆盖至 `工序表单` 表格之上，隐藏当前表单行内容与按钮。按批准T2“稳定操作区”方案改为 **position:static**，动作顺序保持保存→取消，查看保持返回列表/停用。前后正式截图已分目录留存：`g5-readonly/1440|1280|900-detail-forms.png` 和 `g5-footer-after/1440|1280|900-detail-forms.png`，编辑同理。实际Chrome测量：1440 footerTop 1040/cardBottom1003；1280 1109/1072；900 1304/1267，均无覆盖，整页scrollWidth分别与视口一致。
4. **PC 200%等效缩放**：新增只读Chrome专项 `frontend/mes-web/e2e/process-pc-zoom-reflow.spec.ts`，模拟1440/1280/900物理宽度在200%布局下对应720/640/450 CSS像素、deviceScaleFactor2。每个宽度实查工艺编辑/查看及CPP页，共9个页面状态，全部无 document横溢出，工序底栏位于表单之后；**1/1专项PASS**。此测试是 **200%等效布局** 而非浏览器原生缩放/字体最小值/屏幕阅读器合规证据，不得扩大解释。

### 产物与运行结果
- 正式源码 `frontend/mes-web/src/views/process/ProcessDetailView.vue` SHA256 `252A1EE2FA90E1A03FFF2F37F2331EB01EC694ED7B07811F51B57287A0D70256`。
- 真实Chrome合并运行：`e2e/process-formal-live.spec.ts` 4项（含1440/1280/900表单底栏几何）、`e2e/process-real-rbac-live.spec.ts` 2项；**6 PASS/0 FAIL/0 SKIP（19s）**。等效200%专项另 **1 PASS（9.3s）**。
- 正式前端生产 `npm.cmd run build` 含 vue-tsc，4179模块，**BUILD PASS**；全量Vitest **249/249 PASS（111 test files）**，定向4文件10项PASS；`git diff --check` EXIT 0。
- 后端这轮无代码/SQL变更；继承本任务此前Maven20模块构建与Java定向24/24PASS、Flyway76条V076成功与真实DEV数据回读。
- 新证据 `docs/development/process-form-cpp-ebr/20261106-formal-code/g5-footer-after/`：同一正式源码30张 PC原始截图（list/default/filtered, create/default/required-disabled, detail/forms/products, edit/forms/products, ebr-cpp/readback, ebr-form/canvas ×1440/1280/900）、`browser-capture.json`、`audit-manifest.json`。
- **真实逐图第一遍**：已实际以原分辨率浏览30张并登记每图八类视觉审核，工艺主页面无全页横溢出，主操作和正文在修复后无重叠；独立记录演练用EBR草稿把数字字段 `MIX_TEMP` 排在文书标题前，Canvas文书内容排版需调整（MEDIUM、OPEN）。多行 `工艺说明` textarea 标签中心偏移19px来自标签顶部对齐、不是同一行控件错位。前后视觉比较中修复的底栏和产品弹窗问题均标 RESOLVED，不删除旧截图。
- G5检查工具的原始业务API失败数0、JS pageerror0；三个独立Chrome会话在未建立登录态时对`auth/me`/`auth/refresh`出现24个401挑战，已作为身份握手单列，非消失的故障。

### 未达到G5最终门禁：禁止误报READY/ACCEPTED
现行 G5正式manifest `g5-footer-after/audit-manifest.json` 记录30图、6表面、54个九维项目和19WCAG最低准则；九维/基础WCAG仍有 NOT RUN，第二遍完整对抗、全角色/真实并发恢复、浏览器原生缩放与焦点播报未全部完成。**校验器真实退出1 FAIL**。其中特别存在已批准本工艺PC_ONLY（三宽）与当前校验器“仅允许原型 #/processes 使用PC_ONLY例外”的范围冲突，强制正式路由增加375；未经新批准不能篡改其判定、伪造375截图或将其记PASS。当前**功能链路已可用、专项回归PASS；G5整体IN PROGRESS/NOT READY，用户尚未ACCEPTED。**


## 2026-11-06 22:54–23:17 CST — 当前正式源码键盘与 Canvas 文书最后整改

**状态：FORMAL FUNCTION IMPLEMENTED + REAL CHROME 10/10 PASS + G5 PARTIAL / NOT ACCEPTED**。执行者为 ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max；当前唯一 main，HEAD `1682fc5`，所有原有未提交文件均保留，未新建 worktree、未 stage/commit/push。

### 实际已完成
1. **确证工艺当前定义并发合同**：按已批准 `BASIC_CURRENT_DEFINITION_CONTRACT.md`，生产工艺当前定义在 READ_COMMITTED 下行锁串行化、最后合法保存生效，没有 If-Match 和客户端版本号/修订号比较，也不要求“旧版本拒绝 409”。此项原 G5 需求中的“stale concurrent current-definition 409”与冻结合同冲突；不擅自引入伪版本门禁、不弱化 eBR及生产执行既有受控版本校验。
2. **只修本人先前建立的独立 DEV EBR 草稿模板 `663`**：正式GET看到 `canvasDocument.data.main[0]` 是 `MIX_TEMP` 控件，紧贴 `溶液配制记录` 标题。经严格检查模板 `DRAFT`、字段只有 `MIX_TEMP`、原 5 元素 Canvas 和 `EDIT` 权限，用正式 `PUT /ebr/templates/663` 携 versionNo、If-Match、Idempotency-Key、受控原因重新排列 Canvas 到标题/副标题下方正文，同时原封不动保留 form fields、`operationDefId`、所有 rule/signature/review 定义。HTTP **200 OK**，GET 回读版本号 `3`、字段码 `MIX_TEMP` 与 CPP 规则 `CPP_OP1_RECORD_1` 保持，专项 **1/1 PASS**；再次执行时不重复写入。此为 DEV 草稿内容排版修复，不是历史记录改写。新 `1440/1280/900` 原图在 `g5-canvas-corrected/` 和 `g5-current-20261106/`，三张原图实际查看标题、正文分行已正常，原 MEDIUM `G5-DRAFT-CANVAS-FIELD-PLACEMENT` 改标 **RESOLVED**，before/after留存。
3. **键盘无障碍缺陷（正式 Vue）**：原 `role=tab` 的工序与适用产品页签不响应方向键；产品弹窗由键盘开启后焦点留在背景开关，Escape关闭后焦点未恢复。只在 `ProcessDetailView.vue` 增加左右方向键、Home、End、roving tabindex，以及弹窗开闭时聚焦搜索控件/恢复触发按钮。真实 Chrome 首次验证暴露两缺陷并分别修复，最终键盘专项 **1/1 PASS**；未改业务数据或权限。
4. **当前源码完整定向回归**：`process-formal-live.spec.ts`（4）、`process-real-rbac-live.spec.ts`（2）、`process-pc-zoom-reflow.spec.ts`（1）、`process-a11y-keyboard.spec.ts`（1）、`process-canvas-content-repair.spec.ts`（1）、`process-canvas-render-after-fix.spec.ts`（1）共 **10/10真实 Chrome PASS，0 FAIL/0 SKIP**。`npm run build` 包括 TS/Vite（4179模块）PASS；`vitest run` **249/249 PASS，111 files**；`git diff --check` EXIT0。后端源码本轮未改，承接前一轮Java定向24/24PASS与V076 MariaDB迁移成功证据。本机服务8080健康UP，5173和3306运行中。
5. **重新绑定当前源码视觉证据**：`ProcessDetailView.vue` 当前 SHA256 `E2B9E82F0650848B7F7D894E39CC0A6F8EB2040C1822C7083D7E6B4D6DCDC937`。对正式5173/8080在1440/1280/900三种PC宽度重采10状态×3 = **30张全页原始图**与DOM，`g5-current-20261106/browser-capture.json`显示 **0业务API非身份错误、0JS pageerror、0整页水平溢出**；初始未登录的24次auth/me/refresh 401身份握手仍原样单列。当前 `g5-current-20261106/audit-manifest.json` 保留6表面、30图逐项审核、54项九维维度及19WCAG最低条款，4项 HIGH/一项 MEDIUM的已修复项目状态如实记录，不把未核的全维度标PASS。

### 尚未结束的正式验收
现行校验器对本模块的 PC_ONLY 例外仅接受原型 `#/processes`，**不接受正式 `/process/packages` 路由**并强制375手机视口，尽管项目曾批准 PC 1440/1280/900范围。这是范围规则不一致，未经修订授权不能擅改审查器或伪造375截图。审查器真实 **EXIT1 FAIL**。全部角色矩阵、原生浏览器缩放/完整键盘无障碍播报与19项WCAG全量检查、G5二次对抗审查仍有 NOT RUN。虽然单受限仓管真实403与重复代码真实409、工艺/EBR/CPP所有核心功能已实测，仍**不能宣布 G5 READY/ACCEPTED**。本轮不在此轮扩大已冻结需求或审批生命周期。
