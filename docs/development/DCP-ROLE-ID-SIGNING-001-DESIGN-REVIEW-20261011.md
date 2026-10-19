# DCP-ROLE-ID-SIGNING-001 — 第一稿设计审查与 DEV 影响清单

记录时间：2026-10-11 09:49 UTC。状态：**整改原则已授权 / 细节待父会话设计负责人审核 / 未实施 / 未迁移 / 未验收**。

授权来源：用户消息 Sentinel_b9cfa58114548191a507b03e8d6230cb（09:44 UTC）“按这个规则审查，修正设计缺陷”，经父会话 01a115d2-8ace-7574-9a08-ac48dfc970cc 转达。原则为业务身份用稳定 role_id，编码与中文名只作管理标识/展示，权限与签名岗位可配置，发布冻结当时规则，历史证据独立保存。此记录只交付具体差异与执行边界；父会话确认细节后方可进入实现。没有批准恢复成品链验收、物理删除签名、执行账号登录名更改、部署或 DEV DDL。

## 1. 读取门禁、真实环境与最后生效动作

物理读取 AGENTS.md、docs/development/MES_DEV_RULES.md、docs/development/database-and-validation-strategy.md、docs/PROJECT_BASELINE.md、MES_TASKS.md；继承 FINAL BASELINE v1.0.23 与已批准有界 DCP。历史发布不改、已执行迁移追加原则、单一原工作区、签名/审计证据保护、最小验证门禁继续适用。本文件不切换权威指针。

实际只读连接：PC-202401011703，localhost:3306，hospital_pharma_mes_dev，MariaDB 13.0.2，foreign_key_checks=1。当前物料16、FINISHED物料0、产品2。当前角色9、用户角色关系9、角色权限关系353。

本轮改名实际生效 **0**；签名删除 **0**；权限或成员增删 **0**；新迁移 **0**；部署 **0**。没有运行活跃 seed，也没有登录并调用更名接口。上一阶段已批准的源码兼容修复保留原状；它未部署，不能算本设计已生效。原成品业务链继续暂停。

## 2. 签名身份、操作资格、历史证据三层与已证实缺陷

**签名是用户的签名，不是角色的签名。** 实际签名主体字段为gxp_signature.signer_id，来自可信CurrentPlatformContext.actorId()，表示sys_user.id；审计人员字段为gxp_audit_event.actor_id；eBR复核人员字段为ebr_review_record.reviewer_id。gxp_signature没有role_id/role_code字段。本整改不新增role_id替换signer_id，不换签名人、不把岗位当个人身份、不因角色改名要求清理签名。

三层合同：(1) 签名身份=user ID、对象/含义、再认证与签署时间；(2) 操作资格=当前可信roleIDs、已注册permission_code、个人资质、组织上下文、职责分离，仅决定能否进行今后操作；(3) 历史证据=gxp_audit_event.actor_role、ebr_review_record.role_snapshot及冻结规则中的当时岗位说明，用于证明当时资格，不代表当前角色关系。

摘要精确输入：Rfc8785SignatureCanonicalizer将schemaVersion=1.0、objectType、objectId、recordVersion、业务canonicalRecord作为record，以及排序去重的evidenceIds组成envelope，按RFC8785 JCS规范化再计算UTF-8 SHA-256。通用envelope不单独加入roleCode/roleID；业务record本身可包含被冻结的人员/岗位证据，不能重写这些字段来适配当前角色名称。

源码证据：SignatureTransactionService第48行用context.actorId()插入NewSignature的signerId；第46行record_digest取SignatureCanonicalizer.digest(object)；verify第61–66行比较VALID状态与业务对象当前canonical摘要，不查询当前角色编码/启用状态。实际角色字符串耦合在eBR规则匹配/角色证据验真，以及eBR复核canonical中的roleSnapshot/frozenRule，而不是通用签名的用户身份。auth_context_json不含role_code，且本轮不输出其私有值。角色code/name改名本身不改变这些既有历史输入；撤权/停用只拒绝未来操作，不追溯否定过去合法签名。

| 项目 | 现状/风险 | 有界整改方向 |
|---|---|---|
| 用户→角色→权限 | sys_user_role.role_id 与 sys_role_permission.role_id/permission_id 已是真正 FK | 保留现有 ID、关系及权限集合，不重建用户/角色 |
| eBR 签名与复核岗位 | ebr_signature_rule / ebr_review_rule.required_role 为字符串；EbrFormSignableProvider、EbrRuntimeTransactions 按 roleCodes 判断 | 新规则用 required_role_id；后端从可信主体的有效 roleIDs 判断 |
| 角色组织范围 | sys_role、sys_user 没有 org_id；当前身份组织由 ConfiguredPlatformOrganizationResolver 的服务端配置给定 | 保留全局岗位目录，先核对用户组织成员与业务对象隔离合同；不预设新增角色组织范围，详见第3节 |
| 会话 | LoginSnapshot、CurrentPlatformContext、RedisSessionStore 只有 roleCodes；refresh 续用原授权快照 | 增加 roleIDs；失效旧快照；请求及关键操作重新验证当前有效身份和组织范围 |
| 签名岗位证据 | EbrRoleEvidence 把 requiredRole 写到 audit.actor_role，并按字符串匹配；通用 roleSnapshot 取排序后的首个编码 | 新证据绑定实际满足的 roleID、签名ID、冻结政策摘要；禁止首个角色替代实际岗位 |
| DSL | hasRole(code) 及 EbrEngine/EbrRuntimeModel 的角色集合仍是编码 | 新 schema 使用受控 hasRoleId；旧表达式原文保留，执行时只用显式唯一兼容映射 |
| QA 资质 | md_qualification 关联 user_id，带 qualification_code/ACTIVE/有效期；操作需求由 application-local.yml 的 required-codes 配置 | 保留个人资质独立于角色；将操作所需资质绑定设计为受控管理配置，不用角色名推断资质 |
| 功能权限 | permission_code 是已注册的服务端接口与页面合同，数据库授权关系已经可配置 | 保留 qms:test:*、ebr:sign 等稳定功能编码；不机械替换为 ID，不取消服务端默认拒绝 |
| 旧证据 | 发布 JSON、审计 actor_role、签名摘要、打印归档可能保留当时编码 | 不改旧 JSON/hash/审计，不给主体加入旧新双编码，不用删除签名解决身份耦合 |

当前未发现主运行源码硬编码那7个 DEMO 角色/账号作为授权判定条件；风险来自字符串合同、管理配置及活跃 seed。此结论不表示所有代码中的业务 code 都应该改为 ID。

## 3. 需要确认的组织与资格合同

经父会话预审，保留全局 sys_role 主键与现有成员关系，不给角色增加单一org、不复制角色、不默认新增scope表。新增核验：sys_user_department当前0行；登录组织来自服务端mes.platform.organization-id；md_organization.id为23–26而其org_id均1，部门organization_id=23而org_id=1。因此租户/数据隔离org_id与组织树实体ID不能混作同一外键，也不能把空的部门关系当作已有认证组织成员关系。第一阶段沿用当前受信服务器组织上下文、有效全局角色成员关系与业务对象org校验；如后续要求多组织登录，须先独立确定组织内用户成员和角色作用域合同，再判断是否需要额外允许范围，不能从缺列直接推导新模型。

规则保存、lint、提交、发布与签名时均验证：角色存在且启用；规则及业务对象组织与主体受信组织一致；主体当前有效成员关系包含该角色 ID；功能 permission_code 满足；必要个人资质有效；职责分离满足。客户端只能选择 ID，不能传 org_id 改身份、传 roleCode 代替 ID、传 snapshot 决定资格。组织、当前编码/名称由服务器解析；后续若批准允许范围合同，再额外校验该范围。

多角色：冻结规则指定的岗位可以是主体有效角色集合中的任一成员，不能任意选择主体其他岗位满足规则。多个同义规则的匹配顺序/唯一规则标识按现有顺序合同确定；不得通过首个编码选择签名身份。停用角色、撤销成员、撤销所需权限、组织范围撤销均应在下一请求/签名事务拒绝。管理员编辑配置也不能关闭再认证、职责分离、签名顺序、摘要一致性等必要保护。

会话最小机制建议：新增sys_user.authorization_version，LoginSnapshot/Redis会话保存该值及来自可信目录的roleIDs。每请求只按user主键读取enabled与authorization_version并比较，不全库重载角色权限；变化拒绝旧会话，重新登录才重建授权集合。角色成员修改只递增该用户授权版本；角色停用/角色权限更改及权限停用按索引关系找受影响用户，在管理事务中同步递增其授权版本，所有实际写入口必须覆盖。当前两套管理路径对用户角色/角色权限修改均未统一撤销会话，不能只修一个入口。可复用revokeAllForUser清理受影响会话，但授权版本校验作为撤销清理失败时的拒绝保障。显示名/角色编码改名不影响资格，登录名变更单独失效该用户会话。缺roleIDs/授权版本的旧会话受控失效，不从旧code猜ID。

签名事务另做针对当前用户/指定岗位/所需资质的定点查询与并发校验，约定同事务锁定授权版本及相关成员变更的统一锁顺序，避免检查后撤销仍成功签署。Redis或授权版本读取故障时拒绝关键写入。此最小版本机制是待批准新增合同，不声称当前已具备；不把角色/权限写进JWT，不依赖刷新旧快照。

职责分离沿用各业务命令的实际人员 ID：eBR 独立复核、模板创建/提交人与批准人、计划创建/提交人与批准人、QA 与需独立的记录/复核人均继续按用户 ID 判断。修改显示名、登录名或角色编码不得消除独立性约束。资质记录的 user_id、有效期和状态不因角色改名而变。

## 4. 新版冻结 payload 与旧版兼容

新定义 schema 采用明确的新版本（建议2.0，须确认），冻结签名/复核规则包括 `requiredRoleId`、服务器解析的 `roleCodeSnapshot`、`roleNameSnapshot`、规则标识/顺序/独立性/再认证要求。JCS 摘要覆盖新 schema、角色 ID 与当时快照；保存阶段不接受客户端自填快照。发布后角色改名不会重写模板冻结内容。

新签名证据保存实际匹配 roleID、组织、签名ID、规则标识和冻结政策摘要；可通过新增不可变角色证据关系承载，避免修改旧 gxp_signature 或既有 audit.actor_role 语义。新审计继续显示当时编码/名称，但机器判断用 roleID 和政策摘要。旧历史签名验真仍使用当时证据，不因角色日后停用而重写历史真实性；当前签名资格必须采用当前有效授权。

旧 schema 独立解析，原 canonical 字段集合和 hash 不变。只有可证明旧 code 唯一映射到该受信组织上下文允许使用的某个现有角色 ID 时，才能建立显式兼容映射 `(org_id, legacy_role_code) -> role_id`，保存证据/审批来源，禁止重新指向不同身份。无映射/歧义/跨组织/停用时拒绝新业务签署；不能猜测、去前缀自动匹配、给主体同时授予旧新 code、遍历删除字段寻找正确 hash。旧 DSL 的 hasRole(code) 使用同一显式映射，不改表达式原文。该映射用于未来操作的旧规则解析，不用于追溯否定过去已合法签署证据。

DEV 当前签名/复核规则共14行，仅属于未发布模板291：签名180–185、复核269–274要求角色290；签名186、复核275要求角色291。模板291为 DRAFT、row version1、content_hash为空；可在新合同批准后通过受控草稿保存升级，而不是直接重写已发布版本。已生效模板6/7/9/10的原 hash 必须保持；模板10原摘要为 deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f。

## 5. 精确角色和验收账号映射（待执行）

| roleID | 原编码→建议编码 | 建议中文岗位名 | userID / 原登录名→建议登录名 | 当前权限数 |
|---|---|---|---|---:|
|285|DEMO_MES_MASTER→MES_MASTER|主数据维护员|11564 / demo.master→master|7|
|286|DEMO_MES_WAREHOUSE→MES_WAREHOUSE|仓储管理员|11565 / demo.warehouse→warehouse|18|
|287|DEMO_MES_PRODUCTION→MES_PRODUCTION|生产操作员|11572 / demo.production→production|35|
|288|DEMO_MES_SAMPLER→MES_SAMPLER|取样员|11566 / demo.sampler→sampler|13|
|289|DEMO_MES_QC_ANALYST→MES_QC_ANALYST|质量检验员|11567 / demo.qc.analyst→qc.analyst|22|
|290|DEMO_MES_QC_REVIEWER→MES_QC_REVIEWER|质量检验复核员|11568 / demo.qc.reviewer→qc.reviewer|16|
|291|DEMO_MES_QA→MES_QA|质量保证审核员|11569 / demo.qa→qa|32|

9个用户/角色 ID、9条成员关系和353条权限关系全部保留。SYSTEM_ADMIN/id1（207权限）、QC_DOCUMENT_EDITOR/id284（3权限），以及 admin/id11556、qc.documents/id11557保持。当前7个岗位的角色中文名和人员中文显示名本身未命中禁用词；上表中文岗位名只是规范化建议，不伪造人员姓名。登录名规范化后未发现与当前已有账号冲突，执行前仍必须加锁并复查唯一约束。

现有正常 API 能改显示名：PATCH /api/v1/admin/roles/{roleId}/name，PATCH /api/v1/admin/users/{userId}/profile，携带 expectedVersion。现有接口不能修改 role_code 或 login_name；IamContractService.UpdateRoleRequest/UpdateUserRequest 同样不含这些字段。因此不能声称账号或角色编码已通过 API 改好。建议增加两个严格字段白名单的更名命令，权限沿用对应既有管理能力，必须有原因、乐观锁、唯一性、恢复证据和安全事件；新路由/DTO需明确批准，不用 SQL 绕过接口合同。登录名变更需独立动作确认及会话失效，不重置密码、不导出凭据、不接受旧登录名永久别名。

依赖顺序：冻结当前 ID/成员/权限摘要→明确组织范围→批准新版规则/兼容合同→隔离验证并通过代码整改→更新批准范围内的草稿引用和活跃 seed→重新扫描依赖→确认角色编码命令→确认登录名命令→保留身份记录并受控失效旧会话→独立连接复查成员/权限摘要与新旧签名验真。任何 ID/权限/成员摘要变化、冲突、歧义或旧证据变化均中止。角色编码可以回改同一 ID；登录名回改需新的已批准受控命令且不被占用；新发出的签名不靠回改历史证据恢复。

## 6. 其他命名命中与活跃种子

只读词法扫描覆盖126张含字符列的表、3344行；不扫描/输出 password_hash，auth_context_json 及敏感 JSON 键。词法命中不能当作自动改名清单。qms:test:*、TEST_RESULT_REVISION等属于真实检验功能或对象标识；数据库表/列、接口合同与历史迁移不做机械清除。

其他确定需另列影响的当前配置：12个部门编码为 MES_DEMO_*，12条说明含“演示组织”；13条个人资质含 MES_DEMO_*（其中7条MES_DEMO_GMP，6条具体操作资质）；本机 application-local.yml required-codes 8个操作映射使用上述具体资质，不能只改数据库一侧。设备4/5/7的 equipment_type 为 DEMO_SCALE，引用系统字典项1；字典项48/49显示标签含“DEV测试”。这些关联应另行核对正常管理 API、消费者和共享用途再纳入有界改名，不直接改字典编码破坏设备引用。物料批快照2处及大量审计/幂等/打印历史中的旧标识必须保留。

活跃或可触发 seed 路径：backend/mes-boot/src/test/java/com/hospital/mes/demo/{DemoRoleAssignmentsSeedIT,DemoRoleQualificationSeedIT,RealisticIntegrationSeedIT,RealisticProductionFinishedSeedIT,CorrectRunningOperationOwnerIT}.java。它们仍包含旧账号/角色/资质引用，应调整明确的维护入口与名称策略，不能运行来覆盖现有成员或重新造旧演示账号。隔离测试可使用明确的临时 fixture，但不得被宣称为实际正常命名 DEV 数据。私有验收脚本中的账号引用需同步改，不能打印邻接凭据。

## 7. 50条签名物理清理的实际影响（仍不执行）

gxp_signature共50，全部 VALID、org1，INVALIDATED=0。直接外键被46条业务记录引用，另4条为历史工艺版本逻辑绑定。28个外键列、25张引用表，全部 DELETE/UPDATE RESTRICT，无 CASCADE/SET NULL 可自动解链。gxp_signature本表没有删除触发器，但引用业务表存在保护，不能因此直接删除。

| 签名对象/关联表 | 数量 | 当前业务状态/清理冲突 |
|---|---:|---|
|QcSpecificationVersion / qc_specification_version|12|全APPROVED；CHECK要求批准签名存在；已批准证据不可改、不可删，不能重置DRAFT|
|SAMPLING_TASK / qms_sampling_task|11|完成签名与证据不可改；记录禁止删除|
|TEST_RESULT_REVISION / qms_test_result_revision|7|signature_id NOT NULL；所有更新/删除受不可变保护|
|INSPECTION_TASK / qms_inspection_task|7|5 QC_PASSED、2 QC_FAILED；已存在复核签名/证据不可改，记录禁止删除|
|INSPECTION_REPORT / qms_inspection_report|5|全APPROVED；批准证据不可改，记录禁止删除|
|MATERIAL_RELEASE_DECISION / qms_release_decision|3|全RELEASED；对应批6/17/25 RELEASED且AVAILABLE；放行决定禁止更新/删除|
|ProcessVersion / 工艺版本逻辑关联|4|版本8/9/11/12 EFFECTIVE；删除签名不能伪称仍具批准证据|
|PRODUCTION_QUALITY_PLAN / qms_production_plan|1|计划633 APPROVED，签名2021；CHECK要求签名/批准/冻结内容存在；冻结标准314签名2020|

候选签名主键固定为：17–35中实际存在的19条，52–70中实际存在的19条，94–100中7条，117、143、160、2020、2021；以私有证据 JSON 的50个明确主键为准，不能用数字区间DELETE推断缺口或并发新增。

额外共享/冻结影响：13条请验请求保留上述已批准标准的版本ID与specification_content_hash；计划633冻结标准314；打印归档10条均formal=0，全部PDF实际SHA与pdf_hash一致，其中artifact15属于报告11，其批准签名98。prd_process_snapshot、ebr_batch_snapshot、ebr_book_pdf、ebr_pdf_manifest目前均0。空eBR表不等于没有质量历史。模板审批本身另有approved_by/approved_at/hash，不能把删除gxp_signature当作取消模板批准。

**结论：签名物理删除为 DESIGN CHANGE REQUIRED。** 在保留业务行、不可变证据、外键检查和正确批准/放行状态的约束下，没有可执行的纯DML清空方案。不能把可空列一律置空，也不能将APPROVED/RELEASED留作无签名伪状态。现有内部invalidate服务并非可直接调用的全库公开命令；用户已指定物理删除，也不能未经确认改做作废。任何取消/撤回业务事实、新状态或维护能力都需独立有界设计与准确范围批准。本设计整改不包含拆保护、改签名NOT NULL、关闭外键、删业务记录或删审计。DDL不能混入删除事务。

## 8. 恢复与本机证据

现有最近完整备份：C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\deployment-20261011\ebr-version2-prechange\hospital_pharma_mes_dev-full-20261011T090633Z.sql。

大小5,953,960 bytes；SHA256 d0ab4aca617ed67f2c98fb15991616e4bce2ec24e8d230d817723fb9b15e3249。本轮重新核验实际文件大小/摘要一致。此前记录的138表/4109行、结构/触发器/全部存储字段字节的离线重构比对成立；此备份**未做实际导入恢复**。更早备份的恢复测试不能替代它，不再创建第二本地数据库。正式改名/迁移/清理前需新的完整一致备份与明确恢复验证方案及回退条件，不能把09:06备份当作09:49状态恢复证明。

新只读证据：C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\role-id-review-20261011.json；SHA256 1278bfc06b752fc186506c46ee5bc6d3c1ddb65fa309d61a5a81a8c9c2aec45d。保存50个签名候选主键/摘要/状态、9角色、9成员关系、353权限关系、28外键、14个业务保护触发器和17个相关CHECK。业务数据查询以只读一致性快照并ROLLBACK结束；information_schema关联按schema+表名+约束名匹配，防止不同表重复约束名造成虚假影响计数。文件留在本机，不进Git。

## 9. 具体实施路径与需要同步的设计产物

本阶段仅新增本审查文件，不写迁移、代码、运行配置或权威指针。审核后下一阶段应先增加有界决策记录/需求与领域、DB/API/详细设计/UI/测试/RTM一致矩阵，再写尚未进入Flyway扫描目录的迁移草稿。物理号在DEV执行前按最新成功版本确定，不能现在固占V058；已执行V001–V057及历史release不改。

| 受影响路径 | 预计变化 |
|---|---|
|backend/mes-system/src/main/java/com/hospital/mes/system/{application,infrastructure,api}/|RoleAdministration、AssignmentAdministration、IamContractService、SystemIdentityDirectory、SysRolePermissionMapper、UserAdminController、RoleAdminController：有效roleIDs/范围查询、受控编码/登录名更名、撤销会话；不更改授权集合|
|backend/mes-security/src/main/java/com/hospital/mes/security/{identity,session,context,web,application,reauth}/|LoginSnapshot、RedisSessionStore、SessionSnapshot、SessionAuthenticationFilter、AuthService、ReauthenticationService、SpringSecurityPlatformContextResolver：roleIDs与实时授权检查，旧快照拒绝|
|backend/mes-audit/src/main/java/com/hospital/mes/audit/application/{PlatformPrincipal,CurrentPlatformContext}.java|可信roleIDs上下文；保留旧审计字段兼容，不让通用首角色成为业务岗位|
|backend/mes-ebr/src/main/java/com/hospital/mes/ebr/{domain,application,infrastructure}/|EbrCommands、EbrDefinitionRules、EbrDsl、EbrEngine、EbrDefinitions、EbrRuntimeModel、EbrRuntimeTransactions、EbrFormSignableProvider、EbrRoleEvidence、SignatureRuleEntity、ReviewRuleEntity：版本化ID规则/历史解析/不可变新证据|
|backend/mes-boot/src/main/java/ 与 backend/mes-masterdata/src/main/java/ 资质查询适配|核对IncomingActorAdapter、批准/QA命令所需操作资质配置与服务端独立性；不能删除资格保护|
|frontend/mes-web/src/views/ebr/{types.ts,model.ts,attributes.ts,DefinitionDesigner.vue,RuntimeFormView.vue} 与 src/api/|ID选择、中文名/编码展示、冻结只读展示；现有结构和Global UI V2保持|
|frontend/mes-web/src/views/admin/{RolesView.vue,RoleDetailView.vue,UsersView.vue,UserDetailView.vue} 与 src/api/|受控身份标识管理命令；组织允许范围是否需要另行确定，不默认新增页面；新增/实质重设计页面须先mes-demo原型精确hash确认，不用本报告代替原型批准|
|docs/development/decisions/、docs/architecture/、docs/database/、docs/api/、docs/ui/、docs/compliance/ 与 docs/PROJECT_BASELINE.md|统一有界DCP决策、DB/domain/state/API/UI/permission/test/RTM/migration合同；全部一致复核后才引用已批准补充，不改历史release|
|backend/mes-boot/src/test/java/com/hospital/mes/demo/ 与受影响单元/契约测试|停止旧命名seed复现；新负例和历史黄金摘要；不运行持久DEV seed|

迁移草稿预计新增：规则required_role_id（真正FK、RESTRICT）、版本化规则合同标记、sys_user.authorization_version、显式旧code映射、新不可变岗位证据；旧required_role列和已发布数据先保留。角色组织范围不默认新增，见预审后的第3节。当前14条DRAFT规则升级通过受控草稿命令，不能批量重写所有定义。是否新增QA操作资质配置表/管理入口须先确认其适用操作及现有权限边界，不能把本机YAML直接全部迁入新的业务模型。

## 10. 验证合同及交付门禁

先在原工作区做无DEV写入的单元/契约/黄金payload验证：7角色改名保持身份及授权集合；新roleID规则；多角色匹配；停用/撤销立即拒绝；跨组织/客户端伪造拒绝；陈旧/无roleIDs会话拒绝；同人多角色仍违反独立性；管理员不能关闭必要保护；职责及资质到期；旧映射唯一/缺失/歧义失败；旧新code不能双重授权；历史模板/签名/归档黄金摘要不变；未知客户端snapshot/code字段拒绝；并发撤销与签署竞态；API幂等/乐观锁/冲突回退。permission_code接口合同不改。

这里的隔离测试指不向共享DEV提交记录的代码级测试，不创建新端口、新数据库或项目副本。MariaDB迁移/FK/事务检查及真实页面验收需下一阶段报告精确影响并取得父会话执行批准后进行。不得宣称本报告通过运行验收。

父会话本轮需审核：维持当前全局角色/服务端固定组织边界；新schema与旧版兼容边界；最小授权版本/事务并发策略；最小新更名命令与确认边界；QA资质管理配置的具体范围。签名删除单独继续BLOCKED，不能捆绑获批。当前状态为设计第一稿待审核；无READY/ACCEPTED声明。

## 11. 签名清理可评估选项与已支持流程（预审补充）

已核对现有实际能力：QcSpecificationController /specification-versions/{id}/retire可APPROVED→RETIRED，但保留批准签名并新增退役签名；IncomingQualityService支持报告supersedesReportId和放行决定supersedesDecisionId，均追加替代证据而不移除原签名；result(...,revision=true)仅允许任务IN_PROGRESS且追加到立即前一修订，原FAIL不能普通更正为PASS或INVALID。IncomingActionPolicy中已完成取样、QC_PASSED/QC_FAILED任务、APPROVED报告没有重开/撤回命令。ProductionQualityService的REOPEN仅针对CAPA，不能套用于这7个已终结检验任务。ProcessService旧工艺版本transition已由批准的基本维护补充退役，抛UnsupportedOperationException；不能用旧版本API重开4个EFFECTIVE工艺。

7条结果修订3/4/5/8/9/10/13均previous_revision_id为空，5 PASS、2 FAIL；但不是可丢弃的孤立行。偏差135/136引用original_result_revision_id；报告项3/4/7/8/11同时引用原始与选用修订（同5行，不能重复算成10行）。生产检验实例/复核及后继修订当前无此引用。删除7条修订会破坏这些保留结果与偏差事实，因此不建议扩大为全部业务删除。

| 可评估选项 | 当前能否执行 | 满足物理删除50条？ | 真实结果/代价 |
|---|---|---|---|
|A. 现有退役/替代/重新审批命令|有相应流程者可通过正常API逐项执行，仍需操作授权与当前门禁|否|旧签名和原始结果保留；新增签名，标准RETIRE后不能新选用；报告/放行有效链按替代关系更新|
|B. 专项DEV证据重置工具，保留原业务结果数值/文字/ID|当前不存在，需独立DCP和专用工具开发/隔离验证/精确执行批准|可以作为待设计目标；现在不能执行|必须撤销对应批准有效性，处理7条不可空修订签名与现有不允许重开的状态；不得只清签名列后仍显示有效批准/放行|
|C. 扩大删除业务闭包|不在用户现有授权内；未生成/批准新闭包|理论需再扫描，不承诺现成执行|会删除结果/偏差/报告等业务事实；不推荐，不以旧影响清单替代新扫描|

若用户选择B，专项DCP至少应逐对象批准以下**新能力**，不是现有合法状态转换：12个标准版本撤回批准但保留定义及原hash恢复证据；11个取样任务保留取样事实但撤回完成签证，允许仅重签而不重复扣库存/造样品；7条结果修订保留结果与链路，增加明确未签证/待重签语义及受控再签，禁止默认将它们仍计作有效签名结果；7个检验任务退回复核前但保留结果，2 FAIL不能变PASS；5个报告保留内容但回到审批前并重新聚合验证；3个放行决定保留原决定事实但撤销当前放行有效性，同时批6/17/25退出可发料/可用门禁，具体库存/质量枚举与已用库存阻塞需确认；4个历史工艺版本取消批准有效性而不修改当前工艺定义；计划633回审批前，冻结标准314有效性撤回后禁止新生产引用。13个请验请求的历史标准摘要、报告项/偏差结果引用及artifact15原PDF不重写，但必须明确重置期间消费者禁止以旧证据继续运行。

最关键边界：业务原始事实可以保留，原“已签证批准有效”的含义不能保留。新增状态或证据有效性合同必须被所有实际消费方识别，UI显示实际待重签/待审批状态，不能依靠界面隐藏或单张旁表让其他消费者仍接受APPROVED/RELEASED。历史签名/摘要/审计保持不可变的现行原则，与物理删除50签名及改7条不可变修订直接冲突；用户必须明确批准这项仅DEV的证据重置例外与恢复方案，不能把role_id整改授权当作该批准。

B的准备/执行边界：先停止并发写入，刷新完整备份并验证恢复，冻结50主键、46业务行及逻辑/共享依赖原值摘要；批准后方可单独处理必要DDL。NOT NULL/CHECK/保护触发器若需调整，应采用新追加迁移与限定对象/字段的受控重置合同，保留普通操作禁止修改保护，不能通用DROP触发器或关闭外键；DDL独立执行并先验证，因为MariaDB DDL隐式提交。随后仅DML事务按已批准对象撤回有效性、更新签名及成对证据列、最后删除固定50条签名；外键保持开启，任何失败ROLLBACK。提交前验证无有效业务仍引用缺失签名、保留值/ID/成员/权限及旧归档不变、结果链无孤儿、库存门禁生效；提交后独立连接复核。没有这些状态/约束合同批准和工具实现，就不能生成声称可执行的DELETE脚本。

签名清理待决：选择保留旧签名的现有替代路线，还是明确批准专项DEV重置例外；重置后哪些业务须重新签署/审批；对7条原不可变结果修订是否允许仅移除签证并保留原值/链路；放行批次是否已有不可撤销消费；恢复验证采用哪种允许的本机方案。范围扩大删除业务只能另行选择，不默认执行。
