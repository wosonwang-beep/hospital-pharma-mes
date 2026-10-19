# 七个角色编码规范化 — 2026-10-11 执行与独立复核证据

范围/用户授权见[精确授权记录](../development/decisions/ROLE_CODE_NORMALIZATION_AUTHORIZATION_20261011.md)。本次读取AGENTS.md、MES_DEV_RULES.md及数据库验证策略，在原main工作区使用已有本机MariaDB访问方式；凭据未输出、未提交Git。实际schema为hospital_pharma_mes_dev，主机PC-202401011703，端口3306，候选表InnoDB，外键始终开启；无DDL、迁移、部署、权限增删或签名删除。

## 实际事务结果

第1次因准备摘要与稍后正常登录更新后的sys_user不一致，SQL异常处理器显式ROLLBACK；独立复核22个候选/根行原值完全相同、维护审计0条。原较早摘要无旧用户行，不能臆测其精确字段。后续逐字段比较10:15备份与重试前状态，确认仅管理员11556的version402→404和updated_at变化；安全事件924/925为对应成功LOGIN，用户名、密码摘要、启用/锁定状态及其他用户字段不变。

按设计负责人批准第2次执行：锁定用户/审计/候选行，采集同事务完整用户摘要并在更新后再次验证，角色和权限等保护继续比对；保留全部断言。事务成功COMMIT。结果解码器因MariaDB将@@foreign_key_checks在JSON_OBJECT中序列化为未加引号的ON而报错；没有重放DML。只读查询以显式数值转换读取该标志，独立连接证实提交事实与全部保留检查。TIMESTAMP保护摘要按准备时同一SYSTEM时区核验；另以UTC逐存储字段核验Flyway历史无变化，不将显示时区差异当作数据修改。

|表|实际更新行数|结果|
|---|---:|---|
|sys_role|7|roleID285–290版本2→3，291版本3→4；精确编码映射正确|
|ebr_signature_rule|7|180–186的角色引用同步；version_no0→1|
|ebr_review_rule|7|269–275的角色引用同步；version_no0→1|
|ebr_template_version|1|仅291草稿根元数据version_no1→2及更新时间；DRAFT、content_hash/批准信息仍为空|
|sys_security_event|22条追加|审计ID926–947；来源DIRECT_LOCAL_DEV_MAINTENANCE，actor_user_id均NULL；无伪造API事件|

除编码/引用、乐观锁版本与更新时间外，候选行其他字段逐字段不变。成员9条、权限关系353条的完整内容和ID不变；全部50条签名完整内容不变且50条仍VALID；历史业务审计、已发布模板、产品/物料、字典、归档等保留内容不变。独立只读一致性事务检查137组保留数据，无摘要差异；402个外键约束检查0孤儿。当前物料/产品及已签署记录未因角色改名清理。会话未自动刷新，受影响账号需正常重新登录后使用新编码配置；用户名和凭据未改。

## 本机恢复和验收证据

本机证据目录：C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\role-code-normalization-20261011。

完整备份hospital_pharma_mes_dev-full-20261011T101543Z.sql：5,947,380 bytes，SHA256 4a19a2f1d5f6e35e62bda8a92c869b8fc3792014e0d001ade0c613a709810ded。137表、4112行全部存储字段字节、SHOW CREATE结构和触发器离线重构一致；未实际导入恢复。备份以后的两次正常登录增量单独记录，不能盲目全库还原覆盖该历史；候选22行原值HEX恢复证据与审计均保留。

关键文件：before-rows-hex.json、backup-integrity-verification.json、sys-user-drift-review.json、rollback-independent-verification.json、transaction-retry.sql、retry-plan.json、postcommit-private-readonly-snapshot.json、final-commit-verification.json。包含凭据摘要的完整恢复快照仅留本机，不写入仓库。数据库受控事务与独立验证证据确认COMMIT；无浏览器/功能验收或人工ACCEPTED的扩大声明。role_id独立设计未实施，签名物理清理另案未执行。运行状态仅MES_TASKS.md。
