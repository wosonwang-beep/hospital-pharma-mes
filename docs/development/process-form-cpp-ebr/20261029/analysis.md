# 工序动态表单、CPP 与 eBR 配置：产品经理发现记录

日期：2026-10-29 Asia/Shanghai。证据等级：SOURCE_REVIEW；运行证据待补。SELF-REVIEW，不是独立QA。

## 用户任务与第一眼信息

工艺维护人员需要从一道工序找到其记录表单，完成字段、CPP条件及签名/复核配置，保存后仍能定位原工序和同一模板，并经原有受控流程供eBR使用。打开设计器首先应看到工序、表单及模板版本/可编辑状态；其次是字段、规则条件和动作；安全DSL和技术摘要为辅助信息。上述岗位优先级为 INFERENCE，待真实操作验证，不宣称用户已确认具体布局。

## 实际读取的依据

- AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md、docs/PROJECT_BASELINE.md。
- docs/architecture/BASIC_CURRENT_DEFINITION_CONTRACT.md：工艺当前定义不恢复版本/审批；eBR保留自身受控生命周期。
- docs/development/PROCESS_DYNAMIC_FORM_CPP_REQUIREMENTS.md、DCP-PROCESS-DYNAMIC-FORM-CPP-001-APPROVED.md、docs/architecture/PROCESS_DYNAMIC_FORM_CPP_DETAILED_DESIGN.md。
- docs/development/decisions/PROCESS_DYNAMIC_FORM_CPP_PROTOTYPE_20261013.md：CPP确切原型已批准；只引用本表单字段，四类动作、安全DSL。
- docs/development/decisions/ROLE_ID_SIGNING_DESIGN_LOCK_20261011.md 第5项及 docs/architecture/ROLE_ID_SIGNING_CONTRACT.md：同位置替换为中文角色选择器已获有界批准，schema2必须requiredRoleId，不能回退编码。
- docs/development/decisions/PRODUCTION_PROCESS_PROTOTYPE_OPTIMIZATION_20261013.md：整页原型仍待人工确认，不能据此恢复工艺版本生命周期。
- docs/review/PROCESS_EBR_CPP_INTEGRATION_20261015.md：历史验收只覆盖定向回归，不代表实际生产全链。
- Global UI V2、Page Template Standard V2相关章节；business-product-manager及其用户优先、保真、G0、ownership、profile和交接参考；mes-ui-ux-audit、ui-ux-pro-max、cuawright-local。

## 缺口与候选（不能混为运行缺陷）

| ID | 严重度/性质 | 证据与影响 | 后续关闭条件 |
|---|---|---|---|
| F-001 | HIGH / 源码确认的合同不匹配 | DefinitionDesigner.vue 的 addSignature/addReview 创建 requiredRole:''，右栏仅编辑 requiredRole；model.ts definitionForSave 强制 requiredRoleId。设计器未接 RolePicker 或 prepareDraftRoleIds。新签名/复核缺ID会在保存转换时被拒绝；不能删除该校验来修复。 | 复用已有角色选项API及RolePicker；新增、已有草稿、只读冻结角色及失败恢复验证；真实保存重读，不修改已发布证据。 |
| F-002 | MEDIUM / 能力边界待核实 | CPP前端和后端均采用 rootLogic→groups→conditions 的两级结构；批准文档描述嵌套组，但确切批准原型也仅展示条件组。不能直接认定任意递归已获批准，也不能把两级实现宣称为任意嵌套。 | 在真实工艺规则上验证表达能力；如需递归children/schema变更，先DCP精确确认；当前不改DTO、历史cppConfigJson或引擎。 |
| F-003 | MEDIUM / 源码发现、运行待证 | 设计模式仅有保存/检查/关闭，提交审批、批准、发布和新版本在详情模式。工艺入口直接进designer、返回工艺；需验证用户是否能找到同一模板的受控操作。 | 从工序开始完整走到同一模板发布与返回，记录步骤和角色权限；若可达则关闭发现，若不可达先提出有界呈现方案。 |
| F-004 | MEDIUM / 源码候选 | 工艺页取 size=100 模板并逐条详情读取，将不同模板/版本按operationCode聚合；row-key使用form.formCode。需核对多版本同码、超过100及错误/部分成功。 | 构造矩阵验证选择、身份、完整性、性能和错误恢复；不把历史版本全混列直接当业务故障结论。 |
| F-005 | HIGH / 运行证据缺失 | 首次cuawright inspect_url 5173返回ERR_CONNECTION_REFUSED；8080/5173/5174无监听结果。 | 检查实际环境及待执行迁移后恢复原源服务，完成真实5173/8080任务与恢复流。禁止自动启动时顺带执行未审查迁移。 |

## 不可变合同与允许的呈现范围

工艺是当前定义，eBR是受控模板版本；二者不能共用一个状态含义。沿用现有API、权限、组织隔离、幂等、乐观锁、审批、独立用户、审计和签名。历史canonical、hash、角色快照和批次快照不更新。允许分析、记录证据和准备原型；原有角色选择器修复已有明确设计例外，应按该锁单独核验，不需要让用户重复批准同一替换。

不实施未批准工艺整页、SOP/工时/照片、UI V3、新权限/路由/表、递归CPP存储或自动发布。若需要重设计，必须先完整本地Vue原型和确切源码人工确认，再G3/G3.5交接。

## 方案比较与分批顺序

优先方案A：保留现有Canvas及CPP设计器，首先修通角色选择与受控保存，再验证工序上下文、模板生命周期、CPP/eBR消费。它复用已批准资产，适合隔离实际故障。

方案B：将整个工艺重画为新工作区。待批准原型存在工艺版本合同冲突，且当前用户关注的是配置链，不能用此替代已批准行为；保留为后续独立设计议题。

顺序：F-001最小源码复现→界定可实施修复包；F-003/F-004真实只读及角色走查→确认缺口；F-002以真实业务条件核对两级能力→必要时单独变更；最后正常受控模板与eBR联动验证。任何审批/发布/签名不得由计划或AI代签。

## G0与当前边界

G0 IN REVIEW：目标和范围清楚；岗位、交互恢复及嵌套边界待验证，不能签发整包G4。资料结构校验只证明引用和交接一致性，不证明G0、UI或系统验收通过。G1/G2/G3/G3.5/G4/G5均未在本轮执行。历史已验收记录保持原事实。

UI审查计划遵循完整mes-ui-ux-audit：每个surface的状态×1440/1280/900/375矩阵、八组知识检索、原图逐图视觉审阅、九维及WCAG2.2基础检查、完整恢复流、锚点测量、对抗式第二遍和manifest校验。本轮尚未执行，全部NOT RUN；桌面专用豁免只能引用适用设计锁，不能自行缩减。

以上为开始整改前的发现基线。后续直接用户授权、源码修改、失败及暂停事实见下节；不得把开始前事实覆盖为已经修复。

## 直接修改授权后的执行与暂停报告

用户明确要求“修改不符合的地方”。本轮仅按已有ROLE-ID设计锁第5项修复F-001，保持页面结构、API、保存转换校验、冻结快照与角色ID合同。适用修复技能：systematic-debugging、test-driven-development、verification-before-completion。

- 实施/验证预算起点：2026-10-29 09:49:01 Asia/Shanghai，普通UI十分钟；当前在预算内触发重复失败停止。
- 已修改 `frontend/mes-web/src/views/ebr/DefinitionDesigner.vue`：签名/复核文本框替换为现有RolePicker，绑定requiredRoleId；复用ebrRoleOptions；只在可编辑时请求当前角色；冻结态沿RolePicker读快照；错误保留并允许重试；旧搜索响应不能覆盖新结果。
- 新增 `DefinitionDesigner.roles.test.ts`：Canvas/HTTP仅作为边界隔离，角色控件、Vue状态与definitionForSave真实执行；不作为真实浏览器/持久化证据。
- 最小源码模型复现2项：旧编码输入被拒绝；对照提供合法ID后序列化通过。初次脚本相对路径多一级，修正路径后Node退出0，无HTTP。详见role-save-repro.mjs。
- RED：3新增组件用例均按缺失能力失败，role-red.log保留。首轮命令尾部读日志导致shell返回0，Vitest报告明确3失败，不把shell0当作测试通过。
- 首次实现后：3 suites / 15 tests，14通过、1失败，命令退出1，见role-green.log。失败：`designer signature and review role integration > new signature and review rules select an enabled role ID and serialize a valid schema2 draft`，`Unable to find role="combobox" and name "签署或复核角色"`。
- 针对性调整1轮：测试输入改为Vue reactive，与正式页面深响应式definition构造一致；clone替代对代理的structuredClone。所有测试断言保持原样。重测仍14/15、退出1，同签名，见role-green-fixture.log。
- 按MES_DEV_RULES.md §6A重复错误提前停止，未继续第二种补丁，未改断言/阈值或跳过用例。已经向用户请求下一阶段仅诊断/修角色选择控件可访问名称或装配，范围不含业务合同与页面重画，等待明确回复。
- `npm run typecheck` 退出0，typecheck.log保留。build、全前端回归、真实5173/8080保存重读、完整G5审查均NOT RUN。后续不得将当前14/15或类型检查当READY。

当前失败根因尚未证实。已排除纯TS类型错误和原先的角色ID序列化规则本身；新控件集成的可访问名称、渲染装配仍需诊断。源码仍保留未通过验证的修复，未部署。修复计数保留：本轮测试夹具调整1轮；历史计数不清零。没有启动服务、数据库写入、新迁移、审批、签名、发布、提交或推送。

Planning远端计划已创建并成功保存到revision2。此后更新检查点及resume均发生MCP传输错误，最新整改记录以本节与MES_TASKS.md事实留证，未声称远端同步完成；连接恢复后须先resume核对revision再补同步，避免丢失/覆盖。Planning不是运行状态第二来源。

## 用户批准后的第二阶段与再次暂停

用户直接回复“批准，按此范围继续修复”，允许仅诊断/修角色控件可访问名称或装配，原断言、角色ID合同和页面布局不变。累计次数与前阶段失败均保留。

- 02:01:24 UTC / 10:01:24 Asia/Shanghai开始下一限定阶段诊断；不是清零前阶段计数。
- 诊断仅扩大原失败DOM输出，role-stage2-diagnostic.log保留：Ant Select外层div有aria-label，但实际combobox input无对应名称，证据位于日志2714–2749附近。未改测试断言。
- 一次定向修复：RolePicker.vue使用useId关联label与input，并补屏幕阅读名称；可见“角色”及同行布局不变，冻结快照/角色ID行为不变。
- 原3 suites / 15 tests仍14通过、1失败，退出1，role-stage2-green.log保留；同一按名称定位失败。关联label并未证明计算名称正确，不能声称无障碍问题已修复。实际计算名称与额外子文本/装配仍待诊断，不能把猜测当定论。
- 同签名复现即再次停止后续修复/验证。已请求下一阶段最多10分钟，先测实际可访问名称，再仅修标签关联；等待直接用户批准。累计本轮夹具调整1 + 标签修复1；未改断言或绕过。
- 第二阶段未运行新typecheck/build、真实浏览器/API/G5；上阶段类型检查不能冒充新源码验证。git diff --check退出0仅表示补丁格式无错。
- 02:06:29 UTC远端Planning追加同步成功至revision3，此前传输失败事实保留。运行状态仍只由MES_TASKS.md维护。

## 第三阶段：确切名称诊断、修复与定向验证

直接用户授权：“批准这10分钟的限定诊断与修复”。预算起点2026-10-29 10:14:26 Asia/Shanghai，截止10:24:26；源码修复及验证于10:19:47前完成。前三阶段失败和累计计数均保留。

1. 在原用例中临时增加computeAccessibleName诊断输出，不改断言；实测两个角色input的名称均为“签署或复核角色 请选择角色”，各有唯一ID v-0/v-1。完整证据role-stage3-name-diagnostic.log。原因是label包住整个选择器，关联名称包含了后代占位文本，而非ID重复或角色保存转换错误。
2. RolePicker.vue只将外层label改为同行div容器，独立label通过唯一ID关联输入。可见“角色”、同行间距、中文选项、冻结只读显示及角色ID合同保留。临时诊断import/console随后移除。此为本轮累计第3次修正（前两次为测试响应式构造和第一次label关联）。
3. 原用例越过名称定位，出现不同失败：`Found multiple elements with the text: 质量经理（QUALITY_MANAGER）`，位置从第31行控件名定位转为第32行选项点击。日志明确列出已选span及两个保留的下拉内容，是全页面文本locator歧义，不是同一名称失败复现。未再修改生产代码。
4. 仅将测试点击限定到当前combobox的aria-controls所关联的下拉菜单，避免点击前一个已选值或另一个菜单。11条expect断言逐行一致（包含回调中的expect），原件role-stage3-before-locator.ts.txt保留；没有修改期望角色ID、序列化结果、冻结显示、用例数、跳过条件或阈值。此为累计第4次修正，其中生产标签修复2次、测试构造/操作装配2次；不清零旧阶段计数。
5. `npm test -- --run src/views/ebr/DefinitionDesigner.roles.test.ts src/views/ebr/RolePicker.test.ts src/views/ebr/model.test.ts`：退出0，3 suites、15/15通过、0失败、0跳过，6.48秒；role-stage3-final.log。
6. `npm run build`：退出0，包含vue-tsc -b与Vite构建；Vite 17.05秒，保留既有大chunk警告；role-stage3-build.log。`git diff --check`退出0。无新增依赖/迁移/后端改动。

本阶段验证结论限于源码与隔离组件合同：角色选择可被准确定位，两种新规则提交有效ID，冻结角色只读不请求现行角色，加载错误可重试；尚无真实HTTP/DEV持久化或完整视觉证据。F-001源码修复为待真实复验，不将HIGH发现直接签成G5关闭。G5、真实页面/恢复流及其它候选仍NOT RUN。SELF-REVIEW；不宣称UI验收/完整生产工艺完成、部署或ACCEPTED。

本轮实际业务源码仅DefinitionDesigner.vue与RolePicker.vue，新增定向测试及本目录证据；其余用户未提交修改保留。未启动服务、写业务数据、执行迁移、签名、审批、发布、提交或推送。
