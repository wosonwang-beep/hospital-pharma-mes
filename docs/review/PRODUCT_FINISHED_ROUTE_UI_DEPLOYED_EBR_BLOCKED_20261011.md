# 精确路由与生产详情已部署；生产下达eBR依赖阻塞 — 2026-10-11

TARGETED ROUTE/UI PASS / DEPLOYED / EBR PUBLISHED DEFINITION BLOCKED / NOT ACCEPTED。本轮批准最多2轮，已用2轮，原失败和历次授权不重置；新eBR根因超出白名单，停止后续业务写入并返回负责人。不是八项全链PASS。

## 1. 基线依据

实际读取AGENTS.md、MES_DEV_RULES.md、现行设计锁、PRODUCT_FINISHED_CONTRACT、v1.0.23冻结API及WMS/MES012权限矩阵、当前product-finished OpenAPI。精确授权为[路由和UI修复记录](../development/decisions/PRODUCT_FINISHED_ROUTE_UI_REPAIR_AUTHORIZATION_20261011.md)。08:32开始；普通UI修复与截图复核08:39前完成（10分钟内）；后端组织检查、原维护部署及复杂业务链沿原批准范围继续。

## 2. 设计差异与实现

SecurityConfiguration只补4条精确匹配：POST /api/v1/quality/production-plans→qms:plan:create，PUT /api/v1/quality/production-plans/{id}→qms:plan:update，POST /api/v1/quality/production-plans/{id}/approve→qms:plan:approve，GET /api/v1/finished-inventory→既有wms:inventory:view。相邻原规则、末尾denyAll和Service context/组织/状态/独立审核/签名均保留，没有角色grant或permitAll。库存Controller/Service/批准OpenAPI与冻结原permission完全吻合，准入证据已先记录。

ProductionListView删除Ant Select外层错误的native input样式，保留标签/控件整对布局。BatchOverview对schema2隐藏/不额外读取内部Order身份，去除重复Order生命周期及批记录顶层入口；schema1内部订单读取和展示由组件回归证明保留。移动头部按钮换行，摘要单组标签/值避免挤压及溢出，模板/颜色/业务控件不重做。

首轮18项安全测试及10项前端测试通过，实际移动状态/溢出已消除；截图仍发现摘要四列过窄，构建发现新增测试ByRoleOptions不支持exact。第二轮只修同范围窄屏摘要、把完整文本匹配改成锚定正则（语义不变），未降低断言。一次npm误在repo根执行，在进入项目验证前失败，未计为PASS；第二轮build内vue-tsc与Vite构建均实际成功。

## 3. 修改范围及数据

源码6文件：SecurityConfiguration.java、ProductionPlanRouteAuthorizationTest.java、ProductionPlanOrganizationScopeLocalIT.java、ProductionListView.vue、BatchOverview.vue、BatchOverview.test.ts。报告/授权/MES_TASKS配套更新；哈希留本机route-ui-source-hashes.json。未改.env/local配置、执行过的迁移、角色权限、Redis或签名策略；未提交/推送。无新增迁移，DEV仍V057。

14张保留物料/产品/字典/角色权限表加9张eBR模板/定义表，共23张完整存储内容与部署前备份完全相同；物料16/FINISHED0/产品2/FK检查1。正常登录更新现有登录版本/时间及安全事件，不声称用户整行不变。新增正常质量计划633及其独立批准签名2021保留；标准316/版本314/批准签名2020、批次230保留，不重建或删除。

## 4. 定向证据和部署

- `mvn.cmd -o -pl backend/mes-security -am '-Dtest=ProductionPlanRouteAuthorizationTest,SpringSecurityPlatformContextResolverTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`：18/18，0 skip，10.755秒。实际ordered security filter chain精确匹配、正确权限到达probe controller、其他权限403、未登录401、附加path segment仍403；context从认证principal组织解析。probe不冒充业务API。
- `mvn.cmd -o -pl backend/mes-boot -am '-Dtest=ProductionPlanOrganizationScopeLocalIT' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dmes.local.plan.scope=true' test`：1/1，0 skip，suite16.22秒/整体28.914秒。真实DEV、MOCK web无监听端口、仅查询/行锁/事务回滚；质量计划真正消费的ProductionQueryService.lockBatch本组织可读230，错误组织lockBatch与batch均NoSuchElementException。无伪造JWT、改用户组织或新增测试数据。完整跨组织认证HTTP矩阵因现库没有异组织批次/计划未运行，不以此测试冒充该矩阵。
- 前端第二轮2文件10/10，11.00秒；build含vue-tsc成功，Vite23.02秒，既有chunk警告保留。没有改变阈值/skip。
- 实际5173双视口1440x900/390x844：列表6对查询均同排PASS、页面宽不溢出；详情无查询对，原工具0 pairs/pass=false保留，不能作为查询PASS；对应专用宽度/身份/按钮和视觉检查宽度1440/390，不外显INTERNAL，单一顶层电子批记录入口。第二轮手机摘要可读，桌面维持原布局。截图均实际查看；尚未完成全链所有页面的批准参考对照。
- 原checkout打包成功21.661秒，核验并停止旧PID31552，Java21/local启动唯一8080 PID34308，08:41:41 UTC；JAR SHA256 `3aaf266aee000413a3cb4be423e5206a0e54a14cf8561cbc1c9a3632e60b053e`。独立复核唯一监听PID34308、health UP。
- 实际管理员创建计划633为201，QA独立二次认证与批准均200；计划APPROVED/version1/signature2021。实际管理员PUT批准计划到达Service并被422 QUALITY_PLAN_FROZEN拒绝，计划不变。生产账号对三条计划写操作均403；未登录对三条写操作和库存GET均401。
- 仓储既有账号GET /api/v1/finished-inventory返回200，items=[]/total0；trace f24c9c57-a743-403a-929a-9a02c3bbda28。部署后库存两视口页面没有权限错误，7对查询同排/无页面溢出PASS，真实空态与流水0一致。这仅证明独立库存读取，不证明入库/出库。

## 5. 新依赖阻塞、保留状态和下一步

实际POST /main-batches/230/release用processPackageId12、ebrTemplateVersionId10返回422 LINT_FAILED / Published definition hash mismatch；trace488987c0-e7cd-4a9d-8f34-f05bb8eb4ba1。EbrService.java:79 published先重建EbrDefinitions.canonical再比较保存content_hash。模板10为EFFECTIVE，templateCode KCL30-EBR-20261007230246，templateName NULL，version1，**process_package_id=NULL / 旧package_version_id=12**；不是“现行工艺根12”的直接绑定。现库没有EFFECTIVE且process_package_id=12的模板。

模板10保存hash `deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f`；按当前canonical结构从真实详情作无float离线JSON排序重建的辅助hash `155a47e5453d0e2eac9f622a7f4f3aa3899c9c74cca398043dfd5568564565c9`，不同。服务没有返回它实际计算的摘要，因此此值明确是辅助离线结果，不冒称服务原始输出。模板及9张定义表完整内容与07:46备份相同；本輪未修改。不能直接重算覆盖发布hash或绕过校验，也不能把legacy packageVersionId当作current processPackageId。

批次230仍DRAFT/version0/process_snapshot_id NULL；计划633 APPROVED/version1；成品流水0。失败下达没有伪造状态、库存或冻结快照。下一步需要负责人只读判断：是否有可用且匹配现行工艺的现有发布模板；若缺失，则明确批准通过既有正常模板流程创建/发布一个新匹配模板，或授权保留历史canonical合同的兼容缺陷诊断/修复。两者当前均未执行。不得修改旧模板hash、历史签名或归档来强行下达。

八项：1部署/真实权限PASS（跨组织仅native消费契约验证，完整HTTP矩阵NOT RUN）；2产品标准及V2计划批准PASS，完整错误标准矩阵未全跑；3统一创建PARTIAL、下达被eBR依赖阻塞，执行/OUTPUT NOT RUN；4库存读取PASS、入库及重放NOT RUN；5成品检验/新PDF/QA NOT RUN；6发运/并发NOT RUN；7模板真实读取/不变性证据已有，整链溯源/eBR/旧归档对照NOT RUN；8本轮页面针对性检查PASS，整链所有批准参考对照NOT RUN。总体BLOCKED / NOT ACCEPTED。CUAWright最后closed/mutating_requests_allowed=false。

本机私有deployment-20261011证据：route-ui-round1-security.log、route-ui-native-organization-scope.log、route-ui-round1/round2前端测试/构建/浏览器日志、route-ui-package.log、route-ui-launch.json、route-ui-business-root.log、route-ui-live-permission-check.log、route-ui-live-frozen-update.log、route-ui-preservation-and-release-blocker.json、release-blocker-template10-detail.json/hash.json/effective-templates.json。截图在既有CUAWright evidence：1791707799817/7801016/7802256/7803415（round1）、1791707927423/7928764（round2）、1791708247710/8248901（部署后库存）。备份保留在本机，不提交私有证据或凭据。
