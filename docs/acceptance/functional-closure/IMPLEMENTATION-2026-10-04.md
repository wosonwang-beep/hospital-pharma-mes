# Approved functional closure implementation — 2026-10-04

Approval: human “批准方案” for DCP-MES-008-011-FUNCTIONAL-CLOSURE-001. This is implementation evidence, **not completed native acceptance**.

## Implemented scope

- MES-008: signed inventory freeze/unfreeze commands; immutable predecessor-linked inventory decisions; current locked Lot/date/QA eligibility Gate; UI on existing Lot360°; lineage included in charge→Lot incoming trace.
- MES-012 approved IPC producer stage / MES-010 consumer: draft process IPC definitions, publish lint and frozen route snapshot, atomic operation initialization, original result/revision/independent validity review, mandatory and valid-original-FAIL completion Gate. Existing completionRule grammar unchanged. Existing incoming result table and regulated history unchanged.
- MES-010: original signed clearance record, independent signed review, exact operation/current equipment scope, START/RESUME checks current evidence; fail/rejected/latest evidence blocks. Existing execution workbench actions and history.
- API ID/FK/person/signature identifiers use strings; decimals remain strings. Embedded IPC details require qms:ipc:view. New permissions are defined without implicit role grants.

Implementation files: InventoryDecisionService/Controller, IpcService/Controller, ClearanceService and ExecutionController, owner entities/mappers, ExecutionQualityPort/QualityOperationQueryService, approved ProcessCommands/Definitions/Rules changes, FunctionalClosureConfiguration, SignedRecordSupport, existing trace adapter, FunctionalQualityActions and three existing frontend pages.

## Verification

| Gate | Result | Limit |
|---|---|---|
| Backend modular reactor compile/package | PASS, latest mes-functional-compile-verified.log | Does not prove database behavior |
| IPC domain and Gate tests | 3 methods PASS: numeric/text exact boundaries; optional valid FAIL; actual required-instance absence | Unit adapters mocked; not native transaction/signature proof |
| Frontend typecheck | PASS | Rendered tests below cover changed controls |
| Playwright actual browser / mock APIs | 4 cases PASS, 11.7 s, desktop + mobile inventory/IPC flows | Frontend interaction only; not real server E2E |
| UI layout | Horizontal label/control bounding assertions PASS; four screenshots preserved under ui/; mobile inspected | Screenshot predates cosmetic hiding of the internal allowedActions list; label/input layout unchanged |
| Native freeze test RED | Expected missing freeze producer before implementation | Proper initial failing feature test |
| Native freeze GREEN attempt | BLOCKED by failed V024 migration, before fixture/business commands | No positive native claim |
| Independent code review and one fix pass | HIGH optional valid FAIL Gate fixed; string IDs and IPC read permission aligned; targeted static re-review no new HIGH/CRITICAL | Static review, no migration/transaction acceptance |
| Original query-service preservation | Original ExecutionQueryService source recovered after accidental overwrite; packaged bytecode SHA-256 exactly equals pre-turn package | New helper moved to separate QualityOperationQueryService; original consumer contracts retained |

Browser plugin/skill unavailable; repository Playwright used. No full regression, no repeated passing native gates, no alternate database, no physical deletion or commit/push.

## Migration failure and stop

V001–V023 remain successful and unchanged. V024 first attempt failed due to Codex using nonexistent `wms_material_lot` instead of the actual `md_material_lot` FK. Only the IPC definition column was added before failure. V024 original file and native failed history are retained under migration-failure/. No repair, migration rewrite, data removal or DEV rebuild occurred.

[Transparent recovery proposal](../../development/DCP-MIGRATION-V024-RECOVERY-001-PROPOSED.md) requests an explicit one-time exception for this failed migration. Existing AGENTS.md prohibits modifying executed migrations and repair; therefore routine development authorization cannot silently authorize recovery.

Until recovery, normal Flyway/application startup and native business acceptance are blocked. Three real native command-flow tests are written in FunctionalClosureIT but not certified PASS. Required current-transaction/concurrency, audit rollback, actual signing, process snapshot regression and clearance negative-scope cases remain to execute/complete after recovery. No task is READY merely because the code compiles or the browser fixtures pass.

## Baseline and task readiness

v1.0.15 cumulative contract candidate includes approved scope, complete action schemas, owner records, task/integration/RTM mappings, permission/UI/GMP requirements, prototype note and candidate manifest. OpenAPI references resolve and changed CSV matrices retain their header widths; inherited short/overlong CSV cells were normalized without dropping text. Candidate pointer **not switched**; v1.0.14 remains current with explicitly approved bounded delta recorded in PROJECT_BASELINE.

MES-008/008A/010/011 remain IN PROGRESS. MES-012 remains IN PROGRESS and this approval adds only its bounded IPC stage. MES-007 remains human ACCEPTED. MES-009 prior readiness is historical; directly affected initialization regression must run after recovery before integration completion. Remaining full-task RTM and real native gates must be closed before READY FOR ACCEPTANCE; only human approval sets ACCEPTED.
