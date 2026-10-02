# 15 Hospital Pharma MES V2.0 Dependency / Migration / Integration Contract V1.0.1 FROZEN

## 1. Authority and change trace

This document applies approved DCP-MES-001-R2-001. The v1.0 source package is retained unchanged. V1.0.1 supersedes conflicting migration-number, common-column, audit, signature, integration and route text. No business code or Flyway SQL is part of this release.

## 2. Task dependency rule

MES-001 through MES-013 remain ordered construction increments. Before any task: dependency check, gap analysis, impact analysis, consumed/produced contract review, then an approved implementation plan. The existing physical IAM slice does not mark MES-002 accepted and does not invert the logical task graph.

## 3. Logical migration groups versus physical Flyway versions

All historical Section 15 labels such as V001, V002 and V007A are **Logical Migration Groups**. They are never physical Flyway instructions. Physical V001/V002 in the repository are immutable forever and remain in their original order/names/checksums.

- Existing physical V001: KEEP `sys_foundation_probe`; no ALTER by MES-001.
- Existing physical V002: KEEP IAM/security tables; no ALTER by MES-001.
- Planned physical V003: create exactly `gxp_audit_event`, `gxp_signature`, `integration_inbox`, `integration_outbox`, `platform_idempotency_record`, indexes, checks and audit guards.
- Planned physical V004: idempotently seed/assign `audit:view`, `ebr:sign`, `integration:view`, `integration:retry` against the existing V002 schema.
- First later task allocates V005 or the then-next free number from the ledger.

V003/V004 above are planned allocations only. This baseline contains no SQL file and executes no migration.

## 4. Common database profiles

Profile M: `id BIGINT AUTO_INCREMENT`, required `org_id`, `created_by`, `created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)`, `updated_by`, `updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)` with no ON UPDATE, and `version_no BIGINT DEFAULT 0`. All are NOT NULL. Profile A omits update/version columns and is used for immutable append-only evidence. Common identity/org references are logical; explicit actor/signer FKs bind to existing `sys_user`.

## 5. Audit contract

AuditEvent is append-only and carries actor, authorizing role snapshot, action, object type/id, old/new canonical digests, reason, UTC time, transaction ID, request ID, source and idempotency key. It commits with the regulated change. Runtime DB access and MariaDB guards prohibit update/delete.

## 6. Signature ownership

MES-001 owns platform persistence, provider registry, canonicalization, reauthentication and generic APIs. MES-007 owns eBR providers/VERIFY/APPROVE and correction invalidation. MES-013 owns QA release provider/RELEASE/REJECT and release orchestration. RFC 8785 JCS plus lowercase SHA-256 is normative. Reauth TTL is exactly five minutes, single-use, bound to user/session/org/object/meaning/version, consumed on first attempt and not restored on rollback.

## 7. Integration contract

Inbox statuses: RECEIVED, PROCESSING, RETRY_WAIT, PROCESSED, DEAD_LETTER. Outbox statuses: PENDING, DISPATCHING, RETRY_WAIT, PUBLISHED, DEAD_LETTER. The controlled central policy freezes 8 attempts and PT1M/PT5M/PT15M/PT1H/PT4H/PT12H/PT24H delays. Failure 8 is terminal until an authorized audited manual retry. Retry never resets the counter. Claims use optimistic one-winner concurrency; claims older than 15 minutes recover through an explicit command. No delete/discard or generic status API exists.

## 8. UI and authentication handoff

`/audit` is GMP Audit Trail. `/integration/operations` is Inbox/Outbox Operations. `/platform/operations` and PLAT-001 are removed. MES-001 builds protected components with auth-context doubles; MES-002 supplies authentication/navigation integration and E2E. Only metrics backed by the frozen APIs are permitted.

## 9. Enterprise validation gate

RPO, RTO, retention, deployment site and approved time source remain OPEN ENTERPRISE VALIDATION ITEMS. MES-001 implementation blocking: NO. Production validation blocking: YES. Go-live blocking: YES. Production configuration has no fabricated defaults and no GxP purge is authorized.

## 10. Required ledgers and matrices

The authoritative detailed mappings are `15_LOGICAL_TO_PHYSICAL_MIGRATION_LEDGER_V1.0.1.csv`, `15_MIGRATION_DEPENDENCY_MATRIX_V1.0.1_FROZEN.csv`, `15_TASK_DEPENDENCY_MATRIX_V1.0.1_FROZEN.csv` and `15_INTEGRATION_CONTRACT_MATRIX_V1.0.1_FROZEN.csv`.
