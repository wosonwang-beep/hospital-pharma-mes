# DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 — APPROVED

Status: APPROVED by the user’s explicit “批准” on 2026-10-05 after review of this bounded proposal. Published in cumulative v1.0.18 after cross-document consistency PASS. Prior frozen releases including v1.0.16 must remain immutable.

## Bounded scope for HIGH-05/07/08

1. Publish existing GET /main-batches productionOrderId optional positive-ID query parameter. Existing productId/date/status/keyword pagination contracts remain; UI state is restored through existing route query. No entity, persisted column, API path, permission or production state change.
2. Add server-derived read-only allowedActions string arrays to incoming request, sampling, sample, inspection task/item/execution/result, report, incoming investigation and material-release review read DTOs. Action names identify existing commands only. Compute availability centrally from authoritative domain/application command predicates; frontend intersects with existing permissions and server actions. Execution still validates qualification, actor/independent review, frozen evidence, signatures, optimistic locking and idempotency; presence of an action is no authorization bypass. Empty arrays fail closed. No new transition.
3. Define exclusive active equipment occupancy for operation START/RESUME: one equipment identity cannot have RUNNING segments in conflicting operations/execution units. Lock existing equipment rows in sorted identity order within the same transaction; check current committed active runs using a locking/current read. PAUSE/COMPLETE acquire the same equipment locks before ending runs. Rebinding while PAUSED cannot erase past segments. Preserve all run/audit history. No database/migration required if existing row locks suffice. Focused real MariaDB concurrency proof: two distinct operations, at most one starts; pause/complete releases occupancy; resume blocks when occupied.

## Publication and consistency

After explicit approval, publish a cumulative v1.0.18 release plus approved DCP and consistency review. Update only affected Database (no schema delta), Domain/State/API/OpenAPI/UI/Permission/GxP/Test/RTM/Integration/Task references; retain all historical releases immutable. Keep no second mutable source. Switch docs/PROJECT_BASELINE and AGENTS authority pointer only after cross-document consistency PASS. Global UI V2/T1–T6 remains unchanged.

## Exclusions

No business fields or tables, generic status APIs, new permissions/routes, new signature rules, Gate weakening, executed migration edits/repair, MEDIUM navigation/trace redesign, future MES tasks or unrelated refactoring.

## Required proof

Production query request/response filtering + route restoration; incoming server action/state tests + frontend permission/fail-closed tests; real unique native equipment concurrency and lifecycle tests; affected typecheck/build/browser checks. Preserve persistent DEV records with rollback-owned or unique retained fixtures. No full regression by default.
