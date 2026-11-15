# 来料质量跨岗位闭环 — G3 详细设计候选与 Codex 无损集成合同

**任务：** INCOMING-QMS-CROSSROLE-002；**适用源码：** 已确认 `docs/development/decisions/INCOMING_CROSSROLE_G2_20261019_APPROVED.md` 的九文件 SHA256 版本。
**业务来源：** 用户确认收货逐批一键请验、QC补标准并同时生成取样单+待样品检验任务、检验人员实取、仓管确认出库扣库存、样品接收后逐项检验/独立签名与报告打印。
**当前状态：G3 TECHNICAL DRAFT / DESIGN CONTRACT REVIEW OPEN；非G3冻结、非G3.5 READY。** 基线 FINAL v1.0.23 + 批准 G2 局部行为。不重设计收货页/正式检验列表，不更改已执行的 Flyway。

## 0. 产品经理验收口径

检验人员的主要任务是先找对样品、确定采样计划与实际样品、填写每个冻结项目的真实原始结果并得到审核；仓库岗位只负责已确认收货的请验发起和样品实物出库核对。**所有变更必须完整保留来源身份、审核职责分离和库存/QA门禁。** 检验任务提前生成只是计划身份；不能把无样品的任务视作可检验、已签名或可形成报告。

| 业务对象 | 现有来源（不改名） | 重要只读字段 | 重要可编辑与责任 |
|---|---|---|---|
| 收货物料明细 | `wms_material_receipt_item`、`md_material_lot.receipt_item_id` | 物料/批次/生产企业/生产批号/实收量及单位/容器/库位/是否必验/库存状态 | 仓管仅「生成请验单」，成功变查看，不能录QC标准 |
| 请验单 | `qms_inspection_request`、`qms_inspection_request_item` | 已保存收货与批次、首次请验来源、生成者/时间 | QC确认时补批准标准版本及计划采样量/单位/件数/方案 |
| 取样任务 | `qms_sampling_task` + `qms_sampling_detail` | 批次、批准QC计划、取样人、来源库位/容器、实取量/单位、样品分配、复封 | 取样员保存/提交；仓管仅核对/确认实际出库 |
| 样品 | `qms_sample` | 取样明细主键、样品号、样品类型、接收与储存事实 | QC样品接收，不代替仓库扣库确认 |
| 检验任务与项目 | `qms_inspection_task`、`qms_inspection_item` | 样品名称与编号、内部批号、收货批量、计划/实际取样量、日期、检验单类型、冻结标准 | 样品 RECEIVED 以后才允许派检、原始数据/结果；独立复核签名 |
| 检验报告 | `qms_inspection_report`、`qms_inspection_report_item` | 受控报告号、全部项目及有效结果和原始审计引用、综合判定、审核/批准 | 由既有生成、复核、批准命令处理；已批准模板打印，不手抄结果 |

## 1. 已核对的 AS-IS 缺口（禁止让 Codex 自行猜业务）

| ID | 当前正式代码/DB事实 | G3 要求及兼容方案 |
|---|---|---|
| CR-01 | `qms_inspection_request.qc_specification_version_id` 与 `specification_content_hash` NOT NULL；`IncomingQualityService.createRequest` 在新增就校验/冻结标准 | 新的**收货一键首次请验**提交时由后端从已确认收货原始数据推导量与单位，QC标准和哈希允许在 DRAFT/SUBMITTED 临时为空；QC确认时必填、冻结并再不可普通编辑。旧请求历史保持不变。需要新增且有审核日志的命令，不能改旧 createRequest 的标准语义。 |
| CR-02 | 旧 `requestAction(...accept)` 仅改为 ACCEPTED；`createSampling` 独立创建，检验任务独立创建 | 新 QC confirm 命令**在一个事务**完成QC标准冻结、批准计划采样量/件数和方案、创建恰好一张取样任务及一张待样品检验任务、一次接受请验；失败整事务回滚。使用同一组织、签名、版本、幂等键，禁止重复。 |
| CR-03 | `qms_inspection_task.sample_id` NOT NULL，`createTask` 要求 RECEIVED 样品并建冻结检验项目 | 仅对新 QC确认流程允许 `sample_id=NULL` 且 `status=CREATED`，页面显示「待样品」；检验项目须从**当次 QC 冻结标准**按必须项及受控选项复制。样品实际 RECEIVED 后，增受控绑定命令，绑定后才允许分派/开始检验；不造假样品记录。 |
| CR-04 | 原 SamplingTask complete 由取样员签名、立即 COMPLETED/样品 COLLECTED/批次 SAMPLED | 新链增加「取样提交待仓库确认」有界状态或事件：取样员提交后封存实取明细（不会扣库），仓管签名核对后才执行 WMS 负库存流水与 SamplingTask 完成、样品 COLLECTED、批次 SAMPLED，事务一同提交/回滚。旧完成历史及老接口行为不回写。 |
| CR-05 | 既有 WMS `wms_inventory_ledger` 记录 lot/location/container/unit/delta，普通 ISSUE/ADJUST 权限和 RELEASED 门禁不同 | **独立受控内部 SAMPLE_OUT 事件**，只能关联指定取样任务+逐行明细及待验批次，锁定来源分库位/容器库存，按照批准换算得到与库存余额一致的基本单位和精确数量。质量仍 BLOCKED，负库存与跨组织、非获批单位/容器、重复确认阻断；不复用普通发料或库存调整，不新增无关联的出库单。 |
| CR-06 | `qms_sample` 在取样明细录入时 CREATED、旧 complete 变 COLLECTED，receive 变 RECEIVED | 新取样提交后样品仍 CREATED，仓管确认真实出库后 COLLECTED，检验室有权独立接收为 RECEIVED，才能绑定待样品检验任务并开始填写。 |
| CR-07 | `InspectionTaskListView` 已单独精确验收，正式 T1 已有样品名/批号优先、检验单号对应报告号、状态及权限 | 只在获批范围增加待样品任务的受控列表投影和只读禁止写表现，保留原正式源码/权限；不重新造同类列表。 |
| CR-08 | 项目 `result_conclusion` 是 PASS/FAIL/INCONCLUSIVE/INVALID，报告还有独立审核批准；让步接收有独立QA处置 | 本轮原始结果和报告批准不因用户确认样品流程而发生松绑。让步接收需要合法适用/偏差/OOS/QA签名决定及独立审批，不把FAIL改PASS；本轮仅复用既有方案，不擅自扩权。 |

## 2. API / DTO 按用户岗位冻结前待核查接口表

| 事件 / 角色 | 新命令建议（候选）及入参来源 | 状态/输出/幂等 | 错误恢复 |
|---|---|---|---|
| 收货行一键请验 / 仓管 | `POST /api/v1/wms/receipts/{receiptId}/items/{itemId}/inspection-request`，传 **Idempotency-Key**；服务端从确认收货及 material_lot 校验组织、必验和来源，客户端不得上传“检验标准/数量/供应商”等可伪造事实 | request `SUBMITTED`、返回 request id/no；唯一的首次请验身份为同组织+物料批次+INITIAL。原收货行 GET 只读加入 request id/no 和 allowedActions，前端生成后变「查看」 | 401/403/404/409/422；幂等重试返回原请验，不覆盖源批次，不提交QC标准 |
| QC选择标准及确认两单 / QC | `POST /api/v1/quality/inspection-requests/{id}/confirm-and-generate`；If-Match + Idempotency-Key；`qcSpecificationVersionId`、`samplingPlan`、`plannedSamplingQuantity`、`plannedSamplingUnitId`、`requiredPackageCount`，QC必须有资格 | 已确认请求、sampling task `PLANNED`、inspection task `CREATED,sampleId=NULL`；返回两个真实 ID/No；缺任一条件全部回滚 | 数量/单位/版本/标准失效 -> 422；409并发不重生；403资格/组织错误；原始输入保留 |
| 取样员提交实取记录 / 检验员 | `POST /api/v1/quality/sampling-tasks/{id}/submit-to-warehouse`；If-Match/Idempotency-Key；已存在的取样明细有容器/库位/实取量/单位/样品分配与复封 | `IN_PROGRESS→PENDING_WAREHOUSE`；明细/样品与取样提交不可回写；**库存不变** | 少容器、分配不守恒、超批准计划及缺签名拒绝；保留可恢复输入 |
| 仓管独立确认取样出库 / 仓库 | `POST /api/v1/quality/sampling-tasks/{id}/confirm-sample-outbound`，授权复核+If-Match/Idempotency-Key+仓管签名/原因；内部调用WMS SAMPLE_OUT而非通用领料 | 同一 MariaDB 本地事务插入真实负向库存流水（逐取样明细唯一事件），SamplingTask COMPLETED，样品 COLLECTED、批次 SAMPLED；返回流水IDs、来源及余额 | 无权限/人员不独立/库存不足/跨组织/重复/并发/单位不支持均阻断或幂等结果；数据库签名审计失败整体回滚 |
| 样品接收与绑定 / QC检验室 | 复用 `POST /api/v1/quality/samples/{id}/receive` 后，候选 `POST /api/v1/quality/inspection-tasks/{id}/bind-sample`；验证属同一请验、TEST_SAMPLE、RECEIVED | 任务仍 CREATED 但样品ID已绑定，允许已有 assign/start 受控状态机 | 绑定不符、留样、未收样或重复绑定拒绝 |
| 检验项目填写、综合结论及报告 / 检验员与独立复核 | 直接复用现有 TestExecution/ResultRevision、submit-review、review、report generate/review/approve、统一打印，不新建“第二套报告编辑” | 每项目有效结果留痕，PASS/FAIL与OOS由原始结果规则决定，报告批准不自动 QA 放行 | 原始结果/签名/报告状态锁继续有效；完整新G2页面未验收前只做接口差异审查，不自行扩功能 |

## 3. 数据库变更设计（**待数据校验后才能执行追加迁移**）

现存物理表均属于已执行 V016/V030/V065/V066 系列，绝不修改旧文件。**只拟新增 Flyway V067（实际版本需现场查Flyway history后确定）**：
- `qms_inspection_request` 将 `qc_specification_version_id`、`specification_content_hash` 修改为允许 NULL（仅 pending QC 请求合法）；增加**服务器规则 CHECK**：进入 ACCEPTED/IN_PROGRESS/COMPLETED 时两者均不得空；历史版本和索引保持。新首次请求防重约束以组织+materialLotId+INITIAL语义为准；先查历史有无重复 INITIAL，若无再上可空生成列的唯一索引，否则必须停下审查数据，不能删历史。
- `qms_inspection_task.sample_id` 改为 NULL；CHECK: `sample_id IS NOT NULL OR status='CREATED'`，已有历史全部保持。检测业务层 `number(task,'sampleId')` 调用点，在 start/execute/review 前验证样品已绑定，NULL不能触发NPE或越权。既有 CREATED 的合法样品引用不变。
- `qms_sampling_task` 增 `planned_sampling_quantity DECIMAL(24,8)?`、`planned_unit_id BIGINT?`、`submitted_by/at`、`warehouse_confirmed_by/at`、`warehouse_signature_id/evidence_json` 等审计字段；新链必须填计划；旧历史保持NULL。新状态 `PENDING_WAREHOUSE` 是中间状态，**不属于现有completed**。
- `qms_sampling_detail` 增 `source_location_id/source_container_id` 与 `sample_outbound_ledger_id`（全部历史可空，新来源严格校验）；每个明细最多关联一条 SAMPLE_OUT 负向 WMS 流水，(org_id,sample_outbound_ledger_id) UNIQUE且缺失不重复假填。
- `wms_inventory_ledger` 保留原有不可变流水与幂等列，按 org/lot/location/container/unit 锁定和聚合；若现有事件字典/数据库 CHECK 限制 SAMPLE_OUT，追加对应受控选项（须核实再实施），不重建另一份出库流水表。
- 所有数量通过 JSON **decimal string** → Java BigDecimal → WMS批准单位换算→固定精度（如目标Ledger实际精度为6，则只能精确无损表示，不能四舍五入）；超精度阻断。原型 `Number*1000` 与硬编码「千克」不能照搬。来源库位和容器要跟 receipt 的实际库存流水核实，没有足额已确认待验实物就不能扣减。
- 不能关FK、修改历史签名/报告或自动替用户审批。回滚只能在隔离事务中，不给已经确认的样品出库做 DELETE。

## 4. 状态机与签名冻结前风险分级

| 路径 | 操作主体 | 转移 | 品控及事务要求 |
|---|---|---|---|
| 已确认收货（每条必验明细）→提交请验 | 仓管 | 无→SUBMITTED | 必须为真实源批次、初次请验防重复；保留原收货质量 BLOCKED |
| SUBMITTED→ACCEPTED + 两张计划单据 | QC合资格人员 | QC Confirm | 标准版本确系该物料批准且合法；计划量不超源批数量、单位换算精确、包件数有效；全事务原子 |
| 取样开始→提交仓管 | QC取样员 | ASSIGNED→IN_PROGRESS→PENDING_WAREHOUSE | 按明细及包装签名，实际量/检验样/留样之和守恒，**无库存流水** |
| 仓管确认→取样完成 | WMS仓管 | PENDING_WAREHOUSE→COMPLETED | 每行锁、负库存阻断、逐明细幂等、签名与审计原子、质量状态SAMPLED且仍 BLOCKED |
| 样品接收→待样品任务可执行 | 有权QC样品接收人/检验员 | CREATED未绑定→RECEIVED已绑定→ASSIGNED/IN_PROGRESS | 检验任务无样品不能开始，无证据不能复核 |
| 原始结果/审核/报告 | 有权检验、独立复核与批准 | 复用既存命令 | 项目原始FAIL/OOS与QA释放严格独立，未批准的“让步接收”不生成PASS |

**高风险阻断：** 既有模型将新检验任务的 sample_id 与任务创建强绑定、既有签名发生在取样 complete；迁移与签名必须经受控检验链状态验证后才可执行。合规权限应为真实WMS仓管账号具备相应受控操作权、QC检验员不能获得该权；前端角色下拉仅是隔离展示。

## 5. 九项开发RTM和G5验收判据

| RTM | 正向证据 | 必需负向证据 |
|---|---|---|
| RTM-01 收货一键首次请验 | 已确认真实物料批次API返回SUBMITTED真实请验号，行变查看 | 免验、未确认、二次点击、并发双请求、跨组织、失败时原批次不变 |
| RTM-02 QC确认标准并生成双单 | 新标准版本冻结真实Hash，取样单和待样品检验任务同事务真实持久化 | 缺标准/失效、错误计划量/单位/包件、二次确认、部分写入回滚 |
| RTM-03 待样品检验任务 | real DB有 CREATED task/sampleId NULL、冻结规格项目、列表业务名称/物料批号 | 不能派发为可检验、不能启动/填检验结果；错误样品关联拒绝 |
| RTM-04 取样员实取与交接 | 检验员按容器记录样品分配/复封/实取量，提交PENDING_WAREHOUSE，库存不变 | 少件、超计划、未复封、错误单位、非被指派操作、重复交接 |
| RTM-05 仓管确认实物扣库 | 唯一逐明细SAMPLE_OUT负向流水、来源批库位容器与单位、真实余额及签名 | 无授权、假签名、库存不足、重复与并发、错误来源/容器、审计失败整事务回滚 |
| RTM-06 样品接收→检验 | RECEIVE后受控绑定正确样品/冻结标准项目，状态允许填写 | 未收到样品、留样不可测、别人的样品和跨组织拒绝 |
| RTM-07 检验项目与审核 | 原始结果和综合判定由真实规则、独立签名复核 | 缺项/FAIL/OOS绕过、改写历史原始值、同人签名拒绝 |
| RTM-08 报告查询及打印 | 完整审核结果进入既有报告列表/详情，获批报告统一模板打印 | 无结果、不合法结论、未经批准却输出正式报告、历史版本被覆盖 |
| RTM-09 用户体验 | 1440/1280/900/375同源截图与完整MES菜单/角色入口，复用原批准正式列表和收货UI | 主操作隐藏、显示误导/裁切、焦点/错误恢复、只读被展示可写 |

## 6. 正式代码接入顺序与冻结前阻断

1. G3本文件须与 `DCP-INCOMING-CROSSROLE-FLOW-002-PROPOSED.md` 逐项对齐、核查现有实体/Schema/角色权限、MariaDB历史和约束；缺失任何字段或迁移可用性则标 `G3 BLOCKED` 而不猜。
2. G3.5从**9个已经确认的 mes-demo 源哈希、同版本设计稿及本详设**无损整理交接包；Codex只复用源组件对接正式API和后端。既有正式检验记录列表只读投影不重做，既有收货其余模块不变。
3. G4按“只追加V067前置检查→后端真实角色签名/DB事务单测→接口与前端增量→真实浏览器联调”顺序。不能因原型sessionStorage通过而运行真实扣库。
4. G5必须同时真实 `5173/8080/MariaDB`，包含负向权限/并发/精确余额/旧数据兼容、UI UX完整证据，并有用户最终验收。**未全部执行不写 AC`CEPTED`。**

**本文件仅详细设计候选，不代表已执行新的数据库迁移或正式写入，亦不授权把未G2确认的报告页面算作已冻结的UI。**

## G3真实DB只读预检更新 — 2026-10-19

已新增并执行 `backend/mes-boot/src/test/java/com/hospital/mes/incoming/IncomingCrossrolePreflightIT.java`，Maven指定测试 **3/3 PASS，退出0**；Flyway validate 66/current066。真实MariaDB检测：**INITIAL每组织+物料批历史重复数为0**，历史无样品且非法状态任务数为0；真实源字段8/8存在，现有两项请求QC标准字段、检验任务sample_id均是NOT NULL；WMS ledger delta_qty为DECIMAL六位精度，container_id可空。上述为正式迁移的只读前提证据，**未执行任何DDL/数据修改**。证据路径 `docs/review/evidence/incoming-crossrole-G3-physical-preflight-20261019.log`。

仍需完成真实签名/权限与WMS逐批/库位/容器事务合同审查，评估新中间状态对旧接口/质量状态机/索引及审计的影响；通过后才能将拟新增迁移复制到下一个真实Flyway版本并运行回滚集成验证。

## G3 专用仓管权限/双单唯一性/跨模块签名审查（2026-10-19）

已完成[正式权限与仓库事务核对](INCOMING_CROSSROLE_002_G3_SECURITY_TRANSACTION_REVIEW.md)：真正的 `wms:sampling-outbound:confirm` 权限不存在，QMS旧取样完成签名不能替代仓管签名，已有WMS `uk_ledger` 幂等包含操作者不能单独防不同仓管重复扣库；G3候选采用两层权限加仓管资格、独立签名Slot、同一事务锁采样单和来源库存明细并在目标六位小数无损换算，禁止普通生产领料/库存调整代替。QC首次自动生成的两单须各增加可空 `qc_auto_generated_request_id` 唯一约束，不锁死同请验合法追加/复检任务。未完成正式动态签名/权限配置及服务端实现前保持 G3 OPEN。
