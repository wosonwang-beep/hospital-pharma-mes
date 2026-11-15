# HYTEA DEV 生产批655后续成品链预检（2026-11-12）

状态：READ-ONLY PREFLIGHT / PARTIAL / BLOCKED，非最终验收。环境hospital_pharma_mes_dev，唯一main。依据：AGENTS.md、MES_DEV_RULES.md、PROJECT_BASELINE v1.0.23、MES_TASKS.md及已批准的DEV全流程和IPC权限方案。

## 1. 当前实际状态（正式API只读读回）
- 批655 IN_PROGRESS；执行单628仍IN_PROGRESS。
- OP01–OP05 COMPLETED，eBR822–826 VERIFIED；OP06/784 IN_PROGRESS、eBR827 VERIFIED；OP07/785与OP08/786仍PENDING，eBR828/829 DRAFT。
- IPC13/FILL_VOLUME为OP06冻结必检项，NUMERIC 95–105ml，当前PENDING、results=0。QC分析角色289及QC复核角色290的四项IPC权限已获用户批准并通过正式IAM API新增（289 23→25，290 16→18），两账号11567/11568重新登录验证。其后的IPC结果及独立复核写操作被OpenAI安全检查拦截，未到达本机、未创建结果；不以其它通道/SQL/后台任务规避。
- 包装物料14/16批1354/1355仅为明确标识的合成DEV供货数据，已完成收货、取样、QC独立复核、报告批准和QA物料放行，均RELEASED/AVAILABLE；不代表真实采购、真实供货资质或真实实验室检测。

## 2. 质量计划及QC标准
- 质量计划666：APPROVED，QA签名5271；schema V2，productId2，inspectionObjectType FINISHED_PRODUCT，inspectionObjectId655；冻结输入物料平衡规则：ISSUE vs CHARGE，单位kg，容差±0.5%，BATCH_COMPLETE。
- 成品QC标准1419 QC-HYTEA100-REG-20261109：ACTIVE，产品HYTEA100/100ml瓶，8个必检项目：性状、大肠埃希菌、装量、鉴别、pH、相对密度、需氧菌总数、霉菌和酵母菌总数。来源纸质报告file/2-6及装量记录file/2-4仅作历史业务映射参考，不可冒充2026批实测；标准effectiveDate为空、现行注册标准原文未核，本流程只作DEV软件闭环。
- 当前655 finished inspection requests=0；finished inbound requests=0；finished reports中已有1条但属于旧批325/KCL产品1，不可复用；655 eBR archive pdfManifests=[]。

## 3. 已存在原料投料及物料平衡输入
- WMS material issue 524，CONFIRMED，六项处方物料：1121/96kg、1123/48kg、1124/48kg、1122/16kg、1125/30kg、1126/1.4kg。
- 已读回6条ISSUE数量事件（344–349）与6条CHARGE（340–343、350–351），按冻结单位合计各239.4kg，数量相等。当前MaterialBalance仍results=[]、BALANCE_RESULT_MISSING。须待全部生产数量事件形成后再按正式服务重算，不能提前生成过期digest。

## 4. 成品输出、入库、QC/QA与归档的正式顺序
1. 先完成IPC13的PASS原始签名及另一岗位CONFIRMED独立复核；随后完成OP06，按工序顺序执行OP07、OP08。
2. 在批655仍IN_PROGRESS时，通过`POST /main-batches/655/quantity-events`记录实际产品输出；Product HYTEA100 baseUnitId=3(ml)，生产输出地点7“成品待检区”有效。OP07冻结数据为实际4000瓶、留样20瓶、待验入库3980瓶；按100ml/瓶换算，待验入库量候选398000ml，留样量候选2000ml。必须在操作时与OP07实际记录再次核对后再签名；不直接照搬猜测。
3. 产品输出API同时要求实际生产日期、有效期、活动成品待检区，并将首次输出的日期/单位/产品快照冻结到批次。产品主数据仅含名称/规格/单位，未给出货架期；有效期必须从产品标签/受控资料核实，不得擅自假定6个月。现有文件2-1至2-6已获用户批准作为DEV业务资料，但其有效期需检查原件后才能输入。
4. 全部8工序完成、CPP/eBR/IPC审核通过后，按正式ProductionService完成生产。随后系统强制成品输出、确认成品入库、成品QC报告批准和质量计划有效。
5. 入库链：现有角色权限下系统管理员具有finished-inbound:create/submit，仓库账号11565具有confirm/view。可由admin创建/提交入库单，仓库11565独立确认至位置7；不得用同一身份创建并确认。
6. 成品请验：系统管理员有finished-request:create/submit，QC分析员11567及QA11569有accept；由admin建单/提交，QC分析员或QA按职责受理。取样由已有资格的sampler11566，QC分析员11567接收样品并按1419八项目建立测试/签署，QC复核员11568逐项独立确认并批准报告，QA11569执行最终批次放行。
7. QA放行后批次进入QA_RELEASED，成品库存状态变为RELEASED/AVAILABLE。最后由具备ebr:pdf:generate的系统管理员生成FINAL eBR PDF；该档案必须关联有效QA RELEASED决定及当前记录摘要。当前不应生成FINAL归档。

## 5. 当前QA审核模型已读回的阻断码
`FINISHED_RELEASE_STATE_CONFLICT`、`BATCH_EXECUTION_INCOMPLETE`、`EBR_FORM_NOT_SUBMITTED`、`EBR_REVIEW_REQUIRED:293`、`EBR_SIGNATURE_REQUIRED:VERIFY:OP07_RECORD`、`EBR_RULE_BLOCKED`、`EBR_REVIEW_REQUIRED:294`、`EBR_SIGNATURE_REQUIRED:VERIFY:OP08_RECORD`、`PRODUCTION_QC_REQUIRED_RESULT`、`BALANCE_RESULT_MISSING`、`FINISHED_OUTPUT_REQUIRED`、`FINISHED_RECEIPT_REQUIRED`、`FINISHED_INSPECTION_REQUEST_REQUIRED`。上述均为当前状态对应的真实门禁，未绕过。

## 6. 权限及前置核对
- 289：已拥有qms:sample:receive、qms:test:execute/record、qms:finished-request:accept、qms:finished-report:generate及新IPC view/record。
- 290：已拥有qms:test:review、qms:finished-report:approve及新IPC view/review。
- 288：qms:finished-sampling:create；账号11566具GMP_OPERATION_AUTHORIZATION。
- 291：qa:release、qa:batch-review，账号11569具GMP_OPERATION_AUTHORIZATION。
- 286：wms:finished-inbound:confirm/view，账号11565具GMP_OPERATION_AUTHORIZATION及WAREHOUSE_SAMPLE_OUT。
- 目前数量事件mes:quantity:record仅SYSTEM_ADMIN角色1持有，生产角色287无此权限；如要求输出由生产岗位11572而非admin本人签署，需要另行批准角色287仅增加此现有权限。尚未提出/执行该授权。DEV流程若由admin签署输出，签名主体将明确为admin，不能假称生产操作员。
- 成品入库create/submit及成品请验create/submit也只在SYSTEM_ADMIN权限集中；其余分岗确认/检验/复核/QA动作均已有相应角色权限。可在保留身份分离的前提下由admin执行建单/提交，仓库/QC/QA分别完成各自操作，不需扩展权限。

## 7. 本轮证据
`private-data-audit/hytea-resume-20261111/current-655-readonly-20261112.json`、`finished-chain-preflight-20261112.json`、`production-gates-readonly-20261112.json`、`balance-inputs-readonly-20261112.json`、`balance-detail-readonly-20261112.json`、`ipc-role-grants-before.json`、`ipc-role-grants-after.json`。本轮无业务写入，无数据库迁移，无应用源码改动，无Git提交/推送或worktree。结论PARTIAL/BLOCKED/NOT ACCEPTED。

## 2026-11-12 OP06–OP08已通过DEV执行验证（IPC13关闭）
- 用户明确要求先完成IPC13，再继续OP06～OP08。通过正常岗位API完成既存IPC13：QC分析员11567录入100.00000000ml（冻结范围95–105ml），QC复核员11568独立CONFIRMED；IPC实例13 COMPLETED/PASS，结果修订14，签名5317/5318。该数值明确为合成DEV软件流程输入，不是现场实物测量或真实质量证据。
- 使用现有run_operations.py从现场续跑：OP06/784 COMPLETED、OP07/785 COMPLETED、OP08/786 COMPLETED。eBR827/828/829均VERIFIED，各1个生产签名、1个独立复核。8道工序779–786全部COMPLETED，eBR822–829全部VERIFIED。
- OP06：药瓶批号HYT-HDPE100-20261111-01，领4050、使用4000、损耗20、余30；装量检查范围99.5–100.5ml，20瓶抽查叙述；CPP_TEA_FILL在ON_CHANGE/ON_SAVE/ON_SUBMIT均PASS。上述均为合成DEV流程输入。
- OP07：标签批号HYT-LABEL100-20261111-01，领4050、用4000、损耗20、退30；包装4000瓶、留样20、待验入库3980、成品率/物料平衡100。OP08物料已清点、设备/区域清洁及处置字段均有记录。eBR和工序清场复核均通过。
- 生产批655仍IN_PROGRESS；尚未记录成品OUTPUT数量、未完成生产完成动作、物料平衡结果仍待重算、成品入库/请验/QC/QA/FINAL归档均未开始。前述OP06–08数据只证明DEV软件工作流，不证明真实现场执行。
- 只读复核证据private-data-audit/hytea-resume-20261111/op06-op08-verified-20261112.json，SHA256 174A194B4450364304212693BB0DE26223609C492FF1221DA961832EB5739FCF；IPC证据ipc13-closeout-20261112.json；单工序完成证据op06/07/08-complete.json。

## 2026-11-12 成品输出有效期门禁与物料平衡
用户要求继续完成成品闭环。正式API物料平衡计算405已PASS：ISSUE/CHARGE各239.4kg，差异0%，限度±0.5%。OP07产4000×100ml=400000ml，其中留样2000ml、待验入库398000ml；计划输出数量事件分别OUTPUT398000ml/SAMPLE2000ml，入库接口将以OUTPUT事实生成批次来源快照。产品输出接口强制制造日期和有效期，且首次输出冻结这两个属性。产品主数据无货架期；file/2-5仅为历史批号20231215、分装日期2023-12-22、标签打印有效期20240614；file/2-6是2023 QC报告，不提供产品有效期。官方目录仅确认粤药制字Z20070632及规格，没有有效期。未擅自猜测或写入数量事件。待确认本批产品有效期至日期或明确DEV-only货架期规则。新增输出/样品后需重新计算余额405的最新输入摘要。当前批655仍IN_PROGRESS，无成品入库/请验/QC/QA/FINAL PDF。细节见MES_TASKS.md及本文件。

## 2026-11-12 成品数量权限路由修复及运行时阻断
用户已批准六个月DEV假设：制造日期2026-11-12、有效期2027-05-12；OP07产400000ml，待验入库398000ml、留样2000ml。物料平衡405 PASS（239.4kg=239.4kg）。尝试记录OUTPUT/SAMPLE时403，未写入；只读确认批655仍IN_PROGRESS/version2、无输出事件。根因SecurityConfiguration遗漏现有quantity-events API的RequestMatcher；已仅添加GET→balance:view、POST→mes:quantity:record、reverse→mes:quantity:reverse，未增权限。19模块编译成功，但repackage被运行中8080 PID31124锁定jar；8080健康UP。当前AIC无elevated helper，Stop-Process被Access Denied，未绕过。需本机管理员停止/重启MES API后继续。详细见HYTEA_OUTPUT_SECURITY_ROUTE_FIX_20261112.md。

## 2026-11-12 V077恢复后成品链最新状态
- V077已按用户一次性DEV批准修复并repair一次，正常Flyway启动重跑成功；八份原签名岗位策略迁移完成，eBR_REVIEW gate PASS。详见`HYTEA_V077_FAILED_MIGRATION_RECOVERY_20261112.md`。
- 最新物料平衡406 PASS，ISSUE=CHARGE=239.4kg，差异0%；生产批655已由生产账号11572完成，状态PRODUCTION_COMPLETED/version5。
- 管理员11556创建并提交成品待检入库单126（FG-IN-HYTEA-655-20261112），398000ml、产品2、批655，SUBMITTED。仓库11565只读核实allowedActions包含CONFIRM。确认写操作被OpenAI安全检查拦截，未执行；不通过其他工具/SQL/后台任务绕过。尚无成品收货库存流水。
- 当前QA blockers：FINISHED_RELEASE_STATE_CONFLICT（批次尚未PENDING_QA）、PRODUCTION_QC_REQUIRED_RESULT、FINISHED_OUTPUT_REQUIRED（待确认收货）、FINISHED_RECEIPT_REQUIRED、FINISHED_INSPECTION_REQUEST_REQUIRED；EBR_REVIEW与QUALITY_PLAN、MATERIAL_BALANCE均PASS。成品QC标准1419为ACTIVE/8项。待仓库确认后才可创建finished inspection request、取样、8项测试、独立复核、报告批准、QA提交/放行及FINAL PDF。
- 批655仍非最终放行，finishedQualityStatus=QUARANTINE、finishedInventoryStatus=BLOCKED、finishedLotId=null。所有业务数据仅合成DEV软件联调，不代表真实现场生产/检验。
