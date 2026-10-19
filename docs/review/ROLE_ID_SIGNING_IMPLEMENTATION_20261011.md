# ROLE-ID-SIGNING-001 implementation evidence — 2026-10-11

Current DEV update (2026-10-12): [actual deployment/conversion and remaining failed gates](ROLE_ID_SIGNING_DEV_DEPLOYMENT_20261012.md). V059 already present; formal JAR deployed; actual draft291 PUT committed/version3/schema2. Acceptance BLOCKED; earlier DEV V058/unapplied statements below are historical.


Latest final-code/isolation supplement: [ROLE_ID_SIGNING_ISOLATION_VALIDATION_20261011.md](ROLE_ID_SIGNING_ISOLATION_VALIDATION_20261011.md). Final81/81 plus native migration1/1 and strict85-row service rollback1/1 PASS;1440/375 component gates PASS. Existing isolated schema is nowV059; new backup preserved. Two production files received the explicitly approved canvas-presence compatibility correction; all other33 source hashes and10 frontend hashes remain unchanged. DEV remainsV058/no migration/conversion/deployment. Intermediate failures and the earlier unavailable-isolation statements below are historical, not current outcomes.

**Historical pre-compatibility code result: backend78/78 PASS; mes-boot test-compile PASS; frontend15/15/typecheck/build evidence reused with unchanged hashes. DEV migration/conversion/deployment NOT RUN. Full business/visual acceptance NOT claimed.** The earlier two-round stop below is retained as history and was superseded by explicit bounded authorizations. Latest81/81 and native results are in the supplement linked above.

## Authorization and read gates

Approved source: [minimal design](../development/DCP-ROLE-ID-SIGNING-001-MINIMAL-DESIGN-20261011.md) plus [locked six constraints](../development/decisions/ROLE_ID_SIGNING_DESIGN_LOCK_20261011.md), explicit implementation delegation from parent 01a115d2-8ace-7574-9a08-ac48dfc970cc. Read AGENTS.md, MES_DEV_RULES.md, MES_TASKS.md, relevant UI V2/T3/T5 and database-validation rules, and affected sources. Preserve the dirty checkout on main, including prior EbrDefinitions canvas compatibility and SecurityConfiguration changes. No source copy, commit or push.

## Delivered changes and checks

Additive V059__ebr_required_role_identity.sql: five columns, two role-ID indexes, RESTRICT FKs and ID/name pair integrity CHECKs. No backfill or DML. File SHA256 **142ba90d6d8f852b0f226405cb18ab8d7c3f388b57979b181bef188f5178c6b2**, 1532 bytes. Highest successful DEV migration still58; zero new columns physically present. No deployment/start/restart performed: new code must not run on the old schema before coordinated migration.

Backend: schema2 typed role IDs, server snapshots, lifecycle validation, draft metadata invalidation, legacy JSON omission and frozen reads; readonly role options exact permission; current actor/organization/enabled role/permission checks inside existing transactions; trusted role-ID session format and legacy relogin; schema-versioned immutable role-policy evidence. No generic updateStatus, new role/organization model, username API, grants or signature deletion.

Frontend: existing code input replaced by RolePicker, Chinese labels with ID values and inline label/control. Designer reads enabled paged/search options; controlled save strips snapshots/legacy code. Runtime history uses frozen labels. T3/T5 layouts and Global UI V2 preserved. Username/Chinese account-name suggestions are an independent plan, not implemented.

## Actual validation results

1. Affected backend test-compile: PASS before supplementary tests were added, 13.582s (logs/role-id-signing-compile.log).
2. Targeted backend test run: **71/71 passed**, zero failure/error/skip, Maven 15.609s. Suites: EbrCanonicalCompatibilityTest8, EbrRoleSchemaTest12, EbrRoleAuthorizationTest5, EbrDefinitionRulesTest5, EbrRuntimeBoundaryTest5, EbrRuntimeRulesTest5, CurrentSigningAuthorityTest6, RoleIdSessionCompatibilityTest3, context resolver2, route authorization20. logs/role-id-signing-backend-tests.log and matching Surefire XML. This earlier passing run is not proof that the final tree test-compiles.
3. Frontend: typecheck PASS after repair1; targeted component/model/runtime **15/15 passed**, three suites, 5.38s after repair2; frontend build PASS, Vite18.06s, existing bundle-size warning. logs/role-id-signing-typecheck-round1.log, role-id-signing-frontend-tests-round2.log, role-id-signing-frontend-build.log.
4. Supplementary backend history/separation test-compile: **FAIL** at EbrRoleHistoryAndSeparationTest.java:33, setRevision(Integer) receives 1L. This was the initial stop result; the explicit additional type-only repair and final78-test/boot-compile result below supersede it. No assertions changed.
5. Migration application/validate-after-migration, native transaction revocation/separation integration, live5173/8080 persistence: **NOT RUN**. Initial screenshots were not run at the stop; later source-backed isolated component screenshots are recorded below. The existing5173 endpoint responds200, but that alone is not visual or business acceptance. CUAWright MCP was not callable in this environment; no fallback screenshot test started after the anti-loop stop.

Initial frontend unit startup was blocked by sandbox esbuild spawn EPERM. The bounded unit/build escalation was approved by auto-review and succeeded; there was no auto-review rejection or tool bypass. Maven PowerShell argument-parsing failures were corrected by quoting native CLI property arguments; they executed no tests or database operations.

## Repair accounting and stop

Repair1: duplicate frontend schemaVersion declaration, typecheck implementation error; removed duplicate declaration without changing tests. Repair2: existing empty editor-state helper gained an unnecessary schema property; restored helper shape while actual create/copy/save stays schema2. Existing model acceptance assertion was preserved. The later supplementary fixture compile error is classified TEST FIXTURE ERROR. Two repair rounds are exhausted; no third correction or new validation was attempted. Required next bounded correction would be setRevision(1L) -> setRevision(1), and was not applied at the initial stop. The subsequent explicit parent authorization allowed exactly that type-only repair; final targeted code results appear below. No whole-system PASS/READY/ACCEPTED claim is made.

## Historical hash evidence

Readonly native DEV capture contains exact retained definition rows for templates6/7/9/10: four templates, four forms, four fields; no associated sections/groups/options/rules/signature/review rules. Fixture backend/mes-ebr/src/test/resources/role-id-retained-published-20261011.json, SHA256 **1b4e3d6ab719101bb98dbe839eb7bb31eae154d6ceb980ab520532134c808943**. No credentials or user table data included.

Stored published hashes captured:

| Template | Existing stored hash |
| --- | --- |
| 6 | 69e728d6a98034cf3749becbcd77bfdc21aeb91f4c985aade58981c92411228a |
| 7 | c0c90ce30d984e05d7da20683e6848dae8b7a865a6dc69097dc4461714a7fb02 |
| 9 | c4894c13c1f39bd37584e1622d61d75b38067b18fb94b9708ae1bc4427dbeae2 |
| 10 | deaf79582492a7ae474a72c39fb031e32b2fdfdd5e8947affdd3f23bd25b707f |

Existing template10 golden canonical comparison passed in the 71-test run. The new four-template serializer comparison subsequently passed all4 cases in the final authorized retest, reproducing these exact hashes; independently reread stored DEV hashes also match the captured fixture. Historical authority-free role evidence remains covered by the existing boundary case; the two new multi-role user-ID separation cases and new historical role evidence case subsequently passed in that final run.

## 迁移状态与 V058 归属（完整计划；本轮不应用）

只读核实 DEV 的最高成功版本仍为058。实际 Flyway 行为 installed_rank58、version058、description="retire unused qms object binding"、script=V058__retire_unused_qms_object_binding.sql、checksum=946183528、installed_by=mes、installed_on="2026-10-11 18:08:02"（数据库会话返回时间）、execution_time19ms、success1。本机 V058 源码的逐行 UTF-8 CRC32 为946183528，与历史一致；文件 SHA256为675c4493b796c1923967b165027447564f7c8d613a6b2fcb8c5a6d6e3d813f03。它属于文档记录的 QC 对象关联纠偏：在零行、零入站外键预检后退役 qms_inspection_object_binding 空表。归属依据为 QC_OBJECT_ASSOCIATION_20261011.md 与 QC_OBJECT_ONE_FIELD_ACCEPTANCE_20261011.md；Flyway 只记录执行账号mes，不据此推断具体人员或线程。V055仍保留在不可变历史中。本次未重跑或修改V058。

本次 V059__ebr_required_role_identity.sql 仅有五列、两个索引、两个 RESTRICT 外键和完整 ID/名称配对 CHECK，无回填DML。文件1532B，SHA256=142ba90d6d8f852b0f226405cb18ab8d7c3f388b57979b181bef188f5178c6b2；预计算逐行UTF-8 CRC32=-503587429。DEV中V059历史行0、新列0，与已执行V058无版本冲突；V059 checksum尚无执行历史，部署前仍须以实际Flyway校验。本轮没有迁移、启动/重启或部署。

迁移集成未执行：现有本机规则只给出共享持久 DEV；没有提供可用的既有隔离库名称、连接和保护方案。MariaDB ALTER TABLE会隐式提交，不能把DEV上的DDL称为可回滚测试；本轮不新建数据库、不启动Docker数据库、不使用未授权的其他schema。父任务如提供既有隔离环境和既定保护方案，可在那里执行V059并验证，不触碰DEV。代码和受控保存事务的单元验证不能替代该真实迁移检查。

## 草稿291转换计划（完整范围；本轮不执行）

后续由父任务协调：先重新核验本机schema、表引擎、最高版本、目标状态/版本/ID/引用，建立并核验新的完整可恢复备份，审核V059，再单独应用V059并部署匹配源码。DDL与业务转换不得混入同一事务；新代码不得先运行在没有新列的DEV上。部署后先独立复核四个旧模板canonical/hash及历史签名证据。

转换复用已有受控 PUT /api/v1/ebr/templates/291，If-Match=2、versionNo=2、明确reason、唯一Idempotency-Key，提交完整保留定义的schemaVersion="2.0"。除14条规则的requiredRoleId输入及移除响应快照/旧code输入外，其余请求定义必须与执行前快照逐字段一致。签名180–185和复核269–274映射角色290；签名186和复核275映射角色291，服务端生成当前代码/中文显示快照。任何候选、角色映射、状态或版本变化即阻塞，不扩大候选集合。保持DRAFT，不批准、不发布。

为什么14条规则涉及85行：业务字段目标是14条规则加1个模板根；现有 EbrDefinitions.save 对完整定义逐条调用write，EbrService.save再更新模板根，因此同时维护70条未改变的section/group/form/field。此次没有新增旁路SQL或绕过审核的角色专用命令；优先复用的是能保留子实体ID的受控键匹配保存，不是删除后重建。该既有全量保存的精确范围如下：

| 表 | 准确ID范围 | 行数 | 前后语义与版本 |
| --- | --- | --- | --- |
| ebr_template_version | 291 | 1 | definition_schema_version1→2，version_no2→3；业务version仍1、DRAFT仍DRAFT，hash/approved_by/approved_at保持NULL，流程/编码/名称不变 |
| ebr_section_def | 143,144,145,146,147,148 | 6 | version_no0→1、正常updated_by/updated_at维护；标题、序号、重复策略等业务字段不变；visibility_rule_id现均NULL且保持NULL |
| ebr_group_def | 143,144,145,146,147,148 | 6 | version_no0→1及维护元数据；section归属、groupCode、布局/重复/次数约束不变 |
| ebr_form_def | 417,418,419,420,421,422 | 6 | version_no0→1及维护元数据；均DRAFT保持DRAFT，form.schema_version仍1.0，operation绑定/字段/canvas内容不变；schema_json由既有保存路径重序列化，必须按下述严格比较预检 |
| ebr_field_def | 每个整数ID423–474 | 52 | version_no0→1及维护元数据；form/group归属、字段code/label/type/source、单位/精度、必填/只读、默认/校验等业务字段不变 |
| ebr_option_def | 无 | 0 | 不新增、不删除 |
| ebr_rule_def | 无 | 0 | 不新增、不删除 |
| ebr_signature_rule | 180,181,182,183,184,185,186 | 7 | version_no1→2；required_role保留同一MES_*代码并改作schema2代码快照，新required_role_id为290/291、新名称快照由服务端生成；scope/code/meaning/顺序/重新认证/失效策略不变 |
| ebr_review_rule | 269,270,271,272,273,274,275 | 7 | version_no1→2；同上添加角色ID/名称，复核类型/对象/顺序/独立用户要求不变 |

合计85行：15行有目标业务变化（根1+岗位14），另70行只有正常保存维护/版本变化及既有form JSON重序列化。created_by/created_at、所有主键和原有父子外键不得改变。section可见规则的额外更新只在引用发生变化时触发；本次NULL→NULL应为0次，不允许额外写出计划范围。

子实体ID不会重建：save使用稳定sectionCode/groupCode/formCode/fieldCode以及签名/复核的scope+objectCode+meaning或reviewType+sequenceNo匹配原行，find返回已有实体，write对非NULL ID调用update；retained阻止删除原键，字段/规则ownership检查阻止原归属迁移。转换请求必须保留完整稳定键集合且不得添加新键，因而没有子记录insert/delete。准确候选ID及版本必须在执行前锁定/核对，保存后再次核对，不仅比较行数。

本轮已重新扫描这些目标表的入站外键。候选form的runtime实例0、候选field的value revision0、候选review rule的review record0、模板291的ebr_batch_snapshot和prd_process_snapshot均0；内部引用为52个field→form、52个field→group、6个group→section、6个form/6个section/各7个岗位规则→template。证据为 draft291-incoming-references.json。保留原ID和归属可保持这些引用；后续执行前仍须重新检查物理及逻辑/冻结JSON引用，不能用本轮0计数替代当时预检。

存储ID和schema不会漂移：只由服务器校验角色290/291并设置required_role_id，不从显示代码或客户端快照鉴权。根实体definition_schema_version、读写DTO定义schemaVersion和新canonical根schemaVersion同步为2/2.0；form自己的schema_version和通用签名封套schemaVersion保留1.0，它们是独立契约。旧模板继续definition_schema_version1，不回填新角色列，不在旧canonical注入空字段。

非目标内容保护必须先形成受控保存前后差异，再执行真实事务：记录85行全部原内容及主键/版本，指定允许变化列集合；对其余业务列做原值/字节比较，逐字段核验section/group/form/field和岗位策略。对schema_json同时保留原字符串与完整JSON树：若重序列化仅有空白/对象键顺序差异，仍须明确展示字节差异，不称原字符串不变；任何属性存在性、类型、数值、canvas、操作绑定或字段内容差异都阻塞。如果验收要求该字符串也逐字节不变而既有保存做不到，则不得执行此85行路径，先报告并另审最小保留方案，不能临时直写规避受控保存。

转换保持单个受控业务事务、外键检查启用和既有乐观锁/幂等/审计。只允许新增该保存的审计及幂等证据；不得修改旧审计、50条签名、其他模板、权限或产品数据。提交前核对目标ID/数量/版本/角色快照及所有非目标差异，任何异常回滚；提交不明确先只读查证，不能重放不确定写入。提交后独立连接复核。旧模板6/7/9/10须仍重现既有hash，50条历史签名内容/状态须与执行前保护摘要一致；迁移应用与转换完成均需实际证据后才能报告。

本轮实际仅做代码和只读核验，没有85行转换后结果。DEV草稿291仍DRAFT/version_no2/业务version1，14条岗位规则仍version_no1；50条签名所有已保存字段与此前本机私有保护快照相同，证据 signature-preservation.json（完整HEX快照SHA256=c187ce4f8b61050ccd58c263999846e1d4d19cfd13b313590e4ee707ff225227），没有导出签名私有内容到Git。四个已发布模板的存储hash再次独立复核未变。

## Final authorized retest and remaining gates

Additional authorization: ROLE_ID_SIGNING_FIXTURE_REPAIR_AUTHORIZATION_20261011.md. Only setRevision(1L) -> setRevision(1) changed; every assertion and production file remained unchanged. Final targeted Maven run at19:10:09 workstation time: **78/78 PASS**, zero failures/errors/skips,12 suites,13.997s. Includes all4 retained published hash comparisons, all3 historical/multi-role separation cases, current revocation and legacy session/exact-route checks. Actual current-role tests use controlled mocks, so native concurrent transaction integration is not claimed. Final mes-boot aggregate test-compile also PASS at19:15:14,4.580s; this compile is not a full-system regression. logs/role-id-signing-final-backend-tests.log, role-id-signing-final-boot-test-compile.log and final-targeted-results.json record the final tree; covered source hashes did not change during the run.

Frontend passed evidence reused:15/15, typecheck and build; all10 affected frontend production file hashes equal the prior passed manifest. Final hashes of the three test sources are included in final-targeted-results.json. No frontend production changes were made during the fixture repair.

Readonly screenshots: scripts/role-id-picker-readonly-screenshot.mjs renders the actual RolePicker module from existing5173 with fixture options; all non-GET/HEAD requests are blocked. role-picker-1440.png and role-picker-375.png were captured after inline-label/no-overflow geometry assertions, and visually inspected: Chinese label and select stay horizontal at both sizes. These are isolated component evidence, not authenticated MES page/API persistence acceptance. The script then failed before selection because the ant-select display span intercepted the combobox click; selected-ID assertion was not reached and the runner exit is1. No selector/test assertion repair or retry was applied. The15-pass frontend run already includes ID emission through real Ant component interaction; the screenshot-run interaction failure remains explicitly recorded, not hidden as PASS.

PASS: final backend78, aggregate compile, reused frontend15/typecheck/build, four historical hashes, unchanged signature cells, component static screenshots. FAIL: supplementary screenshot runner interaction locator. NOT RUN: isolated migration application, native transaction race/revocation/separation integration, authenticated full-page screenshot/persistence and DEV migration/conversion/deployment. No established authorized isolated DB was available. Existing frontend large-chunk warning is non-blocking. Independent seven-account naming/QA qualification references remain unimplemented. Runtime status only MES_TASKS.md; full acceptance is not claimed.
