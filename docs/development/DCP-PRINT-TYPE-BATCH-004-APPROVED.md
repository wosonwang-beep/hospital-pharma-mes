# 模板打印类型与选中记录打印 · 有界批准补充

批准依据：2026-10-09，用户提出模板增加单据/列表属性，确认“对，按照这个设计进行开发”。本补充取代 PRINT_PARAMETER_CONTEXT_DESIGN.md 中早期通过 outputMode 选择输出的方案。既有冻结发布、迁移、签名、生产及库存状态保持历史权威。

## 范围和契约

模板版本新增 `printType: DOCUMENT | LIST`，必填并由服务端持久化。旧版本回填 DOCUMENT，不改原 DOCX/PDF/摘要。`businessType` 仍是来源及页面绑定键，`printType` 仅决定输出结构。同一业务可绑定多个两种类型的发布版本。改变模板类型保存为新版本，不覆盖已发布版本。

DOCUMENT：一个选中 ID 对应完整单据及该单据明细；多条按选中顺序合并 PDF，各自分页。调用既有单据归档，正式检验报告仍逐条核验批准及有效签名，正式幂等不变。LIST：选中 ID 的抬头事实作为循环表记录，提供动态字段，不混用单据明细。列表不能作为正式签署报告。记录数量不能用来猜测输出类型。

POST `/api/v1/printing/batches` 接受 businessType、templateVersionId、recordIds（1–100 个不同正整数 ID）及 formal，禁止客户端事实、outputMode、客户端 schema 或任意查询表达式。合并单据输入及最终输出限制25MB，超限整体回滚，不提交部分归档。服务端读取模板类型、验证已发布绑定、逐条业务权限/组织范围，使用实际源版本和事实；失败整体回滚。POST 返回批次 id、printType、recordCount、formal、templateVersionId 和两个摘要。GET `/batches/{id}/pdf` 复核组织、全部源记录权限及冻结摘要，读取存档，不重算。

已有 `/artifacts` 单据接口保持 DOCUMENT 语义；LIST 必须使用 batches。eBR 册内部冻结适配器仍只消费 DOCUMENT。既有审批、电子签名、版本、绑定及不可变历史保留。

GET `/printing/sources` 返回真实已注册的数据源 value/module/label；GET `/printing/fields` 增加可选 printType，默认 DOCUMENT。LIST 返回 reportNo/modeLabel/recordCount 与循环记录字段（record 前缀），数据来自同一 provider。模板上传增加可选 printType，旧调用默认 DOCUMENT；模板查询增加类型筛选，所有模板 DTO 返回 printType。GET `/printing/templates/{id}` 返回权威版本元数据，用于 Word/Canvas 模板重开，不从客户端路由推断保存属性。非法 JSON 或未授权的请求字段返回 400。列表模板验证需包含实际记录循环与记录字段。

## 数据库、领域和集成

新增物理迁移 V047（实施前本机成功最高版本 046）和 V049 归档/模板类型不可变保护：mes_print_template_version.print_type、CHECK；追加 mes_print_batch 存储模板引用、类型、选中 ID、有序源归档引用/事实快照、摘要、PDF 及审计身份。不存在 batch 编辑/删除/状态转换 API；新增事件 PRINT_BATCH_ARCHIVE 沿用现有审计服务。未改变原表状态机、权限代码、生产及库存命令。无新模块依赖，boot 负责业务适配。共享工作区的 V048 部门数据、V050 部门状态字典属于另一个任务，未改变其内容；V049 已执行后保留原版本及校验和，未执行的重名部门字典顺延 V050，不 repair。

WMS_RECEIPT 来源读权限 wms:receipt:view，调用 WmsService 及受组织约束的引用读取；优先已确认收货来源/物料快照。草稿引用未冻结时使用读取时引用名称并归档。不虚构备注、联系人或电话，不合计不同单位。收货打印不冒充质量正式签署报告，不改变确认、库存、质量放行状态。

## UI、权限、测试与 RTM

UI V2 不变：模板列表 T1 增加类型列/同行查询；canvas-editor T2 增加同行属性，选择类型后加载真实字段结构；原 Word 导入、预览、版本及发布保留。WMS 收货查询 T1、检验报告查询 T1 增加勾选和打印入口，收货详情 T3 单条复用。打印弹窗显示所选模板的类型，用户不重复选择输出模式。查询标签/控件始终同行，整对换行。Canvas 新 DOCX 补齐缺失的 Normal 与中文字体默认样式，循环表默认 12px 字体保证列内容可读；保留原始编辑快照、已有明确字体和历史输出。

权限沿用 print:template:view/manage/publish、print:document:generate 及源业务读权限；未新增菜单/路由权限。仅源已注册的业务页面消费，其他来源不自动注册。

|要求|实现|验证|
|---|---|---|
|模板决定输出形态|PrintType/PrintSchema/PrintBatchService|同两个 ID，DOCUMENT 两单据、LIST 两行|
|真实动态字段|sources/fields + WmsReceiptPrintProvider|来源切换、列表字段、保存重开|
|真实事实与冻结证据|业务 provider、单据归档、追加 batch|PDF 内容、hash、归档后源改变不重算|
|组织及逐条权限|ScopedStore + authorizeRead|未知/跨组织记录、权限拒绝|
|正式规则保留|DOCUMENT 使用既有 generate|LIST formal 拒绝、已有检验签名回归|
|版本/历史兼容|默认 DOCUMENT；追加版本/migration|旧接口及 NativePrintDesignerIT|
|页面可用与响应式|V2 T1/T2/T3 同行标签|真实 5173/8080 交互及匹配尺寸截图|

当前增量 API 契约为 [print-type-batch.openapi.yaml](../api/print-type-batch.openapi.yaml)，仅覆盖上述有界变更，历史 controlled-printing.openapi.yaml 保留。隔离 Word 转换进程仅传入 Windows 系统定位变量，按大小写不敏感匹配保留 windir 以识别中文字体，不继承凭据或 JVM 注入变量。

运行结果和 readiness 仅 MES_TASKS.md；验证证据记录 docs/review/PRINT_TYPE_BATCH_20261009.md。历史 DCP 和冻结发布不修改，不切换基线指针。
