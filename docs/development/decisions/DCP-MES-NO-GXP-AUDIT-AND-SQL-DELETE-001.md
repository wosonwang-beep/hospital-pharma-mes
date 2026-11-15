# DCP-MES-NO-GXP-AUDIT-AND-SQL-DELETE-001

Status: Explicit user decision, 2026-11-11. Applies to all deployments, including formal deployment. This decision supersedes conflicting GxP audit/retention requirements in FINAL BASELINE v1.0.23 and prior engineering rules.

## User-approved global requirements
1. No GxP audit event logging, audit trail query, audit trail UI, or audit-trail export.
2. Remove the gxp_audit_event table and audit:view permission/API/UI.
3. No database triggers in any deployment. Add a forward-only Flyway migration to DROP all existing triggers. No CREATE TRIGGER statements in this or future migrations.
4. All business data may be deleted by direct SQL DELETE. Keep ordinary foreign keys for referential integrity; delete child rows before parents. No delete-blocking triggers, append-only rules, or hidden application deletion guards.
5. Do not automatically create persistent placeholder business data or retained test fixtures in the business database.
6. Electronic signatures remain a separate business approval capability for now. They are not written to a GxP audit table and their rows are ordinary SQL-deletable data. Do not silently remove signature UX or approval semantics in this DCP.
7. Existing migrations are immutable. Add a new migration after V076. No later migration may introduce triggers.
8. Do not clear all existing business data in this DCP. Remove only the GxP audit event table and audit:view feature. Other operational records remain unless separately instructed.

## Implementation scope
- V077 drops all triggers present in V076; removes audit:view role/menu links and permission; drops gxp_audit_event.
- Backend audit writers become no-op compatibility adapters; remove audit query API and all audit event reads/writes from signature/archive paths.
- EBR role validation occurs at signature time and no longer depends on gxp_audit_event.
- Frontend removes audit route/API/dashboard feed and active audit links.
- Replace audit-dependent tests with state/authorization/signature assertions; assert zero triggers after migration.
- Update AGENTS.md, MES_DEV_RULES.md, PROJECT_BASELINE.md and MES_TASKS.md.

## Acceptance
- Fresh install and upgraded DEV database finish at V077 with zero triggers.
- gxp_audit_event and audit:view do not exist; /api/v1/audit-events is absent.
- No application path inserts or queries GxP audit events.
- Direct SQL DELETE works when FK children are deleted first.
- Normal business workflows and separate signature capability remain intact.
- No database trigger is created.
