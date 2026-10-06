# Main merge CI reconciliation

Human acceptance and main merge authorization: 2026-10-06, “验收，提交和推送，合并到main”. PR: https://github.com/wosonwang-beep/hospital-pharma-mes/pull/5.

Initial GitHub CI run37488979868: repository contract and frontend tests/typecheck/build/dependency audit PASS. Both backend jobs stopped at the same Mes002MigrationContractTest assertion: an old exact V001..V027 list did not include already-approved V028..V031. The infrastructure tests had not reached their execution stage; this was not a native migration failure.

Minimal reconciliation: keep exact contiguous versions and all historical V001/V002 hashes / V005 RBAC assertions; update the exact list to V001..V031 and add exact SHA256 assertions for each approved V028..V031, matching frozen v1.0.22 00_TRACE_MIGRATION_HASHES.json. No SQL, Flyway history, frozen release bytes, CI workflow, skips, production rules or local configuration changes. The change strengthens checksum coverage instead of accepting arbitrary extra migrations.

Local verification: existing two Mes002MigrationContractTest methods PASS (0 failures/errors). Independent read-only review PASS: exact version/hash assertions remain strict and are not weakened. GitHub CI reruns on the PR before merge. Runtime acceptance remains recorded only in MES_TASKS.md. This evidence supplements the targeted implementation report and does not alter any frozen release.

Second run37489575990: backend unit, frontend and repository jobs PASS. Infrastructure integration executed704 cases (26 skipped by existing suite definitions), with3 failures: the separate IamSchemaIT migration resource count still27; FinalSystemPermissionIT expected six QA gates instead of the eight approved in the finished-chain DCP; EbrRuntimeArchiveIT found a real read-model omission for batches without a finished lot (required finishedReceiving array missing).

Corrections retain exact expectations: IamSchemaIT checks31 files and scans every SQL for forbidden account seeds; QA permission test asserts eight exact ordered gate codes and retains blockers/actions/authorization checks. EbrArchiveService.readModel renders an empty finishedReceiving array when there is no source receiving fact; sourceEvidence, canonical digests, existing archived records and signatures are untouched. The original archive correction-history test now asserts empty-array semantics in addition to the full closed v1.0.22 schema and all original signature/revision-history assertions. No database, migration, frozen contract or QA decision rule change.

Targeted local verification PASS: migration unit2, EbrRuntimeArchiveIT selected method1, FinalSystemPermissionIT selected parameterized method9 and IamSchemaIT selected method1 (native total11, failures/errors0). Read-only follow-up review PASS: no weakened assertions or signature/history changes. Full hosted CI remains the final main merge gate.
