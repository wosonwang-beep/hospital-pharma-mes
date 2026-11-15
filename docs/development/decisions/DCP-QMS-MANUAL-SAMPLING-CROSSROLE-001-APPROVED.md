# DCP-QMS-MANUAL-SAMPLING-CROSSROLE-001 — approved bounded closure

Status: APPROVED BY USER in conversation (reply “批准” to the specifically proposed minimum DCP on 2026-11-10).
Authority: FINAL BASELINE COMPLETE v1.0.23 plus approved DCP-QMS-SAMPLING-PLAN-COMPLETION-001. No general workflow redesign.

## Defect and approved behavior
Six manually created DEV sampling tasks 689–694 have valid 0.125 kg plan updates and sample detail records but cannot use `submit-to-warehouse`: the former source-type selector only marks automatically generated tasks as CROSSROLE. The legacy `complete` action skips independent WMS sample outbound. QC analyst already has `qms:sample:receive` and a QC test qualification, whereas the service/HTTP matcher incorrectly require sampler execution. The existing manual inspection-task creation API accepts a received linked sample, so it must be reused rather than extending automatic-only `bind-sample`.

## Bounded decisions / design lock
1. A manually created sampling task with a valid persisted positive planned sampling quantity and compatible unit (populated by the already-approved `/plan` command) is warehouse-handover eligible. In `IN_PROGRESS`, offer `details` and `submit-to-warehouse`, not legacy `complete`. In `PENDING_WAREHOUSE`, require independent `confirm-sample-outbound`. Other legacy tasks without an approved completed plan retain their previous path. Automatically generated cross-role tasks remain unchanged.
2. Existing signature target `SAMPLING_TASK` and warehouse signature target `SAMPLING_WAREHOUSE_OUTBOUND`, reauthentication, Idempotency-Key, If-Match, WMS source-specific ledger and independent actor/qualification checks remain mandatory.
3. `POST /quality/samples/{id}/receive` uses the already-existing `qms:sample:receive` authority and existing QC `test-execute` qualification. This does not grant new permissions or permit receive before warehouse-controlled collection. Sample label, retention and disposal permissions remain unchanged. Allowed actions must match actual authorization.
4. Manual pathway after warehouse outbound: QC analyst receives TEST_SAMPLE, then uses existing `POST /quality/inspection-tasks` with matching inspectionRequestId and sampleId to create a regulated QC task from the frozen QC specification; cannot use auto-generated-only `bind-sample`. For warehouse-handover planned manual sources, require confirmed outbound ledger and completed source before task creation; reject duplicate linked task. No new API, schema, table, migration or alternate test evidence.
5. Preserve organization, provenance, request-item lineage, sample type, frozen standard digest, QA release, signatures and audits. Six DEV workflow records are software exercise fixtures, not proof of physical sampling or actual test readings. Do not fabricate QC results or QA releases.

## File write scope
- `backend/mes-qms/src/main/java/com/hospital/mes/qms/domain/IncomingActionPolicy.java`
- `backend/mes-qms/src/main/java/com/hospital/mes/qms/application/IncomingQualityService.java`
- `backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java`
- `backend/mes-qms/src/test/java/com/hospital/mes/qms/domain/IncomingActionPolicyTest.java`
- `backend/mes-boot/src/test/java/com/hospital/mes/incoming/IncomingCrossroleBatchBIT.java`
- `backend/mes-boot/src/test/java/com/hospital/mes/incoming/IncomingQualityFixture.java` — test fixture only: include existing `qms:sample:receive` in the simulated permission set; never change real IAM grants
- `backend/mes-security/src/test/java/com/hospital/mes/security/IncomingCrossroleRouteAuthorizationTest.java`
- `docs/development/process-form-cpp-ebr/20261110-manual-sampling-crossrole/*.spec.ts.txt` — executed DEV-only Playwright acceptance harnesses archived after use to avoid accidental replay by the default E2E suite; no mock results
- `MES_TASKS.md`, this decision, and Planning evidence.

## Acceptance / stopping rules
- Unit/real MariaDB rollback integration: planned manual task handover and warehouse outbound exactly once; legacy without plan unchanged; unauthorized receive and self-warehouse confirmation rejected; valid QC analyst receipt and manual inspection task created from exact received test sample; mismatched sample, no outbound ledger, wrong version, and duplicate binding rejected.
- Browser/DEV re-read of six records; only run signed operations when proper DEV reauth and server authorization are available; never bypass client review panel, modify business data by SQL, create a worktree, stage/push, reset existing work or erase evidence.
- If new issue falls outside these six objects and exact code boundary, STOP with a deviation, without claiming acceptance.
