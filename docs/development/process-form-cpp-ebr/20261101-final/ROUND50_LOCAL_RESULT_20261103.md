# 第50批 CI 修复本机验证与送审记录

## 基线依据

用户明确批准follow-redirects单包安全例外，引用PROCESS_CI_REDIRECTS_SECURITY_20261103_APPROVED。已物理读取AGENTS/MES_DEV_RULES、相关任务索引、全部具体批准记录和直接代码/依赖；v1.0.23、UI V2及原业务/权限/测试门槛不变。

回复恢复时为18:05:21，旧16:45:34窗口已结束，原事实不改；按最新明确具体执行批准恢复最后第50批，30分钟上限至18:35:21，已向用户说明。累计49→50，不清零、不自动51。

## 设计差异与实现

保留此前获批的回调9参数stub/verify、两个Maven任务中文字体安装验证、原完整前端--maxWorkers=2和测试开发依赖JSDOM30.1.3。本批仅将follow-redirects从1.16.0更新为1.16.2，不执行npm audit fix；原HIGH生产审计随后为0漏洞。

未改原5000ms、断言、用例清单、隔离、skip、a11y查询、CI四任务、Java21或生产角色页。角色源码与CPU诊断前SHA一致；没有修改生产实现去迁就测试。

## 修改范围与数据保护

代码/配置仅.github/workflows/ci.yml、EditorCallbackTest.java、frontend/mes-web/package.json/package-lock.json；附本任务批准、过程和结果记录，MES_TASKS只选择本任务新增内容暂存。Axios1.20.0及其余123个生产锁包的版本、来源和完整性逐个不变，生产直接依赖声明不变。

唯一main checkout，未创建其他本地环境。无数据库或业务记录写入、无迁移/历史修改。其他QC/QMS/WMS与全局UX改动、本机配置不进入本提交；另观察到MaterialConversions/MaterialSuppliers由并行工作变化，保持其原状，不纳入本次提交。原始日志/CPU档案保留本机，仅结论与必要元数据送Git。

## 验收证据

本批实际完成：

- npm test -- --run --maxWorkers=2：退出0，55文件234/234通过，103.19秒。frontend-round50-full-20261103.log。
- npm run build：退出0，包含vue-tsc类型检查与Vite构建，构建35.01秒，原大分块警告保留。build-round50-20261103.log。
- npm audit --omit=dev --audit-level=high：退出0，found 0 vulnerabilities。audit-round50-20261103.log。开发依赖审计的其他问题未以生产审计通过掩盖或全量自动升级。
- 原EditorCallbackTest：本批提交前4/4通过，0失败/错误/skip；Maven退出0。callback-round50-precommit-20261103.log。此前打印模块54测试、0失败/错误、5原有skip是第47–48批的验证事实，不冒称本批重跑54项。
- Repository/Compose、原CI正向与负向策略：实际通过；未修改校验器或负向夹具。
- 生产锁包逐项比较：仅node_modules/follow-redirects改变为1.16.2，其他123条不变；role源码不变。

本机工作树包含其他既有未提交测试，本机234项不冒称为远端同树数量。下一步仅按原授权提交、推送本次范围，由原远端四项CI验证真正提交树和Linux字体/数据库集成；未跑前不宣称其通过。

## 结论

本机CI相关修复验证通过，原生产HIGH审计已消除，准备送审。必须四远端CI全部通过才合并；若失败需另改代码，停止而不自动第51批。SELF-REVIEW。生产工艺整体仍IN PROGRESS，整页工作区/IPC完整链/G5仍未验收，不能标READY/ACCEPTED。
