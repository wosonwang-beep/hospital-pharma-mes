package com.hospital.mes.audit.idempotency;

public record IdempotencyHandle(long id, long versionNo) { }
