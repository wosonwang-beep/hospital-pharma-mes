package com.hospital.mes.audit.application;

import com.hospital.mes.audit.domain.AuditCommand;

public interface AuditEventRepository {
    void append(AuditCommand command);
    AuditEventPage query(long organizationId, AuditEventQuery query);
}
