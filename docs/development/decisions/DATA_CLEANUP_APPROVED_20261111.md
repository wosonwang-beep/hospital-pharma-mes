# 2026-11-11 DEV 构造数据清理授权与执行边界

用户原文：“把所有的垃圾无意义的数据全部删除”，随后明确“批准删除数据”。目标仅本机 localhost:3306/hospital_pharma_mes_dev，当前main唯一工作区。用户另问为何使用AI Commander，已解释沿原被拦截通道重试以避免绕过；原通道在新授权后实际成功。不得把历史拦截继续描述为当前永久不可操作。

权威：AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md及数据库策略；FINAL BASELINE v1.0.23及已批准补充。此次是已确认来源的DEV构造记录清理，不变更数据库结构、业务字段、状态机、API、权限或UI，无Flyway迁移。原真实业务资料、原始收货/取样/出库数量、既存审计和有效原始采样签名保留。

## 精确删除与一致性恢复

1. NativeConcurrencyIT遗留：引用private-data-audit/cleanup-20261111/20261111-180236/inventory.json，SHA256 eded5e7232cae79c442b95be26fa67a7ee42818a64947719a6e02972a9006d92。逐ID/创建人确认2929条构造记录，仅测试账号iq_*及其独立夹具；不是按时间批量清库。外键外部引用盘点0。3条字段值291/292/293先行事务删除，其余2926条61表后续同一事务删除。只有测试IPC实例7的current_result_revision_id=7形成循环，事务内先将该可空指针置NULL后删除双方；不关闭FK。纯构造的审计/签名随独立夹具删除，清理前备份及盘点证据保留，不删除原业务审计/签名。
2. 旧临时QC标准1065/1066，编码E2E-TABS-309768、E2E-DBG-01066；对应item1178/1179、revision409/410/792/793、scope367/368，共10条。未发现清单外依赖，按子表到父表删除。
3. 六批合成检验结论：DCP-QC-SIMPLE-DEV-ENTRY-20261110.md、QC-SIMPLE-DEV-SIX-LOT-CLOSEOUT.md确认其来源是DEV软件构造值，非原始检验读数。删除执行603–621（19条）、空报告833–844（12条）；先前已删的结果/报告明细/QA决定不重复计数。
4. 删除合成结论必须同时解除遗留的QC通过、报告批准、物料放行状态，否则会留下更严重的不一致。依据QC-QA-EVIDENCE-GATE.md及IncomingQualityService.start的既有语义，任务771–776恢复IN_PROGRESS/EFFECTIVE，清除其已删除结果的提交/复核指针；请验721–726恢复IN_PROGRESS/EFFECTIVE；对应六检验样品1419/1421/1423/1425/1427/1429恢复RECEIVED；物料批1121–1126恢复TESTING/BLOCKED。仅移除构造结论，不删除原请求、任务、物料批、取样或库存数量，不回退version_no。
5. 六批对应37条构造结论签名4691–4727注销为INVALIDATED，保留原签名行、摘要、认证证据及原始审计。无代签，不伪造合格/放行。除这37条外的原有采样/仓储等签名不变。所有可变记录version_no递增，清理操作者元数据为现有admin11556。清理报告是本次维护操作的来源记录，不称实际药品质量验收。

## 备份、事务与证据

执行前全库备份：D:\codex\_project\gmp\_backups\hospital-pharma-mes\hospital_pharma_mes_dev-approved-delete-20261111-181317.sql，9819128 bytes，SHA256 46df7dd7c36b5330f8f716c8c37caf06dea30126405c33eb4f8f93ba156419e5。

并发夹具SQL SHA256 5e6f4cc962282f011319989a94af796a3a16809e760bf209ce209ac7b7b8a323；剩余清理SQL SHA256 26d123693d36a15f423f40c58628fc417cfb77e05082be26e1c01bd8afc831de。均使用固定库、逐ID身份/版本校验、行锁、清单外引用检查、行数断言、异常全事务回滚、FK始终开启。不创建/删除触发器、不改Flyway历史、不重建/清空数据库。

文件白名单：private-data-audit/cleanup-20261111/下计划/SQL/前后快照/执行日志，本决策、同日执行报告及MES_TASKS.md追加记录。不得修改业务源码、迁移、用户已有未提交文件或提交/推送Git。

本记录形成于2929条独立并发夹具删除后、剩余41条删除及六批一致性恢复前，不伪称在全部执行之前已存在。运行进度及验收状态只维护MES_TASKS.md。
