# 产品成品 B：三轮有界追加修复报告

结论：**BLOCKED / NOT READY FOR ACCEPTANCE / NOT ACCEPTED**。父会话明确批准本任务追加最多三轮，原两轮失败历史及原 §6A 全局规则均保留；本次三轮已用尽，停止代码修改及重测。

## 1. 依据与授权

已物理读取 AGENTS.md、MES_DEV_RULES.md、数据库验证策略、B 冻结记录和前次阻塞报告。批准来源：父会话 `01a115d2-8ace-7574-9a08-ac48dfc970cc` 本轮转发的设计/验收负责人授权。范围仍为产品 + MainBatch 的已批准整改，不写 DEV、不重启、不推送；复用已有 native 隔离库。详见 [有界例外记录](../development/decisions/PRODUCT_FINISHED_EXTRA_REPAIR_AUTHORIZATION_20261011.md)。

## 2. 已闭合的源代码差异

- QC 增加 `getProductHistorical`；MATERIAL 历史读取和 snapshot 拒绝 PRODUCT/schema 2，保留原物料 canonical 分支。新增三个专用读取单元测试。
- V2 计划创建/编辑/审批要求新模式 DRAFT 批次；产品快照读取再校验模式，已有产品/标准一致性检查保留。前端计划选择器使用 mode 2 + DRAFT，未取得派生产品时不回退物料标准查询。
- 内部 Order 不投影独立 EDIT；产品 OUTPUT 不显示旧 materialLotId，切换 OUTPUT 清除旧输入。
- 两个原成品种子入口统一复用 rollback-only 的真实服务链 fixture，移除 @Commit、17/18/FINISHED 物料重建及独立 Order 创建。目标物料、产品、单位、成品库位按业务编码解析。隔离 URL 在 Spring datasource 初始化前严格校验，拒绝 DEV。
- 全链 fixture 调用实际 Process/eBR/Production/QC/QMS/WMS/QA 服务与数据库；仅身份上下文和再认证端口采用测试替身。第 2 轮业务链已通过，但第 3 轮扩展 fixture 失败，不能报告最终全链 PASS。

## 3. 迁移、文件与保留数据

原本任务未执行 V055 改为 V056，SQL SHA-256 保持 `416e07ac901bfa9895dfa52230331fbee4650c53e5fa14c418470c02a53f38d6`；另一任务 V055 保持 `93b07ae911e92131bb779825ae3df059ff33a99297dfca3fac42359a33fbdf23`。改号前 DEV 和隔离库均止于 V054；仅检查可访问本机环境，没有声称核验远程库。移除的 target/classes 旧资源与本任务旧迁移字节完全相同。

第 1 轮隔离库执行 V055/V056 后，真实链路暴露原 QC 插入触发器只校验 material 的兼容缺口。V056 此后保持不可变；使用追加 V057 为同一个触发器保留原 MATERIAL 组织条件和错误消息，增加等价 PRODUCT 同组织条件及未知主体拒绝。没有放宽原物料分支、签名、审计或权限。V057 仅隔离库应用；DEV 未应用 V055–V057，未运行 repair。

新增/本轮修改路径及 SHA-256 见 `docs/review/PRODUCT_FINISHED_B_FILE_REVIEW_20261011.json`。此清单区分本轮实际修改和前阶段范围相关文件；共享工作区中其他并发文件不得认作本任务所有。用户配置和并发供应商/QMS 实现保留。

## 4. 实际验证

| 门禁 | 结果 |
|---|---|
| 后端源及测试编译 | 三轮 Maven reactor 均进入真实测试；最终 testCompile 成功 |
| 前端 typecheck | 本轮 PASS，退出 0；无真实 UI 验收 |
| QC 证据/规则/历史分支 + WMS 规则/流水单元 | 三轮各 19 项 PASS，0 skip |
| 第 1 轮真实链 / 迁移 IT | ERROR：QC 旧触发器 / fixture 错查 sys_user.org_id |
| 第 2 轮真实链 | PASS：995 OUTPUT、入库 995、检验、QA、发运 300、余量 695；事务回滚 |
| 第 2 轮迁移 IT | FAIL：已编译测试仍期望 56，实际 57；源同步构建时序错误，保留证据 |
| 第 3 轮迁移 IT | PASS：V057、产品/物料/签名保留、旧 1:N、新 1:1、主体 XOR、旧/新同组织拒绝、未 OUTPUT 禁库存、事务回滚 |
| 第 3 轮扩展真实链 | ERROR：外层测试事务 rollback-only，QA review 无法建 savepoint |
| 并发、HTTP/真实 UI、最终打印/eBR 读取 | NOT RUN / 未执行到；不可拿第 2 轮成功覆盖第 3 轮失败 |

三轮原始结果：本机 `C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\extra-round-{1,2,3}-result.json`；完整日志同目录，保留本机。迁移协调、源摘要、触发器原文、最终只读复核亦在此目录。原备份不新增、不提交。

## 5. 最小根因与剩余影响

第 3 轮 fixture 在单个 Spring 测试事务内执行预期异常断言 `specificationHistory.getHistorical(...)`。该调用经过 `@Transactional(readOnly=true)` 代理，业务拒绝异常被断言捕获后，外层事务仍已标为 rollback-only。随后 `FinishedReleaseService.collectBlocker` 建 savepoint 报 `Cannot create savepoint for transaction which is already marked as rollback-only`。这是新增负向测试的事务组织问题，已经由独立历史分支单元测试证明确实拒绝；不是允许放宽业务读取断言的理由。

最小后续修正是将预期拒绝检查放在独立事务边界，或在正向链完成后检查并确保回滚；保留断言，再执行一次迁移/真实链及追加打印/eBR 证据。当前不再修改，额度已用尽。

当前打印目录仅有原入厂检验、eBR 表单及 WMS 收货 provider，没有成品检验报告专用 provider。本轮复用 eBR provider 的实际读取检查未执行到；没有新增打印类型、权限或配置来绕过缺口。若业务验收要求成品检验报告新打印类型，需要父任务确认其既有批准边界。历史存储 hash 的只读对比不能替代历史运行时 canonical/archive 再生对比。

OpenAPI 已按 B 身份去除独立成品 lot、同步 MainBatch/计划/QC/OUTPUT/入库/发运/链查询契约并校验本地引用；RTM 按各项实际结果区分 PASS、PARTIAL、NOT RUN、BLOCKED。跨文档综合一致性、并发幂等及真实 UI 门禁尚未 PASS；任务仍 BLOCKED。

最终独立连接只读复核：DEV成功版本54、foreign_key_checks=1、物料16/目标与FINISHED合计0/产品2；隔离55–57成功。35张用户/权限/字典/产品/物料及GxP/eBR/归档/打印保护表行数和稳定SHA完全相同；隔离测试批次0、成品流水0、FINISHED物料0；402个物理外键检查无孤儿。证据：本机 EXTRA_FINAL_READONLY_VERIFICATION.json。此结果不替代未执行到的运行时 canonical/打印验收。
