package com.hospital.mes.audit.signature;

import java.time.Instant;

public record NewSignature(long organizationId, long signerId, SignatureMeaning meaning, String objectType,
                           String objectId, String recordDigest, Instant signedAt, String authContextJson,
                           Long revokedSignatureId) {
    public SignatureRecord toRecord(long id) {
        return new SignatureRecord(id, organizationId, signerId, meaning, objectType, objectId, recordDigest,
            signedAt, SignatureStatus.VALID, null, null, authContextJson, revokedSignatureId, 0);
    }
}
