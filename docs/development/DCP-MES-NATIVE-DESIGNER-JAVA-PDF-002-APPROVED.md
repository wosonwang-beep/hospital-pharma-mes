# DCP-MES-NATIVE-DESIGNER-JAVA-PDF-002 — 用户确认技术路线（2026-10-08）

## Approval and bounded delta

User explicitly selected **“PDF 转换接口采用docx4j + FOP方式”** and then **“MES 内置可视化模板设计器”**, superseding the prior DCP-CONTROLLED-WORD-PRINTING-001's *mandatory* ONLYOFFICE web Word + LibreOffice engine direction for new templates. Approved intent: ordinary users create/edit native visual templates from Chinese-labeled business fields on the MES page, without installing Office or connecting a separate document-editing server. Keep DOCX as the canonical printable version to preserve existing printing, immutable archive and server-side data/sig evidence gates.

Scope is restricted to the printing subsystem, published permission vocabulary, and existing inspection report provider; no new business type, recipe, GMP signature/qualification rule, warehouse/production change or UI V3. FINAL BASELINE COMPLETE v1.0.23 remains the governing baseline and previous frozen release bytes remain immutable. This DCP is an additive approved delta; final baseline pointer changes require separate consistency closeout.

## Data and version model

Reuse already applied V033 `mes_print_template_version`, `mes_print_binding`, `mes_print_artifact` and audit/signature infrastructure; **no migration** and no new table, no editing applied migrations. A new `NativePrintDesigner` receives only bounded JSON block types `TITLE`, `TEXT`, `FIELD`, `TABLE`, `DIVIDER` (1–80 elements) with 8–28pt font and LEFT/CENTER/RIGHT alignment. All business fields/columns must be from `PrintDataProvider.fieldDefinitions` (Chinese label and stable technical key); no arbitrary HTML, SQL, expression evaluation, access to front-end data snapshots, or unbounded loops. `modeLabel` and `reportNo` are mandatory.

Backend Apache POI renders a legitimate A4 DOCX with required `{{approvedField}}` placeholders and `{{items}}[approvedItemKey]` table; page footer uses PAGE / NUMPAGES. Embed native design JSON in DOCX custom document property for later *bounded round-trip editing* while keeping a single canonical artifact. Legacy imported/complex DOCX continues to be printable and immutable, but without native designer metadata it is not representable for native editing. No silent lossy conversion of arbitrary DOCX.

`POST /printing/designer` creates an independent DRAFT revision via existing `PrintService.uploadFromEditor`, including organization/permission checks, content hash, current version/history locking and audit. Original PUBLISHED and VALIDATED revisions do not change. `GET /printing/templates/{id}/design` is scoped, read-only and returns existing template metadata plus native design. `GET /printing/designer/default` returns bounded default. Existing validate(PDF) → publish → bind → draft/formal generate → hash-verified stored PDF read is unchanged. Formal mode still requires existing report approval and valid electronic signature; backend only accepts business IDs and template IDs for generation.

## Converter choice

Default `mes.print.converter=docx4j` (also default when unset) activates `Docx4jFopPdfConverter`, backed by open-source docx4j-export-fo 11.5.12 + Apache FOP. No LibreOffice/ONLYOFFICE executable, DLL/UNO, ports or additional DB/Redis instance is required for PDF conversion. The previous `LibreOfficePdfConverter` is retained **only when explicitly configured** with `mes.print.converter=libreoffice` as a bounded compatibility option for legacy complex DOCX; not a deployment prerequisite and not an automatic fallback. No external HTTP conversion.

Conversion rejects zero/over-5MB DOCX input, serializes concurrent jobs and fails closed on failure; PDF output must be `%PDF-` and ≤25 MB, otherwise no valid artifact. DOCX upload and designer render both pass the existing DocxGuard. Server CJK fonts are an **explicit deployment quality prerequisite**, not the shipping of proprietary font files; the tested Windows machine has compatible system fonts. On new Linux hosts, install appropriately licensed CJK fonts and verify rendering, line wrapping and pagination. FO/Word fidelity for arbitrary imported styles is not guaranteed and must be tested against approved templates before declaring a formal production deployment.

## UI and permissions

Use Global UI V2 and existing T1 template list; the new designer is a T2 grouped editing page. Header: template code, name and reason. Left: Chinese field search and component toolbox; center: A4 page-like preview with visible generated example values; right: selected field/text/table properties, 8–28pt text, alignment, loop columns. Click-to-insert at selected position, up/down and native drag/drop reorder; saved document remains editable via embedded design. PC/mobile responsiveness required. The native editor does **not** require ONLYOFFICE. Existing optional advanced Word editor route stays available for original imported DOCX but its external service is not required for native design or PDF.

Permission mapping unchanged: `print:template:view` for dict/default/existing scoped design, `print:template:manage` for new template/edit/save; separate `print:template:publish` and `print:document:generate` remain mandatory. No new automatic admin/QA grants. No modifications to electronic-signature records, activity audit or existing archived PDFs.

## Evidence and open acceptance boundaries

Native designer roundtrip/whitelist/anti-injection 3 PASS; mocked PrintService new draft revision preserves original published 1 PASS; Java pure-Java PDF real tests 3 PASS including invalid input, Chinese title/first and 75th result, long table spanning 8 pages. Reporting module default tests PASS. PC/mobile mocked browser UI 2 PASS for Chinese field choice and submitted draft metadata. These are tests, **not** a claim that current running 8080 service has been redeployed or that a user has persisted an actual formal business artifact.

Formal acceptance still requires deploying new backend (with correct dependencies and fonts), verifying actual admin/user permission grants, real database template DRAFT→PDF VALIDATED→PUBLISHED→BOUND with authentic incoming inspection report, actual immutable PDF archive/reprint, concurrency and hash/audit evidence, checking PC/mobile layout and font fidelity. V033 was already applied in earlier testing; no new schema action under this DCP. Any new business fields, signatures, product-specific visual features or migration require separate approval.

## Contract pointers

- API: `docs/api/controlled-printing.openapi.yaml` plus 3 additive native designer routes.
- Implementation: `backend/mes-reporting/.../NativePrintDesigner.java`, `Docx4jFopPdfConverter.java`, `PrintService.java`, `PrintController.java` and `frontend/mes-web/src/views/printing/NativePrintDesigner.vue`.
- Tests: `NativePrintDesignerTest`, `PrintLifecycleTest.nativeVisualDesignerAppendsNewDraftWithoutOverwritingPublishedTemplate`, `PureJavaPdfSmokeTest`, `e2e/native-print-designer.tmp.spec.ts`.
- Previous DCP-CONTROLLED-WORD-PRINTING-001 retained for immutable record, but its mandatory office-server deployment premise is superseded for native templates; no history deletion.
