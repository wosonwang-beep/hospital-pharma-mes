# Controlled Word printing — validation in progress

Status: IN PROGRESS / NOT READY FOR ACCEPTANCE. Scope and approval: DCP-CONTROLLED-WORD-PRINTING-001. Environment PC-202401011703, main 8f61f75. Existing user modifications retained; no commit/push/deploy.

| Requirement / RTM | Implementation | Current evidence |
| --- | --- | --- |
| Versioned template / whitelist / secure DOCX | PrintService, DocxGuard, ContentControlFields, V033 | ControlledDocxTest + PrintLifecycleTest; SQL not executed |
| Browser Word with Chinese fields / loop columns | PrintEditorService, Word plugin, T3 editor | EditorSafetyTest + live-service-missing UI; actual Office save not verified |
| Complete report and formal approval | InspectionPrintProvider, backend ID-only command | Provider compiled; real DB/signature integration pending |
| Immutable archive / old version reprint / same PDF | PrintService + InspectionPrintPanel + PdfPreview | Lifecycle tests; browser fixture preview/download byte hash equality pending |
| True DOCX to PDF / Chinese / multipage / table header / footer / signature | poi-tl + isolated JODConverter/LibreOffice | Actual installed LibreOffice RealPdfTest, PDFBox assertions and page PNGs |
| Empty data / illegal expressions / permission denial / timeout | Guard + lifecycle + real converter deadline | Focused tests; actual timeout returns no artifact and cleans temp |
| Migration / organization / concurrency / database triggers | V033 + scoped locking/store | NOT RUN: no authorized isolated database target |

LibreOffice official installer signature valid (The Document Foundation), SHA256 verified;26.2.6.3 executable works. Installer exit3010 requested restart; computer not rebooted. Windows named-pipe transport was unavailable; switched to bounded per-job loopback UNO transport. Initial PDF text assertion incorrectly assumed no extraction whitespace; corrected to whitespace-normalized presence, without changing required item75 or pagination checks. Initial 25-page example verified complete; visual review led to improved column proportions, final regeneration pending.

Focused backend previously21 tests passed including real Chinese multi-page/empty PDF/content controls; latest real suite4 passed including actual timeout and directory cleanup. Frontend typecheck/build passed; API/navigation6 passed. Browser6/8 passed initially; report-detail render blocker under diagnosis. Final counts/results to be updated after final verification.

No fake conversion is counted as real conversion. Mockito lifecycle tests verify orchestration only. Browser routes are isolated fixture APIs and use an actual previously generated LibreOffice PDF; they do not verify server persistence, actual business data or real Office editing. No real DEV data is created or altered.

Remaining gates: JWT secret approval/configuration; private mutually reachable Office service deployment; live Word upload/cursor insertion/loop/save/callback/new-version/publish/business PDF workflow; isolated V033 application/trigger/transaction/concurrency tests and target safety review; design-authority consistency review. No existing migrations or accepted task statuses changed.

## 2026-10-08 V034 数据库及 DEV 部署续报（覆盖上文“未执行/受阻”的历史状态）

当前最新记录见 `docs/review/MES_V034_DATABASE_AND_BACKEND_DEPLOY_2026-10-08.md`。前文 “V033 not executed”、“Flyway checksum blocked” 是原审查时点记录，**不再代表当前 DEV 运行状态**：原三表版本 V033 内容现与数据库执行校验和 1576484630 一致；新物理 V034 已成功应用并含编辑会话表、6 个防篡改触发器、独立打印生成权限和 SYSTEM_ADMIN 补授权；数据库 Flyway 34/34 校验 PASS，原历史 checksum 不变。

后端已打包并切换 8080 到独立不可变 V034 JAR（PID21896，包含精确打印 API Security 权限匹配修复）。真实 Spring/MariaDB 模板创建→生成中文 PDF→验证→发布→绑定的事务测试 1/1 PASS 且回滚确认，开发数据库保留原业务数据。管理员打印模板/中文字段/原生设计器 PC+Mobile HTTP 实际 200 的浏览器测试 2/2 PASS，无关 QC 岗位的打印资源 403 浏览器测试 2/2 PASS；既有七岗位 21 次真实 GET 权限隔离矩阵 PASS。真实业务签名受控报告模板的人工版式对照/审核发布归档验收仍待完成，**不能将本次技术部署记作整个打印模块的最终 GMP 验收**。
