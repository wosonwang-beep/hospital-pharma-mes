# FINAL BASELINE COMPLETE v1.0.3

This release applies approved `DCP-MES-002-R2-001` on top of v1.0.1. It is the complete textual authority for Authorized Design Change Governance and the incoming-material quality lifecycle. The v1.0.1 directory remains unchanged historical evidence.

Start with `00_MANIFEST_FINAL_FROZEN_V1.0.3.md`, then use the numbered documents and task cards. Markdown/CSV/OpenAPI artifacts are authoritative. The prior Word bundles are deliberately not copied because they contain v1.0.1 text; no stale binary is presented as v1.0.3.

See `00_FINAL_RELEASE_V1.0.3.md`, the approved DCP record, Design Change Trace, Cross-Consistency Review and Open Enterprise Validation Register before using any artifact.

# Hospital Pharma MES V2.0 — FINAL DEVELOPMENT BASELINE

## Start here
1. Read `00_MANIFEST_FINAL_FROZEN.md`.
2. Read `14_AGENTS.md`.
3. Read PRD + Domain + Database + State/API/Functional/UI for the current task.
4. Execute exactly one file under `tasks/`.

## Baseline contents
01 PRD
02 Architecture
03 Domain Overview
04 Domain Detailed Design
05 Database Design
06 eBR Dynamic Form Detailed Design
07 State Machine Detailed Design
08 API Detailed Design + Full OpenAPI (171 operations)
09 Functional Detailed Design
10 UI/Page Detailed Design V1.1 + Route Matrix
11 GMP/Audit/eSignature
12 Test Acceptance
13 RTM
14 AGENTS
tasks/MES-001-R2 .. MES-013-R2
UI Prototype Baseline V1.0 FROZEN (runnable prototype + 13 frozen screenshots)

## UI frozen rule
Query/List page is query-only. Create, View and Edit are separate routes/pages. Transaction workbenches are separate from query pages.

## Change control
Any change to a FROZEN requirement, domain invariant, DB field/relation, state transition, API contract, permission, UI route rule or GMP control requires a Design Change Request and cross-review before implementation.

## Section 15
15 Module Dependency / Migration / Integration Contract is mandatory for every Implementation Plan.

## UI Prototype
Open `ui-prototype/README.md` for the runnable interaction baseline, design system, UI mapping, frozen screenshots and browser/design QA evidence.

