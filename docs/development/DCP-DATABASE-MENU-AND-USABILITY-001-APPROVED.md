# DCP-DATABASE-MENU-AND-USABILITY-001

## Authorization and scope — 2026-10-08

Approved by the human user in this conversation: directly modify identified problems; unify homepage and business-page menus; store maintainable hierarchical menus in the database; assign menu visibility and ordinary/special functions together; use two ordinary permission levels; remove operator-entered change reasons and review/approval from basic data/system management; process packages activate directly; remove list-heading subtitles and sidebar footer; compact/move login panel; searchable selections in the input with no secondary folded lookup; centered simple-edit modals; audit usability and visual quality, not just technical availability. No commit requested. Existing real business records and historical audit/signature evidence are preserved.

## Bounded contracts

- V037: persistent menu hierarchy and menu-function definitions; existing grants preserved; authenticated GET /api/v1/auth/navigation provides one filtered shell source. /menus CRUD and POST /menus/{id}/permissions maintain configuration. Role assignment carries explicit menuCodes plus permissionCodes atomically; guards and organization isolation remain.
- V038: proc_package_version.activation_mode defaults to INDEPENDENT_APPROVAL for all historical versions. New versions use DIRECT; validated DRAFT/SUBMITTED versions publish to EFFECTIVE with canonical hash, timestamp, audit and existing publication event, without approval signature. Historical APPROVED versions retain signature validation. EFFECTIVE content remains immutable; existing snapshots and signatures remain unchanged. Obsolete submit/approve APIs removed and catalog grants disabled, without deleting historical role grants.
- Supplier maintenance saves qualification as usable directly, retaining validity-date gates and audit. No bulk supplier status change.
- Basic-data configuration reason is generated automatically; actor/time/before/after audit remains. Quality decisions, actual production, inventory, weighing and frozen historical releases are outside this simplification.
- Existing Global UI V2/T1–T6 remains the authority; this supplement changes approved interaction patterns, not a V3 baseline. T2 simple editing uses centered dialogs. Complex workflows keep their full-page context.

## UI review criteria

Review each actual browser flow for rational steps, visual consistency, understandable information, ease of operation, logical information order, and layout/typography. Check search/select/clear/no-result/dependent filters; edit/save/cancel/error feedback; hierarchy/default grants; desktop and smaller viewport. Technical unit/build/API passes alone cannot close visual review. No global acceptance without human confirmation.

## Validation and runtime status

Targeted unit/rollback integration tests and real-browser evidence are recorded in MES_TASKS.md. Configuration backups remain outside the repository. Flyway applied migrations are immutable; no repair/reset, no data deletion or global status rewrite. Preserve unrelated user edits and frozen releases. Do not represent unverified flows or outdated approval tests as passed.

## Additional explicit authorization — 2026-10-08

Human requested removal of all basic-information version-control operations. The UI no longer exposes version numbers, switching, comparing, copy/new-version or separate activation. PUT /api/v1/process-packages/{id}/current-definition requires process:package:edit and atomically validates/saves/activates the current configuration. Internal immutable rows preserve consumed historical snapshots; this is automatic persistence, not an operator version workflow. Existing optimistic locking, audit, idempotency, organization scope and frozen historical records remain. No business-data save is performed by browser validation.
