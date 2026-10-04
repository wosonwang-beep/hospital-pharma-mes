# 测试与验收规格

覆盖正常、异常、权限、状态机、幂等、乐观锁、事务回滚、审计、电子签名、版本冻结、库存一致性、物料平衡、Release Gate。

# V1.0.9 Enterprise Validation Gate — DCP-MES-001-R2-001
DG-008 is an OPEN ENTERPRISE VALIDATION ITEM. It is not a MES-001 implementation blocker. It blocks production validation and go-live until enterprise-approved RPO, RTO, GxP retention, site, time source, restore-drill and referenced SOP/validation evidence are supplied. V1.0.9 supplies required no-default configuration bindings and validation tests only; it contains no invented value and no physical purge behavior.

## DCP-MES-002-R2-001 Acceptance Delta

Acceptance must prove versioned policy/snapshot history; required and exempt receipt branches; qualification failures; package-level sampling lineage; append-only result correction; approved report/result linkage; `QC_PASSED + BLOCKED`; QA release/reject; immutable/superseding decisions; separated statuses; and the shared Reservation/Issue/Weighing/Charge eligibility gate.

Inspection-exempt acceptance explicitly proves that no request, sampling, sample, inspection, result, or report row is fabricated and that `RELEASED + AVAILABLE` occurs only in the same transaction as the `INSPECTION_EXEMPT`/`SYSTEM_RULE` ReleaseDecision and audit evidence.




## DCP-MES-003-R2-001 approved delta

MES-003 readiness requires targeted domain/API/MariaDB/frontend/browser evidence and zero CRITICAL/HIGH review findings for DCP-MES-003-R2-001.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.


## DCP-MATERIAL-NAMES-UI-001 current correction
See `00_DESIGN_CHANGE_DCP-MATERIAL-NAMES-UI-001_APPROVED.md`. The three alternate material-name fields are retired from current API/UI/search/consumer snapshots; legacy physical values and audits are preserved. Labels and controls remain side by side on all viewport sizes. This supersedes inherited inconsistent descriptions within that boundary.


## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.
