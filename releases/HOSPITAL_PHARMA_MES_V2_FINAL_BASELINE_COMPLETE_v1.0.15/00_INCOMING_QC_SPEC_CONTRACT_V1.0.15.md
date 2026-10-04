# Approved incoming QC specification contract

Approval: user approved `DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md`, including contract completion, on 2026-10-03. This document expands DG-01 and the QC portion of DG-08 only. It is an integration input for the next cumulative release; This appendix is normative within v1.0.15 after its cross-consistency review. No migration has been executed by this document. Legacy `md_material_version` / `md_material_quality_spec` remain historical evidence and are not the QC source of truth.

## Ownership and dependencies

`mes-qc` owns Specification, SpecificationVersion and SpecificationItem, under api/application/domain/infrastructure boundaries. It consumes existing IAM organization/user/permission context, master-data material/unit query contracts, and mes-audit signing/audit/idempotency. Ordinary persistence uses MyBatis-Plus; no new platform, database, workflow engine or material approval subsystem. The standard root identifies one material. Version is the regulated aggregate and its items belong exclusively to it. `mes-qms` consumes the independent `mes-qc` producer through the query contract below, never another module's mapper. `mes-boot` remains the only executable module.

## Exact database contract

All three tables use InnoDB, utf8mb4 and existing platform collation. All FKs use RESTRICT/NO ACTION, never cascading deletion. Each table has these exact common columns:

| Column | MariaDB type | Null/default | Rule |
|---|---|---|---|
| id | BIGINT | NOT NULL AUTO_INCREMENT | primary key |
| org_id | BIGINT | NOT NULL | authenticated organization; never supplied by client |
| created_by | BIGINT | NOT NULL | authenticated creator, immutable |
| created_at | DATETIME(3) | NOT NULL DEFAULT CURRENT_TIMESTAMP(3) | UTC |
| updated_by | BIGINT | NOT NULL | last mutator |
| updated_at | DATETIME(3) | NOT NULL DEFAULT CURRENT_TIMESTAMP(3) | UTC, application updates on mutation |
| version_no | BIGINT | NOT NULL DEFAULT 0 | optimistic record token; CHECK >= 0 |

Organization scoping and cross-reference ownership validation are mandatory in every application query/write; existing platform common metadata conventions do not add standalone org/user FKs for these common columns.

`qc_specification` adds:

| Column | Type/null | Constraint |
|---|---|---|
| material_id | BIGINT NOT NULL | FK `fk_qc_spec_material` -> md_material(id) |
| specification_code | VARCHAR(100) NOT NULL | trimmed nonblank |
| specification_name | VARCHAR(200) NOT NULL | trimmed nonblank |

PK id; `uk_qc_spec_code(org_id,specification_code)`; `ix_qc_spec_material(org_id,material_id,id)`. Code uniqueness follows database collation. There is no active-version pointer, root status or root edit endpoint. Code/name/material are immutable after root creation; new business revisions are versions under the same root. Multiple standards may reference the same material; selection is explicit and never silently chooses the newest.

`qc_specification_version` adds:

| Column | Type/null/default | Constraint |
|---|---|---|
| specification_id | BIGINT NOT NULL | FK `fk_qc_version_specification` -> qc_specification(id) |
| version_no_business | INT NOT NULL | CHECK > 0, distinct from version_no |
| status | VARCHAR(20) NOT NULL DEFAULT 'DRAFT' | CHECK IN ('DRAFT','APPROVED','RETIRED') |
| content_hash | CHAR(64) NULL | lowercase SHA-256 immutable definition digest, set on approval |
| approved_by | BIGINT NULL | FK `fk_qc_version_approver` -> sys_user(id) |
| approved_at | DATETIME(3) NULL | UTC |
| approval_signature_id | BIGINT NULL | FK `fk_qc_version_approval_signature` -> gxp_signature(id) |
| approval_reason | VARCHAR(1000) NULL | nonblank on approval |
| retired_by | BIGINT NULL | FK `fk_qc_version_retirer` -> sys_user(id) |
| retired_at | DATETIME(3) NULL | UTC |
| retirement_signature_id | BIGINT NULL | FK `fk_qc_version_retirement_signature` -> gxp_signature(id) |
| retirement_reason | VARCHAR(1000) NULL | nonblank on retirement |

`uk_qc_version_business(org_id,specification_id,version_no_business)` and `ix_qc_version_select(org_id,specification_id,status,id)`. `ck_qc_version_evidence`: DRAFT requires every approval/retirement field and content_hash NULL; APPROVED requires every approval field/content_hash NOT NULL and all retirement fields NULL; RETIRED requires all approval, retirement and content_hash fields NOT NULL. CHECKs enforce complete evidence tuples; service additionally validates signature identity, scope and cryptographic binding. Store reasons/signatures in this version row as technical GxP evidence, not another business aggregate.

`qc_specification_item` adds:

| Column | Type/null/default | Constraint |
|---|---|---|
| specification_version_id | BIGINT NOT NULL | FK `fk_qc_item_version` -> qc_specification_version(id) |
| item_code | VARCHAR(100) NOT NULL | trimmed nonblank |
| item_name | VARCHAR(200) NOT NULL | trimmed nonblank |
| required | TINYINT(1) NOT NULL | CHECK IN (0,1) |
| result_type | VARCHAR(20) NOT NULL | CHECK IN ('NUMERIC','TEXT') |
| lower_limit | DECIMAL(18,6) NULL | inclusive lower bound |
| upper_limit | DECIMAL(18,6) NULL | inclusive upper bound |
| unit_id | BIGINT NULL | FK `fk_qc_item_unit` -> md_unit(id) |
| text_acceptance_criteria | VARCHAR(1000) NULL | TEXT acceptance criteria |
| method_code | VARCHAR(100) NOT NULL | trimmed nonblank |
| method_version | VARCHAR(50) NOT NULL | trimmed nonblank |
| active | TINYINT(1) NOT NULL DEFAULT 1 | CHECK IN (0,1); draft removal tombstone |

`uk_qc_item_code(org_id,specification_version_id,item_code)` and `ix_qc_item_version(org_id,specification_version_id,active,id)`. `active` is technical retention metadata: PUT never physically deletes prior draft items. An omitted item is tombstoned, a reintroduced same code reactivates the same item ID; new codes obtain new IDs. Inactive items remain audited evidence, excluded from current DTO/definition/snapshot. Item codes cannot be renamed in place: omission plus new code is audited. Items cannot move between versions.

`ck_qc_item_shape`: NUMERIC requires at least one nonnull limit, unit_id NOT NULL, text_acceptance_criteria NULL, and lower_limit <= upper_limit when both exist; TEXT requires both limits and unit_id NULL and nonblank text_acceptance_criteria. No silent precision truncation: reject more than six fractional or twelve integer digits. No default PASS for missing measurement. Text criteria describe the controlled human assessment; they are not executable expressions or a newly invented matching language. Only PASS satisfies qualification; FAIL/INCONCLUSIVE/INVALID and absent result do not. Approved content and active flags are immutable, including inactive historical rows.

DDL must implement the named unique/index/FK/check constraints above in a new physical Flyway migration after the actual highest successful history version. No migration number is reserved here. Table creation precedes consumer FK installation and must not rewrite an executed migration. No historical rows are fabricated or backfilled to an arbitrary standard.

## Domain commands and HTTP contract

The companion `incoming-approved-qc-spec-openapi.json` is an OpenAPI 3.0.3 addition with `/api/v1` server prefix. API IDs are decimal strings, timestamps UTC date-time, decimal values exact strings. HTTP success envelope is existing `{code,message,data,traceId}`; failures use existing ApiError `{requestId,code,message,fieldErrors,allowedActions}`. `versionNo` is the record token; `versionNoBusiness` is the business version. Body reason is nonblank <=1000 characters. All writes require Idempotency-Key; writes to an existing version require quoted If-Match with body versionNo equal to the header. No generic updateStatus or DELETE endpoint.

| API/command | Permission | Input -> result |
|---|---|---|
| GET /quality/specifications | qms:specification:view | page (0, zero-based), size (20,max100), keyword, materialId, versionStatus -> page of SpecificationSummary |
| POST /quality/specifications / CreateSpecification | qms:specification:create | materialId, specificationCode, specificationName, reason -> 201 SpecificationDetail with empty versions |
| GET /quality/specifications/{id} | qms:specification:view | ID -> SpecificationDetail including version summaries |
| POST /quality/specifications/{id}/versions / CreateSpecificationVersion | qms:specification:create | versionNoBusiness, items[], reason -> 201 SpecificationVersionDetail in DRAFT |
| GET /quality/specification-versions/{id} | qms:specification:view | ID -> SpecificationVersionDetail; ETag quoted versionNo |
| PUT /quality/specification-versions/{id} / EditSpecificationVersion | qms:specification:edit | versionNo, items[], reason -> 200 full version detail; DRAFT only |
| POST /quality/specification-versions/{id}/approve / ApproveSpecificationVersion | qms:specification:approve + ebr:sign | versionNo, reason, reauthToken -> 200 full version detail |
| POST /quality/specification-versions/{id}/retire / RetireSpecificationVersion | qms:specification:retire + ebr:sign | versionNo, reason, reauthToken -> 200 full version detail |

Root creation checks material is enabled and same-org using current material eligibility semantics (legacy enabled master states remain supported). Version create/edit validates unit identity/organization/enabled state. Every version needs >=1 active item, duplicate item codes prohibited; no mandatory business requirement to mark at least one item required is invented. Explicit versionNoBusiness must be unique; there is no automatic clone endpoint or mandatory monotonic increment. DRAFT definition may be copied client-side from a historical read, but new item identities are allocated and audit records the supplied content.

List defaults order id DESC; keyword searches code/name; versionStatus filters roots having at least one version in that state. Version summaries order versionNoBusiness DESC,id DESC; items order itemCode ASC,id ASC. Paging total counts roots. Details include IDs and code/name display values, authors/times, versionNo and approval/retirement evidence; no reauthentication data is ever returned. Root GET is how a client selects an exact APPROVED version; no implicit latest resolver.

DRAFT -> APPROVED only by approve; APPROVED -> RETIRED only by retire. No reverse transition, draft retirement, approve-in-place edits, or automatic retirement when another revision is approved. Domain methods enforce these transitions even without HTTP. Approval excludes the version creator and every actor who authored its definition, using append-only audit evidence of create/edit (including edits later overwritten); checking only last editor is insufficient. Retirement requires its permission/signature but does not add an unapproved independent-retirer rule.

400 VALIDATION_ERROR/INVALID_IF_MATCH for malformed/unknown/read-only inputs, decimal/shape/empty-reason problems; 401 authentication/reauthentication errors use existing platform codes; 403 PERMISSION_DENIED or QC_SPEC_INDEPENDENT_APPROVER_REQUIRED; 404 QC_SPEC_NOT_FOUND for missing or other-org identities; 409 RECORD_CHANGED, QC_SPEC_CODE_EXISTS, QC_SPEC_VERSION_EXISTS, QC_SPEC_STATE_INVALID, QC_SPEC_NOT_SELECTABLE, QC_SPEC_MATERIAL_MISMATCH, QC_SPEC_SIGNATURE_INVALID, IDEMPOTENCY_KEY_REUSED or IDEMPOTENCY_IN_PROGRESS. All failures leave domain/evidence unchanged. Permission and organization checks precede replay disclosure. Exact successful replays return original response and never create duplicate version/item/signature/audit rows. Token is excluded from durable idempotency/audit payloads; digest includes action, target, expected version, definition or reason. Different actor has a different platform idempotency scope.

## Signature, audit, concurrency and immutable digest

Use existing SignatureApplicationService, provider registry, reauthentication binding, Rfc8785SignatureCanonicalizer, PlatformIdempotencyService and AuditApplicationService. Business commands perform domain write, signature persistence, command audit and idempotency completion in one database transaction. Reauthentication consumption remains the existing security boundary; a failed transaction may require a fresh token. Redact token/password in logs, DTO toString, request caches and audit.

Register two scoped providers: `QcSpecificationVersion` (approve) and `QcSpecificationRetirement` (retire), both with existing meaning APPROVE. This avoids introducing a RETIRE signature enum or overwriting approval evidence. objectId is version ID; signing recordVersion is immutable versionNoBusiness, following ProcessVersion convention; command If-Match remains mutable versionNo. Reauth binding is actor/session/org/objectType/objectId/APPROVE/versionNoBusiness. Providers recheck state, permission, author separation and definition validation. Generic record-sign must not bypass the business command: provider rejects if no same-transaction authorized approval/retirement intent is available; only command commits state/evidence together.

Immutable definition JSON fields, in camelCase: orgId, specificationId, specificationCode, specificationName, materialId, specificationVersionId, versionNoBusiness, items. Each item is `{specificationItemId,itemCode,itemName,required,resultType,lowerLimit,upperLimit,unitId,textAcceptanceCriteria,methodCode,methodVersion}`; nullable fields are explicit null, booleans JSON booleans, IDs decimal strings, decimals canonical six-place strings (e.g. "1.000000"), items sorted itemCode then ID. `content_hash` is lowercase SHA-256 of RFC8785 canonical definition JSON (not the signature envelope). Do not include mutable status, record token, master-data display names, retirement evidence or signature IDs in this definition.

Approval signable canonicalRecord is `{definition,action:"APPROVE",reason:approvalReason}`. Retirement is `{definition,action:"RETIRE",reason:retirementReason,approvalSignatureId}`. evidenceIds respectively `["QcSpecificationVersion:<id>"]` and `["QcSpecificationVersion:<id>","Signature:<approvalSignatureId>"]`. Existing canonicalizer adds schemaVersion/objectType/objectId/recordVersion/evidenceIds and hashes the envelope; this recordDigest differs intentionally from content_hash. During signing, the provider reads the validated reason from the transaction-scoped command intent; persist the reason, signature ID and complete target-state evidence tuple together in the final CAS update, so the DRAFT evidence CHECK is never temporarily violated. After commit the provider reconstructs the same immutable payload from persisted reasons for verification. The command intent is an application adapter over existing platform signing, not a second signing service or persistent table. Verification must not reject a historical signature solely because the current business state is RETIRED. Audit records status before/after separately.

CAS update version row WHERE org_id/id/version_no and expected state; increment exactly once for each edit/approve/retire; lock aggregate while updating its items. Updating item rows increments their common version_no and metadata but consumer lock is aggregate versionNo. Approve/create incoming request/retire serialize on the selected version row so a request cannot commit a new reference after retirement wins. Existing references remain valid after retirement. Audit actions: QC_SPEC_CREATED, QC_SPEC_VERSION_CREATED, QC_SPEC_VERSION_EDITED, QC_SPEC_VERSION_APPROVED, QC_SPEC_VERSION_RETIRED. Audit includes organization/actor/role/time/trace, target, before/after hash or content, reason, idempotency and signature reference where applicable. No notification consumer is required; any later cross-module notification uses domain events.

## Produced integration contract

`QcSpecificationQueryService.requireSelectable(long orgId,long materialId,long specificationVersionId): SpecificationSnapshot` runs inside the requesting transaction, serializes against retirement, requires same-org/material, APPROVED state, intact content_hash and VALID bound approval signature. Failure uses the explicit codes above. `getHistorical(long orgId,long specificationVersionId): SpecificationSnapshot` permits APPROVED/RETIRED, checks immutable content/signature evidence and never remaps to a newer revision. Neither method returns an HTTP envelope or persistence entity. Material display metadata is not an approval dependency.

SpecificationSnapshot fields: specificationId, specificationCode, specificationName, materialId, specificationVersionId, versionNoBusiness, contentHash, approvalSignatureId, approvedBy, approvedAt, immutable items[]. ItemSnapshot fields are exactly the immutable item fields listed above. Java IDs long, decimal values BigDecimal, collections immutable; API maps IDs/decimals to strings. No mutable status in the frozen snapshot.

Consumer qms inspection-request stores `qc_specification_version_id` FK -> qc_specification_version(id). Its quality-standard display is derived from root code/name + versionNoBusiness; no independently editable standard/version string. Every inspection-item freezes source `qc_specification_item_id` FK plus the full item snapshot; request freezes the definition contentHash. Item must belong to that same version/material/org. Optional items may exist, but report required-item completeness derives from *all required snapshot items*, never only tasks already created. Source links and snapshots are written atomically. New revision approval/retirement cannot mutate any prior request/item snapshot, result, method, report or release. A retired version cannot serve a new request, but existing requests may create/complete their later tasks using their frozen version. Signature invalidation/content mismatch is fail-closed with explicit integrity error; retirement alone does not invalidate approval signature. Never backfill existing requests without controlled provenance.

## UI, permissions and field mapping

Add menu `质量管理 / QC质量标准` at `/quality/specifications`, visible with qms:specification:view; do not grant permissions implicitly to roles. Register exact permissions view/create/edit/approve/retire in the existing IAM catalog, with ebr:sign independently enforced. Existing role assignment mechanism is used. Add only routes from the approved proposal: list, `/quality/specifications/create`, `/quality/specifications/:id`, `/quality/specification-versions/:id/edit`. Creation of a version is a controlled action on the root detail using existing edit/dialog patterns. No extra release list, sign page or finished-batch fields.

| Screen | Fields/columns and API mapping | Actions |
|---|---|---|
| List | 标准编码 specificationCode, 标准名称 specificationName, 物料 materialCode/materialName, 更新时间 updatedAt; filters keyword/materialId/versionStatus | 新建(create), 查看(view) |
| Create | 物料 materialId selector, 标准编码 specificationCode, 标准名称 specificationName, 原因 reason | 保存 POST root |
| Root detail | read-only root fields; version table 版本 versionNoBusiness, 状态 status, 编制人 createdBy, 批准人/时间 approvedBy/approvedAt, 记录版本 versionNo | 新建版本(create), 编辑(edit+DRAFT), 查看(view), 批准(approve+DRAFT), 退役(retire+APPROVED) |
| Version edit/detail | 业务版本 versionNoBusiness immutable after create; item rows 项目编码 itemCode, 项目名称 itemName, 必检 required, 结果类型 resultType, 下限 lowerLimit, 上限 upperLimit, 单位 unitId, 文本合格标准 textAcceptanceCriteria, 方法编码 methodCode, 方法版本 methodVersion | DRAFT save PUT; approved/retired fields read-only; remove draft row produces retained tombstone |
| Approval/retirement interaction | 版本/物料/全部项目摘要, action, reason, signer and reauthentication; returned signature/evidence | existing controlled confirmation/signature interaction; submit corresponding command |
| Incoming request | QC标准版本 qcSpecificationVersionId selector constrained by material and APPROVED; code/name/business revision displayed from returned detail | required selection before create; snapshot read-only thereafter |

All labels/control pairs remain horizontal at every viewport, with scroll/wrap of the pair as needed; no labels stacked above controls. Reuse existing list/edit/view/table/signature components, validation placement and status badges. NUMERIC shows bounds/unit and clears text criterion only on explicit type edit; TEXT shows criterion and clears numeric fields with the same explicit edit. Display missing numeric measurement as 未录入, never PASS/0. Draft edit conflicts preserve unsaved input and require reload/reconcile; UI hiding never substitutes server authorization. Existing request/task/result/report screens show frozen method/limits, not live master values. Prototype review must include list, root/create, version draft and signed read-only state with these exact horizontal mappings.

## Targeted tests and RTM additions

These IDs are contract requirements for implementation, not claims of passing tests. Add to detailed test catalog, coverage matrix and RTM in the cumulative release.

| Requirement | Test IDs | Required assertions |
|---|---|---|
| REQ-IN-QCS-001 identity/schema | TC-IN-QCS-001 | root org/code uniqueness, FK/orphan rejection, tenant isolation, business revision uniqueness; legacy material version untouched |
| REQ-IN-QCS-002 item definition | TC-IN-QCS-002 | NUMERIC one/two-sided bounds incl endpoints, lower>upper rejection, TEXT exclusivity, decimal precision, no missing-value PASS; draft tombstones retained |
| REQ-IN-QCS-003 lifecycle | TC-IN-QCS-003 | DRAFT edit, approve, retire; invalid transitions and all approved-content mutations rejected; no automatic old-version retirement |
| REQ-IN-QCS-004 GxP | TC-IN-QCS-004 | all author actors excluded from approval, both permissions required, reauth binding, altered reason/content rejection, original approval still verifies after retirement |
| REQ-IN-QCS-005 atomicity | TC-IN-QCS-005 | stale If-Match and body mismatch, concurrent edit/approve, concurrent new-request/retire, replay/same-key changed payload; injected audit/signature failure rolls back every business change |
| REQ-IN-QCS-006 immutable consumers | TC-IN-QCS-006 | wrong material/org/DRAFT/RETIRED new selection rejected, historical RETIRED continues, revision N+1 leaves N snapshots unchanged, all required items exposed to report |
| REQ-IN-QCS-007 UI/API | TC-IN-QCS-007 | all 8 operations/DTOs/envelopes/errors, menu+four routes, horizontal fields at desktop/mobile, state/permission actions, read-only signed details |

Producer readiness requires Java21 affected-module compilation/unit tests, targeted native MariaDB migrations/constraint/CAS/transaction tests with unique owned records, API contract checks, frontend typecheck and targeted UI tests. No full regression/default database reset. Review must reconcile this document with physical migration plan, OpenAPI fragment, incoming-item source/snapshot columns, state/permission/route catalogs, tests/RTM and task dependency rows before any authority switch. Required dependency edge: incoming request/item creation consumes the approved QC version producer; full acceptance must not mark that edge satisfied by placeholder strings or seed-only standards.


## Approved functional closure delta — v1.0.15

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.
