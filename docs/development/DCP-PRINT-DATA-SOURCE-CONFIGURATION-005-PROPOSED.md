# DCP-PRINT-DATA-SOURCE-CONFIGURATION-005 — 现有打印详细设计增量

状态：MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。主设计已审核并批准同一DCP005及后续显式升级增量；用户已授权主对话批准。批准及专项执行边界见末尾时间记录；此前第8–11节文档阶段测试状态保留为历史，不代表当前实现/验收完成。

## 1. 权威、范围与原设计连续性

本文件补齐同一打印设计：FINAL BASELINE COMPLETE v1.0.23 + `docs/architecture/CONTROLLED_WORD_PRINTING_DCP_2026-10-08.md` + 原生设计器002、Canvas003、打印类型/批次004批准补充。既有已批准打印功能不重做；旧发布、审批记录、迁移及归档保留。新工程展开在主对话审核前不替代已批准运行契约。不另建产品/视觉基线。

原详细设计补充点：001的 Database and domain、State/identity/signature、Permission/UI、Integration/task、Validation/consistency；002的 Data/version、UI/permissions；003的 Database/domain/state/API、UI/integration、Test/RTM；004的 sources/fields、数据库/领域/集成、UI/权限/Test/RTM。历史 ONLYOFFICE/LibreOffice 强制前提、最初 V033未执行等文字是历史，不当作当前实施前提；当前 converter、Canvas、DOCUMENT/LIST 继续依据002–004。

读门证据：根AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md、两份UI V2规范、上述001–004、两个打印配置说明、ApiResponse/ApiError/FieldError/GlobalExceptionHandler、ScopedEntity、MasterMutation、PlatformIdempotencyService、IamMutationExecutor、PrintService/PrintDataProvider/PrintField/PrintSchema/PrintController/PrintBatchService、Canvas绑定、SystemDictionaryController、V013/V028/V033/V034/V047/V049及WmsReceiptPrintProvider/WmsService。规则：同一main工作树，保留未提交内容，MariaDB兼容，ID/租户/审计/版本复用，受控记录不物理删除，业务读取权限和签名不被配置绕过。无新模块或服务部署。

本轮白名单：本文件、`docs/api/print-data-source-configuration.openapi.json`、`docs/development/PRINT_DATA_SOURCE_CONTRACT_MATRIX_20261010.csv`、文档检查记录；两个现有打印配置说明、001–004仅追加当前增量引用、PROJECT_BASELINE/MES_TASKS索引。禁止业务源码、迁移脚本、冻结releases、角色授予、运行环境、数据库和Git提交/推送变更。预算：一次文档编写及一次一致性复核；不是普通UI自动实现/修复任务，不启动业务修复循环。


本次追加授权上下文：用户明确“把这个任务做到收尾为止”，随后“授权所有执行”；主对话明确仍先完成文档、审核通过下发才编码。后续必要DEV迁移/后端更新/测试授权须识别真实DEV目标、保留现有代码与数据、有备份回滚；不包含生产、不可恢复删除、持久安全权限、付款、推送或无关功能。当前文档阶段不执行这些动作。

## 2. 身份、关系及类型元数据

一个组织内单据类型最多一个源身份；源可绑定多个类型，允许未配置。businessType来自BUSINESS/TREE字典PRINT_BUSINESS_DOCUMENT子项的不可变item_code；label可改。复用既有字典身份，不向通用sys_dict_type/sys_dict_item添加任意JSON。现有V039已提供字典(org_id,id)唯一键和组织组合FK；本增量追加独立PrintDocumentTypeMetadata表及受控读写入口，字典种子不宣称已落库。

保存源草稿中的businessTypes只原子预留唯一归属，UNIQUE(org_id,business_type)最终裁决并发；未绑定PRINT_SOURCE_UNBOUND，新绑定无覆盖发布版PRINT_SOURCE_NOT_PUBLISHED。每个PUBLISHED修订冻结publishedBusinessTypes；新打印同时检查当前绑定源ID、该修订覆盖该businessType、源可用及模板固定引用。新增类型不能借旧发布版立即使用；必须下一修订逐类型真实验证后发布。已有发布模板固定revision，不随源新发布改变；历史归档仍按旧字节读取。解绑/换源有在用发布模板依赖则409 SOURCE_DEPENDENCY_EXISTS，返回依赖，不暗改打印范围。

类型元数据可配置authorizationPolicyRef、compatibleMainCatalogIds、fixedTypeFilters，只接受后端注册的中文策略目录和可见catalog/字段，禁止类名、脚本或任意SQL。首次PUT expectedVersion=0表示“尚无元数据”，锁该组织字典叶项并检查不存在，首次版本写1；存在则409防并发首建覆盖。后续PUT使用实际versionNo。元数据变化更新metadataVersion/hash，使所有引用类型的草稿validation失效；发布冻结每类型的元数据版本/摘要和完整过滤/兼容快照。

已发布修订按冻结的根实体/固定过滤/兼容定义取数，但授权始终按当前类型元数据定位当前有效受支持策略、当前组织及业务读取权限。当前策略ref须与冻结ref一致，版本须经受审注册目录证明兼容；ref不同或无法证明兼容就fail closed，不能直接调用旧ref绕过当前配置。撤权403 PERMISSION_DENIED；策略被移除/禁用或与冻结快照不兼容时422 PRINT_SOURCE_POLICY_INCOMPATIBLE，新生成fail closed，不借旧策略恢复权限。根实体兼容集、授权策略或固定过滤语义改变前检查该类型在用发布模板；存在则409 SOURCE_DEPENDENCY_EXISTS，允许不改变这些语义的显示标签更新仍沿字典治理。元数据版本竞争409 VERSION_CONFLICT。策略目录只描述允许引用能力，不授予业务读取权。

## 3. 领域与物理数据契约（设计，未建表）

源身份/修订/绑定/校验/类型元数据复用ScopedEntity：id BIGINT AUTO_INCREMENT PRIMARY KEY；org_id/created_by/updated_by BIGINT NOT NULL；created_at/updated_at DATETIME(3) NOT NULL UTC；version_no BIGINT NOT NULL DEFAULT 0；UNIQUE(org_id,id)。API ID十进制字符串，版本非负整数。JSON为MariaDB兼容LONGTEXT+JSON_VALID，摘要CHAR(64)小写SHA256。证据sidecar为不可变技术记录，仅创建审计，无可编辑version字段。以下均为拟新增设计，不宣称已建表。

### 3.1 PrintDataSource / mes_print_data_source

共用字段；source_code VARCHAR(60) NOT NULL、source_name VARCHAR(120) NOT NULL、status VARCHAR(16) NOT NULL CHECK IN ('ENABLED','DISABLED')；draft_revision_id/published_revision_id BIGINT NULL；deleted_at DATETIME(3) NULL、deleted_by BIGINT NULL。UNIQUE(org_id,source_code)覆盖逻辑删除身份，编码永不复用。复合FK(org_id,id,draft_revision_id)/(org_id,id,published_revision_id)→revision(org_id,source_id,id)确保同组织同源，建表后追加循环FK。首次建身份/草稿同事务。编码^[A-Za-z0-9_-]{1,60}$，引用后不可改。

status仅是运行开关，不进入config_json/configHash。停用/启用由专用deactivate/reactivate（publish权限）执行，不能PUT草稿修改；停用不改已发布配置正文/摘要。DISABLED拒绝新模板引用、校验、发布和新生成，授权历史归档可读。reactivate只恢复身份可用，不恢复过期校验。restore只恢复从未发布、从未使用草稿的逻辑删除（manage权限），不恢复任何旧类型绑定，必须重新选择并保存，编码保留。

### 3.2 PrintDataSourceRevision / mes_print_data_source_revision

共用字段；source_id BIGINT NOT NULL、revision_no BIGINT NOT NULL、status VARCHAR(16) NOT NULL CHECK IN ('DRAFT','PUBLISHED')、schema_version INT NOT NULL首版1、main_catalog_id VARCHAR(80) NOT NULL、parameter_declarations_json/config_json LONGTEXT NOT NULL、config_hash CHAR(64) NOT NULL；published_business_types_json/type_metadata_snapshots_json LONGTEXT NULL；published_binding_set_hash/publication_evidence_hash CHAR(64) NULL；published_catalog_version VARCHAR(80) NULL；published_by BIGINT NULL、published_at DATETIME(3) NULL。发布专有字段仅PUBLISHED非空；各JSON_VALID。UNIQUE(org_id,source_id,revision_no)、UNIQUE(org_id,source_id,id)；FK(org_id,source_id)→source(org_id,id)。当前草稿由身份锁和draft指针保证唯一，PUBLISHED整修订不可变，编辑后复制成新草稿，不以status唯一键限制多历史发布版。

SourceConfiguration仅schemaVersion/sourceName/mainCatalogId/parameterDeclarations/joins/fields。sourceName纳入摘要；status/versionNo/DB ID/时间/操作者/validationId排除。根nodeId=main；nodeId/fieldId稳定，JSON内唯一；字段key/datasetKey是DOCX安全子集^[A-Za-z][A-Za-z0-9]{0,79}$，不扩大下划线白名单。JoinNode/FieldMapping增加displayOrder非负整数；关联/字段按(displayOrder,稳定nodeId/fieldId)排序表示用户顺序，改顺序即改摘要。1:N关联具有唯一datasetKey；非集合关联datasetKey=null。字段sourcePath与输出key分离，集合字段datasetKey必须匹配祖先唯一集合，主字段datasetKey=null。

冻结TypeMetadataSnapshot[]包含businessType、metadataVersion、metadataHash、authorizationPolicyRef、authorizationPolicyVersion、compatibleMainCatalogIds、fixedTypeFilters；按businessType排序。业务过滤/兼容定义冻结，当前权限和策略兼容检查不冻结为授权通行证。具体规范化/摘要/黄金样例见第6节。

### 3.3 PrintDataSourceTypeBinding / mes_print_data_source_type_binding

共用字段；business_type VARCHAR(80) NOT NULL、source_id BIGINT NOT NULL；UNIQUE(org_id,business_type)；FK(org_id,source_id)→source(org_id,id)，FK(org_id,business_type)→metadata(org_id,business_type)。字典业务码一致性由锁定字典叶项+元数据写服务校验。预留只改唯一归属，不证明某发布版覆盖新类型；运行额外核对publishedBusinessTypes。解绑无在用模板依赖才可移除技术关系并审计，源/发布修订不可物理删除。多个源加锁按源ID稳定顺序，失败整体回滚。

### 3.4 PrintSourceValidation / mes_print_source_validation

共用字段；source_revision_id/source_version_no BIGINT NOT NULL；config_hash/binding_set_hash/publication_evidence_hash CHAR(64) NOT NULL；catalog_version VARCHAR(80) NOT NULL；type_metadata_snapshots_json/type_results_json LONGTEXT NOT NULL；result VARCHAR(8) CHECK IN ('PASS','FAIL')；validated_by BIGINT NOT NULL、validated_at/expires_at DATETIME(3) NOT NULL、invalidated_at DATETIME(3) NULL。FK(org_id,source_revision_id)→revision(org_id,id)，索引(org_id,source_revision_id,config_hash,expires_at)。typeResults只存businessType/recordId/recordDigest/结果/问题，不存业务样本正文或凭据；typeMetadataSnapshots是受控配置快照，不是业务事实。有效期30分钟；任何草稿/绑定/相关类型元数据改变令旧证明失效，catalog/策略版本变化在发布复核时拒绝。校验预览只即时内存响应。

### 3.5 PrintDocumentTypeMetadata / mes_print_document_type_metadata

共用字段；dict_item_id BIGINT NOT NULL、business_type VARCHAR(80) NOT NULL、authorization_policy_ref VARCHAR(80) NOT NULL、compatible_main_catalog_ids_json/fixed_type_filters_json LONGTEXT NOT NULL、metadata_version VARCHAR(80) NOT NULL、metadata_hash CHAR(64) NOT NULL。UNIQUE(org_id,business_type)、UNIQUE(org_id,dict_item_id)，FK(org_id,dict_item_id)→sys_dict_item(org_id,id)，已有V039组合索引复用。metadataVersion=本行versionNo的十进制字符串（首建1），metadataHash仅语义投影{businessType,authorizationPolicyRef,compatibleMainCatalogIds, fixedTypeFilters}，排除ID、版本、操作者、时间。兼容catalog集合排序去重；fixedTypeFilters作为AND条件按(catalogId,catalogFieldId,operator,值的JCS表示)排序，重复/矛盾同字段EQ过滤拒绝。策略版本由只读注册目录提供，冻结快照另存，客户端不可伪造。主实体根兼容集/策略/过滤语义有在用模板依赖则拒绝直接PUT。

### 3.6 模板版本源引用

mes_print_template_version追加source_execution_mode VARCHAR(24) NOT NULL DEFAULT 'LEGACY_PROVIDER' CHECK IN ('LEGACY_PROVIDER','CONFIGURED')；source_id/source_revision_id BIGINT NULL、source_config_hash CHAR(64) NULL。LEGACY三者NULL；CONFIGURED三者非NULL、固定PUBLISHED修订且同组织同源。业务类型仍单值；发布源引用不可变由新增迁移追加约束/触发器，保留V034/V049。旧模板不回填伪造源或校验证明，重开按固定legacy模式。

### 3.7 不可变归档sidecar证据

PrintArtifactSourceEvidence / mes_print_artifact_source_evidence：id BIGINT AUTO_INCREMENT PRIMARY KEY；org_id/artifact_id/source_id/source_revision_id BIGINT NOT NULL；config_hash/type_metadata_hash CHAR(64) NOT NULL；catalog_version VARCHAR(80) NOT NULL；created_by BIGINT NOT NULL、created_at DATETIME(3) NOT NULL UTC。UNIQUE(org_id,id)、UNIQUE(org_id,artifact_id)；FK(org_id,artifact_id)→mes_print_artifact(org_id,id)；FK(org_id,source_id,source_revision_id)→revision(org_id,source_id,id)。不提供更新/删除API，新迁移禁止UPDATE/DELETE。

PrintBatchSourceEvidence / mes_print_batch_source_evidence字段同上，将artifact_id替为batch_id；UNIQUE(org_id,batch_id)，对应FK→mes_print_batch(org_id,id)。旧artifact/batch若缺(org_id,id)组合唯一索引，只追加该索引，不改正文/旧摘要/旧触发器。CONFIGURED新输出与sidecar在同一事务提交，任何证据失败整体回滚；artifact与batch分别都有自己的唯一证据。typeMetadataHash是本次businessType的冻结元数据摘要，catalogVersion来自执行配置证据。同组织校验包含源/修订/模板/归档/sidecar全部引用。

**不ALTER回填旧artifact/batch正文或新增mode/source列，不drop/改旧触发器，不重算旧snapshot_hash/pdf_hash**。legacy没有sidecar就是原契约，按旧字节摘要核验。新归档实际投影（包括LIVE_LOOKUP单位解析值）进入既有新记录正文和摘要，sidecar是同事务补充配置证据，不重写历史。

## 4. catalog、受控查询、授权和多集合

CatalogMetadata只返回当前管理员允许配置的catalogId/中文实体字段/类型/精度/时区/nullable/来源语义/主键/已知关联键/组织软删过滤/策略引用，不枚举敏感全库或连接凭据。AuthorizationPolicy目录是后端受审注册的policyRef/中文label/版本/启用/兼容catalog/当前业务读权限说明，API不暴露类名、脚本和凭据。新增支持业务域或物理表仍须审核catalog及策略，不宣称任何表零代码安全接入。配置管理权不授业务记录读权。

GenericConfiguredPrintResolver读固定修订和类型过滤，验证白名单实体/字段/关系/参数，编译参数化SQL并投影稳定key；标识符只能取验证catalog，不接受SQL/where/operator表达式或客户端orgId。参数仅DOCUMENT_ID和服务器推导上下文；固定类型EQ值参数化。复用PrintDataProvider授权和服务器事实，使用当前支持策略逐记录authorizeRead；不要写收货/请验switch或把另一业务ID命名空间误当合法。元数据当前策略撤权403；策略不兼容422 PRINT_SOURCE_POLICY_INCOMPATIBLE；元数据写依赖/并发409。

主记录0条NOT_FOUND，多条PRINT_SOURCE_CARDINALITY_INVALID；N:1/1:1多条同错误，不first截断。v1无环根树，每路径最多一个1:N，可在集合下关联单值，不支持集合套集合。各1:N以独立datasetKey有序列表取数，不join平行集合造成乘积。根深度0，最大深度4、关联节点12、字段200、每集合2000行、一次解析总查询期限5秒；超限明确拒绝不截断。默认稳定主键ASC，显式排序只能本节点catalog字段ASC/DESC并以唯一主键兜底。

数据结构新增datasets[{datasetKey,label,fields}]目录，repeated字段附datasetKey；运行数据按datasetKey独立列表。旧fields/itemFields只作为主字段及items集合兼容投影，旧repeated缺datasetKey归items。DOCUMENT支持多个独立集合；LIST继续所选单据抬头items循环，业务明细不得嵌套或混入LIST。

Canvas每个循环表固定一个datasetKey，字段不得跨集合行混绑；插入/校验/恢复/导出/填充同步扩展，旧缺省items及原{{items}}/[fieldKey]语法保留。新控件mes-loop:<datasetKey>/mes-table:<datasetKey>与受控mes-item字段按所属循环集校验，不能只验证repeated=true。DocxRenderer根据已验证datasets逐一bind(datasetKey,LoopRowTableRenderPolicy)，ContentControlFields/DocxGuard/NativePrintDesigner/EditorLoopDocx/PrintSchema/服务传参与Canvas全部同步；字段key和datasetKey安全正则不含下划线。多个集合字段key全局唯一，固定所属datasetKey；旧模板items语义不变。PDF converter仍处理渲染后DOCX，必须实测两独立表、中文、合并单元格、跨页分页和旧输出回归。

BigDecimal/业务原值遵循现有六位精确规则，不经double、不改数量；rawValue精确十进制字符串，计算/合计/质量判断用rawValue。显示默认RAW保留原精度；NUMBER显式scale0–6及HALF_UP仅生成displayValue，纳入configHash/模板发布校验，预览可查原值/显示值。去掉额外UNNECESSARY显示选项；业务层UNNECESSARY精度保护不变。DATE_ONLY/UTC_DATE_TIME按现有DATE或UTC毫秒规则，无任意格式脚本。缺失ERROR/EMPTY/DEFAULT，0/false不是空；数字DEFAULT是精确十进制字符串，不能'-'冒充数字。

收货单位以真实单据unitId为准，经已授权组织受控单位字典LIVE_LOOKUP中文名，界面说明非历史名称快照；不从当前物料主数据推算/替换，不扩WMS快照schema。unitId缺失或单位不可用按配置缺失规则，默认ERROR。实际解析单位随新归档事实/摘要固定，旧PDF不重渲染；未来单位历史快照另列有界事项，不阻当前真实收货。

## 5. 稳定字段、版本绑定与历史兼容

保留receiptNo/materialName/receivedQty及items旧键；原型main.code/items.receivedQty是示例，不能覆盖历史键。fieldId不可变；fieldKey唯一且^[A-Za-z][A-Za-z0-9]{0,79}$；中文label可改，sourcePath={nodeId,catalogFieldId}与输出键分离；type来自catalog、dataGroup为MAIN或datasetKey，集合字段datasetKey必填。字段/集合/类型/分组/格式/默认值/模板可用开关都是固定源修订定义，字段不可跨集合混绑。

旧模板保留LEGACY_PROVIDER及原字段和业务资格；不伪造迁移校验。新模板单businessType解析唯一当前绑定且覆盖的发布源，持久化mode/source/revision/configHash；未绑定/未发布明确报错，不回退任意provider。已有模板按固定revision及其冻结类型元数据/过滤取数，并用当前有效业务权限策略，源新发布不修改旧模板；新模板修订升级当前源必须显式且重新校验。当前绑定和固定revision类型覆盖每次新生成双重检查；已归档读取不重新取数，仍复核业务读权/组织/原摘要，不受后来配置重渲染。源停用阻新使用，不删历史。

旧Canvas字段接口definitions/fields/itemFields/example形状保留，可追加datasets和sourceRevision；既有legacy版本编辑传templateVersionId；新固定配置模板按其sourceRevisionId取字段，字段目录不可混源。旧repeated未标datasetKey视为items；LIST的record前缀抬头fields仍沿既有PrintSchema，禁止把业务明细当选中单据抬头。

## 6. 状态、事务、摘要、幂等和审计

保存草稿：完整SourceDraftRequest含expectedVersion/sourceName/businessTypes/configuration/reason，无status；配置无sourceStatus。If-Match与expectedVersion同值，格式沿项目裸数字或成对双引号；不一致400 INVALID_REQUEST，过期409 VERSION_CONFLICT。名称/类型元数据/catalog/稳定ID/结构/唯一绑定原子检查，所有更改令旧validation过期；取消不PUT，刷新重开读已保存。身份运行状态只能deactivate/reactivate；每个动作version+Idempotency-Key+reason。

类型元数据PUT：锁组织内字典叶项、核对ACTIVE及合法注册策略/catalog/filter、检查依赖，再首建或乐观更新；首次expectedVersion=0且必须不存在，versionNo从1开始；更新expectedVersion必须当前。变化增加metadataVersion、计算metadataHash、使所有相关草稿验证失效，同事务审计。可用发布模板依赖根兼容/策略/固定过滤语义则409拒绝，不将当前打印范围静默改掉。元数据GET不授业务读取权。

校验：每个绑定类型恰一个真实有权样本，不能提交正文/组织覆盖。捕获revisionId/configHash/sourceVersion/bindingSetHash/catalogVersion及每类metadataVersion/hash/policyVersion/固定定义，独立取主信息/各集合/字段问题；取数后再复核全部版本，变化409。校验结果30分钟、仅持久化ID/摘要/配置和错误，无原始样本正文。样本证明不替代每次运行的授权、cardinality/限制检查；元数据、绑定、配置变化主动invalidated，catalog/策略变化复核失效。

发布PublishRequest明确expectedVersion/revisionId/configHash/validationId/reason，同事务锁源身份/目标草稿/验证/绑定/类型元数据，核对源ENABLED、未删除、同源同组织，所有类型真实校验PASS未过30分钟，配置/集合/catalog/每类型metadataVersion/hash及策略兼容一致，发布者当前仍有所有样本业务读权。冻结revision/types/完整metadataSnapshots/证据摘要，更新published指针并清draft；失败整体回滚。新绑定类型只在此冻结后可用。相同目标revisionId+configHash重复发布返回该已发布revision，不新建第二修订；不同目标不能借当前sourceId误判成功。核对权限/目标后，已完成相同目标可在expectedVersion旧值检查前返回；其余严格乐观锁。下一草稿maxRevision+1由源锁分配。

幂等复用现有mes-audit PlatformIdempotencyService，组织+actor+operationCode+key，24h，IDEMPOTENCY_KEY_REUSED/IDEMPOTENCY_IN_PROGRESS为409。校验及发布回放仍重查源和每个样本当前业务读取权限/策略，撤权403，不直接返回存储response绕过授权；校验回放preview=null/previewIncluded=false，不持久化正文。重放所需sampleId和摘要属于技术证据，来自已存validation，不接收前端事实。源码层需给MasterMutation.execute式回放路径增加当前操作授权前置检查或在业务服务调用幂等前检查，不改平台既有签名摘要。

deactivate/publish权限停用身份并令验证失效，不改冻结config；reactivate同权限只启用身份、过期验证不恢复。DELETE/manage仅从未发布/未使用/无依赖草稿逻辑删除，解除无依赖绑定并留审计、编码保留；restore/manage同条件恢复身份但不恢复绑定，用户重选类型后保存并由唯一约束检查。恢复不把旧验证当有效。源/修订/sidecar物理删禁止。事务审计记录配置差异与摘要，不记录样本正文/凭据，不更改业务状态或电子签名。

### 6.1 固定摘要算法与投影

复用MasterMutation已有JsonCanonicalizer（io.github.erdtman:java-json-canonicalization:1.1，RFC8785）→getEncodedString→UTF8→SHA256→小写Hex。不自建hash算法。规范DTO先拒未知字段、填齐缺省；业务小数和NUMBER DEFAULT为精确十进制字符串，保留词法精度，"0"与"0.000000"不同hash；scale/displayOrder等配置整数允许。不会宣称所有数值都禁用。

configHash投影严格{schemaVersion,sourceName,mainCatalogId,parameterDeclarations,joins,fields}；joins按(displayOrder,nodeId)、fields按(displayOrder,fieldId)排序，不直接用输入数组偶然顺序；格式/missing/default/available包含在字段。displayOrder和中文label必填，不以偶然输入位置/技术key代替用户顺序和显示名。JoinNode默认sort=[]、datasetKey=null（1:N仍必须显式datasetKey且keys匹配catalog）；FieldMapping默认dataGroup=MAIN、datasetKey=null、available=true、format={kind:RAW,scale:null,roundingMode:null,dateStyle:null}、missing=ERROR、defaultValue=null。父节点/catalog/knownRelation/cardinality/keys、fieldId/key/type/sourcePath为必填；这些默认只是规范化，不替代完整结构校验。

bindingSetHash投影为businessTypes排序去重后的字符串数组。metadataHash投影见3.5。publicationEvidenceHash投影{configHash,bindingSetHash,catalogVersion,typeMetadataHashes:[{businessType,metadataHash}]}，类型项按businessType排序。DB ID、操作者、时间、status/versionNo、validationId排除。metadataVersion/policyVersion另作为验证/发布一致性条件和冻结快照，不混入上述configHash。黄金样例见同目录PRINT_SOURCE_HASH_GOLDEN_V1.json，包含完整输入、缺省/排序后的投影、规范化字符串和固定SHA256字面量：中文、对象乱序等值、用户字段顺序改变异值、小数字符串精度不同、绑定乱序等值，并用现有Java JCS库核对。不是正式业务测试。

## 7. API、DTO、权限及错误

精确定义仍以同一docs/api/print-data-source-configuration.openapi.json为准，成功ApiResponse{code:OK,message:Success,data,traceId}，动作200，错误ProblemDetail就是现有ApiError{requestId,code,message,fieldErrors,allowedActions}，不另造响应包。ID字符串；未知字段400。三个新增权限print:source:view/manage/publish经现有治理登记，不自动授业务读权/普通角色权限。写页面view+manage，发布动作publish；取数预览必须业务读授权，打印generate+逐记录业务读取。

源前缀/api/v1/printing/data-sources：GET分页/GET{id}/GET{id}/revisions/{revisionId}；POST新建/PUT{id}/draft/POST{id}/validate/POST{id}/publish/POST{id}/deactivate/POST{id}/reactivate/POST{id}/restore/DELETE{id}。create用Idempotency-Key，其余写均Idempotency-Key+If-Match+expectedVersion+reason；发布额外明确revisionId/configHash/validationId。create及draft没有status写字段。deactivate/reactivate为publish权限；restore和未使用草稿DELETE为manage。

类型元数据GET/PUT /api/v1/printing/document-types/{businessType}/metadata：读view；写manage+Idempotency-Key+If-Match+expectedVersion+reason；PUT只收registered authorizationPolicyRef、compatibleMainCatalogIds和fixedTypeFilters，不收版本/hash/任意类名。首次expectedVersion=0按锁字典叶项规则创建；后续乐观锁。新增GET /data-sources/metadata/authorization-policies提供可配置中文策略目录(view)。既有catalogs和document-types元数据读端点保留，选项给当前绑定/禁选原因/可见性，不返回无权限项。

GET /printing/sources保留provider目录；GET /printing/fields保持definitions/fields/itemFields/example，追加可选datasets/sourceRevision，query保留businessType/printType，可选templateVersionId。固定legacy版本按其原契约；无固定模板则唯一发布源，无绑定/覆盖版报错不fallback。新DTO DatasetDefinition{datasetKey,label,fields}、FieldMapping/LegacyPrintField增加datasetKey；每循环表固定集合；LIST只抬头items。模板编辑重开按固定版本，新增修订才升级。

新增TypeMetadataRequest/Response、AuthorizationPolicy/Policy目录Response、TypeMetadataSnapshot、DatasetDefinition、PrintArtifactSourceEvidence/PrintBatchSourceEvidence及publicationEvidenceHash字段。SourceDraftRequest移除status，SourceConfiguration移除sourceStatus；JoinNode/FieldMapping增加displayOrder和datasetKey；FieldFormat只RAW/NUMBER+HALF_UP/DATE约定，删除显示UNNECESSARY。Validation和Revision都带冻结typeMetadataSnapshots，PublishRequest显式目标。

400 INVALID_REQUEST（非法schema/标识符/类型策略/catalog/未知字段），403 PERMISSION_DENIED（源配置或当前业务读权拒绝，含重放），404 NOT_FOUND（跨组织隐藏），409 VERSION_CONFLICT/SOURCE_TYPE_CONFLICT/SOURCE_DEPENDENCY_EXISTS/IDEMPOTENCY_KEY_REUSED/IDEMPOTENCY_IN_PROGRESS（元数据首建/更新竞争、绑定抢占、在用语义变更），422 PRINT_SOURCE_UNBOUND/NOT_PUBLISHED/INACTIVE/VALIDATION_REQUIRED/VALIDATION_EXPIRED/CATALOG_CHANGED/TYPE_METADATA_CHANGED/POLICY_INCOMPATIBLE/CARDINALITY_INVALID/STRUCTURE_INVALID/LIMIT_EXCEEDED/MISSING_VALUE，500 INTERNAL_ERROR。新增码仍由现有异常层承载，旧PRINT_STATE_INVALID等不改；运行策略不兼容fail closed，不能用旧冻结授权策略越过撤权。

## 8. 已确认页面、类型管理入口及源证据

用户已确认UI UX Pro Max四步原型并授权主对话批准详细工程设计；当前主设计复核中，不等待用户再次批准已授权方向。2026-10-10确认由委派明确转达，旧原型PENDING说明保留历史。入口http://127.0.0.1:5174/#/printing/data-sources，本轮不启动或测试服务，不声明当前可达。

Library图libfile_6dfdffa292d48191873f3cd3e006ede9版本1，1586×992；mes-demo/design/打印数据源配置_UIUXProMax.png SHA256 3F8DB34EA010C8676D849C450A6B410078ECEE85592991AEFC2F2753261CBF47。原型实际使用UI UX Pro Max并查看真实像素的证据沿既有交付，不重新设计。已确认源码hash：

|mes-demo相对文件|SHA256|
|---|---|
|src/features/printing/UiuxPrintDataSources.vue|BF0341F4916BB577019ABCB505225A590B9641B2E1F9C413BA21C8BCA7E26887|
|src/features/printing/uiuxSourceContract.ts|4735F66B6605D91E93DC206561CFB2F9AEC00D472DDF21E2C93B83BD49DBBAC9|
|src/features/printing/uiuxConfirmedSeed.ts|73AB47485D54F7F3ADA21594CFB444805BC49891D6F21CE00284F0F0F9517113|
|src/PrototypeRoot.vue|C6EB7F6A12DDE9C5E8512DB7AACE97DAE287F8F67F1A740161D41833F961D3BA|

列表T1、新增/编辑T2、查看T3只读；四步basic/relations/fields/validation，沿现有用户管理和shell。顶部名称、多类型、运行状态同行；关系/七列字段左、属性右；例图仍收货主表→收货明细1:N→物料N:1，主表→供应商N:1。七列中文名称/稳定标识/类型/来源字段/分组/模板可用/操作；标签输入同一行，窄屏整对换行。状态只通过专用停用/启用动作，不能草稿请求暗改；逻辑删除恢复后类型空选并提示重新选择。

适用单据类型旁增加主设计批准的“管理类型”入口，沿现有详情/编辑规范显示字典身份、中文策略目录选择、兼容主实体catalog和受控固定过滤；view查看、manage编辑，注册目录选择不得输入类名/脚本，在用语义变更返回依赖。源页面不另造通用字典JSON编辑器。类型占用前端禁选原因+服务端事务+数据库唯一三层；新加类型提示“等待覆盖修订校验发布”，不立即使用旧发布版。

集合字段在检查器明确datasetKey，每个模板循环表只一组，跨集合行混绑拒绝；模板入口仍单选businessType自动加载唯一源中文字段，不额外选源。预览可检查rawValue/displayValue，LIVE_LOOKUP单位来源限制清晰标注。原型BLANK/DASH/ERROR映射正式EMPTY/文本DEFAULT('-')/ERROR，数字DEFAULT必须数字字符串。原型3位小数不作为全局默认。

既有19项mock浏览器/13项模型及截图结果保留，不能算正式后端/DB/PDF集成；整页刷新及同一配置多类型同时保存原先NOT RUN不提升。新类型管理、多个独立循环表、专用状态动作必须在正式实施后以真实页面/API补验，当前未实现或测试。

## 9. TestCase、RTM、迁移和集成协调

同一PRINT_DATA_SOURCE_CONTRACT_MATRIX_20261010.csv同步Domain/DB/State/API/OpenAPI/UI/Permission/Audit/Test/RTM/Migration/Integration/Task。冻结01–15及历史001–004审批不改写；当前增量引用作为主设计复核输入，审核下发后才实施。直接依赖字典身份/类型扩展、受审catalog/策略、既有三个provider、ScopedStore/MasterMutation/平台幂等、RFC8785库、Canvas/DOCX/PrintSchema/归档sidecar；无新重复基线。

后续必须实际验证：并发类型抢占一成功一409、跨组织/无业务读权、元数据首建竞争/更新乐观锁/在用语义拒绝、metadata变更使校验过期、当前策略撤权与不兼容fail closed、校验/发布重放撤权拒绝；SQL注入、平行明细无乘积、单值多行拒绝、null/0/false、超时/2001行/深度节点上限；草稿刷新重开/取消、专用停用启用/恢复不抢回绑定、过期与并发/重复显式目标发布；旧字段/旧Canvas/DOCX/items/DOCUMENT/LIST/字节归档不变；真实收货/检验样本和PDF，两独立中文表合并单元格和跨页分页；sidecar创建/回滚/同组织唯一及禁止修改。以上正式业务测试全部NOT RUN，本稿不编译或跑业务。

迁移源码最高已见V051菜单在途；本次未查询真实DB最大成功号。迁移号UNALLOCATED，不锁V052。实施前核对最高成功、其他在途，协调编号；新增表/模板源引用/sidecar组合索引及新增不可变保护只追加，保留V033/V034/V047/V049/冻结旧脚本及旧触发器。旧artifact/batch不ALTER回填正文/source列、不更新旧hash，legacy无sidecar照旧。系统兼容源真实全字段/权限/模板结构校验后才发布，不伪造数据迁移校验。授权覆盖必要DEV迁移/后端更新/测试，仍需真实DEV目标、备份回滚、保留当前数据/代码，无生产/推送/不可恢复删除授权。

## 10. 主设计复核与实施阶段门

本稿状态MAIN DESIGN REVIEW（主设计复核中），不是等待用户重复批准；用户已授权主对话批准。已按主设计八项修正定稿候选，由主对话读准确差异后明确下发实施。正式开发NOT STARTED；文档/黄金样例核对不是正式功能测试。当前不改业务、迁移、数据库、运行环境或Git提交推送。检查证据见同一PRINT_DATA_SOURCE_DESIGN_REVIEW_20261010.md；只验证本轮修正文档，不重复旧2453断言。

## 11. 决定已解决及实施核对项

已由主设计决定：独立类型元数据读写与中文策略目录；元数据参与验证/冻结及当前授权fail closed；status专用停用/启用、逻辑删除恢复不恢复绑定；多独立dataset同步Canvas/DOCX，LIST仍抬头items；不可变sidecar且不改旧归档触发器；RFC8785固定投影/黄金样例；重放再授权和显式目标发布；RAW/NUMBER+HALF_UP及约定日期格式，不提供显示UNNECESSARY。单位LIVE_LOOKUP限制照既定，不扩WMS快照。

这些不再列为等待用户批准的设计阻碍。实施前仍需核对真实数据库最高迁移号/在途与备份回滚、MariaDB组合FK/循环指针/唯一索引、当前catalog和策略可暴露权限、与既有模板全字段兼容及黄金样例执行；不声称已通过。字段key安全正则按现有DOCX子集，无需扩展下划线。用户已用源/发布修订/sidecar不可物理删除，所有新旧历史规则保持。

若执行期发现实际工程与主设计决定冲突，只报告具体冲突交主对话，不重新设计或暗改测试。当前所有正式编译/业务测试/真实DB/Flyway/PDF/部署均NOT RUN；主设计尚未下发实施。



## 主设计批准与实施下发 — 2026-10-10T13:02:40.670492+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc本次委派，已审阅DCP005第1–11节及修正实体/API，批准同一文件及18操作/61schema契约作为实施基线。用户先前已明确授权主对话批准、任务做到收尾及必要DEV迁移/后端更新/测试；本记录不把主设计批准写成用户最终验收，不改历史审批。

本次白名单：mes-reporting打印领域/API/应用/持久化及直接消费provider、mes-boot定向测试与新增Flyway、正式Vue打印页面/API/路由/现有导航入口、当前同一设计/契约/复核/任务索引。现有其他菜单/权限/UI未提交改动保留，开始清单和文件备份位于本机print-source-implementation/worktree-before.txt及file-backup/。不修改旧迁移、旧归档正文/hash/触发器、冻结releases、无关业务，不提交或推送。

专项环境覆盖：本次主设计下发明确要求迁移先在隔离测试库验证，仅该验证允许专项隔离库；正式DEV仍为已有localhost:3306/hospital_pharma_mes_dev、Redis6379、后端8080、前端5173；不drop旧库、不repair旧checksum、不增加持久安全开关。真实目标/版本/备份可恢复确认后才执行DEV迁移。复杂打印集成初始预算90分钟，最多2轮自动修复，重复同根因立即报告；不改批准验收断言。当前实施开始，迁移/编译/业务/UI/PDF验证未运行，不能宣称验收或部署完成。


## 本任务修复上限有界例外及显式升级批准 — 2026-10-10T13:36:48.414288+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源同一主对话本次委派：用户已授权主对话全权批准当前任务并做到收尾，主设计批准DCP005复杂前后端集成对MES_DEV_RULES §6A两轮修复上限作专项放宽，只覆盖本任务兼容/契约集成，不修改全局规则、不弱化断言和安全要求；逐原因定向修复/复测，同原因复发或需要改变设计立即报告，不无限循环。原Mockito撤权拒绝断言保留；失败是夹具在thenReturn参数构造时再次stubbing，先构造夹具即可修复，非权限校验通过。复杂集成预算仍按本任务90分钟记录，不因此扩至无限。

主设计批准现有Word/Canvas保存增加可选baseTemplateVersionId与sourceSelection(mode=INHERIT|UPGRADE,targetSourceRevisionId,targetConfigHash)。默认INHERIT，首次新模板解析唯一当前已发布源，同编码后续版本继承旧引用。UPGRADE需base及显式目标、同businessType/同源、当前绑定与ENABLED/PUBLISHED/类型覆盖，hash/目标冲突409，缺字段/集合兼容422带问题；复制新模板版本并重验，不修改旧版或静默删除绑定。LEGACY转CONFIGURED必须显式UPGRADE及完整兼容；前端仅增加版本升级差异预览/确认，不提供任意换源。本增量沿同一文件身份同步契约/矩阵后实施。此前BLOCKED仅为已获专项放宽的阶段记录，不是当前终止。


## DCP005执行进度（非最终验收）— 2026-10-10T14:17:01.430749+00:00

正式实现候选已接入。V052在第二隔离库恢复备份并执行通过后，经后续明确授权及自动审批重试，于13:58:57 UTC通过Flyway升级本机hospital_pharma_mes_dev；当前最高成功V052、136表。V001–V051原始文件哈希均保留。打印纯单元30/30、真实DEV回滚集成10/10通过，原断言保留、回滚检查通过；收货实际PDF与批次单据/LIST已覆盖，检验模板真实PDF转换已覆盖，尚未覆盖实际检验artifact/PDF。

正式Vue数据源API、四路由、列表/四步/属性检查器、目录关联和字段、多类型冲突、元数据管理（中文同行等值筛选）、草稿/取消导航保护、真实样本校验及明确版本发布、最小字段预览已接入。Canvas独立datasetKey循环、固定版本字段加载和显式源升级差异确认已接入；Word导入保存同契约，OnlyOffice回调明确继承base。最新类型检查和构建PASS（既有大包提示保留），Canvas6旧+3新增测试9/9通过。未运行正式浏览器，不将构建或mock原型作为正式验收。

自动审批拒绝且未执行：print-source-final-backend-checks.py（历史正式归档重用当前策略复核、单位LIVE_LOOKUP语义及真实检验PDF/升级回滚追加用例）；print-source-deploy.ps1（停止明确8080 MES PID24276、重打包、local8080及正式5173启动）。拒绝理由分别为额外后端行为范围与停止既有服务/部署授权未被审查接受。没有间接执行或绕过。8080仍旧进程health200，5173未启动，原型5174正常。正式计划URL http://127.0.0.1:5173/admin/print-data-sources 当前不可打开。临时正式浏览器写开关仍待专用stdio进程专项批准。

当前状态 IMPLEMENTATION IN PROGRESS / BLOCKED DEPLOYMENT AUTO-REVIEW，非READY/ACCEPTED/收尾。仍需处理精确审批、后端补充检查与DEV部署、真实浏览器写路径/重开/导航/权限/控制台/设计图对照、实际检验PDF、升级/绑定并发/发布竞态。完整候选清单及证据C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\implementation-status.json 与 CURRENT_OUTCOME.md。保留193文件初始备份、file-backup及5.8MB已恢复验证的DEV备份。未提交/推送/合并。


## 最新专项授权执行与门禁 — 2026-10-10T14:32:25.101208+00:00

历史归档补充已按用户准确动作授权应用；PrintService.generate命中正式归档前调用现有archiveAuthorize，保留provider.load、组织隔离、当前业务权限、旧字节/hash，单位目录语义准确LIVE_LOOKUP。30项单元通过；显式升级/默认继承/错误hash/旧模板不变用例通过。实际检验artifact/PDF/evidence用例首次因夹具遗漏正常模板业务绑定被规则拦截；补齐标准publish->bind步骤后定向1/1通过，未弱化断言。修正后WMS真实PDF/批次历史回归2/2、旧生命周期13/13再次通过。合计12个不同DEV用例分别通过，不宣称一次新全套12/12执行。

同一部署动作在用户准确列表后回复授权的证据下重试，仍被自动审批拒绝且未执行。拒绝理由：授权回复缺乏对具体服务中断副作用明确逐项确认。再次STOP，不间接启动/换端口/杀其他进程。8080仍旧local进程24276，5173未启动；正式运行/浏览器验收NOT RUN。CUAWright临时写客户端仅准备未启动，开关未变更，不宣称已完成正式截图。完整构建/模块包及hash已保存，boot薄包不可直接运行，重打包及回滚说明保留。

本任务90分钟窗口截至14:32:40 UTC；已申请专项延长30分钟至15:02:40 UTC，尚未收到明确批准，届时停止新修改和新测试，保持上述部署门禁。尚缺正式服务启动、真实API/浏览器核心闭环、并发与竞态验收；不是READY/ACCEPTED/任务收尾。未提交推送或合并。



## 2026-10-10 本次授权重新打包/重启与核心验收收尾

DEV已部署（后端PID25580/前端26676，8080/5173 HTTP200），V052与52迁移验证通过；源码修复精确HTTP权限路由、参数显式命名、实际单位字典SQL及按授权元数据生成默认设计。最终定向回归9/9、安全7/7、前端构建PASS。真实收货取数/发布、中文字段模板预览、409冲突回滚及真实PDF生成成功；归档PDF字节/摘要不变。浏览器完成保存重开、取消与只读等核心流程，1280标签同行10/10。CUAWright PDF嵌入阅读空白，完整多类型/字段关系CRUD浏览器矩阵及多行真实样本等未完成，状态为DEV_DEPLOYED_CORE_API_PASSED_BROWSER_ACCEPTANCE_PARTIAL，不能宣称全量验收通过。原型和正式代码均保留、无提交推送。完整证据：C:\Users\Administrator\Documents\Codex\2026-10-10\task\print-source-implementation\CURRENT_OUTCOME.md。


## 2026-10-10 主设计批准的有界验收补充

来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc本次明确批准。验收约定见[打印数据源验收补充](decisions/PRINT_SOURCE_ACCEPTANCE_SUPPLEMENT_20261010.md)。允许auth/me401→一次refresh200→一次重试200并保留会话；其他未恢复401、循环续期或失败续期未退出仍FAIL。模块无view权限接受既有守卫回首页，必须在源组件创建/挂载前阻止且不调用受限数据；服务端仍403，不新增403页或改业务路由。

同DEV回滚fixture补多行映射/顺序和独立dataset，隔离上下文补view/manage/publish正反例，独立事务补唯一绑定冲突；不写用户、角色、权限授予或凭据。旧FAIL/原报告保留，新约定结果另列。只测试/文档，不开写浏览器、不部署、不大回归。基线/迁移/全局安全不变，未验证项明确列出；本次定向测试预算16:15–16:45 UTC。批准补充不等于用户最终ACCEPTED。
