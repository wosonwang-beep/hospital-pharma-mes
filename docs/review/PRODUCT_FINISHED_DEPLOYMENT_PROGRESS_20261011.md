# PRODUCT_FINISHED 已批准部署阶段进度 — 2026-10-11

IN PROGRESS / NOT READY FOR FULL ACCEPTANCE / NOT ACCEPTED。此报告回应父任务阶段进度请求，不代表八项验收完成。

- 规则/依据：已物理读取 AGENTS.md、MES_DEV_RULES.md、数据库验证策略、DESIGN LOCK及方案B冻结记录；写范围与原工作区差异已留本机manifest。用户07:40 UTC授权整体部署，授权记录在docs/development/decisions/PRODUCT_FINISHED_DEPLOYMENT_AUTHORIZATION_20261011.md。
- 维护：停服前通过本机已有管理连接只读检查活动事务=0；唯一8080停止后其他DEV会话=0。只用了已有权限，未GRANT/修改权限。MariaDB/Redis/5173未停，未建额外环境。
- 构建：Java21原checkout Maven package成功6.084秒；运行中JAR文件锁的第一次repackage失败留证，停服后成功，未修改业务代码。当前JAR SHA-256 dd49a987182c57a8c2e406c8710dcf34bfcb16e6a291b3af2dac29cd310bd4c8，内含57个迁移文件，V055–V057与源字节一致。
- 备份：本机临时目录mes-quality-plan-root-review-20261011/deployment-20261011/hospital_pharma_mes_dev-full-20261011T074646Z.sql；5,898,278字节，SHA-256 27e64408d5b6db705fab7a36ffb1f75bc2d44e47c8f6c1d9ac74c9b3b17b3189。136表/4055行存储字节、schema及trigger与原库一致，无重建差异。采用离线等效可恢复验证；此前06:40备份原生恢复exit0证据保留，此刷新文件未实际导入。私有SQL与日志不上传/提交Git。
- 迁移：本机PC-202401011703:3306/hospital_pharma_mes_dev，MariaDB13.0.2；V055=1211285628、V056=-2096839805、V057=1304788399，均success=1。保留V054=-746231051及历史执行文件；未repair/clean，foreign_key_checks=1。
- 服务：启动原JAR/local profile/root.env，8080 PID6372，health HTTP200/UP；5173 HTTP200。后端启动12.044秒，实际中断时间另据维护日志统计，不将启动时间冒充总中断。
- 数据：物料16、FINISHED物料0、产品2；402外键检查无孤儿。产品、物料、字典、权限/角色等既有内容保留；整库对比中Flyway新增迁移与正常登录新增security_event属于预期新增。旧Flyway行的首轮TIMESTAMP差异是SYSTEM/UTC读取口径，按备份同一SYSTEM口径复查54旧行无改变。admin正常登录仅使sys_user.version/updated_at变化（AuthService.recordLoginSuccess）；账号/密码/权限等内容没有改变，不能声称整行完全未变。
- CUAWright：使用已注册本机MCP服务，admin真实登录/identity HTTP200。生产批列表1440×900的6对查询标签/控件同行且无横向溢出。截图/调用日志已留本机，尚未完整视觉对照、移动端或全部页面验收。会话临时写模式已关闭，最终browser_status.mutating_requests_allowed=false、browser_active=false，全局配置不变。
- 待继续：一套有界正式真实业务链尚未创建；新PRODUCT标准/审批、MainBatch/计划/执行/OUTPUT、入库、检验/新报告/QA、发运并发、PDF与历史归档hash、来源链和全部页面双视口证据仍待固定八项清单执行。未出现需要业务代码修复的新根因，未动测试断言，无新增自动代码修复额度。

私有证据根：C:\Users\Administrator\AppData\Local\Temp\mes-quality-plan-root-review-20261011\deployment-20261011；关键文件为new-backup.json、backup-integrity-verification.json、maintenance-preflight.json、source-manifest.json、launch-identity.json、post-migration-preservation.json、cuawright-mcp-calls.jsonl。保留原失败和授权轮次记录。
