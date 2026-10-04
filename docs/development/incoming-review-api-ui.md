# 来料质量 Baseline Review：API / UI / Permission（E/F/G）

2026-10-03；仅只读审查。v1.0.9 + 已批准且已发布DCP为设计权威；008A A/B六新route及workbench映射已获授权但尚未发布，绝不重复申请。mes008a-contract候选不是权威。未改产品代码/基线，未执行测试。

## E. Incoming主链所选API操作与权限（selected）

所有路径前缀 /api/v1。下表MATCH仅表示当前源代码存在与method/path/权限吻合，非运行验证。MISSING是相对于完整008A交付；符合既有分阶段授权，不等于越权实现。

|Method|Path|Permission|Request schema|Success schema|当前实现分类|
|---|---|---|---|---|---|
|GET|/wms/receipts|wms:receipt:view||ApiReceiptPage|MATCH|
|POST|/wms/receipts|wms:receipt:create|ReceiptCreate|ApiReceipt|MATCH|
|GET|/wms/receipts/{id}|wms:receipt:view||ApiReceipt|MATCH|
|PUT|/wms/receipts/{id}|wms:receipt:update|ReceiptUpdate|ApiReceipt|MATCH|
|POST|/wms/receipts/{id}/confirm|wms:receipt:confirm|Command|ApiReceipt|MATCH|
|GET|/wms/material-lots|wms:inventory:view||ApiMaterialLotPage|MATCH|
|GET|/wms/material-lots/{id}|wms:inventory:view||ApiMaterialLot|MATCH|
|GET|/wms/material-lots/{id}/timeline|wms:inventory:view||IncomingQualityRecordApiResponse|MISSING|
|GET|/wms/material-lots/{id}/eligibility|wms:inventory:view||MaterialEligibilityApiResponse|MISSING|
|GET|/quality/inspection-requests|qms:inspection-request:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-requests|qms:inspection-request:create|InspectionRequestCommand|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/inspection-requests/{id}|qms:inspection-request:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-requests/{id}/submit|qms:inspection-request:submit|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-requests/{id}/accept|qms:inspection-request:accept|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/sampling-tasks|qms:sampling:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/sampling-tasks|qms:sampling:create|SamplingTaskCommand|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/sampling-tasks/{id}|qms:sampling:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/sampling-tasks/{id}/assign|qms:sampling:assign|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/sampling-tasks/{id}/start|qms:sampling:execute|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/sampling-tasks/{id}/details|qms:sampling:execute|SamplingDetailCommand|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/sampling-tasks/{id}/complete|qms:sampling:complete|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/samples|qms:test:view||IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/samples/{id}|qms:test:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/samples/{id}/label|qms:sampling:execute|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/inspection-tasks|qms:test:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-tasks|qms:test:execute|InspectionTaskCommand|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/inspection-tasks/{id}|qms:test:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-tasks/{id}/assign|qms:test:execute|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-tasks/{id}/start|qms:test:execute|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-items/{id}/executions|qms:test:execute|TestExecutionCommand|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-items/{id}/results|qms:test:execute|TestResultRevisionCommand|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/test-results/{revisionId}/revisions|qms:test:correct|TestResultRevisionCommand|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-tasks/{id}/submit-review|qms:test:execute|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-tasks/{id}/review|qms:test:review|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/inspection-reports|qms:report:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-reports|qms:report:create|InspectionReportCommand|IncomingQualityRecordApiResponse|MISSING|
|GET|/quality/inspection-reports/{id}|qms:report:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-reports/{id}/review|qms:report:review|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|POST|/quality/inspection-reports/{id}/approve|qms:report:approve|GenericRequest|IncomingQualityRecordApiResponse|MISSING|
|GET|/qa/material-lots/{lotId}/release-review|qa:material-release:view||IncomingQualityRecordApiResponse|MISSING|
|POST|/qa/material-lots/{lotId}/release-decisions|qa:material-release:decide|ReleaseDecisionCommand|IncomingQualityRecordApiResponse|MISSING|

### Schema实读结论与最新需求差异

|正式契约|最新需求映射|分类 / 处理|
|---|---|---|
|InspectionRequestCommand：required materialLotId:int64、requestType；枚举INITIAL/RETEST/SUPPLEMENTARY/INVESTIGATION；reason≤1000|请验关联receipt/lot/material/type，创建冻结qc_specification_version_id|部分MATCH：lot可反查receipt/material；DESIGN GAP：正式API无qc_specification_version_id，FD说明质量标准/规格版本但未定义该实体ID与标准选择/冻结接口。不能凭字段名创造QC标准模块|
|SamplingTaskCommand：inspectionRequestId、samplingPlan必填；requiredPackageCount≥1、reason可选|QC接收后派取样计划/SOP|MATCH业务概念；人员分派/start/complete请求仍GenericRequest，需要从已有记录字段具体化，不将GenericDTO本身当阻塞|
|SamplingDetailCommand：containerNo/sampleQuantity/unitId/sampledAt必填，samplingPoint/packageResealed可选|物料批、位置、数量、单位、方法、取样人、时间、电子签名；1:N样品|数量/单位/位置/时间MATCH；方法/操作者/签名可由任务及受控上下文关联，不能让performedBy冒充登录人。sample_type在DB第798行只有字段名，没有检验样/留样/复检样/其他批准类型枚举或产生规则：DESIGN GAP|
|InspectionTaskCommand：inspectionRequestId、sampleId必填，qualityStandardVersion:string可选|Sample→TestInstance→TestResult，qc_specification_version_id|现有Task/Item/Execution/ResultRevision链可语义映射，不需重命名实体；质量标准自由字符串不是版本ID外键：SEMANTIC MISMATCH / DESIGN GAP，需设计权威裁决关联与冻结方式|
|TestExecutionCommand：startedAt/performedBy必填，instrumentId/rawData/observation/calculationInput/completedAt可选|TestInstance保留原始执行、条件/方法/上下限快照|执行事实MATCH；具体仪器/试剂/标准品、方法版、冻结范围需与DB/Domain对应，不能只存未验证自由JSON当完整标准|
|TestResultRevisionCommand：resultConclusion必填；枚举PASS/FAIL/INCONCLUSIVE；resultNumeric/resultText/resultUnitId/reasonForChange/signatureId可选|PASS/FAIL/INVALID；FAIL→OOS→获批复检新instance/result，禁止覆盖FAIL|DESIGN GAP / 显式待裁决（本轮要求与冻结契约差异）：INVALID不在既有enum；这不是两个正式文档彼此冲突，不能把INVALID静默映射成INCONCLUSIVE。不可覆盖历史MATCH；OOS/复检批准所需跨MES012契约与范围由主审查另判，不能用revision替代获批复检|
|InspectionReportCommand.required inspectionTaskId、items；item必填inspectionItemId/resultRevisionId；overallResult PASS/FAIL/INCONCLUSIVE|从InspectionRequest自动汇总多个instances/results|DESIGN GAP / 显式待裁决（本轮要求与冻结契约差异）：现有单inspectionTaskId与用户request级多instances报告的兼容性尚未定义，不断言正式文档彼此矛盾；若多个TestExecution均属于该单task可兼容，若跨task则需明确聚合范围和来源规则。不应构造第二个报告真源或手抄跨task结果|
|IncomingQualityRecordResponse仅id:int64、recordStatus必填，可选qualityStatus/inventoryStatus/releaseScope/releaseBasis/decisionSource/version|六类完整list/detail、来源/版本/历史/allowedActions/分页|DESIGN GAP：泛化返回未具体描述分页/记录业务详情；可据既有实体字段补齐typed read DTO，不把该Generic程度单独升级新业务授权|
|ReleaseDecisionCommand：decision RELEASED/REJECTED/OTHER_DISPOSITION；basis FULL_INSPECTION/INSPECTION_EXEMPT/RETEST/OTHER_APPROVED_BASIS；readOnly decisionSource USER_QA/SYSTEM_RULE；reason/signatureId/supersedesDecisionId|QA检查证据、有效签名后release/reject；免验不伪造检验|MATCH；用户不能提交SYSTEM_RULE；QC通过不等于库存可用；原始signatureId必须由服务端验证对象/版本/摘要/意义，不能仅信任存在性|
|MaterialEligibilityResponse：materialLotId/eligible/reasonCodes必填，effectiveReleaseDecisionId/evaluatedAt可选|既有统一资格门禁、有效期/复验/冻结|MATCH设计；当前无controller/service完整producer，MISSING实现。不得新增LOCKED/EXPIRED质量状态；已有状态枚举为准|

共享写契约已实读：每个写操作有Idempotency-Key；version-sensitive动作If-Match；权限403、状态/并发409、规则422；audit同事务。`/auth/reauth`和`/records/{type}/{id}/sign`为MES001平台签名，`ebr:sign`加业务provider权限；五分钟单次对象/版本/组织绑定。六记录业务角色权限不等于免除签名权限。

补充逐操作metadata复核：incoming主链selected共41条，当前成功码均200；WMS阶段7条已实现路径操作另列400/404，008A路径当前仅200/401/403/409/422。全部写操作x-audit-required=true、IdempotencyKey引用存在；其If-Match参数为optional（可由body版本约定提供），不得在实现中略掉并发检查。QMS列表操作未声明page/filter query参数，而WMS列表明确page/size/keyword/sort/status等。IncomingQualityRecordResponse的id为int64数值、version字段，与公共ID string及WMS versionNo约定存在待统一的BASELINE CONFLICT（传输契约），不应静默当两者相同。ReceiptPage/MaterialLotPage均items/total/page/size，已读取其schema。

## F. 六业务记录与页面/动作映射

|记录|List / Query|Create / Execute|View|Review / Approve|当前代码|
|---|---|---|---|---|---|
|原辅料收货|/wms/receipts|/wms/receipts/create；/:id/edit；confirm动作|/wms/receipts/:id|confirm，wms:receipt:confirm；无独立检验审核/报告批准含义|MATCH已交付阶段|
|请验单|/quality/inspection-requests|/quality/inspection-requests/create【已批准未发布】；submit/accept动作|/quality/inspection-requests/:id|accept为QC接收，qms:inspection-request:accept；不是检验报告批准|MISSING|
|取样记录|/quality/sampling-tasks|/quality/sampling-tasks/create、/:id/execute【已批准未发布】；assign/start/details/complete|/quality/sampling-tasks/:id|complete及签名；qms:sampling:complete；没有额外审批API|MISSING|
|检验记录|/quality/inspection-tasks|/quality/inspection-tasks/create、/:id/execute【已批准未发布】；item executions/results/revisions|/quality/inspection-tasks/:id|submit-review、review；qms:test:execute/review；修订另qms:test:correct|MISSING|
|检验报告|/quality/inspection-reports|/quality/inspection-reports/create【已批准未发布】；生成引用结果不可手抄|/quality/inspection-reports/:id|review/approve；qms:report:review/approve|MISSING|
|物料放行记录|无独立release-decision列表路由；物料批详情timeline承担记录入口|/qa/material-lots/:id/release已正式WORK route，受控决定|/qa/material-lots/:id/review；物料批360放行tab|release-review GET、release-decisions POST；qa:material-release:view/decide|MISSING；若用户要求独立放行记录List，需要确认是否现有入口足够，不能新造route|

边界说明：上表41条是incoming主链selected，非全系统所有样品API。legacy `/samples` 五条API归MES012，不属于008A可直接启用的替代入口，不能绕过`InspectionRequest → SamplingTask → SamplingDetail → Sample`。本次不扩展其实现或测试。

补充样品页：`/quality/samples`、`/quality/samples/:id`已有正式route，均qms:test:view；label API为qms:sampling:execute；样品由SamplingDetail追溯创建，**无独立Sample Create API/页面**。不能为了机械满足每类CRUD页面而新增直接MaterialLot→Sample入口。

质量工作台`/quality/workbench`已在正式UI详细第375行；列入映射属先前008A批准A，不重新请求。workbench并未拥有新的后端聚合API，已有权限过滤质量队列不能扩大用户可读范围。

UI约束沿用现有：List仅查询和导航；Create/View/Edit独立，View只读；不在列表Modal/Drawer新增；允许状态动作/专用GxpSignatureDialog；URL保留查询条件；409要求重新加载但保留输入；422定位字段保留输入；水平label/control桌面手机均同排；三种status分别标识，免验阶段显示“不适用（免验）”，不可显示已完成取样/检验。Review/Approve是受控动作/工作面，不意味着每个记录必需新增独立路由。

## G. 当前实现、原型证据与差距分类

|对象 / 证据|分类|结论|
|---|---|---|
|WmsController operation26–32；SecurityConfiguration 115–121；router index.ts 43–46|MATCH|收货list/create/view/edit/confirm和权限实际存在；本轮未运行验证|
|mes-qms、mes-qc、mes-release仅pom/package-info；router无quality/qa；SecurityConfiguration无QMS/QA路由匹配且185行denyAll|MISSING|008A五类后续业务API/UI尚未实现；这属于分阶段未完成，不能说有偷偷实现的新链|
|WmsController无timeline/eligibility；MaterialLotView.vue 19行仅概览/库存tab和“后续质量模块”说明|MISSING|完整360要求概览/收货/请验/取样/样品/检验/报告/放行/流水/生产使用/审计尚不具备；质量/库存状态分开已有，recordStatus应来自对应业务记录，不能编造MaterialLot.recordStatus|
|MaterialLotView库存MOVE/ADJUST正式授权动作|MATCH|不是QMS状态编辑；未发现越权放行或新QMS API实现|
|代码范围内UNAUTHORIZED IMPLEMENTATION|未发现|未发现新QC标准表、INVALID状态、直接Sample创建、重写FAIL或自制放行真源；候选文档不能算实际代码|
|UI_MAPPING.csv来料请验/取样/检验映射相同#/quality/workbench和UI-WMS-REC-Q.png|DESIGN GAP|图映射不等于真实六记录原型；缺专用业务场景。既有布局规范可复用，但不得声称已严格复刻不存在的页面|
|UI_MAPPING报告/来料QA映射UI-QA-RELEASE.png，后者正式成品批页面|SEMANTIC MISMATCH|原型显示eBR、物料平衡、成品批、QA_REVIEW，不能当作来料QA业务字段/状态权威；只能沿用layout/签名交互|
|UI-WMS-REC-Q.png trace写UNRESOLVED GET receipt-query，映射同时又列GET /wms/receipts|已消歧历史陈旧文案（不阻塞）|当前正式OpenAPI已明确定义GET，不应复制旧UNRESOLVED；PNG标签上下排列也被后来批准水平对齐覆盖|
|完整来料链到MaterialCharge反向追溯|MISSING + 后续任务依赖|当前只有receiptItemId/material snapshot/库存事实；不能凭空展示不存在的charge及QMS记录。MES011生产消费事实未实现，不伪造完成|

### 实际查看原型证据

使用view_image实际读取并视觉检查：

- `ui-prototype/screens/UI-WMS-REC-Q.png`：1600×1000收货查询，深蓝导航、白色查询卡/表、收货/物料/供应商/供应商批号/状态/日期列；显示旧UNRESOLVED trace、上下标签布局；无请验、取样、检验记录表单。
- `ui-prototype/screens/UI-QA-RELEASE.png`：1440×1024成品QA放行，左侧六Gate、右侧批摘要/允许动作、签名放行；可见eBR、偏差CAPA、物料平衡、IPC/QC、电子签名、PDF，**非来料QA证据清单**。

入口HTML实际读取：只加载styles.css与app.js。继续读取app.js导入model.mjs、render分派、release/signatureModal及model.mjs resolveScenario；未定义incoming quality场景，未知`#/quality/workbench`回退scenarios[0]。所以映射中的quality/workbench不是已实现quality原型。未启动浏览器或声称完成交互测试。

### Read coverage

完整相关operation读取采用解析正式`08_OPENAPI_FULL_V1.0.9_FROZEN.yaml` paths中/wms/receipts、/wms/material-lots、/quality/inspection-*、/quality/sampling-*、/quality/samples、/quality/test-results、/qa/material-lots全部操作；对应request/200/201 schemas逐一读取。具体读取InspectionRequestCommand、SamplingTaskCommand、SamplingDetailCommand、InspectionTaskCommand、TestExecutionCommand、TestResultRevisionCommand、InspectionReportCommand、ReleaseDecisionCommand、IncomingQualityRecordResponse/ApiResponse、MaterialEligibilityResponse/ApiResponse、GenericRequest、ReceiptCreate/Update/ItemInput/Item、Command、Receipt/MaterialLot及ApiReceipt/Page、ApiMaterialLot/Page；无以候选替换正式schema。

其他读取：MES008A任务卡；08 API Detailed来料节及平台签名节；08 Interface Guide全文；10 UI Detailed强制分离/页面结构、incoming路线与后续UI批准增量；10 Route Matrix incoming行；UI_MAPPING全文件；DESIGN_SYSTEM、DESIGN_QA全文；prototype index.html及导入业务分派/路线解析；上述两张实际PNG；正式DCP-MES002 incoming批准全文、material basic/names UI与007008 sequencing相关批准段；docs/api/README、foundation-api（仅平台指引，无incoming新契约）；docs下UI验收/设计图片不作为替代正式设计。用户已批准008A提案仅核实授权记录A/B，不当作新发布baseline。

为核查新需求差异，补读正式01 PRD §11、04 Domain controlled chain/MaterialSnapshot/ReleaseDecision、09 FD Six Incoming Records、05 DB incoming扩展798–812与master supersession；本报告不替代另一个审查人的数据库/状态全范围review。

实现只读：frontend router及MaterialLotView、WMS类型/状态相关命中；backend WmsController/WmsService确认与库存状态相关段、SecurityConfiguration对应matcher/denyAll；qms/qc/release文件清单证明目前空模块。没有查数据库、改migration、跑Maven/typecheck/测试或发布baseline。
