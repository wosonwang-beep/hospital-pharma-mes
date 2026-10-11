# 菜单功能原型确认与正式集成门禁

确认时间：2026-10-10 16:53 +08:00。用户：“确认原型，先读取开发规范，再完成菜单功能的开发”；追加：“菜单的数据和权限做为系统的基础数据需要作为正式系统的数据初始化固定”。

UI 原型：APPROVED / FROZEN，指当前本机 Vue 源码及已确认列表分隔线；不是正式功能 ACCEPTED。原型旧验证 BLOCKED 证据保留，不改写为PASS。

## 原型版本与规格

根目录 D:/codex/_project/gmp/mes-demo，入口 http://127.0.0.1:5174/#/menus。

| 文件 | SHA256 |
| --- | --- |
| src/features/menus/MenuPermissionsPrototype.vue | A0138C45E864DEBBDA6FA83C730A7A14971BE0D9BA32C8CDEAAFF7B732DD41B8 |
| src/menu-permissions-prototype.css | BA9C825265514A1984422A0DD36FB335EF3A2B613E37EBF464281F5783D0AA53 |
| src/prototype-page-5173.css | 4D7C44C26FCFBB35CD5A4B5F29149F55FCB373622EFD9D56158593BAFCB0FC4F |

规格引用原型 docs/development/decisions/MENU_PERMISSIONS_PROTOTYPE_20261010.md 与 MES_UI_5173_USER_MANAGEMENT_STYLE_V1.md 的列表补充，截图 evidence/menu-list-dividers/menus-{1440,1280,900,375}.png。T1 菜单管理：页面标题、紧凑查询、树列表、计数分页；查询和列表标题下统一1px细线。T2 菜单信息、固定只查看/管理说明、特殊权限配置/只读关联角色、统一保存和取消。新增/编辑采用已确认独立页面，替代该菜单复杂配置原有弹窗；不将此例外推广到其他页面。

菜单字段 menuCode/menuName/routePath/parentId/sortNo/status 绑定正式 sys_menu 与既有请求，编辑编码不可改。权限名称/编码/状态绑定 sys_permission；说明字段为尚不存在的真实契约缺口，不映射为reason或menuRoute。固定两级来自既有真实权限集合，不使用DEMO或仅凭后缀猜敏感权限。角色展示只读复用真实 /roles 的 menuCodes/permissionCodes；缺失读取权限应明确展示无权，不伪造角色数据。普通等级不自动取得特殊权限。

查询标签控件同行，PC两列表单/窄屏一列，树表局部滚动。查询/重置、搜索祖先、分页、保存后重读、错误保留草稿、取消确认、权限正反例、共享权限影响均需要验证。原型里的本地快照保存不能直接作为正式事务实现。

## 读取与契约核对

已真实读取：AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md 系统管理记录、docs/PROJECT_BASELINE.md Authority、docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md 字体/间距/边框/表格、MES_PAGE_TEMPLATE_STANDARD_V2.md T1/T2、DCP-DATABASE-MENU-AND-USABILITY-001-APPROVED.md；上述本机原型规格和源码；正式 MenusView.vue/MenusView.test.ts/menuCatalog.ts/api/http.ts/router/index.ts；IamContractController.java、IamContractService.java、SysPermissionEntity.java、V037__database_menu_tree.sql 与 V044–V050 菜单相关引用。

权威保持 v1.0.23 + 已批准DCP、Global UI V2/T1/T2。已执行 git status，保留本机配置和所有无关未提交文件。

## 已获授权的基础数据范围

正式菜单层级、真实路由、已批准普通/特殊权限及菜单关联应成为可版本化、可重复初始化的正式种子数据。V037和后续迁移已存在此类数据，必须先核对差异，不重复创建平行菜单/权限。初始化用真实批准编码，不导入DEMO角色/权限/假路由，不自动扩张角色授权。升级不覆盖用户自定义配置或清空历史授权；固定初始化基线不解释成未经授权取消全部后续维护能力。

若需补数据，只追加新Flyway；物理编号实施前以DEV成功history最高值核对，历史迁移不可改。禁止清库、repair、重复本机数据库或全表重建。新数据/结构范围须匹配已授权契约，不借初始化重画业务。

## 实施阻断

2026-10-10 16:59 用户明确确认两项扩展，本节下方为批准前历史，不再构成当前阻断。当前实施契约唯一指向 ../DCP-MENU-CONFIGURATION-001-APPROVED.md、../../api/menu-configuration.openapi.yaml 与 ../../review/MENU_CONFIGURATION_CONSISTENCY_20261010.md；APPROVED/FROZEN UI版本不变。复杂集成预算30分钟，自17:03起，最多2轮修复；实施白名单、逐项字段/版本/安全/验收约束见批准契约，不允许越界修改。

DESIGN CHANGE REQUIRED：
1. 原型权限说明可编辑，但正式 CreatePermissionRequest/UpdatePermissionRequest/IamPermissionResponse 与 SysPermissionEntity 均无该字段。需要明确批准新增存储/API字段，或用户另行批准修改原型；不能静默删字段或假保存。
2. 原型统一保存包含菜单、权限定义与关联；正式 /menus、/permissions、/menus/{id}/permissions 分属独立事务。多请求会部分成功且取消不能恢复。需要明确批准聚合保存契约，或另行批准分步持久化交互。

最小变更申请见 ../DCP-MENU-CONFIGURATION-001-PROPOSED.md。原型确认与基础数据初始化授权不自动授权上述字段/API变更。

## 本轮白名单与结果

2026-10-10 17:26 用户明确批准补查并修复共享顶栏窄屏遮挡和页面刷新登录会话恢复后继续验收。扩展文件白名单为 frontend/mes-web/src/layouts/dashboard-shell.css、frontend/mes-web/vite.config.ts 及对应定向测试/证据；仅修复共享布局与本地同源代理，不修改业务权限、刷新认证/CSRF规则或用户本机数据库配置。继续沿用原30分钟预算（17:33止）、最多2轮修复，不重置失败计数。首次真实浏览器失败证据保留 result.json/failure.png；诊断得到存在HttpOnly续期Cookie而刷新HTTP401，移动端同时显示两组用户控件。

只修改本记录、DCP提案和MES_TASKS.md对应菜单记录。正式业务源码、原型源码、迁移、DB/API/权限/角色数据均未修改，无提交推送。
本轮是前置契约评估，没有启动自动代码修复，修复轮数0。构建/浏览器/数据库/正式集成验收NOT RUN；引用原型证据不冒充正式验收。
结论：UI版本已人工确认；正式菜单开发 BLOCKED / DESIGN CHANGE REQUIRED。批准补充后才能锁定实施白名单、完成跨文档一致性与集成门禁。

## 正式实施证据补充

2026-10-10 17:32：上述仅为历史前置评估。已按批准DCP完成当前源码集成与V051，运行结果/实际文件范围/差异/保留数据/修复记录见 ../../review/MENU_CONFIGURATION_RUNTIME_20261010.md；任务状态唯一维护于MES_TASKS.md。正式源SHA256：MenusView.vue 897C88EC1FAED00DF1E5FCC05F4898E6B6F8F095E792D662774158A9AC87A453；MenuDetailView.vue 0CC0135D139DB07FB0017136D2F72676766B355C86645126B81C0A20F38327F5；menuConfiguration.css FD2175C97A737AFD1CCA1077C5C2B892DB17E26C8BF616A31AED5510D06470A2。原型冻结hash不变。正式源码已验证不代表人工ACCEPTED。
