# 物料收货：正式接入合同核对与有界变更候选

feature_id：RECEIPT-CLOSEOUT-001；产品负责人：ChatGPT BPM SELF-REVIEW；2026-10-18。
状态：**DRAFT / G3准备材料 / NOT FROZEN / NOT G3.5 READY**。本文件明确补齐产品经理的接入决策建议，不授权直接修改正式代码、数据库或历史记录，不替代确切原型G2确认。

## 一、已确定而不再反复询问的业务
用户已选择A：选择物料后，系统分配本次收货明细的内部物料批号，字段只读；同一行保存和编辑不重编号。物料编码、名称、规格、适用单位来自主数据。生产企业来自物料与合格供货来源，不以供货企业名称作无条件兜底；生产批号依据实物/随货资料核对并独立保存；供货方并非必须另编批号。收货、逐批请验与质量放行是不同动作。界面沿用已核对设计图的T1/T2/T3结构，不交给Codex重新决定页面或业务。

## 二、实际读取的正式实现与复用结论

| 正式位置 | 读取得到的合同 | 接入要求 |
|---|---|---|
| WmsController.java，类前缀 /api/v1 | /wms/receipts 的GET、POST；/{id}的GET、PUT；/{id}/confirm的POST；写操作使用Idempotency-Key，更新/确认使用If-Match或versionNo | 保留全部原路径、命令、封装和现有权限，不另起一套收货接口 |
| WmsCommands.java | ReceiptCreate/Update/ReceiptItemInput有严格字段集合；lotNo、supplierLotNo、manufacturerLotNo分离；收货检查为Boolean | 原型字段不能直接透传；先按下节有界补充解决新增语义 |
| WmsService.applyReceipt/saveItems/confirm | 草稿也要求检查非null；supplierLotNo必填、manufacturerLotNo可空；保存明细不得减少；确认校验来源、日期、仓库、单位换算并冻结快照、建批和流水 | 不绕过校验；为用户新需求提出明确DTO/DB/校验差异，确认前不造正式库存 |
| ReceiptEntity.java | receivedAt是服务端接收记录时间，另有receivedBy/confirmedAt/confirmedBy；没有独立arrivalDate与acceptanceText | 实物到货日期不能冒用createdAt/receivedAt；自由文本不能塞入reason或审计签名 |
| ReceiptItemEntity.java | 已有materialSnapshotJson、sourceSnapshotJson、manufacturerLotNo、requiresIncomingInspectionSnapshot | 生产来源优先复用已有快照，不建立重复来源表/质量对象体系 |
| IncomingQualityService.createRequest | materialLotId、qcSpecificationVersionId、requestedQuantity、requestedUnitId、requestedPackageCount、requestedDate、priority、requestType、reason；校验实际批次/标准适用性/换算/数量件数；创建后DRAFT，后续submit/accept分别执行 | 原型“请验”映射创建草稿，不偷做提交、接受、取样；复验沿既有调查批准要求 |
| 正式ReceiptDetailView.vue | api、useControlledRequest、auth.can、allowedActions、WmsLookup/ProcessLookup、ReceiptAttachments、BatchPrintButton均已存在 | 复用真实控件与受控请求逻辑；替换原型sessionStorage和固定数组，不把原型当新的平台基础设施 |
| BatchPrintButton / ReceiptAttachments | 已有WMS_RECEIPT业务打印入口、实际附件管理与版本刷新 | 打印和附件接现有能力；原型提示未生成文件不能当正式打印完成，也不能删除正式附件功能 |

取证源码SHA256：WmsController `efb59d34784a8f0c1b4320ef524886dec2a59199839e976e9805ba9e78952465`；正式ReceiptDetailView `99544d2cad40ffd96430860c93e634bd98da51695d5f717e28f2711fb45f1aee`；正式wms/model.ts `c96d28380a7ea31796bb6ab4c4c4f44278f4b19963bfddbe765e3fc4da4b807f`。实施前必须重新核对，不能覆盖期间其他任务的修改。

## 三、产品经理建议的最小兼容方案（待有权批准）

### C1：内部批号由服务端分配，不使用浏览器计数器
- 原型RM-年月日-五位流水仅演示赋号；已搜索受影响WMS及common/system/masterdata编号相关实现，没有获得可引用的正式批准格式。
- 候选格式：`RM-{yyyyMMdd}-{seq:00000}`，组织内按业务日期连续分配；规则版本、组织时区、序列域、启用批准必须来自受控配置。此格式是建议，**不是用户已经批准的事实**。
- 行首次选择物料时申请预约号；客户端带稳定clientLineId及Idempotency-Key，后端同一意图重试返回同一个号。不存在批准规则必须明确阻断，不以随机值或materialCode代替。
- 预约不是正式MaterialLot：草稿保存绑定预约，确认后才依原WmsService建立批次及库存流水。已保存行不得重新分配，重复提交不能消耗重复号或建重复批。
- 用户删除未保存行或放弃草稿时，已分配号不得回收给另一业务；预约/作废信息留可追踪事实。取消不需要用户手工清理号池。
- 优先复用已有平台受控编号能力；受影响范围未找到可复用能力时，需将最小预约命令及序列表/唯一索引纳入DCP。未批准前不创建新接口、表或权限。

### C2：生产批号与供货方不同标识分清，历史不偷换
- 新明细的生产批号使用现有manufacturerLotNo表达，并在保存/确认按获批合同校验；现有supplierLotNo改为仅在供货方确有不同标识时提供，不再强制用户编造。
- UI名称统一“生产批号”；供货方另行标识放在明细补充，不能把生产批号暗传supplierLotNo来绕过旧后端。
- 旧记录原字段和快照原样保留。manufacturerLotNo为空的历史记录不能自动认定supplierLotNo就是生产批号；显示“历史生产批号未记录”，必要时同时显示旧供货标识及来源。更正走既有受控记录流程，不能批量覆写已确认历史。
- SQL字段空值能力、DTO校验和所有消费者必须一起核对；Flyway只追加新版本，具体编号在实施前查真实已执行最大版本，不在本草案猜号。

### C3：生产企业复用供货来源关系和已有冻结快照
- 草稿选择“物料+供货企业”取得有效来源与生产企业；缺关联或资格不满足必须说明原因，不能把来源为空默认判合格。
- 更换物料/供货来源使生产企业、生产批号、日期、检查等依赖事实重新核对；已保存行身份保持，不能套用别的行数据。
- 确认时由服务端复核并复用sourceSnapshot/materialSnapshot冻结；历史详情读历史快照，不随今天的主数据变动。
- 若主数据生产企业与实物不一致，明确阻断确认并转主数据/质量处理，不能允许前端任填厂家直接覆盖受控来源。

### C4：真实到货日期与记录时间分离
- 新增独立arrivalDate（业务日期），用户输入/核对；createdAt、receivedAt、confirmedAt保留各自语义，创建人/确认人仍由服务端身份产生。
- 历史arrivalDate缺失保留空值，不批量把创建日期写成实物到货日期。展示时注明未记录，不伪造业务事实。
- 本项需要正式DTO/数据库/API文档追加字段并更新RTM；当前原型字段不代表已修改正式表。

### C5：未知检查允许草稿，确认仍严格阻断
- 未检查、通过、不通过必须可区分；草稿允许未知检查，但核心物料/批次身份、数量、来源等保持既有有效性要求，不扩成任意脏数据入库。
- 确认条件仍为全部受要求检查通过、物料/来源/单位/库位/效期可用。任何false或unknown都不能确认。每个物料批次独立记录检查，不以整单一次勾选代替。
- 改变草稿Boolean非null限制时，必须同步DTO、数据库空值约束、历史兼容、错误提示和测试；不能把unknown偷偷传false。

### C6：验收说明不复用受控操作原因
- 设计图有验收信息。现正式ReceiptEntity没有此文本字段；建议将它作为可选、真实录入的到货验收说明，纳入与arrivalDate同一有界补充，并允许历史空值。
- 系统不能自动把所有记录写成“包装完好”等已核对结论。模板性说明只有相应实际检查支持时才可作为展示摘要，不能伪装人工记录。
- reason继续是创建/修改/确认命令的审计原因，不承担验收说明的持久化语义；历史签名/审计不改。

## 四、逐动作工程映射

| 原型动作 | 正式目标 | 必须保留的控制与结果 |
|---|---|---|
| 查询/重置/分页 | GET /api/v1/wms/receipts?page=0&size=...&keyword=...及已支持过滤 | 服务端分页；不能照搬原型全量数组筛选；返回列表保留query/page；total与过滤一致 |
| 查看/编辑初始化 | GET /api/v1/wms/receipts/{id} | 全部行按实际记录读取；保留ID/版本/旧快照，未知ID与403不显示其他记录 |
| 选择物料/来源/单位/库位 | 正式WmsLookup/ProcessLookup及现有主数据服务 | 用真实ID和批准关系，不用原型中文名称作主键；库位限当前仓库，单位使用批准换算 |
| 取得内部批号 | C1获批后的受控编号预约 | 只读、幂等、唯一、不回收；预留不等于库存或质量放行 |
| 保存新增/编辑 | POST /api/v1/wms/receipts；PUT /{id} | useControlledRequest；Idempotency-Key；versionNo/If-Match；原型错误保值/冲突恢复必须继承 |
| 确认收货 | POST /api/v1/wms/receipts/{id}/confirm | 现有权限+allowedActions、版本、原因、审计；逐批来源与检查校验，事务内建批/流水 |
| 发起请验 | 既有POST /api/v1/quality/inspection-requests | 真实materialLotId+标准版本+量/单位/件数/日期/类型/原因；返回草稿ID后展示；不自动submit/accept |
| 查看请验/批次质量 | 现有按materialLotId查询/详情、物料批次360及QA记录 | 质量/库存/请验状态分开；未放行不能因QC合格变可用；不另建质量单据 |
| 打印/附件/审计 | BatchPrintButton(WMS_RECEIPT)、ReceiptAttachments、audit:view路由 | 用真实业务ID与权限、版本刷新；实际PDF/附件需单独集成验收，不能以原型入口提示代替 |

## 五、免验、权限、数据与回归边界
- 物料是否入库必验已经有requiresIncomingInspectionSnapshot及ReceiptConfirmed事件处理。**保留已批准免验/QA链**，不把原型“所有演示物料必验”的场景扩大为正式规则，不在UI增加自动质量放行按钮。
- sessionStorage权限只是原型场景。正式页面仍使用auth.can及服务端allowedActions，后端重验组织、角色与受控权限；不得依赖隐藏按钮完成鉴权。
- 金额/数量和换算用已有精度规则；不同物料/单位不直接相加；包装件数与数量语义按获批准物料包装规则解释，不推断未经维护的换算。
- 回归必须包括新建两批→保存→查看→编辑→确认→请验草稿；未检查/不通过/无资格/过期/超量/跨组织/重复意图/并发版本/存储事务失败；免验批准链、真实打印、附件和审计签名不退化。
- 无人验确切源码+设计锁、无C1–C6批准和一致性矩阵时，不生成可实施的CODEX_DEVELOPMENT_TASK或声称G3.5 READY。

## 六、责任与状态
产品经理已将此前笼统的“正式合同待明确”收敛为上述具体兼容方案；这些决策不能留给Codex重新发散。G2发现的UI问题仍由产品经理先关闭，用户不承担替代QA的检查工作。
本文件只完成接口/业务合同核对及候选设计，不改变v1.0.23权威指针、不运行迁移、不部署、不提交代码。批准及原型验收后，按本候选逐项补正式PRD/ERD/API/权限/异常/RTM，完成G3冻结与G3.5无损交接。
