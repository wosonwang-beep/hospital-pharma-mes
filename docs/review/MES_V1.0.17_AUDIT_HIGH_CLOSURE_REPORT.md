# Audit HIGH closure — 2026-10-05

Status: READY FOR ACCEPTANCE. Explicit approval: user “批准” after review of DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001. Current business authority: FINAL BASELINE COMPLETE v1.0.17; inherited v1.0.16 is immutable. Global UI V2/T1–T6 is unchanged. Original classifications and code evidence remain in [CURRENT STATE CONFIRMATION](MES_V1.0.16_CURRENT_STATE_CONFIRMATION.md).

## Results

| Issue | Result |
|---|---|
| HIGH-01 | FIXED — material-lot guidance reflects the implemented incoming eligibility chain. |
| HIGH-02 | FIXED — issue guidance describes actual prerequisites instead of permanent unavailability. |
| HIGH-03 | FIXED — existing trace-source incoming/stock/production lineage in T4 tabs, permission-controlled source navigation. |
| HIGH-04 | FIXED — production-order product/date/status queries and navigation restoration. |
| HIGH-05 | FIXED — production-batch order/product/date/status queries; existing positive productionOrderId formally published in v1.0.17 OpenAPI and producer contract. |
| HIGH-06 | FIXED — independent frozen Order/MainBatch state enums and localized selectors. |
| HIGH-07 | FIXED — incoming read DTOs return server-derived allowedActions; nested item/execution/revision controls and root controls fail closed without actions and intersect existing operation permissions. Assigned actor, qualification, independent review, completion evidence, report digest, retest approval/quota and signing permission narrow availability. Existing transactional command checks remain authoritative for current evidence and submitted inputs. |
| HIGH-08 | FIXED — equipment identity mutex acquired in sorted ID order before material gates, current locking reads check active runs, and PAUSE/COMPLETE acquire the same mutex. Conflicting operations cannot both acquire active occupancy; all segments/audits retained. Existing paused binding appends a usage, rather than overwriting prior usage/run identity. |
| MEDIUM-05 | FIXED as direct companion of HIGH-03. |
| MEDIUM-01 | RESOLVED — 2026-10-06 minimal list Edit navigation fix; UPDATE permission + server EDIT + DRAFT, independent edit route and query context preserved. See current-state review below. |
| MEDIUM-02 | RESOLVED — 2026-10-06 existing business root pickers supplemented with supported product/material filters; exactly one root unchanged. |
| MEDIUM-03 | RESOLVED — business-domain grouping already implemented at HEAD 092b219; fresh desktop/mobile evidence confirms it. |
| MEDIUM-04 | RESOLVED — both desktop and mobile already consume the same navigationGroups at HEAD 092b219; fresh menu/permission comparison confirms it. |

## Verification

- Backend targeted unit tests: 15 PASS — IncomingRulesTest 4, IncomingRecordStatesTest 6, IncomingEvidenceTest 1, ExecutionRulesTest 2, IncomingActionPolicyTest 2. Affected modules compile on Java 21.
- Native MariaDB targeted integration: 11 distinct cases PASS — IncomingAllowedActionsIT 1; IncomingQualityIT 6 (complete chain, unhandled OOS, original FAIL protection, approved retest, rollback and org isolation); IncomingFinalContractIT 1 (audit); IncomingProductionIT 2 (production-order filter and equipment segments/predecessors); NativeConcurrencyIT equipment occupancy 1.
- Final signing-action check additionally verified that missing ebr:sign removes signed result actions, result signatures still verify, and QC/report completion remains separate from QA release. No full regression.
- Native concurrency uses separate connections and different production orders. At most one active owner is asserted, then a fresh losing command must return EQUIPMENT_OCCUPIED; PAUSE frees occupancy, occupied RESUME is rejected, COMPLETE frees occupancy, and all three ended segments remain. Native MariaDB snapshot isolation can return error 1020 on a locking read of a row inserted after the transaction snapshot. The existing CONCURRENT_MODIFICATION translation fails closed; the test does not accept a second successful start. Diagnostic evidence and unique retained fixtures are preserved; no global privilege, database rebuild, deletion or Flyway repair was used.
- Flyway validates all 26 migrations; schema version 026 is up to date. No new migration or physical schema change.
- Frontend typecheck PASS; 16 related unit tests across three files PASS (1.10s); build PASS (9.45s), inherited LOW bundle-size warning only.
- 26 distinct targeted Chromium desktop/mobile cases PASS across scoped runs: 10 existing audit query/lineage/permission cases; 12 incoming root action/permission cases; 2 nested result controls; 2 QA material-decision action cases. No unrelated full browser regression. Initial fixture permissions and Chinese two-character button spacing were corrected; no application assertion or business gate was weakened.
- Browser tests render the actual Vue app with API fixtures, not a native backend. Fixtures intentionally prove that actions are consumed from server arrays instead of inferred from status; they are not fabricated GMP records. Native integration provides the separate business/state/signature proof.
- Checked root screens have zero console/page errors and document overflow. Existing screenshots and incoming desktop/mobile screenshots were saved; representative mobile rendering was inspected. Browser plugin was unavailable, so existing Playwright Chromium was used.
- Deterministic cross-document consistency PASS: 109 parent files unchanged; exact bounded query/DTO delta; unchanged paths, permissions, UI routes, business enum schemas and executed migrations; generated DTO schemas aligned; Test/RTM mappings present. Current release hash manifests refreshed before completion.

## Evidence

- [Approved contract](../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_AUDIT_HIGH_CONTRACT_V1.0.17.md)
- [Consistency review](../../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_AUDIT_HIGH_CONSISTENCY_REVIEW.json)
- Screenshots: `C:/Users/Administrator/AppData/Local/Temp/mes-audit-high-existing/` and `C:/Users/Administrator/AppData/Local/Temp/mes-audit-incoming-actions/`.
- Scoped build/test summaries in module target reports; local command logs under `%TEMP%/mes-audit-*.log`. Native test-owned history/evidence stays in the shared DEV database and docs/acceptance/incoming-quality/evidence; no regulated history is concealed or removed.

## Review and limits

Scoped code/contract review: no remaining CRITICAL/HIGH finding among HIGH-01–08. Dynamic actions are removed recursively from audit fact decoration and never enter signature canonical evidence; availability is not an authorization or release bypass. No generic status API, new business table/state/permission/route, GxP weakening, future MES task or Phase 2B. Historical remaining debt at the 2026-10-05 HIGH closure: MEDIUM-01–04 and inherited LOW frontend bundle warning. The four MEDIUM classifications are superseded by the 2026-10-06 current-state review below; the inherited LOW bundle warning remains. Persistent unique diagnostic fixtures are retained and their material lot blocked by the existing audited QA decision.

Authority pointers in AGENTS.md and docs/PROJECT_BASELINE.md now identify v1.0.17. Existing accepted MES statuses remain unchanged. User-owned local database configuration changes are preserved. This work is uncommitted/unpushed; task readiness does not imply human acceptance.


## MEDIUM-01–04 current-state confirmation and resolution — 2026-10-06

Authority: FINAL BASELINE COMPLETE v1.0.17 + approved DCPs. Reviewed starting Git HEAD: `092b219263087b2db9e63fa8d30e6e18b21764dc`; current working-tree patch is uncommitted. The user explicitly requested HEAD-based rechecking and minimal repair of any remaining MEDIUM-01–04. No earlier task acceptance, frozen release or initial audit finding is rewritten.

| Issue | Starting HEAD finding | Current result | Code / Chromium evidence |
|---|---|---|---|
| MEDIUM-01 | STILL VALID: ProductionListView action cell only offered View. Backend already publishes EDIT for DRAFT Order/MainBatch; independent edit routes and UPDATE permissions already exist. | RESOLVED: update permission AND server EDIT AND DRAFT gate the direct Edit button. Route/query/page context retained; no inline/modal write. | [ProductionListView](../../frontend/mes-web/src/views/production/ProductionListView.vue) lines 25–26 and 43; [ProductionService](../../backend/mes-production/src/main/java/com/hospital/mes/production/application/ProductionService.java) line 79 is the unchanged producer. `orders-edit-list-*` / `batches-edit-list-*` screenshots. |
| MEDIUM-02 | PARTIALLY FIXED: UI construction already replaced primary raw-ID fields with business root pickers and keyword search, but lacked product/material filtering. | RESOLVED: reuse existing product/material name/code lookups; filter existing batch/lot lists by productId/materialId and existing keyword. Filter changes invalidate the corresponding selected root. Final GET /trace still has exactly one mainBatchId or materialLotId; product/material filters never enter /trace. | [TraceView](../../frontend/mes-web/src/views/production/TraceView.vue) lines 14–15 and 35; [IncomingReferencePicker](../../frontend/mes-web/src/views/quality/IncomingReferencePicker.vue) lines 5, 9, 17, 34; `trace-business-roots-*` and regression trace screenshots. |
| MEDIUM-03 | ALREADY FIXED at HEAD 092b219: nonempty groups Master / WMS / Production / Quality / System are derived from existing permitted routes. | RESOLVED: fresh desktop/mobile screenshots and route interaction show matching group membership and permission-hidden entries. No regrouping or new menu was added. | [AppLayout](../../frontend/mes-web/src/layouts/AppLayout.vue) lines 26–32, 50, 61; `grouped-navigation-*` screenshots. |
| MEDIUM-04 | ALREADY FIXED at HEAD 092b219: one navigationGroups configuration supplies both renderers. Repeated presentation markup is not a duplicated route/permission definition. | RESOLVED: identical desktop/mobile authorized item order verified; groups disappear for permission-limited identity. Mobile trigger explicitly uses click; no route or permission change. | [AppLayout](../../frontend/mes-web/src/layouts/AppLayout.vue) lines 26, 50, 56, 61; [focused browser cases](../../frontend/mes-web/e2e/audit-medium-current.spec.ts). Approved T5 PC reference navigation remains a protected separate presentation. |

### Verification of this increment

- Target flow: production list → permitted direct Edit → original independent edit page → original leave-confirmation → restored list query/page; Trace business root selection → existing lookup filters → exactly one unchanged trace root; grouped desktop/mobile menu → existing authorized route.
- Browser plugin not available; project Playwright Chromium renders the actual Vue application with mock responses shaped from existing contracts. This is UI/command/query proof, not live database acceptance.
- Eight distinct focused Chromium cases PASS across the final targeted runs (four flows × desktop/mobile): list Edit state/action/permission + navigation; view-only route guard; product/material/keyword roots and dual-root rejection; grouped shared menus and restricted identity. Final failing mobile menu selector was corrected for Ant Design's `ant-dropdown-menu-*` rendering. Original permission, invariant and payload assertions were retained. Existing edit-page leave confirmation is accepted by the test; no production guard was weakened.
- Eight direct companion regressions PASS (18.7s): original order/batch query and navigation restoration, existing Trace/Audit/Integration evidence and source navigation, and WMS issue selector/original command contract (desktop/mobile).
- Typecheck PASS; affected platform/App unit tests 12 PASS (2 files, 6.15s); build PASS (10.41s); existing LOW bundle-size warning retained. No full regression or database test required for this frontend-only increment.
- Visual inspection: desktop list action hierarchy, populated Trace filters on desktop/mobile, grouped mobile navigation; no page/console errors or document horizontal overflow in focused cases. Wide tables/graph scroll inside their own existing containers. Initial selector width uses the existing shared query pattern.
- Independent scoped review: no remaining CRITICAL/HIGH/MEDIUM/LOW finding in changed code; initial Trace selector-sizing concern corrected. Historical screenshots restored; fresh evidence saved separately.

### Evidence and scope

- [Current screenshot gallery](../acceptance/audit-medium-2026-10-06/REPORT.html)
- [Machine-readable evidence and result manifest](../acceptance/audit-medium-2026-10-06/evidence.json)
- Changed implementation: ProductionListView.vue, TraceView.vue, IncomingReferencePicker.vue; AppLayout.vue only adds explicit mobile click activation. One focused E2E file added.
- Backend / API / DTO / database / migration / state machine / permission / allowedActions business meaning / signature / QA release / frozen baseline: UNCHANGED. Protected inspection record/report and T5 PC structures unchanged.
- Prior local/ci database configuration changes and separate authority-pointer documentation corrections preserved. No commit/push performed in this increment. MES task ACCEPTED statuses remain unchanged; this technical resolution is READY FOR ACCEPTANCE, not inferred human acceptance.

## Human-confirmed final Closeout — 2026-10-06

The user explicitly confirms the completed Functional / UI V2 Final Closeout Audit: Functional Baseline v1.0.17 and UI V2 Implementation `CLOSED`; CRITICAL `0`, HIGH `0`, MEDIUM-01–04 `CLOSED`. This supersedes earlier readiness and uncommitted-state descriptions for current governance, while preserving their historical evidence. Task acceptance remains indexed in MES_TASKS.md. See [current Closeout](MES_V1.0.17_FUNCTIONAL_UI_V2_FINAL_CLOSEOUT.md). Unapproved DCP-WORKBENCH-GAPS-001 contracts are excluded and remain PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED; Global UI V3 is NOT IN CURRENT SCOPE. No new implementation audit or test run is claimed by this documentation update.
