# Material detail/create/edit — taste refinement

2026-10-06. User authorized taste-based layout refinement after the screenshot audit. Business authority FINAL BASELINE COMPLETE v1.0.18; visual authority Global UI V2 / T2 create-edit / T3 detail. Ready for human review; no commit/push.

## Delivered

- Existing material name/code/status in object header; semantic status tag.
- Removed repeated basic-information section title; quieter dividers and aligned two-column sections.
- Reduced form spacing and further compacted read-only rows; read-only required markers hidden.
- Sticky bottom cancel/save area targets the existing form through HTML form association, retaining native validation, existing command, change reason, hidden payload values, optimistic lock and idempotency semantics.
- Scoped all selectors to material-detail-page; no global shell/theme modifications.
- Eleven-field presentation whitelist and order retained. No API, DTO, backend, database, migration, state, permission, signature, allowedActions or authority change. No new images/workbench features or UI V3.

## Validation

Typecheck PASS; affected material/resources unit suites 9 PASS; final targeted Playwright Chromium desktop flow 1 PASS (10.3s), covering list/detail/supplier/edit/save/create. Footer in viewport before and after scroll, header material name, absent read-only required markers, field order, hidden payload retention, overflow and page/console errors checked. Final build PASS (see current execution result); inherited LOW bundle-size warning remains.

Rendered actual Vue with existing API-shaped fixtures, not a live database workflow. No unrelated full regression. First visual capture exposed incorrect Vue scoped-global selector compilation; corrected to material-root-prefixed CSS, and reran visual and save-flow gates. Full-page images can show the viewport-sticky footer over the stitched document; desktop viewport images are the primary visual evidence.

## Screenshots

- [Detail desktop](material-detail-desktop.png)
- [Edit desktop](material-edit-desktop.png)
- [Create desktop](material-create-desktop.png)
- Full-page variants: material-detail.png, material-edit.png, material-create.png.

## Remaining limits

No CRITICAL/HIGH issue identified within this refinement scope. Material type remains the existing contract-backed string; no enum dictionary is invented. Full accessibility compliance and production data variants were not asserted. Supplier/conversion command semantics and their existing lower-page layouts remain unchanged. Local database configurations and installed skills remain unrelated working-tree changes.
