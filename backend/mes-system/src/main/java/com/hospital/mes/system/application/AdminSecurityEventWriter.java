package com.hospital.mes.system.application;

import com.hospital.mes.system.infrastructure.SysSecurityEventEntity;
import com.hospital.mes.system.infrastructure.SysSecurityEventMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "spring.datasource", name = "url")
public class AdminSecurityEventWriter {
    private final SysSecurityEventMapper events;
    private final Clock clock;

    public AdminSecurityEventWriter(SysSecurityEventMapper events, Clock clock) {
        this.events = events;
        this.clock = clock;
    }

    public void append(String type, Long actorId, Long targetUserId, Long targetRoleId,
                       Long targetPermissionId, String traceId) {
        SysSecurityEventEntity event = new SysSecurityEventEntity();
        event.setEventType(type);
        event.setOutcome("SUCCESS");
        event.setActorUserId(actorId);
        event.setTargetUserId(targetUserId);
        event.setTargetRoleId(targetRoleId);
        event.setTargetPermissionId(targetPermissionId);
        event.setTraceId(traceId);
        event.setOccurredAt(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        events.insert(event);
    }
}
