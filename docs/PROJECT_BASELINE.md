# Hospital Pharmaceutical MES V2.0 Project Baseline

This document is the compact, stable entry point for product and engineering context. It does not track task status or reproduce detailed domain design.

## Authority

### Current approved optional inspection draft authority — 2026-10-06

Current business authority: `FINAL BASELINE COMPLETE v1.0.21` — `AUTHORITATIVE`, approved DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001 and cross-document consistency PASS. [Frozen manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21/00_MANIFEST_FINAL_FROZEN_V1.0.21.md), [consistency](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21/00_FINISHED_INBOUND_DRAFT_CONSISTENCY_REVIEW.json). Optional atomic inbound/inspection DRAFT pair uses existing contracts; physical warehouse confirmation remains mandatory for QC execution. All138 v1.0.20 files unchanged. Existing v20 finished-chain/WMS/source contracts inherited; no new migration/permission/endpoint/route/signature rule. UI V2/T1–T6 unchanged. Runtime readiness and explicit human acceptance only MES_TASKS.md. Earlier dated pointers remain historical.

### Retained v1.0.20 approved finished-goods authority — 2026-10-06

Historical authority at that approval: `FINAL BASELINE COMPLETE v1.0.20` — `AUTHORITATIVE`, approved DCP-FINISHED-GOODS-CHAIN-001 and cross-document consistency PASS. [Frozen manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20/00_MANIFEST_FINAL_FROZEN_V1.0.20.md), [consistency](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20/00_FINISHED_GOODS_CONSISTENCY_REVIEW.json). Complete controlled finished receiving/quality/QA/shipment/trace delta supersedes only its bounded inherited contracts. Parent v1.0.19/126files unchanged; UI V2/T1–T6 unchanged. Runtime readiness and explicit human acceptance are maintained only in MES_TASKS.md. Earlier dated pointers remain historical.

### Retained v1.0.19 WMS authority — 2026-10-06

Historical WMS authority at that maintenance: `FINAL BASELINE COMPLETE v1.0.19` — `AUTHORITATIVE`, following approved DCP-WMS-REQUEST-INVENTORY-RETURN-001 and detailed-design confirmation; design consistency PASS. [Manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.19/00_MANIFEST_FINAL_FROZEN_V1.0.19.md). Existing v1.0.18 and earlier entries below are retained history. UI remains Global UI V2 / T1–T6; runtime status and explicit human acceptance are maintained only in MES_TASKS.md.


### Retained v1.0.18 maintenance authority — 2026-10-06

At that approval, `FINAL BASELINE COMPLETE v1.0.18` was `AUTHORITATIVE`. User approved database changes and chose manufacturer ownership on each material-supplier relationship with receiving-source freeze. [Approved DCP](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md), [frozen manifest](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MANIFEST_FINAL_FROZEN_V1.0.18.md), [consistency PASS](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18/00_MATERIAL_STORAGE_SOURCE_CONSISTENCY_REVIEW.json). v1.0.17 and all prior frozen releases remain unchanged historical authority. Current UI authority remains Global UI V2 / Page Template V2 (`T1–T6`). Current maintenance readiness is only in MES_TASKS.md.

The v1.0.17 Closeout and subsequent dated approval descriptions below retain their original scope; earlier “current” references are historical and do not override this v1.0.18 pointer. No new final-system regression or human acceptance of this maintenance is inferred. Workbench image/planned-time/SOP proposals remain PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED; UI V3 remains NOT IN CURRENT SCOPE.

### Retained v1.0.17 authority and Closeout — 2026-10-06

- Business: `FINAL BASELINE COMPLETE v1.0.17` — `AUTHORITATIVE`; the v1.0.17 release directory and frozen manifest linked below remain unchanged.
- UI: [Global UI Design System V2](ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md) and [Page Template Standard V2](ui/MES_PAGE_TEMPLATE_STANDARD_V2.md), `T1–T6`.
- Human-confirmed Functional Baseline v1.0.17 and UI V2 Implementation: `CLOSED`. Current task acceptance is maintained in [MES_TASKS.md](../MES_TASKS.md).
- [Historical v1.0.17 Closeout record](review/MES_V1.0.17_FUNCTIONAL_UI_V2_FINAL_CLOSEOUT.md) records acceptance and existing evidence; this administrative update creates no new release or business contract.
- DCP-WORKBENCH-GAPS-001: Material Master Image, Operation Execution Photo, Planned Operation Time, SOP/Method Reference remain `PROPOSED / NOT AUTHORIZED / NOT IMPLEMENTED`, outside the closed scope.
- Global UI V3: `NOT IN CURRENT SCOPE`. Current UI authority is not switched.

### Retained approval history

The dated approval entries below retain their original release descriptions. Any earlier use of “current” or “authoritative” is historical; only the current v1.0.17 pointer above applies now.


- Current approved audit HIGH closure: user explicitly approved DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 on 2026-10-05. Cumulative v1.0.17 is authoritative after cross-document consistency PASS. Scope: existing productionOrderId query, incoming read allowedActions and exclusive active equipment occupancy. [Current contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_AUDIT_HIGH_CONTRACT_V1.0.17.md). No schema/migration, new permission/route/state, signature or QA release-rule change. Global UI V2 and T1–T6 are unchanged. All earlier entries below are preserved approval history; the current authoritative pointer takes precedence.

- Current authorized completion: user approved DCP-MES-012-013-CONTRACT-001 on 2026-10-04; reviewed cumulative v1.0.16 is authoritative after 2026-10-05 consistency review. Production quality, material balance, finished QA/archive and narrow producer/consumer extensions are authorized. v1.0.15 remains immutable historical authority; runtime completion is not inferred. Read [current contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md).

- Approved bounded functional closure: user explicitly replied “批准方案” on 2026-10-04 for [DCP-MES-008-011-FUNCTIONAL-CLOSURE-001](development/DCP-MES-008-011-FUNCTIONAL-CLOSURE-001-PROPOSED.md). Inventory decisions, controlled clearance and only the staged MES-012 IPC producer are authorized. v1.0.15 is now the authoritative cumulative release after cross-document consistency review. The user separately approved the one-time failed V024 recovery; migration and scoped native verification passed. Original failure evidence and bounded approval are retained in [recovery/acceptance evidence](acceptance/functional-closure/RECOVERY-2026-10-04.md); no general authorization to rewrite migration history is implied.

- Current approved completion: DCP-INCOMING-QUALITY-GAPS-001 plus referenced MES-009/010 decisions and full acceptance scope; explicit approval 2026-10-03. Reviewed v1.0.11 core, v1.0.12 runtime/return, v1.0.13 weighing producer and v1.0.14 frozen-standard trace contracts are current. Prior releases are preserved; verification is recorded in docs/acceptance/incoming-quality.

- Current bounded incoming delta: DCP-MES-008A-CONTRACT-001 A/B, previously approved routes and staged QMS FKs; cross-consistency review PASS. v1.0.9 retained unchanged. That earlier gap register is superseded by the approved completion contracts.

- Current approved bounded change: DCP-MES-007-008-SEQUENCING-001; explicit approval 2026-10-03, staged eBR/WMS, delayed FKs and receipt PUT; v1.0.8 preserved.

- Authoritative design: `FINAL BASELINE COMPLETE v1.0.17`.
- Release directory: [`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/).
- Frozen manifest: [`00_MANIFEST_FINAL_FROZEN_V1.0.17.md`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17/00_MANIFEST_FINAL_FROZEN_V1.0.17.md).
- Current bounded correction: `DCP-MATERIAL-NAMES-UI-001` removes three material name fields from business use and enforces horizontal form labels; v1.0.7 retained, no physical migration.
- Approved MES-006 bounded completion: `DCP-MES-006-R2-001` (explicit “确认授权”, 2026-10-03); original design retained, v1.0.6 immutable.
- Approved MES-004/005 bounded change: `DCP-MES-004-005-R2-001` (human approval 2026-10-03); prior v1.0.3 retained.
- Approved MES-003 bounded change: `DCP-MES-003-R2-001` (human approval 2026-10-03); prior v1.0.2 retained.
- Approved change trace: `DCP-MES-002-R2-001` → v1.0.2 (cumulative over retained v1.0.1 / DCP-MES-001-R2-001).
- Task state and next-task selection: [`MES_TASKS.md`](../MES_TASKS.md).

Frozen requirements, data models, states, APIs, permissions, routes, integration contracts, and GxP controls may change only through approved design change and cross-consistency review.

- Approved current presentation change: `DCP-UI-LIST-EDIT-001` (attached reference list/edit style, 2026-10-03); prior v1.0.5 retained.

## Product baseline

Hospital Pharmaceutical MES V2.0 supports regulated hospital-preparation manufacturing from master data and process definition through eBR, inventory, production execution, genealogy, quality, material balance, QA release, and archive evidence.

Records and state transitions must preserve traceability, data integrity, attributable audit evidence, and required electronic signatures. `RELEASED` means finished-product release, not production completion.

## Technical baseline

- Backend: Java 21, Spring Boot 3.x, Maven modular monolith, MyBatis-Plus, Flyway, MariaDB, Redis, Spring Security/JWT, Springdoc OpenAPI.
- Frontend: Vue 3, TypeScript, Vite, Pinia, Vue Router, Ant Design Vue; Vitest and Playwright for focused verification.
- Runtime boundary: `mes-boot` is the only executable backend module. Domain modules are library JARs and must remain acyclic.
- Module structure: `api`, `application`, `domain`, and `infrastructure`; domain events for notifications and query services for complex cross-module reads.

## Architecture and GxP invariants

- Domain commands and methods own business state changes; no generic status-update endpoint.
- Core production state machines operate without BPMN/workflow runtime availability.
- API envelopes remain outside domain services.
- Mutable regulated aggregates use optimistic locking; regulated records are never physically deleted.
- Critical writes are transactionally designed with authorization, audit, idempotency where required, and electronic-signature binding where specified.
- Audit/signature evidence is append-only or superseded through explicit lineage; credentials and sensitive reauthentication material never enter logs or audit payloads.
- Dynamic-form expressions never execute arbitrary JavaScript, SQL, or SpEL.

## Database and environment baseline

- MariaDB is the production-compatible SQL target; formal schema evolution is Flyway-only and append-only.
- Section 15 migration identifiers are logical groups, not physical Flyway versions. New physical versions follow the actual highest successful repository/database version.
- Local development and local database tests share the persistent native `hospital_pharma_mes_dev` using unique rollback-owned fixtures. Hosted CI retains its isolated service container. Tests never connect to PROD.
- Persistent DEV data is not deleted, rebuilt, or cleared without explicit destructive-action authorization.
- Operational details: [`development/database-and-validation-strategy.md`](development/database-and-validation-strategy.md).

## Minimal document routing

Start with `AGENTS.md` and `MES_TASKS.md`. After selecting one task, open its Task Card and only the referenced sections needed for that task:

- Product and domain: PRD, Domain Model, and relevant state-machine sections in the current authoritative release.
- Data and interfaces: Database Design, API Detailed Design, Full OpenAPI, and Section 15 contracts named by the Task Card.
- Behavior and UI: Functional Design, UI/Page Design, Route Matrix, and prototype mapping only when the task touches them.
- Compliance and tests: GMP/Audit/eSignature, required Test Cases, and RTM rows named by the Task Card.
- Repository implementation guidance: [`architecture/`](architecture/), [`api/`](api/), [`development/`](development/), and accepted evidence under [`acceptance/`](acceptance/) only when directly relevant.

Do not read every baseline artifact or all repository documentation as a startup step. Detailed domain documents remain at their existing locations and are loaded on demand.

Current approved material-only change: DCP-MATERIAL-BASIC-001, direct basic maintenance with multiple suppliers/one preferred and consumer snapshots; v1.0.4 retained as historical authority.
