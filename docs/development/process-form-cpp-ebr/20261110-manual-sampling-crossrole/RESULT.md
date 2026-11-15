# MES 手工取样跨岗位闭环修复 — 2026-11-10 DEV RESULT

Decision: docs/development/decisions/DCP-QMS-MANUAL-SAMPLING-CROSSROLE-001-APPROVED.md
Scope: only manual sampling with completed plan, WMS independent sample outbound, QC receipt, existing linked inspection task; baseline v1.0.23.
Verdict: CODE + DEV SAMPLE HANDOFF VERIFIED / QC RESULTS AND QA RELEASE NOT RUN / OVERALL PARTIAL.
Environment: one local main workspace, MariaDB hospital_pharma_mes_dev, Flyway V076; no worktree or direct SQL business updates, no git stage/commit/push.

## Build and regression
- `mvn -pl backend/mes-boot -am -Dtest=IncomingCrossroleBatchAIT,IncomingCrossroleBatchBIT,IncomingCrossroleNegativeIT,IncomingQualityIT,IncomingQualityAcceptanceIT,IncomingActionPolicyTest,IncomingCrossroleRouteAuthorizationTest -Dsurefire.failIfNoSpecifiedTests=false test -q` exit 0, 63/63 PASS (6+5+7+6+3+5+31), 0 failures, 0 errors, 0 skipped.
- MariaDB Flyway 76 validated, V076, no new migration.
- First package attempt failed because running Java 10304 locked the target JAR. Exact local 8080 DEV process was identified and stopped, then `mvn -pl backend/mes-boot -am -DskipTests package -q` exit 0; executable JAR 128,973,044 bytes. New Java PID 31124, local port 8080, /actuator/health = UP.
- No unrelated business schema/UI changes introduced for this DCP. Existing uncommitted working tree was preserved.

## Real DEV REST execution (not mocked)
- `npx.cmd playwright test e2e/hytea-manual-handoff-20261110.tmp.spec.ts --project=chromium-desktop --workers=1 --reporter=line`, exit 0, 1/1 PASS. Script was archived after execution.
- Sampling task 689→694: all six with plannedSamplingQuantity 0.125 kg, plannedUnitId 2, one resealed sample detail, TEST_SAMPLE 0.100 kg + RETENTION_SAMPLE 0.025 kg.
- Each task signed by the sampler through /auth/reauth and /submit-to-warehouse (HTTP 200), then independently signed by warehouse account via /auth/reauth and /confirm-sample-outbound (HTTP 200). All six transitioned IN_PROGRESS → PENDING_WAREHOUSE → COMPLETED.
- Every sampling detail read back non-null `sampleOutboundLedgerId`; QC analyst received each test sample over `POST /quality/samples/{id}/receive` (HTTP 200, RECEIVED).
- Existing `POST /quality/inspection-tasks` created 6 linked QC tasks 771–776 (HTTP 201, CREATED) against requests 721–726; no duplicate inspection system introduced.
- `npx.cmd playwright test e2e/hytea-qc-start-20261110.tmp.spec.ts --project=chromium-desktop --workers=1 --reporter=line` exit 0, 1/1 PASS. QC analyst assigned and started tasks 771–776 via formal API, all now IN_PROGRESS. Item counts in order: 5, 5, 2, 5, 1, 1. Script archived after execution.
- Inventory stock readback `hytea-sampling-source-read-20261110.tmp.spec.ts`, exit 0, 1/1 PASS: 1121 101.000→100.875 kg; 1122 18.000→17.875; 1123 50.000→49.875; 1124 50.000→49.875; 1125 32.000→31.875; 1126 2.000→1.875. Exactly 0.125 kg physical-book deduction per lot (0.750 kg aggregate). Six lots still inventoryStatus BLOCKED with qualityStatus SAMPLED; no premature QA release.

## Boundary and remaining work
- All sample events are DEV electronic workflow exercise using DEV role accounts and historic/source-based fixture context, not proof of physical sampling, real employee action, or contemporaneous measured laboratory results.
- Actual QC item executions/results and result signatures, independent QC reviewer decisions, inspection reports and QA material release decisions remain NOT RUN. No numeric result or QA approval was invented. Samples/inspection tasks must not be claimed QC_PASSED or RELEASED.
- Batch655 quality plan666 remains outside this DCP; QA signature, production execution, CPP/eBR, finished quality, inbound and archive remain pending in the wider HYTEA program.
- For production evidence, obtain actual test results or explicit DEV-only provenance-controlled source measurements before submitting result records. Do not bypass result review/independent QA gates.
