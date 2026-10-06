# Finished goods controlled chain implementation plan

Execution: superpowers:executing-plans in this session, under explicit design-change/implementation approval. Spec: DCP-FINISHED-GOODS-CHAIN-001-APPROVED.md. Native root workspace retained to honor persistent database/local environment; isolated named branch codex/finished-management-v1-0-20 preserves main. No automatic commits/pushes.

Goal: complete the approved production→request→warehouse→quality→QA→shipment chain without duplicate stock or regulated fact replacement.

## Constraints / interfaces
WMS uses boot production/effective-decision ports, QMS consumes WMS read evidence and existing production QC, QA consumes QMS gates, trace consumes source query ports. Locks order→batch→business record→lot/ledger, current reads in mutation transactions. Java21/Spring/MyBatis/MariaDB localhost hospital_pharma_mes_dev; UI V2/T1–T6. V030 follows verified29; historic migrations/releases immutable. Permission before replay, exact units/version/idempotency/signatures and source digest. Original MES tasks remain ACCEPTED; new maintenance separately IN PROGRESS.

## Task 1 — warehouse ownership
- [x] Add meaningful domain/native RED cases: OUTPUT no physical stock; completed independent request/confirm adds once; replay/legacy exact quantities.
- [x] V030 new normalized FK/check/profile/immutable schema and WMS request/shipment mappers; scoped domain rules/services/controllers/permissions.
- [x] GREEN affected rules/compile/native lifecycle, rollback-safe fixtures. Command logs summarized; no schema repair.

## Task 2 — finished quality
- [x] RED post-completion request+sampling provenance, frozen results-derived report, invalid/FAIL/OOS/independent/stale approval.
- [x] QMS request/sampling/report/review entities, read/command service, signature provider, closed API. Existing production samples/tests/results remain sole source.
- [x] GREEN native positive/negative cases and direct result/review regressions.

## Task 3 — QA and shipment / trace
- [x] RED missing receipt/report prevents release; atomic signed valid release; shipment stock/expiry/permission/concurrency/idempotency/reverse trace.
- [x] Extend QA/eBR source gates and actual report reference. Signed WMS shipment, current effective decision port, full-source trace integration.
- [x] GREEN native complete chain plus relevant existing QA/archive fixtures updated to produce actual new evidence.

## Task 4 — UI
- [x] RED targeted PC routes, truthful fields/references/actions, exact command/version/signature payloads.
- [x] T1/T2/T3 finished WMS/QMS pages and menus, real selectors; existing QA T6/T4 source links only.
- [x] GREEN typecheck/affected units/build and targeted Chromium; save actual page screenshots/no overflow/page errors.

## Task 5 — publication / final review
- [x] Cumulative v1.0.20 with reconciled DB/domain/state/API/OpenAPI/UI/permissions/test/RTM/integration/migration/task artifacts. Parent hashes unchanged; all refs and authority pointers consistent only after PASS.
- [x] One fresh independent final review per executing-plans; fix CRITICAL/HIGH with targeted RED/GREEN, ledger minor debt.
- [x] Evidence/RTM/migration summary and READY FOR ACCEPTANCE in MES_TASKS.md. No next task/commit/push/implicit acceptance.

## Ledger
- Startup2026-10-06: explicit four-gap design-change approval, prior audit reviewed. Native highest29/success29/failure0. Existing local configurations and installed skills retained. Source interfaces and ownership reviewed; warehouse receipt replaces new OUTPUT stock effect, legacy ledger immutable; QMS finished report independent of incoming report and actual raw results remain sole truth.

- Tasks1–4 complete: database/domain/commands/repositories/services/API/signatures/UI executed in plan order; meaningful OUTPUT RED→GREEN and immutable FAIL aggregation RED→GREEN. V030→V031 additive preservation migration after successful30; neither executed file edited.
- Ruling: retained native root checkout on isolated branch — AGENTS requires same root .env/native database — no copy/rebuild needed; risk is shared checkout, so unrelated dirty config/skills preserved and no automatic commit.
- Ruling: sampling signature targets requestId:expectedRequestVersion before append-only record generation — binds one authorized version and full signed created record — no guessed future DB ID.
- Ruling: finished report independent FK rather than incoming report ID reuse — prevents semantic conflation and preserves historic decisions — new nullable FK in approved scope.
- Ruling: public request child collections permission-filtered, complete internal evidence separate — dedicated view scopes enforced without actor-dependent digest.
- Final independent review2HIGH fixed/targeted verified; graph MEDIUM additionally fixed. Remaining MEDIUM two committed race/load evidence and projection paging performance recorded in acceptance REPORT.md.
- Targeted rules2/native new11 + affected QA32/archive1/schema2/OOS1 PASS across scoped gates; frontend units7/typecheck/build PASS; PCChromium4/15screenshots PASS. No full regression or implicit acceptance.
- Candidate v20 consistency PASS; parent126 hashes unchanged. Publish current pointers only after recorded cross-document review, preserve prior releases. Maintenance READY FOR ACCEPTANCE in sole index.
