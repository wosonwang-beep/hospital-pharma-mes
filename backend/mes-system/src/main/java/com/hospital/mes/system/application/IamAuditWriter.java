package com.hospital.mes.system.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.audit.application.AuditApplicationService;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.domain.AuditCommand;
import com.hospital.mes.audit.domain.AuditSource;
import com.hospital.mes.audit.idempotency.PlatformIdempotencyService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class IamAuditWriter {
    private final AuditApplicationService audit;
    private final ObjectMapper json;

    public IamAuditWriter(AuditApplicationService audit, ObjectMapper json) {
        this.audit = audit;
        this.json = json;
    }

    public void append(CurrentPlatformContext context, String action, String objectType,
                       String objectId, Object oldValue, Object newValue, String reason,
                       String idempotencyKey) {
        audit.append(new AuditCommand(context.organizationId(), context.actorId(),
            context.roleSnapshot(), action, objectType, objectId, digest(oldValue), digest(newValue),
            normalizeReason(reason), null, Instant.now(), UUID.randomUUID().toString(),
            context.requestId(), AuditSource.API, idempotencyKey));
    }

    private String digest(Object value) {
        if (value == null) return null;
        try {
            return PlatformIdempotencyService.digest(json.writeValueAsString(value));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Audit value cannot be canonicalized", ex);
        }
    }

    private static String normalizeReason(String value) {
        if (value == null || value.isBlank()) return null;
        String reason = value.strip();
        if (reason.length() > 1000) throw new IllegalArgumentException("Reason is too long");
        return reason;
    }
}
