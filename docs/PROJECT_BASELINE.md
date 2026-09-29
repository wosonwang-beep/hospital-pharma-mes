# Hospital Pharmaceutical MES V2.0 Project Baseline

This document is the compact, stable entry point for product and engineering context. It does not track task status or reproduce detailed domain design.

## Authority

- Authoritative design: `FINAL BASELINE COMPLETE v1.0.1`.
- Release directory: [`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/).
- Frozen manifest: [`00_MANIFEST_FINAL_FROZEN_V1.0.1.md`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.1/00_MANIFEST_FINAL_FROZEN_V1.0.1.md).
- Approved change trace: `DCP-MES-001-R2-001` → v1.0.1.
- Task state and next-task selection: [`MES_TASKS.md`](../MES_TASKS.md).

Frozen requirements, data models, states, APIs, permissions, routes, integration contracts, and GxP controls may change only through approved design change and cross-consistency review.

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
- DEV is persistent in `hospital_pharma_mes_dev`; TEST is ephemeral and isolated; PROD is a future validated environment. DEV, TEST, and PROD are never interchangeable.
- Persistent DEV data is not deleted, rebuilt, or cleared without explicit destructive-action authorization.
- Operational details: [`development/database-and-validation-strategy.md`](development/database-and-validation-strategy.md).

## Minimal document routing

Start with `AGENTS.md` and `MES_TASKS.md`. After selecting one task, open its Task Card and only the referenced sections needed for that task:

- Product and domain: PRD, Domain Model, and relevant state-machine sections in the v1.0.1 release.
- Data and interfaces: Database Design, API Detailed Design, Full OpenAPI, and Section 15 contracts named by the Task Card.
- Behavior and UI: Functional Design, UI/Page Design, Route Matrix, and prototype mapping only when the task touches them.
- Compliance and tests: GMP/Audit/eSignature, required Test Cases, and RTM rows named by the Task Card.
- Repository implementation guidance: [`architecture/`](architecture/), [`api/`](api/), [`development/`](development/), and accepted evidence under [`acceptance/`](acceptance/) only when directly relevant.

Do not read every baseline artifact or all repository documentation as a startup step. Detailed domain documents remain at their existing locations and are loaded on demand.
