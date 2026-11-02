# 生产工艺原型与 IPC 缺口核对

## 基线依据

用户本轮指出「没有按原型」「过程检验 IPC 根本没有」，确认来源是本机 `mes-demo`，并从两套历史页面中明确识别「上方路线带＋左侧工序＋右侧配置」。这确认了所指历史界面，不等同重新批准其中所有历史版本审批、产品关系或新增 API。

已真实读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、business-product-manager 及 user-first-design、code-implementation-fidelity、end-to-end-ownership、hospital-mes 参考。业务权威仍为 v1.0.23 及批准 DCP，视觉仍为 UI V2。执行者为 Codex SELF-REVIEW，技能不是独立评审人。

本次识别的历史原图，均已按原始分辨率查看：

- `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791868391241_process-workbench-ipc-edit-1280.png`：IPC 工序设置。
- 同目录 `1791868397982_process-workbench-ipc-parameters-1280.png`：检验项目与判定标准。
- 同目录 `1791868402567_process-workbench-ipc-forms-1280.png`：中间产品检验记录表单。

可直接查看同目录 `IPC_PROTOTYPE_COMPARE_20261102.html`，内嵌上述原图和本轮正式页截图；这是证据对照页，没有生成新业务原型。

源码位置：`D:/codex/_project/gmp/mes-demo/src/features/processes/ProcessPrototype.vue`，由 `src/PrototypeRoot.vue` 的 `#/processes` 入口加载。当前 SHA256 为 `67264A831056D900F69DE8920E22683117D2CC9A8E898C62DF2485EA0A88EB74`；20261013 待确认记录为 `491CEF30EB4F73D5643E14EC9A8D6C0C1654BCA56183E766B28BD8EB5840B8F4`。二者不同，不能把当前源码冒充当年的冻结源码。原型服务当前未监听；不能把历史 5174 地址说成已运行。MagicPath 两个可访问项目共五个组件，未找到生产工艺页面，未修改远端设计。

## 设计差异

**本轮用户明确纠正的工序级规则：** 「检验合格后才能进入后续工序」不是全工艺统一要求。是否有 IPC 工序、该工序的判定配置和后续工序的前置关系共同决定检查位置及影响范围。不要求每套工艺必须含 IPC，不自动给普通工序增加 IPC，也不把某个 IPC 的条件扩大成整套工艺统一等待。既有必检、原始有效 FAIL 阻断、独立复核、签名和冻结证据仍按适用工序的既有合同保留。

当前 `ProcessPrototype.vue` 的 `validateDraft()` 仍强制要求存在完成条件为「检验结论合格」的 IPC 工序；这与本轮明确纠正不符，登记 **BPM-IPC-002 / HIGH / OPEN**。历史截图不改写，识别布局不表示批准这条旧规则。后续复用时必须纠正，不能照搬整文件就声称保真。

| 核对项 | 用户识别的原型 | 当前事实与判定 |
|---|---|---|
| 工序工作区 | 上方完整路线、左侧工序清单、右侧当前工序配置 | 正式页是工艺路线、工序表单、IPC、参数四个顶层页签，未交付该工作区。BPM-DESIGN-001 / HIGH / OPEN。 |
| IPC 在路线中的位置 | OP03 过滤 → IPC01 中间产品检验 → OP04 灌装 | 本轮实际打开工艺 1，只有六道工序；检验项 KCL_ASSAY 挂在 OP03 过滤。不存在原图中的独立 IPC01 工序。不能据此说整个系统没有任何 IPC 能力。 |
| 当前工序配置 | 工序设置／记录表单／参数与判定三个局部分区 | 原型中 IPC 有质量检验员、检验结论合格、中间产品检验记录及检验项。当前正式页未按该上下文组织。 |
| 按工序判断后续进入条件 | 用户明确：有无 IPC、该工序判定及前置关系决定；非整套工艺统一必检 | 源码已有 IpcService.requireComplete：适用工序的必检原始 PASS、独立复核和有效签名；ExecutionService.complete 调用质量门禁后才推进后继，startGates 检查前置工序。此为 SOURCE_REVIEW，本轮没有执行真实 IPC 合格／不合格／待复核链路，不能标端到端 PASS。 |
| 需求和验收覆盖 | 需追踪完整生产工艺及 IPC 岗位任务 | 现 product-spec 2.2.0 六条需求、RTM 与 170 格采证未包含 IPC。29 个组件测试和 53 个浏览器检查不能抵消这一遗漏。BPM-IPC-001 / HIGH / OPEN。 |
| 生命周期冲突 | 历史原型带工艺版本、提交审批、新建版本 | BASIC_CURRENT_DEFINITION_CONTRACT 已明确工艺维护当前定义，停用旧版本动作；不能因恢复界面顺带恢复旧审批。此冲突须在确切交接中处理，不能默默删改用户体验或冻结合同。 |

## 修改范围

本次用户纠偏后只记录证据、差异、Planning 与任务状态；没有按错误候选稿继续修改正式工艺页、mes-demo 源码、后端或业务数据。既有第40–41批 CPP 标签与 V2 主色修复保留，累计41，不自动开展第42批。无新增迁移，已有数据、ID、关联、签名、冻结快照和历史报告保留。

## 验收证据

- 原型识别：USER_OBSERVED；准确识别上述布局和历史截图，不代签完整源码版本批准。
- 三张 IPC 原型原图及本轮正式页原图：已实际查看；同为1280宽度的设置图与正式页可直接比较。
- 正式5173：管理员会话认证200，工艺1和IPC页签真实打开；初始曾有401记录，未冒称控制台清洁。
- 源码：`ProcessDetailView.vue`、`model.ts`、`ProcessCommands.java`、`IpcDefinitionRules.java`、`IpcService.java`、`ExecutionService.java` 定向核对；既有 IPC 定义、执行与质量门禁可复用。
- IPC 新增保存、排序/前置变更、检验记录、独立复核、签名、未检/不合格阻断及合格后进入下道工序：本次 NOT RUN。
- 原型工作区保真与 IPC 整体 G5：REWORK_REQUIRED，不能以 CPP 局部验收代替。

## 结论与下一步交接

生产工艺尚未完成。产品经理技能已调用，但完整原型追踪、IPC 需求覆盖及 G4/G5 反向验收执行不足；这是产品工作遗漏，不是仅剩测试工具问题。

后续工作保持同一任务和历史计数：先以用户识别的工作区逐项补回原型→业务→源码→验收映射，明确 IPC 工序和既有质量门禁、表单/CPP/eBR 的关联；对历史审批等冲突形成最小兼容合同；绑定确切可复用原型源码及详细设计，再集成真实页面。不得重新另画一套、只增加一个 IPC 页签、重做已证实的登录测试或凭累计测试数宣称交付。

后续验收必须分别覆盖：无 IPC 的合法工艺；普通工序按自身完成条件判断；含 IPC 时在对应工序执行适用检查；未检/不合格/待复核时阻断依赖该工序的进入；满足条件后按既有前置关系推进。不得把这些场景合成「所有工艺必须有 IPC 并合格」一条断言。
