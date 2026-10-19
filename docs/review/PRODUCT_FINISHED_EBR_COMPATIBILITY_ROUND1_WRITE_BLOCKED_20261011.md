# eBR 兼容定向实现与业务写入阻塞证据 — 2026-10-11

SOURCE TARGETED VALIDATION PASS / NOT DEPLOYED / BUSINESS WRITE BLOCKED / NOT ACCEPTED。本阶段为设计负责人新批准最多两轮的第1轮实现及定向验证；没有失败后修复重试。业务命令被自动审批在启动前拒绝，没有绕过或重试，也未进行模板创建、提交、审批、发布、生产下达或下游数据写入。

## 1. 规范依据

实际重新读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、当前BASIC_CURRENT_DEFINITION_CONTRACT、批准的EBR模板名称补充、database-and-validation-strategy及直接源代码/测试。批准边界记录于[兼容与新版本设计](../development/decisions/PRODUCT_FINISHED_EBR_COMPATIBILITY_REVISION_AUTHORIZATION_20261011.md)，来源为设计负责人在源线程01a115d2-8ace-7574-9a08-ac48dfc970cc的明确A–D授权。该设计授权不被冒充为自动审批认可的直接人类业务写入授权。

## 2. 设计差异及实现

历史发布模板10保存schema.form没有canvasDocument，但当前重建器新增null。EbrDefinitions.canonical仅对历史packageVersionId绑定，检查保存发布schema/schemaVersion=1.0、formCode及form.schemaVersion匹配，且发布form没有该字段，才省略重建null。非null不省略；新直接processPackageId定义、历史保存的显式null/真实canvas一律完整纳入。没有目标hash/ID/时间白名单、删字段试hash算法，也没有改保存hash或published比较门禁。

语义核对而非仅代码匹配：操作29/332均配制混合、顺序1、前置空、完成规则/角色/设备空、清场0、IPC空、参数0条；历史/当前配方均1000ml、物料1需20g、超量0。原模板只有MIX_RECORD/OBS，MANUAL STRING必填；无表达式/单位/选项/其他引用/规则/签名规则/复核规则。因此已批准的新版本内容可以按现有功能映射29→332。尚未执行该业务版本创建。

## 3. 修改与保留范围

- EbrDefinitions.java：仅canonical投影；接口、审批状态机、权限、签名/审计和hash门禁不变。
- EbrCanonicalCompatibilityTest.java及legacy-template10-canonical.json：固定历史样本/原hash，实际JCS摘要和published篡改拒绝回归。
- EbrPublishedCanonicalLocalIT.java：真实DEV只读消费验证，MOCK web不占新端口、readOnly事务回滚；测试专用禁Flyway避免只读验证触发迁移，不代替迁移检查。
- 设计授权记录、BASIC_CURRENT_DEFINITION_CONTRACT的有界补充链接、本报告、MES_TASKS.md。

无数据库迁移，实际最高V057及055/056/057校验码不变。未改.env/local/ci配置、系统授权或其他用户源代码，未Git提交推送。未重新打包或部署；8080仍为原PID34308、原JAR3aaf266a…b053e，health200。不能称线上兼容缺陷已修复。

## 4. 测试、备份、拒绝和数据证据

1. `mvn.cmd -o -pl backend/mes-ebr -am -Dtest=EbrCanonicalCompatibilityTest,EbrDefinitionRulesTest,EbrRuntimeBoundaryTest -Dsurefire.failIfNoSpecifiedTests=false test`：18/18 PASS、0 skip；8项新兼容回归+10项既有规则/边界。suite1.672/0.132/0.407秒，命令8.618秒。覆盖原deaf7958…707f精确复现，新显式null保留、新canvas正常读取及篡改拒绝、历史新增canvas拒绝、规范化字段篡改拒绝、保存发布证据缺失/不匹配不触发兼容。
2. `mvn.cmd -o -pl backend/mes-boot -am -Dtest=EbrPublishedCanonicalLocalIT -Dsurefire.failIfNoSpecifiedTests=false -Dmes.local.ebr.canonical=true test`：真实nativeDEV1/1 PASS、0 skip，suite16.29秒/命令29.008秒。实际源码EbrQueryService.requirePublished(1,10)成功返回原hash；仍为旧packageVersionId12，不伪装current绑定，原模板行/保存schema不变。并非8080 HTTP部署验收。
3. 新完整备份：用户本机Temp/mes-quality-plan-root-review-20261011/deployment-20261011/ebr-version2-prechange/hospital_pharma_mes_dev-full-20261011T090633Z.sql，5953960字节，SHA256 `d0ab4aca617ed67f2c98fb15991616e4bce2ec24e8d230d817723fb9b15e3249`，dump exit0。138表/4109行全部存储字节、结构和触发器经离线完整重建比较一致。本次新备份没有实际导入恢复，没有创建第二数据库；恢复导入NOT RUN，不冒用早期备份恢复结果。
4. 25张保留表与09:06新备份的完整存储字节在单个只读一致性快照中一致，显式ROLLBACK。物料16/FINISHED0/产品2/FK1；模板家族只有10/version1/EFFECTIVE/versionNo4，原hash不变；230仍DRAFT/version0/snapshotNULL；633仍APPROVED/signature2021/version1。无新模板、无下游虚假记录。
5. 自动审批在CreateProcess启动前拒绝命令：通过正常API从模板10创建版本2、保存/lint并提交。原始理由："This creates, edits, and submits a persistent regulated eBR template via API, outside the explicitly authorized database-cleanup task; no trusted user authorization covers that business-side effect." 明确禁止换方式间接执行。没有命令进程启动、没有登录/业务API调用。需要原用户对该受监管写入范围提供直接确认。
6. 独立批准依赖：实际角色权限/用户角色查询，只有admin(11556)/SYSTEM_ADMIN拥有ebr:template:approve；其他现有验收账号没有此权限。新版本由admin创建/提交后，EbrService正常门禁禁止其自批。即使业务写入授权被确认，仍需要一个现有具备模板审批权限且不同于创建/提交人的授权账号；不临时/永久授予权限，不伪造批准。没有发出自批试验或给QA加权限。

## 5. 结论与剩余门禁

兼容源代码首轮定向验证19/19 PASS，审核在此实现范围未发现CRITICAL/HIGH；未部署，生产直绑定门禁和审批账号依赖仍保留。业务版本2及整个下游为BLOCKED：自动审批需原用户直接授权，且缺少独立模板批准账号。没有失败后修复重试；不以余下轮次越过授权、审批或设计边界。

原八项状态未扩大：标准314/计划633批准有证据；批次230建立有证据；独立库存空列表/目标页面有证据；非正式打印27已有GET/hash证据。新模板发布、下达、执行/OUTPUT、独立入库/QC报告PDF/QA、发运/幂等并发均未运行。历史eBR归档0条仍为无样本/不适用，不是PASS。需确认上述两项依赖后，按正常服务继续；不得重建标准/计划/产品/批次或改旧模板hash。

本机私有证据：ebr10-operation-semantic-preflight.json；ebr-version2-prechange目录的备份/快照/恢复性比较；ebr-compatibility-round1-tests.log；ebr-compatibility-round1-native-readonly.log；ebr-compatibility-round1-final-readonly.json（25表比对、连接/迁移、模板/批次/计划及源hash）。未提交私有备份或凭据。
