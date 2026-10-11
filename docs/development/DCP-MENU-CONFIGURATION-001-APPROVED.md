# DCP-MENU-CONFIGURATION-001 Approved Supplement

Approval: 2026-10-10 16:59 +08:00, human replied “确认” to adding permission description and atomic menu configuration save. UI source approval and initialization authorization are recorded in decisions/MENU_FUNCTION_DESIGN_LOCK_20261010.md. Frozen v1.0.23 remains unchanged; this bounded supplement applies only to menu configuration. No new role grants, permission renaming or GxP behavior.

## Database / Migration / Initialization

DEV read gate: localhost:3306/hospital_pharma_mes_dev, highest successful physical version50, 50 applied, 55 menus, 204 permissions. New V051 only: sys_permission.permission_description VARCHAR(512) NULL; old descriptions NULL. Seed missing menu-function links using approved existing view/resource permissions only, never creating DEMO routes/permissions or role grants; never overwrite menu names/status/routes or clear existing associations. V037 and subsequent seeds remain the authoritative menu/permission initial data, V051 is additive. No rebuilding, repair or old checksum changes.

## Domain / State / API / Integration

Permission requests/responses gain optional description (max512, no control characters). Old Java constructors and old HTTP requests remain compatible; omitted update description preserves existing value, empty string clears it. Permission codes/types/routes/status/version and audits remain.

POST /api/v1/menus/configuration creates; PUT /api/v1/menus/{id}/configuration updates. Body: menu (existing CreateMenuRequest fields; update menuCode immutable), permissionCodes (complete menu bindings), permissionChanges [{id nullable, version nullable for new, permissionCode, permissionName, description, status}]. Maximum100 codes/changes; duplicate codes rejected, changes must be in submitted bindings. Existing permission update needs id/code match and expected version; new permissions are ACTION with null route. One outer transaction includes menu, permission definitions, links, audit and existing idempotency replay. Update If-Match applies to menu; each changed existing permission has its own version. Any failure rolls back all writes, including audits.

Entry checks existing iam:menu:create/update. Binding requires iam:permission:view; new definition additionally iam:permission:create, editing definition additionally iam:permission:update. Menu is scoped to current org; roles read only through existing /roles permissions. Unbinding never deletes global permission or role grants. Directory cannot have bindings or changes; existing view permission must remain bound for a page. Ordinary permissions use existing CRUD classification, never include sensitive approval/sign/release in automatic management. No new permission codes.

## UI / Routes

T1 /admin/menus follows frozen menu prototype and full-width 1px section divider. T2 /admin/menus/create and /admin/menus/:id/edit follow approved whole-page menu configuration. Fixed ordinary two-level section, special definition draft modals, read-only role tab, shared bottom save/cancel, dirty-leave guards. Real id/version/catalog replace DEMO; dynamic existing route meta identifies the real view permission for a new page, preserving existing extra requirements. Missing metadata rejects creation, not guessed permissions. Existing read-only view action uses /admin/menus/:id with iam:menu:view. All labels horizontal at1440/1280/900/375; tables scroll locally. No localStorage business persistence.

## Test / RTM / Dependencies

MENU-CONFIG-01: description create/update/old API compatibility; MENU-CONFIG-02: atomic rollback, version conflicts, duplicate validation; MENU-CONFIG-03: permissions/org isolation/idempotency; MENU-CONFIG-04: unlink preserves role grants; MENU-CONFIG-05: seeds preserve custom data and have no grant mutation; MENU-CONFIG-06: T1 search/reset/tree/paging, T2 create/edit/save/reload/cancel/modal/tab, readonly/errors/denied; MENU-CONFIG-07: four viewport DOM and screenshot comparison. Backend rollback tests and real5173/8080 browser/API required; mocks only unit checks.

Dependencies: existing system/security/audit/idempotency/navigation/dictionary infrastructure. No new module, cross-module cycle or production-state dependency. Current task readiness only MES_TASKS.md; prototype confirmation is not final ACCEPTED.

## Implementation Scope / Budget

Whitelist: mes-system IamContractService/IamContractController/SysPermissionEntity/SysPermissionMapper; mes-security SecurityConfiguration new exact matchers only; mes-boot V051 and MenuConfigurationIT; frontend MenusView/MenuDetailView/menuConfiguration.css/menuConfigurationModel(.test).ts, api/http.ts description type only, router/index.ts menu routes only; affected tests/e2e and evidence; approved contract/OpenAPI/UI appendices/design lock/review/PROJECT_BASELINE/MES_TASKS.
Complex schema/transaction/frontend integration: independent30-minute wall-clock budget starting2026-10-10 17:03 +08:00; max2 automatic repairs; same failure after targeted repair stops. No ordinary UI budget reset, no recursive work. Preserve dirty files, local config and all data; no commit/push.

Detailed OpenAPI: ../api/menu-configuration.openapi.yaml. Cross-document consistency must be recorded before authority pointer use; runtime gates recorded separately, never inferred from this document.

Approved UI/API test migration: existing MenusView tests describe retired drawer/modal and partial multi-request binding. Before executing new tests, migrate those cases to approved independent T2 and single aggregate API, preserving name validation, If-Match, conflict draft retention and retry/no-duplicate assertions. The old partial-success expectation is superseded by atomic rollback, not weakened; do not change assertions in response to a failing new test.

2026-10-10 17:26 additional human authorization: fix shared narrow-screen header duplication and local page-refresh session recovery, then continue acceptance. Bounded extra source files: frontend/mes-web/src/layouts/dashboard-shell.css and vite.config.ts. Preserve approved menu business design and all server-side rights/cookie/same-origin rules; no local database configuration changes. Original17:33 budget end and max2 implementation repairs remain.
