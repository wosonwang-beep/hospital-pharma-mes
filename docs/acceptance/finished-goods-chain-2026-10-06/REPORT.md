# Finished goods controlled-chain implementation / verification

2026-10-06. User approved DCP-FINISHED-GOODS-CHAIN-001, four bounded gaps. Original MES acceptance retained; current maintenance readiness is only in MES_TASKS.md. No full-system regression, hosted CI, runtime acceptance, commit or push is claimed.

Implemented: signed actual production output without physical stock → completed-production inbound request → independent signed warehouse confirmation → QUARANTINE/BLOCKED physical stock → frozen finished request → QC acceptance → signed sampling / actual production Sample → existing raw TestInstance/TestResult and independent review → derived report / independent qualified approval → eight signed QA gates → RELEASED/AVAILABLE → signed exact shipment debit → reverse graph to incoming receipt/input charge.

Native MariaDB localhost hospital_pharma_mes_dev: V030 and V031 applied; highest successful31, success31, failure0. No Flyway repair/reset/executed migration modification. Existing historical output ledgers validated and linked without duplicate receipt. All lifecycle fixtures use unique own records and roll back; read-only schema checks preserve persistent data.

## Targeted validation

- FinishedGoodsRulesTest: 2 PASS.
- FinishedGoodsLifecycleIT: all 11 new methods passed across bounded runs. Receipt/sampling/report/missing QA evidence/stale approval/stock expiry/freezing/cross-org/root-lock negatives: six-case gate PASS; independent legacy/precompletion/two-sample OOS/public child permission cases: PASS in correction/review gate; final actual shipment/complete incoming reverse graph/schema/ledger/review nodes: one-case final PASS.
- FinishedReleaseIT including inherited direct production contracts: 32 PASS after producing real new receiving/report evidence. No assertions removed to skip new gates.
- EbrArchiveIT reviewCopyReuseAndFinalChangedSourcePreserveImmutableHashHistory: 1 PASS; FinishedArchiveSchemaIT: 2 PASS.
- ProductionQualityIT immutable FAIL/authorized quota retest: 1 PASS. Actual OUTPUT/reversal and audit rollback/replay regressions PASS (also included in inherited release fixtures). Gates overlap; these are not a claimed distinct all-system total.
- Typecheck PASS; affected frontend units 2 files / 7 PASS; production build PASS. Existing large-chunk advisory remains non-blocking.
- Chromium PC1440x900: 4 distinct scenarios PASS (three new-page/control scenarios + one T4/T6 source/gate scenario), 15 saved screenshots; no observed page/console error or horizontal overflow. Chromium uses controlled API fixtures; real native business lifecycle is proven separately by the integration tests. No mobile/full-system browser claim.
- Frozen consistency verifier PASS: 126 parent files byte-identical; inherited API endpoints unchanged; only approved inherited QA/eBR evidence schemas extended; 24 new operations,18 new permissions, six normalized tables,13 new page routes,13 requirement/test mappings. All OpenAPI refs resolve. Incoming schema unchanged; finished-lot projection correctly permits production-source nullable incoming references.

## Independent review and closure

One fresh whole-branch static review reported CRITICAL0/HIGH2. HIGH1: unrelated new PASS could mask unresolved original FAIL in derived report. Observed native RED expectedFAIL/actualPASS; fixed per-original signed closed investigation/authorized selected-result rule; native regression PASS, immutable original preserved. HIGH2: nested request read bypassed sampling/report view permissions. Public detail/list now omit unauthorized child collections; complete internal evidence separated and reader-independent digest proven by native regression PASS. No second independent review was claimed; implementer closure verification leaves no known CRITICAL/HIGH.

Reviewer MEDIUM graph missing result/report review and stock ledger edges was additionally repaired and native end-to-end graph assertions PASS. MEDIUM remaining: concurrency gate proves actual competing root lock/rollback, not two independently committed shipments under load; a persistent two-commit race/load test is deferred to a specifically authorized milestone to avoid committed regulated test facts. Server root/lot/ledger locks + atomic stock/idem remain enforced. Tenant-wide/N+1 projections are also non-blocking paging performance debt inherited from current architecture.

## Screenshots

See [screenshots](screenshots): five lists, five details, three create forms, finished-lot T4 and QA T6. Gate progress8/8 includes actual finished receipt/report gates; final eBR PDF remains a post-decision archive step. Existing accepted incoming inspection/report/T5 layout unchanged. Workbench image/photo/planned-time/SOP proposals and UI V3 not implemented.
