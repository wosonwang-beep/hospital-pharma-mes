# DCP-BASIC-NO-AUDIT-VERSION-001

Business terminology correction approved by the human on 2026-10-09: the public name is **生产工艺**, never 工艺包. Menu, page titles, fields, permissions display and production/eBR selectors use 生产工艺. Existing ProcessPackage / processPackageId / packageCode identifiers, routes, permission codes and historical evidence remain compatible.

## Approval and bounded scope — 2026-10-09

The human explicitly approved removing both frontend and backend audit and version controls from every function under 基础管理. The human then selected: “扩大授权至生产和eBR，重设计绑定契约，完全取消后台工艺版本；保留历史证据”. This supersedes the audit/optimistic-version requirements and automatic process revision creation in DCP-DATABASE-MENU-AND-USABILITY-001 only for this scope.

Affected basic functions: materials and their supplier relationships, suppliers, organizations, units, unit conversions, equipment, personnel qualifications, products and process packages. Authentication, authorization, organization isolation, validation, transactions and idempotency remain mandatory. Mutable basic records use serialized row-locked updates without client version preconditions or version increments. Historical audit/signature records and existing version counters are retained without rewriting.

Process maintenance becomes one mutable current definition per package. No process version creation, selection, comparison, approval or publication workflow remains. Existing proc_package_version rows and their definitions are historical evidence and are read-only. New current definitions have stable package/formula/route/item/operation/parameter identities; updating current configuration must never mutate the legacy definitions or previously frozen batch snapshots.

Production and eBR bind new work to the current process package, not a process version. Release freezes the actual current definition into the existing immutable batch evidence boundary. Historical batches/templates continue using their original binding and evidence. eBR template versions, runtime audit/signatures, production concurrency/idempotency, WMS inventory/lineage and QA release controls remain regulated. Existing downstream item/operation identities remain valid.

## Governance and verification

This approved supplement is bounded authority for the change; frozen v1.0.23 and prior releases are immutable. No authoritative baseline pointer is switched before cross-document consistency PASS. Database, domain/state, API, UI/permissions, RTM/tests, migration and consumed/produced contracts must be synchronized in this release. Formal schema changes use the next physical Flyway migration after DEV history; no repair, clean, deletion or database rebuild.

Implementation and validation status belongs only in MES_TASKS.md. Passing mock/component tests is insufficient for real-page acceptance; inspect the existing 5173 frontend against the source-backed 8080 backend. Preserve unrelated edits and local credentials/configuration. No commit/push or automatic human acceptance is authorized.

## Synchronized artifacts

- [Domain, database, state, API/UI, permissions, integration, RTM and CI contract](../architecture/BASIC_CURRENT_DEFINITION_CONTRACT.md).
- [Bounded OpenAPI supplement](../contracts/BASIC_CURRENT_DEFINITION_OPENAPI.json).
- [Cross-document consistency review](../review/DCP_BASIC_CURRENT_CONSISTENCY_REVIEW.md).
- Physical append-only migrations: V041 current process/bindings; V042 retired process publish permission.
- [Task index](../../MES_TASKS.md) is the only runtime status record.
