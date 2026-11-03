# 第49批测试DOM依赖修正结果

## 基线依据

用户明确批准PROCESS_CI_JSDOM_SUPPLEMENT_20261103，授权记录在对应APPROVED文件。源CPU诊断、AGENTS/MES_DEV_RULES门禁、原角色权限/UI/验收合同保持；本机仍main唯一工作树。累计48→49，尚余1批；当前窗口16:15:34–16:45:34（Asia/Shanghai）。

## 设计差异和修改

仅frontend/mes-web/package.json将jsdom开发依赖精确固定30.1.3，package-lock.json更新必要开发闭包。实际命令npm install --save-dev --save-exact jsdom@30.1.3 --ignore-scripts；安装日志保留。没有业务角色组件、原测试/5000ms/隔离/skip修改，没有样式mock或node_modules源码补丁。

升级后原两条重点用例782ms、876ms，原角色11/11通过，18.33秒；CPU观测前相应用例3845ms、3669ms。测试用例/可访问名称语义和权限断言不变。完整套件也实际通过，不能只靠单文件改善判成功。

## 修改范围

两依赖文件及批准/执行记录、MES_TASKS本任务段；上一阶段已批准的CI字体/worker/回调差异仍保留。升级前124个生产锁包version/resolved/integrity逐个相等，生产直接依赖声明也相等，role源码不变。无数据库/业务写入、无迁移、本机配置或其他任务修改，无新checkout。未提交、推送或合并本轮修正。

## 验收证据

- 原角色11测试全部通过，原5000ms保留：role-round49-after-20261103.log。
- npm test -- --run --maxWorkers=2退出0，55文件234/234通过，106.81秒：frontend-round49-full-20261103.log。工作树包含其他既有未提交测试，不冒称为远端同树223项。
- npm run build退出0，包含vue-tsc类型检查和Vite构建；原大分块警告保留：build-round49-20261103.log。
- 提交前回调原4项再次退出0：callback-precommit-20261103.log；打印模块54项、0失败/错误、5既有skip仍为此前原源码验证事实。
- Repository/Compose、CI正向/负向策略通过，未改校验器或夹具。
- npm audit --omit=dev --audit-level=high退出1：原axios1.20.0 → follow-redirects1.16.0存在HIGH漏洞，影响1.16.0–1.16.1；候选修复1.16.2。124生产锁包未变证明不是本次JSDOM引入。审计门槛不降低，未运行全量npm audit fix。证据audit-round49-20261103.log与jsdom-upgrade-before-20261103.json。
- Linux字体及原远端四CI仍NOT RUN，尚未宣称恢复或合并。

## 结论

前端原超时已在完整工作树验证中消除，CI整体仍IN PROGRESS。生产依赖存在实际HIGH审计阻断；当前授权要求生产版本不变，因此先保留并提出[follow-redirects单包补充](../../decisions/PROCESS_CI_REDIRECTS_SECURITY_20261103_PROPOSED.md)。尚未获该例外批准前不更新生产包，不关闭审计，不合并；累计49不清零，不自动51。整个生产工艺工作区/IPC完整业务链/G5仍未验收。
