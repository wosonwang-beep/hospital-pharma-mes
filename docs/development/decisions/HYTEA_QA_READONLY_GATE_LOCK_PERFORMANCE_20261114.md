# HYTEA QA 门禁只读计算锁竞争与性能修复

**状态：DESIGN LOCK / IMPLEMENTATION IN PROGRESS**
**用户授权：2026-11-14，明确要求“QA门禁只读计算的锁竞争与性能修复，随后重新执行完整G5验收”。**
**基线：FINAL BASELINE COMPLETE v1.0.23；仅本机唯一main工作区与hospital_pharma_mes_dev。**

## 1. 已核实根因

- `GET /api/v1/qa/batches/{id}/review-model`当前`FinishedReleaseService.review(String)`开启普通事务，调用内部review并锁生产订单/主批。
- 多个GET子调用继续锁定：生产QC锁主批和偏差行；eBR review锁工序；物料平衡锁主批并同步物流数量事件/审计；成品库存锁流水；收货使用共享锁；成品检验报告锁请求、取样和报告；旧物料批次分支锁库存证据。
- 这些锁对写入/正式QA决定必要，但普通GET不应取得。历史DEV请求23.409秒且出现MariaDB 1205。归档历史/PDF已验收，FINAL93/133和REVIEW_COPY94/134必须保持不变。

## 2. 冻结设计原则

**GET只读一致性快照；正式QA决定仍使用现有写事务、生产订单/主批/工序/库存锁、全部门禁与电子签名。** 不加缓存、后台同步、快照表、新接口或数据库字段；不减少检查范围；不改权限、签名、独立复核、recordDigest、版本或状态机。

- readOnly事务使用普通一致性SELECT读取批次、偏差、工序、库存、收货、取样、报告、数量流水和签名，不取`FOR UPDATE`/`LOCK IN SHARE MODE`，不写数量事件或审计。
- 已确认但尚未投影到`mes_quantity_event`的WMS物流事实，readOnly路径先返回“投影是否完整”的只读布尔结果；终结批次缺失来源仍立即保留`QUANTITY_BATCH_NOT_MUTABLE`，来源不匹配仍返回`QUANTITY_SOURCE_CHANGED`。随后按原顺序检查规则与计算历史，再对已有计算返回既有`BALANCE_RESULT_STALE`，因此`BALANCE_RULE_MISSING`/`BALANCE_RESULT_MISSING`的原优先级不变。GET不得补写；正式物料平衡重算负责同步。
- 非只读事务（正式QA决定、生产完成、物料平衡重算等）完整保留现有锁、同步、审计和签名。
- 静止数据下readOnly review与锁定review必须得到相同gates、blockingCodes、allowedActions和reviewDigest；并发变化由决定事务重新校验，GET结果不作为放行凭据。

## 3. 实施白名单

只允许修改：
1. `backend/mes-release/src/main/java/com/hospital/mes/release/application/FinishedReleaseService.java` — GET设为readOnly；内部review按当前事务readOnly选择无锁批次快照，决定路径不变。
2. `backend/mes-qms/src/main/java/com/hospital/mes/qms/application/ProductionQualityService.java` — readOnly使用已有`sourceRows`普通查询；写事务仍锁。
3. `backend/mes-ebr/src/main/java/com/hospital/mes/ebr/application/EbrRuntimeService.java` — readOnly跳过工序锁但执行同一evaluate；写事务仍锁。
4. `backend/mes-qms/src/main/java/com/hospital/mes/qms/application/MaterialBalanceService.java` — readOnly不锁批次/不同步物流，先验证投影完整，再验证最新计算与inputDigest。
5. `backend/mes-execution/src/main/java/com/hospital/mes/execution/application/ProductionQuantityService.java` — 新增只读`requireLogisticsSynchronized`，核对WMS确认事实与现有数量投影，不写入。
11. `backend/mes-execution/src/test/java/com/hospital/mes/execution/application/ProductionQuantityServiceReadOnlyTest.java` — 覆盖缺失投影、终结批次、来源不一致和一致投影，不访问数据库。
6. `backend/mes-wms/src/main/java/com/hospital/mes/wms/application/FinishedGoodsService.java` — readOnly库存/收货普通读取；写决策保留锁。
7. `backend/mes-wms/src/main/java/com/hospital/mes/wms/application/FinishedInventoryService.java` — 新增无锁ledger读取，既有locked仍用于决定路径。
8. `backend/mes-qms/src/main/java/com/hospital/mes/qms/application/FinishedInspectionService.java` — readOnly释放证据/采样/报告无锁读取；写入/批准路径保留锁。
9. `backend/mes-wms/src/main/java/com/hospital/mes/wms/application/FinishedQualityDecisionService.java` — 旧物料批次分支readOnly跳过证据锁，写决定仍锁。
10. `backend/mes-boot/src/test/java/com/hospital/mes/release/QaReadOnlyReviewLockIT.java` — 增加仅在明确系统属性启用时执行的DEV只读锁竞争测试，复用现有批次655，不创建/更新业务数据，持有订单/批次/工序/库存/收货/QC行锁时，独立readOnly review限时返回且与权威review门禁/摘要一致。

允许新增只存在于`private-data-audit/hytea-qa-readonly-g5-20261114/`的测试脚本与结果。禁止修改迁移、数据库结构、持久DEV数据、API/OpenAPI、角色/权限、GxP签名/审计、FINAL/REVIEW_COPY文件或其它模块。

## 4. 必需RED/GREEN与验收

1. 先新增测试，在原实现上证明readOnly review遇到持有的生产订单/批次锁会等待/超时（RED）；不修改断言来规避失败。
2. 实现无锁只读分支后同一测试GREEN：返回QA_RELEASED、8个门禁，gates/blockers/reviewDigest与权威锁定复核一致；正式决定路径仍保留锁。
3. 断言readOnly review不增加`mes_quantity_event`或审计记录；存在未投影WMS确认事实时返回BALANCE_RESULT_STALE，GET后数据库记录不变。
4. 运行相关Java编译、`FinishedReleaseIT`目标测试、`MaterialBalanceIT`和QA受影响回归，再做完整后端构建。
5. 真实DEV只读性能：批655至少10次，记录p50/p95/max、HTTP状态、8门禁与摘要；静止状态目标p95≤2秒且0个1205；并发持有订单/批次锁时GET不等待写锁。比较readOnly与权威review输出。
6. CUAWright重验QA页加载、门禁摘要、两份归档历史和下载；FINAL93/133 SHA `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`、REVIEW_COPY94/134 SHA `41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099`不变。复核九维G5与WCAG2.2未覆盖项。

## 5. 停止条件

若只读路径依赖GET副作用且无法用相同门禁码等价表达、需要新增业务状态/字段/表、或需改变QA决定锁/签名/recordDigest，停止为`DESIGN CHANGE REQUIRED`。禁止PROCESS提权、杀锁、修改MariaDB全局/会话参数或清理数据掩盖性能问题。Git保持单一main，不提交/推送，保留现有工作树改动。

## RED证据（2026-11-14）
新增DEV只读锁竞争测试`QaReadOnlyReviewLockIT`，仅在`-Dmes.qa.readonly-lock-fixture=true`时启用，使用既有批次655且只执行SELECT/SELECT FOR UPDATE。测试在一个独立事务同时持有生产订单、主批、工序、偏差、成品库存、收货、检验请求、取样及报告行锁，再通过readOnly事务请求QA门禁。原实现读取在6秒窗口内未返回（`Future.get`超时，测试RED）；同时此前独立实测该接口23.409秒。首次测试桩把工序执行表误当成含main_batch_id，已按真实`mes_operation_execution.execution_unit_id → prd_execution_unit.main_batch_id`关系修正；没有写入任何业务数据。

11. `backend/mes-execution/src/test/java/com/hospital/mes/execution/application/ProductionQuantityServiceReadOnlyTest.java` — 无数据库的纯Mockito断言缺投影、来源变更与完全匹配等分支，不能写数量事件。

12. `backend/mes-ebr/src/main/java/com/hospital/mes/ebr/application/EbrRuntimeModel.java` — 规则`inputs()`对同一冻结表单的字段元数据每个取值重复读取，改为每个表单只读一次并复用字段查找；原字段类型、单位换算、CPP判定及签名规则保持完全一致。

## 2026-11-15 QA 无锁只读 GREEN、DEV 服务恢复与 CUAWright G5 专项复验
- 唯一 main 已同步到 GitHub，进入本轮时 SHA `4bf835ff524ffe87375e3160f730dcd261020361`；本机原 `application-local.yml` 改动仍未纳入提交。重新打包 `mes-boot` 成功，Spring Boot 8080 / Vite 5173 均 HTTP200；MariaDB 3306、Redis 6379 正常，Flyway 77/77 VALID，schema V077；未进行新的迁移或数据修复。
- 正式 QA 决定无需重复：喉咽茶生产批655已 QA_RELEASED，QA用户11569、决定690、签名5348、八项门禁PASS，原 FINAL93/file133、REVIEW_COPY94/file134 均保留。
- 真实 MariaDB 的 `QaReadOnlyReviewLockIT` GREEN（进程 exit0）；持有订单、主批、工序、库存、收货及QC锁时，readOnly review 耗时1537ms（测试门槛1800ms），8 gates、blockingCodes、allowedActions、reviewDigest 与权威锁定路径一致，mes_quantity_event计数未增加。另记录首次无竞争读取2438ms；不能把单次结果宣称为P95已达标。
- CUAWright/真实Chromium只读G5：用户已存在的管理员登录成功；等待QA区域后八项gate均可见且无warning，浏览器资源单次QA请求1564ms；1440/1280/900/375四视口各有2条归档、无横向溢出，0 JavaScript pageerror、0业务写。FINAL下载501399字节/SHA `62902008f3278ebb80abf1b3654d446213830153fc819752aa4cf30c678e68a0`，REVIEW_COPY下载1161328字节/SHA `41e734d45cdf7f71901c5b9b5c133400e00460085282970b85388f7642d4b099`；键盘展开证据通过。初次打开1440时QA区域后于归档加载，但再次显式等待八项门禁完成正常；3次HTTP401属于首次会话auth/me、auth/refresh流程。
- 相关源码的纯单元回归：ProductionQuantityServiceReadOnlyTest 4/4、EbrRuntimeBoundaryTest 4/4、EbrPdfBusinessLayoutTest 1/1、BookPdfComposerTest 5/6 PASS且1 SKIP，累计14PASS/1SKIP/0FAIL；QA前端模型Vitest 8/8 PASS；QA归档并发/失败恢复 mock Playwright 6/6 PASS（复用本机现有Chromium1248，未更改正式断言）；Vue生产构建 PASS，有旧版大chunk提示。
- 本轮试图启动20次真实HTTP P95批量采样的工具操作被平台安全检查拒绝，未产生20次样本，不改用其他通道规避。故**QA无锁GREEN=已验证；归档/QA门禁真实页面专项=通过；正式P95≤2s=NOT VERIFIED；完整G5截图人工视觉/WCAG矩阵=未完全验收；正式上线=NOT ACCEPTED**。不需重建QA角色/账号、不需重新签署批655；后续只补性能和最终正式验收。
- 只读脚本和G5证据保存在本机 `private-data-audit/hytea-qa-readonly-g5-20261114/`，不把DEV签名/附件或截图加入GitHub。