# Global T1 desktop query density adjustment — 2026-10-13

Status: **IN PROGRESS / HUMAN RECHECK REQUIRED**

## 1. Baseline

- `AGENTS.md`
- `docs/development/MES_DEV_RULES.md`
- `docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`
- `docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md`
- `docs/development/decisions/GLOBAL_T1_T2_UI_UNIFICATION_20261013.md`
- User clarification 2026-10-13: this project has no mobile layout requirement; validate this adjustment at 1440 / 1280 / 900 desktop widths.
- Review executor: `Codex SELF-REVIEW using mes-ui-ux-audit and ui-ux-pro-max`.
- `ui-ux-pro-max` SHA256: `8B827FE03B349AEEA99A7D6CF4CCD892E6065B672859FDB11E13A1919453EDDC`.

## 2. Design difference and implementation

Previous shared T1 query grids used equal `minmax(0, 1fr)` columns. Controls expanded to consume the full row and local page overrides often forced Reset / Query into a separate full-width row.

The shared T1 rule now:

- uses a wrapping flex row of atomic label/control pairs;
- uses approximately 170 px text inputs, 150 px selects, and 190 px entity/reference pickers;
- applies those widths to direct controls and shared wrapper components; dictionary selects no longer collapse to an arrow-only box;
- keeps each label and control on the same horizontal line;
- keeps Reset / Query as one atomic action group and places it on the same row whenever desktop width permits;
- allows complete pairs and the action group to wrap when the row genuinely cannot fit;
- preserves all fields, routes, APIs, permissions, query semantics and button order.

Modified files:

- `frontend/mes-web/src/global-ui-v2.css`
- `docs/development/decisions/GLOBAL_T1_T2_UI_UNIFICATION_20261013.md`
- `MES_TASKS.md`
- this review record

No database, migration, backend, API, permission, route or business-data change.

## 3. Targeted evidence

Visual-first screenshots:

- Print data source 1440 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861401848_t1-print-data-source-1440-final.png`
- Print data source 1280 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861401925_t1-print-data-source-1280-final.png`
- Print data source 900 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861402005_t1-print-data-source-900-final.png`
- Print template 1440 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861267205_t1-print-templates-1440-after-r5.png`
- Production order 1440: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861298799_t1-production-orders-1440-after.png`

Browser checks:

- Print data source at 1440: four condition groups plus Reset / Query remain on one row.
- Print template at 1440: four condition groups plus Reset / Query remain on one row.
- At 1280 / 900, complete pairs wrap without splitting labels from controls; no document-level horizontal overflow.
- `check_horizontal_labels`: all inspected pairs report `delta_px = 0`, `same_row = true`, `no_horizontal_overflow = true`.
- Real GET query with a non-matching keyword produced the zero-result state; Reset restored two rows. Mutation guard remained disabled and no business write occurred.
- Representative live pages inspected: print data source, print template, roles, production orders, WMS receipts and QC specifications.
- Dictionary-wrapper regression: supplier qualification status, material type, organization type and equipment type all render at the shared 150 px width. Material type was opened and all four options were visibly readable.
- Reference-wrapper regression: unit conversion, WMS supplier/warehouse, material-request and production-batch filters retain their shared 190 px width.
- Supplier 1440 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861664792_supplier-dropdown-fixed-1440.png`
- Material 1440 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861666077_material-dropdown-fixed-1440.png`
- Material 1280 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861667335_material-dropdown-fixed-1280.png`
- Material 900 final: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861668578_material-dropdown-fixed-900.png`
- Material dictionary options open: `C:/Users/Administrator/.codex/tools/cuawright-mcp/evidence/1791861703020_material-dictionary-options-fixed-1440.png`

Automated checks:

- `npm run typecheck`: PASS, exit 0.
- `npm run test -- --run src/App.test.ts src/platform-contract.test.ts src/master/resources.test.ts`: PASS, 3 files / 6 tests, 0 failed, 0 skipped.
- `git diff --check -- frontend/mes-web/src/global-ui-v2.css docs/development/decisions/GLOBAL_T1_T2_UI_UNIFICATION_20261013.md`: PASS; CRLF warning only.
- Current `global-ui-v2.css` SHA256: `A124343EDE4266821EB87AA86AFEF9A76226E1E56A72A7F1A4941BDA4FDA0FF6`.
- Current design-lock SHA256: `87820DCCD805D89EE40EE1C50AA47D0DA940F56FD03572D164E73753930564BA`.

## 4. Review result

The compact query treatment fixes the reported oversized controls and reduces avoidable action-row wrapping. Labels, controls and actions retain readable hierarchy and visible focus behavior inherited from Global UI V2. Representative default, empty and reset states are visually usable at the three approved desktop widths.

Remaining evidence is intentionally `NOT RUN`: the complete inventory of every T1 route at all three desktop widths, every loading/service-error/permission state, keyboard-only traversal, WCAG 2.2 evidence set, adversarial second pass for every route, complete audit manifest validation, full frontend regression and human visual confirmation. The earlier full G5 manifest corresponds to the pre-change source hash and cannot be reused.

## 5. Conclusion

**IN PROGRESS / HUMAN RECHECK REQUIRED.** Targeted implementation and representative desktop validation pass. Do not claim `READY FOR ACCEPTANCE` or `ACCEPTED` until the current-source complete G5 desktop matrix and manifest are run and the human confirms the result.
