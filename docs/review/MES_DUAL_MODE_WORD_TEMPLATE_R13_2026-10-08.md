# MES 打印模块双模式兼容审查 — R13（2026-10-08）

## 用户要求与结论

用户原定优化重点：Word 表格复制粘贴 → 尽量保留合并单元格/样式 → 单元格内绑定中文字段 → 实际 DOCX/PDF 对照。后提出：现有复杂 Word 模板建议增加直接 DOCX 原文件导入以保留 OOXML 结构，并要求两种模式兼容。

**结论：兼容，且必须采用双轨架构，不能为了统一 UI 而将复杂 Word 原文件强制转换成受限 JSON。** 当前主业务合同 `PrintService`、`PdfConverter`、`mes_print_template_version`、模板验证/发布/业务绑定/签名/归档哈希、可重复打印沿用，不新增 DB 表或修改 V033。

### A：高保真原文件 DOCX 模式

- 既有 `POST /api/v1/printing/templates` 上传安全检查通过的 DOCX，原始字节保存到一个全新 `DRAFT` 修订；保留原始 OOXML 表格、合并单元格、段落、边框、原始字体和复杂版式语义。不得自动转成简化的 NativePrintDesigner JSON。
- 原导入 DOCX 可下载、受控验证 PDF、审批发布、绑定和从既有 PDF 重打。上传完不再强制跳到需 ONLYOFFICE 的编辑器；列表展示版本结果。原 DOCX 若没有 `mes.native.design.v1` 自定义属性，无法在内置简化模式做无损编辑，应提示保留原文件并使用原 DOCX 编辑再上传新版本。
- `PrintLifecycleTest.importedOriginalDocxRetainsExactOoxmlBytesWithoutNativeConversion` 证实上传 DOCX 对象逐字节保留；原已发布版本状态保持 `PUBLISHED`，不触发转换器、不发生静默 native 改写。
- **未完成的增强**：对任意已导入 DOCX 提供在现有 OOXML 表格原位插入中文字段/内容控件的受控单元格定位工具。该功能需要表格索引/单元格稳定锚点、生成新版本、不修改既有格式和严格原版对照，不能宣称已经完成。

### B：Word 表格复制粘贴模式

- 前端 `wordTableClipboard.ts` 从 Word/WPS 粘贴事件的 `text/html` 中仅解析静态表格（不执行或保存 HTML）：水平/纵向合并、1–12 列、1–30 行、最多 160 个单元格、列宽百分比、可识别底色/边框/字号、加粗/斜体/下划线；禁止脚本、任意模板表达式、嵌套表格及过大输入。图片和未识别边框发出警告。
- Vue `NativePrintDesigner.vue` 的可聚焦粘贴区、中央 A4 表格画布、单元格选中、右侧中文字段绑定、文字编辑与表格拖动排序，导出时使用仅含合法静态值的 `WORD_TABLE` 数据块（无 HTML）。每次保存新建受控 DOCX 草稿，不覆盖历史。
- 后端 `WordTableComposer.java` 对前端结构逐项重新校验并生成**真实** OOXML `gridSpan`、`vMerge`、`tblGrid`、行高、背景色、边框、富文本 run 和单元格内白名单 `{{fieldKey}}` 标记。沿用现有服务端 PrintDataProvider / docx4j+FOP 生成 PDF。
- 粘贴格式的可还原范围由浏览器剪贴板 HTML 决定：Word 浮动图形、嵌套表格、文本框、复杂域、段落细节、完整分页及某些边框样式并不支持；遇到此类情况应改选 A 模式，绝不能伪称原样导入。

### 两种模式共通的发布链

`DRAFT → VALIDATED（实际 PDF 及人工核对）→ PUBLISHED → BOUND → 草稿/正式业务 PDF → 不可变归档与 SHA-256 校验`。

权限保持 `print:template:view`、`print:template:manage`、`print:template:publish`、`print:document:generate`，正式报告仍需原业务审批及有效 QA 电子签名。保留 `docx4j + FOP` 作为默认纯 Java 转换器；用户无需安装外部 Office 或 ONLYOFFICE。

## 已实际运行的测试（R13）

| 验证 | 结果 | 证据 |
|---|---|---|
| Word HTML 粘贴解析 4 项 | 4 PASS | `mes-word-paste-parser-green.log` |
| Word 合并/富文本真实 OOXML/PDF/非法属性 3 项 | 3 PASS | `mes-word-table-backend-green.log` |
| 贴入表格后实际 PDF | 58,245 B，绑定文字/合并标题均保留 | 同上 `WORD_PASTE_MERGE_FOP_PDF` |
| 打印模块全部单元测试 | 39 total / **34 PASS** / 5 opted-out skip / 0 fail | `mes-dual-print-modes-java-final.log` |
| Vue 前端全部单元测试 | **114/114 PASS** | `mes-dual-print-modes-vue-unit.log` |
| 前端正式 vue-tsc + Vite | PASS，存在既有大型 bundle 警告 | `mes-dual-print-modes-vue-build.log` |
| PC+手机真实登录后粘贴、绑定和提交设计 JSON | **2/2 PASS**，表格载荷与创建新版本请求使用测试专用 mock，未写真实 DB | `mes-word-table-paste-pc-mobile.log` |
| mes-boot 含后端全模块 compile（保留 Enforcer） | PASS | `mes-dual-print-backend-compile.log` |

此处是**代码+单元+模拟浏览器流程验证**，不能作为“真实医院 Word 模板像素级保真”或“完整业务验证通过”。仍需上传真实医院原始 DOCX，用 Windows Word 原生打印结果对照 docx4j/FOP 产生的 PDF（优先逐页结构、合并网格、行高、列宽、边框、页码/页眉及关键签署字段），定义差异容许标准。字体与分页引擎不同可能导致不可消除的视觉差异。

## 尚待修复的重要阻断

上轮已确认 Flyway V033 在开发库 `hospital_pharma_mes_dev` 的记录与当前源码校验和不一致（数据库 applied `1576484630` / 本地 resolved `595506057`），因此**真实 MariaDB 模板生成→验证→发布→绑定集成测试仍然 BLOCKED**；不得 `repair` 或禁用校验，必须由受控迁移恢复/后继迁移方案处理。

当前运行在 8080 的仍是旧后端服务，代码未重新部署；Flyway V033 阻断下不可强行重启。Vite 4174、MariaDB 3306、Redis 6379 保持原服务。其它遗留的生产 QC 人员资格 Gate、演示制剂标示强度不一致、历史 QA 人员资质证据不完整仍需受控审核。

**工作区：main；保留并行改动；未提交、未推送；没有运行任何 @Commit 业务 Seed，也没有修改已签名业务证据。**
