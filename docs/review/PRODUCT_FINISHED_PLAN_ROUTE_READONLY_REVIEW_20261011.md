# 质量计划精确路由只读复核与独立业务页面检查 — 2026-10-11

READONLY REVIEW COMPLETE / EXISTING ROUTE MAPPING GAP / BUSINESS ACCEPTANCE BLOCKED / NOT ACCEPTED。08:26–08:31 UTC，只读业务检查；没有修改SecurityConfiguration、授权记录、权限、源码、状态或签名门禁，没有重建316/314/230。正常登录使用已有凭据及既有安全事件/登录版本更新，不冒充数据库绝对零写入。

## 1. 基线依据

实际读取AGENTS.md、MES_DEV_RULES.md、当前DESIGN LOCK、PRODUCT_FINISHED_CONTRACT、当前product-finished.openapi.yaml及v1.0.23冻结API的对应操作、权限矩阵和MES012/013合同。使用既有cuawright-mes技能及全局注册MCP，不启动替代端口/数据库/Redis。签名修复20/20及实际部署证据见前轮报告，本阶段不扩大签名功能。

## 2. 三条精确映射与实现差异

| HTTP method/path | 已有permission | Controller | Service实际权限检查 |
| --- | --- | --- | --- |
| POST /api/v1/quality/production-plans | qms:plan:create | ProductionQualityPlanController.java:16 @PostMapping，201，Idempotency-Key | ProductionQualityPlanService.java:66 mutations.context("qms:plan:create")；:68 batch必须本组织DRAFT/schema2 |
| PUT /api/v1/quality/production-plans/{id} | qms:plan:update | Controller:17 @PutMapping("/{id}")，If-Match及Idempotency-Key | Service:77 context("qms:plan:update")；:79–80按组织get/lock，版本匹配、DRAFT、批次不可重新绑定 |
| POST /api/v1/quality/production-plans/{id}/approve | qms:plan:approve | Controller:18 @PostMapping("/{id}/approve")，If-Match及Idempotency-Key | Service:92 context("qms:plan:approve")；:94–95组织get/lock及版本；独立审核、既有签名不变 |

Controller和Service没有这三项的@PreAuthorize；实际二级授权是上述Service内MasterMutation.context检查（MasterMutation.java:20），没有权限即PermissionException。Controller有@RestController、@RequestMapping和datasource条件；Service有@Service、datasource条件，三个方法均@Transactional。

v1.0.23冻结08_OPENAPI_FULL_V1.0.23_FROZEN.yaml为JSON内容：servers /api/v1；POST create x-permission位置26374、PUT update26564、POST approve27001，对应接口26239/26466/26995。三者x-audit-required=true，全部明确401/403/404；create需Idempotency-Key，update/approve另需If-Match。冻结00_MES_012_013_COMPLETION_CONTRACT_V1.0.23.md:69列出同三条操作与权限；00_MES_012_013_PERMISSION_MATRIX.csv:21–24已有这四种plan权限。当前受控docs/api/product-finished.openapi.yaml:175/221/289保持同权限。因此不是新增角色能力、API或permission。

SecurityConfiguration当前顺序：:212–215角色权限DELETE规则 → :216计划GET /collection、/{id}仅qms:plan:view → :217 QC标准GET → :218标准POST → :219标准详情GET → :220版本创建POST → :221版本GET → :222版本PUT → :223版本批准POST。三条计划写匹配完全遗漏，最终:417 anyRequest().denyAll()。审核后若修复，应仅在:216现有plan GET相邻添加三个method+exact path+hasAuthority匹配，保持相邻已有规则和末尾denyAll。此报告没有执行修改。

实际管理员/auth/me含plan:create/update/approve/view；前轮POST plan403(trace08605eb9-4a3e-404b-8f2e-428d6a57865f)与遗漏一致。仓储没有plan权限，生产/QC仅view，QA原有approve。不能将服务role能力、组织隔离或独立审核变成通用authenticated放行。

## 3. 明确拒绝约束及验证范围

无对应权限应403，未登录应401。主filter的authenticationEntryPoint=unauthorized(:28)、accessDeniedHandler=forbidden(:29)及末尾denyAll保留；MasterMutation.context进行业务权限二次检查。跨组织计划/批次不得访问或修改，应返回组织范围内未找到404，不能允许客户端orgId扩大范围。IncomingStore.java:17–18 get/lock同时eq(org_id,org)/eq(id,id)；plan创建通过production.lockBatch(c.organizationId(),batch)，更新/批准先按当前组织get/lock，create/update字段白名单不接收orgId。

本阶段实际未登录GET plan及finished-inventory均401。三个写方法的无权限/跨组织/未登录HTTP矩阵本阶段NOT RUN（负责人限定只读，且当前正向入口本身被denyAll挡住）；不能声称已经完成这些实际拒绝测试。若修复后，应同时验证既有授权能到达服务、无权限403、未登录401、跨组织404/不泄漏、原独立审核/签名/版本拒绝仍成立。不得伪造跨组织token或改用户组织/权限。

## 4. 独立库存与双视口证据

实际GET /api/v1/main-batches/230由生产账号返回200；实际5173生产列表显示KCL20261011001、产品氯化钾溶液、1000毫升、DRAFT，详情可读。不是仅mock数据。内部订单INTERNAL:摘要在360详情中仍外显，移动端长值溢出；记录差异，未修改。

额外只读发现：GET /api/v1/finished-inventory?page=0&size=20管理员及仓储均403，两者/auth/me均实际有wms:inventory:view。FinishedChainController.java:12已有@GetMapping，FinishedInventoryQuery.java:22已有context("wms:inventory:view")，:24按org_id/schema2读取。SecurityConfiguration没有finished-inventory匹配，落入同一denyAll。当前补充OpenAPI:471已有wms:inventory:view。因此页面“没有操作权限/暂无数据”不能作为库存读取成功或真实空库存证明。此项仅作为新增阻塞报告，不自动扩大本次三条计划映射待审范围。

6个实际截图全部已查看；两个viewport分别1440x900、390x844。成品库存7对查询标签两视口同排、无页面横向溢出，几何检查PASS，但HTTP读取FAIL。生产列表1440的6对PASS；390的“状态”delta9.5px超过工具既有阈值，same_row=false，检查FAIL；不改阈值或测试。生产详情没有查询对，通用工具0 pairs/pass=false，不冒充查询PASS；390 document_width610，实际长内部订单溢出，FAIL。1440详情宽1440不溢出，但0-pair通用检查不能作为详情专用验收。尚未与批准参考做全页面同尺寸对照，不能整体视觉验收PASS。

本机evidence截图：1791707271745_readonly_finished_inventory_1440.png、1791707272950_readonly_product_batches_1440.png、1791707274150_readonly_product_batch230_1440.png、1791707275370_readonly_finished_inventory_390.png、1791707276540_readonly_product_batches_390.png、1791707277660_readonly_product_batch230_390.png，位于C:\Users\Administrator\.codex\tools\cuawright-mcp\evidence。

MCP日志及DOM摘要在本机deployment-20261011/readonly-business-dual-viewports.log、readonly-business-dual-viewports-summary.json；真实HTTP记录为redacted real-http-evidence.jsonl。浏览器最终closed，mutating_requests_allowed=false/browser_active=false。正常页面加载日志仍有401以及库存403，不宣称console clean。

## 5. 结论

只读核对完成：三条计划映射已有冻结和当前API/permission依据，遗漏导致现有授权无效；尚未修改安全配置/授权记录，等待负责人确定执行边界。签名修复已上线，当前主要业务阻塞是HTTP路由映射与只读页面暴露的库存403/移动布局差异。无新迁移、无新业务记录、无推送；固定8项整体验收保持BLOCKED。
