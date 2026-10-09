# Hospital Pharmaceutical MES — Page Template Standard V2

Status: **APPROVED**  
Depends on: `MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md`  
Business authority: FINAL BASELINE COMPLETE v1.0.23 + approved DCPs, as governed by [PROJECT_BASELINE.md](../PROJECT_BASELINE.md). The approved T1–T6 template rules remain unchanged.

Every Vue business page must declare/select one of T1–T6. A page may contain subordinate components from another pattern, but its primary structure must remain one template. If none fits, stop with **DESIGN CHANGE REQUIRED**; do not invent T7.

Approved [shared-navigation supplement](../development/DCP-GLOBAL-NAVIGATION-001-APPROVED.md): all T1–T6 shells, including Production Batch T4 and execution T5, inherit the same domain menu configuration, order and permission filtering. Page structures and routes remain unchanged.

## T1 — Query / List

Use for: Material, Supplier, Equipment, Production Order, Main Batch, QC lists, WMS lists.

Structure:
1. Page Header: title + short context + one primary create action when permitted.
2. Query Card: compact filters, Reset + Query.
3. Result Card/Table.
4. Pagination / result count.

Rules:
- Preserve filters/pagination where contract requires.
- Use Select/Picker for controlled enums/references.
- No inline full-record editing.
- View/Edit navigate to independent routes.

## T2 — Create / Edit Form

Use for: master data creation/edit, receipt editing, controlled definition editing.

Structure:
1. Back navigation + Page Header.
2. Logical form sections.
3. Two-column desktop form where appropriate.
4. Full-width long text/reason areas.
5. Sticky or stable action area: Cancel + Save/Submit.

Rules:
- Label/control horizontal pairing.
- Required/help/validation are local to the field.
- Business references use selectors.
- Do not mix read-only history into the editable form body unless necessary for the decision.

## T3 — Business Detail / Controlled Document

Use for: Receipt, Inspection Request, Sampling Task, Sample, Inspection Task, Inspection Report, Deviation.

Structure:
1. Object header: document number/name + state tag + contextual actions.
2. Key facts summary.
3. Document sections / line items / evidence.
4. Controlled action area driven by server allowedActions + permission.
5. History/audit links.

Rules:
- State transition actions are not generic Edit.
- Explain blocked actions.
- Historical evidence remains read-only.
- Keep the user's place when returning to the originating list.

## T4 — 360° Aggregate Detail

Use for: MaterialLot, MainBatch and other aggregate roots requiring cross-domain lineage.

Structure:
1. Aggregate header + current quality/production/inventory state.
2. Compact key facts.
3. Tabs or segmented navigation across bounded views.
4. Read-oriented cross-domain evidence with deep links to source records.
5. Trace/Audit entry.

Recommended MaterialLot tabs:
Overview / Receipt / Inspection Request / Sampling / Samples / Inspection / Reports / Release / Inventory / Production Use / Audit-Trace.

Rules:
- This page aggregates; it must not create a second mutable Source of Truth.
- Controlled writes remain on their owning T2/T3/T5/T6 pages.
- Use existing source APIs/trace contracts where possible.

## T5 — Execution Workbench

Use for: ExecutionUnit, Operation execution, Weighing, Charge, IPC, eBR runtime.

Structure:
1. Production context header: batch/product/execution/current operation/state.
2. Process/operation navigation or progress.
3. Primary execution workspace.
4. Supporting context: equipment, material, parameter, IPC/eBR evidence.
5. Clear operational controls: Start/Pause/Resume/Complete etc., permission + allowedActions governed.

Rules:
- Current task/action is visually dominant.
- Do not present execution as generic CRUD.
- Show prerequisite/gate failures before action where possible.
- Preserve frozen process/eBR snapshot identity.
- Device/manual provenance and signatures must remain visible.

## T6 — Decision Workbench

Use for: QA Material Release, QA Batch Release, other formal review/release decisions.

The approved first QA Batch Release concept is the reference implementation.

Structure:
1. Decision object header: identifier, product/context, state.
2. Gate completion summary/progress.
3. Main Gate Checklist.
4. Sticky/strong Decision Summary.
5. Allowed Action area.
6. Evidence/archive/history area.

Rules:
- First glance must answer: what is being reviewed, whether it can proceed, what blocks it, and the permitted decision.
- PASS gates are compact; blockers receive stronger semantic emphasis.
- Disabled Release/Approve action explains unmet prerequisites.
- Evidence is grouped by domain rather than a long flat accordion.
- PDF/archive history is a first-class evidence section when the frozen contract requires it.
- Reject and Release remain distinct controlled actions with required reason/signature semantics.

## Special surface — Dashboard

Dashboard is not T1–T6 business CRUD and may use KPI/trend/task panels while inheriting the same Global Design System. It must not introduce another visual language.

## Template selection examples

- Material query → T1
- Material create/edit → T2
- Receipt detail → T3
- Inspection report → T3
- MaterialLot → T4
- MainBatch aggregate → T4
- Operation/Weighing/Charge/eBR runtime → T5
- QA Material Release → T6
- QA Batch Release → T6

## Implementation declaration

For every new or substantially redesigned Vue page, the implementation/report must state:

`UI Template: T1|T2|T3|T4|T5|T6`

and verify:
- Global Design System applied;
- no frozen business contract changed;
- desktop layout checked;
- mobile/responsive layout checked;
- relevant allowedActions/permissions preserved;
- targeted UI tests/build passed.

## Stop condition

If a required page cannot be represented by T1–T6 without changing its business meaning, stop with `DESIGN CHANGE REQUIRED` and propose the smallest design-system change. Do not silently invent a new page structure.


Approved entry/navigation delta: DCP-PRODUCTION-FINISHED-ENTRY-001, frozen v1.0.23. Seven shared domains, production five/finished nine entries; new selectors select T1 exactly, existing details/workbenches retain T3/T5/T6. No new visual system, T7, business state, permission code or QA/signature rule.


## Approved bounded control removal — 2026-10-09

Human-approved [DCP-BASIC-NO-AUDIT-VERSION-001](../development/DCP-BASIC-NO-AUDIT-VERSION-001-APPROVED.md) supersedes only basic maintenance audit/version controls and the process binding of new production/eBR work. [Bounded contract](../architecture/BASIC_CURRENT_DEFINITION_CONTRACT.md) specifies database, domain, state, API, permissions, UI, migration, integration and RTM changes. Basic pages retain UI V2 and their existing T1–T6 templates, without audit panels, client version controls or process revision actions. Production/eBR retain their own audit, signatures, revision checks and immutable frozen evidence. Historical releases and evidence remain immutable. Runtime verification/status is only MES_TASKS.md. This approved supplement does not authorize any further business/UI redesign.

Approved 2026-10-09 [eBR template-name supplement](../development/DCP-EBR-TEMPLATE-NAME-001-APPROVED.md): retain existing page structures and visual authority; create requires 模板名称, draft designer edits it through controlled Save, list/detail/production selectors show name with code/revision, and keyword searches name or code. Unnamed historical revisions display 未命名（历史模板）; signed/frozen history is not backfilled. No broader UI redesign is authorized.
