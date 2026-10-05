# MES-012/013 main integration — 2026-10-05

Authorization: user explicitly requested 更新到main after pushing accepted MES-012/013 and QA page refinement.
Main documentation commits through d558a40 are merged without loss. New global UI governance is retained. QA Batch Release selects T6 Decision Workbench: object/context header, Gate summary, checklist, decision summary/actions and grouped archive/evidence; business flow follows approved v1.0.16/DCPs. No new business or visual contract is invented during this merge.

CI run37267381971 identified a stale exact migration-count assertion24 in Mes002MigrationContractTest, blocking both backend and integration before native cases. Approved V025/V026 require26 physical migrations. Update exact contiguous1..26 and exact resource count26 in IamSchemaIT. Historical V001/V002 hashes, original1..5 identities, V005 non-destructive clauses and all migration account-seeding checks remain enforced. Assertions are not removed, generalized or skipped; workflows are unchanged. Scope review confirms this aligns CI fixtures with approved migrations rather than weakening a CI contract.

Local targeted Mes002MigrationContractTest:2 tests,0 failures/errors/skips PASS. Native IamSchemaIT and complete CI validation remain subject to the new hosted run. No local database writes, repair/reset, executed migration edits, credential/config staging or forced push. PR#4 is the required integration gate; merge only after all applicable jobs pass.
