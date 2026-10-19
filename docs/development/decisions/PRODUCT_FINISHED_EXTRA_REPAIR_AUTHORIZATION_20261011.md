# 本任务 §6A 有界例外与迁移协调

父会话 01a115d2-8ace-7574-9a08-ac48dfc970cc 于本轮明确批准追加最多 3 轮定向修复与验证，仅闭合已批准方案 B；此前 2 轮失败记录保留，不修改全局规则、断言或业务边界。仍禁止 DEV 写入/迁移、重启及提交推送；只复用已批准隔离库。

本任务未应用草案 V055__product_finished_direct_batch.sql 改为 V056__product_finished_direct_batch.sql；SQL 字节不变，SHA-256 `416e07ac901bfa9895dfa52230331fbee4650c53e5fa14c418470c02a53f38d6`。另一任务 V055__qms_inspection_object_binding.sql 字节保持 SHA-256 `93b07ae911e92131bb779825ae3df059ff33a99297dfca3fac42359a33fbdf23`。本机可访问 DEV/test_mes 历史中均未应用本任务旧/新版本，逐库证据留本机 migration-coordination-before.json；未宣称检查不可访问远程环境。测试期望同步为 56，不降低断言。仅移除 target/classes 中与本任务旧草案字节相同的过期构建资源。

版本 056 由本任务占用；其他写任务不得覆盖。若新并发占号或已应用证据出现则暂停。运行状态仍只在 MES_TASKS.md。

追加协调记录：V056 已在隔离库执行，原字节保持不变；追加 V057 只修复 QC 主体组织兼容条件，旧 MATERIAL 分支原谓词及错误文本保留。DEV 未应用。三轮结果及停止原因见最终报告，未修改全局 §6A。
