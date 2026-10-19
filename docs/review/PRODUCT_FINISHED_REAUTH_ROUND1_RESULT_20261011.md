# 追加一轮二次认证修复实际结果 — 2026-10-11

BLOCKED / 1 OF 1 ROUND USED / NOT DEPLOYED / NOT ACCEPTED。批准设计记录为PRODUCT_FINISHED_REAUTH_REPAIR_AUTHORIZATION_20261011.md；开始08:07 UTC，10分钟修复/验证预算；本轮失败后停止自动代码修改，保留原断言。

## 已实施范围

RedisReauthenticationTokenStore.java的PUT仍同一个Lua，参数/key类型前置检查、逐字段HSET、首次创建即PEXPIRE300000ms；后续pcall错误清理当前token key。非hash既有key在写前拒绝并保留，CONSUME的原子HMGET+DEL不变。没有认证策略/数据库/权限/Redis版本/配置修改。

QcSpecificationDetailView.vue只读主体标签按subjectType显示产品/物料，原MATERIAL分支、位置和布局保留；新增RedisReauthenticationTokenStoreLocalIT.java提供实际6379测试。

## 精确测试结果

原工作区Maven定向共14项，13通过、1失败、0 skip：ReauthenticationContractTest1/1、PasswordServiceTest5/5、SessionSnapshotTest2/2、ReauthenticationErrorContractTest1/1全部通过。RedisReauthenticationTokenStoreLocalIT真实Redis3.0.504为4/5通过（1.643秒）：

1. 全10字段存储/回读、TTL295000–300000ms、一次消费/重复消费通过。
2. user/session/org/objectType/objectId/meaning/version七个绑定错配逐个拒绝，错误消费后不可再用，通过。
3.应用有效期检查及本次key实际Redis过期通过。
4.8个并发消费者恰好1个成功，通过。
5. wrongTypeFailsBeforeMutationAndInvalidArgumentsLeaveNoKey的非hash拒绝/既有值和TTL保留断言通过；最后的null method断言失败：line96期望store.put(...method=null)抛异常，实际没有抛异常。先前参数数量检查没有拒绝序列化后的空值；正常issue路径固定method="PASSWORD"。本次key均finally清理，仅删除测试自建key，未遗留无TTL令牌、未删共享Redis数据。

失败是新增实际Redis用例检出的存储空方法入参边界，不能改断言强行PASS。当前实现已证明原多字段HSET与Redis3.0.504不兼容问题得到低层验证，但因本轮整体未通过，未打包部署新后端、未尝试真实标准审批，不声称HTTP503已修复。

前端typecheck成功；第一次build受沙箱esbuild进程限制，经正常提权审核后原源码build成功17.60秒，保留既有chunk体积warning，不改配置/阈值。实际标签只读CUAWright截图另存本机；写模式保持false。

Maven两次初始命令因PowerShell参数解析在测试运行前失败，随后正确引用参数才运行上述14项；没有把命令启动失败算测试通过。无新生产代码修复轮次。

## 等待负责人决定的最小范围

建议另批准最多1轮，仅RedisReauthenticationTokenStore.put在任何Redis写入前拒绝method为空/blank，复验相同14项，保留所有断言。该存储前置校验不改变正常PASSWORD认证策略，也不扩大到Redis升级或配置变化。通过后才能原checkout重新打包/短暂重启唯一8080，从标准316/草稿314继续八项真实验收。

当前运行仍旧已部署JAR dd49a987182c57a8c2e406c8710dcf34bfcb16e6a291b3af2dac29cd310bd4c8、DEV57、8080 UP；316/314保留DRAFT，无下游业务链/库存流水。CUAWright临时写模式已关闭并复核false。私有结果及日志位于本机deployment-20261011/reauth-native-round-result.json、reauth-repair-targeted-private.log、reauth-label-typecheck.log、reauth-label-build-approved.log、label-readonly-verification.log，不提交私有数据。
