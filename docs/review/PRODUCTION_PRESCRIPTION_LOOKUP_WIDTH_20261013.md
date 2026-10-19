# Production prescription shared lookup width correction — 2026-10-13

Status: **IN PROGRESS / HUMAN RECHECK REQUIRED**

## Scope and cause

- Baseline: Global UI V2, Page Template Standard V2 T2 and `GLOBAL_T1_T2_UI_UNIFICATION_20261013.md`.
- Review executor: `Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max`.
- Reported defect: product and batch-unit selectors on `/production/prescriptions/create` collapsed to arrow-only controls.
- Root cause: the shared `ProcessLookup` component did not define a full-width outer wrapper or full-width inner Ant select. T1 query CSS masked the omission on list pages, while T2 forms exposed it.

## Implementation

- Added the missing width contract to `ProcessLookup`: block/full-width wrapper, full-width inner select and bounded error presentation.
- Added explicit business column widths to the production-prescription ingredient table so material and unit selectors cannot be reduced to arrow boxes when an editable row is added.
- Per the user-confirmed annotated screenshot, moved the single “＋ 添加原辅料” action from below the table to the right side of the “配料明细” card header and changed it to the blue primary treatment. The duplicate lower action was removed.
- No business field, API, permission, route, database, workflow or GxP contract changed.

## Targeted evidence

- 1440 product selector opened with both product names/codes visible: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862517540_prescription-create-product-options-fixed-1440.png`.
- 1440 ingredient row: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862519316_prescription-create-item-columns-fixed-1440.png`.
- 1280 ingredient row: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862519395_prescription-create-item-columns-fixed-1280.png`.
- 900 ingredient row: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862519471_prescription-create-item-columns-fixed-900.png`.
- Header action 1440 empty state: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862748306_prescription-add-material-header-primary-1440.png`.
- Header action 1280 empty state: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862749493_prescription-add-material-header-primary-1280.png`.
- Header action 900 empty state: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862750669_prescription-add-material-header-primary-900.png`.
- Header action after adding a row: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791862752540_prescription-add-material-header-after-add-1440.png`.
- Visual-first inspection: base-form product and batch-unit selectors fill their control columns; product options are readable; added ingredient-row material and unit selectors have usable widths; no page-level horizontal overflow. At 900, the 980 px ingredient table remains inside the existing horizontal table scroller.
- Visual-first action inspection: the blue add-material button is right-aligned in the card header at 1440 / 1280 / 900 and remains in that position after a row is added; the previous lower-left duplicate is absent.
- The generic horizontal-label checker does not recognize the page-specific `.prescription-form`, returned zero pairs and therefore is not counted as a PASS. Visual evidence and source inspection were used for this targeted check.

## Automated checks

- `npm run typecheck`: PASS.
- Targeted Vitest: 6 files / 13 tests PASS, 0 failed, 0 skipped.
- `git diff --check` for the two implementation files: PASS; CRLF warning only.

## Result

Targeted defect is corrected. Full current-source T2 route/state inventory, adversarial pass and formal G5 manifest remain NOT RUN, so this report does not claim `READY FOR ACCEPTANCE` or `ACCEPTED`.
