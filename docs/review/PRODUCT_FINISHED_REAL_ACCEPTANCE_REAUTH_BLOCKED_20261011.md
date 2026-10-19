# PRODUCT_FINISHED 真实验收二次认证阻塞 — 2026-10-11

BLOCKED / NOT READY / NOT ACCEPTED。已部署版本及备份保持上一阶段证据，不回退数据库，不重建已生成记录。此阶段未自动修复业务代码。

## 实际失败与根因

独立审批账号demo.qc.reviewer（既有权限）登录及identity HTTP200；POST /api/v1/auth/reauth 对 QcSpecificationVersion:314 / business version 1 / APPROVE 返回503 DEPENDENCY_UNAVAILABLE，traceId d6e425d0-a45b-4cfa-8be8-b71dc7a638ec（2026-10-11 07:59:41 UTC）。

实际日志：RedisReauthenticationTokenStore.put:35 → ReauthenticationService.issue:39 → RedisSystemException；根因 RedisCommandExecutionException，Lua第1行 Wrong number of args calling Redis command。本机127.0.0.1:6379只读INFO确认Redis3.0.504/Windows/standalone。存储PUT Lua使用一次HSET写10个field/value；该Redis版本不支持这种多字段HSET语法。[Redis官方HSET历史](https://redis.io/docs/latest/commands/hset/)明确多field/value参数从4.0.0开始支持。

分类：现有平台二次认证存储的本机Redis兼容实现错误；不是QC模型/签名设计冲突，不是凭据或审批权限不足。此前隔离全链使用mock ReauthenticationPort，未覆盖此真实Redis路径，不能拿隔离PASS替代本次失败。

## 已提交的有界业务记录

仅创建一套PRODUCT质量标准：root316，specificationCode=KCL30-QC-20261011，productId1，materialId=NULL；version314，DRAFT，definitionSchemaVersion2，business version1，approval_signature_id=NULL。标准名称为氯化钾溶液成品质量标准；业务字段未夹带测试/演示/模拟，验收身份记录在本机数据集元信息。

尚未批准标准、创建批次/内部指令、计划、库存流水或下游业务。保留草稿继续使用，不删除/重复创建。正常登录产生安全事件和用户version/updated_at变化，密码/身份/权限未改。

本次独立只读复查产品/物料/系统字典及权限等14张保留表全部内容无差异；物料16、FINISHED0、产品2、成品流水0、foreign_key_checks1。上一阶段402外键无孤儿。8080仍为PID6372、JAR SHA-256 dd49a987182c57a8c2e406c8710dcf34bfcb16e6a291b3af2dac29cd310bd4c8；DEV成功迁移57。

## 固定八项当前状态

| 项 | 状态 | 实际覆盖及未执行部分 |
| --- | --- | --- |
| 1 同源服务/HTTP/权限 | PARTIAL | 唯一进程、JAR版本、V057、health/5173及7个既有角色登录/identity通过；业务权限拒绝门禁未完整执行 |
| 2 PRODUCT标准/V2计划 | FAIL / PARTIAL | PRODUCT标准及草稿真实持久化，主体正确；独立签名reauth503，审批/计划/错配拒绝尚未执行 |
| 3 指令/执行/OUTPUT | NOT RUN | 前置标准审批受阻；未创建批次 |
| 4 独立入库/重放 | NOT RUN | 前置生产链未完成 |
| 5 检验/新报告/PDF/QA | NOT RUN | 未生成新报告/PDF，不再生或覆盖旧报告 |
| 6 发运/重放/超库存并发 | NOT RUN | 没有验收成品库存，未执行发运 |
| 7 新来源链/旧归档读取hash | NOT RUN | 未创建新批次；旧文件读取/hash仍待执行，历史未修改 |
| 8 双视口/UI | PARTIAL | QC列表1440×900和390×844各4对标签同行，无横向溢出；生产列表桌面6对通过；其余页面/批准参考对照未完整执行 |

视觉发现：/quality/specifications/316在390×844实际显示PRODUCT值，但详情静态标签仍为“物料”，与PRODUCT主体语义不一致；源文件QcSpecificationDetailView.vue的只读详情标签固定为物料。已留截图，不自行改变设计或标签断言。

## 具体追加修复范围（未批准、未实施）

此前原2轮+追加3轮+再追加2轮已用完。MES_DEV_RULES.md Section6A规定增加修复轮数必须先获用户明确批准；本次部署授权要求超额度先报告根因/范围。

请求追加最多1轮，仅针对本次直接失败：

1. backend/mes-security/src/main/java/com/hospital/mes/security/reauth/RedisReauthenticationTokenStore.java：同一个PUT Lua内逐字段HSET，保持原10字段绑定、随机令牌摘要key、5分钟TTL、现有原子消费/一次性/有效期/对象及版本检查；不升级/替换Redis、不改密码/权限/配置、不引入迁移或签名模型变更。增加或运行必要的定向签名/令牌测试，使用原6379验证临时唯一token并只消费本次token，不刷新/删除共享Redis数据。原checkout构建并短暂重启8080，然后从316/314继续原清单。若仍同签名失败立即停止。
2. 若同时批准已发现的UI差异，只改frontend/mes-web/src/views/quality/QcSpecificationDetailView.vue只读主体标签随subjectType显示产品/物料；不改字段顺序、布局、主体存储或测试阈值。未批准前保持原源文件。

CUAWright临时写模式已关闭：最终browser_status mutating_requests_allowed=false、browser_active=false；全局MCP配置不变。无正在提交的事务，未推送Git，未改生产库、权限或历史签名/归档。

私有证据目录：C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\deployment-20261011，包含real-http-evidence.jsonl（凭据/token脱敏）、business-chain-state.json、real-chain-root.log、reauth-blocker-retention-check.json、cuawright-mcp-calls.jsonl及后端trace日志。截图留本机cuawright-mcp/evidence：1791705599325_product_qc_standard_1440.png、1791705621045_product_qc_standard_390.png、1791705667840_product_qc_standard_316_390.png。
