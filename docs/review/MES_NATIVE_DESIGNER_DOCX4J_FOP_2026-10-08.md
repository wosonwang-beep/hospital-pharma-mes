# MES 内置可视化打印设计器 + docx4j/FOP — 2026-10-08 实施和验证

## 用户批准范围

用户连续确认：
1. “PDF 转换接口采用docx4j + FOP方式”
2. “MES 内置可视化模板设计器”

适用当前医院制剂 MES 已批准打印基础组件，保留 FINAL BASELINE v1.0.23、Global UI V2 / T1–T6 和已签 GMP 历史。批准 DCP：`docs/development/DCP-MES-NATIVE-DESIGNER-JAVA-PDF-002-APPROVED.md`。原强制在线 Word 编辑器/LibreOffice 的技术前提被新打印技术路线取代，但历史原始 DCP 不重写。

## 已实施

- 新 Vue T2 设计器 `frontend/mes-web/src/views/printing/NativePrintDesigner.vue`：左侧中文字段搜索/控件，中央 A4 预览，右侧字号/对齐/文字/循环表列，点击插入、拖动/上下排序、保存原因。PC/Mobile 样式。模板管理 T1 新建按钮和已有版本可视化编辑入口。
- 后端 `NativePrintDesigner.java`：严格白名单 TITLE/TEXT/FIELD/TABLE/DIVIDER，防任意 SQL/JS、表达式；最多 80 块、每个 8–28pt、最多 8 循环列，必须包含报告编号和草稿/正式字段；生成 Apache POI A4 DOCX，保留表头重复、PAGE/NUMPAGES，将有限布局说明写入 DOCX 自定义属性，以供原版新草稿编辑。
- 仍用现有 `PrintService.uploadFromEditor` 创建独立 DRAFT 修订，沿用 `mes_print_template_version`、组织/权限/audit、immutable published artifact、preview/validate/publish/bind、正式报告批准和签名校验、最终 PDF SHA-256 归档。当前只支持 `INSPECTION_REPORT`；未新增数据库表。
- 新默认 `Docx4jFopPdfConverter`：docx4j 11.5.12 + Apache FOP 将真实 DOCX 转换为 PDF，不启动 Office 或网络服务。用户选用 CJK 系统字体，由部署运维提供授权字体；DOCX 0/超 5MB/无效拒绝，PDF 需 PDF 文件头、≤25MB，有限并发门闸。旧 `LibreOfficePdfConverter` 仅显式 `mes.print.converter=libreoffice` 时可选；默认 unset 自动 docx4j，旧引擎不是必要部署依赖。Maven 统一 transitive PDFBox fontbox/pdfbox-io 至现有 MES 3.0.8，未禁用 Enforcer。
- 3 个新增打印 API：GET `/printing/designer/default?businessType=INSPECTION_REPORT`；GET `/printing/templates/{id}/design`；POST `/printing/designer`。契约已追加 `docs/api/controlled-printing.openapi.yaml`。新的 native DOCX 本身是源设计的可编辑版本，上传普通 DOCX 的复杂设计不能自动还原为内置结构，这种情况仍可打印和只读预览，避免有损转换。

## 已完成的验证（非模拟转换）

- `NativePrintDesignerTest` **3 PASS**：中文块 JSON/DOCX 双向读取、白名单、文本注入拦截、loop 表头保留。
- `PrintLifecycleTest` **11 PASS**，新增“对已有发布版本另建新 DRAFT 且旧版本不变”的 Mockito 生命周期场景，未以 Mock 当成实际持久保存。
- `PureJavaPdfSmokeTest` **4 PASS**（`mes-native-designer-end-to-end-java.log` 已先前通过其中 4 个）：docx4j/FOP 真正生成中文 PDF，样例 47,249 字节；75 行旧 DOCX 真正转换跨 8 页；75 行**内置设计器生成 DOCX**转换 PDF 共 **13 页、76,533 字节**，最后的“检验项目75”从 PDF 文本确认存在；非法/空 DOCX 被拒绝。
- 上述 3 份测试合计 **18/18 PASS**，Maven exit 0。
- 前端全量 Vitest **110/110 PASS（30 suites）**，见 `mes-native-designer-frontend-unit.log`。
- 前端 vue-tsc + Vite 正式 build PASS；后端 **mes-boot 多模块 `-DskipTests compile` PASS**（包括 Maven Enforcer PDFBox 依赖收敛检查），见 `mes-native-designer-frontend-build-final.log`、`mes-native-backend-full-compile-final.log`。
- PC/Mobile Playwright `e2e/native-print-designer.tmp.spec.ts` **2/2 PASS**；真实 admin 登录、临时浏览器级 GET 字典和 POST 草稿保存 Mock，验证中文字典、字段插入、循环表、提交新版本 JSON。没有实际生产写操作，不等同于线上用户真实保存版本。
- 新增的 rollback-only Spring / MariaDB IT `backend/mes-boot/src/test/java/com/hospital/mes/reporting/NativePrintDesignerIT.java` 原计划校验真实 DRAFT→VALIDATED（真实 FO PDF）→PUBLISHED→BOUND；**未能执行测试方法，因为 Flyway 校验在应用上下文启动前报错，绝不能记录为通过**。

## 当前阻断：Flyway V033 校验和不一致（HIGH）

`mes-native-designer-real-db-it.log` 显示：
```
Migration checksum mismatch for migration version 033
Applied to database : 1576484630
Resolved locally    : 595506057
```
早前测试已在持久 DEV `hospital_pharma_mes_dev` 自动应用 V033 打印模块；当前未提交的 V033 文件在并行开发期间发生变更，文件内容与已应用版本不同。此为**真实迁移完整性问题，不是设计器业务功能失败**，但会阻止当前 Flyway 正常启动，且可能妨碍其它模块测试和后端重新部署。

依照 `AGENTS.md`，**禁止 `flyway repair`、禁用校验、改写数据库历史、编辑已应用 V033 后伪装成功、重建/清空 DEV**。必须从有证明的原始 V033 已应用内容恢复完全一致的字节，再用新迁移（V034+）承接追加改动；若无法找回原件，提交独立受控数据库变更/修复方案由项目所有人批准。检查 `flyway_schema_history` 及文件不能替代解决这一差异。本轮没有对 V033 作更改或任何 `flyway repair`。

## 未完成/不应宣称

- 新版 Java 后端 **尚未打包部署到**当前运行的 8080（旧 PID 1968），且 V033 校验问题阻断安全重启。Vue Vite 4174、MariaDB 3306、Redis 6379 仍运行；无需停止或替换外部服务。
- 实际数据库版本保存、模板真实发布/绑定、检验报告批准后生成正式 PDF/历史重打的全链路验证尚未成功（Spring 测试被 V033 阻断）。用户不应将前端 Mock 2/2 当成实际数据库集成。
- 复杂原有 DOCX 与 FO 引擎版式还原并非 100% 与 Word 一致。当前确认了中文、跨 13 页行完整性，仍需批准模板的正式排版核对、字体、页眉表头及与存档签名一致性复核。
- 旧 MES GxP HIGH（生产 QC 个人资格 Gate 缺口、演示制剂强度/处方表观 5 倍差异、历史人员资格登记时间）保持 OPEN；本打印任务没有修改受控 QC/QA 签名或历史。

**状态：限定范围源码和无数据库测试完成；真实业务/DB 集成验收 BLOCKED（V033 CHECKSUM）。未提交、未推送。**
