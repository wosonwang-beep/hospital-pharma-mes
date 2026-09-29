package com.hospital.mes.audit.domain;

import java.time.Instant;

public record AuditEvent(long id, long organizationId, long actorId, String actorRole, String action,
                         String objectType, String objectId, String oldValueDigest, String newValueDigest,
                         String reason, String clientInfo, Instant occurredAt, String transactionId,
                         String requestId, AuditSource source) { }
