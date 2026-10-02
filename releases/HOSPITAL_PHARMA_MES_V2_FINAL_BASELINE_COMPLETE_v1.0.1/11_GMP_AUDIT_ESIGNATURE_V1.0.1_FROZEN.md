# GMP数据完整性与审计

禁止硬删除GxP记录；禁止覆盖历史值；更正追加revision并保留前后值、原因、人员、时间；必要时原复核失效并重签。签名绑定身份、含义、时间、对象和record_digest。

# V1.0.1 Audit and eSignature Controls — DCP-MES-001-R2-001
AuditEvent records the authenticated/registered technical actor, authorizing role snapshot, stable action/object, canonical old/new digests, reason, UTC millisecond timestamp, transaction ID, request ID, source and optional idempotency key. `created_by=actor_id` and `created_at=occurred_at`. Background jobs use a registered technical user. Credentials, tokens, payload bodies and secrets never enter AuditEvent or ordinary logs. Runtime database rights and MariaDB guards make the table append-only.

Electronic signature is not an image. It uses a server-built RFC 8785 canonical envelope and lowercase SHA-256 digest. Reauthentication is exactly five minutes, single-use and bound to user/session/org/object/meaning/version. Consumption occurs on first sign attempt and is not restored on rollback. `auth_context_json` contains only schema version, method, reauthentication timestamp, SHA-256 session ID hash and request ID.

Invalidation is a controlled `VALID → INVALIDATED` mutation of status/invalidation/update metadata/version only. Re-sign inserts a new row and links the immediately superseded invalidated signature. Existing signature digests are never recalculated in place.
