package com.hospital.mes.audit.signature;

public interface SignatureRepository {
    SignatureRecord insert(NewSignature signature);
    SignatureRecord find(long organizationId, long signatureId);
    java.util.Optional<SignatureRecord> findLatest(long organizationId, String objectType,
                                                   String objectId, SignatureMeaning meaning);
    java.util.List<SignatureRecord> findValid(long organizationId, String objectType, String objectId);
    boolean invalidate(long organizationId, long signatureId, long expectedVersion, long actorId,
                       java.time.Instant at, String reason);
}
