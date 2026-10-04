# Native concurrency fixture decision — 2026-10-04

APPROVED: user explicitly replied “批准” to the bounded retained DEV fixture request. The exception applies only to the uniquely identified fixtures and preserved audit history described below. Existing business data, immutable migration history and no-delete guards remain unchanged. NativeConcurrencyIT is opt-in via mes.retained-concurrency-fixtures=true; ordinary rollback suites do not silently enable it. Record each retained fixture's exact IDs and final REJECTED/BLOCKED state in acceptance evidence.

## Concrete remaining gate

MES-007's required TC-EBR-009 is the already-passing two-client stale-revision sequence, not an extra simultaneous transaction requirement. Its Designer duplicate-field gap is now fixed and verified. Remaining real concurrency requirements are order allocation (MES-009 approved order lock) and final-boundary quality supersession/reservation/issue/weigh/charge (TC-ELG-001..004).

Two physical database transactions must see the same previously committed fixture. An enclosing rollback fixture is invisible to independent ordinary transactions; sharing its connection is not real transaction concurrency. Once a released snapshot/result/signature/ledger fixture is committed, its regulated evidence cannot be deleted or overwritten for cleanup. Existing no-delete triggers must remain enabled. No additional local database, Docker database, database reset, migration repair or seed mutation is permissible.

## Proposed bounded fixture-retention exception

Use the existing native hospital_pharma_mes_dev only. Create uniquely named IT_RACE resources through existing application commands and the established qualified test identities; no new schema, fields, APIs, states, permission or production workflow. Record exact IDs in acceptance evidence.

1. Order allocation: one test product/unit/order, a committed draft allocation and two independent competing createBatch transactions. Planned sum must stay within order plan, one conflicting allocation must fail, and the rejected command must leave no batch/audit/idempotency success. Retain only test-created order/draft batches and audit history.
2. Final quality gate: real receipt, frozen standard, sampling/sample/result/report and independent QA release; real batch/execution/reservation/issue/verified weighing prerequisites. Use latches around existing lock boundaries to exercise both serial orders for QA REJECTED supersession versus each final command. A command serialized after rejection must fail without downstream facts; a legitimate command serialized first must retain exact once-only evidence and subsequent rejection must prevent further use.
3. Preserve all original and successor signatures, results, snapshots, stock facts and lineage. Finish each test material lot using the existing signed REJECTED decision, keeping remaining stock BLOCKED and preventing production reuse. Prefix ordinary fixture codes/names so they are visibly test-owned. Retain the audit chain rather than physically cleaning regulated tables.
4. Do not run these committed-fixture cases until the exception is explicitly selected. Current rollback tests and ordinary development continue without this exception. No PROD/external service interaction or wider MES-012/013 producer development.

## Owner decision

Repository [AGENTS.md](../../AGENTS.md) requires local tests to preserve existing data and “roll back or remove only records created by that test”, and separately prohibits physically deleting production/regulated records. This plan requests a narrow exception to retain the uniquely identified committed test fixture and its immutable audit history in DEV. Existing general implementation/design approval is not silently treated as approval for this persistence-policy exception.

Without that exception, keep the real concurrency gates unverified and the affected whole tasks IN PROGRESS. Do not substitute mocked gates, shared-connection threads, or SQL mutation for the required concurrent application workflow. Actual IPC/clearance producer dependencies remain independently unavailable regardless of this fixture decision.


## Executed approved gate

11 native concurrency cases passed against distinct connections and observed pending FOR UPDATE queries; summary: ../../docs/acceptance/incoming-quality/evidence/native-concurrency-summary-2026-10-04.json.32 archived test lots verified signed REJECTED/BLOCKED, including failed RED trials. Two exposed overallocated draft test orders69/70 were reconciled using existing audited ProductionOrder:EDIT; original failed-trial facts remain retained. No new schema or migration. Any repeat reconciliation is separately opt-in with mes.reconcile-retained-red-trials=true and writes a new uniquely named evidence file, never replacing the original correction archive. Current production increment review has no remaining CRITICAL/HIGH.
