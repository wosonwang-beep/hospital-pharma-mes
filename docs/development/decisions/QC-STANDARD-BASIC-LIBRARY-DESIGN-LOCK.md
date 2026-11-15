# Design confirmation record — QC quality standard basic library

Status: **DESIGN LOCK** (recorded 2026-11-02). Authority:
[DCP-QC-SPEC-BASIC-LIBRARY-001](../DCP-QC-SPEC-BASIC-LIBRARY-001-APPROVED.md). This is the single
design record for the change; do not implement from conversational memory.

## 1. Human decision

The human (vendor Quality/IT owner) stated on 2026-11-02:

> 检验标准只做为一个基础信息库维护，增删改查就可以，不涉到审批和批准。不需要做版本的管理。标准基础信息有一个标准的依据，这个很重要的信息。还有标准适用于那些物料和产品，也需要管理。

and then selected all three recommended options of the design question: full library refactor with
downstream freeze switched to a standard snapshot (explicitly authorizing deletion of the historical
approval/signature evidence), keep the inspection items, and allow one standard to cover many
materials **and** many products at the same time.

## 2. Reconciled baseline

| Aspect | Frozen before | Authorized now |
| --- | --- | --- |
| Lifecycle | `DRAFT` → `APPROVED` → `RETIRED` per version, signed approval and retirement | one mutable current definition, `ACTIVE`/`INACTIVE` |
| Versioning | `qc_specification_version` aggregate with `version_no_business`, `content_hash` | none; the standard row is the only identity |
| Approval | `qms:specification:approve` + `ebr:sign`, independent approver, signature verification | none; permissions and signable providers removed |
| 标准的依据 | absent | mandatory `basisType` + `basisReference` (+ optional effective date / remark) |
| 适用范围 | exactly one material **or** one product | many-to-many `qc_specification_scope` |
| Items | per version | per standard, same value rules |
| Downstream freeze | version id + content hash | standard id + frozen standard snapshot digest |

Everything else in FINAL BASELINE v1.0.23 stays frozen: organization isolation, idempotency,
optimistic locking, audit, transaction boundaries, no physical deletion of regulated evidence other
than the QC approval/version evidence explicitly authorized here, and the downstream modules' own
states, permissions, routes and snapshots.

## 3. Locked implementation decisions

1. **Scope 1** (full refactor) over the narrower "keep versions, add fields" option, because the human
   rejected version management outright; a partial option would leave two competing lifecycles.
2. **History deletion** is confined to `qc_specification_version` (and its version-scoped items) plus
   the `gxp_signature` rows and audit events that only evidence QC approval/retirement. No other
   evidence is deleted. Flyway history is never repaired or rewritten.
3. **Maintenance trail replaces the version chain**: `qc_specification_revision` records
   CREATE/UPDATE/DELETE with reason, before/after JSON, actor and time, and survives entry deletion
   (its foreign key is dropped in V073).
4. **Downstream column names are retained** (`qc_specification_version_id`) but their values are
   translated in place to standard ids and re-pointed at `qc_specification(id)`. Renaming four frozen
   downstream columns would exceed the authorized scope.
5. **Downstream never re-reads a live standard to justify itself.** Documents keep their own snapshot
   JSON + digest; `QcSpecificationQueryService.read(...)` re-reads only to verify that stored snapshot.
6. **Dictionary over free text** for 标准的依据: `QC_STANDARD_BASIS` (7 items). Free-text basis would
   make the legal/registered/internal distinction unreliable.
7. **Multi-select pickers over typed ids** for applicability (`QcScopePicker`), reusing `ProcessLookup`
   for single subject references. No new visual language, no hard-coded drop-down.
8. **Templates**: list = T1, create/edit/detail = T2/T3. The version page and its route are removed;
   the trace view links to `/quality/specifications/{id}`.

## 4. Write scope

- `backend/mes-qc/**` — domain, application, infrastructure, api.
- `backend/mes-qms/**` — QC reference, freeze digest and snapshot plumbing only.
- `backend/mes-traceability/**` — trace node type/label only.
- `backend/mes-security/**` — the QC URL rules only.
- `backend/mes-boot/src/main/resources/db/migration/V070…V073` — new physical migrations.
- `frontend/mes-web/**` — the QC pages, api module, router entries and the reference labels of the
  reused pages.
- Tests and documents listed in the DCP.

Out of scope: any other module's business rule, any other page redesign, any second Source of Truth,
any implicit approval/versioning under a new name, any database rebuild or Flyway repair.

## 5. Acceptance assertions (must hold, not adjustable to force PASS)

1. No QC standard endpoint requires approval, signature, `ebr:sign`, or a version identity; the
   approve/retire/submit paths are unmapped.
2. `qms:specification:approve` / `:retire` no longer exist; `view/create/edit/delete` do, and each
   endpoint returns 403 without its own permission.
3. Editing a standard keeps its id, bumps its record token, and never changes the record token of a
   document that froze it.
4. One standard can be attached to many materials and many products; a standard cannot be selected
   for a material/product outside its scope, and an `INACTIVE` standard cannot be selected at all.
5. 标准的依据 is mandatory and restricted to `QC_STANDARD_BASIS`; applicability requires at least one
   subject.
6. Item value rules (numeric bounds/unit vs text criterion, precision, case-insensitive unique code)
   fail closed with `VALIDATION_ERROR`.
7. Unknown JSON fields (including any approval/version field a client might replay) are rejected.
8. Deleting a standard removes the entry and keeps its maintenance trail.
9. A frozen incoming request / production plan / finished request keeps its stored snapshot and
   digest after the library entry is edited or inactivated.
10. `vue-tsc` clean; the QC model, picker and trace presentation tests pass; the 5173 page shows the
    new fields with labels and controls on the same horizontal line.

## 6. Evidence

Recorded verification: see
[QC quality standard basic library — contract](../qc-standard-basic-library-contract.md) §7 and the
`MES_TASKS.md` entry. Runtime status is maintained only in `MES_TASKS.md`; this record is the design
lock, not a status board.
