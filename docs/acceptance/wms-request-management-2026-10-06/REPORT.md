# WMS request and management closeout — 2026-10-06

Current authority: FINAL BASELINE COMPLETE v1.0.19, approved DCP-WMS-REQUEST-INVENTORY-RETURN-001. Scope approval “批准并补齐” and detailed-design confirmation “确认” authorized this additive implementation. Human runtime acceptance was subsequently explicitly given by “确认验收，提交和推送” on 2026-10-06. Current status is maintained only in MES_TASKS.md.

## Implementation

- Ordered warehousing entries: 原辅料收货记录 → 库存管理 → 领料申请 → 出库管理 → 退料管理. Receiving remains the existing capability; no separate inbound-management aggregate.
- Formal request root and retained lines bind the active production batch's immutable process snapshot and formula identities. Controlled create/edit/submit/cancel commands use org isolation, optimistic versions, reasons, platform idempotency and audit. Requests do not create reservations or acquire a new approval/signature workflow.
- Existing MaterialIssue supports nullable request/line linkage. Confirmations preserve QA eligibility, BOM, reservation and stock gates; gross issued quantities advance submitted requests to partial/full completion atomically. Linked UI supports selecting a subset and splitting a line into lot allocations. Existing persisted line identities cannot be reassigned or removed by these new controls.
- Lot inventory aggregates true ledger balances and existing outstanding reservations after charges. Location filters match actual nonzero placement without truncating lot totals or inventing location reservation allocation. Zero stock with outstanding reservation fails integrity validation.
- Independent return entry/history uses existing immutable IssueReturn facts and command. Event unit and original-unit cumulative/remaining quantities remain distinct. A return neither reopens gross fulfilled demand nor double-increases stock. Weighing, independent verification and charging stay in MES.
- UI inherits Global UI V2 and explicitly selects T1/T2/T3. No fake KPI, export, return number/status or unsupported action was added. The supplied board guides PC composition; controlled source-of-truth quantities and states take precedence over illustrative values.

## Migration and baseline consistency

V029__wms_material_request_management.sql was allocated after reading successful native physical history 028 and applied through Flyway. Final native read: highest29, success29, failure0, V029 checksum1763109818; five request permissions seeded for SYSTEM_ADMIN only. No executed migration was edited or repaired. Prior v1.0.18's119 files match their recorded byte hashes. New authority pointers, OpenAPI, UI/permission/RTM matrices and physical mapping are consistent; parent frozen releases remain history.

## Verification

Evidence: [machine summary](verification-summary.json), [requirement execution mapping](RTM.md), [review disposition](REVIEW.md).

- Backend domain rules3 PASS; native integration13 distinct methods PASS: ten management methods, two genuine physical-transaction concurrency methods and one existing unlinked incoming/production regression.
- Real native chain: existing receiving/QC/QA → request/reservation → linked outbound → actual weighing → independent electronic signature → charge → bounded unused return → original source trace. QUARANTINE and QC PASS before QA release remain blocked. Issue/return alone leave physical stock unchanged; actual charge deducts it.
- Native cases include scoped HTTP permissions and cross-org denial, closed input, invalid dates, stale/replayed/changed-key commands, retained identities, exact unit conversions, partial/full/over-issue, multi-location/multi-batch balances, zero-stock integrity and concurrent cancellation/confirmation.
- Frontend affected unit tests5 PASS; typecheck and production build PASS. Targeted Playwright Chromium PC cases and screenshots are listed in the machine summary. API-mocked browser fixtures verify real Vue interaction/payload/layout; they are distinct from real native backend integration. No page/console errors or document horizontal overflow in these checks.
- No unrelated full regression or hosted CI was run. Existing global bundle-size warning remains.

## PC screenshots

1440×900 viewport; full-page capture when content extends beyond it:

- [Inventory](screenshots/inventory-desktop.png)
- [Request list](screenshots/request-list-desktop.png), [create](screenshots/request-create-desktop.png), [detail](screenshots/request-detail-desktop.png), [edit](screenshots/request-edit-desktop.png)
- [Outbound list](screenshots/issue-list-desktop.png), [linked create](screenshots/issue-create-linked-desktop.png), [detail](screenshots/issue-detail-desktop.png)
- [Return history](screenshots/return-list-desktop.png), [execute return](screenshots/return-create-desktop.png)

## Remaining debt and boundaries

MEDIUM: tenant-wide read projections perform repeated reference enrichment and in-memory pagination; SQL-level pagination/batched enrichment needs a bounded follow-up. This is recorded rather than silently expanding the authorized implementation. There are no remaining identified CRITICAL/HIGH findings after targeted fixes. Independent review's original verdict and author verification are retained in REVIEW.md; no second independent review is claimed.

Retained concurrency fixtures use unique test actors and records, preserve regulated history, and are QA-rejected/inventory-blocked after every committed trial. Earlier unsuccessful trials' evidence remains in docs/acceptance/incoming-quality/evidence/native-concurrency-*.json. Rollback-only tests neither clear shared data nor modify shared seeds.

Original MES task acceptance is unchanged. Workbench image/photo/planning/SOP proposals and UI V3 remain outside scope. Local database configurations, credentials and unrelated installed-skill files are excluded from delivery. No commit/push occurred during development; the subsequent human acceptance explicitly authorizes submission to main.
