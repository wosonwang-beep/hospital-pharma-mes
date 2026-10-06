# WMS request and management implementation plan

Execution: native in this session, as authorized by the user (“确认” after the detailed-contract review). Use executing-plans and targeted red/green verification. No automatic commit/push. Preserve unrelated changes and local configuration.

Spec: [DCP-WMS-REQUEST-INVENTORY-RETURN-001](DCP-WMS-REQUEST-INVENTORY-RETURN-001-APPROVED-SCOPE.md).

## Constraints / review focus

Java21, existing modular boundaries, native persistent MariaDB, UI V2/T1–T6; no historic migration edits or physical business deletion. Mixed units, concurrent confirmations, stale/replayed commands, historical NULL linkage and cross-org lookup are mandatory targeted cases. Request quota is gross confirmed issuance; charge entitlement stays net of returns. Inventory totals are lot-level and never pretend location reservation allocation. Authoritative release publication is separate from runtime acceptance.

## Tasks and verification

1. Publish approved additive v1.0.19 contract and consistent authority pointers; preserve parent hashes and human acceptance history. New V029 only after successful native history 028 read (verified 2026-10-06).
2. Domain/Flyway: `MaterialRequestRules`, `MaterialRequestEntity/ItemEntity` and mappers; new request tables plus nullable issue links and request permissions. Red/green `MaterialRequestRulesTest` proves quota/state/quantity behavior. V029 is append-only.
3. Application: `MaterialRequestService` consumes production frozen identity and unit-conversion ports; creates/edits/submits/cancels retained requests, resolves detail and totals. `WmsProductionService` locks request before issue for linked mutations, validates lines/quota, atomically advances state and audits both. Existing unlinked commands remain supported.
4. Read/API: `WmsManagementQueryService` projects lot inventory and existing return history; extend frozen production context read adapter. `WmsManagementController` and existing issue query expose approved filters/DTOs; closed input/state validation and permission enforcement retained.
5. UI: `WmsManagementListView`, `MaterialRequestDetailView`, `IssueReturnCreateView` and request picker; existing `IssueDetailView/WmsListView` gain request/context display, stable queries and exact linked payloads. Menu/routes match approved five-entry order, use T1/T2/T3 and existing global styling. No fake KPIs/export/status.
6. Targeted gates: affected domain tests and backend compile, safe rollback native `WmsRequestManagementIT` plus direct issue/return regression, frontend typecheck/affected unit/build, PC Chromium screenshot and console checks. Fix CRITICAL/HIGH review findings.
7. Record acceptance evidence and RTM execution, migration success, debt and READY FOR ACCEPTANCE in MES_TASKS.md; accepted original MES statuses stay unchanged.

Progress is recorded below, not inferred from candidate contract tests.

## Ledger

- Startup: detailed design confirmed; current native successful physical version 028. Existing rename work and ignored installed skills preserved.
- Shared interfaces: requests and issues share batch-first locking and immutable frozen formula identity; query adapters must not introduce a WMS→production module cycle.

- Contract publication complete: approved additive v1.0.19 documents, eight operations, permission/UI/RTM/migration mapping and governance pointers consistent. All119 parent v1.0.18 file hashes preserved; candidate is review history only.
- Database/domain complete: append-only V029 applied to native persistent MariaDB, highest29/success29/failure0/checksum1763109818. Five request permissions; three domain rules PASS. No repair or historical migration changes.
- Application/read/API complete: immutable request identities, retained edit lines, audited controlled submit/cancel and gross issued fulfillment; atomic linked issue quota and existing unlinked flow preserved. Lot inventory and immutable return projection preserve exact units and true source facts. Eight scoped endpoints enforce organization/permissions and controlled invalid-input responses.
- UI complete: five ordered WMS entries, T1 lists, T2 request/return commands, T3 request detail, optional linked issue allocation with partial lines and multiple lots. Existing request/issue identities and legacy payloads preserved. PC query picker sizing corrected; edit discard reloads server facts.
- Independent wms_final_review returned CHANGES REQUIRED: current-read cancellation safeguards and partial/split UI allocation were HIGH; both corrected and targeted verified. Zero-stock reservation integrity and invalid-date400 findings also corrected. Native stale cancellation already encountered a controlled concurrency conflict; no cancelled-with-linked-issue reproduction is claimed. No second independent review claimed.
- Final gates PASS: backend domain3, native integration13 distinct cases (management10, concurrency2, existing unlinked signed-flow regression1), affected frontend unit5, typecheck, production build and PC Chromium7. Ten1440x900 screenshots, no page/console error or horizontal overflow. Browser fixtures are API-mocked; actual backend/transaction/source trace verified separately on native MariaDB. No full regression or hosted CI claim.
- Final evidence/RTM/review recorded in docs/acceptance/wms-request-management-2026-10-06. MES_TASKS.md alone records READY FOR ACCEPTANCE; original ACCEPTED MES tasks unchanged. Remaining MEDIUM: tenant-wide/N+1 projection paging; inherited LOW build chunk warning. User-owned local configuration and prior rename changes retained. No commit/push, next task, unapproved workbench gaps or UI V3 implementation.

- Human acceptance on 2026-10-06: “确认验收，提交和推送”. WMS maintenance and bundled outbound naming recorded ACCEPTED in MES_TASKS.md; commit/push to main now explicitly authorized. Previous no-commit statements retain development history. No Frozen Release content changed for acceptance.
