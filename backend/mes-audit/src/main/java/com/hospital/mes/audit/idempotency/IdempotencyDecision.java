package com.hospital.mes.audit.idempotency;

public record IdempotencyDecision(IdempotencyDecisionType type, IdempotencyHandle handle,
                                  Integer httpStatus, String responseJson, String resourceType,
                                  String resourceId) {
    public static IdempotencyDecision owner(IdempotencyHandle handle) {
        return new IdempotencyDecision(IdempotencyDecisionType.OWNER, handle, null, null, null, null);
    }
}
