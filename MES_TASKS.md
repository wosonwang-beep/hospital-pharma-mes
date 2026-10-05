# MES Task Status Index

Latest verified increment (2026-10-05): [MES-012 closeout](docs/acceptance/mes-012/CLOSEOUT-2026-10-05.md) and [MES-013 closeout](docs/acceptance/mes-013/CLOSEOUT-2026-10-05.md) are ACCEPTED following explicit human confirmation “确认验收” on 2026-10-05. Native V001..V026 validated/schema026; MES-01315 distinct native business methods and required targeted gates PASS, independent CRITICAL0/HIGH0/MEDIUM0. Earlier MES-001..011 human acceptance remains unchanged. FINAL BASELINE COMPLETE v1.0.16 is authoritative. Acceptance is explicitly authorized by the user.

This is the sole task-status index for future Codex sessions. It records status and navigation only; requirements remain in `FINAL BASELINE COMPLETE v1.0.16`. Update status here when a task starts or reaches a verified gate. Only human approval may set `ACCEPTED`.

## Completed

### MES-001-R2

- Status: `ACCEPTED`
- Summary: Platform foundation completed for GxP audit, electronic signature, idempotency, integration inbox/outbox, and formal operations routes.

### MES-002-R2

- Status: `ACCEPTED`
- Summary: Authentication/RBAC, IAM administration APIs and UI, permission-filtered navigation, audit/idempotency integration, and physical migration V005 completed.

## Current and future

| Task | Status | Title | Dependencies | Task Card |
|---|---|---|---|---|
| MES-003-R2 | `ACCEPTED` | 组织、单位、设备与人员资格 | Hard: MES-001, MES-002 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-003-R2.md) |
| MES-004-R2 | `ACCEPTED` | 物料基本信息与单位换算 | Hard: MES-001, MES-003; Soft: MES-005 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-004-R2.md) |
| MES-005-R2 | `ACCEPTED` | 多供应商关系与唯一首选供应商 | Hard: MES-001, MES-003, MES-004 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-005-R2.md) |
| MES-006-R2 | `ACCEPTED` | BOM处方、工艺路线与参数版本 | Hard: MES-003, MES-004; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-006-R2.md) |
| MES-007-R2 | `ACCEPTED` | 动态eBR定义与运行引擎 | Hard: MES-001, MES-002, MES-006; Soft: MES-009 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-007-R2.md) |
| MES-008-R2 | `ACCEPTED` | WMS收货、库存、预留与发退料 | Hard: MES-003, MES-004, MES-005; Soft: MES-009 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-008-R2.md) |
| MES-008A-R2 | `ACCEPTED` | Incoming Material Quality & Material Release | Hard: MES-001, MES-002, MES-003, MES-004, MES-005, MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-008A-R2.md) |
| MES-009-R2 | `ACCEPTED` | 订单与正式批模型 | Hard: MES-003, MES-004, MES-006, MES-007; Soft: MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-009-R2.md) |
| MES-010-R2 | `ACCEPTED` | 工序、设备运行与参数采集 | Hard: MES-003, MES-006, MES-007, MES-009; Soft: MES-008 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-010-R2.md) |
| MES-011-R2 | `ACCEPTED` | 称量、投料与Genealogy | Hard: MES-004, MES-006, MES-008, MES-008A, MES-009, MES-010; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-011-R2.md) |
| MES-012-R2 | `ACCEPTED` | IPC、生产质量调查与物料平衡 | Hard: MES-008A, MES-009, MES-010, MES-011; Soft: MES-007 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-012-R2.md) |
| MES-013-R2 | `ACCEPTED` | QA放行与eBR归档 | Hard: MES-007, MES-008A, MES-009, MES-011, MES-012 | [Open](releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/tasks/MES-013-R2.md) |

DCP-MES-002-R2-001 resolves the former MES-012 ambiguity. Incoming material request/sampling/testing/report/release belongs to MES-008A; MES-012 owns production IPC, OOS/OOT, Deviation, CAPA, investigation, and material balance.

## MES-003–MES-005 pre-implementation review — 2026-10-03

- Authorized request: develop MES-003 through MES-005 sequentially and autonomously, with minimal targeted validation. No frozen-design change was authorized.
- MES-003 review outcome: `DESIGN CHANGE REQUIRED`; implementation has not started. MES-001/MES-002 are accepted, but the following frozen-contract gaps prevent completing MES-003 without inventing contracts:
  - `tasks/MES-003-R2.md` section 3 requires organization/unit/equipment/qualification UI. `10_UI_PAGE_ROUTE_MATRIX_V1.0.2_FROZEN.csv` defines equipment routes only for this scope; organization/unit/qualification routes are absent. They require an approved route/UI contract.
  - `08_API_DETAILED_V1.0.2_FROZEN.md` DTO rules require enums to be listed in OpenAPI. MES-003 operations in `08_OPENAPI_FULL_V1.0.2_FROZEN.yaml` use `GenericRequest`/`GenericResponse` (unrestricted objects), without the organization/equipment/qualification status enums. `07_STATE_MACHINE_DETAILED_V1.0.2_FROZEN.md` does not define their state transitions. These business contracts require specification before implementation.
  - `05_DATABASE_DESIGN_V1.0.2_FROZEN.md`, `md_unit_conversion`, specifies nullable `material_id BIGINT FK`. The migration matrix assigns conversion to LG-003 and material to dependent LG-004; it does not specify how the forward foreign key is staged. Approve its creation timing/ownership without introducing an early material table or silently omitting the required FK.
  - `10_UI_PAGE_DETAILED_V1.0.2_FROZEN.md`, UI-EQP-Q, requires a location column, while the frozen equipment schema defines no location field or relationship. Specify its authoritative source or approve a bounded UI/schema correction.
- Proposed approval boundary: complete only the MES-003 DTO/enums/state/UI contracts and the LG-003→LG-004 foreign-key handoff; resolve the equipment location source. Follow AGENTS.md design-change governance, preserve v1.0.2, and review affected authoritative artifacts together before switching baseline. This proposal is not approval and is not a second source of truth.
- MES-004/MES-005 remain `NOT STARTED`: their required MES-003 contracts are not physically available (`DEPENDENCY_NOT_READY`). Their task cards and implementation are deferred until MES-003 is unblocked.
- Evidence: read-only contract review; no product code, migration, database operation, or tests executed. Existing user-owned worktree changes were preserved. No task is ready for acceptance.

## MES-003 approval and execution — 2026-10-03

Human reply “授权” approves the four previously listed gaps. DCP-MES-003-R2-001 is recorded in v1.0.3; cross-document consistency review completed before authority-pointer switch. MES-003 is IN PROGRESS. Prior pre-implementation review is historical; its “no authorization” condition is superseded. MES-004/005 will begin after the required producer contracts are physically available.

## MES-004/005 approval — 2026-10-03

Human reply “授权” to DCP-MES-004-005-R2-001 explicitly approves its bounded material/supplier contract completion. v1.0.4 review PASS; old releases retained. Required MES-003 contracts available. MES-003 targeted verification: 8 unit tests, 12 native MariaDB IT, 15 frontend unit tests, 6 browser cases across desktop/mobile (API mocked); no remaining CRITICAL/HIGH in final scoped review. MES-004/005 IN PROGRESS under the user-authorized sequential scope.

## MES-003–005 verified delivery — 2026-10-03

- MES-003/004/005: READY FOR ACCEPTANCE. Human design authorization is not acceptance; no task was marked ACCEPTED.
- Implemented organization/UOM/conversion/equipment/qualification; complete material master and immutable versioned quality/storage/production/inspection policy; supplier qualification and retained approved relationships; frozen routes and permission-filtered UI.
- Native MariaDB: V006, V007, V008, V009 successfully applied, including the delayed conversion-material FK and frozen indexes. V001–V009 validation/history intact; no data reset or executed migration modification. Test-created users/materials checked with zero residue.
- Targeted backend: 13 distinct domain tests and 23 distinct integration cases passed across task gates. MES-004/005 first gate: 20/22 integration PASS including all 12 upstream cases; two test-construction errors fixed and only those two rerun PASS. One additional relationship replay/stale/expiry contract case PASS. No full regression.
- Frontend build PASS; latest 15 focused unit tests PASS (2.25s). Browser: 6 MES-003 and 4 MES-004/005 desktop/mobile cases passed across task gates. Material same-URL state refresh bug fixed; only its two failed flows rerun (2/2 PASS, 8.5s). Browser APIs mocked; actual API/database/audit behavior verified separately by MockMvc/native MariaDB.
- Final read-only scoped and increment reviews: CRITICAL=0, HIGH=0, remaining necessary MEDIUM=0. Preserved existing frontend bundle-size warning and inherited duplicate WMS route-matrix rows as LOW/out-of-scope debt. Batch/receipt snapshot consumer integration remains in its respective later task, not preimplemented here.
- Evidence: docs/acceptance/mes-003/ACCEPTANCE.md, docs/acceptance/mes-004/ACCEPTANCE.md, docs/acceptance/mes-005/ACCEPTANCE.md. No commit/push, no local credential/config staging. Stop after MES-005 as requested scope.

## Material basic maintenance correction — 2026-10-03

Human explicitly cancels material version/approval and confirms exactly one preferred supplier per material. DCP-MATERIAL-BASIC-001 authorized; v1.0.5 cross-document review PASS. MES-004/005 returned to IN PROGRESS for this bounded rework; previous verification remains historical. MES-003 UOM consumed contract stays ready. No future task started.

## Material basic correction verified delivery — 2026-10-03

- MES-004/005 READY FOR ACCEPTANCE under v1.0.5 DCP-MATERIAL-BASIC-001. Material has direct basic maintenance, no business version/approval, named units/material-specific conversion, multiple suppliers and exactly one preferred among active relationships. Supplier qualification retained.
- Native MariaDB V010 applied; history 001–010 successful, append-only and no rebuild. Zero current test material/actor residue.
- Focused Maven gate: 10 unit tests + 14 integration cases PASS; only review-affected legacy ACTIVE-filter integration case rerun PASS. Frontend build and 8 focused unit tests PASS. Six desktop/mobile browser flows PASS across scoped gates; API mocked, actual native API/DB/audit verified separately. No full regression.
- Final review no CRITICAL/HIGH/necessary MEDIUM; OpenAPI 1405 local refs valid and cross-document alignment recorded. Prior releases immutable; receipt/batch consumer work remains future scope. Acceptance records in docs/acceptance/mes-004 and mes-005, historical v1.0.4 behavior explicitly superseded. MES-003 remains READY FOR ACCEPTANCE; later tasks not started. No commit/push/config staging.

## Reference list/edit presentation — 2026-10-03

User-authorized DCP-UI-LIST-EDIT-001 published in v1.0.6; v1.0.5 preserved. Shared list/edit style adopts supplied index.html, inline query labels and horizontal edit labels with desktop/mobile adaptation. No data/API/permission/state change or migration. Frontend build PASS, 12 existing desktop/mobile flows PASS, four IAM form layout checks PASS, scoped review no blocking findings. Evidence: docs/acceptance/ui-list-edit/ACCEPTANCE.md. MES-003/004/005 remain READY FOR ACCEPTANCE; authorization is not acceptance; future tasks remain NOT STARTED.

## MES-006 startup contract review — 2026-10-03

User requested “开发mes-006”. Required Material/UOM contracts are physically available (Task Card 4A satisfied; upstream acceptance not inferred). Status IN PROGRESS; implementation paused at DESIGN CHANGE REQUIRED: generic/conflicting API DTO/path contracts, missing route dependency/equipment/clearance persistence, process lifecycle/signature/eBR soft-dependency conflict, and product write/UI scope inconsistency. Concrete bounded proposal: docs/development/DCP-MES-006-R2-001-PROPOSED.md. Current v1.0.6 unchanged; no business code/migration/test execution in this startup review. No later task started.

## MES-006 design authorization — 2026-10-03

Human “确认授权” explicitly approves DCP-MES-006-R2-001. Approved v1.0.7 contracts and pre-implementation consistency review replace prior blocker. MES-006 IN PROGRESS. V010 DEV history verified; V011 reserved. No later task authorized.


## MES-006 verified delivery — 2026-10-03

- MES-006 READY FOR ACCEPTANCE under original design and authorized DCP-MES-006-R2-001 / v1.0.7. Products, BOM, route/parameters, controlled versions, signature approval, publication and independent UI completed. No MES-007 or later task started; no ACCEPTED status inferred.
- Native DEV V011 applied; 001–011 history and checksums validated, no old migration edits or reset. Final test actor/product residue=0.
- Targeted validation: 5 domain unit tests + 9 distinct MES-006 integration tests + 1 direct UOM regression PASS across focused gates; frontend build PASS; 6 distinct desktop/mobile browser cases PASS. Only failed/affected cases were rerun. Actual signature/audit rollback and two concurrent transactions verified.
- Scoped review findings fixed: no remaining CRITICAL/HIGH/required MEDIUM. 1,424 OpenAPI references valid, 17 original operations retained, cross-document review and hashes completed.
- Evidence: [MES-006 acceptance record](docs/acceptance/mes-006/ACCEPTANCE.md). LOW debt: existing bundle-size warning and process-list query volume; clean migration/CI gate remains required before merge. No commit/push/local-config staging.


## Material name retirement and inline layout correction — 2026-10-03

User explicitly removes 通用名/英文名/别名 and reiterates horizontal labels/controls. DCP-MATERIAL-NAMES-UI-001 / v1.0.8 is the current bounded correction. Removed the fields from material UI, current DTOs/read snapshots and search; retained legacy physical values and audits. Shared master/query styling and process sidebar inputs align labels with controls; current prototype CSS/design-system synchronized. No new migration or later task.

MES-003/004/005 remain READY FOR ACCEPTANCE after the correction: 4 targeted native integration cases, 5 frontend tests, build and 4 distinct desktop/mobile browser cases PASS, including 28 master list/form viewport checks. Scoped self-review has no CRITICAL/HIGH; inherited bundle/CI debt remains. Evidence: [material/UI correction](docs/acceptance/material-ui-correction/ACCEPTANCE.md). Historical delivery entries above remain historical, not claims that superseded fields/layout are current.


## MES-006～008 baseline review and MES-006 correction — 2026-10-03

- User explicitly requested continued MES-006/007/008 work, first reading all baseline designs including UI standards and prototype. Full numbered design-document review was shared between the implementer and two read-only domain reviewers; exact coverage/evidence is in docs/development/mes-006-008-baseline-review.md. v1.0.8 manifest: 80/80 hashes match. Authority unchanged.
- MES-006 remains READY FOR ACCEPTANCE after correcting the existing UI contract: dedicated approval signature dialog, object-filtered audit links, query preservation across create/edit/version/lifecycle/return, and correct restoration of keywords containing uppercase T. No backend/schema changes. Fresh vue-tsc PASS; 6 desktop/mobile flows PASS (14.8s); 2 signature geometry/cancel-credential cases PASS. API mocked in browser; prior native evidence retained, not rerun or claimed fresh.
- MES-007/008 are IN PROGRESS for authorized pre-implementation review, stopped at DESIGN CHANGE REQUIRED for full delivery. MES-007 runtime migration belongs to MES-009 and references MES-010 operations, while its own acceptance requires actual runtime integration. MES-008 has a receipt-edit UI without save API, unsequenced future MainBatch FKs, and cross-task eligibility/test ownership. Required producer contracts are not yet physically available. Product code for these two tasks has not been implemented; no READY/ACCEPTED claim.
- Concrete bounded resolution for review: docs/development/DCP-MES-007-008-SEQUENCING-001-PROPOSED.md. No frozen API/dependency/migration change has been applied. Definition/receipt portions are technically implementable; partial implementation would not satisfy current full task acceptance. No temporary production models, permissive release substitutes, extra migration, DB operation or full regression.


## MES-007/008 approved staged execution — 2026-10-03

Human explicitly approved DCP-MES-007-008-SEQUENCING-001. v1.0.9 pre-implementation consistency review PASS and authoritative pointer switched; v1.0.8 retained immutable. MES-007/008 continue IN PROGRESS under approved stages; no repeated approval is required within that boundary. V012 eBR and V013 WMS allocated after read-only native DEV history confirmation at V011. Definition/Designer/DSL and receipt/lot/ledger are current delivery; original real runtime/eligibility/batch acceptance remains pending later dependencies.


## MES-007/008 approved staged execution — 2026-10-03

Human explicitly approved DCP-MES-007-008-SEQUENCING-001. v1.0.9 pre-implementation consistency review PASS and authoritative pointer switched; v1.0.8 retained immutable. MES-007/008 continue IN PROGRESS under approved stages; no repeated approval is required within that boundary. V012 eBR and V013 WMS allocated after read-only native DEV history confirmation at V011. Definition/Designer/DSL and receipt/lot/ledger are current delivery; original real runtime/eligibility/batch acceptance remains pending later dependencies.


## MES-007/008 approved current-stage delivery — 2026-10-03

- Human “批准按该方案补齐契约并继续开发” implemented through approved DCP-MES-007-008-SEQUENCING-001 and cumulative v1.0.9. Prior v1.0.8 80/80 hashes preserved; v1.0.9 83/83 valid. Concrete 11 eBR + 35 WMS operations and exact permission mappings verified; no unapproved route.
- MES-007 LG-007A implemented: normalized definitions, independent Designer, safe DSL/lint/simulation, controlled lifecycle and immutable published query contract. Runtime LG-007B, real batch snapshots/corrections/review/signature/PDF and later operation FK remain scheduled with the actual producer tasks.
- MES-008 current stage implemented: draft/save/confirm receipts, material/policy snapshots, blocked lots, append-only ledger, inventory/move/adjust and fail-closed dependent writes. Real MES-008A eligibility/release and MES-009 reservation/issue/FEFO integration and delayed main_batch_id FKs remain required; no quantity-only gate or placeholder batch.
- Native DEV upgrade: V012 and V013 applied, 13/13 history successful and validated, no reset/repair/history rewrite. Read-only post-checks: test actor/unit residue 0, nine eBR definition tables and two ledger guards present.
- Targeted validation: backend 20 unit + 17 distinct native integration PASS. First native run 16/17; one invalid-date test collided with its own receiptNo, corrected only the fixture and reran that one case PASS. Includes two-transaction family/lot locks, optimistic version, replay, audit rollback, permissions, organization and direct producer contracts. Frontend typecheck + 6 unit + 12 distinct desktop/mobile cases PASS (browser APIs mocked). No full regression.
- Fresh scoped review: CRITICAL=0, HIGH=0, necessary MEDIUM=0 after fixes. LOW: process picker only first 100 packages; per-row aggregation reads; inherited Flyway tested-version warning for MariaDB 13. Hosted CI and full future integration gates remain mandatory before their relevant milestones.
- MES-006 remains READY FOR ACCEPTANCE. MES-007 and MES-008 remain IN PROGRESS by the approved sequencing rule; current-stage delivery is not original full-task acceptance. MES-008A/009/010 remain NOT STARTED; no further task automatically begun.
- Evidence: docs/acceptance/mes-007/ACCEPTANCE.md and docs/acceptance/mes-008/ACCEPTANCE.md, with desktop/mobile screenshots. No commit/push, credential staging or unrelated worktree cleanup.


## MES-006 human acceptance — 2026-10-03

Human explicitly stated “mes 006验收”. MES-006-R2 is ACCEPTED; prior readiness entries remain historical evidence. This acceptance does not change MES-003/004/005/007/008 statuses. Next recommended task is MES-008A-R2 (incoming material quality and release), consuming the delivered receipt/lot/ledger contracts; it remains NOT STARTED because this turn asks for sequencing, not implementation. No code, migration or test change.


## MES-007/008/008A completion request — 2026-10-03

- Human requested “完成mes007，mes008，mes-008A的开发”. MES-008A moved to IN PROGRESS for authorized startup; MES-006 stays ACCEPTED, MES-007/008 stay IN PROGRESS with prior stage evidence preserved.
- Current read-only task-card/backend/UI contract review found DESIGN CHANGE REQUIRED: incoming Create/execute pages lack routes despite frozen independent-page rules; qms_sample/qms_release_decision MainBatch FKs lack staged-install ownership before MES-009. Nullable MainBatch, retired MaterialVersion wording, GenericDTO and extensible signature providers were expressly ruled out as separate blockers.
- Concrete bounded proposal: docs/development/DCP-MES-008A-CONTRACT-001-PROPOSED.md. Existing 007/008 sequencing approval covers only the previously named WMS/eBR FKs, not these QMS references/routes. v1.0.9 authority retained; no new business code, migration or tests run.
- Full MES-007/008 runtime and production integration still requires actual MES-009/010 producer work; merely adding008A cannot complete those gates. No unapproved future business module was started or mock gate installed. Await bounded contract decision before affected implementation.


## MES-008A approval and MES-009/010 scope authorization — 2026-10-03

Human explicitly approved `docs/development/DCP-MES-008A-CONTRACT-001-PROPOSED.md` and added MES-009/010 to this development round. The six incoming create/execute routes, existing quality workbench mapping, nullable incoming MainBatch references, delayed QMS MainBatch FKs and finished_lot_id → md_material_lot.id target are authorized. MES-009/010 are IN PROGRESS for dependency and contract review; this does not imply acceptance or authorize additional frozen design changes.

Read-only native DEV history check: V001–V013 all successful. No new migration executed and no historical migration changed. Candidate allocations: V014 incoming quality, V015 real production + delayed batch FKs, V016 eBR runtime, V017 execution + operation FK; these are not claims of completed implementation. v1.0.9 remains authoritative until cumulative cross-document review passes. Existing worktree and persistent data preserved.


MES-009/010 contract review found additional frozen conflicts outside the approved incoming change: MainBatch snapshot NOT NULL vs release-time creation; required batch edit page without save API; plan-date UI without columns; undefined ProductionOrder lifecycle; conflicting Operation gate-failure states. Status remains IN PROGRESS, current integration implementation stopped at DESIGN CHANGE REQUIRED. Concrete bounded proposal: docs/development/DCP-MES-009-010-CONTRACT-001-PROPOSED.md; supporting scoped contracts: mes009-contract.md and mes010-contract.md. Incoming candidate extracted 32 existing operations/36 schemas. Current authority v1.0.9 integrity rechecked: 83/83 hashes match. No business-code change, new migration, or application test was performed in this review; no new readiness claim. Prior approvals remain valid.


## Incoming workflow clarification — 2026-10-03

User supplied the six-record required-inspection workflow from supplier delivery through QA release and downstream consumption. Scoped comparison is recorded in docs/development/mes008a-contract.md: existing frozen main chain agrees; QC_PASS is not material release, unresolved OOS blocks, report references exact result revisions, QA release atomically controls availability, TEST/retention samples preserve sampling lineage. Current WMS lacks a COA delivery-document association in receipt DTO/entity/UI; this remains a concrete implementation-contract gap, not a passed capability. No product/schema change or application test in this clarification; MES-008A stays IN PROGRESS. Existing exemption policy and future-task boundaries were not silently removed or expanded; prior approvals remain valid.


## Incoming quality full Baseline Review — 2026-10-03

At the user's explicit request, review and implementation mapping only: docs/development/INCOMING_QUALITY_IMPLEMENTATION_MAP.md plus incoming-review-data.md, incoming-review-api-ui.md and incoming-review-controls-tests.md. No product code, SQL, configuration, frozen release, or earlier candidate was changed; no database access, migration or application tests. User confirmation of the map/review is required before implementation.

Current v1.0.9 manifest rechecked 83/83. Six-record main chain maps to frozen requirements; current WMS receipt/QUARANTINE ledger stage matches, while incoming QMS/QA and real downstream consumer/trace producers are missing. New requirement-to-contract gaps include the QC specification-version producer/FK, sampling/retest-instance semantics, INVALID vs INCONCLUSIVE, request-level report aggregation, delivery attachments and incoming investigation linkage. Old draft enum/association assumptions must not become authority. Explicit stale signature predecessor is a static SEMANTIC MISMATCH requiring a later targeted fix/test. See the map for bounded DESIGN GAP/BASELINE CONFLICT findings and already-approved/resolved exclusions.

MES-008A remains IN PROGRESS; no task newly marked READY FOR ACCEPTANCE or ACCEPTED. Prior bounded approvals remain valid; no scope expansion to future tasks or implementation occurs through this review.

## Incoming quality confirmed foundation implementation — 2026-10-03

User confirmed “确认流程已清楚就可以开发”. Implemented the unambiguous frozen request/sampling/sample/inspection/report named state transitions and request-type dictionary; corrected explicit re-sign so it cannot skip the latest invalidated predecessor. No new database contract, API or UI was invented. Evidence: docs/development/mes-008a-confirmed-execution.md.

Targeted verification: 17 unit/contract tests PASS; 1 native MariaDB transaction regression PASS, unique fixtures rolled back. Flyway validated 13 migrations, current V013, no new migration. Scoped independent review: CRITICAL=0, HIGH=0. No full regression run. Existing MariaDB/Flyway compatibility and Mockito agent warnings remain non-blocking debt.

MES-008A remains IN PROGRESS. Domain helpers are not the six-record application workflow; request persistence, quality-standard version producer, retest identity, report aggregation, incoming investigation contracts and UI gaps remain bounded by the implementation map DG-01..08 / BC-01..02. Full incoming E2E and production reverse trace are not implemented or accepted. No task newly marked READY FOR ACCEPTANCE or ACCEPTED.

## Confirmed incoming mapping and approved-contract publication — 2026-10-03

Human confirmed “确认这份映射和审查结果，继续开发”. That review confirmation is recorded; it is not re-requested. Published only the earlier explicitly approved DCP-MES-008A-CONTRACT-001 A/B as cumulative v1.0.10: six create/execute routes, existing workbench mapping, staged QMS batch FK ownership and canonical finished-lot reference. Database/Domain/State/API/UI/Permission/GxP/Test/RTM/Integration/Task references were reconciled within that bounded delta before switching authority pointers.

Validation: previous v1.0.9 83/83 manifest hashes unchanged; new v1.0.10 84/84 hashes verified; 1,558 OpenAPI local references resolve; seven approved route mappings unique; API content otherwise unchanged. Independent bounded review found no blocking issues. No product-code change, migration, database operation or application test in this document-only step. Earlier test evidence is not counted again.

Remaining DESIGN GAP boundaries are unchanged. A concrete proposed completion boundary is docs/development/DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md; it is not approved and not a second formal authority. The first blocking application contract is the missing QC specification-version producer/FK required at request creation. The proposal also addresses sampling/retest/report/attachment/investigation/UI gaps and separates real MES-011 integration scope from mere flow approval. MES-008A and other in-progress tasks retain their status; no new READY FOR ACCEPTANCE or ACCEPTED claim.


## Incoming full-acceptance contract completion authorization — 2026-10-03

Human explicitly approved DCP-INCOMING-QUALITY-GAPS-001 completion and full acceptance scope, including the referenced MES-009/010 contract decisions, real MES-011 weighing/charge/genealogy and only the incoming OOS/blocking-deviation/retest subset of MES-012. Existing production IPC/CAPA/material-balance and MES-013 are not expanded. No repeated approval is needed for these bounded details. MES-011 and MES-012 (limited subset) move to IN PROGRESS for contract completion.

Plan: docs/development/incoming-approved-implementation-plan.md. Independent QC, incoming-chain and production contracts are being completed; root owns attachment contract and baseline integration. Native read-only Flyway history confirms successful V001–V013, next allocation starts V014. No new migration executed yet. Preserve all existing data and unrelated worktree changes.


## Approved full incoming contracts published — 2026-10-03

v1.0.11 cross-consistency review PASS and authority switched after approval. Exact QC/incoming/attachments/production appendices, typed OpenAPI, original-scope protection, UI field prototype and TC/RTM/dependency deltas complete. Source v1.0.10 84/84 hashes unchanged; merged 233 operations / 1,953 local refs checked. No runtime PASS inferred. Physical plan V014–V020 begins next; no new migration applied by publication.


## Incoming approved completion supplement — 2026-10-03

Existing explicit approval covers v1.0.12 bounded runtime DTO/review evidence, issue-return fact, deterministic independent weighing verification and actual trace node identities. Cross-document review passed before authority pointer switch; v1.0.11 preserved byte-for-byte. New migrations remain unexecuted at publication. MES-007/008/008A/009/010/011 and incoming-only MES-012 scope remain IN PROGRESS. No runtime acceptance or wider MES-012/013 authorization inferred.


## Incoming implemented chain and bounded acceptance evidence — 2026-10-03

The explicit contract-completion/full incoming acceptance authorization has been implemented through cumulative v1.0.14. Six distinct records, frozen QC standard, immutable original FAIL and approved retest, independent QA release, receipt attachments, real production reservation/issue/weigh/charge and reverse trace are integrated. eBR required-role review evidence binds the exact persisted signature ID in the original signature transaction. Frozen standard trace nodes reference the actual request FK, never the latest standard.

V014–V022 applied successfully; final Flyway validation/history shows 22 successes, 0 failures, current V022. Executed migrations and earlier releases remain immutable. v1.0.14 manifest 98/98 hashes verified. Read-only final test sentinel counts: incoming actors 0, incoming material fixtures 0; no database reset/repair used.

Targeted evidence: 26 distinct native integration methods PASS across scoped runs; final affected production-chain rerun 1/1 PASS (11.423s suite), covering actual Issue blocking before QA, confirmation blocking after QA rejection, and exact frozen-standard trace node/edge. 18 desktop/mobile browser cases PASS with API mocks; these are not native browser E2E. Frontend vue-tsc -b PASS. Platform signature unit tests 8/8 and eBR rules/boundaries 9/9 PASS. Scoped HIGH signature correlation and trace omissions closed with regression evidence.

The requested incoming positive chain and A–E scenarios have verified evidence and are available for bounded human acceptance. Broader MES-007/008/008A/009/010/011/012 task statuses remain IN PROGRESS: their full mandatory RTM cases are not all proven or waived. Production IPC/CAPA/material balance and MES-013 were not implemented under this bounded approval. No task is automatically ACCEPTED. Remaining formal coverage/producer boundaries are listed in docs/acceptance/incoming-quality/RTM.md; acceptance and compact evidence are in docs/acceptance/incoming-quality/ACCEPTANCE.md and evidence/verification-summary.json. No full regression/hosted CI/merge claim.


## Incoming quality bounded human acceptance — 2026-10-03

- Human approval: user explicitly replied “验收” after the final implementation/verification report.
- Accepted scope: this round's six-record incoming-quality chain and user A–E acceptance scenarios, including real production issue/weigh/charge and reverse trace, against v1.0.14 and the archived verification-summary.json.
- Scope status: `ACCEPTED` by explicit human approval. Evidence: docs/acceptance/incoming-quality/ACCEPTANCE.md and evidence/verification-summary.json.
- Broader MES-007/008/008A/009/010/011/012 task rows retain IN PROGRESS for their remaining formal RTM obligations. This acceptance does not waive those cases or expand production IPC/CAPA/material balance/MES-013 scope.
- Documentation-only acceptance recording; existing passing evidence reused. No additional tests, migration or automatic next-task implementation performed.


## MES-003–MES-005 human acceptance — 2026-10-03

Human explicitly confirmed “MES-003～005 确认验收”. MES-003-R2 (organization, units, equipment and personnel qualification), MES-004-R2 (material basic information and unit conversion), and MES-005-R2 (multiple supplier relationships and unique preferred supplier) move from READY FOR ACCEPTANCE to ACCEPTED. Existing implementation and verification evidence is retained. This documentation-only status update introduces no code/schema change or additional test run and does not start another task.

## MES-007–011 continued implementation — 2026-10-04

Latest closeout supersedes this earlier increment's MES-007 IN PROGRESS limitation: MES-007 is READY FOR ACCEPTANCE after TC-EBR-001 Designer/API gap correction and exact001..009 mapping. Evidence: docs/acceptance/mes-007/CLOSEOUT-2026-10-04.md and docs/acceptance/incoming-quality/CLOSEOUT-2026-10-04.md.34 distinct native methods,30 units,10 API-mocked browser cases and typecheck passed in this closeout; no full regression. No new physical migration; V001–V023 retained. Independent review has no remaining actionable HIGH. TC-BAT-002 stale errors were already resolved by the approved009/010 contract; no fresh DCR inferred. MES-008/008A/009/010/011 stay IN PROGRESS for actual concurrency and IPC/clearance boundaries; owner selection on docs/development/mes-007-011-native-concurrency-fixture-decision.md is pending before committed test-fixture retention. No human acceptance inferred.

- Authorization: human “继续完善和开发MES-007～011”; existing bounded v1.0.14 incoming/runtime/production approvals remain applicable. No frozen fields/routes/permissions/state/task redesign or future-task expansion.
- Fixed demonstrated eBR required repeated empty-array and optional zero-row defects; real production/execution/trace HTTP parameter binding; immutable production snapshot enforcement via new V023. All executed prior migrations retained unchanged.
- Verification:16 distinct new native methods PASS,14 affected units PASS; final affected regression EbrRuntimeIT3/3 (12.83s) and IncomingProductionIT2/2 (3.442s). Final HTTP/snapshot gate3/3 (10.62s). No full regression/repeated browser loop; frontend unchanged. Independent review findings corrected, no remaining demonstrated CRITICAL/HIGH in this increment.
- Native DB read-only check:23 successful/0 failed migrations, highest23;2 snapshot guards; test actor/role/material sentinels0. Existing data/configuration preserved, no reset/repair/alternate database or commit/push.
- Per-task exact methods, TC mapping and remaining obligations: docs/acceptance/incoming-quality/MES-007-011-2026-10-04.md and RTM.md. MES-007/008/008A/009/010/011 remain IN PROGRESS: positive/sequential flows do not waive unproven concurrent/negative matrices. MES-010 actual production IPC/clearance integration is DEPENDENCY_NOT_READY and remains fail closed. No whole-task READY/ACCEPTED claim; prior bounded incoming and MES-001–006 human acceptance preserved.


## Approved native concurrency closeout — 2026-10-04

Human “批准” approves only the bounded retained DEV concurrency fixtures in docs/development/mes-007-011-native-concurrency-fixture-decision.md. This supersedes the earlier pending owner-selection statement; no further design change or task acceptance is inferred.

MES-009 READY FOR ACCEPTANCE: required TC-BAT-001..004/TC-UI-002 and PRD-001..004 mapped in docs/acceptance/mes-009/CLOSEOUT-2026-10-04.md. Actual competing allocation exposed repeatable-read oversubscription; production now uses locked current allocation facts, current locked target order and one current child list for completion. Three related HIGH review findings are fixed. Native concurrency11/11 PASS (5.589s); affected production regressions2/2 PASS (9.995s); ProductionRules3/3 PASS. Added SubBatch no-release/no-QA mapped-route and no-action assertions passed in a single targeted retry. Reruns do not add cases.

MES-007 remains READY FOR ACCEPTANCE. MES-008/008A/010/011 remain IN PROGRESS: QA-supersession final-boundary concurrency is now evidenced, but wider required incoming/expiry/retest/freeze cases and actual IPC/clearance producer integration remain unclosed. No future-task producer invented. See updated incoming RTM and closeout for exact boundaries.

No new migration: V001–V023 validate successfully,23 success/0 failed.32 exact archived test lots all REJECTED/BLOCKED; intentional retained fixtures are not zero residue. Two RED-trial draft orders69/70 were reconciled through audited ProductionOrder:EDIT, preserving original allocation/audit evidence. No database reset/delete/repair, executed migration edit, commit/push or credential/config staging. Scoped review no remaining CRITICAL/HIGH; LOW pagination/N+1 and Flyway supported-version warning retained. Evidence: docs/acceptance/incoming-quality/evidence/native-concurrency-summary-2026-10-04.json.


## MES-007 human acceptance — 2026-10-04

User explicitly confirmed “MES-007 确认验收”. MES-007-R2 is ACCEPTED for the completed dynamic eBR definition/Designer/DSL/runtime scope and required TC-EBR-001..009 mapping in docs/acceptance/mes-007/CLOSEOUT-2026-10-04.md. Prior validation and recorded LOW debt remain preserved; PDF/archive TC-EBR-010 remains MES-013 scope. This status-only acceptance does not accept other MES tasks or change the frozen baseline. No implementation, migration or test rerun accompanies this record.


## MES-008–011 second closeout increment — 2026-10-04

User requested “MES-008～011 继续收尾”. Scope remains v1.0.14 and previously approved bounded completion, not implicit new frozen contracts or production MES-012/013 expansion. MES-007 stays ACCEPTED and MES-009 stays READY FOR ACCEPTANCE; no accepted task reimplemented.

10 new native cases passed:8 expiry/retest-date boundary cases across actual reserve/confirmIssue/weigh/charge, plus2 incoming guards (missing required result cannot submit review; exhausted retest quota and signed VALID original FAIL closure still block). Date tests use real authored receipt dates, full QC/signed-QA and physical database, advancing only thread-local Instant.now; no stored date/state SQL alteration. Initial gate6/8 passed,2 failed only on test postcondition column name; only those2 rerun,2/2 PASS. Incoming2/2 PASS. No production/UI change or full regression. Increment review no concrete CRITICAL/HIGH.

MES-008/008A/010/011 remain IN PROGRESS. Exact remaining contract/producer boundaries and formal TC mappings are recorded in docs/acceptance/incoming-quality/CLOSEOUT-2026-10-04-R2.md: frozen inventory command/API/permission/signature contract is unavailable (DESIGN CHANGE REQUIRED for that portion); actual required IPC/clearance producer remains DEPENDENCY_NOT_READY. Existing broad incoming partial RTM assertions are not silently waived. Do not fabricate a freeze command, generic updateStatus, IPC fact, clearance evidence or broaden MES-012.

No new migration: native Flyway23 successful/0 failed,max023,validated. All new fixtures rolled back; DATE-prefixed RELEASED fixture lot count0. Existing approved32 retained concurrency fixtures/history remain. LOW pagination/N+1 and Flyway supported-version warning retained. No reset/delete/repair/seed changes/commit/push/config staging. Evidence: docs/acceptance/incoming-quality/evidence/closeout-2026-10-04-r2.json.


## MES-008/008A/010/011 third closeout increment — 2026-10-04

User again requested continued closeout.7 new passing native methods close two-required-item numeric/text aggregation and exact review, retention label/storage/quantity/unit/source and signed disposal, real competing QA successors, actual competing consumption of one verified weighing, QC approval permission/version/signer failure/replay and V2 approval preserving V1, and empty/oversized delivery-document zero-write boundaries. Required mapping and limitations: docs/acceptance/incoming-quality/CLOSEOUT-2026-10-04-R3.md.7 is a distinct-method count, not rerun count; failed fixture assumptions corrected with failure-only gates. No production code/UI change or full regression. Scoped review no concrete CRITICAL/HIGH.

V001–V023 validate;23 success/0 failed,max023, no migration.35 approved retained native trial lots all REJECTED/BLOCKED (32 prior plus3 trials); original failed-trial evidence preserved, no physical cleanup. Other new facts rollback. No reset/repair/seed changes/commit/push/config staging. Existing LOW pagination/N+1/Flyway tested-version warning remains.

Statuses unchanged: MES-007 ACCEPTED, MES-009 READY FOR ACCEPTANCE, MES-008/008A/010/011 IN PROGRESS. Freeze workflow contract and production IPC/clearance producer boundaries remain; wider unclosed RTM clauses are not waived. A concise request for any already approved supplemental document paths is pending; it is not new design authorization. Existing-contract work proceeded without waiting. Evidence: docs/acceptance/incoming-quality/evidence/closeout-2026-10-04-r3.json.

## MES-008/008A/010/011 final readiness — 2026-10-04

- Status: all four `READY FOR ACCEPTANCE`; only human approval may set `ACCEPTED`.
- [Formal TC-by-TC closure / independent review](docs/acceptance/incoming-quality/FINAL-CLOSEOUT-2026-10-04.md); [exact evidence](docs/acceptance/incoming-quality/evidence/final-closeout-2026-10-04.json).
-13 new native cases,11 QC units,3 frontend interception units,4 QC desktop/mobile cases and typecheck PASS. Required existing receipt→QA→production chain and actual four-gate concurrency evidence reused with exact references; no repeated full regression.
- Fixed business signature HTTP401/403 response and prevented login renewal/replay on reauthentication errors. Independent review CRITICAL0, HIGH0 remaining after one verified fix pass. Existing LOW pagination/N+1 and MariaDB13/Flyway warning retained.
- V001..V024 validated, no migration/repair/reset/config staging. Accepted tasks, MES-009 readiness, MES-012 bounded IPC and MES-013 not-started preserved.

## MES-008/008A/010/011 human acceptance — 2026-10-04

- Explicit human approval: user replied “验收” to the completed four-task closeout report.
- MES-008-R2, MES-008A-R2, MES-010-R2 and MES-011-R2: `ACCEPTED`.
- Accepted scope and existing validation evidence: [final closeout](docs/acceptance/incoming-quality/FINAL-CLOSEOUT-2026-10-04.md).
- Administrative acceptance only: no product code, baseline, migration or database change; no repeat tests. Prior readiness entries remain historical evidence.
- MES-009 remains `READY FOR ACCEPTANCE`; MES-012 remains its approved IPC stage `IN PROGRESS`; MES-013 remains `NOT STARTED`. No next task started.

## MES-009 human acceptance — 2026-10-04

- Explicit human approval: user replied “mes009验收” after the task-status report confirming MES-009 was READY FOR ACCEPTANCE.
- MES-009-R2 (订单与正式批模型) is `ACCEPTED`. Scope and existing verification evidence: [MES-009 closeout](docs/acceptance/mes-009/CLOSEOUT-2026-10-04.md), including TC-BAT-001..004, TC-UI-002 and PRD-001..004. Earlier readiness entries remain historical evidence.
- Administrative acceptance only: no product code, frozen baseline, migration or database change; no repeat tests.
- MES-012 remains `IN PROGRESS` within its approved scope; MES-013 remains `NOT STARTED`. No next task started.

## MES-012/013 continued-development baseline review — 2026-10-04

User requests continued development of both tasks. MES-012 hard dependencies are ACCEPTED; MES-013 needs the remaining actual MES-012 producers. Scoped review found missing material-balance grammar/approval, production investigation/CAPA contracts and typed finished QA/PDF contracts, plus conflicting inherited balance state names. DESIGN CHANGE REQUIRED for these portions; existing accepted incoming and staged IPC contracts are preserved. Concrete findings and bounded approval proposal: [DCP-MES-012-013-CONTRACT-001-PROPOSED](docs/development/DCP-MES-012-013-CONTRACT-001-PROPOSED.md). No frozen redesign authorization inferred from the implementation request. No product-code change, migration, database operation or tests; statuses remain MES-012 IN PROGRESS / MES-013 NOT STARTED.

## MES-012/013 bounded contract completion approval — 2026-10-04

User explicitly approved “批准该范围，补齐正式契约后依次完成 MES-012、013”. DCP-MES-012-013-CONTRACT-001 is authorized within the proposal's listed balance, production quality/CAPA, finished release/PDF and consistency boundaries. The prior approval stop is superseded. [Execution ledger](docs/development/mes-012-013-approved-execution-plan.md) records contract-first implementation and validation. MES-012 stays IN PROGRESS; MES-013 starts after its real producer dependencies are available. Neither task is accepted by this design/development authorization.

## Reviewed MES-012/013 contract publication — 2026-10-05

v1.0.16 is the reviewed authoritative cumulative release under explicit DCP-MES-012-013-CONTRACT-001 approval. OpenAPI references resolve,35 added/typed operation entries checked, scoped independent HIGH findings closed, v1.0.15 remains unchanged. Native physical history24 successes/0 failures, highest24; planned V025 is not yet applied. Design publication does not imply runtime completion. MES-012 IN PROGRESS, MES-013 NOT STARTED until actual producer contracts are available.

## MES-012 readiness and authorized MES-013 handoff — 2026-10-05

MES-012-R2 READY FOR ACCEPTANCE after scoped plan/quantity/balance/production QC/OOS/CAPA implementation, native required producer/gate tests, direct IPC/incoming regressions, original UI targeted checks and independent CRITICAL0/HIGH0 review. No human acceptance inferred. Evidence: [MES-012 closeout](docs/acceptance/mes-012/CLOSEOUT-2026-10-05.md). Native V025 applied; latest read-only highest25/success25/fail0. User explicitly requested MES-012 then MES-013 and had approved the bounded completion contract; real MES-012 consumed producers are now physically available. MES-013 moves to IN PROGRESS for finished release/PDF/final lifecycle only. Its scoped Task Card and mandatory contract/reference sections were reviewed; append-only next physical allocation V026, no executed migration edits. Existing accepted tasks and local configuration exclusions remain preserved.

## MES-013 readiness — 2026-10-05

MES-013-R2 READY FOR ACCEPTANCE after finished QA/inventory/audit/signature/outbox atomicity, actual late OOS/original FAIL, explicit immutable supersession, locked source evidence, deterministic immutable eBR/PDF and original desktop/mobile UI were verified. Runtime RTM and15 distinct new native business methods (QA10/archive4/form-history1), schema2, focused backend4 units/platform signature13 regressions, frontend5 units/14 mock-browser cases/typecheck and CRITICAL0/HIGH0/MEDIUM0 scoped review: [closeout](docs/acceptance/mes-013/CLOSEOUT-2026-10-05.md). V026 append-only applied, latest26 validated/schema026; no failed migration or repair/reset. MES-012 public QC item projection corrected to its existing frozen contract and checked through actual downstream full-schema/native OOS chain; original source/signature facts unchanged. No design baseline change, no future task start, no automatic ACCEPTED. Remaining inherited LOW pagination/N+1 debt; native local configurations preserved/excluded.

## Human acceptance — MES-012 / MES-013 — 2026-10-05

The user explicitly replied “确认验收” to the joint MES-012 / MES-013 closeout. Both MES-012-R2 and MES-013-R2 are now ACCEPTED. Prior verification and readiness records remain historical evidence; no new test result or baseline change is implied. No subsequent MES task started.

## QA page presentation refinement — 2026-10-05

User supplied the selected screenshot and requested “调整 QA 批放行审核页面”. Scoped UI-QA-V / UI-REL-W presentation updated: compact checklist, batch/Gate banner, summary/actions, PDF history and derived evidence directory. No fields/API/permission/state/migration change; FINAL remains after QA decision. Typecheck and14 existing direct mock-browser cases PASS; final2 responsive/filter/original-fact cases PASS. [UI refinement review](docs/acceptance/mes-013/ui-refinement-2026-10-05/REVIEW.md). MES-012/013 ACCEPTED remains unchanged; no new human acceptance inferred, no next task started.


## Global UI V2 Phase 1 readiness — 2026-10-05

User explicitly approved global visual foundations only: Shell, typography, palette, spacing, cards and forms; preserve business layouts. Phase 1 is READY FOR ACCEPTANCE after frontend build/typecheck,20 existing targeted desktop/mobile browser cases and4 final new foundation cases PASS. Actual Vue pages were rendered in Chromium with API fixtures;11 final screenshots saved. Expanded sidebar228px/header56px, horizontal labels, mobile evidence drawer and collapsed icons verified. No business/database/API/permission/GxP contract change, no migration, no native database writes, no commit/push. [Phase 1 report](docs/acceptance/ui-v2-phase-1/REPORT.md). Inherited LOW bundle-size warning remains. Existing accepted MES statuses remain unchanged. Phase 2 NOT STARTED; STOP after this phase.


## Global UI V2 Phase 1 human acceptance — 2026-10-05

The user replied “确认” to the Phase 1 readiness report. Global UI V2 Phase 1 is ACCEPTED. Previous verification and screenshots remain evidence; no new test result, product-code change or commit/push is implied. Phase 2 NOT STARTED.


## Global UI V2 Phase 1.1 visual refinement readiness — 2026-10-05

User explicitly scoped FIX-01..04 only. Phase 1.1 READY FOR ACCEPTANCE: Material T1 query120px/natural responsive height, compact empty table; T2 form max1100px/mobile labels95px; Chinese recursive QA evidence labels retain original facts; FINAL PDF separated as post-decision archive per v1.0.16/DCP §5, server Gate count/progress unchanged. Typecheck,15 affected frontend tests,24 targeted Chromium cases and build PASS; current-run before/after screenshots saved and inspected. [Report](docs/acceptance/ui-v2-phase-1.1/REPORT.md). Business/API/DTO/state/permission/signature/database/baseline unchanged; no migration, no native business write, no commit/push. Scoped CRITICAL0/HIGH0; inherited LOW bundle warning. Phase 1 and existing MES ACCEPTED statuses unchanged. Phase 2 NOT STARTED; no new MES task. STOP.


## QA screenshot reference refinement readiness — 2026-10-05

User requested QA page follow the newly attached image. T6 presentation READY FOR ACCEPTANCE: compact header/checklist, summary/actions, parallel PDF history/evidence, Chinese status and category icons. Final PDF remains a labelled post-decision step inside PDF history under v1.0.16/DCP; no fictitious seventh Gate, product facts/photo or unauthorized Edit action. Typecheck/7 related frontend tests/build PASS;18 related desktop/mobile browser cases PASS,6 affected status/icon and4 final column-width cases PASS. [Latest QA visual report/screenshots](docs/acceptance/qa-reference-refinement/REPORT.md). Existing business script unchanged except visual imports; no backend/database/API/DTO/permission/state/signature/baseline change. Scoped CRITICAL0/HIGH0; inherited LOW bundle warning. Prior Phase1.1 evidence and user-owned configurations retained, no commit/push, no Phase2/new MES task or implicit acceptance.


## QA batch summary border refinement — 2026-10-05

User scoped batch-summary border only. Light1px outer/header border; remove inherited duplicate table rules, retain one light separator per row and none after final row. CSS only; no business/schema changes.2 targeted desktop/mobile Chromium cases PASS,14.2s; screenshots and note in docs/acceptance/qa-reference-refinement/REPORT.md. READY FOR ACCEPTANCE; prior accepted MES tasks unchanged, no Phase2/commit/push.


## Global UI V2 Phase 2A T1 pilot readiness — 2026-10-05

Authorized scope: Material master list, Production Batch list and Inspection Request list only. UI Template: T1 Query/List. READY FOR ACCEPTANCE: common compact Query Card, Result Card/count, external pagination, horizontal label/control pairing, single-column mobile filters and controlled table scrolling; primary identifiers emphasized. Batch state picker uses the existing MainBatch enum with localized QA labels. Other shared-resource lists retain their previous structures/action ordering. Existing query parameters, page sizes, routes, APIs, permissions and allowedActions preserved. Typecheck/build PASS;14 affected frontend unit tests PASS;6 targeted Playwright Chromium desktop/mobile cases PASS (15.7s), covering query/reset/pagination/state/empty results and zero console/page errors or page overflow.12 actual-app screenshots (API fixtures) saved outside the repository at C:/Users/Administrator/AppData/Local/Temp/mes-ui-v2-phase2a; populated desktop/mobile captures inspected. Scoped review: no CRITICAL/HIGH UI findings; inherited LOW bundle-size warning remains. No migration, database/business/API/DTO/state-machine/signature/baseline change. User-owned local database configuration changes preserved. Existing accepted MES statuses unchanged; no Phase2B, commit or push performed.


## v1.0.15 audit current-state remediation — 2026-10-05

ACCEPTED. Human acceptance: user explicitly replied “验收” on 2026-10-05 for HIGH01–08 and direct companion MEDIUM05. User explicitly approved DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 on 2026-10-05. HIGH01–08 and direct companion MEDIUM05 are implemented and verified. ProductionOrderId existing query is formally published; incoming server read allowedActions fails closed and respects actor/qualification/evidence/signature concerns; equipment mutex/current-read gates enforce exclusive active occupancy and preserve segments. Cumulative FINAL BASELINE COMPLETE v1.0.17 is authoritative after cross-document consistency PASS;109 v1.0.16 parent files remain unchanged. [Final report](docs/review/MES_V1.0.17_AUDIT_HIGH_CLOSURE_REPORT.md) and [original current-state confirmation](docs/review/MES_V1.0.16_CURRENT_STATE_CONFIRMATION.md). Targeted15 backend unit/11 native integration/16 frontend unit/26 distinct Chromium cases PASS; typecheck/build PASS; Flyway26 migrations/V026 validated with no migration or physical schema delta. No remaining scoped CRITICAL/HIGH; MEDIUM01–04 and inherited LOW bundle warning remain. Existing accepted MES statuses and Global UI V2/T1–T6 preserved. Unique retained native fixture/audit evidence preserved; local database configs untouched. No full regression, commit/push, Phase2B or next MES task. MEDIUM01–04 is not included in this acceptance.
