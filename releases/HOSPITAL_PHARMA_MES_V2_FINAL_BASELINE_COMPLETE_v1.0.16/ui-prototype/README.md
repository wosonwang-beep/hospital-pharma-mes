# UI PROTOTYPE BASELINE V1.0 — FROZEN

Status: **FROZEN / REVIEWED / TRACEABLE**  
Primary viewport: **1440 × 1024 desktop**

## Run

From `ui-prototype/prototype/` run `./serve.ps1`, then open `http://127.0.0.1:4173/#/audit`.

The prototype is dependency-free and uses only local static assets. Hash routes are intentional so it can run from an archive or any static server.

## Truth hierarchy

- Business behavior: FINAL BASELINE > UI V1.1 > prototype.
- API, permission and state: FINAL BASELINE is absolute.
- Visual: frozen screenshot > DESIGN_SYSTEM.md > implementer judgment.
- Interaction: UI V1.1 plus this runnable prototype.

The prototype never authorizes a business action. Visible actions are demonstrations of server `allowedActions` combined with permission checks.

## Contents

- `prototype/` — runnable interaction source.
- `screens/` — 13 frozen visual sources, one for each MES-001–013 R2 domain.
- `UI_MAPPING.csv` — route/requirement/task/API/permission mapping.
- `DESIGN_SYSTEM.md` — locked tokens, layout and component rules.
- `DESIGN_QA.md` — browser, visual, interaction and consistency evidence.
- `FREEZE_NOTE.md` — scope, exceptions and sign-off statement.
- `tests/` — contract tests for routing and traceability.

## Mandatory implementation rules

1. Query/List pages only query, reset, sort, paginate, export and navigate.
2. Create/View/Edit are independent routes. Never replace them with CRUD modals or drawers.
3. View is read-only. Approved/history objects create a new version rather than mutate.
4. Edit uses version/If-Match; 409 forces reload, and 422 retains inputs and identifies fields.
5. GxP state actions use a dedicated signature dialog and immutable audit events.
6. eBR Designer, Renderer, execution, weighing, charging and QA release remain dedicated workbenches.


## V1.0.16 route control

Formal routes are `/audit` and `/integration/operations`. `/platform/operations` and PLAT-001 are removed. Only API-backed audit and integration data appears in the production mapping; sample values in other scenario mockups remain prototype-only.

