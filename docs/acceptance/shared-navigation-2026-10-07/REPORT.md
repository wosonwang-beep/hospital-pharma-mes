# Current expanded entry/read-contract evidence

Current authority v1.0.23. [Final scoped closeout, current validation and screenshots](ENTRY-CLOSEOUT.md) supersedes the earlier menu-only increments below. Earlier results and exclusions describe only their historical scope.

# HIGH-01 shared global navigation — verification

Date: 2026-10-07. Scope: user-approved shared configuration and seven fixed top-level menu names/order. Contract: [DCP-GLOBAL-NAVIGATION-001](../../development/DCP-GLOBAL-NAVIGATION-001-APPROVED.md). Business authority remains FINAL BASELINE COMPLETE v1.0.22 with this approved navigation supplement; Global UI V2/T1–T6 unchanged.

## Implementation and review

- `navigation.ts` is the single destination/group configuration and permission-filtering function. Normal desktop/mobile AppLayout consume it; Production Batch T4/T5 receive the same filtered objects. ExecutionReferenceNavigation contains only rendering, icons and open-state behavior.
- Approved order: 首页 → 基础管理 → WMS管理 → 质量管理 → 生产管理 → 成品管理 → 系统管理. Empty/inaccessible domains are hidden consistently. Existing normal-shell secondary grouping is retained; production-only divergent grouping is removed.
- Existing named routes and permission codes are retained. No parameterless execution destination is invented; T5 is contextual to an execution unit and highlights Production Batch where authorized. Detail/create/edit selection respects exact path boundaries.
- Browser testing exposed an existing lower-menu accessibility defect with a full permission set. Both menu renderers now scroll; the production footer does not cover entries. Actual navigation to the existing finished inbound page succeeds from normal/T4/T5 shells.
- Scoped self-review found no remaining CRITICAL/HIGH within HIGH-01. No independent subagent was started. HIGH-02 and MEDIUM-01–03 from the latest finished-page audit are not closed or implicitly implemented by this maintenance.

## Targeted validation

| Gate | Result |
|---|---|
| Typecheck | PASS (`npm run typecheck`; final build also runs vue-tsc) |
| Navigation unit tests | PASS, 3 tests / 1 file |
| New PC Chromium shared-navigation tests | PASS, 2 scenarios, 17.6 s |
| Existing affected PC Chromium grouped-menu regression | PASS, 1 scenario, 9.2 s; assertions updated for approved domain names/order and inherited finished inventory permission |
| Final production build | PASS, process exit 0, Vite 9.05 s |
| Console/page errors | 0 in targeted browser scenarios |
| Horizontal overflow | NONE in normal/T4/T5 screenshots |
| Frozen v1.0.22 checksums | 149 entries checked, 0 mismatches |
| Route declarations and migrations | UNCHANGED |

Chromium uses mocked authenticated read fixtures to verify shell rendering, permission filtering, contextual selection and navigation; this is not native backend integration or a new full-system regression. Three desktop screenshots are 1440×900. The existing regression also captures a screenshot; no mobile redesign or full mobile audit was performed.

The first full-menu browser run detected an unscrollable normal sidebar and failed; production code was corrected and both new scenarios passed. An intermediate redirected build generated output but ended with a Windows Node/libuv shutdown assertion; a subsequent standalone build passed with exit 0. No assertions were disabled and no dependencies were changed.

## Screenshots

- [Normal shell](screenshots/ordinary-desktop.png)
- [Production Batch T4](screenshots/batch-t4-desktop.png)
- [Execution T5](screenshots/execution-t5-desktop.png)
- [Affected grouped-menu regression](screenshots/grouped-navigation-chromium-desktop.png)

## RTM

| Requirement | Implementation | Verification |
|---|---|---|
| NAV-01 seven approved domains, unique destinations | navigationConfiguration / navigationHome; both shell renderers | unit order/uniqueness; Chromium full-menu scenario |
| NAV-02 one permission-filtered source | permittedNavigation → AppLayout → ExecutionReferenceNavigation props | unit denied/subset cases; restricted-user Chromium across all three contexts; affected existing regression |
| NAV-03 contextual selection and accessible actual destinations | selectedNavigationKey; shared navigate handler; menu scroll | unit path boundaries/denied highlight; actual finished-entry clicks from normal/T4/T5; screenshots/no errors/overflow |

## Impact and remaining debt

Database/migration, backend/API/DTO, business routes/state machines, permission codes and GxP rules: NO CHANGE. No frozen release content modified; local database configuration changes and skill installations remain user-owned and excluded. Existing bundle-size warning remains LOW debt. No other MES task or finished-page gap is included. Runtime readiness is maintained only in MES_TASKS.md; human acceptance has not been inferred. No commit or push performed for this request.

## Final submenu amendment verification — 2026-10-07

User explicitly fixed 基础管理 to 物料主数据 / 供应商 / 组织 / 单位 / 单位换算 / 设备 / 人员资格 / 产品管理 / 工艺包管理 and WMS管理 to 原辅料收货记录 / 库存管理 / 领料申请 / 出库管理 / 退料管理. The shared configuration now includes the existing unit-conversion route and uses 设备 as the equipment menu label. Existing permission codes and routes are unchanged. No independent equipment group exists.

Expanded navigation units: 3 PASS (includes explicit submenu order and UOM-only filtering). Latest PC Chromium shared-shell scenarios: 2 PASS, 18.5 s, actual unit-conversion navigation additionally verified from all three starting contexts; no console/page errors or writes. Latest build including vue-tsc: PASS, exit 0, Vite 8.94 s. Screenshots ordinary/T4/T5 regenerated for this amendment. Initial new navigation assertion incorrectly rejected the existing list pagination query string; changed to compare the exact URL pathname, preserving query behavior. Existing bundle-size warning remains LOW; other audit items remain outside scope.

## WMS route and quality submenu verification — 2026-10-07

WMS routes are explicitly unchanged: /wms/receipts, /wms/inventory, /wms/requests, /wms/issues, /wms/returns; backend MaterialIssue unchanged; UI term 出库. Quality shared-menu order is 请验单 / 取样记录 / 样品 / 检验记录 / 检验报告 / QC质量标准 / 生产质量计划 / 生产检验 / 质量调查. All existing permissions, routes and quality logic remain unchanged. Latest validation: expanded unit assertions3 PASS, typecheck PASS, PC Chromium shared-shell scenarios2 PASS (18.2 s), no console/page errors, no writes, no horizontal overflow. Screenshots regenerated. Git diff confirms no route/QMS/WMS/frozen-release changes. No additional build or full regression was needed for this configuration-only reorder; the preceding build result above is retained with its original scope.


Correction to earlier incremental checksum counts: the actual v1.0.22 SHA file has143 entries (not149), of which90 already differ at committed HEAD. Earlier checksum-PASS statements below must not be treated as historical-release integrity evidence. The current ENTRY-CLOSEOUT/FINAL-VERIFICATION distinguish actual150-file parent byte immutability from that pre-existing attestation issue. No historical files changed.
