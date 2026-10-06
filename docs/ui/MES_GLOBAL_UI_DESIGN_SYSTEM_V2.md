# Hospital Pharmaceutical MES — Global UI Design System V2

Status: **APPROVED GLOBAL VISUAL BASELINE**  
Approved: 2026-10-05  
Applies to: all current and future Vue business pages.  
Business authority remains: **FINAL BASELINE COMPLETE v1.0.19** and approved DCPs, as governed by [PROJECT_BASELINE.md](../PROJECT_BASELINE.md). The approved Global UI Design System V2 visual rules remain unchanged.

## 1. Design intent

The approved QA Batch Release concept (first generated variant) is the global visual reference: restrained Apple-like clarity, professional pharmaceutical MES density, strong information hierarchy, white surfaces, blue navigation/action accents, explicit GMP status semantics, and minimal decoration.

This is a **visual/design-system baseline**, not a new business contract. It must never add, remove, rename, reinterpret, or bypass frozen fields, states, permissions, routes, APIs, signatures, audit evidence, gates, or workflows.

## 2. Global shell

- Desktop sidebar target: 224–232 px expanded; collapsible.
- Header target: 56 px.
- Content background: very light neutral blue/gray, not pure white.
- Content gutter: 24 px desktop; 16 px tablet; 12–16 px mobile.
- Breadcrumb is secondary navigation and must not visually compete with page title.
- Sidebar uses domain grouping rather than a flat list where permissions expose many modules.
- Header and sidebar remain visually quiet; the current task/page is the focal point.
- Avoid large uninterrupted dark blocks unless required by an approved shell variant.

## 3. Typography

Preferred UI stack: Inter / system UI / Segoe UI / Microsoft YaHei / PingFang SC / sans-serif.

- Page title: 20–22 px, weight 600–700.
- Object/business identifier: 17–20 px, weight 600–700.
- Section title: 15–16 px, weight 600.
- Body/control text: 13–14 px.
- Secondary/meta text: 12–13 px.
- Helper text: 12 px minimum.
- Avoid tiny low-contrast text for regulated evidence.
- IDs, hashes and digests may use a legible monospace stack where useful.

## 4. Spacing and rhythm

Use a small consistent spacing scale: 4 / 8 / 12 / 16 / 20 / 24 / 32.

- Page sections: 16–24 px vertical gap.
- Card internal padding: typically 16–20 px.
- Form row vertical rhythm: 12–16 px.
- Dense evidence tables may use 10–12 px cell padding.
- Do not create large empty areas merely to fill the viewport.

## 5. Surfaces

- Cards: white or near-white.
- Radius: 8–10 px.
- Prefer subtle 1 px neutral borders and background separation over heavy shadows.
- Shadow, when used, must be soft and low contrast.
- Do not nest multiple visually heavy cards.
- Important blockers may use a very light semantic tint, never a saturated full-card fill.

## 6. Controls

- Standard control height: 32–36 px.
- Labels and controls stay horizontal on desktop and mobile unless the approved template explicitly requires another layout.
- Standard edit label width: approximately 110–130 px depending on viewport/template.
- Required fields use a restrained red marker.
- One primary action per local decision area.
- Secondary actions use default/ghost treatment.
- Destructive/reject actions must be visually distinct but not compete with the primary safe action.
- Disabled regulated actions must explain the unmet prerequisite nearby.

## 7. Status semantics

Canonical visual semantics:

- Blue: primary action / active navigation / informational state.
- Green: PASS / complete / available / successful.
- Amber/orange: pending / warning / incomplete prerequisite.
- Red: blocked / rejected / failed / destructive.
- Gray: disabled / historical / inactive / neutral.

Use text + icon/tag; never rely on color alone.

## 8. Tables

- Standard row height target: ~44 px.
- Header uses subtle neutral background.
- Primary business identifier is visually stronger than metadata.
- Actions remain compact and right aligned where practical.
- Empty state must explain what is missing or the next valid action when relevant.
- Preserve query filters and pagination across normal detail navigation where the page contract requires it.

## 9. Forms

- Query forms are compact and scan-friendly.
- Create/Edit forms use logical sections; do not render a single undifferentiated wall of fields.
- Desktop form pages normally use two columns when field semantics permit.
- Mobile becomes one column but retains readable label/control pairing.
- Long reason/comment fields may span full width.
- Business references use pickers/search selectors, not raw database ID entry.

## 10. Regulated/GMP UX

- Gate state, signature state, review state and evidence completeness must be visible before the user attempts a controlled action.
- Do not hide why a controlled action is disabled.
- Corrections, invalidations, reversals and historical revisions must remain visually distinguishable from current values.
- Audit/evidence views favor readability and traceability over decorative density.
- QA decision pages must answer immediately: which object, current state, can it proceed, what blocks it, and what action is available.

## 11. Responsive behavior

- Desktop is the primary operational target.
- Tablet/mobile must remain usable for review and lightweight operations.
- At <=900 px, multi-column forms/work areas may collapse to one column.
- Do not allow label/control pairs to degrade into inconsistent page-by-page layouts.
- Wide evidence tables use controlled horizontal scrolling rather than shrinking text below readable size.

## 12. Prohibited visual drift

Codex/agents must not:
- invent page-specific fonts, colors, spacing systems, radii or shadows;
- introduce a new global visual language for one module;
- use decorative gradients/glassmorphism/neon effects;
- convert independent Query/Create/View/Edit routes into ad-hoc modal CRUD;
- create a seventh business page structure without approved design change;
- copy the QA Decision Workbench layout onto unrelated CRUD pages;
- treat generated reference imagery as authority over frozen business contracts.

## 13. Reference implementation principle

The approved first QA Batch Release concept is the visual reference for hierarchy, density, status treatment, card restraint, evidence presentation and decision clarity. It is **T6 Decision Workbench**, not a universal page wireframe.

All pages inherit this Design System and select exactly one approved page template from `MES_PAGE_TEMPLATE_STANDARD_V2.md`.
