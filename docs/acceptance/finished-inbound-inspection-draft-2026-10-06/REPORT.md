# Finished inbound optional inspection draft — targeted verification

Approval: human explicitly approved “入库申请同时生成请验草稿”的流程变更 on 2026-10-06. [Bounded DCP](../../development/DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001-APPROVED.md). This report does not grant human acceptance; task readiness belongs only to MES_TASKS.md.

## Implementation

- Existing inbound T2 provides an off-by-default option and a conditional actual inspection number. Unchecked serialization preserves the old closed command. Child option requires existing QMS create permission.
- WMS synchronous typed domain event / boot bridge / MANDATORY QMS draft creation use the same transaction, original actor and organization. Parent/child/audit/idempotency failures roll back together; no stock is generated.
- Child freezes the actual dispatched approved signed quality plan/specification. Replay retains the pair; lost child permission blocks replay. Parent and child remain DRAFT.
- Actual warehouse confirmation still gates inspection submit/accept/sample, QA, availability and shipment. Standalone inspection create stays confirmed-receipt-only. Cancelled inbound retains original child facts but cannot progress them.
- Existing permission-scoped reads display actual linked child number/status. New draft detail uses “关联成品入库申请”; it does not falsely label the source already confirmed. No new pages/routes/signature rules; existing accepted inspection/report/T5/T6 layouts are preserved.

## Targeted gates

| Gate | Evidence | Result |
|---|---|---|
| Native MariaDB lifecycle | FinishedInboundInspectionDraftIT new4 + FinishedGoodsLifecycleIT receiving/full-chain2; 38.769s build | PASS6, failures0/errors0 |
| Closed OpenAPI command | FinishedInboundDraftSchemaTest; checked/unchecked, false+child forbidden, blank/string flag/extra qualityStatus rejected | PASS1 |
| Frontend typecheck | npm run typecheck | PASS |
| Frontend units | finished/model.test.ts + quality/finishedReleaseModel.test.ts | PASS9 |
| PC Chromium | Optional T2 create -> actual inbound child link -> pre-receipt child DRAFT; 1440x900, 9.5s | PASS1, 3screenshots, no console/page errors or document overflow |
| Build | npm run build, 12.81s | PASS; existing bundle-size advisory retained |
| Cross-document consistency | scripts/finished-inbound-draft-baseline.py --check | PASS13 checks; all138 v20 parent files unchanged |

Native tests use unique actual fixtures and transaction rollback in the existing hospital_pharma_mes_dev database. No alternate database, cleanup/reset, repair or shared seed modifications. Flyway validated31 migrations; current physical version031, no migration necessary. Existing V030/V031 match frozen approved SQL exactly.

Observed RED before implementation: native rejected the new option / permission expectation, frontend serialization lacked the helper, browser lacked checkbox. Schema RED against v20 rejected createInspectionDraft; successor schema GREEN. Additional screenshot assertion first failed on “关联成品入库申请”, then passed after the minimal T3 label correction. The browser draft fixture was corrected to have no unexecuted sampling/report facts; runtime business evidence comes from native tests, not the browser fixture.

## Review and remaining debt

One fresh scoped final reviewer found no CRITICAL/HIGH/MEDIUM blockers. LOW evidence suggestions: assert no rolled-back audit rows and exercise same-key changed-payload conflict. These assertions were added to the existing native failure/replay case; final rerun PASS1 (22.457s build, failures0/errors0), with no remaining scoped LOW finding. The first expanded conflict assertion used a ComplianceException-only helper, while the actual correct API conflict is ResourceConflictException; the test was corrected to assert that established conflict type, with no production change.

Inherited non-blocking debt: real concurrent committed-transaction load/performance and existing list paging/N+1 checks remain outside this bounded change. Existing picker state enum translation and unrelated UI refinement are unchanged. No new business feature is implied.

## Screenshots

- [Inbound T2 optional paired draft](screenshots/inbound-paired-draft-create-desktop.png)
- [Inbound T3 actual linked child](screenshots/inbound-linked-draft-detail-desktop.png)
- [Inspection DRAFT before warehouse confirmation](screenshots/inspection-draft-before-receiving-desktop.png)

## Release discipline

Cumulative successor v1.0.21 reconciles Database/Domain/State/API/UI/Permission/Test/RTM/Integration/Task Dependency/Migration NONE, with the same existing contracts except the approved conditional command extension. v1.0.20 remains immutable history. Global UI V2 / T1–T6 unchanged. Unapproved workbench gaps and UI V3 remain excluded. No full-system regression, automatic human acceptance, commit or push.

Final state: cumulative v1.0.21 consistency PASS and locally frozen; all5 current authority pointers agree, parent138files unchanged. No new migration. Required targeted gates complete; human acceptance pending.
