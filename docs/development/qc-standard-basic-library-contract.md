# QC quality standard basic library — contract

Status: **CURRENT** (DCP-QC-SPEC-BASIC-LIBRARY-001, approved 2026-11-02).
Supersedes the QC standard lifecycle, version and approval rules of
[incoming-approved-qc-spec-contract.md](incoming-approved-qc-spec-contract.md) and the QC
specification portion of [DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md](DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md).
Everything outside this document's bounded scope remains governed by FINAL BASELINE v1.0.23.

## 1. Runtime model

A QC quality standard is **one mutable current definition** kept in a basic information library.

- There is no draft/approved/retired lifecycle, no electronic signature, no content hash, no
  approver independence rule and no revision numbering of the standard itself.
- Header, applicability and inspection items are maintained directly through 增删改查.
- `status` is only `ACTIVE` / `INACTIVE`. `INACTIVE` keeps the record readable and referenced by
  already-frozen documents, but a new inspection document cannot select it.
- Every write is a serialized, row-locked, compare-and-swap update guarded by `version_no`
  (surfaced as the record token and as the HTTP `ETag`). A stale token fails closed with
  `RECORD_CHANGED`; nothing is silently overwritten.
- Deleting a library entry removes the standard and its scopes/items, and **keeps the maintenance
  trail** (`qc_specification_revision`) as evidence of the deletion.
- Mandatory GxP controls that stay in force: organization isolation, one transaction per command,
  idempotency keys, optimistic locking, audit events, and the rule that a downstream regulated
  document keeps its own frozen snapshot instead of re-reading a live standard.

## 2. Database contract

Physical migrations (append-only, no repair/rebuild):
`V070__qc_standard_basic_library.sql`, `V072__qc_standard_revision_after_nullable.sql`,
`V073__qc_standard_revision_trail_survives_deletion.sql`.

### 2.1 `qc_specification` — the standard itself

| Column | Type | Rule |
| --- | --- | --- |
| `id, org_id, created_by, created_at, updated_by, updated_at, version_no` | — | `ScopedEntity` audit + optimistic token |
| `specification_code` | VARCHAR(100) NOT NULL | unique per organization (`uk_qc_spec_code`) |
| `specification_name` | VARCHAR(200) NOT NULL | display name |
| `basis_type` | VARCHAR(32) NULL | 标准的依据 category, closed list (§5) |
| `basis_reference` | VARCHAR(300) NULL | 标准的依据 text: legal/registered/internal source of the limits |
| `effective_date` | DATE NULL | effective date of the standard |
| `remark` | VARCHAR(1000) NULL | free remark |
| `status` | VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' | `ck_qc_spec_status` (`ACTIVE`,`INACTIVE`) |
| `material_id` | BIGINT NULL | **legacy column only**; applicability moved to `qc_specification_scope` |

`ck_qc_spec_basis_pair` keeps `basis_type`/`basis_reference` both null or both present. The API
requires both for every create/edit; the columns stay nullable so the V070 backfill of historical
rows could run before the new mandatory rule applied to them.

The V014/V056/V057 identity triggers (`qc_specification_no_update`, `qc_specification_no_delete`,
`qc_specification_org_insert`) and the single-subject columns `subject_type`/`product_id` are
removed by V070 — a standard is now mutable and its subjects live in the scope table.

### 2.2 `qc_specification_scope` — 适用范围 (many-to-many)

`(id, org_id, created_by, created_at, updated_by, updated_at, version_no, specification_id, subject_type, subject_id)`

- `ck_qc_scope_subject`: `subject_type` in (`MATERIAL`,`PRODUCT`).
- `uk_qc_scope_subject (org_id, specification_id, subject_type, subject_id)`: the same subject cannot
  be attached twice.
- `ix_qc_scope_subject_lookup (org_id, subject_type, subject_id)` serves the list filters.
- `fk_qc_scope_specification` → `qc_specification(id)`.

One standard may carry many materials **and** many products at the same time. V070 backfills the
pre-existing single material/product subject of every historical standard into this table.

### 2.3 `qc_specification_item` — 检验项目

`(id, org_id, specification_id, item_code, item_name, required, result_type, lower_limit, upper_limit, unit_id, text_acceptance_criteria, method_code, method_version)`

- Items belong to the **standard** (`fk_qc_item_specification`), not to a version.
- `uk_qc_item_code (org_id, specification_id, item_code)`; item codes are compared case-insensitively
  by the domain rule and are reused in place when the code is unchanged.
- Shape rules are unchanged from V014: `NUMERIC` requires at least one bound plus a unit and forbids
  a text criterion; `TEXT` forbids numeric fields and requires a text criterion; bounds are
  inclusive, at most 12 integer / 6 fractional digits.

### 2.4 `qc_specification_revision` — 维护记录 (evidence trail)

`(id, org_id, created_by, created_at, updated_by, updated_at, version_no, specification_id, revision_no, revision_type, revision_reason, before_json, after_json)`

- `revision_type` in (`CREATE`,`UPDATE`,`DELETE`); `revision_reason` is mandatory; `revision_no` is a
  per-standard ascending counter (`uk_qc_revision_no`).
- `before_json`/`after_json` are JSON-validated; `after_json` is nullable because a `DELETE`
  revision has no resulting state (V072).
- V073 drops `fk_qc_revision_specification`: the trail must survive deletion of the standard it
  describes.

### 2.5 Downstream references (frozen snapshot, standard id)

V070 translates the four existing reference columns in place so they hold the **standard id**
(`qc_specification.id`) instead of the deleted `qc_specification_version.id`, and re-points their
foreign keys at `qc_specification(id)`:

| Table | Column (name retained) | New foreign key |
| --- | --- | --- |
| `qms_inspection_request` | `qc_specification_version_id` | `fk_inspection_request_qc_standard` |
| `qms_production_plan` | `qc_specification_version_id` | `fk_plan_qc_standard` |
| `qms_sample` | `qc_specification_version_id` | `fk_sample_qc_standard` |
| `qms_finished_inspection_request` | `qc_specification_version_id` | `fk_finished_request_qc_standard` |

The column names are deliberately **not** renamed: they are part of frozen OpenAPI/schema contracts
of the incoming, production and finished modules, and renaming them would be an unauthorized change
outside this DCP. Their meaning is now "id of the QC standard this document froze". The
`preserve_qms_finished_inspection_request` immutability trigger is unchanged.

`qms_production_plan` rows that were `APPROVED` while carrying no specification snapshot (a
pre-existing defect) are reset to `DRAFT` by V070 so they can be re-planned against the library.

## 3. Freeze semantics for downstream documents

1. When an incoming request, production quality plan, finished inspection request or sample binds a
   standard, the caller resolves it through `QcSpecificationQueryService`:
   - `requireSelectable(orgId, materialId, specificationId)` — material flow, requires `ACTIVE` and a
     `MATERIAL` scope for that material.
   - `requireProductSelectable(orgId, productId, specificationId)` — product flow, requires `ACTIVE`
     and a `PRODUCT` scope for that product.
   - `read(orgId, specificationId)` — read-only re-read of a document's own frozen reference (does not
     require `ACTIVE`); it exists so a document can verify its own snapshot, never to re-decide.
2. The resolver returns `QcStandardSnapshot(schemaVersion=3, specificationId, specificationCode,
   specificationName, basisType, basisReference, effectiveDate, materials, products, items)`, where
   `materials`/`products` are string lists of subject ids and `items` are `SpecificationItemSnapshot`.
3. The document stores the standard id plus a digest of that snapshot
   (`specification_content_hash`). Editing or deactivating the library afterwards cannot change a
   frozen document: `snapshotDigest(...)` is recomputed from the stored snapshot and compared with the
   stored digest, failing closed with `SPECIFICATION_DIGEST_MISMATCH` on divergence.
4. Traceability reports the frozen reference as node type `QC_SPECIFICATION` (labels 质量标准 /
   质量标准依据) with edge type `FROZEN_STANDARD`.

## 4. API contract

Base path `/api/v1/quality`. All responses use `ApiResponse<T>` (`data`, `traceId`).

| # | Method & path | Permission | Headers | Success |
| --- | --- | --- | --- | --- |
| 1 | `GET /specifications` | `qms:specification:view` | — | 200 page |
| 2 | `POST /specifications` | `qms:specification:create` | `Idempotency-Key` | 201 + `ETag` |
| 3 | `GET /specifications/{id}` | `qms:specification:view` | — | 200 + `ETag` |
| 4 | `PUT /specifications/{id}` | `qms:specification:edit` | `If-Match`, `Idempotency-Key` | 200 + `ETag` |
| 5 | `PUT /specifications/{id}/scope` | `qms:specification:edit` | `If-Match`, `Idempotency-Key` | 200 + `ETag` |
| 6 | `PUT /specifications/{id}/items` | `qms:specification:edit` | `If-Match`, `Idempotency-Key` | 200 + `ETag` |
| 7 | `DELETE /specifications/{id}` | `qms:specification:delete` | `If-Match`, `Idempotency-Key` | 200 |

There is no approve, retire, submit or version endpoint; those paths are not mapped at all.
`ETag` and `If-Match` use the quoted record token `"<versionNo>"`.

List query parameters: `page` (default 0), `size` (default 20, ≤100), `keyword` (code / name / basis
reference), `basisType`, `materialId`, `productId`, `status`. Filters are combined with AND.

Command bodies (`unknown` JSON fields are rejected with `VALIDATION_ERROR`, so no client can smuggle a
silently ignored approval or version field):

- Create: `specificationCode`, `specificationName`, `basisType`, `basisReference`, `effectiveDate`,
  `remark`, `materialIds[]`, `productIds[]`, `items[]`, `reason`.
- Edit: `recordVersion`, `specificationCode`, `specificationName`, `basisType`, `basisReference`,
  `effectiveDate`, `remark`, `status`, `reason`.
- Replace scope: `recordVersion`, `materialIds[]`, `productIds[]`, `reason`.
- Replace items: `recordVersion`, `items[]`, `reason`.
- Remove: `recordVersion`, `reason`.
- Item: `itemCode`, `itemName`, `required`, `resultType`, `lowerLimit`, `upperLimit`, `unitId`,
  `textAcceptanceCriteria`, `methodCode`, `methodVersion`.

Error codes: `VALIDATION_ERROR`, `INVALID_IF_MATCH`, `QC_SPEC_CODE_EXISTS`, `RECORD_CHANGED`,
`QC_SPEC_NOT_SELECTABLE`, `QC_SPEC_MATERIAL_MISMATCH`, `QC_SPEC_PRODUCT_MISMATCH`,
`QC_NOT_FOUND` (404), `PERMISSION_DENIED`, `IDEMPOTENCY_CONFLICT`.

## 5. Permissions, dictionary, menu, audit

- Permissions: `qms:specification:view`, `qms:specification:create`, `qms:specification:edit`,
  `qms:specification:delete`. `qms:specification:approve` and `qms:specification:retire` are deleted
  together with their role grants and menu bindings; `ebr:sign` is no longer required by any QC
  standard endpoint.
- Menu: `QC质量标准`, route `/quality/specifications`, permission `qms:specification:view` (unchanged).
- Dictionary `QC_STANDARD_BASIS` (`SYSTEM`, flat) with `CHINESE_PHARMACOPOEIA`《中国药典》、
  `MINISTRY_STANDARD`局（部）颁标准、`DRUG_REGISTRATION_STANDARD`药品注册标准、
  `NATIONAL_DRUG_STANDARD`国家药品标准、`ENTERPRISE_INTERNAL`企业内控标准、
  `CUSTOMER_SPECIFICATION`客户标准、`OTHER`其他依据.
- Audit actions on `QcSpecification`: `QC_SPEC_CREATED`, `QC_SPEC_UPDATED`, `QC_SPEC_SCOPE_CHANGED`,
  `QC_SPEC_ITEMS_CHANGED`, `QC_SPEC_DELETED`. They record who changed what for traceability; they are
  not approval evidence and carry no signature.

## 6. UI contract

| Route | Permission | Template | Content |
| --- | --- | --- | --- |
| `/quality/specifications` | `qms:specification:view` | T1 | query: 编码/名称, 标准的依据, 适用物料, 适用产品, 状态 |
| `/quality/specifications/create` | `qms:specification:create` | T2 | create form |
| `/quality/specifications/:id` | `qms:specification:view` | T2/T3 | edit/detail, 适用范围, 检验项目, 维护记录, 删除 |

`/quality/specification-versions/:id/edit` and its view are removed. Every query label stays on the
same horizontal line as its control; 标准的依据 uses `DictionarySelect` (`QC_STANDARD_BASIS`),
applicability uses the multi-select `QcScopePicker`, and subject references use the shared
`ProcessLookup`/`ReadReference` components — no hard-coded drop-downs.

## 7. Verification evidence

- `mvn -o -DskipTests compile` and `mvn -o test-compile` — BUILD SUCCESS.
- `mvn -o test -Dtest=QcSpecificationRulesTest` — 7/7.
- `mvn -o test -Dtest=QcSpecificationIT` — 7/7; `QcFinalContractIT` — 13/13.
- Incoming/finished/trace regression: `IncomingClosureIT` 9/9, `IncomingQualityIT` 6/6,
  `IncomingCrossroleBatchAIT` 6/6, `IncomingCrossroleBatchBIT` 2/2, `IncomingFinalContractIT` 1/1,
  `IncomingQualityAcceptanceIT` 3/3, `IncomingCrossroleNegativeIT` 7/7,
  `IncomingCrossrolePreflightIT` 3/3, `IncomingProductionIT` 13/13.
- Frontend: `npx vue-tsc --noEmit` exit 0; `QcSpecificationModel.test.ts` 6/6;
  `IncomingReferencePicker.test.ts` 7/7; production/trace/master/api suites 25/25.
- DEV database after V070–V073: 13 standards, 13 scopes, 13 items, all `basis_type` filled and all
  `ACTIVE`; `qc_specification_version` dropped; the four downstream reference columns hold standard
  ids and their foreign keys point at `qc_specification(id)`.

`MES_TASKS.md` is the only runtime status record.
