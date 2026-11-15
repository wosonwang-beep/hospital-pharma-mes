# 喉咽茶合剂完整生产闭环 DEV 实施记录 — 2026-11-09

执行范围：正式本机5173/8080 + MariaDB `hospital_pharma_mes_dev` V076；单一本机 `main`，HEAD `77e1c67`。操作均在用户授权的开发环境，原纸质记录是业务场景数据源，不是实际电子生产记录或合法质量放行证据。
依据：AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、docs/acceptance/BATCH_RECORD_PROCESS_EBR_ACCEPTANCE_20261015.md、docs/review/MASTER_DATA_COMPLETION_20261012.md、file/2-1.jpg 至 file/2-6.jpg、已批准 EBR_BOOK_DCP。

## 实际执行（独立真实请求与正式页面）
1. 生产工艺已有 `HYTEA100-制备工艺` ID2：8道工序/8份表单；OP02、03、04、06共4条CPP规则；页面及正式后端API核对。
2. eBR表单模板 `EBR-HYTEA100-BATCH-RECORD` ID497：Lint显示“校验通过”，正式页面点击提交审批，状态 DRAFT→SUBMITTED，重读成立；**尚未批准及发布**。当前管理员11556试图执行批准，后端拒绝 `Independent approver cannot be creator/submitter`，状态仍SUBMITTED。现有七个DEV岗位登录实测均无 `ebr:template:approve`，15个现有角色中仅SYSTEM_ADMIN能授予该操作；未通过伪造第二身份、改权限或直接SQL写状态绕过独立审批。
3. 生产处方 `RX-HYTEA100-制备工艺` ID2：正式页面点击“设为在用”，状态 DRAFT→ACTIVE，重载回读成功；生效时间2026-11-09T09:25:21.374Z。产品ID2、工艺ID2、单位ID3、批基准400000ml；6项配料质量分别为威灵仙96kg、野菊花48kg、地胆草48kg、甘草16kg、蔗糖30kg、苯甲酸钠1.4kg，与2罐及配液历史记录对应。无处方内的历史实测数据预填。
4. 新 DEV 生产指令：业务命名校验 `tools/check_business_seed_payload.py` PASS / violations []。通过正式 `POST /api/v1/main-batches` 返回HTTP201、OK、ID582，批号 `HYTEA-20261109-001`，产品HYTEA100，计划400000ml，状态DRAFT。正式 `GET /production/batches/582` 页面读回一致，进度0/0工序、物料预留0、尚未下达/签名/执行/放行。未冒用历史批号20231215。

## CPP真实配置读取（只证明定义，不是运行中实测）
- OP02第一次提取：压力0.04–0.25MPa，温度95℃；OP03第二次提取同样压力0.04–0.25MPa、温度95℃。
- OP04浓缩：真空度0.06–0.07MPa；OP06分装：装量最小≥95ml、最大≤105ml。
- 四条均 `VALIDATION` / `ON_CHANGE` / `BLOCK`，cppConfigJson中的超限动作均为 `MARK_EXCEPTION`、`BLOCK_SUBMIT`、`CREATE_DEVIATION`；真实运行触发及偏差落库还未执行。历史2026-10-15验收曾证明0.30MPa阻断，但不得等同本轮实际运行通过。

## 受控前置阻断
1. eBR模板尚缺另一名独立且有 `ebr:template:approve` 的合法审核身份与批准记录；不得让提交人另用自己控制的临时账号冒充独立复核。
2. 正式库存分页100条中，四味药材（materialId6–9）未查到可用物料批；蔗糖(materialId10)库存标记TESTING/BLOCKED，苯甲酸钠(materialId11)批次处于PENDING_QA_RELEASE、QC_PASSED或QUARANTINE且均BLOCKED。按docs/review/MASTER_DATA_COMPLETION_20261012.md，威灵仙等四味药材缺已证明的供应商关系，不允许从产地猜供应商并伪造资格、检验、物料放行。
3. 产品ID2关联的现有ACTIVE QC标准仅 `E2E-DBG-01066`、`E2E-TABS-309768` 两条历史临时数据，不是有可靠来源的喉咽茶正式成品质量标准；不能用作QA合格结论。
4. 已有真实业务回归旧夹具仍有质量计划/产品直连模型不一致（`Product output uses MainBatch only`、`New product plans cannot bind legacy batches`）、旧来料 `REPORT_EVIDENCE_CHANGED`，本轮没有修改正式安全门禁掩盖问题。

## 没有执行并必须保留NOT RUN的步骤
生产批下达、WMS领料与实际投料、八工序实际填写与电子签名/独立复核、CPP运行超限、完整eBR整册、成品请验/取样/检验、QA决策、成品入库和FINAL PDF归档。所有这些必须在真实受控资料和权限满足后逐步执行；不能从历史纸质签名或测试值推断为已完成。

## 证据、变更边界、结论
浏览器脚本及执行日志见 `docs/development/process-form-cpp-ebr/20261109-operation-scripts`（脚本归档为txt，防止被Playwright全量测试自动再次执行写请求），原运行证据在frontend/mes-web/test-results和进程日志。新业务载荷检查文件 `hytea-dev-batch-business-payload.json`。未修改已验收QC业务代码、没有重建DB或执行迁移、无新worktree、无commit/push，现存500余项未提交其它改动保持原样。
状态：**PARTIAL EXECUTION / BLOCKED BY INDEPENDENT APPROVAL + REAL MATERIAL/QC DATA / NOT READY / NOT ACCEPTED**。已实际生效的是处方和生产指令草稿；eBR仅已提交。不能声称整批次生产、QA放行和归档已经完成。


## 2026-11-09 18:10 eBR管理员直接发布已部署并真实生效

### 用户确认与DCP
用户明确确认：eBR模板配置无需独立批准，所有模板配置由同一系统管理员完成。批准DCP：`docs/development/decisions/DCP-EBR-ADMIN-DIRECT-PUBLISH-20261109-APPROVED.md`。范围仅配置性模板；工序运行态电子签名、独立复核、CPP偏差、QC/QA、成品放行均保持既有控制。

### 实现与部署
- 后端 `EbrDefinitionRules.transition` 允许DRAFT/SUBMITTED/APPROVED直接PUBLISH→EFFECTIVE；旧SUBMIT/APPROVE API保留兼容。
- `TemplateEntity.allowedActions` 为DRAFT/SUBMITTED/APPROVED提供PUBLISH，不再为正式UI提供SUBMIT/APPROVE操作。
- `EbrService.command(PUBLISH)` 对已批准历史模板继续校验原哈希；DRAFT/SUBMITTED则基于当前canonical生成真实contentHash，保留发布Lint、工艺绑定、schema2角色ID、审计、If-Match、幂等和不可变定义。
- `TemplateDetailView.vue` 移除提交审批/批准按钮，保留管理员直接发布，审批信息改为只读历史信息。
- 本机Java21/Maven后端package成功；前端 `npm run build`（vue-tsc + Vite 4179 modules）exit0。运行中8080旧进程仅停止MES Java PID26432；备份旧JAR至系统TEMP。新JAR PID19288启动，Tomcat8080正常，健康检查UP。MariaDB Flyway 76/76 valid，V076，无迁移。

### 实际发布证据（真实正式5173/8080 Chrome，不是模拟）
- 管理员admin在模板详情497看到“发布模板”，填写操作原因并点击；真实 `POST /api/v1/ebr/versions/497/publish` HTTP200 / code OK。
- 模板 `EBR-HYTEA100-BATCH-RECORD` ID497 V1：SUBMITTED→EFFECTIVE；重载正式页面后仍显示“已生效”。
- contentHash：`4b9aaa431f7a7834ecd57d96836299143fcc06b514088723c84b998f840f456c`；effectiveFrom：`2026-11-09T09:59:31.034Z`；8表单/4CPP规则保留；approvedBy、approvedAt为空，UI显示“无（配置直接发布）”。没有创建或伪造审批人。

### 回归
- `EbrIT`：15/15 PASS，包含DRAFT直接发布、SUBMITTED直接发布、发布权限、Lint拒绝空模板、哈希和不可变、运行时review/signature规则仍存在。
- `EbrDefinitionRulesTest`：6/6 PASS。
- `EbrRoleSchemaTest`：12/12 PASS。
- `EbrRuntimeArchiveIT`：14 tests / 13 PASS / 1 FAIL / 0 ERROR。为使运行态签名测试使用当前schema2，测试fixture `EbrRuntimeIT.java` 已补显式schema2、真实roleId及事务内sys_user_role/sys_role_permission关系；运行态签名、独立复核、重签/撤销、角色负例和其他12项通过。唯一剩余失败是既有冻结DTO断言 `BatchEbrReadModel.processSnapshot undeclared processPackageId`，不在本DCP范围，也非本次改动文件，未修改冻结断言/生产DTO掩盖。
- 临时真实浏览器发布脚本已归档到 `docs/development/process-form-cpp-ebr/20261109-operation-scripts/`，不留在Playwright自动全量测试目录。

### 当前边界
模板497已生效，处方2仍在用，DEV生产批582仍为草稿。未下达批次、未假造原料批次/来料放行、未做生产签名、QC/QA或成品入库。原料可用库存和正式成品QC标准仍是喉咽茶真实生产闭环的下一阶段前置条件。无数据库迁移、无worktree、无Git提交/推送；其他未提交改动保持原样。

结论：**EBR CONFIGURATION DIRECT-PUBLISH DESIGN IMPLEMENTED / DEPLOYED / HYTEA TEMPLATE 497 EFFECTIVE。** 生产运行态完整闭环仍PARTIAL/BLOCKED。

## 2026-11-09 18:30-18:45：真实运行态实例核验与生产批T4页面状态修复

### 真实Chrome/8080只读运行证据
- 本机依AGENTS.md、MES_DEV_RULES.md与冻结基线作业；正式管理员登录实际读取：HYTEA-20261109-001，批次582，status=RELEASED（生产已下达），冻结工艺快照480，直接执行单元515/READY，计划400000ml。
- 八道生产工序 id655-662 已初始化；OP01 READY，OP02-08 PENDING，全体clearanceRequired=true，无完成证据。八份eBR表单 id689-696（OP01_RECORD至OP08_RECORD）均DRAFT、formRevision0，每份定义哈希都与已发布模板497的4b9aaa431f7a7834ecd57d96836299143fcc06b514088723c84b998f840f456c一致。分装OP06已初始化95-105ml数值IPC，尚未执行。物料预留0、QA未放行。
- 库存真实读取：威灵仙6/野菊花7/地胆草8/甘草9均无可用仓储批次；蔗糖10为TESTING/BLOCKED，苯甲酸钠11为PENDING_QA_RELEASE或QUARANTINE及BLOCKED，单独QC_PASSED不等于物料放行。
- 18:37发现同一工作区原hytea-prerequisite-read.tmp.spec.ts已被其他进程新增供应商写操作，本轮没有运行/覆写该脚本。独立只读检查确认DEV系统已有三家新增企业及4味药材关联：威灵仙/甘草->盛实百草，野菊花->九州通安国，地胆草->广东药材；系统资料显示approved/合格。但这些仅为当前DEV配置，不是已经核实具体采购批质量或实际历史供货方。本轮未再次新增或变更供应商。
- 成品标准方面，产品HYTEA100仍只有两个调试性临时规格被查询到。根据file/2-6.jpg与2-4.jpg整理9个历史检验项目及标准限度，见20261109-HYTEA-QC-SOURCE-DRAFT.md；未将历史检验结论写成2026新批的实测，未新增ACTIVE成品质量标准。

### UI/T4受控显示修复
- 冻结生产状态RELEASED原被360视图误译为“已放行”（易被当成QA成品放行），只在BatchOverview.vue改成“已下达”，保留QA_RELEASED原有含义及业务状态/API不变。BatchOverview.test.ts新增生产已下达和QA未放行对照断言。
- Vitest BatchOverview 4/4 PASS，npm run build（vue-tsc + Vite）exit0，git diff --check无差错。真实浏览器原共享test-results目录遇并行清理发生trace ENOENT，虽业务断言通过但exit1；使用独立临时output、trace=off复跑实际Chrome 1/1 PASS、exit0，产品名称/规格/产量/“已下达”/QA未放行显示一致，8工序和8份eBR定义哈希与已生效模板完全一致。只读专项脚本已归档到20261109-operation-scripts/hytea-readonly-status-20261109.spec.ts.txt以免污染默认全套回归。
- 本轮未改QC基础页面、数据库迁移、既有签名和已放行记录，未建worktree、未Git提交或推送；保护单一main其他既有未提交修改。

### 当前结论
实际批已下达且eBR表单实例化正确，仍未领料或执行工序；缺少六味合格可用来料批与正式成品QC标准，尚不能取得八工序实绩、电子签名/CPP实测、QC/QA放行、成品入库与FINAL PDF的完整通过证据。STATUS=PARTIAL / NOT ACCEPTED。

## 2026-11-09 DEV喉咽茶原辅料收货与QC请验真实写入
依据用户再次明确指令“继续完成整个流程”，本机单一main、同一MariaDB hospital_pharma_mes_dev实施。新记录为DEV业务流程演练，不等同真实采购、物理取样、仪器测量或合规签名。已实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md、docs/PROJECT_BASELINE.md及本任务既有证据，保护其他未提交改动。

- 生产批582 HYTEA-20261109-001已RELEASED（生产已下达），工艺快照480、DIRECT执行515、工序655–662、8份运行表单689–696、模板497已生效。生产执行、物料消耗、成品QA与入库尚未完成。
- 正式Chrome/5173/8080接口完成5张WMS收货单和确认：1146（威灵仙101kg lot1121、甘草18kg lot1122）、1147（野菊花50kg lot1123）、1148（地胆草50kg lot1124）、1149（蔗糖32kg lot1125）、1150（苯甲酸钠2kg lot1126）。所有CREATE/CONFIRM HTTP200/codeOK，确认APPROVED；库存回读6批均QUARANTINE/BLOCKED，无伪造质量放行。
- 通过当前ACTIVE入厂质量标准1415/1416/1417/1418/23/24，对6个物料批次正式创建请验721–726，各HTTP201/OK；提交各HTTP200/OK，全部SUBMITTED。尚未受理、取样、检验、报告与物料QA。
- 由admin尝试请验受理，六次HTTP422 QUALIFICATION_REQUIRED，业务事务拒绝，状态保持。现存DEV取样11566（demo.sampler）有MES_DEMO_SAMPLING资格但缺qms:inspection-request:accept权限；DEV QC分析11567（demo.qc.analyst）有该受理权限但缺MES_DEMO_SAMPLING资格。QC复核11568、QA11569有各自有效资格。不能用系统管理员权限代替独立岗位培训/签署。
- 已准备通过正式资格管理API给现有DEV QC分析岗位补DEV专用MES_DEMO_SAMPLING资格、再由该用户独立登录正常受理；桌面代码编辑连续两次被平台安全预检阻止，按规范提交review_device_call，等待用户在审核面板Run，操作尚未执行。未通过SQL修改资格、未冒充岗位。
- 历史纸质资料file/2-1~2-6只用于流程与质量标准参考，不能自动作为新批的实测/QA决定；现存供应商关系为DEV配置而非证实的历史供货。

真实Chromium：hytea-incoming-20261109.tmp.spec.ts 1/1 PASS；hytea-qc-request-20261109.tmp.spec.ts 1/1 PASS；hytea-actors-read.tmp.spec.ts 1/1 PASS；hytea-qc-accept-20261109.tmp.spec.ts 0/1 FAIL（独立岗位权限缺口）。所有正式业务写入使用REST+幂等键，无数据库迁移、未建worktree、未git提交推送。

下一阶段：核准受理资格与账号→六批物料真实QC受理/取样/检验/复核/QA物料放行→领料称量与8道工序CPP/eBR→成品QC/QA/入库/整册归档。状态 PARTIAL/BLOCKED/NOT ACCEPTED。

## 2026-11-09 19:22 新DEV生产批与质量计划前置补齐
前一生产批582 `HYTEA-20261109-001`已正式下达/冻结工艺eBR但没有APPROVED生产质量计划；`ProductionQualityPlanService.draftBatch`只允许DRAFT批建立/批准计划，`MaterialBalanceService.requirePlan`及`ProductionQualityService.plan`均要求已下达快照冻结签署APPROVED计划。故不得修改582原快照/补SQL伪造质量计划；582只作已冻结业务验证证据，不记作成品闭环合格。
通过正式5173/8080管理员业务REST创建新的DEV生产批 `HYTEA-20261109-002` ID655（201/OK，DRAFT），绑定产品2、标准批量400000ml；随后创建生产质量计划ID666（201/OK，DRAFT），关联已在用喉咽茶成品标准ID1419，平衡规则 INPUT_MATERIAL_BALANCE 为 ISSUE 计划投入量 vs CHARGE 实际投料量、单位kg、差异±0.5%、BATCH_COMPLETE检查点。质量计划作者admin(11556)。待独立QA demo.qa(11569)用正式重新认证与审批接口签署，方可下达本批冻结质量计划。准备的 `hytea-plan-sign-20261109.tmp.spec.ts` 编辑调用因平台安全预检连续两次拒绝，已发起Desktop Commander用户review_device_call，尚未实际写入脚本或发出批准请求；不可声称计划已批准或批655已下达。
真实MariaDB回滚测试 `mvn -pl backend/mes-boot -am '-Dtest=ProductionQualityPlanIT' '-Dsurefire.failIfNoSpecifiedTests=false' test -q` PASS，15 tests/0 failures/0 errors/0 skips，exit0；Flyway76 VALID，现V076；该测试属于回滚夹具验收，不能替代业务批655实际独立QA签署。功能流程仍 PARTIAL / NOT READY / NOT ACCEPTED，未Git提交/推送，单一main，保留所有其他已存在修改。

## 2026-11-09 19:32 续跑：前端恢复、当前状态复核及来料质量链回归
- 本次重新读取本机AGENTS.md、docs/development/MES_DEV_RULES.md、docs/PROJECT_BASELINE.md、MES_TASKS.md和数据库验证规范；单一main工作区保留既有未提交修改，无stage/commit/push。
- 确认后端8080、MariaDB3306、Redis6379正常，健康UP；原Vite 5173没有监听，已从frontend/mes-web用npm run dev启动原有5173 strictPort，Vite ready 400ms，PID16152。没有第二个前端端口或数据库。
- 正式Chromium1248管理员登录，实际GET生产批655仍DRAFT、生产质量计划666仍DRAFT且无signatureId；6张请验单721–726全部SUBMITTED、versionNo1；现有DEV QC分析岗位11567对应MES_DEMO_SAMPLING资格不存在。前两项DC审核面板操作均尚未执行；未写资格或签名。
- GET入厂QC标准1415威灵仙(5项目)、1416野菊花(5)、1417地胆草(2)、1418甘草(5)、23蔗糖(1)、24苯甲酸钠(1)，和成品标准1419(8)，全部HTTP200且ACTIVE。仅证明DEV标准配置存在，不代表物料实检或正式放行。
- 本机真实MariaDB回滚集成运行 IncomingQualityIT，命令mvn -pl backend/mes-boot -am -Dtest=IncomingQualityIT -Dsurefire.failIfNoSpecifiedTests=false test -q：退出码0，6/6 PASS。Flyway76 valid，V076，无迁移；只证明来料质量代码路径，不代替批次1121–1126实检或QA签名。
- 真实只读Playwright hytea-readiness-snapshot-20261109.tmp.spec.ts 1/1 PASS；脚本归档docs/development/process-form-cpp-ebr/20261109-operation-scripts/，避免误自动运行。
- 下一步需用户在已有两个Desktop Commander审核面板分别点击Run，授权正式DEV岗位取样资格配置以及独立QA对质量计划666的重认证签署。未执行前不得将655批下达、QC结果或QA成品放行当作已完成；582旧批冻结快照不能事后改写。
- 状态 PARTIAL / BLOCKED / NOT ACCEPTED。五张WMS收货1146–1150、六批原辅料1121–1126（QUARANTINE/BLOCKED）、请验721–726提交、eBR模板497生效、处方2在用、生产批655及质量计划666草稿均真实持久化保留。

## 2026-11-09 22:17 续跑：正式QA/eBR现状和未完成路径回归
本轮再次真实读取AGENTS.md、docs/development/MES_DEV_RULES.md、docs/PROJECT_BASELINE.md、MES_TASKS.md和任务记录，单一main且不改既有冻结契约。Desktop Commander设备PC-202401011703在线。5173、8080、3306、6379监听，8080健康检查HTTP200/UP；未重启数据库或执行迁移。
真实Chromium1248以现有DEV QA角色的登录会话只读检查 /api/v1/qa/batches/655/review-model 和 /main-batches/{655,582}/ebr：655 QA审核模型HTTP404 NOT_FOUND、655 eBR HTTP404 NOT_FOUND（质量计划666仍草稿、批次655尚未下达，符合目前流程状态）；已下达的批582 eBR HTTP200/OK，formCount=8，definitionHash=4b9aaa431f7a7834ecd57d96836299143fcc06b514088723c84b998f840f456c 与模板497发布哈希相同。未产生新签名/放行。
本轮目标性真实MariaDB回滚测试：IncomingQualityAcceptanceIT 3/3 PASS；FinishedArchiveSchemaIT 2/2 PASS；mes-qms BalanceFormulaTest 10/10 PASS，所有命令exit0。前者涉及来料QC签署、拒绝和不可变证据负例；归档表结构约束和物料平衡规则边界/缺输入/非法表达式均有可核验回归结果。Flyway当前V076，76项VALID。上述回滚测试不构成业务批655完整E2E通过证据。
真实只读Playwright hytea-qa-ebr-readonly专项1/1 PASS，脚本已去除明文DEV测试账号密码改成环境变量后从e2e默认目录移到docs/development/process-form-cpp-ebr/20261109-operation-scripts/hytea-qa-ebr-readonly.tmp.spec.ts.txt（保存测试运行摘要，不自动执行写入）。
尚未执行：QC分析岗位11567对应DEV取样资格、六张入厂请验721–726受理，物料1121–1126取样/检验/QA放行，QA对质量计划666重认证签署，655正式下达，领料称量、八工序清场/CPP/eBR、成品QC/QA、入库与最终PDF。此前两项Desktop Commander受控审核面板仍需用户按Run；不能用用户单独回复“继续”代替宿主面板授权。保持PARTIAL / BLOCKED / NOT ACCEPTED。无git commit/push，没有新worktree。

## 2026-11-10 10:28 DEV岗位配置与来料受理续跑
- 用户持续要求完成喉咽茶DEV全流程，依据已批准限域DCP DCP-DEV-HYTEA-SAMPLER-ACCEPT-001.md 进行本地角色最小权限修正，严格复用原账号和现有资格，不改生产角色/资格映射。
- 已通过正式IAM的 IamContractService.assignRolePermissions（@Commit MariaDB测试）给既有取样用户11566/角色288增加 qms:inspection-request:view、qms:inspection-request:accept；保留其有效 MES_DEMO_SAMPLING、qms:sampling:execute/complete。目标测试 DemoRoleAssignmentsSeedIT#grantInspectionRequestAccessToAlreadyQualifiedSamplerOnly PASS 1/1。
- 已通过正式 QualificationService.create 在本机DEV为既有仓储测试账号11565建立资格码 WAREHOUSE_SAMPLE_OUT（只属本机DEV测试资格，不代表任何真实员工培训）。目标测试 DemoRoleQualificationSeedIT#installLocalWarehouseSampleOutboundQualification PASS 1/1。
- 同一IAM审计化命令为QC分析角色添加 qms:sampling:assign，为仓储角色添加 qms:sampling:warehouse-confirm、wms:inventory:sample-outbound；保留QC分析用户11567既有 MES_DEMO_QC_EXECUTION 及仓储用户11565现有库存权限。目标权限修正测试再次PASS 1/1，日志 HYTEA_CROSSROLE_SAMPLING_PERMISSIONS=PASS,SAMPLER=11566,ANALYST=11567,WAREHOUSE=11565。
- 正式Chromium以demo.sampler执行6张请验721–726受理，6/6均HTTP200/OK，状态全部SUBMITTED→ACCEPTED；Playwright hytea-qc-accept-20261109.tmp.spec.ts 1/1 PASS。此前来料收货1146–1150及lot1121–1126不变。
- 交叉岗位来料取样/仓储出库集成回归 IncomingCrossroleBatchBIT 2/2 PASS（MariaDB回滚夹具），Flyway76 valid/V076。该回归验证业务链代码，不替代当前6批业务实绩。
- 原始全量种子测试的两个旧缺陷未绕过：DemoRoleAssignmentsSeedIT全类因现有权限目录缺少 qms:specification:approve 而失败（专用权限修正方法独立PASS）；DemoRoleQualificationSeedIT原全类被BusinessSeedPayloadGuard拒绝现有MES_DEMO_*字段，未修改guard，单独新增的合法 WAREHOUSE_SAMPLE_OUT DEV资格方法PASS。
- 当前待执行：已有Desktop Commander审核面板中的Playwright脚本修改仍awaiting_user（hytea-qc-accept...），只有用户点击Run才写入脚本；在其执行前，六个采样任务尚未创建。计划使用QC分析创建/分派采样计划，sampler完成取样/复封/样品接收，随后QC分析绑定/检验、QC复核、QA物料放行。之后仍需批次655质量计划666独立QA签署、批次下达、8工序CPP/eBR、成品QC/QA/入库/归档。
- 状态：来料请验已受理；采样任务、实物检验及物料放行尚未完成。不把DEV资格当作真实员工培训，不把DEV输入值当作实测。无Flyway迁移、无Git提交/推送、无新worktree。
## 2026-11-10 10:35 QC标准快照核对（只读）
通过只读的 QcSpecificationQueryService.read(1,id) 从当前MariaDB读取以下真实配置（不是新批实测结果）：
- 1415 威灵仙：水分≤15%，总灰分≤10%，酸不溶性灰分≤4%，浸出物≥15%，齐墩果酸≥0.30%。
- 1416 野菊花：水分≤14%，总灰分≤9%，酸不溶性灰分≤2%，浸出物≥7%，蒙花苷≥0.80%。
- 1417 地胆草：性状及基原鉴别两项文本判定；当前没有定量项目，依据引用《广东省中药材标准》第三册（2011年版）第219页，需在正式使用前核对原标准文本。
- 1418 甘草：水分≤12%，总灰分≤10%，酸不溶性灰分≤3%，甘草酸≥2.0%，甘草苷≥0.50%。
- 23 蔗糖、24 苯甲酸钠：含量均为99.0%–100.5%，单位%；依据引用《中国药典》2025年版二部。
- 1419 喉咽茶成品：相对密度1.030–1.070、pH4.00–5.50、装量95–105ml、需氧菌总数≤100、霉菌和酵母菌总数≤10、大肠埃希菌不得检出，另有性状及鉴别文本项。当前basisReference为《喉咽茶合剂注册标准》但effectiveDate为空，DEV流程可以用，正式生产批准前必须补齐注册标准编号/生效依据；不能据此声称正式标准已受控批准。
- QC标准ID1415/1416/1417/1418/23/24/1419共7项均可由服务读取。历史纸质成品报告的相对密度1.046、pH4.94、装量100.1/100.1/100.2/100.1/100.2ml、需氧菌80CFU/ml、霉菌酵母菌<10CFU/ml、大肠埃希菌未检出，仅能作为DEV流程字段映射输入，必须明确注明来源为历史纸质记录，不得记为2026批655实测。
- 采样量需考虑物料平衡：批655处方用量分别96/48/48/16/30/1.4kg，收货量101/50/50/18/32/2kg。采样/留样应控制在合理的小样量，确保苯甲酸钠剩余量仍高于1.4kg；不得使用测试夹具1kg的大样量造成物料不足。


## 2026-11-10 14:05 sampling plan contract gap — DESIGN CHANGE REQUIRED

Six existing DEV sampling tasks 689–694 are ASSIGNED to sampler 11566, but have null plannedSamplingQuantity and plannedUnitId. Root cause confirmed from current code: SamplingTaskCommand/createSampling only persists samplingPlan and requiredPackageCount, while submit-to-warehouse uses plannedSamplingQuantity/plannedUnitId for unit conversion and limit checks. Continuing as-is cannot complete cross-role warehouse outbound. Do not bypass with direct SQL or the simpler complete action.

A bounded proposal is recorded at docs/development/decisions/DCP-QMS-SAMPLING-PLAN-COMPLETION-001.md. It adds an audited, idempotent, If-Match protected pre-start sampling-plan completion command using existing qms:sampling:assign permission and conversion/request quantity checks. 0.125kg is proposed solely as a DEV sample fixture, not historical measured evidence.

A local experimental implementation was tested against real MariaDB rollback using IncomingCrossroleBatchBIT: 3/3 PASS after correcting decimal assertions. However AGENTS/MES_DEV_RULES state that continuing a task does not authorize changing a frozen API. The experimental service/controller/schema/test edits were therefore withdrawn and not deployed; 8080 remains the previous code. Only the proposed DCP and task record remain.

A Desktop Commander review panel is pending for writing a Playwright script to invoke the proposed API; because the DCP is not approved, do not Run it; cancel it. No changes were made to sampling tasks 689–694. Current blocker requires explicit user approval/rejection of DCP-QMS-SAMPLING-PLAN-COMPLETION-001. After approval, deploy the bounded API and continue sampler details/signature, warehouse outbound signature, QC sample receipt/binding and inspection. DB V076/no migration; no Git stage/commit/push or worktree. Status PARTIAL / DESIGN CHANGE REQUIRED / NOT ACCEPTED.
