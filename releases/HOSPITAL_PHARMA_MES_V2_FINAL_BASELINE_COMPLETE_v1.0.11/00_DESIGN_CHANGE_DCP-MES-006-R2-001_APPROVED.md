# DCP-MES-006-R2-001 APPROVED — 2026-10-03

User explicitly replied “确认授权” to the bounded proposal. The following specification is the current MES-006 contract and supersedes inconsistent inherited prose. v1.0.6 remains immutable. No implementation outside this boundary is authorized.



### A. 产品与工艺 API

- 保留 OpenAPI 已列的 17 个端点及各端点权限；以 `/process-versions/{id}/approve` 为唯一审批路径，修正文档，不另建兼容审批入口。
- 为这些端点补全闭合 DTO、枚举、当前组织、分页/排序、当前版本选择、allowedActions、乐观锁和幂等/错误响应契约。保持既有写操作成功 200；没有本需求驱动的新增 REST 端点。
- 产品正式纳入 MES-006 Write Scope：沿用 md_product 已冻结字段 productCode、productName、dosageForm、specification、baseUnitId、status；ACTIVE/INACTIVE，编码不可修改，维护用明确 UPDATE/DISABLE 命令。根表公用审计/组织/乐观锁字段依既有全局标准。
- 产品查询与新增/详情/编辑采用独立 `/process/products`、`/process/products/create`、`/process/products/:id`、`/process/products/:id/edit`；使用已存在 master:product:view/create/update 权限，不新增另一套权限。工艺新增页按产品编码/名称选择产品。工艺页面沿用现有四条 `/process/packages` 路由及刚确认的列表/编辑风格。

### B. 路线、参数和 lint

- 在 proc_operation_def 增加 predecessor_codes_json（同路线 operationCode 数组，JSON有效、去重、禁止自引用）、required_equipment_type（可空）、clearance_required（布尔，默认 false）。不引入工作流引擎或另一套路线表。
- sequence_no 仅控制展示顺序；前置边为显式执行依赖。保存可允许未完成草稿；lint/提交/审批/发布验证引用存在、编码唯一、无循环、存在入口且节点可从入口访问。
- required_role 沿用既有权限/角色语义；completion_rule 只能是声明式规则数据，禁止任意 JS/SQL/SpEL。补齐允许的操作符和字段白名单，并与未来执行模块的读取契约一并冻结。
- BOM：正数基准批量/理论量，非负超量，组织内可用物料及单位；按已存在 UOM 契约验证量纲/物料专属换算。参数采集模式仅 MANUAL/AUTO/HYBRID，单位必须同组织存在，上下限可空但 lower≤upper；批准后全部定义不可原地修改。

### C. 状态、GxP 与 eBR 边界

- 工艺版本采用 DRAFT→SUBMITTED→APPROVED→EFFECTIVE；只有 DRAFT 可编辑，版本复制生成新 DRAFT，批准/发布版不可覆盖。本阶段不实现未列入 OpenAPI 的 Withdraw 命令；将该命令明确为后续受控设计项，不偷偷增加状态/入口。
- Submit、Approve、Publish 均执行服务端 lint、当前版本乐观锁、幂等及同事务审计。审批职责与创建/最后编辑职责分离；建议 Approve 必须使用现有电子签名与再认证能力，签名绑定工艺版本内容摘要，发布使用已批准的不可变内容。
- eBR 定义由 MES-007 管理；MES-006 完成 BOM/路线/参数自己的 lint 和批准/生效，不伪造 eBR 数据。MES-009 冻结生产定义时同时检查工艺和 eBR 已批准/生效；修正文档聚合边界及软依赖说明，消除循环依赖。
- 列表审批人/审批时间从现有不可变 Audit/Signature 证据读取；不凭空写入用户传入的审批信息。后续快照消费者获得独立不可变工艺定义 DTO，物料基本信息按已批准的使用时快照策略处理。

## 批准后的交付与验证

1. 发布累积新基线，统一 Database/Domain/State/API/OpenAPI/UI/Permission/Test/RTM/Migration/Integration/Task Dependency；保留 v1.0.6 原件，完成一致性检查后切换指针。
2. 查询原生 DEV 最高成功 Flyway 版本后分配下一物理版本（现有交付记录为 V010，预计 V011，执行前再次核对）。正式创建产品和七张工艺表及上述列/约束/种子，不改任何已执行迁移。
3. 在 mes-process 实现产品、工艺版本、BOM、路线/参数、lint、提交/审批/发布及相应独立页面；复用上游查询与平台审计/签名/幂等能力。不预先实现 MES-007/009。
4. 最小定向验证：编译/typecheck，TC-PROC-001/002/003 与 RTM 要求的 TC-PROC-004，组织/权限、状态、审计与签名事务、并发/幂等、UOM直接回归；桌面/手机必要流程一次。失败才做对应补测，不跑全量回归。


## Implementation rulings within the approved boundary

- Product and package identities/codes are immutable. Product commands UPDATE (all editable basic fields) and DISABLE; package PUT supports DISABLE. Both roots ACTIVE/INACTIVE, optimistic versionNo; existing definitions remain readable after disabling.
- Package POST creates root plus business version 1 DRAFT. Version creation requires package If-Match/versionNo, reason and optional sourceVersionId belonging to that package. Source omitted creates empty draft; source specified copies formula/route/parameters. Only one DRAFT per package. Serialized package lock allocates business versions. Formula/route mutations use ProcessVersion.versionNo; package mutation uses package.versionNo.
- Process business version states DRAFT→SUBMITTED→APPROVED→EFFECTIVE. No withdrawal/reject endpoint in this release. Non-DRAFT content is immutable. Only an ACTIVE package/product can submit/approve/publish; approved historic read remains available. Publish effectiveFrom is the server UTC time.
- Draft line_no and operation_code/parameter_code identify persisted rows. Saved rows are never physically removed; updates and additions are allowed. Omission of saved rows is rejected with RETAINED_DEFINITION_REQUIRED. To omit old lines create an empty new draft and maintain its desired definitions; copy is optional. UI permits removing only unsaved rows. This preserves schema and audit history without invented deletion fields.
- completionRule is null or a closed JSON tree: leaf {kind:EBR_COMPLETE|PARAMETERS_WITHIN_LIMITS|REVIEW_COMPLETE}, or {kind:ALL|ANY,rules:[...]} with 1–20 children, maximum depth 8. It is data only, never evaluated as script. Future execution consumes this exact tree; MES-006 validates syntax only.
- predecessorCodes is an array of distinct same-route operation codes; max 200 operations, 200 parameters per operation, 500 BOM items. No self-reference/dangling nodes/cycles. Multiple entry nodes are valid; all nodes must be reachable from an entry. sequenceNo positive and unique per route. MANUAL/AUTO/HYBRID only. Decimal(18,6) quantities/limits and Decimal(9,6) nonnegative overage use decimal strings; do not round invalid precision silently.
- Definition content snapshots contain IDs and fixed authored definition values, not live display labels. Master data is checked by same-org upstream query services. Material-specific UOM conversion is allowed exactly when the existing convert contract supports it. Validation errors include a concrete formula line/operation/parameter path.
- Approve requires process:package:approve and existing ebr:sign; UI obtains /auth/reauth challenge. Inline approval calls existing signature service, preserving its single-use five-minute reauthentication. Signature meaning APPROVE, objectType ProcessVersion, objectId version ID, recordVersion immutable business version. This differs from mutable versionNo used by If-Match. Signature canonical record excludes lifecycle/audit/lock metadata and includes full authored BOM/route/parameters. It therefore remains verifiable after publication. ContentHash is its signature canonical digest.
- Approver must differ from version creator, submitter and current definition row editors. Approval actor/time shown only from valid platform Signature evidence. Audit, signature and lifecycle transition commit together; token consumption is non-rollback platform behavior. Permission checks precede replay; replay does not consume another token. Idempotency canonical request excludes reauthToken but includes object/reason/expected lock version.
- Lint returns {valid:true,issues:[]} on success; errors return existing 422 envelope with code LINT_FAILED and concrete paths in message. Submit/approve/publish re-run lint. No eBR runtime is required; MES-009 additionally gates approved/effective eBR at production freeze. ProcessPackage aggregate excludes eBR-owned tables/entities.
- Lists: page 0+, size 1–100; keyword; product status; package productId/status/version/effectiveFrom filters; whitelist sort only. Package list/detail contains current selectedVersion and business versions; GET /process-packages/{id}?versionId= selects a historical version of the same package. Product and package UI routes remain independent list/create/view/edit; labels and controls follow approved v1.0.6 presentation.
- Produced ProcessQueryService snapshot uses org/version ID; requireUsable additionally checks APPROVED/EFFECTIVE and immutable digest. No HTTP wrapper crosses module boundaries. Domain Submitted/Approved/Published events carry org/packageVersion ID; no future batch/eBR runtime is implemented.
- Physical migration V011 follows verified DEV successful V010. md_product plus seven proc tables use shared org/audit/optimistic columns, FK/unique/check/index constraints. Product/process permissions and menus seed existing SYSTEM_ADMIN; approval additionally consumes existing ebr:sign permission.
- RTM TC-PROC-004 is required alongside card 001–003. Local enhanced gate validates upgrade V010→V011, complete Flyway chain integrity, cross-module Material/UOM→BOM→Route, signature verification, rollback, and concurrency. Clean database replay is reserved for existing hosted CI (local database must not be reset/duplicated).
