## Current v1.0.5 verification — material basic maintenance, 2026-10-03

Status: READY FOR ACCEPTANCE; not ACCEPTED. This section supersedes the historical v1.0.4 behavior below. Authority: DCP-MATERIAL-BASIC-001, explicit user instruction removing material versions/approval and confirmation of one preferred supplier per material.

Implementation: direct basic create/update/disable, named base/pack units, material-specific conversions, multiple supplier relationships with exactly one preferred among active relationships (zero only when none). No business-version creation/submit/approval API or UI. Supplier qualification unchanged. Internal versionNo remains optimistic concurrency metadata. Same-org, qualification/UTC expiry, permission checks, transactional audit, idempotency and detached snapshots retained. Revoked relationships and retired legacy version/rule evidence remain; legacy enabled states are included by ACTIVE filtering. No future task implementation.

Migration: V010 applied to native localhost hospital_pharma_mes_dev. Flyway history 001–010 all successful; prior migrations immutable and no reset. Material policy copied from legacy evidence without rewriting it; preferred generated unique constraint added; retired approval permissions disabled. Zero BASIC fixture materials and mb_ fixture actors after rollback.

Targeted verification: Maven clean verify PASS (10 domain tests and 14 native-MariaDB integration cases: 13 current material/supplier plus one direct upstream conversion case). Review-found legacy ACTIVE-filter fix verified by only its affected integration case plus the 10 focused unit tests, PASS. Frontend typecheck/build PASS and 8 focused unit tests PASS. Browser six desktop/mobile flows passed across focused gates: create/direct edit and supplier preference 4/4; conversion 2/2 PASS after fixing fixed-header focus visibility and component selector targeting. Browser APIs mocked; native MockMvc/service/database tests verify actual API, SQL constraints and audit rollback. No full regression. Browser plugin unavailable, repository Playwright used.

Review: final read-only increment review: CRITICAL=0, HIGH=0, necessary MEDIUM=0. Closed OpenAPI responses match derived unit names/codes, supplier qualification and traceId; 1405 local references resolve. Database/domain/state/API/UI/permission/test/RTM/migration/dependencies aligned under v1.0.5 and prior frozen releases retained. LOW debt: existing bundle-size warning and unrelated inherited WMS route-matrix duplication. Consumer receipt/batch snapshot integration remains in its own future task. No commit/push or local credential/config staging.

Screenshots: evidence/basic-v5-* captures loaded content for desktop/mobile.

---

## Historical v1.0.4 verification (superseded material workflow)

# MES-005-R2 verification record — 2026-10-03

Status: READY FOR ACCEPTANCE, not human-accepted. Authority: approved DCP-MES-004-005-R2-001 in v1.0.4, with prior releases retained.

Implementation: frozen Supplier and MaterialSupplier fields; named UPDATE/QUALIFY/DISABLE, qualification UNAPPROVED/APPROVED/INACTIVE; same-org reference validation; inclusive UTC supplier and relationship validity; root material version governs assignment concurrency; omitted relationships revoked, never deleted; shared transactional audit/idempotency. ApprovedSupplierQueryService produced for MES-008 receipt validation, without implementing receipt behavior. Existing relationship permission remains master:material:update. UI includes independent supplier pages, qualification controls/status, and retained relationship editor under existing material route.

Migration: V008 supplier/relationship tables; V009 frozen query indexes. Native DEV history success through V009; no old migration edit/reset. Rollback fixtures, zero test-user/material residue.

Required cases: TC-SUP-001 approved relation usable; TC-SUP-002 relation expiry blocks with SUPPLIER_NOT_APPROVED at next UTC date. Additional supplier expiry/disabled gate, same-org isolation, assignment replay producing one audit, root stale token, omitted relation remains as approved=false; all PASS. Included in MaterialSupplierIT 11 distinct cases passed across first gate and failure/additional-case-only checks. Other cases cover current material/upstream regressions and audit rollback.

Frontend final build and latest 15 targeted tests PASS. Desktop/mobile qualification→relationship editing: 2/2 browser PASS (first gate; no unnecessary rerun). Browser API mocked; actual service/API/SQL/audit checked separately. Page qualificationStatus column/sort/title verified in final review. No screenshot retained after Playwright output cleanup; browser gate result remains recorded.

Review: CRITICAL=0, HIGH=0, remaining necessary MEDIUM=0. Retained LOW debt: existing frontend bundle-size warning and inherited duplicate WMS route rows. Stop at MES-005; further MES tasks remain NOT STARTED.
