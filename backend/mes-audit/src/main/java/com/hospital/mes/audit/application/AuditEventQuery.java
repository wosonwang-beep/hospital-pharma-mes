package com.hospital.mes.audit.application;

import com.hospital.mes.audit.domain.AuditSource;
import java.time.Instant;

public record AuditEventQuery(Long actorId, String action, String objectType, String objectId,
                              AuditSource source, String transactionId, String requestId,
                              Instant occurredFrom, Instant occurredTo, int page, int size) {
    public AuditEventQuery {
        if ((objectType == null) != (objectId == null)) throw new IllegalArgumentException("objectType and objectId must be paired");
        if (page < 0) throw new IllegalArgumentException("page must be non-negative");
        if (size < 1 || size > 200) throw new IllegalArgumentException("size must be between 1 and 200");
        if (occurredFrom != null && occurredTo != null && !occurredFrom.isBefore(occurredTo)) {
            throw new IllegalArgumentException("occurredFrom must precede occurredTo");
        }
    }
}
