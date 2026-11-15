# DCP-QC-SPEC-BASIC-LIBRARY-001

QC quality standards become a basic information library. Approved by the human on 2026-11-02.

## Approval and bounded scope — 2026-11-02

The human stated (2026-11-02): 检验标准只做为一个基础信息库维护，增删改查就可以，不涉到审批和批准。不需要做版本的管理。标准基础信息有一个标准的依据，这个很重要的信息。还有标准适用于那些物料和产品，也需要管理。

The human then explicitly selected, in the design question of the same date:

1. **Scope** — `方案 1（推荐）：标准库全量重构 + 下游全部改为「标准快照」冻结（授权删除历史签名证据）`.
2. **Items** — `保留检验项目（推荐）`.
3. **Applicability shape** — `一个标准可以同时挂多个物料和多个产品（推荐）`.

This is explicit Design Authority authorization, under AGENTS.md `Authorized Design Change Governance`, to change frozen QC fields, tables, state, APIs, permissions and GxP controls **only** within the bounded scope below. It supersedes the QC-specific rules of `incoming-approved-qc-spec-contract.md`, the QC portion of `DCP-INCOMING-QUALITY-GAPS-001-PROPOSED.md`, and MES-008A QC specification controls. Everything else in FINAL BASELINE v1.0.23 stays immutable.

### Authorized changes

- **No approval, no electronic signature, no version management** for QC quality standards. Version creation, approval and retirement commands, their permissions `qms:specification:approve` / `qms:specification:retire`, the `ebr:sign` requirement, the independence rule, the content-hash/signature verification and the frozen snapshot signable providers are removed.
- **Historical signature evidence deletion is authorized.** `qc_specification_version` and the version-scoped `qc_specification_item` rows, together with the `gxp_signature` rows and audit events that exist only to evidence QC standard approval/retirement, are deleted by the new physical migration. This is the one destructive GxP deletion the human explicitly approved. No other table's history, and no other module's evidence, may be deleted.
- **New standard root fields**: 标准的依据 `basis_type` + `basis_reference` (+ optional effective date, optional remark, status). 标准依据 is mandatory and must record the legal/registered/internal source of the limits.
- **Applicability is now many-to-many** through `qc_specification_scope`: one standard can apply to many materials and many products. The legacy single `material_id` / `product_id` / `subject_type` columns and their triggers are removed.
- **All inspection items belong to the standard root** (`qc_specification_item.specification_id`). Items keep their existing shape, result type rules and one row-identity rule per code.
- **Downstream freezing moves from "version id" to "standard id + frozen standard snapshot"**. Incoming inspection requests, production quality plans, finished inspection requests and traceability store the standard id plus an immutable snapshot JSON captured at the moment of freeze. A downstream document never re-reads a live standard to prove its own content.

### Explicitly NOT authorized

- Any change to non-QC modules' business rules, states, permissions or routes beyond the mechanical QC reference rename described above.
- A second approval/version mechanism under a different name.
- Redesigning the reused downstream pages (来料请验单、生产质量计划、成品检验、可追溯) beyond what the reference change requires.
- Deleting anything else in the DEV database, repairing Flyway history, or rebuilding the database.

## Current-definition contract

A QC quality standard is one mutable current definition. There is no draft/approved/retired cycle. Editing a standard's header, applicability or items is a direct serialized row-locked update with a compare-and-swap record token; no client approval is required. Validation, organization isolation, transactions, idempotency, audit events and the GxP rule that a frozen downstream document keeps its own snapshot remain mandatory.

Historical audit events for create/update/delete of a standard remain recorded (`QC_SPEC_CREATED` / `QC_SPEC_UPDATED` / `QC_SPEC_SCOPE_CHANGED` / `QC_SPEC_ITEMS_CHANGED` / `QC_SPEC_DELETED`); they are informational audit, not approval evidence. The maintenance trail `qc_specification_revision` records CREATE/UPDATE/DELETE with before/after state and survives entry deletion.

## Governance and verification

This approved supplement is bounded authority for the change; frozen v1.0.23 and prior releases are immutable. Database, domain, API/OpenAPI, UI/permissions, tests, migration and consumed/produced contracts are synchronized in this release. The formal schema change uses the next physical Flyway migration (V070) after DEV history; no repair, clean or rebuild.

Runtime status belongs only in `MES_TASKS.md`. Passing unit tests is insufficient for acceptance; the existing 5173 frontend must be inspected against the source-backed 8080 backend. Preserve unrelated working-tree edits and local credentials. No commit/push and no automatic human acceptance are authorized.

## Synchronized artifacts

- [QC quality standard basic-library contract](qc-standard-basic-library-contract.md) — database, domain, API, permissions, UI and integration contract.
- [QC standard basic-library OpenAPI supplement](qc-standard-basic-library-openapi.json).
- [Design confirmation record](decisions/QC-STANDARD-BASIC-LIBRARY-DESIGN-LOCK.md).
- [Task index](../../MES_TASKS.md) is the only runtime status record.
