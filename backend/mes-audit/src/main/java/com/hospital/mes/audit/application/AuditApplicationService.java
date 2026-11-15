package com.hospital.mes.audit.application;

import com.hospital.mes.audit.domain.AuditCommand;
import org.springframework.stereotype.Service;

/**
 * Compatibility adapter retained for legacy mutation call sites.
 * GxP audit event persistence is disabled by the approved product baseline.
 */
@Service
public class AuditApplicationService {
    public void append(AuditCommand command) {
        // Intentionally no-op: this deployment does not provide audit event logging.
    }
}
