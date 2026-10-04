# MES-007/008 approved execution plan

Authority: current frozen baseline plus explicit approved DCP-MES-007-008-SEQUENCING-001. The user authorized sequential MES-006～008 and minimal validation; no later business task is implied. Branch codex/mes-006-process contains existing user work and is reused. No commit/reset/clean/local credential staging.

1. Publish cumulative v1.0.9 with concrete stage API/DTO/state/UI/table/dependency contracts; verify local refs and previous release hashes before pointer switch.
2. MES-007: normalized definitions, immutable published content, safe DSL/lint/simulate/lifecycle, independent Designer/list/forms and published query contract. V012 allocated after actual DEV V011 confirmation.
3. MES-008: warehouse/location/container, audited receipt draft/edit/confirm, material/policy snapshot, blocked lots and append-only inventory, fail-closed batch/eligibility consumer boundary; original routes plus approved receipt PUT. V013 follows V012.
4. Integrate boot/security; apply both new migrations through native MariaDB targeted integration gate only; compile and current-stage domain/transaction/org/permission/idempotency/invariants. Frontend typecheck and minimal desktop/mobile interactions/layout. No full regression.
5. Fresh final scoped code review, fix critical/high findings, preserve original required later integration tests as pending. Update sole task index and evidence.

Shared interfaces: parent freezes concrete JSON/OpenAPI from backend contracts before UI writes; boot imports modules and security permits exact operations; all domains reuse platform and masterdata. Runtime production schemas/gates remain future producer responsibilities. No test adapter may be production-allowing fallback.
