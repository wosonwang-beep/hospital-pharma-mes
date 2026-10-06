# Approved audit HIGH closure — v1.0.18

Status: APPROVED / FROZEN after cross-document consistency PASS on 2026-10-05. User explicitly approved DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 on 2026-10-05. Cumulative inheritance from immutable FINAL BASELINE COMPLETE v1.0.16. MES-012/013 completion remains accepted; historical approvals are retained.

## Database / migration
No table, column, FK, index or physical migration change. md_equipment is the existing identity mutex. mes_equipment_usage links equipment_id to operation_execution_id; mes_equipment_run links equipment_usage_id to immutable start/end segment history. Executed Flyway files remain unchanged. Native localhost:3306/hospital_pharma_mes_dev is retained.

## Query API / UI-BAT-Q
GET /main-batches adds the already implemented optional query productionOrderId, positive ID string. Existing filters, pagination and response remain unchanged. T1 order/batch query restores product/status/date/order/keyword/page from route query. No new route or permission.

## Domain / state / equipment integration
START/RESUME lock existing equipment identities in ascending ID order in the same transaction, before material gates. Current locking reads of usages and runs reject any existing RUNNING segment with EQUIPMENT_OCCUPIED. PAUSE/COMPLETE acquire the same mutex before closing segments. Locks and inserts/updates/audits commit atomically. No status API, global equipment status rewrite, history replacement or new state. MariaDB snapshot conflict is surfaced as CONCURRENT_MODIFICATION; it fails closed and a fresh command must encounter the occupancy gate. Existing production order/batch/operation lock hierarchy and qualifiers remain.

## Incoming read DTO / API / UI
Required read-only allowedActions string[] is added to InspectionRequest, SamplingTask, SamplingDetail, Sample, InspectionTask, InspectionItem, TestExecution, TestResultRevision, InspectionReport, Deviation (incoming branch), ReleaseDecision and ReleaseReview. Empty/missing actions fail closed in consumers. Action names are existing command suffixes: submit, accept, approve-plan, assign, start, details, complete, label, receive, retain, dispose, executions, approved-retests, results, revisions, submit-review, review, approve, update, investigate, decide, close, release-decisions.

Domain IncomingActionPolicy shares state candidates with command validation. Application narrows by authenticated assignee, configured qualification, independent reviewer, completed/resealed sampling, complete test evidence, exact confirmed results, report digest and signed retest quota. DTO availability is advisory at read time: request-specific inputs and authoritative current evidence are validated again by the existing command transaction. ReleaseReview offers the existing decision command only in supported lot transition states; eligibleForRelease still governs RELEASED, not reject/disposition. No generic availability implies release eligibility.

Frontend intersects server actions with the existing operation permission. Nested inspection item/execution/revision controls use their own source actions. Existing T1–T6 visual language/layout remains; no T7 or Phase 2B.

## Permission / GxP / source of truth
No permission, electronic-signature target/meaning, qualification rule, release gate or allowed command transition is added or weakened. Dynamic actions never enter signature canonical evidence or audit fact snapshots. Read DTO decoration is not persisted. Original FAIL corrections remain prohibited; retest creates new execution/result facts. Incoming Sample/TestResult/Report/Release and WMS lot source-of-truth ownership is unchanged.

## Required tests / RTM / dependency
AUD-H05 -> TC-AUD-H05: native existing productionOrderId filtering plus browser query restoration/reset/pagination.
AUD-H07 -> TC-AUD-H07: native request/sample/item/execution/revision/report/release actions, actor independence and signature integrity; browser missing actions and permission fail closed.
AUD-H08 -> TC-AUD-H08: native different-order concurrency has at most one RUNNING owner; fresh retry blocked; PAUSE frees; occupied RESUME blocked; COMPLETE frees; ended history retained.
Existing MES-008/008A/010/011 producer-consumer boundaries stay accepted. EquipmentQueryService exposes an internal transaction mutex, not a public API. Incoming consumers use decorated existing DTOs, not a second fact source. MEDIUM-01..04 remains outside this change. Runtime verification and human acceptance are separate from baseline publication.
