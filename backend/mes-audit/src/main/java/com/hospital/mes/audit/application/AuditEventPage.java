package com.hospital.mes.audit.application;

import com.hospital.mes.audit.domain.AuditEvent;
import java.util.List;

public record AuditEventPage(List<AuditEvent> items, int page, int size, long total) {
    public AuditEventPage { items = List.copyOf(items); }
}
