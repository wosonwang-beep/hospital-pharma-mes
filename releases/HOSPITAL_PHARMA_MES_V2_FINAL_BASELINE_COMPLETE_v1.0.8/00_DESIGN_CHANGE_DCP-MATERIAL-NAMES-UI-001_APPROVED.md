# DCP-MATERIAL-NAMES-UI-001 — APPROVED, 2026-10-03

Explicit user instruction: “通用名，英文名，别名删除”, with correction of MES-003–005 labels/control alignment. No further authorization is needed for this bounded correction.

- Retire genericName/englishName/aliasName from material create/update DTOs, public responses/query snapshots, keyword search and create/view/edit UI. Unknown-field rejection remains in effect. Do not clear legacy column values during ordinary edits.
- Retain generic_name/english_name/alias_name physical columns and existing audit/history solely for historical preservation. No new Flyway migration or physical deletion; V001–V011 remain immutable. Removed names do not participate in current business commands or consumers.
- Current requirement is labels left, controls right in the same row on desktop and mobile. Query forms and master edit forms share the layout, including inline reason fields. MES-006 action inputs also reuse this arrangement. Mobile reduces the number of field groups per row, never stacks label above input.
- Synchronize the active design system and runnable prototype, preserving older releases and their screenshots as immutable historical evidence. Existing screenshot files in this cumulative release are identified as inherited until refreshed.
- No change to material state, units/conversion, multiple suppliers/one preferred, permissions/routes, audit/signature, or subsequent tasks. Product/process consume unchanged material identity/UOM/availability data.
- Verify native MariaDB create/edit/retired-field rejection and historical preservation, affected process consumption, frontend build and focused desktop/mobile geometry checks. No full regression.
