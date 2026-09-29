package com.hospital.mes.audit.idempotency;

public record IdempotencyCommand(long organizationId, long actorId, String operationCode,
                                 String idempotencyKey, String canonicalRequest) {
    public IdempotencyCommand {
        if (organizationId <= 0 || actorId <= 0) throw new IllegalArgumentException("idempotency actor context is required");
        required(operationCode, 100, "operationCode"); required(idempotencyKey, 128, "idempotencyKey");
        if (canonicalRequest == null) throw new IllegalArgumentException("canonicalRequest is required");
    }
    private static void required(String value, int maximum, String name) {
        if (value == null || value.isBlank() || value.length() > maximum) throw new IllegalArgumentException(name + " is invalid");
    }
}
