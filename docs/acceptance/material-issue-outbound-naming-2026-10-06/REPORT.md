# MaterialIssue UI outbound naming

2026-10-06. Explicit user authorization: rename UI 发料管理/发料单 to 出库管理/出库单, reuse MaterialIssue contract. Business v1.0.18 / Global UI V2 / T1-T3 unchanged.

Display changes: sidebar 出库管理; execution reference 出库单; route meta titles 出库管理 / 新增出库单 / 编辑出库单 / 出库单详情; list/document headings, single-number/quantity/unit/time/item/confirmation/save/navigation text; production evidence and ISSUE enum display labels. Current UI blueprint terminology aligned, frozen release artifacts unchanged.

Source delta verification PASS: all seven changed source files differ only by approved Chinese text replacements and route metadata titles. No route path, variable, API, payload, permission, backend, database, migration or logic changes. Local database configuration changes are pre-existing and excluded.

Typecheck PASS. Two distinct targeted desktop Chromium scenarios PASS: issue selectors/create submit (exact original IDs and payload asserted); existing eligibility guidance remains available. Menu and create/detail titles explicitly asserted in final command-contract rerun. Screenshot inspected: issue-create-chromium-desktop.png. No page/console errors in these fixtures. API-shaped browser fixtures render the actual Vue; no live database acceptance or full regression claimed. No new technical debt identified in this bounded text change.

Current readiness is maintained in MES_TASKS.md; no automatic commit or push.
