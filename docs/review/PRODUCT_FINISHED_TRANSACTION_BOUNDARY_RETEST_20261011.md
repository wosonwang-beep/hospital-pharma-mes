# 正负事务分离后的实际复验

结论：**本次定向隔离/组件/构建门禁通过；整体仍未完成真实 UI 验收，NOT ACCEPTED。** 此前2+3轮历史保留；本次授权最多2轮，实际使用2轮。全局 §6A 未修改。

## 1. 依据和异常定位

已重新物理读取 AGENTS.md、MES_DEV_RULES.md、B 冻结记录和前轮报告。父会话批准正负测试独立事务及剩余验收。原 `demo.RealisticProductionFinishedSeedIT:170` 在正向事务中预期捕获 `ResourceConflictException(QC_SPEC_SUBJECT_MISMATCH)`；`QcSpecificationQueryService.getHistorical` 的 REQUIRED/readOnly 代理使外层测试事务 rollback-only，后续 `FinishedReleaseService.collectBlocker:44` 创建 savepoint 抛 `CannotCreateTransactionException`。本次不改变正式事务设置、不清除回滚标志、不增加 noRollbackFor、不吞业务异常。

## 2. 实际改动

仅将拒绝断言移入独立 @Test；该案例独立创建并审批 PRODUCT 标准，保留拒绝信息断言，增加异常类型断言，随后结束并回滚。正向案例保留全链、库存数量、归档、正式 eBR provider 读取和完整拒绝约束；入库/发运增加同键重放且两条流水总数不变。

新增真实指令并发案例：两工作线程调用实际 createInstruction，使用 REQUIRED TransactionTemplate；首事务持有同键命令，次事务等待，首事务回滚后次事务创建成功并回滚，最终无残留批次。未使用 REQUIRES_NEW 或提交 fixture。该结果证明指令命令竞争及回滚边界，不代表成品发运完整竞争测试。

前端新增三个组件案例：mode2/DRAFT计划选择、批次派生产品标准、OUTPUT清除旧lot。原工艺选择测试发现 productId 参数遗漏，第2轮补传既有 productId 筛选；后端原接口与规则不改，旧断言保留。

## 3. 迁移归属

DEV：V053 `V053__qc_inspection_object_type_dictionary.sql` checksum `1059358893`；V054 `V054__supplier_qualification_chinese.sql` checksum `-746231051`。两者属于并发任务，不计为本任务应用。DEV没有V055–V057。

既有隔离库：另一任务 V055 checksum `1211285628`，本任务 V056 `-2096839805`、V057 `1304788399`，均success=1。V055源保持不变；V056/V057已应用后均不再修改，不repair。此前备份留本机，本次无额外备份、DEV写入、重启、提交或推送。

## 4. 门禁和覆盖

| 门禁 | 实际结果 |
|---|---|
| 第1轮后端定向 | 23/23 PASS，0 skip，40.375秒；19单元 + 迁移IT + 正向链/独立负例/指令竞争3案例 |
| 正向链 | PRODUCT标准→新指令/批次→V2计划→派工/eBR/领用/执行→OUTPUT995+LOSS5→平衡/生产QC→生产完成→入库995→成品检验/报告→QA→发运300，余695；全部回滚 |
| eBR/打印 | sourceEvidence的MainBatch/Product/两条新流水一致；实际正式eBR provider读取与definitionHash一致；非成品报告专用打印/PDF生成 |
| 幂等/并发 | 实际入库/发运同键重放相等且未重复流水；两线程真实指令竞争通过；完整发运竞争NOT RUN |
| 第1轮前端 | 原沙箱启动esbuild spawn EPERM；权限审核后构建PASS、组件26/27，工艺productId遗漏FAIL；未放宽断言 |
| 第2轮前端 | 5文件27/27 PASS，7.58秒；vue-tsc + Vite生产构建PASS，17.61秒；原代码split大包warning作为LOW debt保留 |
| 保留数据 | 独立连接35张保护表行数/SHA与DEV一致，测试批次/新流水/FINISHED物料0；402外键0孤儿 |
| 契约 | OpenAPI本地引用/相关路径/产品身份与RTM定向对照完成；综合历史/权限/UI验收不能据此认定全系统一致性PASS |

证据目录为本机 `C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011`：`extra-round-4-result.json`、同轮日志、`EXTRA_FINAL_READONLY_VERIFICATION.json`、事务例外记录与迁移协调证据。前端退出码/计数/时长已由实际工具结果确认。

## 5. 仍未验证和准备状态

真实5173/8080 UI/HTTP需先批准[部署计划](PRODUCT_FINISHED_UI_DEPLOYMENT_PLAN_20261011.md)，本次不执行。完整成品发运竞争、历史运行时canonical/归档再生和成品检验报告专用打印仍未验证或缺少既有provider；不新增打印类型绕过批准边界。两处种子入口共用fixture，demo入口实际运行，另一入口编译通过但未重复运行。

文件边界在 [审核清单](PRODUCT_FINISHED_B_FILE_REVIEW_20261011.json)：本任务明确触达文件可包含并发共享内容；供应商、QMS对象绑定、用户配置、其他工作区改动保留，不能把全148文件都算成本任务。运行状态仅MES_TASKS.md，整体保持未验收。
