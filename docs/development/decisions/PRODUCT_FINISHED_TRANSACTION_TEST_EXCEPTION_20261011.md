# 正负测试事务分离：本任务两轮追加授权

父会话 01a115d2-8ace-7574-9a08-ac48dfc970cc 明确批准再追加最多 2 轮，仅已证实的测试事务边界和批准的剩余验收。原 2+3 轮记录保留，全局 §6A 不变。本次执行窗口按 10 分钟自 07:09 UTC 起约束，不以预算无限重试。

设计决定：正向服务链和拒绝负例使用独立隔离测试案例、独立测试事务及各自 fixture/回滚。不得清除 rollback-only、改 noRollbackFor、吞业务异常或使用 REQUIRES_NEW 留下永久业务数据。旧 fixture 170 行的 ResourceConflictException(QC_SPEC_SUBJECT_MISMATCH) 经 REQUIRED/readOnly 代理加入测试外层事务后，使其 rollback-only；随后 QA review savepoint 报 CannotCreateTransactionException。仅将负例拆到独立 @Test，正式产品代码与事务传播不改，保留原拒绝断言及新增异常类型断言。

继续使用既有隔离库 test_mes_product_finished_20261011_0640，无 DEV 写/迁移/重启/推送。并发测试每个工作线程使用 REQUIRED TransactionTemplate 并最终回滚，不提交业务记录。最终运行状态只在 MES_TASKS.md。
