> LATEST IMPLEMENTATION STATUS / 2026-10-16: third approved window 5/10, historical20 preserved, cumulative25. Current-source second-page browser body, real text contrast, 200% text-only resizing at1440/1280/900/375, and existing limited-role403 all PASS. Frontend15/15 and build PASS; database/print-archive fingerprints unchanged. Formal G5 validator PASS with READY FOR ACCEPTANCE, 36/36 coverage, bound to the same integration manifest. No material/template/account/permission/business/archive mutation. READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING, not ACCEPTED.

# 打印集成实测 — 2026-10-15

状态：**生成链路已实测；完整集成验收 FAIL / BLOCKED / NOT ACCEPTED**。无产品源码修改。

## 基线与范围

已真实读取 AGENTS.md、docs/development/MES_DEV_RULES.md、MES_TASKS.md 打印条目、docs/PROJECT_BASELINE.md、PRINT_ONCE_DESIGN_LOCK_20261012.md、当前打印 OpenAPI 与 Vue/Java 实现；业务依据为 v1.0.23 + 已批准打印补充，视觉仍为 UI V2/T1–T6。复用既有打印模板列表 G5 结果，不将列表结果扩大到业务打印全流程。Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max。

## 已核实现状

当前没有页面级打印入口配置 UI。现有「打印模板」和「打印数据源配置」管理模板、单据类型与取数；业务页按钮及记录 ID 仍需代码接入通用组件。详见 [入口核验](PRINT_ENTRY_CONFIGURATION_FACTS_20261015.md)。

## 已完成实测

- 使用现有 5173/8080、正常本机凭据、收货单 22，无业务/配置写入。
- CUAWright 真实页面打印返回 HTTP 200、application/pdf、no-store；45,479 B，SHA256 `e3d4600c9109c8e41ea7d340ac141f3a0dab27175f3bd8405f97f3969d4015dd`。
- 模板 69「收货记录打印」V2，PUBLISHED；CONFIGURED，数据源 19、发布版本 22。前端默认选第一项，提交明确的 templateVersionId。
- 1440/1280/900/375 取消关闭未发打印请求；生成时两次真实连续点击仅一个 POST；重开已清空预览。
- 检验报告 11 的无模板提示存在、生成按钮禁用；未创建/发布模板补足场景。
- PDFBox 将浏览器返回的同一文件渲染为 1 页，中文标题、供应商、氯化钾、批号及 50000.000 克可读。API 单张 PDF 47,644 B；双记录 PDF 88,991 B、2 页，顺序 25→22；第 1 页纯化水/200000.000 毫升、第 2 页氯化钾/50000.000 克，中文、批号、单号匹配现有记录。3 页原始分辨率人工视觉检查与字段断言均已留证。
- 7 个实际 8080 API 用例中，6 个与预设状态一致：单张200、双记录200、空选择400、重复记录400、无效记录404、匿名401。错单据类型返回422 PRINT_STATE_INVALID，预设400不一致，原始失败保留。原 controlled-printing.openapi.yaml 对未绑定/不适用模板记录422，新 print-once.openapi.json 对状态失败写409，存在文档映射不一致；没有修改任何断言或合同使其通过。
- 浏览器及 API 前后：10 artifact、9 batch、1/0 source-evidence、66 signature、123 PRINT% audit 均不变；模板摘要与来源引用不变。

## 当前限制与待完成

- 自动浏览器内嵌 PDF 区域仍为空白；已验证 PDF 文件真实可渲染，不能据此宣称页面预览可用。沿用历史停止的原生 PDF 弹窗项目，不重试。
- 真实受限账号 403 未执行；匿名 401 不能替代权限负例。
- G5 已执行并保留失败：17/44 个矩阵截图单元已采集并逐张按原始分辨率查看，27 个状态/视口单元缺失；27 条维度记录与19项 WCAG 保留未完成项；真实受限账号403、键盘/缩放、服务错误UI恢复、完整表单锚点/目标尺寸及批准参考同视口对照未齐备。validator exit1。报告与集成 JSON 绑定同一份 audit-manifest.json，整体 FAIL。
- 1440/1280 下侧栏浮层覆盖打印弹窗下载区域；375 收货单/批号长值裁切；检验报告模板选择只有 placeholder，无持续可见标签。均留证，不修改页面。几何检查只确认文档宽度等于视口、打印模板 label/select 中心线偏差0px，不覆盖上述肉眼问题。

## 当前最短配置步骤

1. 系统管理 → 打印数据源配置：选现有数据源，确认其适用单据类型、主表/受控关联、中文字段及已发布版本。一个数据源可适用多个类型；每种类型仅一个启用数据源。新增底层目录/读取授权策略仍需后端代码注册，页面不能任意执行SQL。
2. 系统管理 → 打印模板 → 新增/编辑：选单据类型与「单据/列表」，使用该类型中文字段排版并保存草稿。每个模板只属于一个单据类型。
3. 模板列表「更多」→「验证并生成PDF」→「发布」→「业务绑定」，选择启用绑定。这些是现有UI操作，本次仅核验，未执行保存/发布/绑定。
4. 已接入页面：WMS → 原辅料收货记录，列表选记录或打开详情 →「打印选中记录」→ 选择模板 →「生成打印预览」。当前已有模板69，无需重新配置就能生成现有收货单PDF。

业务页接入仍需代码，例如详情传 `business-type="WMS_RECEIPT"` 和 `[record.id]`，列表传 selectedKeys；后端返回可用模板清单，前端默认第一项并允许切换，POST提交明确 templateVersionId。当前没有可配置任意页面按钮、ID取值或路由的「页面级打印入口配置」界面，也没有独立的通用业务打印执行中心页。不能将建议描述为已实现。

## 交付、验证与修复限制

实际新增：入口核验/本报告、3个 print_integration_*_20261015.py 工具及同名证据目录。无迁移、业务数据/配置/权限修改、产品源码修复、提交或推送；保留并行未提交变更。产品编译/单元测试 NOT RUN（本轮只验证现有运行服务，无产品代码改动）。浏览器运行 exit0；实际HTTP脚本 exit0 仅表示完成，原始状态断言6/7不能写成7/7。PDFBox exit0。完整 G5 validator exit1。

累计轮次保守记 **10/10**：继承7轮；第8轮本机客户端TLS/JSON只读取证适配，第9轮加载态按钮定位及稳定截图，第10轮清单字段/状态格式适配。全部是测试工具或证据格式修正，产品源码修复0次；初始环境/脚本失败和初版validator失败均保留。停止任何进一步修复，不能换线程、换测试名或重置轮次。当前任务仅交付问题及建议；继续修复需明确限定下一阶段范围。

最小后续建议：先用受支持的可见浏览器核实PDF插件/内嵌渲染边界；在现有shell/modal契约内处理侧栏浮层遮挡；按批准V2补持续可见水平标签与长值恢复；由权威合同确认409/422后再单独修改对应契约或实现；提供授权受限账号后补权限负例。不添加页面级配置功能或重设计业务流程。

证据目录：`docs/review/evidence/print-integration-20261015/`。入口事实、run2/http-journal.json、http/results.json、pdf-render/content-checks.json、audit-manifest.json、manifest-validation.txt、integration-acceptance.json 共同支持上述结论。报告完成不等于打印全流程 READY/ACCEPTED。

## 后续批准窗口进展（旧结论原样保留）

用户 Sentinel_f9b5368819c0819187a09f7ce1332c59 已批准继续。当前窗口第2/10轮，总累计12：已确认默认shell无PDF插件，已安装Chrome的原生容器也为空白；菜单popup1050高于modal1000；原始打印合同和异常映射支持422，补充文档误写409。已实现同一PDF本地PDF.js预览、打印modal局部层级、迟到请求隔离、Blob错误消息与持续可见标签；已纠正当前OpenAPI，未改后端异常规则。类型检查和真实浏览器重测正在进行。没有T1/共享布局/权限/业务数据修改，尚未宣称修复通过。后续阶段报告：[PRINT_REPAIR_WINDOW_20261015.md](PRINT_REPAIR_WINDOW_20261015.md)；机器进展：`docs/review/evidence/print-repair-window-20261015/stage.json`。

追加进展：窗口4/10、总累计14。类型检查已PASS；真实默认浏览器已显示中文PDF，四视口下载命中、键盘焦点/Escape、重复点击、取消重开及真实迟到请求隔离已PASS。唯一新失败为下拉可访问名称混入placeholder，已保持断言并修正aria-label；正补真实下载摘要与重测。T1/共享布局/权限/业务数据未改，完整G5尚未完成，仍不得整体READY。

当前已知结果：真实双记录列表打印/两页预览/键盘第二页/下载摘要一致；HTTP7/7、后端15/15、typecheck/build PASS。前端和主浏览器检查各9/10，同一可访问名称修复未成功，依据重复失败规则停止该项；真实403仍NOT RUN。窗口仍4/10、总累计14，整体BLOCKED；详见追加窗口报告，旧报告失败不覆盖。

## 当前源码最终复核（覆盖上段阶段结论）

2026-10-15窗口10/10、累计20：可访问名称及异步禁用断言后续已按批准范围恢复并通过，当前定向前端4文件15/15；AppLayout仅在折叠时清空受控openGroups，四视口不再残留菜单popup。当前真实单条PDF下载45,481B，下载与响应SHA256同为`a1e13d5ffcff188151a788f066ef69ec94abb0aafd84b1b2c6aa194835f04f5d`；双记录25→22为88,989B/2页，下载与响应SHA256同为`412288b463c1ce2028cbb77a6c3690123649df53d3babc652702672f795c47bf`。后台15/15、HTTP7/7、typecheck/build均重新PASS，数据库/打印归档状态前后相同。窗口10只纠正HTTP证据的旧轮次元数据，当前达到20轮总上限并停止后续自动修复。

Formal G5清单已更新为`docs/review/evidence/print-repair-window-20261015/audit-manifest.json`，validator exit0且27/27截图覆盖；清单自身verdict=`FAIL`，集成报告绑定同一清单，原因是无现成授权受限账号可完成真实403，且新鲜对比度/200%纯文本缩放保守记NOT RUN。结论仍`BLOCKED / NOT ACCEPTED`，不能因其他套件通过写READY。详细命令、轮次、视觉复核及文件见[追加窗口报告](PRINT_REPAIR_WINDOW_20261015.md)。

## 2026-10-16 第三批准窗口覆盖结论

旧段落是历史阶段事实；当前正式结论由 `docs/review/evidence/print-repair-window3-20261016/audit-manifest.json` 与同目录 `integration-acceptance.json` 覆盖。第二页实际浏览器正文、文字对比度、200%纯文本缩放及既有受限账号真实403均PASS；G5为36/36、validator exit0、`READY FOR ACCEPTANCE`。本轮没有后端/API/数据库迁移；受影响前端15/15和build重新PASS。状态为 `READY FOR ACCEPTANCE / HUMAN ACCEPTANCE PENDING`，不可写为已人验接受。

### 当前源码快速接入

业务详情页复用共享打印组件，传冻结合同中的 `businessType` 与 `[record.id]`；列表页将当前 `selectedRowKeys` 转成字符串数组传给 `recordIds`。组件调用 `/printing/applicable?businessType=...` 集中获取该单据类型已发布且启用绑定的模板，默认第一项并允许切换，生成时显式提交 `templateVersionId`。检验报告页复用 `InspectionPrintPanel` 并传报告ID和现有审批状态。业务页只提供入口、单据类型与业务ID，不复制模板/字段/数据源逻辑。

系统管理中的配置顺序不变：打印数据源配置允许一个数据源覆盖多个单据类型，但每个单据类型只启用一个数据源；打印模板中每个模板只属于一个单据类型并使用中文字段；模板验证生成PDF后再发布并建立启用绑定。当前没有页面级按钮/路由/ID映射配置界面，本次也没有新增。运行时仍为当前模板+当前授权业务数据→内存PDF→返回/下载→结束，不新增打印内容归档、快照或模板历史使用。
