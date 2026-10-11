# 打印业务字典、数据源及纸张设置设计锁定记录

## 当前确认与阶段 — 2026-10-10

用户已确认UI UX Pro Max四步原型；主设计委派给出最终关系契约并要求先完善同一详细设计、审核后开发。当前后端详细设计 **主设计复核中 MAIN DESIGN REVIEW；DEVELOPMENT NOT STARTED**。唯一当前增量入口：[DCP-PRINT-DATA-SOURCE-CONFIGURATION-005](../DCP-PRINT-DATA-SOURCE-CONFIGURATION-005-PROPOSED.md)，其§8记录图片与四个原型源码SHA256。旧静态图、V2/V3及旧PENDING文字保留历史，不再作为当前原型选定状态。

当前关系：每个组织内单据类型最多一个数据源身份；一个源可绑定多个类型；模板仍单个businessType自动解析唯一已发布源，不单独选源。独立源类型绑定不能复用mes_print_binding。已有发布模板固定源修订；草稿/绑定变化令30分钟校验证明失效。不存在无源fallback。主设计已定稿单据unitId的LIVE_LOOKUP及displayValue显式HALF_UP；新增类型仅预留，须发布revision覆盖才可打印；主设计已定稿独立类型元数据写入口/中文策略目录、专用启用恢复、多dataset安全键、不可变sidecar和RFC8785黄金样例；校验/发布重放重新授权，发布显式revisionId/configHash，等待主对话复核准确差异后下发，本轮不写业务或迁移。


状态：需求范围已获用户授权；原三页原型 **REJECTED**。修订版 **PENDING HUMAN APPROVAL**。日期：2026-10-10。

## 用户纠正交付顺序：先独立静态设计，再交互原型

2026-10-10 用户明确要求“先出设计图，再出交互原型”。既有 V2/V3 原型及哈希只保留为未批准的工程探索，不能作为已确认设计或正式集成依据。暂停修改原型和正式程序。先绘制独立静态稿：T1 数据源列表、T2 取数来源维护、T2 完整数据源配置；以收货单和出库单解释来源/主表/明细/关联/字段的完整关系。打印模板保持不变。

本次仅追加静态交付白名单：`design/系统管理/打印数据源配置/静态设计-20261010/` 内设计图、生成提示词、流程注解和交付清单；本记录和 MES_TASKS.md 本任务状态。不写 mes-demo Vue、正式 Vue/Java、API、数据库、迁移或运行配置。图稿由 Codex 编写设计说明并使用内置 imagegen 绘制，不声称是人类设计师作品或现有原型的前置稿。沿用 UI V2/T1/T2；所有筛选标签和控件同行。静态设计与后续具体原型版本均须分别经人类确认。

## 2026-10-10 修订：独立数据源配置，与打印模板同级

### 当前补齐授权：完整取数配置流程
用户明确要求“取数来源、主表、明细、关联数据和字段都要能配置……先用收货单和出库单把完整配置流程做出来，打印模板保持不变”。授权补齐原型源维护：来源目录可新增/编辑接口、响应根、记录标识和结构/示例；主数据选择来源/记录；多明细区域路径/行标识；关联配置来源/本地键/目标键/一对一或一对多/缺失处理；字段从组合后的结构加载，选择/名称/绑定键/分组/格式/启停可维护，试取按配置计算而非固定例值。系统管理源配置与打印模板同级，字典沿用既有维护。
本轮白名单追加：mes-demo/src/features/printing/CompleteDataSources.vue、completeSourceModel.ts、sourceExampleFixtures.ts、PrototypeRoot.vue入口；配套设计说明、本记录、MES_TASKS.md及本轮检查/evidence。旧原型和hash保留。仅设计原型，不改正式MES/数据库/模板/权限。普通UI预算10分钟、最多2次修复；起点2026-10-10 16:32:34。记录原型中所有示例集中在fixture文件，加载后来源/主表/明细/关联/映射通过配置保存，引擎不根据收货/出库业务编码分支。正式来源元数据从受控接口结构/API或授权视图发现，样本不替代完整结构契约；当前原型仅本地JSON结构模拟，不宣称真实HTTP接入。

V3交付2026-10-10 16:42，预算内结束。构建PASS、12项原型检查PASS；1轮静态契约修复纠正示例供应商/单位/仓库接口路径，无测试断言修改。曾一次从错误工作目录调用脚本失败MODULE_NOT_FOUND，修正为绝对脚本路径后末次12项PASS，不隐瞒该环境调用错误。源哈希：CompleteDataSources.vue `0CB0ACDF63D1EE317A61665181EC66915262CAE9E4764D66FD323D427F87F48C`；completeSourceModel.ts `8C215176A8C7434BD28B046847A3357963A6F77AD184BA425F9F3291ABEC30C2`；sourceExampleFixtures.ts `99D39FF010D19A549B9E6C90A3F0F73DB5382FD2E0491CB791D78A0FEB183583`；PrototypeRoot.vue `64BF31A5D12ED2C9D5D29ACF80DD3754F8CF3689A4A7F663DB9E901CC43856FD`。证据mes-demo/evidence/printing-source-v3-20261010，独立说明同目录PRINT_CONFIGURATION_UI_SPEC_20261010.md。状态PENDING HUMAN APPROVAL，不是正式READY/ACCEPTED；正式来源结构/权限/持久化、模板数据兼容与入库待具体版本批准后验证。审查未改正式模板/业务事实；继承包体LOW警告保留。

用户否定原设计，随后明确：“打印模板功能已经可以了，就是补齐业务类型、单据类型还有字段怎么出来”“都是要可配置的……数据源配置和打印模板应该是同级的”。原三页纸张/分类/源配置方案、哈希及证据保留为被拒绝历史，不作为当前施工依据。

本次只设计独立数据源 T1 列表和 T2 配置页。系统管理同级入口：数据字典 / 打印数据源配置 / 打印模板。模板编辑器与既有打印发布/批量/归档功能保留；不重做编辑器、不拆出纸张设置页。新增正式菜单和源配置 API/表尚未实施，需原型版本批准后有界工程集成。

正式基础数据仍采用统一 PRINT_BUSINESS_DOCUMENT 树形字典：根为业务类型、子项为单据类型。用户已明确授权插入正式数据库基础数据；本修订原型不将浏览器示例当作已入库，实际执行及证据单独记录。保持 WMS_RECEIPT / INSPECTION_REPORT / EBR_PROCESS_FORM 既有来源身份兼容。

历史关系（已被2026-10-10主设计契约替代，禁止实施）：原记录允许“一个单据多个启用源、新模板选源”。当前唯一关系为单据类型→唯一源身份；源身份→多个类型；模板单个businessType自动解析，已发布模板固定源修订。DOCUMENT/LIST仍为已有模板属性，非业务字典。

配置基本信息：源编码、源名称、业务类型、单据类型、状态；取数配置：已接入的数据读取定义、调用页面、记录标识、主数据/明细数据区域。选择读取定义自动加载字段结构。字段映射：稳定绑定键、显示名称、真实字段选择、分组、显示格式、启停。字段类型由读取契约提供，明细字段不能伪装为主字段。关联名称由受控投影或已登记关联关系提供，不把 ID 冒充名称。

取数定义、页面绑定、业务分类、单据分类、字段映射和显示元数据均为数据库配置；运行时不得用业务 enum/List.of/前端数组作为选项权威。通用读取/权限/格式能力属于程序能力。可配置不意味着执行任意 SQL、URL、JavaScript 或 SpEL。新增取数定义须通过正式业务读取契约校验和组织/权限验证；当前未支持的数据需明确待接入，不得虚构自动可用。

原型以收货/出库为例：收货字段来自已实现 WmsReceiptPrintProvider；出库基础事实来自 /material-issues/{id}，批次/物料/单位显示投影尚待正式接入。原型试取数据仅为明确标识的本地示例，按所选映射实际计算展示，不生成 PDF、不请求业务写接口。

本轮读取：AGENTS.md、MES_DEV_RULES.md、PROJECT_BASELINE.md、MES_TASKS.md、Global UI V2、Page Template V2、DCP-PRINT-TYPE-BATCH-004、现有字典 API/V039、WmsReceiptPrintProvider、IssueDetailView 与 mes-demo/AGENTS.md。权威不变；冲突结论：原设计已被用户拒绝，以本次有界方向替代未批准方案，不修改冻结发布。

写白名单：mes-demo/src/features/printing/PrintDataSources.vue、dataSourceDesign.ts、src/PrototypeRoot.vue；本记录、配套说明、MES_TASKS.md；本地 evidence 与检查脚本。预算10分钟/最多2次自动修复。禁止本轮修改正式 Vue/Java/迁移/权限/已发布模板、用户配置与其他原型。

修订版验证：2026-10-10 16:20启动，16:28完成当前检查；普通UI预算10分钟。首次typecheck/build通过；静态自查发现字段选择在全选时不可用及新增深链接未覆盖，1次实现修复改为可维护勾选集合并修复new路由；修复后13项补充浏览器检查通过，无页面异常。无重复失败，不修改冻结设计/门禁或既有测试断言。检查脚本logs/verify-print-source-v2.cjs，构建日志logs/print-source-v2-build-final.log，证据mes-demo/evidence/printing-source-v2-20261010。原型构建有继承的1.56MB入口包LOW告警；本轮不扩展为包体重构。

修订版SHA256：
- PrintDataSources.vue：`71A3D9B78A972C1452724861914C1A2ED5AF23F945DB26F2EAB893E855912BB3`
- dataSourceDesign.ts：`F92F9EA9D57AEE1D405580B962DE9C2228F751539EA14B59660BA2BCFEB0374E`
- PrototypeRoot.vue：`8194A4B695E13F060478C07E684DF1FDD0B7E32E255EE063C26DBF83AFF5D82C`

可验证范围是当前源列表/字段配置设计，读取定义新增、调用页面/关联关系独立编辑尚未原型实现；其他完整验收缺口见配套规格和MES_TASKS.md。状态IN PROGRESS / PENDING HUMAN APPROVAL；尚未正式字典入库，不是正式功能READY/ACCEPTED。当前原型文件/独立说明已交付，不能把“按这个思路设计”算作具体原型验收。

## 依据、范围与执行门禁
实际读取 AGENTS.md、MES_DEV_RULES.md、MES_TASKS.md、PROJECT_BASELINE.md、UI V2、T1–T6、DCP-PRINT-TYPE-BATCH-004、V039 字典结构、SystemDictionaryController、IncomingQualityController、三种已实现 PrintDataProvider 和 canvas-editor 的纸张命令类型声明。当前权威仍为 FINAL BASELINE v1.0.23 及批准补充。

用户授权：“补齐打印数据源配置”“单据类型也是可以维护的”“解决业务类型、单据类型的字典维护”“补齐”“这些都是可配置的，不能写死在前端或后端”。质量场景明确包括请验单、取样单、检验报告；横向按打印纸张解释。

本轮白名单：mes-demo/src/features/printing/*、mes-demo/src/PrototypeRoot.vue、mes-demo/src/main.ts（仅入口包装）、本设计记录及配套说明、MES_TASKS.md 的本任务记录。正式 MES 前后端/数据库/权限/迁移不在本轮写范围。普通原型任务预算 10 分钟，最多 2 次修复；本轮不连接正式 API/数据库。原型直接在已有 mes-demo 5174 展示。

## 数据与页面结构
复用 sys_dict_type/sys_dict_item 的 BUSINESS/TREE 及 parent_id，不新增独立字典基础设施。一个 PRINT_BUSINESS_DOCUMENT 字典：一级业务类型，二级单据类型。分类名称/编码/归属/排序/启停是数据库记录，不由前后端内置列表决定。业务分类不改全局七域导航。

字典维护 T1，新增/编辑独立 T2。树选中业务只显示其子单据；修改名称同步更新所有选择器。编码唯一，引用后稳定；只停用，不物理删除。父级停用后不能用于新模板，历史仍按冻结身份显示。预置质量/WMS/生产及其已有单据仅是初始数据库数据。新增类别无需编译部署。

数据源列表 T1、维护 T2：编码、名称、一级业务、二级单据、调用页面、单条读取接口、记录 ID 参数、明细数组路径、字段映射、状态。字段维护键/名称/返回路径/主表或明细/数据类型/启用，可增行、改名、停用；字段/接口选择依据真实读取契约，不凭空增加业务事实。正式实现只允许系统已登记、经过组织/权限审核的只读数据契约，接口地址与字段路径作为配置存储；不能配置任意外部 URL、SQL、JS、SpEL。原型接口字符串可编辑，用于说明配置而不是执行 HTTP。

质量映射：请验单 → /api/v1/quality/inspection-requests/{id}（items）；取样单 → /api/v1/quality/sampling-tasks/{id}（details）；检验报告 → 已实现 INSPECTION_REPORT 的受控投影。前两者已有业务读取接口，但完整打印适配、正式资格与引用显示还没有实现，原型不得被视为实际打印 PASS。人员/单位不能将 ID 冒充显示名称；关联字段通过受控读取投影提供。

模板设置 T2：业务类型 → 单据类型 → 启用的数据源联动，模板打印类型仍 DOCUMENT/LIST；页面设置含 A4 横/纵向、上下左右边距。297×210mm 为横向，210×297mm 为纵向；canvas 使用 executePaperDirection(HORIZONTAL/VERTICAL)，不丢正文或字段。保存/重开保留设置，Word 导入读取方向，DOCX sectPr、编辑预览与服务器 PDF 一致。原型纸张是可交互尺寸示意，不冒充 PDF 或正式 canvas 验收。

## 拟议正式契约与兼容
现有字典 CRUD、字典 options 接口复用。打印 sources/fields DTO 需从字典及配置解析；源配置新增 CRUD/校验及版本读取契约，复用 print:template:view/manage 权限，源业务权限在每条读取时仍由服务端核验。现有模板/绑定/批次与 DOCX 数据需关联冻结的配置版本，不能修改配置就重算历史输出。已发布模板沿用旧源身份/字段定义；升级生成新版本并重新验证。

源字段路径或关联发生变化时追加配置版本；已有模板继续引用冻结版本。显示名改动不变业务键。配置变更保留审计、乐观锁；旧已签署/已归档事实、电子签名和来源规则不改。正式表/列/API/OpenAPI/RTM/权限/迁移与集成依赖需在原型批准后的同一有界补充中同步；所有 Flyway 只追加、现场检查最高成功号，不预定/改写迁移编号。

## 具体原型交付和验收
入口 http://127.0.0.1:5174/#/printing/types；同一原型包含 /printing/sources、/printing/template-settings。模板 T1/T2，继承 UI V2 的 228px 侧栏、56px 顶栏、24/16/12px 页边距、32px 控件、白色表面和蓝色主操作。

验收：字典新增/改名/子项关联/编码重复校验/启停；数据源字段增改保存重开；两级联动仅显示有效选项；横向宽高切换且重开保留；PC1440/1280、窄屏900/375 标签与控件始终同行；所有数据仅保存在本机原型浏览器。已用 ui-ux-pro-max 的可访问表单错误指导，cuawright-mes 作为浏览器验证首选。build、截图/DOM/交互结果和源哈希随后追加。没有用户批准该具体文件版本前不得迁入正式 MES。

## 本轮实际证据与批准版本

构建两次通过；末次日志 logs/print-config-prototype-build-final.log。补充浏览器脚本11项检查通过：新增单据保存重开、字段改名保存重开及选择器同步、横向尺寸与重开、1440/1280/900/375表单同行无溢出、375查询同行、无页面脚本错误。CUAWright实测375查询2对标签delta0、无溢出、PASS。其默认保护阻止本地保存按钮，未改保护设置；补充Playwright拦截所有非GET/HEAD HTTP，只验证本地原型。

证据目录：D:/codex/_project/gmp/mes-demo/evidence/printing-20261010；截图01字典、02字段映射、03横向1440、04横向375；result.json、source-hashes.json。

当前状态仍PENDING HUMAN APPROVAL；正式5173/8080、数据库持久化、PDF横向输出、正式源读取/权限与历史兼容NOT RUN。父级停用、重复编码、完整新增数据源、多角色等交互尚未自动覆盖；不能称全功能验收通过。现有共享原型样式使部分表格操作/纸张方向选项纵向排列，列为待人工设计审查项；不改变测试断言隐藏问题。

批准须对应以下SHA256：
- src/features/printing/PrintingConfiguration.vue：`80156cb2be07fa47469003653e17e87f9213ad97a16874b72c65f4e70288c340`
- src/features/printing/printConfiguration.ts：`6cb77cdfae5191408af5555832c3b03d2ac6e8a6308eaa6f229884948bd3d707`
- src/PrototypeRoot.vue：`7b79a665e384e1f14982aa7c784955cc39f0c3181452519e4da77e364257bb58`
- src/main.ts：`bd58a008077cb8b93cff68c0f8a56f14aabd3718ea3f0ffd3543aece005dc341`


## 主设计批准与实施下发 — 2026-10-10T13:02:40.670492+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源：主对话01a115d2-8ace-7574-9a08-ac48dfc970cc本次委派，已审阅DCP005第1–11节及修正实体/API，批准同一文件及18操作/61schema契约作为实施基线。用户先前已明确授权主对话批准、任务做到收尾及必要DEV迁移/后端更新/测试；本记录不把主设计批准写成用户最终验收，不改历史审批。

本次白名单：mes-reporting打印领域/API/应用/持久化及直接消费provider、mes-boot定向测试与新增Flyway、正式Vue打印页面/API/路由/现有导航入口、当前同一设计/契约/复核/任务索引。现有其他菜单/权限/UI未提交改动保留，开始清单和文件备份位于本机print-source-implementation/worktree-before.txt及file-backup/。不修改旧迁移、旧归档正文/hash/触发器、冻结releases、无关业务，不提交或推送。

专项环境覆盖：本次主设计下发明确要求迁移先在隔离测试库验证，仅该验证允许专项隔离库；正式DEV仍为已有localhost:3306/hospital_pharma_mes_dev、Redis6379、后端8080、前端5173；不drop旧库、不repair旧checksum、不增加持久安全开关。真实目标/版本/备份可恢复确认后才执行DEV迁移。复杂打印集成初始预算90分钟，最多2轮自动修复，重复同根因立即报告；不改批准验收断言。当前实施开始，迁移/编译/业务/UI/PDF验证未运行，不能宣称验收或部署完成。


## 本任务修复上限有界例外及显式升级批准 — 2026-10-10T13:36:48.414288+00:00

MAIN DESIGN APPROVED / IMPLEMENTATION IN PROGRESS。来源同一主对话本次委派：用户已授权主对话全权批准当前任务并做到收尾，主设计批准DCP005复杂前后端集成对MES_DEV_RULES §6A两轮修复上限作专项放宽，只覆盖本任务兼容/契约集成，不修改全局规则、不弱化断言和安全要求；逐原因定向修复/复测，同原因复发或需要改变设计立即报告，不无限循环。原Mockito撤权拒绝断言保留；失败是夹具在thenReturn参数构造时再次stubbing，先构造夹具即可修复，非权限校验通过。复杂集成预算仍按本任务90分钟记录，不因此扩至无限。

主设计批准现有Word/Canvas保存增加可选baseTemplateVersionId与sourceSelection(mode=INHERIT|UPGRADE,targetSourceRevisionId,targetConfigHash)。默认INHERIT，首次新模板解析唯一当前已发布源，同编码后续版本继承旧引用。UPGRADE需base及显式目标、同businessType/同源、当前绑定与ENABLED/PUBLISHED/类型覆盖，hash/目标冲突409，缺字段/集合兼容422带问题；复制新模板版本并重验，不修改旧版或静默删除绑定。LEGACY转CONFIGURED必须显式UPGRADE及完整兼容；前端仅增加版本升级差异预览/确认，不提供任意换源。本增量沿同一文件身份同步契约/矩阵后实施。此前BLOCKED仅为已获专项放宽的阶段记录，不是当前终止。
