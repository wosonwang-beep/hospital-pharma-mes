package com.hospital.mes.security.identity;

import java.time.Instant;

public record SecurityEvent(String eventType, String outcome, Long actorUserId,
                            Long targetUserId, Long targetRoleId, String traceId,
                            String requestContext, Instant occurredAt) {
}
