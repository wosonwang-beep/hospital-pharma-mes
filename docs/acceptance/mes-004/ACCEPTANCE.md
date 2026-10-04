## Current v1.0.5 verification — material basic maintenance, 2026-10-03

Status: READY FOR ACCEPTANCE; not ACCEPTED. This section supersedes the historical v1.0.4 behavior below. Authority: DCP-MATERIAL-BASIC-001, explicit user instruction removing material versions/approval and confirmation of one preferred supplier per material.

Implementation: direct basic create/update/disable, named base/pack units, material-specific conversions, multiple supplier relationships with exactly one preferred among active relationships (zero only when none). No business-version creation/submit/approval API or UI. Supplier qualification unchanged. Internal versionNo remains optimistic concurrency metadata. Same-org, qualification/UTC expiry, permission checks, transactional audit, idempotency and detached snapshots retained. Revoked relationships and retired legacy version/rule evidence remain; legacy enabled states are included by ACTIVE filtering. No future task implementation.

Migration: V010 applied to native localhost hospital_pharma_mes_dev. Flyway history 001–010 all successful; prior migrations immutable and no reset. Material policy copied from legacy evidence without rewriting it; preferred generated unique constraint added; retired approval permissions disabled. Zero BASIC fixture materials and mb_ fixture actors after rollback.

Targeted verification: Maven clean verify PASS (10 domain tests and 14 native-MariaDB integration cases: 13 current material/supplier plus one direct upstream conversion case). Review-found legacy ACTIVE-filter fix verified by only its affected integration case plus the 10 focused unit tests, PASS. Frontend typecheck/build PASS and 8 focused unit tests PASS. Browser six desktop/mobile flows passed across focused gates: create/direct edit and supplier preference 4/4; conversion 2/2 PASS after fixing fixed-header focus visibility and component selector targeting. Browser APIs mocked; native MockMvc/service/database tests verify actual API, SQL constraints and audit rollback. No full regression. Browser plugin unavailable, repository Playwright used.

Review: final read-only increment review: CRITICAL=0, HIGH=0, necessary MEDIUM=0. Closed OpenAPI responses match derived unit names/codes, supplier qualification and traceId; 1405 local references resolve. Database/domain/state/API/UI/permission/test/RTM/migration/dependencies aligned under v1.0.5 and prior frozen releases retained. LOW debt: existing bundle-size warning and unrelated inherited WMS route-matrix duplication. Consumer receipt/batch snapshot integration remains in its own future task. No commit/push or local credential/config staging.

Screenshots: evidence/basic-v5-* captures loaded content for desktop/mobile.

---

## Historical v1.0.4 verification (superseded material workflow)

# MES-004-R2 verification record — 2026-10-03

Status: READY FOR ACCEPTANCE, not human-accepted. Human reply “授权” to the exact DCP-MES-004-005-R2-001 proposal is recorded in frozen v1.0.4. Older v1.0.2/v1.0.3 preserved. Cross-document review preceded authority-pointer switch.

Implementation: all frozen material identity/common fields; versioned full quality, storage, production objects and requiresIncomingInspection default true; explicit selected version submit/approve; immutable approved version reads/content digest; named root disable; optimistic root token; complete audited/idempotent typed APIs and independent UI routes. Approved root's new DRAFT rule edit omits identity data. Policy changes create a new version. In-process domain events and MaterialQueryService are produced; later batch/receipt consumers not implemented.

Migrations: V007 material/version/rule tables plus deferred md_unit_conversion.material_id FK; V009 additive frozen IDX declarations. DEV native MariaDB successful through V009; append-only migration history intact. Root credentials/configs unstaged. Fixture residue checks: zero.

Targeted validation: backend compile/package and frontend final build PASS. MaterialSupplierRulesTest 5 PASS. MaterialSupplierIT first run 8/10 PASS, with test-only wrong audit table and immutable mocked-permission snapshot corrected; only those 2 cases rerun PASS. Additional relation replay/stale/expiry case PASS. Combined with 12 current upstream cases, 23 distinct IT cases pass across gates. TC-MAT-001 complete POST 201/DB/Audit/policy; TC-MAT-002 exact duplicate code error; TC-MAT-003 approved edits forbidden/wrong version rejected; TC-MAT-004/005 explicit old version JSON unchanged after V2 approval with opposite policy. No fabricated future batch or receipt table: historical producer-version ID reads prove stability; real consumer snapshots remain MES-008/009 integration work.

UI/TC-UI-001: full grouped fields, version selector, inspection checkbox, approval permissions, audit link, new version creation, supplier relationship section, conflict-preserving forms. Latest 15 focused frontend tests PASS; desktop/mobile material creation→submit→approve 2/2 PASS after fixing same-URL record/button refresh (8.5s). Date copy for new versions uses selectedVersion. Browser API mocked; native MockMvc integration proves actual API/DB/audit behavior. Playwright fallback because Browser plugin absent. Query captures in evidence/ (captured during loading; structural visual comparison only).

Review: final scoped and increment review CRITICAL=0/HIGH=0/necessary MEDIUM=0. Existing bundle-size warning retained as LOW debt; inherited duplicate WMS route-matrix entries unchanged outside scope. No full regression or next task auto-start beyond user-authorized MES-005.
