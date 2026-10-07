# Shared navigation / production and finished entry closeout

2026-10-07. Approval: user explicitly approved DCP-PRODUCTION-FINISHED-ENTRY-001. Current authority: FINAL BASELINE COMPLETE v1.0.23; Global UI V2/T1–T6 retained. This report supersedes the earlier menu-only REPORT increment for the expanded entry scope.

## Implementation

All ordinary desktop/mobile, Production Batch T4 and Execution T5 shells consume navigation.ts. Seven domains share the fixed names/order. Production has five entries; finished has nine. Existing WMS paths and backend MaterialIssue unchanged; front-end term 出库. No independent equipment domain remains.

Six new T1 context entries select actual records. Execution opens an actual ExecutionUnit; balance opens existing batch balance; QA/release opens existing T6; finished inspection opens existing production-test detail after server verification of actual finished origin. Receiving reuses inbound submission/confirmation, filters SUBMITTED/CONFIRMED before pagination and displays the actual lot quality status. It creates no independent receipt entity or duplicate stock.

Five additional GET contracts expose reduced existing facts only, with organization and context permission enforcement. Original tests are selected via FinishedSamplingRecord → Sample → FinishedInspectionRequest, matching scope, source reference, batch and lot; ordinary production QC/IPC cannot enter this list merely by sharing a batch. Original attempts and controlled retests remain original records. QA read access does not silently grant generic production batch access. Release navigation requires both existing QA rights. Existing report list/details remain reachable from finished-test and request contexts.

Default finished lists/details resolve existing business numbers/names, with readable unavailable-name fallbacks instead of raw reference IDs. IDs/versions remain in collapsed audit metadata, preserving trace facts. List navigation handles stale asynchronous responses; warehouse detail returns to the receiving list when entered from there. A production-batch picker is shown only when its existing read permission is available.

No accepted incoming inspection-record/report design or T5 layout was redesigned. No new production steps, mandatory weighing, business state, permission code, signature rule, QA Gate or source-of-truth changes.

## Verification

- Backend compile/test compilation PASS; NavigationEnvelopeContractTest2 PASS (1.79s) verifies actual controller ApiResponse wrapper and closed detail-query rejection.
- Native MariaDB ProductionFinishedEntryIT: three targeted methods PASS, 21.76s; actual controlled fixture chain, organization/permission isolation, finished-origin/sample/keyword filters before pagination, IPC exclusion, receiving DRAFT exclusion and QA selection. New DTOs checked against v1.0.23 closed schemas. Unique fixtures rolled back; no shared seed edits or rebuild. Flyway validated31, current031, no migration.
- Frontend navigation3 + finished-model4 =7 tests PASS; typecheck and final build PASS (Vite11.57s).
- PC Chromium shared navigation2 PASS across ordinary/T4/T5 (29.3s combined run included an initially failing new-entry scenario); corrected new-entry scenario1 PASS (16.8s). Actual six entry paths, actual detail/workbench IDs, no default internal reference IDs, no forbidden generic batch list reads for restricted user, no mutations, no console/page errors, no horizontal overflow. Browser data are explicit API fixtures; backend facts were independently exercised by the native tests. No mobile redesign or full regression claim.
- Current v1.0.23 cross-document review PASS: five bounded new read operations, inherited OpenAPI schemas/commands unchanged, approved query extension only, routes/test/RTM/integration/migration ledger reconciled. All150 parent v1.0.22 files immutable, including its143 SHA entries. Migrations unchanged.

Intermediate failures were fixed without relaxing assertions: helper method collided with an inherited helper; Surefire mistakenly ran the inherited test class from module cwd and could not load root environment configuration; fixture attempted a second inbound request for the same batch and correctly hit the existing unique constraint; browser mock detail responses had the wrong array/review shape. Final execution used the project Failsafe root cwd and only the three selected methods. No production uniqueness or authorization control was weakened.

## RTM / current findings

| Requirement | Implementation | Evidence |
|---|---|---|
| HIGH-01 shared names/order/config | navigation.ts, AppLayout, ExecutionReferenceNavigation | navigation units and shared-shell Chromium |
| HIGH-02 real finished-family entrances | router, NavigationEntryList, existing FinishedList/Detail | native3; six-entry Chromium |
| MEDIUM-01 route matrix alignment | approved v1.0.23 route matrix and bounded read contracts | frozen consistency review PASS; v1.0.22 immutable |
| MEDIUM-02 default technical references | finished list columns and friendly detail references | six-entry screenshots and readable fallback assertions |
| PE-01 execution/balance contexts | NavigationQueryService, existing T5/T3 | native selector/isolation case; PC execution/balance |
| PE-02..06 finished receiving/test/QA contexts | FinishedGoodsService, FinishedTestReadService | native finished-origin/receiving cases; PC selectors |

HIGH-01/HIGH-02 and scoped MEDIUM-01/MEDIUM-02 fixes are ready for acceptance. MEDIUM-03 full finished-page visual refinement remains PARTIAL: T1 entry/list structure is unified; this approved read/entry DCP does not redesign every existing finished T2/T3 against a new per-page reference. Do not claim the entire previous UI audit closed.

Remaining debt: inherited tenant-wide aggregation/per-page label lookups in finished-test projection (MEDIUM, large-tenant performance); existing >500kB frontend bundle warning (LOW). No remaining CRITICAL/HIGH found by scoped self-review. No independent subagent review, hosted CI or final-system regression claim.

Runtime status: ACCEPTED in MES_TASKS.md, explicit human acceptance “验收，提交和推送” on2026-10-07. Commit/push to main is authorized. This report captures verification before submission; actual publication is verified using Git. Local DB configuration changes, .agents/ and skills-lock.json remain user-owned and excluded.

## Screenshots (1440×900 PC; full-page capture)

- [Ordinary menu](screenshots/ordinary-desktop.png)
- [Production Batch T4 menu](screenshots/batch-t4-desktop.png)
- [Execution T5 menu](screenshots/execution-t5-desktop.png)
- [Execution entry](screenshots/execution-entry-desktop.png)
- [Balance entry](screenshots/balances-entry-desktop.png)
- [Receiving entry](screenshots/receiving-entry-desktop.png)
- [Finished tests](screenshots/tests-entry-desktop.png)
- [QA review entry](screenshots/qa-reviews-entry-desktop.png)
- [Release entry](screenshots/releases-entry-desktop.png)


## Historical manifest discrepancy (outside this DCP)

LEGACY-BASELINE-SHA-01 — HIGH governance evidence issue, still open. An additional exact-byte check found90 of143 historical v1.0.22 SHA entries do not match the current files; all90 discrepancies also exist in committed HEAD (git cat-file checked all143 listed entries). This is not treated as a verified historical checksum manifest. All150 parent files remain byte-identical to the snapshot captured before this work, and git reports no v1.0.22 changes. The new uncommitted v1.0.23 candidate has a freshly verified exact-byte manifest. Parent immutability and current-delta cross-document PASS are distinct from verification of the old publication attestation.

Historical frozen metadata was not repaired, concealed or rewritten. Correcting/reattesting the historical release is outside this approved entry/read DCP and requires separate controlled scope. Current bounded entry implementation can be reviewed, but a blanket full-baseline integrity/zero-global-HIGH claim is withheld. Human acceptance covers the bounded entry implementation; historical metadata repair remains outside scope.
