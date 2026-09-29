package com.hospital.mes.audit.application;

import com.hospital.mes.audit.domain.AuditCommand;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(AuditEventRepository.class)
public class AuditApplicationService {
    private final AuditEventRepository repository;

    public AuditApplicationService(AuditEventRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(AuditCommand command) { repository.append(command); }
}
