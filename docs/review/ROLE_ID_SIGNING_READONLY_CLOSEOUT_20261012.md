# ROLE-ID-SIGNING-001：追加只读核验

父任务明确授权追加只读诊断；本轮未修改产品代码、执行/验证脚本、断言、权限或页面设计。此前两次失败结果保留。DEV V059、正式JAR及草稿291 COMMIT事实不变。状态仍为 **IN PROGRESS / NONADMIN HTTP GATE NOT RUN / NOT ACCEPTED**。

| 分类 | 当前事实 | 证据 |
| --- | --- | --- |
| 已通过 | 按实际列直接SELECT，签名50、产品2、物料16、字典7/29、用户9、角色10、菜单58、成员9、角色权限353等12张保留表核验；用户保护字段不变，只有admin的version/updated_at登录维护变化；两种角色FK孤儿0 | evidence/role-id-dev-deploy-20261012/readonly-protected-data-verification.json |
| 已通过 | 正式JAR的真实页面重新加载成功；401具体为无Bearer的GET /auth/me，随后POST /auth/refresh200、带Bearer重试/me200、导航200。accessToken仅在前端http.ts内存中，页面重载清空，auth.load先请求/me，401拦截器恢复认证。属于现有预期恢复回链，无业务拒绝或新增401代码缺陷；控制台仍会显示首次401 | readonly-auth401-trace.json；frontend/mes-web/src/api/http.ts、stores/auth.ts |
| 已通过（本次组件/轻量操作范围） | 稳定375状态：页面x12/宽351，14对角色x34/right341/宽307/delta0，同排且在视口内。实际已查看头部及滚动后的QC/QA规则截图；原因、保存/关闭、角色选择均可见。窄屏角色面板位于页面下部，可正常滚动到达，不要求首屏展示全部规则 | readonly-layout-computed-style.json；readonly-stable375.json；1791766988208_stable375_header.png；1791766988585_stable375_signature_roles.png |
| 保留失败、已分类 | 初始375截图与几何在视口刚切换时出现页面x约212/宽151、角色right约530；稳定500ms后不复现，且未改源码。按观测应归类为布局尚未稳定的截图时机，而非已证明的稳定布局缺陷。原截图不删除；未重设计、未放宽同排/可见性要求。toolbar/canvas保持原编辑器结构，本次不是完整编辑器全功能验收 | readonly-browser-diagnostics.json；1791766623568_role291_diagnostic_375.png；新稳定截图 |
| 保留FAIL | 原完整保护脚本仍误用sys_user.version_no，实际物理列为version；脚本不再修改、不冒充PASS。追加SELECT已独立证明相关保护事实。以后复用此脚本需要有界fixture修正；无需改变业务字段或签名模型 | logs/role-id-dev-postcommit-auth-classified-20261012.log；原脚本tools/role_id_dev_postcommit_20261012.py |
| 未测/依赖不足 | 8个非管理员实际均没有ebr:template:view，预期认证后role-options拒绝。Windows已注册MES凭据目标只有CUAWright/MES/local-admin；无可用非管理员登录目标。没有重试此前被拒绝的凭据目录读取，也没有猜密码/借admin凭据登录别人、建用户、增权或伪造认证。真实非管理员HTTP403仍NOT RUN，需使用现有账号正常登录完成 | readonly-chain-and-permission-state.json |

截图结论与设计：Global UI V2 §11要求mobile可用于review/lightweight操作，ROLE_ID_SIGNING_DESIGN_LOCK要求原设计器布局、中文角色名/编码、同排标签控件及整对换行。稳定状态满足本次角色控件与轻量操作覆盖，未发现需要新页面设计的稳定裁切。没有完整375px原设计器批准参考，因此不宣称完整编辑器视觉逐像素验收或全部功能通过。

## 两轮修改及停修依据

1. 环境执行修正：tools/role_id_guard_runtime_20261012.py将现有根.env属性传给Maven测试JVM；原模块目录相对.env未加载，Redis AUTH不匹配导致真实登录503。未改Redis配置、密码、业务源码或权限。
2. 验证fixture分类：tools/role_id_dev_postcommit_20261012.py保留旧sys_security_event全部行，新增仅允许admin的LOGIN SUCCESS且角色/权限目标NULL；与正常认证事实区分。未写数据库、删除审计或放宽受保护业务数据。随后version列名错误仍FAIL，停止第三次自动修正。

MES_DEV_RULES.md §6A原文：**“每个任务最多执行 2 轮自动代码修复（首次实现不计入；每轮失败后的代码修复计 1 次）。修复后的针对性验证不另计修复次数；达到上限仍不通过时立即 STOP: BLOCKED，不得重置计数、换测试名称或重新启动代理绕过限制。”** 本次是父任务新明确要求的只读事实核验；失败脚本、代码和计数不变。

## 成品链的当前事实

直接SELECT确认：root12模板251 DRAFT/version0、252 DRAFT/version2，均由admin创建/更新；旧模板10 EFFECTIVE仍package_version_id12、process_package_id NULL；没有root12可直接消费的EFFECTIVE模板。批次230仍DRAFT/version0；计划633已APPROVED、approved_by11569。现有启用ebr:template:approve用户仅admin11556，未出现新独立批准人。历史hash兼容源码已在本轮正式JAR部署，四个历史canonical/hash已在预提交核验通过；不再将“兼容代码未部署”列为当前缺陷。没有其他任务已解决上述模板/独立批准依赖的数据库事实。

后续仅待既定业务依赖和授权后继续：正常有效eBR与独立批准、批次下达/执行、成品质量及库存/发运/证据实际链。未创建/发布eBR、签名或成品业务数据。已完成的计划批准、reauth和路由修正不再列为未修；打印模板及数据源不列待开发。

所有JSON/PNG位于docs/review/evidence/role-id-dev-deploy-20261012；浏览器本轮始终mutating_requests_allowed=false，结束browser_active=false。备份与私有全字段快照仍留本机，未提交Git。
