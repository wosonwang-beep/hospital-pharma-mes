# DCP-MES-006-R2-001 — 工艺/BOM/路线契约补齐提案

状态：PROPOSED，尚未批准；不是新的 Source of Truth。当前权威仍是 FINAL BASELINE COMPLETE v1.0.6。本提案不修改冻结文件，也不授权迁移或业务实现。

## 授权与依赖检查

本轮请求为“开发mes-006”。此前明确批准的 DCP 范围是 MES-003、MES-004/005、物料基本信息/唯一首选供应商及界面风格；未发现覆盖下述 MES-006 新增路线字段和 eBR 边界调整的具体授权。

MES-003/004 虽为 READY FOR ACCEPTANCE，其必需物理契约已经具备：MaterialQueryService.snapshot/requireUsable，MasterQueryService.unit/convert。依据 Task Card 4A 可以消费这些契约，无需先把任务标记 ACCEPTED。物料不再有业务版本；Task Card 4C 的 Material/MaterialVersion 是残留文字，应按已批准 DCP-MATERIAL-BASIC-001 改为 Material/basic snapshot，不能重新引入物料审批。

## 已确认的冻结缺口

1. API/DTO：OpenAPI 将全部 17 个 MES-006 操作定义为 GenericRequest/GenericResponse，没有 Product、ProcessVersion、Formula、Route、Parameter 的明确请求/响应/枚举。08_API 详细设计第 15 行审批路径是 `/process-packages/{id}/versions/{versionId}/approve`，OpenAPI/catalog 实际路径是 `/process-versions/{id}/approve`，二者不一致。现有端点本身存在，缺的是完整且一致的契约。
2. 路线持久化：PROC-ROUTE-001 要求前置依赖、设备/清场及无循环/可达 lint；proc_operation_def 只有 sequence_no、required_role、completion_rule，没有保存依赖边、设备要求和清场要求的字段。仅顺序号无法表达并验证业务要求的依赖图。
3. 工艺状态/电子签名/eBR：PRD 只到 APPROVED，功能设计到 EFFECTIVE，领域命令另列 Withdraw；状态机文件没有 ProcessPackage transition 表，也未明确审批 Signature(若策略要求) 的策略。领域聚合又包含 eBR 且要求其 lint，而任务矩阵把 MES-007 列为软依赖，MES-007 反向硬依赖 MES-006。直接要求 eBR 就绪会产生依赖循环。
4. 产品范围：Section 15 和 OpenAPI 明确将 product 及 `/products` CRUD 分配 MES-006，但 Task Card 表清单未列 md_product，权限范围只列 process:package:*，UI route matrix 没有产品维护页。需要统一任务边界和一个具体的产品录入/选择方式。

## 建议批准的有限范围

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

## 当前结果

仅完成只读任务/依赖/契约评估和本提案。未新增产品代码，未执行迁移，未运行测试；MES-006 为 IN PROGRESS，实施受 DESIGN CHANGE REQUIRED 阻塞。本提案需明确批准其有限范围后才能进入冻结设计修改及实现。
