# DCP-EBR-TEMPLATE-NAME-001 — Proposed

## Problem and approval boundary

2026-10-09: the human identified the missing eBR template name as key business information. Source inspection confirms the create page, list, DTO and persistence entity contain templateCode but no templateName. The prior basic-maintenance supplement does not authorize this field addition. Implementation requires explicit approval under AGENTS.md Authorized Design Change Governance.

## Concrete proposed contract

- Add templateName (模板名称), trimmed nonblank text, maximum 100 characters, required on new template creation. Names are descriptive and need not be unique; templateCode remains the existing family identifier.
- Store the name per eBR template revision. Draft editing uses the existing controlled save, permission, audit, reason, optimistic lock and idempotency. Submitted/approved/effective revisions cannot be renamed in place; a new revision inherits the name and permits draft changes.
- Creation, list, detail, designer identification and production template selection show the name together with code/revision. Existing keyword search matches name or code. Keep current UI V2 and page templates; no layout redesign or permission/route change.
- New named definitions include the name in the approved canonical definition and frozen batch evidence. Preserve the exact existing canonical representation when a legacy name is absent, so existing signatures, hashes and published templates remain valid.
- Append a Flyway migration after the highest successful physical version, adding nullable template_name VARCHAR(100) to ebr_template_version. Existing rows remain NULL. Display them as 未命名（历史模板） alongside the code; do not invent names, backfill signed records or rewrite existing snapshots/PDFs. Historical templates gain a name only through a new controlled draft revision.
- Exclude whole-book template redesign, process naming changes and removal of eBR audit/signature/revision controls.

## Required delivery and validation after approval

Update domain/database/API/OpenAPI/UI and direct production/snapshot contracts, affected test/RTM and migration/dependency references together. Record cross-document consistency review before recognizing the supplement as authority. Keep frozen releases immutable and runtime status only in MES_TASKS.md.

Validate required/blank/length handling, name search and draft edit, revision inheritance, inability to rename effective revisions, unchanged historical canonical hashes and snapshots, new snapshot name inclusion, existing concurrency/audit/signature gates, and real 5173/8080 create/list/detail/selection interactions. Use rollback or uniquely identified test records in the existing persistent DEV database. No database reset, migration repair, second environment or historical deletion.
