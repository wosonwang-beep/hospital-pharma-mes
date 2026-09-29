package com.hospital.mes.audit.idempotency;

public interface IdempotencyRepository {
    boolean claim(IdempotencyRecord record);
    IdempotencyRecord find(long organizationId, long actorId, String operationCode, String idempotencyKey);
    boolean complete(IdempotencyHandle handle, int httpStatus, String responseJson, String resourceType, String resourceId);
}
