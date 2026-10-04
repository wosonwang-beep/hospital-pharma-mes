# v1.0.7 consistency review

PASS before authority switch for approved DCP-MES-006-R2-001. Existing 17 endpoint paths/permissions preserved, approval prose uses /process-versions/{id}/approve. Closed product/process/formula/route/parameter DTOs and enum schemas specified; product ownership/routes and three route columns are approved additions. Domain/lifecycle/signature rules, row-retention, RTM TC-PROC-004, eBR soft dependency and consumer snapshot requirements are explicit in the normative approved specification referenced by affected documents. V011 is next after verified successful V010; old migrations/releases untouched. Business version for signature remains stable across status changes; optimistic token is separate. Local enhanced gate preserves native DEV; clean replay belongs to hosted CI. Implementation verification remains to be recorded before readiness.

## Implementation consistency evidence — 2026-10-03

- PASS: 17 original operations and permissions retained; closed DTOs match implementation. Empty-draft formula/route responses are nullable; completion-rule leaf JSON omits null children. 1,424 local OpenAPI references resolve.
- PASS: product and seven process tables match V011; three approved route columns, org-scoped FK checks, draft uniqueness and version locking implemented. DEV 001–011 all successful, Flyway validates all 11; executed migrations were not edited.
- PASS: DRAFT→SUBMITTED→APPROVED→EFFECTIVE, stable business-version signature binding, canonical authored content, independent approver, replay permission checks, audit/signature atomic rollback and immutable approved definitions verified.
- PASS: product/package eight independent routes, prototype BOM/route/parameter/compare tabs, publication validation panel and inherited inline-label presentation implemented. Desktop/mobile process flow verified.
- PASS: Material/UOM producer integration and detached process snapshots verified; named Submitted/Approved/Published events produced. eBR runtime and future tasks remain outside MES-006.
- Review findings: completion-rule round trip, rename preservation and saving-time input protection corrected; product edit loading race corrected. No remaining CRITICAL/HIGH or required MEDIUM findings.
- Enhanced gate: real V010→V011 upgrade and integrity, two concurrent database transactions, cross-module test and direct UOM regression passed. Clean-chain hosted CI is still required before merge; no local rebuild or second database.
- Readiness/evidence remains in MES_TASKS.md and docs/acceptance/mes-006/ACCEPTANCE.md; approval is not acceptance.
