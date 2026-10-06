# GMP数据完整性与审计

## Current approved WMS successor delta — v1.0.22

[DCP-WMS-REQUEST-INVENTORY-RETURN-001](00_WMS_REQUEST_READ_CONTRACT_V1.0.22.md), section 6, supersedes inherited clauses only for formal material demand, request-linked issue, lot-level inventory read, return history and five WMS entry routes. Current approved scope and exact fields/states/rights/actions are in that normative section. Prior accepted tasks and QA/MES signature/stock rules remain unchanged. Do not infer runtime acceptance from publication.


禁止硬删除GxP记录；禁止覆盖历史值；更正追加revision并保留前后值、原因、人员、时间；必要时原复核失效并重签。签名绑定身份、含义、时间、对象和record_digest。

# V1.0.22 Audit and eSignature Controls — DCP-MES-001-R2-001
AuditEvent records the authenticated/registered technical actor, authorizing role snapshot, stable action/object, canonical old/new digests, reason, UTC millisecond timestamp, transaction ID, request ID, source and optional idempotency key. `created_by=actor_id` and `created_at=occurred_at`. Background jobs use a registered technical user. Credentials, tokens, payload bodies and secrets never enter AuditEvent or ordinary logs. Runtime database rights and MariaDB guards make the table append-only.

Electronic signature is not an image. It uses a server-built RFC 8785 canonical envelope and lowercase SHA-256 digest. Reauthentication is exactly five minutes, single-use and bound to user/session/org/object/meaning/version. Consumption occurs on first sign attempt and is not restored on rollback. `auth_context_json` contains only schema version, method, reauthentication timestamp, SHA-256 session ID hash and request ID.

Invalidation is a controlled `VALID → INVALIDATED` mutation of status/invalidation/update metadata/version only. Re-sign inserts a new row and links the immediately superseded invalidated signature. Existing signature digests are never recalculated in place.

## DCP-MES-002-R2-001 — Incoming Material GxP Controls

All six records use shared AuditEvent, ElectronicSignature, Attachment, Comment, Revision, and Lineage infrastructure. Audit covers create, submit, modify, review, approve, reject, cancel, resample, retest, result revision, QA release/reject, inspection-exempt evaluation, freeze/unfreeze, and supersession with who/when/what/before/after/reason/request/transaction identity.

Signature-required meanings include sampling completion, result confirmation/correction, inspection review, report approval, user QA release/reject, and approved disposition. A signature binds business type/id, record version, canonical digest, signer, time, and meaning. `signed=true` is never sufficient.

`SYSTEM_RULE` inspection-exempt decisions have no fictitious human signature or actor. They bind rule/version and qualification snapshots, technical actor, audit event, transaction, and deterministic decision digest. Human decisions use `USER_QA` and the applicable electronic signature. Existing decisions and result revisions are immutable; corrections supersede or append.




## DCP-MES-003-R2-001 approved delta

MES-003 mutation evidence, reason, version, isolation and idempotency requirements are defined by DCP-MES-003-R2-001. No additional signature-required business point is introduced.


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


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.


## Approved incoming full-chain completion in v1.0.22

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.22.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.22 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.22.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.22.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.22.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.22.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.22.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.22.md)


## Authorized v1.0.22 material weighing policy producer

The approved full incoming acceptance scope includes this missing producer. [00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.22.md](00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.22.md) governs deployment configuration, immutable production snapshot, consumers and required tests. It does not restore retired material master fields or change material APIs. It supersedes earlier references to nullable legacy material weighing fields. No new table, permission, route or status.


## v1.0.22 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.22.md](00_INCOMING_TRACE_STANDARD_V1.0.22.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved functional closure delta — v1.0.22

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.22.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.22.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.


## Verified functional closure recovery — 2026-10-04

The approved bounded delta and one-time V024 exception are recorded in [functional closure §§9–10](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.22.md). V024 is successful; V001–V023 remain immutable. Trace inventory decisions use INVENTORY_DECISION / INVENTORY_CONTROL with original SIGNED_EVIDENCE, decimal signing values are exact strings, and mandatory missing headers return 400. Scope-level native verification PASS does not waive other formal task RTM or authorize full MES-012/013.


## Approved MES-012/013 completion — v1.0.22

Read [00_MES_012_013_COMPLETION_CONTRACT_V1.0.22.md](00_MES_012_013_COMPLETION_CONTRACT_V1.0.22.md), DCP-MES-012-013-CONTRACT-001, approved 2026-10-04. It supersedes missing/generic production balance, investigation/CAPA, finished QA and PDF contracts only. BalanceResult remains immutable PASS/FAIL; approval belongs to the separate investigation. Original incoming and IPC contracts stay unchanged. No runtime readiness follows from publication.


## Approved audit HIGH delta — v1.0.22

[00_AUDIT_HIGH_CONTRACT_V1.0.22.md](00_AUDIT_HIGH_CONTRACT_V1.0.22.md) governs only productionOrderId query publication, incoming read-only allowedActions and exclusive active equipment occupancy. All other inherited contracts remain unchanged. No schema/migration, new permission/route/state or signature/release-rule change.


## Approved storage/source maintenance — v1.0.22

[00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.22.md](00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.22.md) governs the bounded storageCondition, relationship manufacturerName, receipt-source snapshot, UI field cleanup and V028 delta. Other inherited contracts unchanged.


## Approved finished-goods delta — v1.0.22

[00_FINISHED_GOODS_CONTRACT_V1.0.22.md](00_FINISHED_GOODS_CONTRACT_V1.0.22.md) controls the approved finished chain. It supersedes inherited wording only for new OUTPUT stock effects (identity/quantity only), independent warehouse receipt, post-completion finished request/sampling/report, two additional QA gates, signed shipment and finished trace. Existing incoming quality, historical OUTPUT ledgers, immutable original results/OOS/retest, independent signed QA and post-decision FINAL PDF remain authoritative. V030/V031 append-only. UI V2/T1–T6 unchanged. Inherited embedded manifests/reviews attest prior deltas; current finished-goods manifest governs cumulative bytes. Acceptance/readiness remains in MES_TASKS.md.


## Approved optional inbound inspection draft — v1.0.22

[00_FINISHED_INBOUND_DRAFT_CONTRACT_V1.0.22.md](00_FINISHED_INBOUND_DRAFT_CONTRACT_V1.0.22.md) is the controlling bounded supplement: optional atomic linked inspection DRAFT during inbound creation; warehouse confirmation remains mandatory before execution. Existing tables/states/signatures/permissions/routes and 201 response unchanged. Only FinishedInboundCreate gains conditional optional command fields. No migration (native highest31); no physical stock from drafts. Existing UI T2/T3 only, no additional finished-image layout/menu scope. Test/RTM FD-01..06 and synchronous domain-event integration below are cumulative. Inherited reviews/manifests attest prior deltas, not current cumulative bytes. Current draft manifest/consistency review and SHA256SUMS govern this release. Readiness and human acceptance are only in MES_TASKS.md.


## Approved read closure — v1.0.22

[00_TRACE_FINISHED_INVENTORY_CONTRACT_V1.0.22.md](00_TRACE_FINISHED_INVENTORY_CONTRACT_V1.0.22.md) controls only charge-based forward/WMS/source trace and finished inventory projection/menu. Existing DB/state/signature/permission/stock/QA/weighing rules unchanged; migration NONE. Copied older manifests/reviews retain prior-delta attestation only; current read-closure manifest and SHA256SUMS govern this successor. TI-01..05 cover Test/RTM. Status is only MES_TASKS.md.
