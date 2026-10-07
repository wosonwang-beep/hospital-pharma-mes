# DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001 — approved bounded contract

Approval: human explicitly approved “入库申请同时生成请验草稿”的流程变更 on 2026-10-06. Parent FINAL BASELINE COMPLETE v1.0.20 stays immutable. Implement only this option; the supplied finished-goods image remains visual reference, not authorization for other menu/route/field changes. UI V2/T1–T6 unchanged. No automatic human acceptance, commit or push.

## Business / state / source rules

Production must be PRODUCTION_COMPLETED. Creating an inbound application can optionally create its associated finished inspection DRAFT in the same transaction. Neither record submits automatically. Inbound and child share actual mother batch / finished lot; the child freezes the actual dispatched APPROVED signed quality plan/specification at generation. Child is not physical receiving, Sample, QC acceptance, QC PASS or QA release.

Standalone finished-request create retains its existing confirmed-receipt precondition. Only the synchronous inbound-created domain event can use the early-draft application method, which requires an existing transaction, actual scoped active DRAFT/SUBMITTED/CONFIRMED inbound source and existing qms:finished-request:create permission. Domain event integration is synchronous and failures roll back parent/child/audit/idempotency. No WMS→QMS module dependency or asynchronous eventual half-success.

Submit inspection, accept QC, sample, report/review, QA and shipment retain actual confirmed warehouse receipt and all existing gates/signatures. Before warehouse confirmation, child DRAFT does not expose SUBMIT. A cancelled inbound cannot progress its child; preserve both original regulated facts, no physical delete or invented cancellation enum. Original FAIL/OOS/retest and independent review unchanged.

## Database / domain / API

Reuse wms_finished_inbound_request and qms_finished_inspection_request with existing unique(org,inbound_request_id), PK/FKs, source freeze, optimistic locking and append-only audit. No column/table/state/permission/route change, no Flyway migration; existing successful V030/V031 remain byte-identical.

Extend existing POST /finished-inbound-requests closed create body with optional createInspectionDraft:boolean (omitted/false preserves old behavior) and inspectionRequestNo:string max80. True requires a nonblank inspectionRequestNo and BOTH wms:finished-inbound:create and qms:finished-request:create before work/replay. False/omitted forbids inspectionRequestNo; non-boolean flag rejected. Existing requestNo/mainBatchId/reason and 201 FinishedInboundRequest response unchanged. One Idempotency-Key preserves exact parent payload; child uses the same key in its existing separate operation namespace. No new endpoint/DTO envelope or manual source facts.

Internal QMS early draft method uses existing closed finished-create fields and MANDATORY transaction. No new electronic signature at DRAFT creation; existing creator/time/reason, frozen version, Audit Trail/idempotency retained. Confirmation/QA/signatures unchanged.

## UI / permission / integration

Existing inbound T2: optional “同时生成成品请验草稿” checkbox (off by default, visible only with child-create permission); when selected show 成品请验单号. Preserve field order, horizontal labels/input, global styles and actual source selector. Existing T3 inbound loads the associated child through existing permission-scoped finished-request list(mainBatchId), matches inboundRequestId, shows actual number/status/link only with qms:finished-request:view. No new stored projection fact. Draft T3 explains warehouse-confirmation prerequisite; action visibility comes from actual server checks. Existing standalone finished-request picker remains confirmed-only.

Producer WMS emits FinishedInboundInspectionDraftRequested; boot synchronous listener bridges to QMS under the original actor/org and transaction. WMS emits only when true, inside first successful idempotent work. QMS owns child facts. QA/eBR/trace consume unchanged source records; early DRAFT cannot satisfy confirmed receiving or approved report gates.

## Test / RTM / dependency / migration allocation

FD-01 unchecked create preserves old payload, creates no child or stock. FD-02 selected create yields exactly one linked DRAFT and frozen actual plan; replay neither duplicates nor changes facts. FD-03 submit/accept/sample are blocked before warehouse confirmation; afterwards normal QC/report/QA flow succeeds. FD-04 child-create permission missing, child audit failure or conflicting idempotency/invalid optional input yields no partial rows/audit/key/stock; lost permission still blocks replay. FD-05 cancelled inbound cannot submit child. FD-06 PC T2 checked/unchecked serialization, T3 actual child link and prerequisite hint; no console/page errors or overflow.

Hard dependencies: existing finished inbound/QMS/production quality/signature/idempotency contracts from DCP-FINISHED-GOODS-CHAIN-001. No new MES task, migration allocation NONE, highest successful31 remains. Reconcile affected API/OpenAPI/UI/permission/test/RTM/integration/dependency documents in cumulative v1.0.23; consistency PASS before current pointers switch. Task state only MES_TASKS.md.
