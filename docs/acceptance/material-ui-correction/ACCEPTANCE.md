# Material names and inline form correction — 2026-10-03

Authorization: explicit user instruction to delete 通用名/英文名/别名 and enforce side-by-side labels/controls. DCP-MATERIAL-NAMES-UI-001 in cumulative v1.0.8 records the bounded contract; previous v1.0.7 retained unchanged.

## Implementation

Removed genericName, englishName and aliasName from material creation/update DTOs, public material data/snapshots, frontend create/view/edit fields and keyword search. Legacy physical columns/audit history remain intact and ordinary edits preserve their stored values. No new migration; executed V001–V011 unchanged.

Historical implementation used block labels and then page-specific overrides instead of one consistently enforced layout. v1.0.7 still included the old stacked prototype style; MES-006 sidebar fields used another stacked grid. Previous functional browser checks did not assert label/control geometry. This correction removes the obsolete master block rule, explicitly applies shared non-wrapping query rows, aligns sidebar inputs and updates the current design system/runnable prototype. Inherited PNGs are marked historical; current implementation screenshots are attached here.

## Targeted evidence

- Frontend vue-tsc/Vite build PASS; existing bundle-size warning only.
- Material frontend tests: 5/5 PASS (1.59s).
- Native MariaDB targeted integration: MaterialSupplierIT 3/3 PASS (9.081s), covering create, edit/replay/stale behavior and removed names rejected/hidden/unsearchable while legacy values preserved. ProcessIT direct Material→BOM/UOM regression 1/1 PASS (1.288s). Test records use rollback; no physical migration or persistent schema change.
- Browser plugin not available; existing Playwright used. Two material create/edit browser cases PASS. Two geometry cases PASS (desktop/Pixel 5, 15.7s) traverse seven master list routes and seven create forms each: materials, suppliers, organizations, units, unit-conversions, equipment and qualifications. Each label/control pair is measured for horizontal placement/vertical overlap, and inputs remain usable. Three removed names are absent. MES-006 reason/reauth fields also measured inline. Screenshot inspection PASS.
- Initial geometry runner incorrectly searched the literal two-character Query button while Ant Design's accessible label contains a space. Corrected locator only; geometry assertions retained, checkbox minimum width correctly reflects existing 16px controls. Reran only the affected geometry cases, not full regression.
- OpenAPI 1,424 references valid; four material schemas contain none of the retired properties. v1.0.7 checksum mismatches=0.

## Review and readiness

Scoped self-review: no remaining CRITICAL/HIGH findings. Commands no longer assign or include retired columns in UPDATE; history/audit payloads are not rewritten. Existing material states/UOM/supplier preference, permissions and routes retained. No new technical debt; inherited bundle-size warning and before-merge CI requirement remain. MES_TASKS.md alone tracks readiness. No commit/push/local credential staging.
