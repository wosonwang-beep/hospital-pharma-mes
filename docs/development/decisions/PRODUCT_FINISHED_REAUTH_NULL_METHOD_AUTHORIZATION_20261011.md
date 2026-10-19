# 二次认证存储 method 前置校验 — 2026-10-11

APPROVED / FROZEN。父任务01a115d2-8ace-7574-9a08-ac48dfc970cc的设计/验收负责人明确批准Section6A再次追加仅1轮，范围为任何Redis写入前拒绝null、空串、空白method。08:18 UTC开始，定向修复验证预算10分钟。前轮14项13通过/1失败记录保留，本轮不自行追加。

DESIGN LOCK：在RedisReauthenticationTokenStore.put最前部以null/isBlank校验拒绝无效method，合法method原样保存，不trim、不重新定义合法方法、不增加认证策略。保留原单一Lua写入原子性、全部绑定、摘要key、300000ms TTL、原子一次消费和错误处理。无数据库迁移、配置、Redis升级、业务模型、权限或签名契约变化。

写范围仅RedisReauthenticationTokenStore.java及既有RedisReauthenticationTokenStoreLocalIT.java。保留原失败null断言，另加参数化null/空串/空格/tab/newline负例，每项断言失败后没有key；原相关安全测试和实际localhost6379验证全部重跑，不能mock替代或降低断言。唯一短期自建key由finally清理。

通过后沿原07:40批准从同一checkout打包、重启唯一8080，实际核验部署版本和HTTP二次认证，再从标准316/版本314继续固定八项真实业务验收。未部署不得宣称上线；再次失败先报告精确根因和结果。前轮PRODUCT中文标签修改维持，不新增UI设计。
