# Global UI V2 Phase 1 — approved implementation plan

User confirmed: global visual foundation only (Shell, typography, palette, spacing, cards and forms); retain business page structures. No Phase2/audit PhaseA functionality, new routes, APIs, database or regulated transitions. v1.0.16/DCPs govern business; the approved Global UI Design System V2 governs visuals.

1. Add one final global CSS layer with V2 tokens. Existing CSS remains for business layouts. App ConfigProvider consumes those same CSS values for Ant components including dialogs/drawers.
2. Set expanded shell228px/header56px, restrained light navigation and header, responsive gutters24/16/12px. Preserve route and permission navigation logic; IA regrouping is outside this phase.
3. Apply typography20–22/15–16/13–14/12px minimum, spacing scale, white restrained8–10px cards, controls32–36px and horizontal form labels. Keep T1 query/list, T2 form, T3 detail, T4 aggregate, T5 execution and T6 decision structures independent. QA remains T6, Material listT1/createT2. No T7.
4. Browser plugin absent in available skills; use repository Playwright Chromium desktop/mobile. Exercise shell collapse/navigation, T1 query/reset, T2 entry/label alignment, T6 evidence/status/actions and representative controlled signature modal. Save final screenshots and check console/overflow/token sizes.
5. Run affected build/typecheck and scoped browser regression once; fix actual failures with focused reruns. Record readiness in MES_TASKS.md and save phase evidence. Preserve local configs; no commit/push unless separately requested. STOP after Phase1.
