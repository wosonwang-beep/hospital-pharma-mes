# MES-003–005 implementation plan

Approved method: autonomous inline development with minimal targeted verification, human authorization 2026-10-03. Use writing-plans/executing-plans and focused behavioral tests. Work in the required project root; preserve user changes and workstation configuration. No automatic commit, push or merge.

Goal: deliver the three ordered task cards against the authoritative v1.0.3 baseline, including DCP-MES-003-R2-001. Each task starts only when its consumed contracts are physically available.

Architecture: Java 21/Spring Boot 3 modular monolith; mes-masterdata owns organization/UOM/qualification/material/supplier, mes-equipment owns equipment and consumes master queries. Profile M persistence uses MyBatis-Plus, scoped by org_id, optimistic conditional writes, audit and existing platform idempotency in one local transaction. Vue 3 routes use independent query/create/view/edit pages and permission-filtered navigation.

## MES-003
- [x] Record four-gap approval, preserve v1.0.2, create/review v1.0.3 before switching pointers.
- [x] Write focused domain tests for hierarchy, conversion scale/dimension, equipment and qualification UTC execution gates; establish failing feature evidence once.
- [x] Add V006 after actual DEV V005 history: five scoped Profile M tables, constraints/indexes, equipment location, deferred nullable material FK and frozen permission/menu seeds.
- [x] Implement typed commands/domain rules, repositories, current-org query services, authorized CRUD commands with audit/idempotency/stale-version protection. Backend paths remain frozen GET/POST/PUT, no generic updateStatus or deletion.
- [x] Add Vue master query/detail/form components, URL filters, paging/sorting, explicit actions, audit link, error and 409 handling; inspect supplied equipment prototype.
- [x] Compile affected modules, build frontend, run domain/contract tests and one safe rollback-based native-MariaDB integration gate. Required TC-MD-001/002, TC-EQP-001, TC-QUAL-001; RBAC/current-org/audit/replay/version coverage.
- [x] Browser smoke and final focused review; zero HIGH/CRITICAL; record evidence and readiness in MES_TASKS.md.

## MES-004
- [x] Read only material task references; check MES-003 UOM/org producer contracts and approved delayed material FK ownership.
- [x] Implement complete frozen material/version/quality/storage/production fields, draft/version/submit/approve/disable commands and immutable historical read contract; do not change undeclared business states/APIs. Allocate next new physical migration and add conversion-material FK.
- [x] Deliver frozen material routes and incoming-inspection control. Verify TC-MAT-001–005 and TC-UI-001 plus direct UOM/version regressions once. Record readiness or a newly discovered out-of-approval conflict.

## MES-005
- [x] Check physical material/org contracts; read supplier references.
- [x] Implement frozen supplier and approved material-supplier validity contracts, routes, audited/versioned mutation and eligibility reads with next physical migration.
- [x] Verify TC-SUP-001/002, material/org/qualification regression once. Record readiness.

Review focus: cross-org ID spoofing; stale/repeated writes producing duplicate audit; UTC expiry boundaries; ancestor cycles/concurrent hierarchy writes; approved material-version history surviving new policy versions. Database tests use unique records inside rollback transactions; shared seed records are read-only.

Ruling: user authorizes direct in-session work and minimal test cadence; no repeated approval handoffs, full regression, isolated local database or per-function red/green loops. One focused red evidence followed by targeted task gate. Any newly discovered frozen-contract gap beyond the four approved items still requires DESIGN CHANGE REQUIRED.

Execution complete under DCP-MES-003-R2-001 and explicitly approved DCP-MES-004-005-R2-001. v1.0.4 authority/review recorded; targeted results and readiness in MES_TASKS.md and docs/acceptance. No next task started.
