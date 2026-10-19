# 2026-10-11 七个角色编码规范化执行授权

用户10:03 UTC消息 Sentinel_593fd20289f88191b34aba1cabd5a205：“把那几个的角色编码全部改过来”。10:04 UTC消息 Sentinel_b02d04741a148191bbf2d558bca1ba96：“坚决执行命令，那里改不了就处理那里”。父会话设计负责人审核批准下列精确映射，以及仅模板291的14条可编辑草稿规则同步。

|roleID|旧编码|新编码|
|---|---|---|
|285|DEMO_MES_MASTER|MES_MASTER|
|286|DEMO_MES_WAREHOUSE|MES_WAREHOUSE|
|287|DEMO_MES_PRODUCTION|MES_PRODUCTION|
|288|DEMO_MES_SAMPLER|MES_SAMPLER|
|289|DEMO_MES_QC_ANALYST|MES_QC_ANALYST|
|290|DEMO_MES_QC_REVIEWER|MES_QC_REVIEWER|
|291|DEMO_MES_QA|MES_QA|

模板291：签名规则180–185及复核规则269–274从同一角色290的旧编码改为新编码；签名规则186及复核规则275同步角色291。岗位含义、规则顺序、再认证和职责分离字段不改。依现有草稿保存生命周期递增模板根version_no，保持DRAFT与空批准hash。

授权仅本机hospital_pharma_mes_dev的受控DML事务：候选行锁/旧值与版本断言、外键开启、异常显式ROLLBACK、真实维护来源审计、提交前保护验证和独立连接复核。角色ID、中文显示名、用户ID/用户名/凭据、成员、权限、历史签名、已发布hash与归档保留。账号登录名更改、role_id模式迁移、发布新模板、权限扩张、签名删除、部署与CI修复均不属于此次执行。

首次事务因跨时点sys_user保护摘要不同显式回滚。父会话随后批准同一范围安全重试，要求核查正常登录来源，重新在锁定事务视图内建立完整用户保护基线，不删除断言、不覆盖用户数据。其后查得管理员11556两次正常成功登录，仅version与updated_at变化，身份/凭据/启用状态不变；最初旧摘要未附用户行，无法逐字段追溯，记录此限制。

既有API不能更改role_code，因此按明确批准的最小DEV维护方案执行。新增sys_security_event记录使用actor_user_id=NULL、来源DIRECT_LOCAL_DEV_MAINTENANCE及授权追踪，不冒充业务API或登录用户。受影响行原业务编辑者字段保留，本次维护来源单独记录。完整证据见[执行复核](../../review/ROLE_CODE_NORMALIZATION_EXECUTION_20261011.md)；当前运行状态仅MES_TASKS.md。
