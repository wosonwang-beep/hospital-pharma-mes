# MES 五阶段工作流 V2.0 — 显式启用的技能配套规范

安装范围：仅新增工作流工具与文档。**本文件只在用户明确调用 `$mes-feature-workflow` 或“使用 MES 标准流程开发 XX 功能 / 按 MES 标准流程执行”后使用**。依据本次后续用户选择，原文第 3/12 章的自动触发改为显式启用后的范围识别；CI 配置只保留未启用示例。没有修改 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、既有 CI、数据库、业务代码或正式 UI。普通任务仍由原有规范治理。

## 1. 目标、权威与启动

执行顺序固定为 UI UX Pro Max → Product Design Vue 原型 → 详细设计冻结 → Codex 真实业务接入 → 集成验收。禁止跳阶段、假验收、图片替代原型、build 替代浏览器、单测替代集成、重画验收过的 UI。文件、阶段状态和确认依据均持久化。

从现有 main 工作区读取 AGENTS.md + MES_DEV_RULES.md；检查 Git 状态与当前任务白名单；按 MES_TASKS.md / PROJECT_BASELINE.md 路由最小必要章节。业务权威 v1.0.23 + 有效限定 DCP；视觉 V2/T1–T6；任务状态只在 MES_TASKS.md。页面目录/功能详细设计、API、权限、状态机、数据库及现有测试约定按功能消费，不全仓扫描。冲突按 MES_CHANGE_CONTROL.md 提案、批准、同步后解决，不覆盖基线。

工具实况与交接见 [tool-handoff.md](../../.agents/skills/mes-feature-workflow/references/tool-handoff.md)。不允许声称未发生的跨环境 RPC。

## 2. 范围

显式启用后覆盖一/二/三级菜单、新模块、查询/编辑/详情/审核/执行/报表页面、T1–T6 修改、冻结布局/交互、字段/权限/状态/流程/实体/跨模块变更。菜单不能只建路由，必须覆盖菜单资源、操作权限、后端验证、角色、页面入口及测试。纯文案/低风险契约无变更允许任务卡记明简化范围、理由和有效批准；仍保留五个状态检查点、批准和必要回归，禁止任意 `skip=true`。

## 3. 阶段交付与验收

|阶段|交付|进入下一阶段的条件|
|---|---|---|
|G1 UI UX Pro Max|功能/模板映射、设计稿、标注/token 引用、布局字段按钮、交互、PC/Mobile、审查|现有基线检查、合法单一 T 模板、需求覆盖、字段按钮状态定义、无阻断问题、设计具体版本确认|
|G2 Product Design|本机 mes-demo Vue 源码路径/提交/文件哈希、交接、浏览器审查、截图对照与原始执行报告|安装/编译/启动；所有主要交互；真实浏览器、无控制台阻断、重大视觉偏差修复；保存截图/JUnit/日志；准确代码版本及原型人类确认|
|G3 详细设计|PRD、功能/页面详细设计、实体/ERD、字段、字典、校验、状态机、权限、API/错误/幂等、GxP、增量迁移计划、测试/RTM、契约矩阵、冻结与 DESIGN LOCK|字段和各层一致、按钮权限一致、状态/后端一致、异常边界覆盖、RTM完整、原型设计无冲突、有效冻结/确认|
|G4 Codex 集成|原型继承映射、真实 API、Mock 移除清单、权限/校验/安全审查、迁移验证、冻结 UI 校验|前序确认/冻结有效；Vue 原型直接复用；Spring Boot/MyBatis-Plus/真实 API 完成；测试/编译/迁移通过；未擅改 UI/契约|
|G5 完整验收|真实集成、角色/状态/关联数据、E2E、视觉/DOM、异常/边界、必要回归、问题清单及完整报告|必需实际测试；P0/P1/P2 阻断为零；CRITICAL/HIGH 为零或已有政策下的有效例外；无未批契约变更；无重大视觉偏差；跳过原因+批准；最终人类确认|

所有 UI 使用已有简洁 PC 系统风格、统一顶部/侧栏/面包屑/token/字体/按钮/卡片/表格/表单。**全部标签和控件同水平行；不足时整组换行**。查询条件真实可操作。新增/修改/查看原则独立页，已批准简单编辑弹窗例外继承；不得改造历史页面。普通页只用 T1–T6；不展示非业务必要内部编号/版本，不增无需求按钮/页签/字段。PC/Mobile 明确视口，至少 1440/1280/900/375 与同尺寸参考对照。

菜单权限继承已确认树/Checkbox 可见、只查看/管理、勾选默认管理、前后端一致语义；菜单信息/操作/角色按已批准结构。不授权一般删除受监管记录。

原型交付栈固定 Vue 3、TypeScript、Vite、Ant Design Vue、Pinia、Vue Router，源码唯一位于 D:/codex/_project/gmp/mes-demo，5174。React/静态 HTML 不可替代；不支持则阻断并取得 Vue 构建方式批准。导航、查询重置、分页筛选排序、增改查、表单校验、弹窗抽屉页签、树/Checkbox、按钮状态、加载/空/失败/成功和业务导航全部按适用设计实现。可标注 Mock，但与 UI 分离并保留 API 适配层；组件和 token 可维护，禁止整页图片代替 UI。功能目录只存路径/版本/证据，不重复源码。

G3 每字段需标识/中文名/实体/页面/类型/长度/精度/必填/默认/来源或字典/校验/可见编辑/保存更新/错误。每操作需按钮、permission、后端同义验证、API、状态数据验证、成功失败和测试。管理覆盖批准范围，查看不能写，特殊操作单独定义。冻结清单唯一关联设计版本、原型提交（缺少提交须真实批准哈希例外）、详细设计、DB/迁移、API、权限、测试版本；含 feature/批准主体/时间/文件 SHA256/Git commit。

G4 先读任务卡、冻结清单、原型位置/提交、API/权限/测试。只工程集成，真实 Spring Boot、MyBatis-Plus、迁移/约束、状态机/异常、适用签名/审计、自动化测试；禁止删除原型重画、更换框架/组件库、改冻结布局/风格、绕后端权限、直改 PROD、假数据充集成、改冻结状态/API。发现设计问题走 RFC。

G5 全部套件见 MES_GATE_POLICY.md：后端单元、前端组件、API集成、Migration、角色、状态、真实场景/关联数据、E2E、正式页/冻结原型视觉、异常边界/非法操作、风险匹配回归。菜单额外覆盖未授权页面/API、只查看禁止写、管理获准操作、刷新/重新登录后权限。正式运行页面用既有 5173/8080、共享 DEV MariaDB/Redis；fixture 单测不能冒充实际运行页面。

## 4. 状态、推进、预算

每阶段 NOT_STARTED → IN_PROGRESS → REVIEW_PENDING → ACCEPTED，失败 BLOCKED/REJECTED；修复后回到 IN_PROGRESS 重新验证。仅上一阶段 ACCEPTED 且签名/版本/测试仍有效才允许启动下一阶段。冻结阶段必须先正式变更控制，不能直接重开。阶段状态在 feature/workflow.yaml；功能运行状态唯一在 MES_TASKS.md，只有人类批准可 ACCEPTED，脚本永不自动更新任务索引。

业务自动修复仍继承 MES_DEV_RULES.md 6A：两轮、普通 UI 十分钟、重复失败提前停止，不能改冻结/阈值/断言/skip 强迫通过。此次用户反复修复授权只针对工作流自身，不延伸到后续业务任务。

## 5. 存放与使用

Skill：`.agents/skills/mes-feature-workflow`；治理：`docs/governance`；模板：`docs/templates`；功能：`docs/features/<FEATURE_ID>/workflow.yaml`，其 design/prototype/baseline/implementation/testing/evidence 存正式交付/证据。`init` 创建模板，不开发、不批准。门禁 JSON + Markdown 报告、原始测试输出留仓库，不提交私钥、凭证、敏感业务数据。

交接与后续启动：用户输入“使用 MES 标准流程开发 XX 功能”，执行者先匹配具体 task 与批准范围；可用 `$mes-feature-workflow` 明确标记平台调用。单纯“开发 XX”不启用本新增流程。固定顺序和各阶段批准不能由这一启动语句代替。

## 6. 原始十五章覆盖

原文逐字保存在 [workflow-v2-source.md](../../.agents/skills/mes-feature-workflow/references/workflow-v2-source.md)，不是新业务权威。

|原文章|实施载体|
|---|---|
|一 总体目标|本文 1/3；Skill；ordered gate engine|
|二 启动检查继承|本文 1；工具交接；强制 reads/hash|
|三 适用自动触发|本文 2；后续用户批准显式调用覆盖自动触发|
|四 UI UX Pro Max|本文 3 G1；UI 模板；设计/覆盖验证|
|五 Product Design|本文 3 G2；原型模板；源码/截图/执行验证|
|六 详细设计冻结|本文 3 G3；冻结模板；字段/操作/状态/RTM 检查|
|七 Codex 开发|本文 3 G4；继承/真实API/无Mock门禁|
|八 集成验收|本文 3 G5；集成模板；实际测试/问题检查|
|九 自动化门禁|MES_GATE_POLICY；可执行脚本；未启用 CI 示例|
|十 状态机|本文 4；advance；上一阶段递归检查|
|十一 文件目录|本文 5；真实 Skill/docs/templates/scripts|
|十二 自动执行策略|Skill；工具交接；显式启用后的阶段路由|
|十三 变更控制|MES_CHANGE_CONTROL；哈希/签名失效机制|
|十四 自身验收|tests/test_workflow.py；实际输出证据；识别测试|
|十五 范围最终输出|安装验收报告及文件清单；不触碰既有规则/业务|
