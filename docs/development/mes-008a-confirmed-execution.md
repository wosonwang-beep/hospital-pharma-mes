# MES-008A confirmed implementation work

User confirmed the implementation map and permitted development on 2026-10-03: “确认流程已清楚就可以开发”. Existing v1.0.9 business contracts and previously bounded approvals remain authoritative. This confirmation permits implementation; it does not decide unresolved standard-version, retest-instance, result-enum or report-aggregation alternatives.

Current work uses the existing codex/mes-006-process checkout, preserving prior uncommitted work. No new worktree, database, executed migration change, commit or push.

## Executable scope

1. Database/Flyway review: these two independent changes need no schema modification. Do not allocate a migration or construct partial inspection-request persistence before the required QC specification-version contract exists.
2. Implement the already frozen InspectionRequest, SamplingTask, Sample, InspectionTask and InspectionReport named state transitions in mes-qms.domain. Add the exact request-type dictionary from the published Domain. The conflicting result dictionary remains deferred. No generic status mutation and no inference that state helpers alone satisfy signature/eligibility gates.
3. Correct the reviewed explicit signature predecessor validation so re-sign cannot skip the immediately preceding invalidated signature. Reuse the existing conflict code; preserve objects, API and history.
4. Targeted unit regressions: allowed/forbidden incoming transitions; explicit stale predecessor rejected without new signature/audit/idempotency completion; latest predecessor accepted; automatic predecessor behavior retained. Run affected compilation and a single combined focused green gate after observing the reproduced signature failure.
5. Review changes, record exact results and remaining blocking contracts in MES_TASKS. Do not mark the incoming workflow complete or ready.

Unresolved parts remain stopped only at their boundary: map DG-01..08 and BC consistency work. Actual request/sampling persistence requires the unresolved specification and lineage contracts; later services/API/UI must not submit incomplete regulated records or pretend dependent functionality exists. Earlier approved UI/FK changes still require cumulative-baseline publication before implementation. Receipt stage is reused, not reimplemented.

## Execution evidence

Implemented IncomingRecordStates with five named state sequences and the four frozen request types. These are domain-order foundations only; no new persistence, endpoint, UI, eligibility decision or completed incoming flow is claimed.

Corrected explicit re-sign predecessor validation in SignatureTransactionService. A specified INVALIDATED predecessor must also be the latest signature for the same organization/object/meaning. Existing automatic linkage, conflict code and immutable history are preserved.

Validation on 2026-10-03:

- RED: explicit stale-predecessor regression failed before the fix because no exception was raised. Domain tests initially failed compilation before their implementation existed.
- GREEN: `mvn -q -pl backend/mes-qms,backend/mes-audit -am test -Dtest=IncomingRecordStatesTest,SignatureTransactionServiceTest,SignatureCanonicalizerTest,SignatureApiContractTest -Dsurefire.failIfNoSpecifiedTests=false` exited 0: 17 tests, 0 failures/errors (suite time 1.099s).
- Native transaction gate: `mvn -q -pl backend/mes-boot -am verify -Pci-integration -Dtest=NoUnitSelected -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=ProcessIT#explicitResignRequiresImmediatePredecessorAndRollsBackRejectedAttempt` exited 0: 1 test, 0 failures/errors, suite time 7.143s. Unique fixtures rolled back by the test transaction; no shared seed mutation. Rejected stale predecessor leaves no new signature/audit/idempotency record, correct predecessor succeeds, old signature statuses/digests remain preserved. Uses the real ProcessVersion provider with consumed-auth fixture; this is not reauthentication or incoming E2E evidence.
- Native Flyway validated 13 migrations, schema stayed V013; no migration executed or historical file changed. Existing Flyway/MariaDB support-version and Mockito agent warnings remain technical debt.
- Independent scoped static review: 0 CRITICAL/HIGH; no unauthorized design change found. No repeated full regression or frontend checks (UI untouched).

Remaining blocked implementation boundaries are DG-01..08 and BC-01..02 in the implementation map. In particular, the required specification-version producer/FK, approved retest identity and report aggregation contracts are not fabricated. No full workflow acceptance test or production-charge reverse trace has passed. Task status is maintained only in MES_TASKS.md.
