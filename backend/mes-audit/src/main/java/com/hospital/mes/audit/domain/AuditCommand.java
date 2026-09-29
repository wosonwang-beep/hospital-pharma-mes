package com.hospital.mes.audit.domain;

import java.time.Instant;
import java.util.regex.Pattern;

public record AuditCommand(long organizationId, long actorId, String actorRole, String action,
                           String objectType, String objectId, String oldValueDigest,
                           String newValueDigest, String reason, String clientInfo, Instant occurredAt,
                           String transactionId, String requestId, AuditSource source,
                           String idempotencyKey) {
    private static final Pattern DIGEST = Pattern.compile("[0-9a-f]{64}");

    public AuditCommand {
        if (organizationId <= 0 || actorId <= 0) throw new IllegalArgumentException("audit actor context is required");
        require(action, "action", 80);
        require(objectType, "objectType", 80);
        require(objectId, "objectId", 100);
        require(transactionId, "transactionId", 100);
        if (occurredAt == null || source == null) throw new IllegalArgumentException("audit time and source are required");
        validateDigest(oldValueDigest);
        validateDigest(newValueDigest);
        limit(actorRole, 100, "actorRole");
        limit(reason, 1000, "reason");
        limit(clientInfo, 500, "clientInfo");
        limit(requestId, 100, "requestId");
        limit(idempotencyKey, 128, "idempotencyKey");
    }

    private static void validateDigest(String value) {
        if (value != null && !DIGEST.matcher(value).matches()) throw new IllegalArgumentException("invalid SHA-256 digest");
    }

    private static void require(String value, String name, int maximum) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        limit(value, maximum, name);
    }

    private static void limit(String value, int maximum, String name) {
        if (value != null && value.length() > maximum) throw new IllegalArgumentException(name + " is too long");
    }
}
