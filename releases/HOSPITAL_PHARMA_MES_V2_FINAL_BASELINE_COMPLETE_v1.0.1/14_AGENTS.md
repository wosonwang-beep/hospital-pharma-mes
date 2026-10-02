# hospital-pharma-mes — FINAL FROZEN Engineering Rules
This directory is the sole authoritative development baseline.

Priority on conflict:
1. 01 PRD Requirement
2. 04 Domain Detailed Design / Frozen invariants
3. 05 Database Design
4. 06 eBR Detailed Design
5. 07 State Machine
6. 08 API + OpenAPI
7. 09 Functional Detailed Design
8. 10 UI Detailed Design V1.1
9. 11 GMP/Audit/eSignature
10. 12 Test Acceptance
11. 13 RTM
12. Current MES-XXX-R2 task

Codex is an implementation agent, not product architect.
Never invent requirements, fields, states, permissions, GMP rules, routes or APIs.
If specification is missing or contradictory: STOP and report MISSING SPECIFICATION / DESIGN CONFLICT.
Do not modify frozen design as part of implementation.
Do not refactor unrelated modules.
One MES task at a time. When acceptance + affected regression pass: STOP.
UI rule: Query/List, Create, View and Edit are separate routes/pages. Do not replace them with CRUD modals/drawers.

# V1.0.1 Change-Control Guidance
16. Treat Section 15 V001/V002/V007A-style identifiers as logical migration groups. Resolve physical Flyway versions from the ledger; never edit, rename or reorder repository V001/V002.
17. Integration retry limit 8 and delays PT1M/PT5M/PT15M/PT1H/PT4H/PT12H/PT24H must come from one controlled platform policy component. No module-local override or scattered hard-coding.
18. Use `platform_idempotency_record` for generic Idempotency-Key handling. A module-specific generic idempotency table requires an approved special need.
19. Reauthentication is exactly five minutes, single-use, bound to user/session/org/object/meaning/version and consumed on first sign attempt without rollback restoration. Credentials/tokens never enter logs, audit or auth context.
20. Formal routes are `/audit` and `/integration/operations`. `/platform/operations` and PLAT-001 are forbidden. Prototype metrics without a formal API contract are non-production-only and must not be implemented.
21. DG-008 enterprise values stay blank until supplied through approved enterprise validation evidence. They block production validation/go-live, not MES-001 construction.
