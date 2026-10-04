# MES-003-R2 verification record — 2026-10-03

Status: READY FOR ACCEPTANCE, not human-accepted. Authority: v1.0.4 cumulative over the human-authorized v1.0.3 DCP-MES-003-R2-001.

Implemented scoped organization hierarchy, exact unit conversion and material-specific handoff, equipment location/calibration/maintenance, personnel qualification UTC validity; typed commands, frozen independent query/create/view/edit routes, permissions, optimistic locking, audit and idempotency. Existing regulated records are retained. UnitConversion material FK was added by MES-004 V007 as approved.

Migration: V006 successfully applied; validated again during V007–V009. No executed migration edit, no repair/reset/alternate database.

Targeted results: MasterRulesTest 5 PASS; EquipmentRulesTest 3 PASS; MasterResourcesIT 12 PASS on native MariaDB (latest 8.033s). Covers TC-MD-001/002, TC-EQP-001, TC-QUAL-001, exact precision, hierarchy, current-org reads, permission-before-replay, stale versions, audit rollback, and actual concurrent dimension/pair/version current-read conflicts. Only unique rollback fixtures or exact raw concurrency-fixture cleanup; no shared seed mutation.

Frontend: original 15 targeted tests PASS; affected master/audit regression included in later 15-test gate. Browser: 6 distinct cases across desktop/mobile PASS, after scoped locator correction and visual changes. API routes mocked for browser only; API/transaction evidence comes from native-MariaDB MockMvc integration. Browser plugin absent, so repository Playwright used. Desktop/mobile equipment captures in evidence/.

Review: final read-only review CRITICAL/HIGH=0, resolved cached dimension comparison and audit deep link. Retained debt: existing frontend bundle-size warning; inherited WMS matrix duplicate outside scope. No downstream production/warehouse workflows preimplemented.

## User-authorized query layout follow-up — 2026-10-03

User explicitly requested every query label and input on the same line. Shared CSS now uses horizontal non-wrapping label/control pairs with flexible control width; query groups wrap responsively, including all seven master-data pages and both IAM query pages. No business fields, API, routes, permissions, states or migrations changed. Frontend build PASS; one focused Playwright layout inspection checks 9 pages × desktop/mobile, 52 label/control pairs: same row, no horizontal viewport overflow. APIs mocked for layout only; no backend/full regression rerun. Existing frozen prototypes are superseded only for this user-approved query-label placement.
