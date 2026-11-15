# 生产纸质实例驱动的MES完整闭环核查（2026-11-09）

状态：IN PROGRESS / NOT READY / NOT ACCEPTED。执行设备：PC-202401011703；本机唯一main，HEAD 77e1c67。当前报告为本轮自查，非人工G5接受。未提交/推送。

## 必读基线
实际读取：AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md。FINAL BASELINE v1.0.23及已批准DCP、UI V2/T1–T6维持权威；仅测试夹具适配，不修改正式业务规则与电子签名。

## 输入业务实例（file目录九张原图）
1-01/1-02/1-03：氯化钾溶液30ml、批次20231218、批量120000ml；原辅料核验、称量、清场、配制、搅拌15分钟、过滤、9.5%–10.5%含量标准（记录9.85%）、分装/贴签/包装、成品检验（报告2023-12-22）。
2-1至2-6：喉咽茶合剂100ml、批次20231215、批量400000ml；称量威灵仙/野菊花/地胆草/甘草，两次水提、浓缩、配制、装量抽样（95–105ml，五次记录100.1/100.1/100.2/100.1/100.2）、分装、贴签、包装及2023-12-27成品检验。主闭环场景建议用此例，氯化钾作为对照。历史纸质单据仅作为业务规格和操作顺序参考，不将其手写签名、放行结论自动导入为电子签名/生产实绩。

## 执行证据
- DC在线，监听端口：前端5173、后端8080、MariaDB3306、Redis6379；当前本机main与origin/main跟踪，无新worktree。
- Maven Java21 + Spring Boot 3.5.16，真实MariaDB hospital_pharma_mes_dev；Flyway76 migrations VALID，当前V076，无新迁移。
- 现有真实数据事务回滚用例 ProductionBookIT：首次3 tests / 0 failed / 3 errors / 0 skipped，exit1。共同根因旧夹具以5参构造EbrCommands.Definition未声明schemaVersion，现行EbrRoleSchema.prepare明确拒绝非2.0草稿。正式校验正确保留。
- 有界修复：仅 backend/mes-boot/src/test/java/com/hospital/mes/ebr/integration/ProductionBookIT.java 的一个测试数据构造，追加显式schemaVersion "2.0"。不改测试断言、业务代码或数据库。该测试基于@ActiveProfiles(ci)、@Transactional的IncomingQualityFixture。
- 复跑原命令：mvn -pl backend/mes-boot -am '-Dtest=ProductionBookIT' '-Dsurefire.failIfNoSpecifiedTests=false' test -q；3 tests / 2 PASS / 1 FAIL / 0 errors / 0 skipped，exit1，约177秒。剩余失败：newBookMappingsCannotSelectArchivalProcessVersions 的预期异常文本包含 'New book mappings must bind'，实际BookTemplateService.requireCurrentBinding返回 'Legacy draft mappings must bind the current production process before publication'。均为拒绝归档历史工艺绑定，但验收断言和当前业务异常消息合同不一致，不能擅自弱化/改断言。
- PDF流程在复测中实际启动，日志包含Fontbox cmap格式警告、不可变整册PDF受控异常；不能把这些当作完整PDF或最终归档验收通过。

## 剩余闭环与门禁
1. 核实上述异常消息的冻结合同，按授权消除契约不一致，重新跑完整ProductionBookIT并保存所有用例结果。
2. 将两条纸质实例映射到正式产品、处方、工序、表单及CPP字段；不得编造未显示的数值、SOP合法性或签名。
3. 使用正式5173/8080真实操作完成WMS物料放行→计划/批次→称量/提取/浓缩/配制/装量抽检/包装→eBR保存/提交/签署→QC检验→QA放行→成品入库/整册归档；用独立DEV演练记录，保留真实DB关联及异常恢复证据。
4. 补齐实际身份权限、CPP超限及负例、签名与PDF历史冻结、G5视觉和手工验收。

结论：本轮找到并修正测试装配失配，使3个schema2 ERROR转为2 PASS/1个契约FAIL；不是全生产闭环PASS。QC基础信息仍保持USER ACCEPTED，不触碰其业务功能。工作区其它未提交源码和配置原样保留。

## 2026-11-09 16:47-17:08 续作：冻结契约、正式Chrome与真实MariaDB

### 冻结契约
读取 EBR_BOOK_DCP_2026-10-08、EBR_BOOK_SELF_APPROVAL_SUPPLEMENT_2026-10-08 及 BookTemplateService。旧映射缺当前工艺绑定应返回业务码 BOOK_CURRENT_PROCESS_REQUIRED；ProductionBookIT 中现有定向校验已改为稳定业务错误码核对，真实MariaDB定向测试 ProductionBookIT#newBookMappingsCannotSelectArchivalProcessVersions 退出码0，PASS。没有更改正式生产规则。完整ProductionBookIT未在本轮重跑，不称全套PASS。

### 正式5173/8080 Chromium1248真实页面（无API拦截、无业务写）
浏览器专项5次各1/1 PASS：9个生产/质量页面、9个成品页面、喉咽茶工艺与处方、8道工序表单、eBR和QC基本资料。首次浏览器依赖预期Chromium1243缺失，后复用已安装Chromium1248，不新建端口。
产品 HYTEA100 喉咽茶合剂 100ml/瓶 启用。工艺 HYTEA100-制备工艺，真实页面 /process/packages/2：8道工序，均有1表单、配置可用。模板 EBR-HYTEA100-BATCH-RECORD (id=497) V1 草稿，尚未审批/发布；该状态不能作为生产执行的有效冻结模板。
OP01 水提投料：23字段、CPP0；OP02 第一次提取：9字段、CPP1；OP03 第二次提取：9字段、CPP1；OP04 浓缩：7字段、CPP1；OP05 配制：19字段、CPP0；OP06 分装与装量检查：11字段、CPP1；OP07 贴签与包装：16字段、CPP0；OP08 清场：8字段、CPP0。尚未逐条复验CPP数值上下限和超限动作。
生产处方 RX-HYTEA100-制备工艺 当前草稿，标准批量400000（UI单位#3）。生产批、生产质量计划、成品入库/检验/放行/库存页面尚未找到喉咽茶的完整关联记录；QC标准列表当前加载页面未命中喉咽茶专用标准，需进一步完整核对。
生产检验页面真实HTTP 403，样品列表请求HTTP 400（Invalid ID），应分别排查角色/查询参数，不可当成通过。
现有氯化钾实际批 KCL-20261012234332-F 在正式页面：入库995ml、出库300ml、库存695ml，检验报告已批准、QA放行、库存可用。只是历史页面数据一致性，不是本轮新执行QA签名。

### 真实MariaDB回滚集成测试结果
CppControlledRoundTripIT：1测试0失败0错误，PASS；不等于HYTEA具体规则已验。
FinishedGoodsLifecycleIT#completeChainRequiresQaThenShipsExactStockOnceAndTracesAllSources：1测试1错误，Product output uses MainBatch only。现有旧测试fixture输出携带lotNo；正式V2产品直连库存仅允许MainBatch，不改生产安全门禁。
ProductionBookFinalIT：36测试，21错误，主要为 New product plans cannot bind legacy batches；该子类approvedBatch仍调用旧preparedBatch(false)，与当前直接生产指令模式不符。不能称最终QA/PDF归档已通过。
RealBusinessScenarioSeedIT#seedProductionAndFinishedChain：1错误 REPORT_EVIDENCE_CHANGED，被正式来料报告证据一致性校验拒绝；测试事务已回滚，禁止修改旧来料报告绕过。
Flyway 76 VALID，当前V076，未执行迁移。无正式业务源码、签名、数据库历史、QA决定或已验收QC基本资料改动；未stage/commit/push或新建worktree。

### HYTEA按九张2023年纸质记录的验收映射
OP01核对威灵仙/野菊花/地胆草/甘草的两罐投料、批次、复核；OP02/03验证蒸汽压力0.04-0.25MPa、釜温约95摄氏度、各次加水1050L及提取收率；OP04验证真空度0.06-0.07MPa和浓缩体积；OP05验证配液投料、补水到400L、搅拌/加热；OP06装量95-105ml及五次抽检100.1/100.1/100.2/100.1/100.2ml；OP07验证包材平衡、产品数量和标签；OP08清场/记录完整性。以上为历史纸质参考，不得写入新批次为伪造实绩或电子签名。

### 状态
契约定向关闭；HYTEA工艺和表单存在性核实；真实批次→受控表单→CPP越限→eBR→QC/QA→成品入库/最终PDF尚未完成。后续必须先确认模板独立审批发布、处方激活、合格物料批和产品质量标准、角色权限，处理遗留测试夹具，再做真实UI写入与G5完整审查。当前 IN PROGRESS / NOT READY / NOT ACCEPTED。

### 补充 CPP 规则详情专项（本轮末）
真实 Chromium1248 /ebr/templates/497 页面点击“CPP规则设计”后，试图点击“第一次提取记录”，定位器等待超时75s。专项 0 PASS / 1 FAIL（exit1）；Playwright 保存 test-failed-1.png、error-context.md、trace.zip，目录 frontend/mes-web/test-results/hytea-cpp-read-HYTEA-CPP-rules-current-draft-read-only-chromium-desktop。仅此一例不能判定规则本身有错，但规则阈值和超限动作尚未核验，不能计入CPP完整验收。临时脚本归档 docs/development/process-form-cpp-ebr/hytea-cpp-read.spec.ts.txt；前面5个只读脚本亦已移到同目录，避免本地浏览器可执行路径影响全仓库CI。
