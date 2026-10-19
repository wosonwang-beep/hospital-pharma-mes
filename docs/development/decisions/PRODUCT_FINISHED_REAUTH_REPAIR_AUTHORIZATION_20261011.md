# PRODUCT_FINISHED 二次认证兼容定向修复设计记录 — 2026-10-11

APPROVED / FROZEN，父任务01a115d2-8ace-7574-9a08-ac48dfc970cc作为已获用户授权的设计和验收负责人，明确批准MES_DEV_RULES.md Section6A追加最多1轮。原2+3+2轮及失败历史保留，本轮不自动追加。开始08:07 UTC，定向修复/验证预算10分钟；真实业务八项清单另按已批准部署执行范围持续，不自动扩大修复预算。

仅修复Redis3.0.504的PUT令牌写入兼容性和PRODUCT详情主体中文标签；保持全部身份/会话/组织/用途/对象/版本绑定、摘要key、5分钟TTL、一次性原子消费和现有错误处理。无业务/API/权限/数据库迁移/配置变化，不升级Redis、不改认证策略，不创建长期凭证。

Lua命令支持：实际服务器只读INFO=3.0.504；HSET单字段、TYPE、DEL、HMGET、PEXPIRE均在该版本已有。PUT仍单一Lua原子执行，先检查参数数和key类型；逐字段HSET，首次创建即附300000ms期限；pcall捕获后续异常并删除仅本次令牌key，禁止留下无TTL的部分记录。CONSUME原子HMGET+DEL保持不变。对非hash既有key在写入前拒绝且不删除。

写范围：RedisReauthenticationTokenStore.java、QcSpecificationDetailView.vue；为明确批准的真实Redis覆盖新增专用RedisReauthenticationTokenStoreLocalIT.java，运行已有相关安全回归，断言不放宽。真实Redis测试仅6379、唯一短期token，finally仅消费本次token，无数据库/身份/权限修改；报告记录测试元信息，不写业务测试标记。

标签只按实际subjectType显示产品/物料，MATERIAL分支保持，字段顺序/布局/来源不改。使用既有标准316、版本314继续，不重复创建。构建、唯一8080短暂重启与CUAWright会话写模式已获既有部署批准，结束核验关闭；若本轮失败先返回具体结果，不绕过签名或跳过门禁。
