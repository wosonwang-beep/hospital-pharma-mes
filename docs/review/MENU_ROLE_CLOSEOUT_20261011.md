# 菜单及角色权限收尾记录 — 2026-10-11

## 1. 基线及本轮范围
用户在未完成项核对后要求「继续收尾」。本轮只处理菜单/角色权限的构建类型错误、原仓库运行恢复、既有接口复核和状态同步，不扩大业务设计，不修改现有角色授权。
实际读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md 当前角色段、docs/PROJECT_BASELINE.md 相关入口、ROLE_PERMISSIONS_PROTOTYPE_FROZEN_20261011.md、DCP-MENU-CONFIGURATION-001-APPROVED.md、database-and-validation-strategy.md 及现有角色测试/接口。权威保持 FINAL BASELINE v1.0.23 + 已批准补充 / UI V2；本轮不重新设计页面。

## 2. 已解决差异及修改范围
- RoleDetailView.test.ts 中 toggleTree 的容器声明由 HTMLElement 修正为 Element，与 Vue Testing Library 返回的实际类型和所需 querySelector 能力一致。一行签名修复，无断言、阈值、跳过条件、权限逻辑或业务页面修改。
- 原临时目录运行不符合单一工作区要求。已在 D:/codex/_project/gmp/hospital-pharma-mes 原 main 工作区重新构建，所得可执行 JAR 与此前运行的完整 JAR SHA256 完全相同；主源码/资源逐文件比对差异为零。完成原仓库路径 8080 受控重启，不继续从临时目录运行，不创建第二个业务端口或数据库。
- 本轮新增本记录，仅定向更新 MES_TASKS.md 对应段及 PROJECT_BASELINE.md 证据链接。其他未提交代码、原型、冻结迁移和历史验收记录不覆盖。

## 3. 本轮实际验证结果
| 门禁 | 结果与证据 |
| --- | --- |
| 前端完整构建 | npm run build，含 vue-tsc，exit 0；menu-role-closeout-build-final.log。初次 PowerShell 把原有 Vite stderr 警告当终止错误，随后由 cmd 原样执行并以真实 exit code 判定；没有修改构建规则。 |
| 前端定向回归 | RoleDetailView.test.ts 11 + MenusView.test.ts 3 + menuConfigurationModel.test.ts 3 = 17/17 PASS，0失败/跳过，39.13秒，exit 0；menu-role-closeout-unit.log。 |
| 原仓库后端构建 | mvn -f backend/pom.xml -pl mes-boot -am -DskipTests package -q，exit 0；原仓库可执行 JAR 128899285字节。不是新后端集成测试。 |
| 运行身份 | 唯一8080 PID32392，命令使用原仓库 backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar，local profile，健康UP。部署前完整JAR摘要与已运行版本相同。 |
| 真实查询 | 使用已有Windows凭据管理器进行正常认证；菜单/角色/权限各验证无关键词、英文关键词、中文关键词，共9次HTTP200；另6次状态/兼容参数查询均HTTP200。中文物料主数据菜单命中1条；角色master命中1条；不是前端假筛选。 |
| 数据库只读核对 | 本轮前后均DEV V059；角色9、菜单58、角色权限关联353、用户角色关联9。无本轮新迁移、无角色/菜单/权限业务写操作。计数一致不冒充全行内容逐字节对比。 |
| 源码差异检查 | 本轮类型修复之外保留之前工作区修改；git diff --check针对角色实现及测试复核。 |
| 页面保存验收 | NOT RUN：尝试写入独立真实保存验收脚本时被工具执行安全检查阻止。检查确认该脚本文件不存在；未执行新增菜单/角色、授权、停用或清理。未换工具绕过。 |

日志仅保存在本机 C:/Users/Administrator/.codex/tools/cuawright-mcp/private-data-audit/，不含密码/令牌。此前已完成的四视口只读浏览器、后端定向集成证据保留为此前记录，不作为本轮重新执行的结果。

## 4. 结论及明确未完成项
**BUILD/DEPLOYMENT/READ QUERY PASS；真实页面写入验收 BLOCKED；总体仍 IN PROGRESS / NOT ACCEPTED。**
构建失败和临时目录运行两个阻断已解决。仍需受允许的执行路径完成「页面保存→真实API→数据库持久化→刷新回显→有权/无权账号验证」，以及本轮最终验收。未以单元测试或只读查询代替它。
没有关闭用户原有浏览器，没有改动数据库/Redis配置、用户账号或角色授权，没有提交/推送Git。基础资料补齐、生产成品链、电子批记录角色签名和打印数据源不在本轮收尾范围，状态保持。

## 5. 用户再次要求继续后的真实页面保存验证 — 2026-10-11

本节为最新运行进展，覆盖上节“所有真实页面写入均未运行”的旧状态；不覆盖历史失败、不改变设计、不作全系统验收声明。本轮读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md 对应段、PROJECT_BASELINE.md、角色原型冻结、菜单批准DCP及实际菜单/角色页面保存实现。只使用现有5173/8080；既有源码、数据库/Redis配置及工具安全设置均未修改。

### 5.1 菜单原值保存（有限范围）
通过正常管理员登录和正式菜单编辑页，对菜单7（物料主数据）只发送一次PUT /menus/7/configuration。脚本在发出前核对名称、编码、地址、父菜单、排序、状态、查看权限及权限集合均与原读值一致，permissionChanges为空；没有增删授权或业务对象。真实响应HTTP200。
原整体验证脚本随后发生AssertionError，结果仍保留pass=false，没有重复保存或将原结果改写为PASS。独立的只读后续浏览器重新登录/打开确认：菜单仍是物料主数据、/master/materials、父19、排序10、启用及原五条绑定；版本2，刷新后名称正常。角色数9、权限总关联353。因为原异常记录未包含断言行号，不能虚构其精确失败原因或声称该整套菜单脚本全部通过。
证据：本机private-data-audit/menu-preserved-save-result.json（原失败）、menu-preserved-save-readback.json（只读后查）。

### 5.2 新建且不关联用户的角色：真实UI保存及回读PASS
使用单个本轮新建角色“计量资料查阅岗”，编码UNIT_DOCUMENT_READER，实际ID387。始终不关联任何用户，不修改原有角色，不新增菜单或权限定义，不授予审核/签名/放行/导出权限。仅允许现有单位菜单的master:uom:view/create/update普通权限。

| 操作 | 真实结果 |
| --- | --- |
| 页面新建空角色 | POST /roles HTTP200；菜单、权限均为空 |
| 勾选单位菜单，默认管理并保存 | POST /roles/387/permissions HTTP200；GET回读仅有view/create/update及一个单位菜单；特殊权限0 |
| 重新打开后切换只查看并保存 | HTTP200；GET回读只剩master:uom:view；再次打开只查看正确勾选 |
| 取消菜单授权并保存 | HTTP200；GET回读菜单和权限均为空；再次打开正常 |
| 本轮对象收尾 | 正常API停用ID387；最终GET确认INACTIVE、0权限、0菜单、0关联用户；保留记录及服务端历史，不物理删除 |
| 既有对象保护 | 规范化API集合前后比较：原有9个角色及其授权/版本、所有既有用户、菜单和权限定义均相同；不等同于全数据库逐字节比较 |

整个角色验证脚本exit0、8.56秒。首轮预检使用错误的master:unit前缀，创建前即停止，steps为空、无业务写入；实际读取目录确认既有前缀为master:uom后仅修正临时验收脚本取数代码，保留旧脚本与原失败结果，没有改正式权限定义或降低断言。正式角色/菜单页面和测试源码本轮不变。
证据：本机private-data-audit/role-unassigned-save-result.json，原始预检失败文件保留在同目录；执行脚本为CUAWright Python环境中的Playwright浏览器，不宣称运行上游LLM驱动CUAWright CLI。

### 5.3 尚未完成与安全边界
- 查询可用MES非管理员凭据的元数据操作被工具安全检查拒绝，未读取密码/令牌，也未通过其他路径尝试获取。未完成非管理员真实账号的有权/无权操作比较；不创建新的登录账号或改现有账号授权。
- 新增独立数据库SELECT核验脚本的写文件动作也被安全检查拒绝，已确认文件不存在，没有改用另一执行路径重试。因此本轮持久化依据是实际服务器GET和页面重新打开结果，不宣称独立SQL核验PASS。
- 本轮没有新增迁移、修改业务源码/原型/权限契约、重启服务或提交推送。8080最终健康UP。此前生产构建和17/17专项测试是上一轮证据，本轮没有重复运行。

**最新结论：角色真实UI保存→服务器回读→重开回显PASS，所有既有账号/角色授权保留；独立数据库核验及非管理员真实权限验证尚未完成，整体IN PROGRESS / NOT ACCEPTED。** 本轮增加且保留1条停用、无授权、无用户的验收角色；不是宣称数据库完全没有写入。

## 6. 菜单保存后断言的定因与关闭 — 2026-10-12

用户要求“继续下一步”，本轮限定为查明上节菜单AssertionError、修正验收脚本加载等待并复验；不修改菜单/角色设计、应用源码或既有权限。重新实际读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md角色段、PROJECT_BASELINE.md相关入口、本记录、菜单实际加载/保存实现及原验收脚本。权威仍为v1.0.23及已批准补充。只使用原工作区、现有5173/8080和普通已授权管理员登录；本轮不尝试此前被拒绝的凭据枚举或SQL核验路径。

### 6.1 已复现并定位，不再是原因不明
给原menu-preserved-save-check.py增加断言行号及异常定位，原14条断言的AST逐项相同，旧脚本/旧失败结果均保留。只允许菜单7以完全相同业务值保存一次，保护原请求白名单及If-Match不变。HTTP200；原行98–104的菜单字段、绑定集合、版本递增、所有既有角色、权限定义、其他菜单比对全部PASS。失败精确发生在原脚本107行：刷新后读取“菜单名称”即与期望值比较，当时input_value为空字符串。
菜单原值保存的版本2→3，业务名称/地址/父菜单/排序/状态/权限均相同；没有修改角色授权。失败位置是DOM加载时序：domcontentloaded不代表Vue数据加载完成，等待尚未出现的.ant-spin-spinning为hidden会提前返回。原保存及服务器回读并未失败。本次诊断记录保留pass=false作为根因证据，不将它改成PASS；2026-10-11旧日志无行号，仍保留为历史失败。
证据：private-data-audit/menu-preserved-save-diagnostic-20261012.json；原脚本SHA256 0d0b320acf3bfe29284d504656e49a34eb64248ae48810839680a05e53db234d。

### 6.2 只改等待，不改业务及验收断言
原脚本刷新后的检查前新增Playwright expect“保存配置”按钮to_be_enabled(timeout=12000)，等待实际菜单及权限目录load完成；原14条assert AST完全一致，不降低阈值、不跳过字段/授权保护。仅验收脚本被修改，旧文件另存private-data-audit/menu-preserved-save-check.before-readiness-20261012.py.disabled。未修改正式Vue/Java/数据库结构，故未重复构建。
之后不再重放保存，用独立只读脚本验证同一菜单的刷新和回读。1440/1280/900/375四种视口：均真实GET200、菜单名称与服务器一致、页面内容/路由正确、无Vite错误覆盖层或JS异常；就绪耗时分别687/1125/968/656ms。全部菜单、角色、权限目录和菜单7前后API结果完全一致；本次只读复验业务写请求0。另复核ID387仍停用/0权限/0菜单/0用户。
证据：private-data-audit/menu-reload-revalidation-20261012-final/result.json，exit0、7.05秒；同目录menu-1440.png及menu-375.png已读取查看，仅作为加载值证据，不新增整页设计一致性验收声明。

### 6.3 本轮环境记录与边界
只读复验启动时普通登录曾返回503/DEPENDENCY_UNAVAILABLE，原失败结果保留；期间观察8080进程更换及并行role-id部署日志，未由本任务重启或修改服务。环境恢复后同一正常登录路径200，完成上述复验；没有绕过认证、改Redis或增开端口。未查明503的更细内部原因，不将其归为菜单业务错误。
未登录GET /api/v1/menus、/roles、/permissions实测均401（3/3），没有发送变更请求。这证明匿名请求被拒绝，不替代非管理员实际账号的权限正反例。

**本轮结论：菜单保存后刷新断言问题已定因并修正；保存/服务器回读数据保护已通过，四视口只读刷新4/4及匿名拒绝3/3通过。** 菜单新增/业务值修改的全组合、独立SQL核验及真实非管理员账号权限仍未新增验证，整项保持IN PROGRESS/NOT ACCEPTED；不得再把本次已解决的等待问题当作未定位业务故障。未提交推送或操作其他功能。


## 7. 角色与权限功能人工确认 — 2026-10-12

用户在获知前述结果及未执行检查后明确回复「权限可以了」，本轮角色与权限配置记录为 **ACCEPTED / HUMAN CONFIRMED**。不扩大到菜单所有操作、电子批记录角色签名或整个MES系统。

基线依据：本次实际读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md对应段、PROJECT_BASELINE.md角色入口、ROLE_PERMISSIONS_PROTOTYPE_FROZEN_20261011.md和本报告；沿用v1.0.23及批准补充/UI V2。
设计差异：无设计或实现变更，仅记录人工确认。
修改范围：任务索引对应角色权限段及本报告追加记录；其他任务、源码和数据保留。
验收证据：沿用第3、5、6节的构建、专项测试、角色真实保存回读及菜单刷新验证；本次未新增测试。独立SQL及非管理员实际账号检查仍NOT RUN；旧失败及安全拒绝记录不改为PASS。
结论：角色与权限配置按用户确认关闭，不再重复开发或要求重新确认原型。其他功能和最终系统验收、提交推送不在本次确认范围。
