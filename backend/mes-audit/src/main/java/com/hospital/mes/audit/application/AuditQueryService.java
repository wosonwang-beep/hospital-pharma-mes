package com.hospital.mes.audit.application;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix="spring.datasource",name="url")
public class AuditQueryService {
    private final AuditEventRepository repository;

    public AuditQueryService(AuditEventRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public AuditEventPage query(long organizationId, AuditEventQuery query) {
        if (organizationId <= 0) throw new IllegalArgumentException("organization context is required");
        return repository.query(organizationId, query);
    }
}
