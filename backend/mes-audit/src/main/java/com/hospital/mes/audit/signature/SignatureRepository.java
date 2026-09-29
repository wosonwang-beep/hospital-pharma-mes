package com.hospital.mes.audit.signature;

public interface SignatureRepository {
    SignatureRecord insert(NewSignature signature);
    SignatureRecord find(long organizationId, long signatureId);
    boolean invalidate(long organizationId, long signatureId, long expectedVersion, long actorId,
                       java.time.Instant at, String reason);
}
