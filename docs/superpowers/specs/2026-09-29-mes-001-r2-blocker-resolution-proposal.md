# DESIGN CHANGE PROPOSAL — MES-001-R2 BLOCKER RESOLUTION

**Proposal ID:** DCP-MES-001-R2-001  
**Date:** 2026-09-29  
**Status:** PROPOSED — NOT APPROVED  
**Task state:** `MES-001-R2 = STOPPED — DESIGN CHANGE REQUIRED`  
**Authority used:** FINAL BASELINE COMPLETE v2, current repository state, and `docs/superpowers/plans/2026-09-29-mes-001-r2-implementation.md`  
**Change control:** This proposal does not modify the FINAL BASELINE, business code, database migrations, V001/V002, or MES-002. Approval authorizes preparation of a controlled baseline addendum and a revised MES-001 implementation plan; it does not authorize coding.

---

## 1. Purpose and Decision Boundary

This proposal resolves the nine design blockers recorded for MES-001-R2 while preserving these non-negotiable constraints:

1. Existing physical Flyway migrations are immutable.
2. Logical construction order and physical Flyway version are separate concepts.
3. No Codex implementation decision may fill an absent business/API/database rule.
4. MES-001 owns platform contracts; MES-007 and MES-013 own their business-specific signature bindings.
5. UI V1.1 business routes outrank prototype route labels.
6. Enterprise RPO/RTO and retention values are not fabricated.
7. No MES-002–013 business capability is implemented in MES-001.

The proposal uses five blocker classifications:

- **DESIGN DEFECT:** two frozen artifacts contradict or misassign the same contract.
- **MISSING SPECIFICATION:** the frozen artifacts require behavior but do not define it sufficiently for deterministic implementation.
- **EXISTING REPOSITORY CONSTRAINT:** immutable repository history or current physical structure constrains the repair.
- **ENTERPRISE DECISION REQUIRED:** the correct value depends on approved SOP, regulatory market, infrastructure or business continuity policy.
- **NON-BLOCKING IMPLEMENTATION DETAIL:** a technical choice can be fixed by this proposal without changing business meaning or delaying construction.

---

## 2. Options Considered

### Option 1 — Controlled baseline addendum plus physical migration mapping — RECOMMENDED

- Preserve physical V001/V002.
- Treat Section 15 `V001`, `V002`, `V007A`, etc. as **Logical Migration Groups**.
- Maintain a repository-specific mapping from each logical group to one or more monotonically increasing physical Flyway migrations.
- Publish an approved FINAL BASELINE errata/addendum containing the contracts frozen by this proposal.
- Revise the MES-001 implementation plan only after approval.

**Benefits:** one authoritative design, no migration-history rewrite, downstream contracts become deterministic.  
**Cost:** requires controlled changes to several frozen artifacts after approval.

### Option 2 — Repository-only compatibility layer — REJECTED

- Add V003+ and implementation conventions without updating the FINAL BASELINE.

**Reason rejected:** it creates two truths. Future MES tasks would not know whether to follow the baseline or repository conventions, and Codex would again be forced to infer contracts.

### Option 3 — Rebuild or renumber migration history — PROHIBITED

- Rename/rewrite V001/V002 or rebuild the repository schema to match Section 15 numbering.

**Reason rejected:** violates append-only Flyway history, the user instruction, repository AGENTS rule 11, and regulated traceability expectations.

---

## 3. Cross-Cutting Decisions Proposed for Freeze

### 3.1 Logical Migration Group versus Physical Flyway Version

- `LG-xxx` is the design dependency identifier. Existing Section 15 labels are interpreted as logical groups after approval.
- `Vxxx__name.sql` is a repository-local, strictly increasing physical Flyway migration.
- A logical group may map to multiple physical migrations.
- A physical migration is never renamed, reordered or edited after execution/shared use.
- `Depends On` in Section 15 describes logical capability dependencies, not a promise that the number equals the physical Flyway version.
- A version-controlled migration ledger must record logical group, task, physical versions, checksum, schema prerequisite and acceptance evidence.

### 3.2 JSON and identifier conventions

- Database numeric identifiers remain `BIGINT`.
- Public JSON serializes all `BIGINT` identifiers as decimal strings to avoid JavaScript precision loss.
- Timestamps are UTC ISO-8601 strings in API responses and `DATETIME(3)` in MariaDB.
- JDBC/database sessions used by the application and Flyway must set the MariaDB session time zone to UTC (`+00:00`); `DATETIME(3)` values are interpreted only under that contract.
- `If-Match` uses a quoted decimal version, for example `"3"`; mismatch returns HTTP 409 with code `VERSION_CONFLICT`.
- JSON payloads use UTF-8.

### 3.3 Canonical JSON and digest convention

- Canonical JSON uses RFC 8785 JSON Canonicalization Scheme (JCS).
- Digests use SHA-256 over the exact UTF-8 bytes of the canonical JSON.
- Digest API/database representation is lowercase hexadecimal, exactly 64 characters.
- Domain payloads must represent decimal values as JSON strings in their normalized domain format when scale is business-significant; binary floating-point values are forbidden in signable payloads.
- Timestamps in signable payloads are UTC with millisecond precision and a `Z` suffix.
- Arrays preserve business order except `evidenceIds`, which is sorted lexicographically before canonicalization.

---

## 4. DG-001 / A — Migration Baseline Resolution

### 4.1 Classification

- Primary: **EXISTING REPOSITORY CONSTRAINT**
- Secondary: **DESIGN DEFECT**

### 4.2 Current conflict

- Section 15 calls MES-001 platform migration `V001` and MES-002 IAM migration `V002`.
- Physical repository V001 already creates `sys_foundation_probe`; physical V002 creates IAM/security tables.
- V001/V002 are immutable.
- The frozen platform tables reference user identity, which cannot exist before logical IAM if physical FKs are required.

### 4.3 Frozen documents involved

- Section 15 DOCX, MD, migration dependency matrix and task dependency matrix.
- `05_DATABASE_DESIGN_V1.0_FROZEN.md`.
- `tasks/MES-001-R2.md`, `tasks/MES-002-R2.md`.
- Repository and frozen `AGENTS.md` migration rules.

### 4.4 Requirements, database, API, tasks and tests involved

- Requirements: AUD-001, SIG-001, INT-001, NFR-001.
- Existing tables: `sys_foundation_probe`, `sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `sys_security_event`.
- New platform tables: `gxp_audit_event`, `gxp_signature`, `integration_inbox`, `integration_outbox`, plus the proposed `platform_idempotency_record` required by the global `Idempotency-Key` contract.
- APIs: all MES-001 writes, particularly retry, reauthentication and signing.
- Tasks: MES-001 produces the platform contract; MES-002–013 consume it.
- Tests: TC-AUD-001, TC-SIG-001, TC-INT-001, TC-INT-002; migration upgrade/clean-install tests.

### 4.5 Existing Migration Baseline — KEEP/ALTER

| Physical migration/table | Decision for MES-001 | Reason |
|---|---|---|
| `V001__foundation_probe.sql` / `sys_foundation_probe` | KEEP, no ALTER | Immutable repository history; unrelated probe |
| `V002__iam_core.sql` | KEEP, no edit | Immutable repository history |
| `sys_user` | KEEP, no ALTER in MES-001 | Supplies physical identity IDs; frozen IAM reconciliation belongs to MES-002 |
| `sys_role` | KEEP, no ALTER in MES-001 | MES-002-owned schema |
| `sys_permission` | KEEP, no ALTER in MES-001 | Existing shape can accept new permission rows |
| `sys_user_role` | KEEP, no ALTER | MES-002-owned relation |
| `sys_role_permission` | KEEP, no ALTER | Can receive SYSTEM_ADMIN mappings |
| `sys_security_event` | KEEP, no ALTER | IAM operational security log; not a substitute for GxP audit |

**MES-001 performs no ALTER against any current V001/V002 table.** Existing IAM design deviations remain a separate MES-002 design/reconciliation concern.

### 4.6 Next Physical Flyway Migration Sequence

No file is created by this proposal. If approved, the next implementation plan must use:

| Physical version | Logical group | Proposed contents |
|---|---|---|
| `V003__mes_001_platform_base.sql` | `LG-001 platform_base` | Create `gxp_audit_event`, `gxp_signature`, `integration_inbox`, `integration_outbox`, `platform_idempotency_record`; constraints, indexes and append-only guards |
| `V004__mes_001_platform_permissions.sql` | `LG-001 platform_base` | Idempotently seed `audit:view`, `ebr:sign`, `integration:view`, `integration:retry`; map them to `SYSTEM_ADMIN` using the existing V002 schema |

The first later task must allocate physical V005 or higher. It must consult the migration ledger rather than derive its version from the logical group number.

### 4.7 Logical-to-physical mapping

| Logical group | Original Section 15 label | Current physical mapping |
|---|---|---|
| `LG-000 repository_foundation` | not previously represented | physical V001 |
| `LG-002 iam_rbac` | V002 | physical V002 plus any future append-only MES-002 corrective migrations |
| `LG-001 platform_base` | V001 | proposed physical V003 and V004 |
| `LG-003` onward | V003 onward | allocate the next free physical versions after predecessor acceptance; never assume numeric equality |

The design dependency remains MES-001 → MES-002 → MES-003, even though the current repository contains a pre-existing physical MES-002 slice. Current IAM is a compatibility prerequisite, not evidence that MES-002 is accepted.

### 4.8 Recommended repair

Adopt Option 1 and amend Section 15 headings/columns from `Migration` to `Logical Migration Group`, with a normative physical mapping ledger rule.

### 4.9 Alternative

Create only V003 and place permission seeds in the same file. This is valid but less reviewable because schema and IAM compatibility changes share one checksum. Separate V003/V004 is recommended.

### 4.10 Downstream impact

- Every MES-002–013 plan must cite a logical group and resolve the next physical version from the ledger.
- Future FKs can depend on accepted physical tables without renumbering the frozen topology.
- MES-002 must separately reconcile its current schema to the frozen IAM dictionary using additive migrations only.

### 4.11 FINAL BASELINE change required

**Yes.** Section 15 and MES-001 migration documentation require an approved addendum. No existing physical migration changes.

### 4.12 Resolution status

**REQUIRES USER DECISION** — approve logical-group semantics, V003/V004 mapping, and the additional platform idempotency table.

---

## 5. DG-002 / B — Common Database Columns Resolution

### 5.1 Classification

- Primary: **MISSING SPECIFICATION**
- Secondary: **NON-BLOCKING IMPLEMENTATION DETAIL** once frozen here

### 5.2 Current conflict

The baseline lists seven common columns but leaves type, nullability, defaults, precision, FKs, optimistic locking and immutable-table exceptions unspecified.

### 5.3 Frozen documents involved

- `05_DATABASE_DESIGN_V1.0_FROZEN.md` common columns and freeze reconciliation.
- Domain append-only/history rules.
- GMP/Audit/eSignature document.
- Repository AGENTS rules 8, 9 and 11.

### 5.4 Requirements, database, API, tasks and tests involved

- Requirements: every regulated requirement; directly AUD-001, SIG-001, INT-001.
- Tables: all new MES tables; directly the five proposed LG-001 tables.
- APIs: every version-sensitive write using `If-Match` or `versionNo`.
- Tasks: MES-001–013.
- Tests: concurrency/version tests in every mutable aggregate; TC-AUD-001, TC-SIG-001, TC-INT-001/002.

### 5.5 Frozen common-column profiles

#### Profile M — Mutable regulated/configuration table

| Column | SQL definition | Rule |
|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | Primary key; never reused; JSON string |
| `org_id` | `BIGINT NOT NULL` | Required tenant/organization partition; indexed; no physical FK until organization table exists |
| `created_by` | `BIGINT NOT NULL` | Identity snapshot reference; logical FK to user; no physical FK in common metadata |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | UTC supplied/validated by server/database time policy |
| `updated_by` | `BIGINT NOT NULL` | Equals `created_by` on insert; changed explicitly by application on update |
| `updated_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | Changed explicitly; no `ON UPDATE CURRENT_TIMESTAMP` |
| `version_no` | `BIGINT NOT NULL DEFAULT 0` | MyBatis-Plus optimistic lock; increments exactly once per successful mutation |

Required indexes: primary key on `id`; leading `org_id` in business unique/index definitions. Do not create a standalone index on every audit metadata column unless a frozen query needs it.

#### Profile A — Immutable append-only evidence table

| Column | SQL definition | Rule |
|---|---|---|
| `id` | `BIGINT NOT NULL AUTO_INCREMENT` | Primary key |
| `org_id` | `BIGINT NOT NULL` | Organization partition |
| `created_by` | `BIGINT NOT NULL` | Actor or approved technical principal |
| `created_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` | Creation/evidence timestamp |
| `updated_by` | omitted | No update is legal |
| `updated_at` | omitted | No update is legal |
| `version_no` | omitted | No mutable state to lock |

Profile A applies to `gxp_audit_event` and later immutable event/revision/ledger evidence tables. It does not apply to a table whose lifecycle includes a controlled state change.

### 5.6 FK strategy

- Business ownership references explicitly marked FK in the table dictionary use physical FKs when the referenced table physically exists.
- Common metadata `created_by`/`updated_by` are logical identity references without physical FKs. This prevents migration cycles and preserves evidence even if identity storage is later archived; user records themselves remain non-deletable.
- `org_id` is a logical reference until the physical organization table is accepted. Later tasks must not retrofit a physical FK to historical tables without an approved impact assessment.
- `gxp_audit_event.actor_id` and `gxp_signature.signer_id` are explicit physical FKs to current `sys_user(id)` because V002 physically exists before proposed V003.
- `gxp_signature.revoked_signature_id` is a physical self-FK.

### 5.7 GxP evidence policy

- `gxp_audit_event`: Profile A, insert/select only.
- `gxp_signature`: Profile M because VALID → INVALIDATED is a controlled mutation; signed identity, meaning, object, digest and signed time are immutable. Only status, invalidation fields, update metadata and version may change.
- Integration inbox/outbox: Profile M; payload and identity fields are immutable after insert, while status/retry/error timestamps mutate through explicit domain methods.
- No table in MES-001 receives a physical-delete API.
- Runtime repositories expose no generic update/delete method.
- Production database grants must restrict `gxp_audit_event` to SELECT/INSERT for the application user. A Flyway/DBA account remains separate.
- A MariaDB `BEFORE UPDATE` and `BEFORE DELETE` guard on `gxp_audit_event` must `SIGNAL SQLSTATE '45000'`; this is part of proposed V003.

### 5.8 Recommended repair

Add the two profiles and FK/time/locking rules to the database design and Section 15 addendum. Apply them prospectively; do not ALTER V001/V002 tables in MES-001.

### 5.9 Alternative

Keep all seven columns on immutable tables and never update them. Rejected because meaningless update/version fields imply unsupported mutation and invite generic CRUD.

### 5.10 Downstream impact

- MES-002–013 receive one deterministic schema convention.
- Append-only revision, ledger and audit tables use Profile A.
- Mutable aggregates use Profile M and `If-Match`/optimistic locking.
- Existing V002 tables are grandfathered until MES-002 performs its own additive reconciliation.

### 5.11 FINAL BASELINE change required

**Yes.** Database design and Section 15 require the two normative profiles.

### 5.12 Resolution status

**RESOLVED** — the proposal freezes all requested physical details; approval is still required before baseline amendment.

---

## 6. DG-003 / C — Electronic Signature Ownership and Contract

### 6.1 Classification

- Primary: **DESIGN DEFECT**
- Secondary: **MISSING SPECIFICATION**

### 6.2 Current conflict

- MES-001 task, FD-AUD-001, RTM and TC-SIG-001 place signature capability/API in MES-001.
- Full OpenAPI assigns the generic sign operation to MES-007/013.
- Canonicalization, reauthentication, provider ownership and invalidation protocol are incomplete.

### 6.3 Frozen documents involved

- PRD SIG-001.
- Domain detailed design.
- eBR detailed design section 8.
- Database design `gxp_signature` and frozen additions.
- API detailed design and full OpenAPI.
- FD-AUD-001.
- GMP/Audit/eSignature.
- RTM, MES-001/MES-007/MES-013 task cards.

### 6.4 Requirements, database, API, tasks and tests involved

- Requirements: SIG-001, AUD-001, EBR-007, REL-001.
- Tables/fields: `gxp_signature`; all listed signature fields and Profile M columns.
- APIs: new `POST /api/v1/auth/reauth`; existing `POST /api/v1/records/{type}/{id}/sign`; later `/field-values/*/corrections` and `/release-decisions` orchestration.
- Tasks: MES-001, MES-007, MES-013.
- Tests: TC-SIG-001, TC-EBR-007 and release-gate/signature regression in MES-013.

### 6.5 Ownership split

#### MES-001 — platform Signature Infrastructure

MES-001 owns:

- `gxp_signature` schema and persistence.
- generic `SignatureService` and explicit sign/verify/invalidate/re-sign commands.
- `SignableObjectProvider` registry contract.
- canonical envelope and record digest algorithm.
- generic reauthentication service/token contract.
- generic `POST /api/v1/records/{type}/{id}/sign` controller and DTOs.
- permission `ebr:sign` enforcement.
- status semantics VALID/INVALIDATED, optimistic locking and audit coupling.
- TC-SIG-001 infrastructure proof using a test-only provider; no fake production object/table.

MES-001 does not own eBR fields, eBR review rules, release decisions or their business gates.

#### MES-007 — eBR Signature Binding

MES-007 owns:

- production providers for `EBR_FORM_INSTANCE` and `EBR_REVIEW_RECORD`.
- the exact eBR snapshot fields and child evidence IDs included in the canonical record.
- allowed meanings `VERIFY` and `APPROVE` according to the eBR state/review rule.
- correction impact graph and same-transaction invalidation calls.
- the rule that an invalidated signature blocks the affected review/form until re-sign.
- TC-EBR-007 and all eBR signature/regression cases.

#### MES-013 — QA Release Signature Binding

MES-013 owns:

- provider for `QA_RELEASE_DECISION`.
- allowed meanings `RELEASE` and `REJECT`.
- release-gate re-read and atomic composition of immutable ReleaseDecision + signature + audit + inventory/outbox side effects.
- signature evidence checks used by QA release.
- superseding release-decision behavior. An old immutable decision/signature is not changed merely because a new decision supersedes it.

The generic sign endpoint never performs QA release by itself.

### 6.6 Signable object contract

Each provider must expose this semantic contract:

- `objectType`: one frozen uppercase identifier.
- `objectId`: stable string form of the domain ID.
- `recordVersion`: current optimistic version of the signable record.
- `canonicalRecord`: JSON object containing only frozen signed fields; no volatile display text, current time or requester input.
- `evidenceIds`: sorted stable identifiers of relevant child evidence.
- `allowedMeanings`: closed set for that object type.
- `loadForSignature`: loads and locks/validates the current domain state inside the local transaction.
- `validateSignable`: returns domain failure if state/gates no longer allow signing.

Frozen production object types and meanings:

| Object type | Owner | Allowed meanings |
|---|---|---|
| `EBR_FORM_INSTANCE` | MES-007 | `VERIFY`, `APPROVE` |
| `EBR_REVIEW_RECORD` | MES-007 | `VERIFY`, `APPROVE` |
| `QA_RELEASE_DECISION` | MES-013 | `RELEASE`, `REJECT` |

Unknown object types or disallowed meanings return 422 `UNSUPPORTED_SIGNABLE_OBJECT` or `SIGNATURE_MEANING_NOT_ALLOWED`.

### 6.7 Canonical envelope and record digest

Before RFC 8785 canonicalization, the server constructs exactly:

```json
{
  "schemaVersion": "1.0",
  "objectType": "EBR_FORM_INSTANCE",
  "objectId": "123",
  "recordVersion": 7,
  "record": {},
  "evidenceIds": ["ATTACHMENT:9", "FIELD_REVISION:81"]
}
```

Rules:

- `recordVersion` is a JSON integer.
- `record` is provider-owned but must obey Section 3.3.
- `evidenceIds` contains `TYPE:ID` strings, deduplicated and lexicographically sorted.
- `record_digest = lowercaseHex(SHA-256(UTF8(JCS(envelope))))`.
- The server computes it; the client never submits it.
- On verification the server rebuilds the envelope from current source-of-truth data and compares in constant time.

### 6.8 Reauthentication contract

Add `POST /api/v1/auth/reauth`, authenticated and protected by `ebr:sign`.

Request schema `ReauthenticateForSignatureRequest`:

| Field | Type | Rule |
|---|---|---|
| `objectType` | string | frozen provider identifier |
| `objectId` | string | 1–100 characters |
| `meaning` | enum | allowed by provider |
| `recordVersion` | integer/int64 | must equal current version |
| `credential` | string | current user's password; never logged or persisted |

Response schema `ReauthenticationResponse`:

| Field | Type | Rule |
|---|---|---|
| `reauthToken` | string | opaque 256-bit random, URL-safe; single-use |
| `expiresAt` | date-time | five minutes after successful reauthentication |

Token rules:

- Store only a SHA-256 token hash in the existing authenticated session store.
- Bind to user ID, session ID, org ID, object type/id, meaning and record version.
- TTL is exactly five minutes.
- Consume atomically on first sign attempt; replay returns 401 `REAUTH_TOKEN_INVALID`.
- Password mismatch returns 401 `REAUTH_FAILED` without revealing which condition failed.
- A consumed token is not restored on database rollback; the user reauthenticates again.
- Credential/token values are excluded from logs, audit old/new digests and `auth_context_json`.

`auth_context_json` canonical schema:

```json
{
  "schemaVersion": "1.0",
  "method": "PASSWORD",
  "reauthenticatedAt": "2026-09-29T02:00:00.000Z",
  "sessionIdHash": "<64 lowercase hex>",
  "requestId": "<request id>"
}
```

### 6.9 Sign API contract

`POST /api/v1/records/{type}/{id}/sign`

- Headers: `Authorization`, `Idempotency-Key`, `If-Match` containing the signable record version.
- Permission: `ebr:sign` plus provider/domain authorization.
- Request `SignRecordRequest`: `{ "meaning": "APPROVE", "reauthToken": "..." }`.
- Response `SignatureResponse`: `id`, `signerId`, `meaning`, `objectType`, `objectId`, `recordDigest`, `status`, `signedAt`, `revokedSignatureId`, `versionNo`.
- 401: invalid/expired/replayed reauth token.
- 403: missing permission/domain authorization.
- 409: version changed or idempotency conflict.
- 422: unsupported object, meaning or invalid business state.

### 6.10 Invalidation and re-sign

- Signed fields changing in a valid domain correction transaction call `invalidateSignatures(objectType, objectId, reason)` in that same transaction.
- Every current VALID signature for the affected evidence set becomes INVALIDATED through an explicit domain method; set `invalidated_at`, `invalidation_reason`, `updated_by`, `updated_at`, increment `version_no`, and append AuditEvent.
- Signed identity, meaning, digest and signed time never change.
- Re-sign inserts a new signature row. `revoked_signature_id` on the new row references the immediately superseded INVALIDATED signature when the same object/meaning is re-signed.
- Invalidated evidence remains queryable forever.
- A new immutable QA ReleaseDecision is a different object; it does not invalidate the historical signature on the superseded decision.

### 6.11 Recommended repair

Assign the generic infrastructure and API to MES-001; assign concrete providers/business binding to MES-007 and MES-013. Update OpenAPI `x-mes-task` for the generic sign endpoint to MES-001 and add provider ownership notes.

### 6.12 Alternative

Keep the endpoint in MES-007/013 and let MES-001 provide persistence only. Rejected because TC-SIG-001 explicitly requires DB+API evidence in MES-001 and would otherwise remain unverifiable.

### 6.13 Downstream impact

- MES-007/013 cannot invent their own digest, reauthentication or signature table.
- Later modules add providers only; the platform API/schema remains stable.
- Any correction workflow must call platform invalidation within its transaction.

### 6.14 FINAL BASELINE change required

**Yes.** OpenAPI, API design, eBR design, task cards, RTM and test ownership notes require coordinated amendment.

### 6.15 Resolution status

**REQUIRES USER DECISION** — approve endpoint reassignment, object types/meanings, RFC 8785 envelope and new reauthentication API.

---

## 6A. DG-004 — Signature Canonicalization and Reauthentication Resolution

### 6A.1 Classification

- Primary: **MISSING SPECIFICATION**

### 6A.2 Current conflict

The baseline mandates a SHA-256 digest and server reauthentication but does not define canonical bytes, data normalization, evidence ordering, token lifecycle, auth-context content, signable-object lookup or replay behavior. Independent implementations could produce different digests for the same record or accept a reusable authentication assertion.

### 6A.3 Frozen documents involved

- PRD SIG-001.
- eBR detailed design sections 8–9.
- API detailed design sign operation.
- Full OpenAPI generic sign request/response.
- Database `record_digest` and `auth_context_json` fields.
- GMP/Audit/eSignature document.
- FD-AUD-001 and MES-001/MES-007/MES-013 task cards.

### 6A.4 Requirements, database, API, tasks and tests involved

- Requirements: SIG-001, EBR-007, REL-001 and supporting NFR-001.
- Tables/fields: `gxp_signature.record_digest`, `auth_context_json`, `status`, invalidation fields, `revoked_signature_id`, Profile M version columns.
- API/OpenAPI: proposed `POST /api/v1/auth/reauth`; existing `POST /api/v1/records/{type}/{id}/sign`; concrete schemas and error codes in Sections 6.8–6.9.
- Tasks: MES-001 owns algorithm/reauth; MES-007 and MES-013 provide canonical domain content.
- Tests: TC-SIG-001, TC-EBR-007, later QA release signature/gate regression.

### 6A.5 Recommended repair

Adopt Sections 3.3 and 6.6–6.10 as normative: RFC 8785 JCS, lowercase SHA-256, the exact canonical envelope, sorted evidence identifiers, five-minute single-use reauthentication token bound to user/session/org/object/meaning/version, fixed `auth_context_json`, explicit invalidation and insert-only re-sign.

### 6A.6 Alternative

Use ordinary Jackson serialization and submit a password directly on every sign request. Rejected because property ordering/number representation would not be a cross-language canonical contract, and credential handling would be spread across business endpoints.

### 6A.7 Downstream impact

- MES-007 and MES-013 must expose only canonical provider data and cannot choose another hashing or token scheme.
- PDF/archive verification, release gates and future audit tools can reproduce the same digest.
- Any canonical schema version change requires a new `schemaVersion` and backward-compatible verifier; existing signatures are never rehashed in place.

### 6A.8 FINAL BASELINE change required

**Yes.** eBR design, API/OpenAPI, GMP guidance, database field semantics and task ownership notes require amendment.

### 6A.9 Resolution status

**REQUIRES USER DECISION** — approve the normative algorithm and reauthentication API/semantics.

---

## 7. DG-005A / D — AuditEvent Contract and DTOs

### 7.1 Classification

- Primary: **MISSING SPECIFICATION**
- Secondary: **DESIGN DEFECT** because requested correlation fields are absent from the frozen table

### 7.2 Current conflict

AUD-001 requires attributable, contemporaneous and reconstructable audit evidence, but the current table/API omit request/source/idempotency correlation and concrete query/response schemas.

### 7.3 Frozen documents involved

- PRD AUD-001 and NFR-001.
- Domain detailed design transaction rules.
- Database design `gxp_audit_event`.
- API detailed design/full OpenAPI.
- FD-AUD-001, GMP document, RTM, MES-001 task card.

### 7.4 Requirements, database, API, tasks and tests involved

- Requirements: AUD-001, SIG-001, INT-001; every later critical write.
- Table: `gxp_audit_event`.
- API: `GET /api/v1/audit-events`; all later audited commands.
- Tasks: producer MES-001; consumers MES-002–013.
- Test: TC-AUD-001 plus every later audit regression.

### 7.5 Frozen AuditEvent table

Apply Profile A and freeze these business columns:

| Column | SQL definition | Meaning |
|---|---|---|
| `actor_id` | `BIGINT NOT NULL` | authenticated or registered technical principal; FK `sys_user(id)` |
| `actor_role` | `VARCHAR(100) NULL` | effective role code used for authorization; null only for registered technical principal without a role |
| `action` | `VARCHAR(80) NOT NULL` | stable uppercase action code |
| `object_type` | `VARCHAR(80) NOT NULL` | stable uppercase object type |
| `object_id` | `VARCHAR(100) NOT NULL` | stable object identifier |
| `old_value_digest` | `TEXT NULL` | 64-char canonical SHA-256 before state; null for create/no prior state |
| `new_value_digest` | `TEXT NULL` | 64-char canonical SHA-256 after state; null only when the action has no resulting state |
| `reason` | `VARCHAR(1000) NULL` | reason; domain command makes it mandatory where required |
| `client_info` | `VARCHAR(500) NULL` | sanitized client channel/IP/user-agent summary; no credential/token |
| `occurred_at` | `DATETIME(3) NOT NULL` | server UTC business event time |
| `transaction_id` | `VARCHAR(100) NOT NULL` | one UUID/correlation ID per local DB transaction |
| `request_id` | `VARCHAR(100) NULL` | inbound request ID; null for background work |
| `source` | `VARCHAR(30) NOT NULL` | `API`, `SCHEDULER`, `INTEGRATION`, or `SYSTEM` |
| `idempotency_key` | `VARCHAR(128) NULL` | client key for audited idempotent commands |

Indexes:

- `ix_gxp_audit_org_time (org_id, occurred_at, id)`
- `ix_gxp_audit_actor_time (actor_id, occurred_at, id)`
- `ix_gxp_audit_object_time (object_type, object_id, occurred_at, id)`
- `ix_gxp_audit_action_time (action, occurred_at, id)`
- `ix_gxp_audit_transaction (transaction_id)`
- `ix_gxp_audit_request (request_id)`

The audit row stores digests, not raw before/after regulated content. Reconstructability comes from source-of-truth revisions plus the audit correlation. Raw credentials, tokens and secrets are never placed in audit evidence.

### 7.6 Actor and transaction rules

- API actor comes from authenticated principal, never request body.
- `actor_role` is the role code that authorized the operation, not a mutable display name or all current roles.
- Background jobs use a configured technical user present in `sys_user`; `actor_id` remains non-null.
- `created_by = actor_id`, `created_at = occurred_at` for audit rows.
- The application generates one transaction UUID at the application-service boundary and reuses it for all audit/outbox side effects in that local transaction.
- `request_id` comes from the trace/request filter; a client-supplied value is validated or replaced according to the platform request-ID policy.

### 7.7 Append-only enforcement

- Domain repository exposes `append` and query only.
- MyBatis mapper update/delete are not surfaced through application services.
- Application runtime DB grant is SELECT/INSERT only for this table.
- V003 creates database update/delete guards.
- Corrections append a new business revision and a new audit event; no audit row is corrected in place.

### 7.8 Concrete Audit API

`GET /api/v1/audit-events`, permission `audit:view`, organization derived from principal.

Query parameters `AuditEventQuery`:

| Name | Type | Rule |
|---|---|---|
| `actorId` | string/int64 | optional |
| `action` | string | optional exact action code |
| `objectType` | string | optional |
| `objectId` | string | optional; requires `objectType` |
| `source` | enum | optional |
| `transactionId` | string | optional |
| `requestId` | string | optional |
| `occurredFrom` | date-time | optional inclusive |
| `occurredTo` | date-time | optional exclusive; must be after from |
| `page` | integer | default 0, minimum 0 |
| `size` | integer | default 50, range 1–200 |

Sort is fixed to `occurred_at DESC, id DESC`; arbitrary sort fields are not accepted.

`AuditEventResponse` fields:

- `id`, `orgId`, `actorId` as strings.
- `actorRole`, `action`, `objectType`, `objectId`.
- `oldValueDigest`, `newValueDigest`, `reason`, `clientInfo`.
- `occurredAt`, `transactionId`, `requestId`, `source`.

`AuditEventPageResponse` fields: `items`, `page`, `size`, `total`.

No public create/update/delete AuditEvent endpoint exists.

### 7.9 Platform idempotency record

To make the global `Idempotency-Key` contract enforceable and replayable, proposed V003 also creates `platform_idempotency_record` using Profile M:

| Column | SQL definition |
|---|---|
| `actor_id` | `BIGINT NOT NULL`, FK `sys_user(id)` |
| `operation_code` | `VARCHAR(100) NOT NULL` |
| `idempotency_key` | `VARCHAR(128) NOT NULL` |
| `request_digest` | `CHAR(64) NOT NULL` |
| `state` | `VARCHAR(20) NOT NULL`, `IN_PROGRESS` or `COMPLETED` |
| `http_status` | `SMALLINT NULL` |
| `response_json` | `LONGTEXT NULL` |
| `resource_type` | `VARCHAR(80) NULL` |
| `resource_id` | `VARCHAR(100) NULL` |
| `expires_at` | `DATETIME(3) NOT NULL` |

Unique key: `(org_id, actor_id, operation_code, idempotency_key)`. Reuse with a different `request_digest` returns 409 `IDEMPOTENCY_KEY_REUSED`; a completed identical request replays the stored response. Retention of these non-GxP technical records is configuration-driven and does not delete regulated evidence.

### 7.10 Recommended repair

Add the three audit columns, concrete DTOs, append-only controls and idempotency table to the baseline addendum.

### 7.11 Alternative

Reuse `sys_security_event` or store raw before/after JSON. Rejected: the former lacks GxP fields/semantics; the latter expands sensitive-data exposure and contradicts frozen digest columns.

### 7.12 Downstream impact

- All later audited services must supply stable action/object codes and canonical before/after digests.
- All idempotent writes can consume one platform contract instead of building module-local dedupe tables.
- Existing IAM security logs remain separate. Before MES-002 implementation, its frozen audit matrix must enumerate which IAM administrative commands also append GxP audit; MES-001 does not classify those commands.

### 7.13 FINAL BASELINE change required

**Yes.** Database, API/OpenAPI, functional design and downstream contract sections require amendment.

### 7.14 Resolution status

**REQUIRES USER DECISION** — approve new audit columns, concrete DTOs and platform idempotency table.

---

## 8. DG-005B / E — Integration Contract and Concrete OpenAPI Schemas

### 8.1 Classification

- Primary: **MISSING SPECIFICATION**

### 8.2 Current conflict

INT-001 requires message identity, idempotency, retry and reconciliation, but the baseline contains only minimal columns and Generic OpenAPI schemas. Status values, routing, retry terminal behavior and concurrency are undefined.

### 8.3 Frozen documents involved

- PRD INT-001 and integration section.
- Domain transaction/outbox rules.
- Database design inbox/outbox.
- API detailed design/full OpenAPI.
- RTM and MES-001 task.
- TC-INT-001 and TC-INT-002.

### 8.4 Requirements, database, API, tasks and tests involved

- Requirement: INT-001; supporting AUD-001 and NFR-001.
- Tables: `integration_inbox`, `integration_outbox`, `gxp_audit_event`, `platform_idempotency_record`.
- APIs: `GET /api/v1/integration/messages`; `POST /api/v1/integration/messages/{messageRef}/retry`.
- Tasks: MES-001 producer; integrations in MES-004–013 consumers/producers.
- Tests: TC-INT-001, TC-INT-002, TC-AUD-001 for manual retry audit.

### 8.5 Frozen `integration_inbox`

Use Profile M. Identity/payload columns are immutable after insert.

| Column | SQL definition |
|---|---|
| `source_system` | `VARCHAR(80) NOT NULL` |
| `message_id` | `VARCHAR(128) NOT NULL` |
| `payload_json` | `LONGTEXT NOT NULL` |
| `status` | `VARCHAR(20) NOT NULL` |
| `received_at` | `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` |
| `processed_at` | `DATETIME(3) NULL` |
| `retry_count` | `INT NOT NULL DEFAULT 0` |
| `next_retry_at` | `DATETIME(3) NULL` |
| `last_error_code` | `VARCHAR(100) NULL` |
| `last_error_message` | `VARCHAR(1000) NULL` |

Constraints/indexes:

- unique `(org_id, source_system, message_id)`; replace the ambiguous global `message_id` unique rule.
- `ix_inbox_claim (org_id, status, next_retry_at, id)`.
- `ix_inbox_received (org_id, received_at, id)`.
- `retry_count >= 0`.

### 8.6 Frozen `integration_outbox`

Use Profile M. Message identity, aggregate reference and payload are immutable after insert.

| Column | SQL definition |
|---|---|
| `message_id` | `VARCHAR(128) NOT NULL` |
| `target_system` | `VARCHAR(80) NOT NULL` |
| `event_type` | `VARCHAR(80) NOT NULL` |
| `aggregate_type` | `VARCHAR(80) NOT NULL` |
| `aggregate_id` | `VARCHAR(100) NOT NULL` |
| `payload_json` | `LONGTEXT NOT NULL` |
| `status` | `VARCHAR(20) NOT NULL` |
| `retry_count` | `INT NOT NULL DEFAULT 0` |
| `next_retry_at` | `DATETIME(3) NULL` |
| `published_at` | `DATETIME(3) NULL` |
| `last_error_code` | `VARCHAR(100) NULL` |
| `last_error_message` | `VARCHAR(1000) NULL` |

Constraints/indexes:

- unique `(org_id, message_id)`.
- `ix_outbox_claim (org_id, status, next_retry_at, id)`.
- `ix_outbox_aggregate (org_id, aggregate_type, aggregate_id, id)`.
- `ix_outbox_target_created (org_id, target_system, created_at, id)`.
- `retry_count >= 0`.

The new `message_id`, `target_system`, error and completion fields are required to satisfy INT-001 and operational reconciliation; they require baseline change approval.

### 8.7 Status machines

#### Inbox status

`RECEIVED → PROCESSING → PROCESSED`  
`PROCESSING → RETRY_WAIT → PROCESSING`  
`PROCESSING → DEAD_LETTER`  
`RETRY_WAIT/DEAD_LETTER --manual retry→ RECEIVED`

#### Outbox status

`PENDING → DISPATCHING → PUBLISHED`  
`DISPATCHING → RETRY_WAIT → DISPATCHING`  
`DISPATCHING → DEAD_LETTER`  
`RETRY_WAIT/DEAD_LETTER --manual retry→ PENDING`

There is no generic status endpoint. Transitions occur only through receive/process/enqueue/claim/publish/fail/manual-retry domain methods.

V003 must add CHECK constraints for exactly the listed inbox and outbox status sets. `gxp_signature.status` likewise permits only `VALID` and `INVALIDATED`.

### 8.8 Retry and final-failure rules

- `retry_count` counts failed processing/publishing attempts, including a failed manual attempt.
- Automatic attempt limit is 8.
- After failures 1–7, set `RETRY_WAIT` and `next_retry_at` using the exact schedule: 1 minute, 5 minutes, 15 minutes, 1 hour, 4 hours, 12 hours, 24 hours.
- Failure 8 sets `DEAD_LETTER` and clears `next_retry_at`.
- No random jitter is used; deterministic scheduling is testable and auditable.
- A permitted manual retry may run from `RETRY_WAIT` or `DEAD_LETTER`. It does not reset `retry_count` or erase errors. It queues the message immediately as RECEIVED/PENDING; a worker performs the later PROCESSING/DISPATCHING claim.
- If a manual attempt fails and total failures are already at least 8, status returns to `DEAD_LETTER`.
- Manual retry requires a nonblank reason of 1–1000 characters.
- There is no delete/discard endpoint in MES-001.

### 8.9 Idempotency and concurrency

- Inbox business idempotency is enforced by unique `(org_id, source_system, message_id)` in the same transaction as the local business effect. Duplicate delivery returns the existing outcome and does not repeat business work.
- Outbox assigns a server-generated stable `message_id` once; every retry republishes the same ID. Consumers must deduplicate it.
- Enqueue and originating business write commit in one local transaction.
- Worker claim is an atomic conditional update on `id + version_no + eligible status`; one winner increments `version_no`.
- A worker never holds the originating business transaction open during a network call.
- A PROCESSING/DISPATCHING claim older than 15 minutes is treated as abandoned and moved to RETRY_WAIT through an explicit recovery command, incrementing `retry_count`.
- Duplicate external effects after an uncertain publish are prevented by the stable message ID and consumer idempotency, not by pretending distributed exactly-once delivery.
- REST manual retry consumes `platform_idempotency_record`, `Idempotency-Key` and `If-Match`.

### 8.10 Manual retry permissions and audit

- Query requires `integration:view`.
- Manual retry requires both `integration:view` and `integration:retry`.
- Manual retry appends AuditEvent action `INTEGRATION_MESSAGE_RETRY_REQUESTED` in the same transaction as the state transition.
- Transition to DEAD_LETTER appends `INTEGRATION_MESSAGE_DEAD_LETTERED` using the registered technical principal.
- Automatic transient attempts update message evidence and structured logs; they do not each create a GxP AuditEvent.
- Payload bodies and credentials are excluded from AuditEvent and normal logs.

### 8.11 Concrete Integration OpenAPI

`GET /api/v1/integration/messages`

Query schema `IntegrationMessageQuery`:

| Parameter | Type | Rule |
|---|---|---|
| `direction` | `INBOX` or `OUTBOX` | optional |
| `system` | string | maps to source for inbox, target for outbox |
| `messageId` | string | optional exact |
| `eventType` | string | outbox only |
| `aggregateType` | string | outbox only |
| `aggregateId` | string | outbox only |
| `status` | repeated enum | optional |
| `occurredFrom` | date-time | inclusive |
| `occurredTo` | date-time | exclusive |
| `page` | integer | default 0, minimum 0 |
| `size` | integer | default 50, range 1–200 |

`IntegrationMessageResponse`:

- `messageRef`: `INBOX:<id>` or `OUTBOX:<id>`; resolves ID collision across tables.
- `direction`, `messageId`, `sourceSystem`, `targetSystem`.
- `eventType`, `aggregateType`, `aggregateId` where applicable.
- `status`, `retryCount`, `nextRetryAt`.
- `lastErrorCode`, `lastErrorMessage`.
- `occurredAt`: inbox received time or outbox created time.
- `completedAt`: inbox processed time or outbox published time.
- `versionNo`.

Payload JSON is deliberately absent from list responses.

`IntegrationMessagePageResponse`: `items`, `page`, `size`, `total`.

`POST /api/v1/integration/messages/{messageRef}/retry`

- Path `messageRef` pattern: `^(INBOX|OUTBOX):[1-9][0-9]*$`.
- Headers: `Idempotency-Key`, `If-Match`.
- Request `RetryIntegrationMessageRequest`: `{ "reason": "..." }`.
- Response: current `IntegrationMessageResponse`.
- 409: version conflict, non-replay idempotency conflict or currently claimed message.
- 422: status not retry-eligible.

Replace `GenericRequest` and `GenericResponse` for these operations with the schemas above.

### 8.12 Recommended repair

Amend the database and OpenAPI contracts exactly as above. Keep connector-specific transport/protocol adapters outside MES-001.

### 8.13 Alternative

Keep minimal tables and store retry history/errors only in logs. Rejected because manual reconciliation, final-failure evidence and deterministic retry cannot be reliably queried or tested.

### 8.14 Downstream impact

- All later outbox producers must provide target system, event type, aggregate identity and payload, and reuse platform message identity.
- ERP/LIMS/device adapters implement `ExternalMessagePublisher`; business modules never call external systems inside local transactions.
- Later consumers inherit exact idempotency and retry behavior.

### 8.15 FINAL BASELINE change required

**Yes.** Database fields/indexes, API/OpenAPI schemas, state semantics, RTM notes and test details require amendment.

### 8.16 Resolution status

**REQUIRES USER DECISION** — approve additional fields, composite inbox uniqueness, statuses/retry policy and concrete DTOs.

---

## 9. DG-006 — Permission Scope Resolution

### 9.1 Classification

- Primary: **DESIGN DEFECT**
- Secondary: **NON-BLOCKING IMPLEMENTATION DETAIL** because authority order resolves it

### 9.2 Current conflict

MES-001 task lists only `audit:view` and `ebr:sign`; full OpenAPI assigns `integration:view` and `integration:retry` to MES-001 operations.

### 9.3 Frozen documents involved

- MES-001 task card.
- API detailed design/full OpenAPI.
- Functional design and UI materials.
- RTM.

### 9.4 Requirements, database, API, tasks and tests involved

- Requirements: AUD-001, SIG-001, INT-001, NFR-001.
- Table: existing `sys_permission` and `sys_role_permission`; no ALTER.
- APIs: audit query, signature reauth/sign, integration query/retry.
- Task: MES-001 produces codes; MES-002 owns general RBAC administration.
- Tests: 401/403/allowed-path tests for every MES-001 endpoint.

### 9.5 Frozen permission catalog

| Permission | Existing physical type | Route/action |
|---|---|---|
| `audit:view` | MENU | route `/audit`; audit query API |
| `ebr:sign` | ACTION | reauthenticate and generic sign |
| `integration:view` | MENU | route `/integration/operations`; integration query API |
| `integration:retry` | ACTION | manual retry; requires `integration:view` as well |

V004 seeds these rows and maps them to `SYSTEM_ADMIN`. MES-001 does not implement role/permission CRUD.

### 9.6 Recommended repair

Correct MES-001 Permission Scope to include all four. OpenAPI already supplies the higher-authority operation permissions.

### 9.7 Alternative

Reuse `audit:view` for integration monitoring or `ebr:sign` for retry. Rejected as a least-privilege violation.

### 9.8 Downstream impact

- MES-002 must expose/manage these four codes without renaming them.
- UI visibility and backend authorization use the same codes.

### 9.9 FINAL BASELINE change required

**Yes.** MES-001 task permission section and UI/permission catalog must be corrected.

### 9.10 Resolution status

**RESOLVED** — authority order and least privilege yield one unambiguous catalog.

---

## 10. DG-007 / F — UI Route and Prototype Data Resolution

### 10.1 Classification

- Primary: **DESIGN DEFECT**
- Secondary: **MISSING SPECIFICATION**

### 10.2 Current conflict

- UI V1.1 freezes `/audit` for AUD-001.
- Prototype maps `/platform/operations` to nonexistent PLAT-001 and mixes audit, integration and unsupported health metrics.
- Prototype is explicitly subordinate and cannot change formal route/API/requirement contracts.

### 10.3 Frozen documents involved

- UI V1.1 detailed design and route matrix.
- Prototype mapping, design system, screenshot and freeze note.
- PRD page requirements.
- MES-001 task card.
- API/OpenAPI.

### 10.4 Requirements, database, API, tasks and tests involved

- Requirements: AUD-001 and INT-001. PLAT-001 is invalid and must be removed.
- Tables: audit and integration tables only through their APIs.
- APIs: audit query and integration query/retry.
- Task: MES-001.
- Tests: route/permission/component tests; formal TC-AUD-001 and TC-INT cases remain backend evidence.

### 10.5 Formal route decision

| Page ID | Route | Requirement | Permission | Purpose |
|---|---|---|---|---|
| `UI-AUD-Q` | `/audit` | AUD-001 | `audit:view` | AuditEvent query only |
| `UI-INT-OPS` | `/integration/operations` | INT-001 | `integration:view` | Inbox/outbox monitoring and permitted manual retry |

- `/platform/operations` is not a formal business route and must not be implemented or retained as an alias.
- `PLAT-001` is removed from prototype traceability.
- The prototype screenshot remains visual inspiration for density, cards, tables and charts, not one indivisible formal page.
- A revised prototype mapping must point audit elements to `UI-AUD-Q` and integration elements to `UI-INT-OPS`.

Adding `UI-INT-OPS` is a controlled UI V1.1 design change because INT-001 already requires interface monitoring but the route matrix omitted it.

### 10.6 Metric and panel data-source disposition

| Prototype element | Formal disposition | Data source |
|---|---|---|
| OpenAPI endpoints = 171 | Remove from runtime page | Release manifest only; no runtime API |
| Audit today count | Keep on `/audit` | `GET /audit-events` with today's UTC range; use `total` |
| Audit trend chart | Remove until an aggregate API is approved | No frozen aggregate API; do not download all rows for client aggregation |
| Outbox pending count | Keep on `/integration/operations` | `GET /integration/messages?direction=OUTBOX&status=PENDING&status=RETRY_WAIT&size=1`; use `total` |
| Dead-letter count | Add on integration page | same endpoint with `status=DEAD_LETTER`; use `total` |
| Compliance health 99.98% | Remove | no definition or formal API |
| Audit/eSignature service health | Remove | no per-service health contract |
| MES Core API health | Remove | Actuator health is operational, not this frozen UI contract |
| ERP Outbox status | Replace with outbox status counts/table | integration query API |
| PDF Archive status | Remove from MES-001 | MES-013-owned capability |
| Audit event table | Keep on `/audit` | audit query API |
| Integration message table/retry | Keep on `/integration/operations` | integration query/retry APIs |

No UI metric may contain sample constants in production code. Empty/loading/error/permission states are required.

### 10.7 Recommended repair

Preserve `/audit`; add `/integration/operations` as an explicit INT-001 route; split/retrace the prototype and remove all unsupported panels.

### 10.8 Alternative

Place integration monitoring inside `/audit`. Rejected because it conflates different requirements and permissions and turns audit access into integration access.

### 10.9 Downstream impact

- MES-002 permission/navigation model must expose both routes.
- MES-013 later adds archive/release operational UI under its own route/API rather than reusing MES-001 placeholders.
- Prototype visual tokens remain shared, but data elements are contract-driven.

### 10.10 FINAL BASELINE change required

**Yes.** UI V1.1 route matrix, detailed design, prototype mapping and MES-001 UI references require coordinated amendment.

### 10.11 Resolution status

**REQUIRES USER DECISION** — approve the new `UI-INT-OPS` route and removal/splitting of unsupported prototype metrics.

---

## 11. DG-008 / G — NFR Enterprise Parameters and Validation Gates

### 11.1 Classification

- Primary: **ENTERPRISE DECISION REQUIRED**

### 11.2 Current conflict

PRD explicitly leaves RPO/RTO, retention, performance and deployment values to enterprise/SOP confirmation, while the MES-001 task asks for NFR acceptance. Treating every missing value as a code blocker would stop valid foundation work; inventing values would violate the baseline.

### 11.3 Frozen documents involved

- PRD NFR-001, NFR-002, NFR-003 and pending-enterprise section.
- Test acceptance and RTM.
- MES-001 task card.
- Deployment/CI rules.

### 11.4 Requirements, database, API, tasks and tests involved

- Requirements: NFR-001, NFR-002, NFR-003.
- Tables: all regulated records; idempotency records are non-GxP technical data.
- API: no new public business API.
- Tasks: MES-001 defines platform configuration/validation gates; all tasks must preserve them.
- Tests: NFR verification; backup/restore and retention evidence before go-live.

### 11.5 Blocking classification

#### Blocking for MES-001 implementation

- Code must support external secure configuration and must not embed enterprise values.
- Production profile must detect missing required enterprise parameters and fail validation/startup before a production deployment can be declared valid.
- Audit/signature timestamps require an approved UTC time-source mechanism, but the particular enterprise NTP source is deployment input.
- No physical purge of GxP evidence may be implemented.

#### Not blocking MES-001 code construction

- Exact RPO.
- Exact RTO.
- Exact GxP retention period.
- Named deployment location/site.
- Approved backup product/procedure and exercise calendar.

These become **OPEN ENTERPRISE VALIDATION ITEM**, not implementation blockers.

#### Blocking for production validation/go-live

- Enterprise-approved RPO/RTO values and successful restore drill evidence.
- Market/SOP-approved retention period and retrieval validation.
- Approved production time source and clock-drift monitoring evidence.
- Security test report, dependency audit, TLS/secret configuration evidence.
- Deployment qualification and monitoring/runbook approval.

### 11.6 Configuration contract without fabricated values

Production configuration keys, with no defaults:

| Property | Format | Validation |
|---|---|---|
| `mes.continuity.rpo` | ISO-8601 duration | required and positive in production |
| `mes.continuity.rto` | ISO-8601 duration | required and positive in production |
| `mes.compliance.gxp-retention` | ISO-8601 period | required and positive in production |
| `mes.deployment.site-id` | nonblank string | required in production |
| `mes.time.approved-source` | nonblank identifier | required in production |

- No repository `.env.example`, test fixture or default value is evidence of enterprise approval.
- Non-production profiles may omit the values; tests validate both missing-production failure and syntactically valid binding without asserting enterprise targets.
- Retention configuration controls validation/reporting only in MES-001. It does not schedule deletion.
- Production readiness report must show the approved values and reference the signed SOP/validation evidence.

### 11.7 Recommended repair

Reclassify the values as open enterprise validation items, add the no-default production configuration contract and define a go-live validation gate.

### 11.8 Alternative

Block all MES-001 coding until values arrive. Rejected because schema/services can be correctly built without those numeric values. Hard-coded placeholder values are also rejected.

### 11.9 Downstream impact

- Later modules cannot introduce their own retention or continuity values.
- Go-live remains blocked until enterprise evidence exists even if all code tests pass.
- No task may equate application completion with GxP validation release.

### 11.10 FINAL BASELINE change required

**Not for MES-001 construction.** Before production validation/go-live, an approved enterprise validation addendum must record the supplied values and evidence references.

### 11.11 Resolution status

**REQUIRES ENTERPRISE INPUT** — non-blocking for implementation, mandatory for production validation/go-live.

---

## 12. DG-009 — MES-001 UI Authentication Dependency

### 12.1 Classification

- Primary: **EXISTING REPOSITORY CONSTRAINT**
- Secondary: **NON-BLOCKING IMPLEMENTATION DETAIL**

### 12.2 Current conflict

MES-001 has no logical dependency but its protected UI needs an authenticated context. IAM is logically MES-002, while the current repository already contains backend IAM but lacks frontend login/token lifecycle.

### 12.3 Frozen documents involved

- Section 15 task dependency matrix.
- API global JWT contract.
- UI V1.1 permission rules.
- MES-001 and MES-002 task cards.

### 12.4 Requirements, database, API, tasks and tests involved

- Requirements: AUD-001, INT-001, NFR-001 and MES-002 IAM requirements indirectly.
- Tables: no MES-001 schema change; current IAM tables are read-only compatibility infrastructure.
- APIs: MES-001 endpoints require current JWT principal/authorities.
- Tasks: MES-001 UI/components and MES-002 authentication/navigation.
- Tests: backend 401/403/authorized tests; frontend mocked authenticated/forbidden states; full login-to-page E2E in MES-002 regression.

### 12.5 Frozen responsibility boundary

- MES-001 implements protected route components, API clients and permission metadata for `audit:view` and `integration:view`.
- MES-001 does not implement login, refresh, logout, user/role administration or token persistence UI.
- MES-001 frontend components receive an `AuthContext`/permission provider contract and are tested with test doubles.
- The current backend JWT/security implementation may be used as physical compatibility infrastructure but is not declared an accepted MES-002 contract.
- Until MES-002 frontend integration is accepted, direct navigation may produce the defined 401/403 state; that does not authorize a temporary login screen.
- MES-002 later implements navigation visibility and authentication lifecycle and must run MES-001 route regression.

### 12.6 Recommended repair

Keep the dependency graph unchanged. Record the UI handoff in MES-001 Contracts Produced and MES-002 Contracts Consumed. Accept component/API security tests in MES-001; defer end-to-end login/navigation acceptance to MES-002.

### 12.7 Alternative

Add MES-002 as a hard dependency of MES-001 or implement a temporary login. Both are rejected: the first creates a dependency cycle in construction order; the second pre-implements MES-002 and creates disposable behavior.

### 12.8 Downstream impact

- MES-002 must consume the route/permission catalog and add full E2E coverage.
- MES-001 remains independently testable and does not create a second authentication mechanism.

### 12.9 FINAL BASELINE change required

**No business-design change required.** Add a non-normative integration note to the MES-001/MES-002 task handoff when the baseline addendum is prepared.

### 12.10 Resolution status

**RESOLVED** — this is a staged integration/testing boundary, not a reason to pre-implement MES-002.

---

## 13. Resulting Physical Schema Summary

If the proposal is approved, proposed V003 creates exactly:

1. `gxp_audit_event` — Profile A plus Section 7 fields/indexes/guards.
2. `gxp_signature` — Profile M plus existing frozen fields; statuses VALID/INVALIDATED.
3. `integration_inbox` — Profile M plus Section 8 fields/statuses/indexes.
4. `integration_outbox` — Profile M plus Section 8 fields/statuses/indexes.
5. `platform_idempotency_record` — Profile M plus Section 7.9 fields/unique key.

Proposed V004 only seeds and assigns the four MES-001 permissions. It creates no new table and alters no existing V001/V002 table.

No other table, column, status, permission, API or UI route is authorized by this proposal.

---

## 14. Resulting API/OpenAPI Change Set

After approval, the controlled baseline addendum must:

1. Replace Generic schemas on:
   - `GET /audit-events`
   - `GET /integration/messages`
   - `POST /integration/messages/{messageRef}/retry`
   - `POST /records/{type}/{id}/sign`
2. Add concrete schemas:
   - `AuditEventQuery`, `AuditEventResponse`, `AuditEventPageResponse`
   - `IntegrationMessageQuery`, `IntegrationMessageResponse`, `IntegrationMessagePageResponse`
   - `RetryIntegrationMessageRequest`
   - `ReauthenticateForSignatureRequest`, `ReauthenticationResponse`
   - `SignRecordRequest`, `SignatureResponse`
3. Add `POST /auth/reauth`.
4. Change generic sign operation `x-mes-task` to MES-001-R2; annotate MES-007/MES-013 provider ownership.
5. Retain `Idempotency-Key`, `If-Match`, 403, 409 and 422 contracts.
6. Use the frozen `ApiError` fields for all error responses.

---

## 15. Test and Traceability Corrections

### TC-AUD-001

- Run against an MES-001 manual integration retry transaction.
- Success: message transition and audit commit together.
- Forced rollback: neither transition nor audit persists.
- Verify append-only DB guard.

### TC-SIG-001

- MES-001 registers a test-only `SignableObjectProvider` under the test profile.
- Reauthenticate, sign through the real API, mutate only the test provider's source record, verify digest mismatch and blocked verification.
- Evidence: API plus `gxp_signature`; no production placeholder object/table.
- MES-007 and MES-013 rerun equivalent tests with their production providers.

### TC-INT-001

- Deliver identical `(org_id, source_system, message_id)` concurrently.
- Exactly one inbox identity and one business callback effect persist.

### TC-INT-002

- Business write and outbox row commit.
- External publisher failure moves through the frozen schedule.
- Retry reuses the same message ID; business effect is not duplicated.
- Eighth failure enters DEAD_LETTER; authorized manual retry is audited and idempotent.

### UI tests

- `/audit` requires `audit:view` and renders only API-backed audit elements.
- `/integration/operations` requires `integration:view`; retry additionally requires `integration:retry`.
- No `/platform/operations` route and no PLAT-001 trace.
- No hard-coded production metrics.

### NFR validation

- MES-001 tests production configuration missing-value failure and secure defaults.
- Production go-live remains open until enterprise RPO/RTO/retention/evidence is supplied.

---

## 16. Downstream MES-002–013 Contract Impact

| Task range | Required impact after proposal approval |
|---|---|
| MES-002 | Consume four permission codes; integrate navigation/auth lifecycle; reconcile IAM schema additively; decide which IAM admin operations append GxP audit |
| MES-003–006 | Use common-column profiles, platform audit and idempotency/outbox; no module-local variants |
| MES-007 | Implement eBR signable providers, allowed meanings and correction-driven invalidation |
| MES-008–012 | Produce/consume integration messages with stable message IDs; use platform audit/outbox and Profile A/M rules |
| MES-013 | Implement QA ReleaseDecision provider/orchestration; re-read signature gates; use outbox and immutable decision semantics |

Every affected future task card/plan must include regression of the consumed platform contract. No downstream task may redefine the statuses, digest, retry schedule, permissions or common columns.

---

## 17. FINAL BASELINE Amendment Set After Approval

Approval should authorize a new versioned addendum, not in-place silent edits. The addendum must update or supersede these portions:

- `05_DATABASE_DESIGN_V1.0_FROZEN.md`: common profiles, audit/integration additions, idempotency table.
- `06_EBR_DYNAMIC_FORM_DETAILED_V1.0_FROZEN.md`: canonical envelope, provider, reauth, invalidation/re-sign.
- `08_API_DETAILED_V1.0_FROZEN.md` and full OpenAPI/catalog: concrete DTOs, `/auth/reauth`, task ownership.
- `09_FUNCTIONAL_DETAILED_V1.0_FROZEN.md`: service ownership and transaction boundaries.
- UI V1.1 route matrix/detail and prototype mapping: split audit/integration routes and supported panels.
- Test catalog/detail: exact MES-001 evidence and later provider regression.
- RTM: AUD/SIG/INT route/API/task ownership corrections.
- Section 15: logical migration group terminology and mapping-ledger rule.
- MES-001, MES-002, MES-007 and MES-013 task cards: permission/signature/handoff corrections.

Enterprise RPO/RTO/retention values must be captured later in a separately approved enterprise validation addendum; they are not inserted into this design proposal.

---

## 18. BLOCKER RESOLUTION MATRIX

| Blocker | Primary classification | Proposed disposition | Must modify FINAL BASELINE? | Status |
|---|---|---|---|---|
| DG-001 Migration order/FKs | EXISTING REPOSITORY CONSTRAINT | Keep V001/V002; logical groups; proposed physical V003/V004; no current ALTER | Yes, Section 15 addendum | **REQUIRES USER DECISION** |
| DG-002 Common columns | MISSING SPECIFICATION | Freeze Profile M/Profile A, FK/time/version/GxP rules | Yes | **RESOLVED** |
| DG-003 Signature ownership | DESIGN DEFECT | MES-001 platform/API; MES-007 eBR provider; MES-013 release provider | Yes | **REQUIRES USER DECISION** |
| DG-004 Canonicalization/reauth | MISSING SPECIFICATION | RFC 8785 + SHA-256 envelope; five-minute single-use bound reauth token | Yes | **REQUIRES USER DECISION** |
| DG-005 Audit/integration DTOs and retry | MISSING SPECIFICATION | Audit correlation fields/DTOs/idempotency plus concrete integration tables, schemas, states, eight-attempt schedule, dead-letter and concurrency | Yes | **REQUIRES USER DECISION** |
| DG-006 Permission omission | DESIGN DEFECT | Freeze four least-privilege permission codes | Yes, task correction | **RESOLVED** |
| DG-007 UI route/data | DESIGN DEFECT | Keep `/audit`; add `/integration/operations`; remove unsupported metrics and PLAT-001 | Yes | **REQUIRES USER DECISION** |
| DG-008 NFR values | ENTERPRISE DECISION REQUIRED | Open enterprise validation items; no defaults; production/go-live gate | Only later enterprise validation addendum | **REQUIRES ENTERPRISE INPUT** |
| DG-009 UI authentication staging | EXISTING REPOSITORY CONSTRAINT | Protected components/API now; login/navigation/E2E in MES-002; no temporary auth | No business-design change | **RESOLVED** |

Only the three status values above are used: `RESOLVED`, `REQUIRES USER DECISION`, `REQUIRES ENTERPRISE INPUT`.

---

## 19. Approval Effect and STOP Condition

If approved, the next allowed activity is **baseline addendum preparation and MES-001 implementation-plan revision only**. Coding and migration creation still require a separately approved revised Implementation Plan.

If any `REQUIRES USER DECISION` row is rejected, revise this proposal and re-run consistency/impact review before changing the baseline.

`REQUIRES ENTERPRISE INPUT` does not block MES-001 code construction, but it blocks production validation/go-live and must remain visible in the validation register.

**STOP — awaiting approval of this Design Change Proposal.**
