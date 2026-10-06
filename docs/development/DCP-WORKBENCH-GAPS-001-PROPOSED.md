# DCP-WORKBENCH-GAPS-001 — data ownership decision pending
## Current governance status — 2026-10-06

| Unapproved contract | Status |
|---|---|
| Material Master Image | `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED` |
| Operation Execution Photo | `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED` |
| Planned Operation Time | `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED` |
| SOP/Method Reference | `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED` |

The user explicitly excludes these contracts from the Functional v1.0.17 / UI V2 Closeout. Closeout does not approve owner selection, contract completion or implementation. Existing-contract frontend follow-up is accepted separately; the proposal below remains a proposal. Global UI V3 is `NOT IN CURRENT SCOPE`. Authority remains FINAL BASELINE COMPLETE v1.0.17 and UI V2 / T1–T6.


Date: 2026-10-06. Current authority: FINAL BASELINE COMPLETE v1.0.17.
User authorizes acceptance, functional gap completion and commit/push. This proposal does not switch the baseline or authorize a selected data model before the owner answers the pending question.

## Existing-contract implementation

- Production deviation: reuse POST /deviations, ProductionDeviationCreateCommand, qms:deviation:create, PRODUCTION scope and existing mainBatchId/operationExecutionId association checks. T5 navigates to the existing create page with these identifiers. The operator supplies severity, description and reason. Final QA roots remain frozen by the backend; the shortcut is hidden for QA_RELEASED/REJECTED. No new permission, status, API or signature rule.
- Parameter presentation: reuse rawValue, textValue, unitId and frozen process parameter definitions. Exact same-unit decimal comparison is labelled 范围核对, not a new authoritative result. Missing bounds, invalid values and different units get neutral labels. The server retains exact conversion, completion gates and all original observations. No DTO or persisted result is introduced.

## DESIGN CHANGE REQUIRED / owner decision

These screenshot features lack corresponding contracts. Existing receipt attachments are receiving evidence; they must not be relabelled as material-master or production-execution photographs.

Recommended data ownership, offered to the user as one bounded option:

1. Material image belongs to material master; reuse immutable gxp_attachment binary storage with an owner association. Preserve prior image/association evidence. MaterialLot must explicitly label this as a master reference image, not proof of the received batch. No English name, CAS or removed fields are restored.
2. Production photograph belongs to OperationExecution; reuse gxp_attachment and an immutable scoped association, upload actor/time/reason/version and audit. Preview and large-image view require access to that owner association. Never use a stock/generated photograph as execution evidence.
3. Expected operation start/end are a production-maintained UTC plan, independent of startedAt/completedAt. A controlled planning command must validate the interval, owner organization, permissible execution state and optimistic version, audit changes and preserve history. They must not be inferred from sample screenshot times or overwrite actual execution times.
4. Operation method references SOP code/version in the controlled process definition, then follows existing version approval and frozen batch snapshot. Existing snapshots without the reference remain empty. A later SOP change must not alter historical batch instructions.

Alternative: keep this delivery within existing contracts; handle the four new data contracts in a separate approved increment.

## Required subsequent consistency gate

After owner selection, finalize the bounded DCP and affected database/PK-FK, domain, commands/state constraints, OpenAPI/read DTO, permission, audit/signature, UI T2/T4/T5, tests/RTM, migration and integration artifacts together. Preserve v1.0.17 as immutable history. Review consistency before changing authority pointers. Allocate any physical Flyway version only after inspecting the native successful history; no repair/reset or executed migration edit. No new backend, database, migration or baseline has been changed in this pending proposal.

Until that selection, the new-contract portion is pending and must not be reported as implemented or accepted. The existing-contract frontend increment can be verified and submitted independently under the user's commit/push authorization.
