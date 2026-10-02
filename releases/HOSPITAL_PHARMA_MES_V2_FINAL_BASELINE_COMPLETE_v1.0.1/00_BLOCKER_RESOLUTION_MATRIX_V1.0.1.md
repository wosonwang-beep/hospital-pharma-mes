# BLOCKER RESOLUTION MATRIX — V1.0.1


| Blocker | v1.0.1 disposition | Status |
|---|---|---|
| DG-001 Migration baseline | logical groups; physical V001/V002 immutable; planned V003/V004 in ledger only | RESOLVED |
| DG-002 Common columns | Profile M/Profile A, FK/time/version/GxP strategy frozen | RESOLVED |
| DG-003 Signature ownership | MES-001 platform; MES-007 eBR providers; MES-013 release provider | RESOLVED |
| DG-004 Canonicalization/reauth | RFC 8785/SHA-256 and exact five-minute single-use binding frozen | RESOLVED |
| DG-005 Audit/integration contracts | concrete tables, DTOs, states, retry, idempotency, concurrency and audit frozen | RESOLVED |
| DG-006 Permissions | four least-privilege codes and dual retry permission frozen | RESOLVED |
| DG-007 UI route/data | `/audit` and `/integration/operations`; obsolete route/metrics removed | RESOLVED |
| DG-008 Enterprise NFR values | independent open validation register; no fabricated values | REQUIRES ENTERPRISE INPUT |
| DG-009 UI authentication staging | MES-001 protected components; MES-002 auth/navigation/E2E | RESOLVED |

Only approved status values are used. The cross-review gate is satisfied because CRITICAL=0 and HIGH=0.
