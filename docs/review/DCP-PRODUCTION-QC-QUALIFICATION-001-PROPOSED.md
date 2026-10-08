# DCP-PRODUCTION-QC-QUALIFICATION-001 — PROPOSED / NOT AUTHORIZED

Date: 2026-10-08
Authority: FINAL BASELINE COMPLETE v1.0.23. This proposal **does not modify** the frozen GxP contract, database schema, historical signatures or existing releases.
Status: **DESIGN CHANGE REQUIRED** for mandatory *personal qualification* beyond existing RBAC.

## Verified diagnosis (R9, rollback-only)

- An R9 test context used real Spring Services, MariaDB and signing but deliberately gave the **warehouse test identity** `qms:test:record` + `ebr:sign` permissions without `R9_QC_EXECUTE` qualification.
- The existing `IncomingActorAdapter.requireQualified(1, warehouseActor, "test-execute")` correctly failed with `QUALIFICATION_REQUIRED`.
- The same actor was nevertheless accepted by `ProductionQualityService.result(...)` as the recorder of a genuine signed production QC result in that rollback transaction: log `mes-r9-production-qc-qualification-probe.log` contains `R9_PRODUCTION_QC_GAP_PROVEN unqualifiedWarehouseActor=11754 acceptedSignedTestResult=83`. Maven test PASS proved the observed (undesirable) current behavior; it was **not** an acceptance test of correct qualification enforcement.
- Review of `backend/mes-qms/src/main/java/com/hospital/mes/qms/application/ProductionQualityService.java`: existing `createSample`, `result`, and `review` rely on authorization, signed-record and independent-review constraints but have no explicit `IncomingActorPort.requireQualified` / `MasterQueryService.requireQualification` call or injected qualification service.
- Distinguish: actual `demo.warehouse` RBAC denies QC endpoints (R9 browser role matrix), but RBAC alone cannot replace qualification checks for **any actor who is given QC permission**, including administrative roles. This is a demonstrated missing second gate, not proof that the live warehouse demo login could exploit it.
- The diagnostic method was removed after collecting evidence, preserving a green existing CI suite. All probe records and signatures rolled back; no historical artifact was modified.

## Proposed bounded decision and controls — pending Design Authority

1. Approve a reusable personnel qualification rule for **production QC** actions:
   - Production sample creation / any controlled sample collection: `sample-execute` (or explicitly approved corresponding operation code).
   - Production test result recording and correction: `test-execute`.
   - Independent production test review / disposition: `test-review`.
   - Decide separately whether test creation, retest initiation and finished-report generation require an additional dedicated qualification or rely on the above role qualification.
2. Reuse the already approved `md_qualification` evidence model and configuration mapping with organization, active status, valid dates and fail-closed missing mapping. **Do not** treat generic GMP qualification or super-admin permission as equivalent to individual QC competency.
3. Keep all existing signed original-result/revision evidence, signature verification, independent reviewer rules, idempotency, audit, optimistic version checks, and frozen QC-standard snapshots.
4. Align backend read-model `allowedActions` with the approved actor-specific Gate; UI is never sole enforcement.
5. Specify how to address historical demo records whose operator/QA qualifications were registered after the actions, including any external contemporaneous training records. Do not backdate, change actor IDs, rewrite e-signatures or silent-recalculate a released batch.

## Required negative and positive acceptance tests (all rollback-only)

- User with QC permission + valid eBR reauthentication but **no valid QC execution qualification** cannot record/alter a production QC result, leave a revision, produce a signature or change batch QA eligibility.
- Correct QC role with active, organization-scoped, valid qualification can execute and sign; independent qualified reviewer may sign result review.
- Expired, inactive, wrong-organization, missing mapping, revoked qualification → fail closed with declared business error; previous originals and signature counts remain unchanged.
- Original result performer cannot independently review; qualified role cannot bypass independent QA release and audit controls.
- Optional new qualification checks for sample/test/finished-report are decided **before** implementation, with explicit coverage of direct Service callers and HTTP roles.
- Existing finalized batch evidence remains immutable. Multi-role end-to-end test preserves batch/manifest/attachment cleanup after rollback.

## Controlled scope

No implementation until explicit Design Authority approval of the qualification action matrix. Then update authoritative PRD, Domain/GxP rules, permission/qualification matrix, API/error contract, relevant UI/read model, test cases, RTM, and controlled baseline consistency review in one bounded release. No new migration unless required and authorized; applied Flyway V001–V032 immutable.

Related: `DCP-QA-PLAN-QUALIFICATION-001-PROPOSED.md` (distinct quality-plan approval gate), `DCP-FINISHED-STRENGTH-GATE-001-PROPOSED.md` (formula strength), and `MES_DEMO_ROLE_EVIDENCE_INCONSISTENCY_2026-10-08.md` (historical actor chronology).
