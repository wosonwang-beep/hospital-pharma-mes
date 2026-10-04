# 系统架构设计说明书

模块化单体：iam/masterdata/process/production/wms/execution/qms/release/audit/integration/reporting。业务写入与审计同事务；集成使用outbox；受控定义发布后不可原地修改；写接口幂等+乐观锁。

# V1.0.10 Controlled Platform Policies — DCP-MES-001-R2-001
The following platform policies are normative and centrally owned by MES-001:

- `IntegrationRetryPolicy` is the only source of the automatic retry limit and delay schedule. V1.0.10 freezes maximum attempts at **8** and delays after failures 1–7 at **PT1M, PT5M, PT15M, PT1H, PT4H, PT12H, PT24H**. Business services, controllers, connectors and downstream modules must not hard-code or override these values.
- `SignatureCanonicalizer` owns RFC 8785 JCS canonicalization and lowercase SHA-256 digest production. Business modules contribute data only through registered `SignableObjectProvider` contracts.
- `PlatformIdempotencyService` owns generic HTTP idempotency using `platform_idempotency_record`. A downstream module may add a specialized idempotency store only after an approved business-specific design change.
- `EnterpriseValidationProperties` binds RPO, RTO, GxP retention, site and approved time-source configuration. It supplies validation gates, not invented enterprise values.

Changes to any of these policies require controlled configuration approval or a new Design Change Proposal. DCP-MES-001-R2-001 does not authorize code or migration execution.

## DCP-MES-002-R2-001 — Incoming Quality Architecture

- WMS owns receipt, physical lot establishment, inventory fact movements, reservation and issue.
- QMS owns inspection request intake, sampling, samples, inspection execution, result revisions and inspection reports.
- QA owns human release decisions; the approved inspection-exempt rule may create a system decision through the same ReleaseDecision aggregate.
- `qms_release_decision` is the sole release Source of Truth for both `INCOMING_MATERIAL` and `FINISHED_PRODUCT`.
- `wms_inventory_ledger`, `qms_test_result_revision`, and `mes_material_charge` remain the sole quantity-change, result-history, and actual-charge facts respectively.
- `MaterialEligibilityService` is a shared domain policy/query boundary. Reservation, Issue, Weighing, and Charge call it; consumers may not duplicate eligibility rules.
- The quality, inventory, and controlled-record state dimensions are separate and may change only through their owning commands.

No physical migration or business-code implementation is authorized by this design release. Logical migration grouping in Section 15 must be converted to new physical Flyway versions only when the owning implementation task begins.




## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.


## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.
