package com.hospital.mes.audit.api;

import com.hospital.mes.audit.application.AuditEventQuery;
import com.hospital.mes.audit.application.AuditQueryService;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.audit.domain.AuditEvent;
import com.hospital.mes.audit.domain.AuditSource;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnBean(CurrentPlatformContextResolver.class)
public class AuditEventController {
    private final AuditQueryService service;
    private final CurrentPlatformContextResolver contexts;
    private final TraceIdProvider traces;

    public AuditEventController(AuditQueryService service, CurrentPlatformContextResolver contexts, TraceIdProvider traces) {
        this.service = service; this.contexts = contexts; this.traces = traces;
    }

    @GetMapping("/api/v1/audit-events")
    @PreAuthorize("hasAuthority('audit:view')")
    public ApiResponse<AuditEventPageResponse> query(
        @RequestParam(required = false) Long actorId, @RequestParam(required = false) String action,
        @RequestParam(required = false) String objectType, @RequestParam(required = false) String objectId,
        @RequestParam(required = false) AuditSource source, @RequestParam(required = false) String transactionId,
        @RequestParam(required = false) String requestId, @RequestParam(required = false) Instant occurredFrom,
        @RequestParam(required = false) Instant occurredTo,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        var result = service.query(contexts.current().organizationId(), new AuditEventQuery(actorId, action,
            objectType, objectId, source, transactionId, requestId, occurredFrom, occurredTo, page, size));
        return ApiResponse.success(new AuditEventPageResponse(result.items().stream().map(AuditEventController::view).toList(),
            result.page(), result.size(), result.total()), traces.currentTraceId());
    }

    private static AuditEventResponse view(AuditEvent e) {
        return new AuditEventResponse(Long.toString(e.id()), Long.toString(e.organizationId()),
            Long.toString(e.actorId()), e.actorRole(), e.action(), e.objectType(), e.objectId(), e.oldValueDigest(),
            e.newValueDigest(), e.reason(), e.clientInfo(), e.occurredAt(), e.transactionId(), e.requestId(), e.source());
    }
}
