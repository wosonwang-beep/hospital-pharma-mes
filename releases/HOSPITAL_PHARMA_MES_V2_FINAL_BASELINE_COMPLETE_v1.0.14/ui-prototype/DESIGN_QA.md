# Design & Browser QA — UI Prototype Baseline V1.0

Date: 2026-09-29  
Primary evidence viewport: 1440×1024  
Browser: Codex in-app Chromium browser

## Browser gate

| Check | Result | Evidence |
|---|---|---|
| Page identity | PASS | expected document title and `h1` for every scene |
| Nonblank render | PASS | all 13 scenes expose meaningful shell and domain content |
| Framework overlay | PASS | no error overlay; static dependency-free app |
| Fresh console health | PASS | 0 error/warning entries across all 13 routes |
| Route separation | PASS | Query → `/master/materials/create`; View/Edit remain distinct hashes |
| 422 recovery | PASS | error summary and field state render while inputs remain |
| 409 recovery | PASS | conflict banner requires reload and prevents silent overwrite |
| eSignature | PASS | QA Release opens dedicated GxP dialog with meaning/reason/identity |
| Responsive smoke | PASS | 1024×768 Material Query body width remains within viewport |

## Screenshot gate

- 13 PNG files, each 1440×1024, captured from the validated local prototype.
- Every screenshot filename matches `UI_MAPPING.csv` and its task card.
- Screens visually inspected: Material Query, eBR Designer, Execution, Weighing/Charging and QA Release.

## Fidelity ledger

| Comparison point | Frozen target | Render evidence | Result |
|---|---|---|---|
| Shell | deep navy header/sidebar, clinical work area | consistent across all scenes | PASS |
| Palette | medical blue + white/cool gray | token values match DESIGN_SYSTEM | PASS |
| Density | enterprise table/form density | 32px controls, 44px rows | PASS |
| Page anatomy | header → trace → working surface | present on every route | PASS |
| CRUD separation | no CRUD drawer/modal | independent Material Q/C/V/E routes | PASS |
| Status semantics | text plus semantic color | success/warning/blocked tags | PASS |
| GMP interactions | audit/version/eSignature | read-only timeline and dedicated dialog | PASS |
| Workbenches | dedicated execution/designer/QA structures | task-specific layouts | PASS |

## Copy and deviation review

Above-the-fold copy introduces no marketing content or unapproved product claims. Visible values are labeled prototype demonstrations. No material visual deviations remain from the selected blue-white clinical MES direction. The prototype intentionally does not call live APIs; it demonstrates contract-bound interactions locally.
