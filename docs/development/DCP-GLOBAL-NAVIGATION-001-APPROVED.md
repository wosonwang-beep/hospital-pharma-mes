# DCP-GLOBAL-NAVIGATION-001 — Shared global navigation

Approved scope, 2026-10-07. The user required a single shared navigation configuration for AppLayout and Production Batch/T5, and explicitly fixed the top-level order to: 首页、基础管理、WMS管理、质量管理、生产管理、成品管理、系统管理.

This is a bounded navigation supplement to FINAL BASELINE COMPLETE v1.0.22. It does not change the frozen release, business page routes, API/DTO, database, state machines, permission codes, signatures, release gates, or T1–T6 structures. Global UI V2 remains the visual authority. No new finished-page family or other audit fix is approved by this change.

## Menu contract

| Order | Domain | Existing destinations |
|---|---|---|
| 1 | 首页 | Existing dashboard `/` |
| 2 | 基础管理 | 物料主数据 → 供应商 → 组织 → 单位 → 单位换算 → 设备 → 人员资格 → 产品管理 → 工艺包管理; existing routes and view permissions reused |
| 3 | WMS管理 | 原辅料收货记录 → 库存管理 → 领料申请 → 出库管理 → 退料管理; existing routes and view permissions reused |
| 4 | 质量管理 | 请验单 → 取样记录 → 样品 → 检验记录 → 检验报告 → QC质量标准 → 生产质量计划 → 生产检验 → 质量调查; existing routes and view permissions reused |
| 5 | 生产管理 | Production orders, main batches, eBR templates |
| 6 | 成品管理 | Existing inbound requests, inspection requests, sampling, reports, inventory, shipments |
| 7 | 系统管理 | Users, roles, trace, audit, integration |

All shells use the same destination keys, names, paths, order and permission filtering. Unauthorized entries and empty domains remain hidden; authorized domains retain the approved relative order. 首页 remains available to the authenticated user. Sidebar width, icons, grouping presentation and the approved production visual shell remain presentation concerns, not alternative menu definitions.

`src/layouts/navigation.ts` is the sole runtime navigation configuration. AppLayout computes permission-filtered groups once and passes the same groups to ExecutionReferenceNavigation; the latter contains no independently maintained destinations or permission filtering. Existing route guards remain authoritative. This replaces the production shell's divergent equipment group and process/trace/eBR placement with the existing normal-shell grouping, reordered/named as explicitly approved above.

Production execution still requires an actual execution-unit id and is opened through the production context. The old nonfunctional global execution menu item is removed; the actual T5 route and its controls remain unchanged. Batch details/create/edit and contextual T5 highlight the existing Production Batch destination only when it is visible to the user. Exact path boundaries prevent unrelated prefix matches. Long permitted menus must scroll, preserving access to the lower domains; the production footer must not cover menu entries.

## Impact and validation

Database/migration, domain/state machine, API/OpenAPI/DTO, permission codes, audit/signature/revision/lineage and integration contracts: NO CHANGE. Business routes: NO CHANGE. Only shared shell navigation and its current UI governance documentation change. Existing accepted MES tasks remain accepted; maintenance readiness is recorded only in MES_TASKS.md.

Required targeted evidence: NAV-01 approved order and unique destinations; NAV-02 identical permission filtering in ordinary/T4/T5 shells; NAV-03 contextual selection and actual finished-entry navigation, no console/page errors or horizontal overflow. PC Chromium uses explicit mocked read fixtures, not a claim of new backend integration testing. See `docs/acceptance/shared-navigation-2026-10-07/REPORT.md` for results and screenshots.

## Explicit submenu clarification — 2026-10-07

The user subsequently supplied the final nine-item 基础管理 and five-item WMS管理 submenu definitions above. 单位换算 is now a visible authorized menu destination using the already-existing master-unit-conversions route and master:uom:view permission; 设备 is its required menu label. No standalone equipment top-level group exists. All three shell contexts consume the same finalized submenus. This amendment supersedes the initial preservation of the hidden unit-conversion menu item; it does not approve other page/route changes.

## WMS route / quality submenu clarification — 2026-10-07

The user explicitly retained /wms/receipts, /wms/inventory, /wms/requests, /wms/issues and /wms/returns, backend MaterialIssue, and the UI business term 出库. Quality keeps the nine existing destinations, ordered as provided in the menu table; 质量调查 retains its existing deviations route and permission. No quality business logic, API, state transition or permission change is included.


## Superseding approved entry supplement — 2026-10-07

DCP-PRODUCTION-FINISHED-ENTRY-001-APPROVED.md and frozen v1.0.23 supersede the earlier production/finished submenu and contextual-selection exclusions. Seven shared domains/base/WMS/quality rules remain inherited. Production now has five entries, finished nine; real execution/QA/balance selectors and bounded read contracts are explicitly approved. Earlier no-new-route and Production Batch-only T5 highlighting statements above describe the original menu-only increment and no longer override this approved supplement.
