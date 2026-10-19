# 二次认证第二轮已部署；质量计划HTTP入口阻塞 — 2026-10-11

REAUTH TARGETED PASS / DEPLOYED / PLAN HTTP403 BLOCKED / NOT ACCEPTED。八项整体验收没有PASS；前轮失败及历次追加记录保留。

## 1. 基线依据

本轮实际读取AGENTS.md、MES_DEV_RULES.md（含6A）、前轮设计记录、直接安全/API契约。负责人明确批准再追加仅1轮，锁定[method前置校验授权](../development/decisions/PRODUCT_FINISHED_REAUTH_NULL_METHOD_AUTHORIZATION_20261011.md)。08:18 UTC开始，定向修复验证预算10分钟；08:26 UTC前完成定向验证和新阻塞分析。唯一8080打包重启沿用07:40批准，无新增业务设计或系统权限。

## 2. 设计差异

RedisReauthenticationTokenStore.put在任何Redis调用前以null/isBlank拒绝method，抛IllegalArgumentException。合法字符串原样保存，没有trim或重定义合法方法。上一轮单一Lua单字段HSET兼容实现、300000ms TTL、摘要key、身份/组织/会话/对象/用途/版本绑定、原子一次消费和错误处理保持。

## 3. 修改范围

本轮仅修改RedisReauthenticationTokenStore.java和RedisReauthenticationTokenStoreLocalIT.java。原null失败断言保留，新增参数化null、空串、空格、tab、换行和混合空白6项异常类型/消息及无key断言。无新增迁移；V054–V057 checksum仍为-746231051/1211285628/-2096839805/1304788399。原PRODUCT详情中文标签修复随原checkout构建保留。未修改.env/local配置、账号权限、合法方法、Redis版本或签名约束，未提交/推送。

14张保留主数据/字典/角色/权限关联表重新逐行存储字节比对，完整内容全部不变；物料16、FINISHED0、产品2、foreign_key_checks1。正常登录按原策略更新sys_user登录版本/更新时间并追加安全事件，不宣称整个用户行不变。本轮未重跑全部402个FK孤儿扫描，不以历史检查冒充本轮结果。

## 4. 验收证据

执行`mvn.cmd -o -pl backend/mes-security -am '-Dtest=RedisReauthenticationTokenStoreLocalIT,ReauthenticationContractTest,PasswordServiceTest,SessionSnapshotTest,ReauthenticationErrorContractTest' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dmes.local.real.redis=true' test`：20/20 PASS，0失败/错误/跳过，13.134秒。真实6379 Redis3.0.504为11/11（2.564秒），相关安全回归9/9。覆盖10字段、295000–300000ms TTL、七类绑定错误、过期、8并发消费者恰好1成功、非hash拒绝/保留原值、原null及6个新负例；只创建短期唯一key，finally清理。无mock代替。

原checkout执行`mvn.cmd -o -pl backend/mes-boot -am '-DskipTests' package`成功21.643秒（安全测试先通过）。核验旧PID6372完整启动命令后停止它，Java21、原local/root.env启动唯一8080 PID31552，08:20:43 UTC。JAR SHA256：`4a15fc28b70d40bb36e929a96735767ea493e242c9c3d1ed5cd486bcac8a5cfa`。独立复核8080唯一监听PID31552、health UP，5173 HTTP200。因此本轮二次认证修复已部署。

真实HTTP /auth/reauth200；独立QC审核人批准既有版本314返回200，数据库APPROVED/version_no1/approval_signature_id2020。标准316没有重建。正常主批次230（KCL20261011001/product1/schema2）创建201，数据库DRAFT。验收辅助脚本先误读token，按既有ReauthenticationResponse精确改为reauthToken；默认Python缺win32cred，改用已有CUAWright Python/Windows凭据组件，无安装或凭据泄露。均属调用纠正，没有改产品契约或降低断言。

新阻塞：管理员实际/auth/me含qms:plan:create/update/approve/view，POST /api/v1/quality/production-plans仍403 FORBIDDEN，trace `08605eb9-4a3e-404b-8f2e-428d6a57865f`。SecurityConfiguration.java:216仅配置计划GET=view，缺POST=create、PUT /{id}=update、POST /{id}/approve=approve，最终:417 anyRequest().denyAll()。Controller和Service已有对应操作及上述既有权限校验。不是账号缺权限，也不是Redis故障。批次230计划0行、成品流水0，后续下达/执行/入库/检验/放行/发运未执行。

固定8项：1服务/登录/二次认证PASS，计划写入口FAIL；2PRODUCT标准批准PASS/V2计划BLOCKED；3批次创建PARTIAL/下达执行OUTPUT NOT RUN；4入库重放NOT RUN；5检验新报告PDF与QA NOT RUN；6发运并发NOT RUN；7溯源eBR旧档案hash NOT RUN；8原两视口标签证据保留，整链页面参考对照NOT RUN。CUAWright临时写模式保持关闭。

私有证据在本机`C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\deployment-20261011`：reauth-round2-targeted.log、reauth-round2-package.log、reauth-round2-launch.json、reauth-round2-prechain.json、reauth-round2-real-chain-root.log/resume.log/native-runtime.log、reauth-round2-postchain-state.json、reauth-round2-retention-check.json、redacted real-http-evidence.jsonl。07:46完整备份及既有恢复证明保留；本轮无DDL/恢复。私有证据和备份不提交Git。

## 5. 结论与待审最小范围

BLOCKED，停止后续业务写入。二次认证定向修复1/1轮已通过并部署；新路由映射遗漏超出本轮写范围，未自动修复。

请负责人决定仅补齐上述3条现有操作到既有权限的精确匹配，并增加实际允许/拒绝验证。禁止泛化authenticated、grant、新权限、弱化denyAll或绕过服务签名/独立审核。批准后从316/314/230继续，不重建或删除正式业务证据。
