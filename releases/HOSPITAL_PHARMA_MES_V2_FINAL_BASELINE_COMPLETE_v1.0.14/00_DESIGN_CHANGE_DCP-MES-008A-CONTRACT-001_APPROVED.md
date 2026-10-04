# DCP-MES-008A-CONTRACT-001 — approved bounded contract

Authority: human approval on 2026-10-03: “批准该补齐方案，并将 MES-009/010 纳入本轮开发，以完成所要求的集成”. The incoming implementation map/review was subsequently confirmed. This release incorporates only the previously approved A/B scope. v1.0.9 remains immutable history. It does not authorize filling DG-01..08 with guessed contracts or resolve unrelated MES-009/010 design proposals.

## A. Existing business actions receive independent pages

| ID | Route | Permission | Action |
|---|---|---|---|
| UI-QMS-IN-C | /quality/inspection-requests/create | qms:inspection-request:create | Create request |
| UI-QMS-SMP-C | /quality/sampling-tasks/create | qms:sampling:create | Create sampling task |
| UI-QMS-SMP-W | /quality/sampling-tasks/:id/execute | qms:sampling:execute | Execute sampling; complete additionally requires qms:sampling:complete |
| UI-QMS-TST-C | /quality/inspection-tasks/create | qms:test:execute | Create inspection task |
| UI-QMS-TST-W | /quality/inspection-tasks/:id/execute | qms:test:execute | Execute testing; correction additionally requires qms:test:correct |
| UI-QMS-RPT-C | /quality/inspection-reports/create | qms:report:create | Generate report from referenced results |
| UI-QMS-WORKBENCH | /quality/workbench | Existing per-queue view permissions | Existing role-filtered queue, no new permission or API |

List/Query, View, QA review/release and Sample routes remain unchanged. Existing horizontal label/control layout applies at desktop and mobile widths. No list modal creation, new business fields, new result entry on reports, or finished-product QA fields in incoming screens. Prototype screenshots remain layout references only; missing incoming scene coverage remains explicit. Existing commands, state machines, same-transaction audit, optimistic locking, idempotency, result revision lineage and required electronic signatures remain mandatory.

## B. Physical reference ownership

MES-008A owns qms_sample and the shared qms_release_decision with nullable main_batch_id. Before the real MES-009 producer and FK exist, any non-null main_batch_id write fails closed. INCOMING_MATERIAL scope always requires main_batch_id IS NULL and finished_lot_id IS NULL, with material_lot_id identifying the incoming target. Do not fabricate a batch or install a placeholder production table.

MES-009 installs and validates qms_sample.main_batch_id and qms_release_decision.main_batch_id foreign keys to the real prd_main_batch.id in a new append-only physical migration after both QMS tables and the batch table exist. Validate existing references first; orphan references stop migration, never delete or repair them implicitly. This is a staged FK dependency, not a new hard task cycle.

qms_release_decision.finished_lot_id references canonical md_material_lot.id and is applicable only to FINISHED_PRODUCT. Preserve the existing finished-product main-batch/target rules; no second FinishedLot table, no new finished-release implementation. Incoming records cannot populate finished_lot_id. This clarification does not change decision/basis/source enums or grant release from QC PASS.

Allocate physical migration versions from successful live Flyway history immediately before implementation. This document reserves no version and executes no schema. Executed V001–V013 remain immutable. No temporary omission becomes a permanent missing FK.

## Verification and traceability

Extend existing TC-QMS-IN-001, TC-QMS-SMP-001..003, TC-QMS-TST-001..003 and TC-QMS-RPT-001..002 to cover their independent create/execute routes, permissions, 409/422 handling and inline labels. Existing MREL cases cover incoming null-target constraints, canonical finished-lot reference and retained signatures/audit. MES-009 migration integration verifies staged batch FKs and rejects orphan references. Existing requirement IDs QMS-IN-001/QMS-SMP-001/QMS-TST-001/QMS-RPT-001/QMS-MREL-001 retain ownership.

No pass/readiness claim follows from publication. Specification version identity, sampling detail-to-sample generation, approved retest identity, INVALID versus INCONCLUSIVE, request-level reporting, receipt attachments, incoming investigations and missing incoming UI contracts remain in the confirmed implementation-map gap register. Production/charge reverse-trace requires real producers.
