# 生产工艺表单优先工作台 G2 原型版本确认候选（待人工批准）

状态：**PENDING HUMAN APPROVAL / G2 PARTIAL / NOT FROZEN**。用户已批准业务方向及继续实施，不等于批准此版本具体源码。此记录只为人工核对版本，不改变 FINAL BASELINE v1.0.23、CPP独立DCP、GMP及既有 eBR 受控生命周期。

## 需求与原型依据

- 目标：生产工艺中按工序管理一份或多份 EBR 记录表单；工序详情不是主操作面，当前工序下的表单列表为主；表单点击“设计表单”进入独立完整设计工作区，同页“表单设计 / CPP规则设计”；工序、表单资料与参数普通子项使用居中弹窗，保留父草稿和取消/失败恢复。
- 源码：D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue
- **当前候选 SHA256：2bbbacd1b4c62be2fc31d3ad3f28fad6b36522ccf3e2ce0fc6ae90f44ec2b82b**。依赖清单：D:/codex/_project/gmp/mes-demo/process-g2-evidence-20261105/source-hashes.json。依赖还包括 WorkspaceFormDesigner.vue、正式 DefinitionDesigner/CppRuleDesigner/CompletionRuleEditor 与现有 Token；确认前必须再核对全部实际源哈希一致。
- 原型访问：http://127.0.0.1:5174/#/processes ；PC视口1440、1280、900，按 PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md 免375手机，900仍为PC窄窗口。
- 页面：T1生产工艺查询列表，T2新增/编辑工作区，T4查看/只读，专业表单设计器保留已批准的例外；主标题仅“生产工艺 / 新增/编辑”；原工艺当前定义无历史版本/审批页，eBR自身版本/审核/签名仍受控。
- 页面布局：工艺编码/名称/路线代码/说明，左工序单导航，右默认记录表单列表（模板名称编码/表单名称编码/模板状态/字段数/设计/查看/编辑），参数与判定次级页签；表单设计器复用Canvas字段独立 +、画布、字段属性和CPP规则设计。
- 交互约束：标签控件同行；T1查询→重置；T2保存→取消；工序/普通子项创建和编辑居中弹窗；父草稿不因弹窗取消/失败丢失；设计器返回工序定位；规则只引用当前表单字段，四类超限动作遵守原DCP。
- 设计参考：用户提供的“药品生产工艺表单设计界面.png”“医院制剂MES工艺动态表单设计界面.png”“GMP MES动态表单规则设计界面.png”；不能把图片当作尚未批准的源码哈希。

## 本轮G2证据（ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max）
- 八组知识查询已经真实执行，原始输出在 mes-demo/process-g2-evidence-20261105/knowledge/；ui-ux-pro-max SKILL SHA256 8b827fe03b349aeea99a7d6cf4ccd892e6065b672859fdb11e13a1919453eddc。
- 1440/1280/900 各9状态共27截图，脚本 process_g2_capture_20261105.cjs，21项定向操作 PASS、0脚本错误；补充6状态/视口共18截图，脚本 process_g2_additional_20261105.cjs，21项定向操作 PASS、0脚本错误。截图均属于内存原型，不是正式业务HTTP/DB。
- 各视口 document.scrollWidth=window.innerWidth；工艺基本信息四个标签/控件中心偏差0px；有视觉审查过的主列表、编辑页、工序表单、设计器/CPP、弹窗；**尚未逐张九维+WCAG19项完整核签或完成对抗二遍**，不得记G2全PASS。
- 本阶段修正：1280记录表单“字段数”被固定操作列遮挡及草稿动作换行，900工艺列表缺失配置状态/工序数可见性，新增工艺校验失败后取消错误进入未保存详情；修正后同版构建及定向浏览器复查通过。未留存所有修前同像素原图，故严格形式验收仍需要补齐前后证据。
- 当前构建：npm run build (Vue TypeScript + Vite) exit0，6423模块；浏览器45张/42项定向成功不代表完整 G2。

## 未决阻断及门禁
- G2 全状态/九维/19条WCAG基线、长文本/键盘/滚动/错误恢复与参考同视口人工核验尚未完成；需独立完整清单的可复验validator。
- **当前版本未获用户针对源码hash和具体页面的人工确认**，因此G3详细设计不可标FROZEN，G3.5不可标READY，G4正式MES新版页面不得凭本记录启动，G5不可标通过。
- 业务来源核对候选：用户此前要求“适用产品”和工艺版本/生效批准日期，但当前工艺当前定义合同仅支持单 productId、当前工艺配置以及名称/说明/路线代码，必须先厘清是否属于本次受控页面范围；不得自行添加字段/表/状态/审批流程。
- 正式 MES 的现有业务代码/DB/API、GMP签名/快照不因本G2原型受影响。用户最终确认前保持待批准。


## 2026-11-05 19:56 G2 扩展验收阶段收尾（NOT FROZEN）
执行者：ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。已实际读取本机 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、当前 PROCESS_FORM_FOCUS_G2_20261105_PENDING、G3.5预检、UI V2/T1–T6及PC范围批准记录。普通UI墙钟预算30分钟本轮已到，停止新业务源码变更和新测试，以下仅归档现有证据。
当前确切原型：D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue；源码SHA256 7ac72a79d52b1c1774de250d35d909bac118523f2213dcb8bf6557d1f708d29f。唯一实际业务源修改是原型查看表单信息的只读值文字对比度优化，保留只读属性和业务数据合同；原型Vue TS+Vite 6423模块构建 exit0，PC 1440/1280/900 真实Chromium已采45张原始分辨率状态截图，定向交互42/42 PASS，另36/36键盘焦点及错误保值PASS，三宽专业设计器固定全屏盖住底层业务页、无全局水平溢出。额外320/400/450/640 CSS窄视口的T1、T2、设计器共12格文档无横向溢出；Canvas独立+字段插入实操成功，字段3→4并在CPP页显示FIELD_4。
已完整逐张原图核对45张，给每张图填充8类观察共360条；对15个页面/弹窗状态填充9维审查共135条，视觉相关120项有观察和证据，未闭合的WCAG整维仍NOT RUN；八组ui-ux-pro-max知识检索和第二轮对抗审查有记录。重要文本配色抽检：正文12.92:1、次级5.74:1、代码4.76:1、状态5.44:1、只读业务值9.4:1；PC控件22px+图标属于WCAG2.2 2.5.8邻近目标间距例外，最小可命中目标中心距离28.4px。19个WCAG基本准则目前17项有专项结果，仍留 1.4.4（浏览器真实文字缩放）与1.4.11（完整非文本UI对比度）NOT RUN，未谎称完整认证。证据保存在 mes-demo/process-g2-evidence-20261105/：audit-manifest.json、45图、两个Chrome检查JSON、a11y-browser-checks.json、wcag-six-probe.json、reflow-small-css-viewport.json、pointer-spacing-minimum.json、field-add-keyboard-alternative.json、designer-viewport.json、source-hashes.json 和知识检索原文。
已实际运行仓库 validator：PASS: PROCESS-FORM-G2-20261105-PENDING — IN PROGRESS; screenshots=45 coverage=45。**这是IN PROGRESS证据结构校验通过，不是G2整体验收通过。** 剩余长CODE、服务失败/409、权限变体等完整状态闭环、用户同hash人工批准和合同差异确认（适用产品、工艺版本/生效批准日期）未完成。G2 IN PROGRESS / PENDING HUMAN APPROVAL；G3 未冻结，G3.5 DRAFT/BLOCKED，G4/G5 NOT READY。没有更改正式MES功能/数据库/Flyway/权限/电子签名/历史快照，没有提交推送合并，保留历史累计81及工作区所有未提交改动。此记录只结束本轮30分钟窗口，不清零历史返工数。


## 2026-11-05 21:26–21:56 DC G2 正式缩放/非文本对比度专项收尾
本轮为新30分钟普通UI窗口。再次实际读取本机AGENTS.md、MES_DEV_RULES、MES_TASKS、PROJECT_BASELINE、已批准UI V2 §6、现有G2待批确认记录、G3.5预检、mes-ui-ux-audit以及原型AGENTS和实际源码；保留FINAL BASELINE v1.0.23/G1–G5、历史累计81和唯一本机工作区，用户授权继续不等于人工批准确切原型。
**原型文件当前最终SHA256：f9a1492393fe1136f7bb8ef8e56c4d83865b28ac26b8348cba0fa605976ded24**（D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue）。本轮仅在此原型追加UI V2非文本边界中性 token #7d8a9b（输入、选择、单复选、Canvas字段插入按钮）及浏览器字体放大兼容的控件高度、工序导航弹性换行、弹窗标签flex宽度、长工序条件区域内部滚动样式。未改变工艺/CPP业务字段、API/DSL、表/迁移/权限/签名或Mock回写边界。
**实际验证**：本轮源码npm run build含Vue TS+Vite6423模块 exit0；正式G2既有42项三PC宽浏览器操作、36项键盘焦点/错误保值专项，以及12项旧主链、4项普通弹窗、4项CPP草稿保存-返回-重开，全链连续运行 exit0（closing-main/additional/a11y/original/modal/cpp.log），保留所有失败历史。非文本边界修复前普通控件1.41:1、字段加号1.74:1；修复后1440/1280/900共18个操作状态117个可见控件边界实测最低3.51:1、低于3:1为0。浏览器隔离原生最小字体minimum_font_size=28px（常规body14px→200%）下发现原900工序竖排/保存按钮裁切并做受控修复，修复后1440/1280/900共18项关键操作、弹窗、全页设计器/CPP、工序不竖排、布局不全局横溢出均PASS；另等效浏览器200%缩放(半CSS视口+DPR2)15/15通过。原始前后PNG和JSON：mes-demo/process-g2-evidence-20261105/native-font28-regression.json、wcag-1411-all-controls.json、zoom200-equivalent-reflow.json、closing-*.log及对应截图。限定为G2原型证据，不代表全站WCAG认证/真实5173/8080G5。
长名称专项：工序长名称在1440/1280/900 3/3保值且没有整页横溢出，停用工艺查看/禁编辑及重复工序编码拒绝等另9项成功；“先改父工序→未经父保存直接改表单资料”在三视口被原型正确保护并提示“请先保存工艺，再编辑表单信息”，该路径不该绕过；**父工艺保存后再次编辑长表单名仍NOT RUN**，不可据失败驱动脚本宣称它已通过。尝试调整针对该测试的定位/重试时工具安全检查拒绝，未换执行方式规避。无真实HTTP 409/403与不同角色RBAC执行证据。
G2审查manifest现45张截图、逐图360条视觉观察、15种surface九维135条评审已登记；19项WCAG基础范围均有定向实测PASS，validator实际输出“PASS for IN PROGRESS; screenshots=45 coverage=45”。**G2仍IN PROGRESS/未获用户同hash人工批准，剩余长名称父保存→表单编辑回读、真实服务错误/权限负例及源设计合同差异确认；G3未冻结、G3.5仍DRAFT/BLOCKED，G4/G5不可放行**。源hash清单与G2/G3.5候选文档已更新本轮f9a...，旧版历史证据保留。未修改正式MES业务文件/数据库/Flyway，未Git提交、推送、合并。


## 2026-11-05 22:07 DC G2 长名称/父草稿保护回归补齐（30分钟窗口到期收尾）
用户要求“继续30分钟任务”。本轮沿用本机单一mes-demo与计划0df9356d-675b-4f37-8b7d-948948c8d694，实际重新读取AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、现有G2待批/G3.5预检、批准的全局UX交互规范及当前ProcessPrototype.vue、原型回归脚本和audit-manifest.json。当前原型SHA256复核 **f9a1492393fe1136f7bb8ef8e56c4d83865b28ac26b8348cba0fa605976ded24**，业务源码本轮零改动，正式MES/DB/Flyway/权限/受控快照零改动。
本轮新增原型真实Chromium专项脚本 mes-demo/process_g2_parent_long_roundtrip_20261105.cjs；在PC 1440/1280/900的完整链路：编辑OP03长名称→未经父工艺保存点击表单编辑出现预期“请先保存工艺，再编辑表单信息”→保存父工艺并切到只读详情→再次进入编辑→修改同工序草稿表单的长名称→列表显示修改后名称→返回查看再重开编辑→对应表单行与只读查看弹窗完整名称一致。最终 **18/18 PASS、errors=[]、6张专项截图**；证据 mes-demo/process-g2-evidence-20261105/parent-save-long-form-roundtrip.json、g2-parent-form-long-*.png。初版专项脚本失败属于隐藏 Ant 表格行定位与底部取消按钮非唯一定位，修正为对应视口可见表单行与bottom-actions作用域后全部通过；保留前失败记录，不改业务断言、不降低阈值。原型自身保存结果仅内存，不等于8080真实持久化。
审查manifest中的“长表单保存后回读NOT RUN”已经按本轮证据纠正，**G2仍IN PROGRESS**：正式HTTP409/403和不同角色权限负例没有专项证据，用户尚未对同哈希原型人工签核，G3详细设计与G3.5交接仍DRAFT/BLOCKED，G4/G5 NOT READY。历史修复累计81及新旧失败/状态均保留。
执行窗口由本轮开始约21:26至实际记录约22:07，已超30分钟上限，已停止新增业务源码/测试，只允许补证据与交接记录。无Git stage/commit/push、无新worktree、未动其它未提交文件。


## 2026-11-05 22:52 DC G2 原型409/403与VIEWER权限状态补齐（30分钟窗口）
执行者：ChatGPT SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。沿用唯一 MES任务、v1.0.23与 UI V2，不新建工作区；真实读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、已批准CPP及MES_GLOBAL_UX_INTERACTION_20261102、G2待批记录、G3.5草稿、正式 ProcessDetailView.vue/model.ts 和相关现有测试。代码白名单只有 mes-demo/src/features/processes/ProcessPrototype.vue 以及新增G2浏览器脚本；正式MES业务源/DB/迁移/角色/签名/历史记录未修改。
**当前候选原型 SHA256：2bbbacd1b4c62be2fc31d3ad3f28fad6b36522ccf3e2ce0fc6ae90f44ec2b82b**。在仅 DEV 原型中通过URL参数 `__g2SaveStatus=409|403` 注入可控保存失败，`__g2Role=VIEWER` 表示只读人物；无页面新增业务按钮、权限定义、API/存储结构变更，**G4不可直接把这两个专用夹具迁移正式应用**。正式必须复用 auth.can()、Axios错误、真实REST、幂等及冲突重载保护。
G2 Chrome实际验证：409/403×1440/1280/900共6组、0 errors，409输入保留/显示错误/模拟冲突解除后保存重读，403无虚假成功/未丢输入；VIEWER三PC宽21个状态检查（3×7）PASS，新增编辑入口隐藏、只读查看、工序弹窗禁用和草稿表单仅查看。两组专项分别有6及3张稳定截图。最初VIEWER图拍在弹窗关闭动画中，已保留误帧发现并修正脚本，等待稳定后重拍；只读业务值computed为#314364，对比度不靠渐变截图判断。npm run build (TS+Vite6423模块) PASS；重新连续执行G2 42操作、36键盘、12主链+4弹窗+4CPP及18长名回读，均成功退出0；正式前端已存在的ProcessDetailView只读/草稿恢复及CppRuleDesigner无障碍三Vitest文件6/6PASS，但仍有原有 maxlength prop type警告，不是G5证据。
G2证据目录：D:/codex/_project/gmp/mes-demo/process-g2-evidence-20261105/，追加 prototype-fault-409-403.json、prototype-viewer-role.json、9张稳定截图、g2-next-*.log 及原45图/关联JSON；审查manifest现 **54张截图/54覆盖单元/162九维条目/51交互条目**，validator真实返回 PASS for IN PROGRESS (54/54)；G2-UNCLOSED-EXCEPTION-RBAC 仅就 **原型模拟UI范围** 有据关闭，真实HTTP409/403/实际角色权限仍归G5的独立验收，绝非后端PASS。G2-VERIFIED-HUMAN-HASH仍OPEN HIGH，故G2整体IN PROGRESS/PENDING HUMAN APPROVAL，G3未冻结，G3.5仅PRE_HANDOFF草案/DRAFT BLOCKED，G4/G5 NOT READY。
G3.5预映射：工艺GET/POST/PUT复用ProcessDetailView.vue中load/createPackage/save/failure/reloadConfirmed，403由auth.can与服务端权限共同限制，409由conflict置位/重载前confirm保值，实际JWT、对象归属与current-definition原子提交不改变；eBR模板GET分页/草稿PUT保留templateId+formCode身份与版本/If-Match。复用OperationItemDialog、DefinitionDesigner与CppRuleDesigner，禁止用原型内存保存结果代替正式持久化。产品“适用产品”与版本/生效批准日期的合同差异及表单名称正式更新语义仍需G3决策，未经用户批准不扩库/审批。
未Git stage/commit/push，没有清理其它未提交文件；此前累计修复81及本阶段事实保留，不清零。
