package com.hospital.mes.audit.idempotency;

import java.time.Instant;

public record IdempotencyRecord(long id, long organizationId, long actorId, String operationCode,
                                String idempotencyKey, String requestDigest, IdempotencyState state,
                                Integer httpStatus, String responseJson, String resourceType,
                                String resourceId, Instant expiresAt, long versionNo) {
    public IdempotencyRecord completed(int status, String response, String type, String resource) {
        return new IdempotencyRecord(id, organizationId, actorId, operationCode, idempotencyKey, requestDigest,
            IdempotencyState.COMPLETED, status, response, type, resource, expiresAt, versionNo + 1);
    }
}
