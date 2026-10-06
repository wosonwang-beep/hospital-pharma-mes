# Main merge CI reconciliation

Human acceptance and main merge authorization: 2026-10-06, “验收，提交和推送，合并到main”. PR: https://github.com/wosonwang-beep/hospital-pharma-mes/pull/5.

Initial GitHub CI run37488979868: repository contract and frontend tests/typecheck/build/dependency audit PASS. Both backend jobs stopped at the same Mes002MigrationContractTest assertion: an old exact V001..V027 list did not include already-approved V028..V031. The infrastructure tests had not reached their execution stage; this was not a native migration failure.

Minimal reconciliation: keep exact contiguous versions and all historical V001/V002 hashes / V005 RBAC assertions; update the exact list to V001..V031 and add exact SHA256 assertions for each approved V028..V031, matching frozen v1.0.22 00_TRACE_MIGRATION_HASHES.json. No SQL, Flyway history, frozen release bytes, CI workflow, skips, production rules or local configuration changes. The change strengthens checksum coverage instead of accepting arbitrary extra migrations.

Local verification: existing two Mes002MigrationContractTest methods PASS (0 failures/errors). Independent read-only review PASS: exact version/hash assertions remain strict and are not weakened. GitHub CI reruns on the PR before merge. Runtime acceptance remains recorded only in MES_TASKS.md. This evidence supplements the targeted implementation report and does not alter any frozen release.
