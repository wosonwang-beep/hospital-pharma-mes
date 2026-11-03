# 生产工艺提交后 CI 收尾批准与实施锁

用户对[具体方案](PROCESS_CI_RECOVERY_20261103_PROPOSED.md)明确回复「批准该有界方案并继续」。状态：APPROVED，保留原草案与历史事实。

窗口：2026-11-03 15:00:40–15:40:40（Asia/Shanghai），40分钟、最多2批，累计46→48，不清零。开始依据为本回合读取的UTC 07:00:40。预算覆盖修正、等待及验证；重复原签名立即停止，禁止自动追加第49批。

白名单和具体批准：

1. EditorCallbackTest既有stub和verify追加eq("10"),isNull()，对齐实际9参数方法。原DOCX字节、业务类型、成功响应、源模板不可覆盖、重放/篡改/撤权断言不变，测试数不减。
2. .github/workflows/ci.yml两个Maven任务补装/核对开源中文字体，沿用原转换器和PDF断言，不分发Microsoft字体。
3. 若原完整并行资源证据吻合，可在原前端完整测试命令后追加--maxWorkers=2；5000ms门槛、原断言/用例/排除、CI四任务及Java21/数据库服务不变。不新增重试或跳过。
4. 仅当前批准/执行证据和MES_TASKS本任务段同步。其余未提交任务、本机.env/application-local.yml/application-ci.yml保留，不暂存、推送或修改。

已物理读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS、PROJECT_BASELINE、当前全局UX批准规范及本草案/直接源码/原测试；业务仍v1.0.23＋原批准补充，不改变字段、接口、权限、签名、业务和页面设计，无迁移或生产/DEV业务数据写入。当前已合入PR #6，不重复合并；修复将按原提交/推送/合并授权送审，原CI全部通过后才合并。CI恢复不等于生产工艺整体G5通过。
