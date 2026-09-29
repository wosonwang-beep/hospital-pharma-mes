package com.hospital.mes.audit.application;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(AuditEventRepository.class)
public class AuditQueryService {
    private final AuditEventRepository repository;

    public AuditQueryService(AuditEventRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public AuditEventPage query(long organizationId, AuditEventQuery query) {
        if (organizationId <= 0) throw new IllegalArgumentException("organization context is required");
        return repository.query(organizationId, query);
    }
}
