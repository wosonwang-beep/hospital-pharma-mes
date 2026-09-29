package com.hospital.mes.audit.api;

import com.hospital.mes.audit.domain.AuditSource;
import java.time.Instant;

public record AuditEventResponse(String id, String orgId, String actorId, String actorRole, String action,
                                 String objectType, String objectId, String oldValueDigest, String newValueDigest,
                                 String reason, String clientInfo, Instant occurredAt, String transactionId,
                                 String requestId, AuditSource source) { }
