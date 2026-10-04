# Clinical Blue MES Design System — V1.0 FROZEN

## Visual character

Deep navy application chrome, medical blue actions, true white work surfaces and cool gray workspace. The system is information-dense, controlled and low-decoration. It must feel like a validated hospital application, not a marketing dashboard.

## Tokens

| Role | Value | Usage |
|---|---:|---|
| Navy 900 | `#0B1F3A` | top application bar |
| Navy 800 | `#102B4E` | primary navigation |
| Medical blue | `#1677FF` | primary actions, selected state, focus |
| Workspace | `#F4F7FB` | page background |
| Surface | `#FFFFFF` | cards, tables, forms |
| Border | `#DBE3EE` | separators and control borders |
| Text | `#17243A` | primary content |
| Muted | `#65758B` | metadata |
| Success | `#16865A` | valid/completed/released |
| Warning | `#C57910` | attention/pending |
| Critical | `#CF3C4F` | blocked/rejected/error |

Typography uses `Inter, Segoe UI, Microsoft YaHei, Arial`. Page title 21px/700; card title 13–14px/700; body 13px; labels 11–12px. Monospace is reserved for IDs, routes, hashes and API values.

## Geometry and density

- Top bar 58px; sidebar 216px; content gutter 22px.
- Base spacing 4px; common gaps 8/12/16/24px.
- Control height 32px; table row 44px; radius 5–8px.
- Cards use a 1px cool-gray border and restrained shadow.
- Frozen canvas is 1440×1024. Primary content must remain visible without horizontal scroll.

## Component contracts

- `PageHeader`: title, controlled context, page-level navigation actions only.
- `TraceStrip`: requirement, route, API and permission; visible in the prototype as QA evidence.
- `SearchPanel`: maximum two rows before expansion; Query/Reset right aligned; Enter queries.
- `ResultTable`: compact rows; action column navigates to routes; no CRUD overlays.
- `FormPage`: sectioned fields plus fixed footer Save/Cancel actions.
- `ViewPage`: status/version, read-only descriptions, state actions and audit/revision entry.
- `StatusTag`: dot plus text; color never carries meaning alone.
- `GxpSignatureDialog`: the only modal allowed for signature; includes meaning, reason and identity.
- `Workbench`: three-region execution surface with steps, live record and gates.

## Accessibility

Maintain 4.5:1 body-text contrast, visible blue focus rings, text labels for every semantic color, 32px minimum controls and keyboard activation for primary interactions. Animation is not required to understand status.


## Current form alignment — DCP-MATERIAL-NAMES-UI-001

Query and edit fields use horizontal label/control pairs. Desktop edit groups occupy two columns; mobile groups occupy one column, each retaining a left label and right control. Labels are right-aligned (130px desktop / 100px mobile). Current material forms omit genericName, englishName and aliasName. This overrides inherited stacked-field geometry; see the approved DCP and current page specification.
